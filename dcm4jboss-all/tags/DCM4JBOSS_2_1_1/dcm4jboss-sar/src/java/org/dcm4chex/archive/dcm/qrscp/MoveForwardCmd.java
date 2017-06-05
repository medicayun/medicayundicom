/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.qrscp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.ExtNegotiation;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.exceptions.UnkownAETException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1385 $ $Date: 2005-01-07 05:15:55 +0800 (周五, 07 1月 2005) $
 * @since 20.04.2004
 */
class MoveForwardCmd {

    private static final String IMAGE = "IMAGE";

    private static final int PCID = 1;

    private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian};

    private final QueryRetrieveScpService service;

    private final AEData aeData;

    private final int msgid;

    private final String callingAET;

    private final String destAET;

    private final int priority;

    private final String[] iuids;

    private ActiveAssociation aa;
    
    public MoveForwardCmd(QueryRetrieveScpService service, int msgid, String callingAET,
            String retrieveAET, int priority, String destAET, String[] iuids)
            throws SQLException, UnkownAETException {
        this.service = service;
        this.msgid = msgid;
        this.callingAET = callingAET;
        this.aeData = service.queryAEData(retrieveAET);
        this.destAET = destAET;
        this.iuids = iuids;
        this.priority = priority;
    }
    
    private final byte[] RELATIONAL_RETRIEVE = { 1 };

    
    public void execute(DimseListener moveRspListener)
            throws InterruptedException, IOException {
        Association a = QueryRetrieveScpService.asf.newRequestor(service.createSocket(aeData));
        a.setAcTimeout(service.getAcTimeout());
        AAssociateRQ rq = QueryRetrieveScpService.asf.newAAssociateRQ();
        rq.setCalledAET(aeData.getTitle());
        rq.setCallingAET(callingAET);
        rq.addPresContext(QueryRetrieveScpService.asf.newPresContext(PCID,
                UIDs.StudyRootQueryRetrieveInformationModelMOVE, NATIVE_TS));
        rq.addExtNegotiation(QueryRetrieveScpService.asf.newExtNegotiation(
                UIDs.StudyRootQueryRetrieveInformationModelMOVE, RELATIONAL_RETRIEVE));
        PDU pdu = a.connect(rq);
        if (!(pdu instanceof AAssociateAC)) { throw new IOException(
                "Association not accepted by " + aeData + ":\n" + pdu); }
        AAssociateAC ac = (AAssociateAC) pdu;
        ActiveAssociation aa = QueryRetrieveScpService.asf.newActiveAssociation(a, null);
        aa.start();
        try {
            if (a.getAcceptedTransferSyntaxUID(PCID) == null)
                throw new IOException(
                    "Study Root IM MOVE Service not supported by " + aeData);
            if (!isRelationalRetrieveAccepted(ac.getExtNegotiation(UIDs.StudyRootQueryRetrieveInformationModelMOVE)))
                service.getLog().warn("Relational Retrieve not supported by " + aeData);
            Command cmd = QueryRetrieveScpService.dof.newCommand();
            cmd.initCMoveRQ(msgid,
                    UIDs.StudyRootQueryRetrieveInformationModelMOVE, priority,
                    destAET);
            Dataset ds = QueryRetrieveScpService.dof.newDataset();
            ds.putCS(Tags.QueryRetrieveLevel, IMAGE);
            ds.putUI(Tags.SOPInstanceUID, iuids);
            Dimse cmoverq = QueryRetrieveScpService.asf.newDimse(PCID, cmd, ds);
            aa.invoke(cmoverq, moveRspListener);
        } finally {
            try {
                aa.release(true);
                // workaround to ensure that the final MOVE-RSP of forwarded
                // MOVE-RQ is processed before the final MOVE-RSP is sent
                // to the primary Move Originator
                Thread.sleep(10); 
            } catch (Exception ignore) {
            }
        }
    }

    private boolean isRelationalRetrieveAccepted(ExtNegotiation extNeg) {
        return extNeg != null && Arrays.equals(extNeg.info(), RELATIONAL_RETRIEVE);
    }

    public void cancel() throws IOException {
        if (aa == null)
            return;
        Command cmd = QueryRetrieveScpService.dof.newCommand();
        cmd.initCCancelRQ(msgid);
        Dimse ccancelrq = QueryRetrieveScpService.asf.newDimse(PCID, cmd);
        aa.getAssociation().write(ccancelrq);
    }
}