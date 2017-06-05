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
 * @version $Revision: 1533 $ $Date: 2005-02-15 02:36:57 +0800 (周二, 15 2月 2005) $
 * @since 20.04.2004
 */
class MoveForwardCmd {

    private static final int PCID = 1;

    private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian};

    private final QueryRetrieveScpService service;

    private final AEData aeData;

    private final String callingAET;

    private ActiveAssociation aa;

	private final Command moveRqCmd;

	private final Dataset moveRqData;
    
	public MoveForwardCmd(QueryRetrieveScpService service, String callingAET,
			String retrieveAET, Command moveRqCmd, Dataset moveRqData)
			throws SQLException, UnkownAETException {
        this.service = service;
        this.callingAET = callingAET;
        this.aeData = service.queryAEData(retrieveAET);
        this.moveRqCmd = moveRqCmd;
        this.moveRqData = moveRqData;
	}

	private final byte[] RELATIONAL_RETRIEVE = { 1 };

    
    public void execute(DimseListener moveRspListener)
            throws InterruptedException, IOException {    	
        Association a = QueryRetrieveScpService.asf.newRequestor(service.createSocket(aeData));
        a.setAcTimeout(service.getAcTimeout());
        AAssociateRQ rq = QueryRetrieveScpService.asf.newAAssociateRQ();
        rq.setCalledAET(aeData.getTitle());
        rq.setCallingAET(callingAET);
        String asuid = moveRqCmd.getAffectedSOPClassUID();
        rq.addPresContext(QueryRetrieveScpService.asf.newPresContext(PCID,
        		asuid, NATIVE_TS));
        rq.addExtNegotiation(QueryRetrieveScpService.asf.newExtNegotiation(
        		asuid, RELATIONAL_RETRIEVE));
        PDU pdu = a.connect(rq);
        if (!(pdu instanceof AAssociateAC)) { throw new IOException(
                "Association not accepted by " + aeData + ":\n" + pdu); }
        AAssociateAC ac = (AAssociateAC) pdu;
        ActiveAssociation aa = QueryRetrieveScpService.asf.newActiveAssociation(a, null);
        aa.start();
        try {
            if (a.getAcceptedTransferSyntaxUID(PCID) == null)
                throw new IOException(
                    "MOVE Service " + asuid + " not supported by " + aeData);
            if (!isRelationalRetrieveAccepted(ac.getExtNegotiation(asuid)))
                service.getLog().warn("Relational Retrieve not supported by " + aeData);
            Dimse cmoverq = QueryRetrieveScpService.asf.newDimse(PCID, moveRqCmd, moveRqData);
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
        cmd.initCCancelRQ(moveRqCmd.getMessageID());
        Dimse ccancelrq = QueryRetrieveScpService.asf.newDimse(PCID, cmd);
        aa.getAssociation().write(ccancelrq);
    }
}