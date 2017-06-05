/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.movescu;

import java.io.IOException;
import java.sql.SQLException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.jboss.logging.Logger;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1645 $ $Date: 2005-03-27 00:28:54 +0800 (周日, 27 3月 2005) $
 * @since 26.08.2004
 *
 */
class MoveCmd implements Runnable {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final AssociationFactory af = AssociationFactory
            .getInstance();

    private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian};

    private static final int INVOKE_FAILED_STATUS = -1;

    private static final int PCID = 1;

    private final MoveScuService service;

    private final Logger log;

    private final MoveOrder order;
    
    private int status = INVOKE_FAILED_STATUS;

    public MoveCmd(final MoveScuService service, MoveOrder order) {
        this.service = service;
        this.log = service.getLog();
        this.order = order;
    }

    public void run() {
        log.info("Start processing " + order);
        ActiveAssociation moveAssoc = null;
        try {
            String retrieveAET = order.getRetrieveAET();
            if (retrieveAET == null)
                retrieveAET = service.getCalledAET();
            moveAssoc = openAssociation(queryAEData(retrieveAET));
            Command cmd = dof.newCommand();
            cmd.initCMoveRQ(moveAssoc.getAssociation().nextMsgID(),
                    UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                    order.getPriority(),
                    order.getMoveDestination());
            Dataset ds = dof.newDataset();
            ds.putCS(Tags.QueryRetrieveLevel, order.getQueryRetrieveLevel());
            putUI(ds, Tags.StudyInstanceUID, order.getStudyIuids());
            putUI(ds, Tags.StudyInstanceUID, order.getStudyIuids());
            putUI(ds, Tags.SeriesInstanceUID, order.getSeriesIuids());
            putUI(ds, Tags.SOPInstanceUID, order.getSopIuids());
            service.logDataset("Identifier:\n", ds);
            Dimse moveRsp = moveAssoc.invoke(af.newDimse(PCID, cmd, ds)).get();
            Command moveRspCmd = moveRsp.getCommand();
            status = moveRspCmd.getStatus();
            if (status == Status.SubOpsOneOrMoreFailures && order.getSopIuids() != null) {
                Dataset moveRspData = moveRsp.getDataset();
                if (moveRspData != null) {
                    String[] failedUIDs = ds.getStrings(Tags.FailedSOPInstanceUIDList);
                    if (failedUIDs != null && failedUIDs.length != 0) {
                        order.setSopIuids(failedUIDs);
                    }
                }                
            }
        } catch (Exception e) {
            log.warn("Failed to invoke C-MOVE-RQ for " + order, e);
        } finally {
            if (moveAssoc != null) try {
                moveAssoc.release(true);
                // workaround to ensure that the final MOVE-RSP is processed
                // before to continue 
                Thread.sleep(10);
            } catch (Exception e) {
                log.warn("Failed to release " + moveAssoc.getAssociation());
            }            
        }
        if (status != Status.Success) {
            order.setFailureStatus(status);
            order.setFailureCount(order.getFailureCount()+1);
            service.queueFailedMoveOrder(order);
        }
    }

    private static void putUI(Dataset ds, int tag, String[] uids) {
        if (uids != null) ds.putUI(tag, uids);
    }

    private AEData queryAEData(String aet) throws SQLException,
            UnkownAETException {
        AECmd aeCmd = new AECmd(aet);
        AEData aeData = aeCmd.execute();
        if (aeData == null) { throw new UnkownAETException(aet); }
        return aeData;
    }

    private ActiveAssociation openAssociation(AEData moveSCP)
            throws IOException {
        Association a = af.newRequestor(service.createSocket(moveSCP));
        a.setAcTimeout(service.getAcTimeout());
        a.setDimseTimeout(service.getDimseTimeout());
        a.setSoCloseDelay(service.getSoCloseDelay());
        AAssociateRQ rq = af.newAAssociateRQ();
        rq.setCalledAET(moveSCP.getTitle());
        rq.setCallingAET(service.getCallingAET());
        rq.addPresContext(af.newPresContext(PCID,
                UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                NATIVE_TS));
        PDU ac = a.connect(rq);
        if (!(ac instanceof AAssociateAC)) { throw new IOException(
                "Association not accepted by " + moveSCP); }
        ActiveAssociation aa = af.newActiveAssociation(a, null);
        aa.start();
        if (a.getAcceptedTransferSyntaxUID(PCID) == null) {
            try {
                aa.release(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            throw new IOException(
                    "Presentation Context for Retrieve rejected by " + moveSCP);
        }
        return aa;
    }

}