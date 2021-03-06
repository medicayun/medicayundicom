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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAbort;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DataSource;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.ExtNegotiation;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.exceptions.NoPresContextException;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileDataSource;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1838 $
 * @since 16.09.2003
 */
class MoveTask implements Runnable {

    private static final String[] NATIVE_LE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian, };

    private static final byte[] RELATIONAL_RETRIEVE = { 1 };

    private static final int PCID = 1;

    private static final String IMAGE = "IMAGE";

    private static final Comparator ASC_FILE_PK = new Comparator() {

        public int compare(Object o1, Object o2) {
            FileInfo fi1 = (FileInfo) o1;
            FileInfo fi2 = (FileInfo) o2;
            return fi1.pk - fi2.pk;
        }
    };

    private static final UIDDictionary uidDict = DictionaryFactory
            .getInstance().getDefaultUIDDictionary();

    private final QueryRetrieveScpService service;

    private final Logger log;

    private final String moveDest;

    private final AEData aeData;

    private final int movePcid;

    private final Command moveRqCmd;

    private final Dataset moveRqData;

    private final String moveOriginatorAET;

    private final String moveCalledAET;

    private final String callingAET;

    private final int priority;

    private final int msgID;

    private final boolean withoutPixeldata;

    private ActiveAssociation moveAssoc;

    private final ArrayList failedIUIDs = new ArrayList();

    private final int size;

    private int failed = 0;

    private int warnings = 0;

    private int completed = 0;

    private int remaining = 0;

    private boolean canceled = false;

    private ActiveAssociation storeAssoc;

    private ActiveAssociation forwardAssoc;

    private RemoteNode remoteNode;

    private InstancesAction instancesAction;

    private RetrieveInfo retrieveInfo;

    private Dataset stgCmtActionInfo;

    private DcmElement refSOPSeq;

    private DimseListener cancelListener = new DimseListener() {

        public void dimseReceived(Association assoc, Dimse dimse) {
            canceled = true;
            if (forwardAssoc != null) {
                try {
                    Command cmd = DcmObjectFactory.getInstance().newCommand();
                    cmd.initCCancelRQ(msgID);
                    Dimse ccancelrq = AssociationFactory.getInstance()
                            .newDimse(PCID, cmd);
                    forwardAssoc.getAssociation().write(ccancelrq);
                } catch (IOException e) {
                    log.warn("Failed to forward C-CANCEL-RQ:", e);
                }
            }
        }
    };

    public MoveTask(QueryRetrieveScpService service,
            ActiveAssociation moveAssoc, int movePcid, Command moveRqCmd,
            Dataset moveRqData, FileInfo[][] fileInfo, AEData aeData,
            String moveDest) throws DcmServiceException {
        this.service = service;
        this.log = service.getLog();
        this.moveAssoc = moveAssoc;
        this.movePcid = movePcid;
        this.moveRqCmd = moveRqCmd;
        this.moveRqData = moveRqData;
        this.aeData = aeData;
        this.moveDest = moveDest;
        this.withoutPixeldata = service.isWithoutPixelData(moveDest);
        this.moveOriginatorAET = moveAssoc.getAssociation().getCallingAET();
        this.moveCalledAET = moveAssoc.getAssociation().getCalledAET();
        this.callingAET = service.isForwardAsMoveOriginator() ? moveOriginatorAET
                : moveCalledAET;
        this.priority = moveRqCmd.getInt(Tags.Priority, Command.MEDIUM);
        this.msgID = moveRqCmd.getMessageID();
        this.size = fileInfo.length;
        if ((remaining = size) > 0) {
            retrieveInfo = new RetrieveInfo(service, fileInfo);
            if (retrieveInfo.isRetrieveFromLocal()) {
                openAssociation();
            }
            moveAssoc.addCancelListener(msgID, cancelListener);
        }
    }

    private void openAssociation() throws DcmServiceException {
        PDU ac = null;
        Association a = null;
        AssociationFactory asf = AssociationFactory.getInstance();
        try {
            a = asf.newRequestor(service.createSocket(aeData));
            a.setAcTimeout(service.getAcTimeout());
            AAssociateRQ rq = asf.newAAssociateRQ();
            rq.setCalledAET(moveDest);
            rq.setCallingAET(moveAssoc.getAssociation().getCalledAET());
            retrieveInfo.addPresContext(rq);
            ac = a.connect(rq);
        } catch (IOException e) {
            final String prompt = "Failed to connect " + moveDest;
            log.error(prompt, e);
            throw new DcmServiceException(Status.UnableToPerformSuboperations,
                    prompt, e);
        }
        if (!(ac instanceof AAssociateAC)) {
            final String prompt = "Association not accepted by " + moveDest
                    + ":\n" + ac;
            log.error(prompt);
            throw new DcmServiceException(Status.UnableToPerformSuboperations,
                    prompt);
        }
        storeAssoc = asf.newActiveAssociation(a, null);
        storeAssoc.start();
        final boolean ignoreUnsupportedSOPClassFailures = service
                .isIgnoreUnsupportedSOPClassFailures(moveDest);
        if (!ignoreUnsupportedSOPClassFailures
                && a.countAcceptedPresContext() == 0) {
            try {
                storeAssoc.release(false);
            } catch (Exception e) {
                log.info("Exception during release of assocation to "
                        + moveDest, e);
            }
            final String prompt = "No Presentation Context for Storage accepted by "
                    + moveDest;
            log.error(prompt);
            throw new DcmServiceException(Status.UnableToPerformSuboperations,
                    prompt);
        }
        Iterator it = retrieveInfo.getCUIDs();
        String cuid;
        Set iuids;
        while (it.hasNext()) {
            cuid = (String) it.next();
            if (a.listAcceptedPresContext(cuid).isEmpty()) {
                iuids = retrieveInfo.removeInstancesOfClass(cuid);
                remaining -= iuids.size();
                final String prompt = "No Presentation Context for "
                        + uidDict.toString(cuid) + " accepted by " + moveDest
                        + "\n\tCannot send " + iuids.size()
                        + " instances of this class";
                if (!ignoreUnsupportedSOPClassFailures) {
                    failedIUIDs.addAll(iuids);
                    failed += iuids.size();
                    log.warn(prompt);
                } else {
                    completed += iuids.size();
                    log.info(prompt);
                }
            }
        }
        AuditLoggerFactory alf = AuditLoggerFactory.getInstance();
        remoteNode = alf.newRemoteNode(storeAssoc.getAssociation().getSocket(),
                storeAssoc.getAssociation().getCalledAET());

    }

    public void run() {
        if (retrieveInfo.isRetrieveFromLocal()) {
            retrieveLocal();
        }
        while (!canceled && retrieveInfo.nextMoveForward()) {
            String retrieveAET = retrieveInfo.getMoveForwardAET();
            Collection iuids = retrieveInfo.getMoveForwardUIDs();
            if (iuids.size() == size) {
                if (log.isDebugEnabled())
                    log.debug("Forward original Move RQ to " + retrieveAET);
                forwardMove(retrieveAET, moveRqCmd, moveRqData, iuids);
            } else {
                DcmObjectFactory dof = DcmObjectFactory.getInstance();
                Command cmd = dof.newCommand();
                cmd.initCMoveRQ(msgID,
                        UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                        priority, moveDest);
                Dataset ds = dof.newDataset();
                ds.putCS(Tags.QueryRetrieveLevel, IMAGE);
                String[] a = (String[]) iuids.toArray(new String[iuids.size()]);
                if (a.length <= service.getMaxUIDsPerMoveRQ()) {
                    ds.putUI(Tags.SOPInstanceUID, a);
                    forwardMove(retrieveAET, cmd, ds, iuids);                    
                } else {
                    String[] b = new String[service.getMaxUIDsPerMoveRQ()];
                    int off = 0;
                    while (off + b.length < a.length) {
                        System.arraycopy(a, off, b, 0, b.length);
                        ds.putUI(Tags.SOPInstanceUID, b);
                        forwardMove(retrieveAET, cmd, ds, Arrays.asList(b));
                        off += b.length;
                    }
                    b = new String[a.length - off];
                    System.arraycopy(a, off, b, 0, b.length);
                    ds.putUI(Tags.SOPInstanceUID, b);
                    forwardMove(retrieveAET, cmd, ds, Arrays.asList(b));
                }
            }
        }
        notifyMoveFinished();
    }

    private void forwardMove(String retrieveAET, Command moveRqCmd,
            Dataset moveRqData, final Collection iuids) {
        remaining -= iuids.size();
        final boolean[] receivedFinalRsp = { false };
        try {
            final AssociationFactory asf = AssociationFactory.getInstance();
            final AEData retrieveAEData = service
                    .queryAEData(retrieveAET);
			Association a = asf.newRequestor(service.createSocket(retrieveAEData));
            a.setAcTimeout(service.getAcTimeout());
            AAssociateRQ rq = asf.newAAssociateRQ();
            rq.setCalledAET(retrieveAEData.getTitle());
            rq.setCallingAET(callingAET);
            String asuid = moveRqCmd.getAffectedSOPClassUID();
            rq.addPresContext(asf.newPresContext(PCID,
                    UIDs.StudyRootQueryRetrieveInformationModelMOVE, NATIVE_LE_TS));
            rq.addExtNegotiation(asf.newExtNegotiation(
                    UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                    RELATIONAL_RETRIEVE));
            PDU pdu = a.connect(rq);
            if (!(pdu instanceof AAssociateAC)) {
                throw new IOException("Association not accepted by " + retrieveAEData
                        + ":\n" + pdu);
            }
            AAssociateAC ac = (AAssociateAC) pdu;
            forwardAssoc = asf.newActiveAssociation(a, null);
            forwardAssoc.start();
            try {
                if (a.getAcceptedTransferSyntaxUID(PCID) == null)
                    throw new IOException(
                            "StudyRootQueryRetrieveInformationModelMOVE not supported by "
                                    + retrieveAEData);
                ExtNegotiation extNeg = ac
                        .getExtNegotiation(UIDs.StudyRootQueryRetrieveInformationModelMOVE);
                if (extNeg == null
                        || !Arrays.equals(extNeg.info(), RELATIONAL_RETRIEVE)) {
                    log.warn("Relational Retrieve not supported by " + retrieveAEData);
                }
                Dimse cmoverq = asf.newDimse(PCID, moveRqCmd, moveRqData);
                DimseListener moveRspListener = new DimseListener() {
                    public void dimseReceived(Association assoc, Dimse dimse) {
                        try {
                            final Command fwdMoveRspCmd = dimse.getCommand();
                            final Dataset ds = dimse.getDataset();
                            final int status = fwdMoveRspCmd.getStatus();
                            switch (status) {
                                case Status.Pending:
                                    notifyMovePending(fwdMoveRspCmd);
                                    return;
                                case Status.Cancel:
                                case Status.UnableToPerformSuboperations:
                                    remaining += fwdMoveRspCmd.getInt(
                                            Tags.NumberOfRemainingSubOperations, 0);
                                case Status.Success:
                                case Status.SubOpsOneOrMoreFailures:
                                    failed += fwdMoveRspCmd.getInt(
                                            Tags.NumberOfFailedSubOperations, 0);
                                    completed += fwdMoveRspCmd.getInt(
                                            Tags.NumberOfCompletedSubOperations, 0);
                                    warnings += fwdMoveRspCmd.getInt(
                                            Tags.NumberOfWarningSubOperations, 0);
                                    if (ds != null) {
                                        String[] a = ds.getStrings(Tags.FailedSOPInstanceUIDList);
                                        if (a != null && a.length != 0) {
                                            failedIUIDs.addAll(Arrays.asList(a));
                                        } else {
                                            failedIUIDs.addAll(iuids);                                        
                                        }
                                    }
                                    break;
                                default: // other error status
                                    failed += iuids.size();
                                    failedIUIDs.addAll(iuids);                                    
                            }
                            receivedFinalRsp[0] = true;
                        } catch (IOException e) {
                            failed += iuids.size();
                            failedIUIDs.addAll(iuids);
                            log.error("Failure during receive of C-MOVE_RSP:", e);
                        }
                    }
                };
                forwardAssoc.invoke(cmoverq, moveRspListener);
            } finally {
                try {
                    forwardAssoc.release(true);
                    // workaround to ensure that the final MOVE-RSP of forwarded
                    // MOVE-RQ is processed before the final MOVE-RSP is sent
                    // to the primary Move Originator
                    Thread.sleep(10);
                } catch (Exception ignore) {
                }
                forwardAssoc = null;
            }
            if (!receivedFinalRsp[0]) {
                failed += iuids.size();
                failedIUIDs.addAll(iuids);
                log.error("No final MOVE RSP received from " + retrieveAET);            
            }
        } catch (Exception e) {
            failed += iuids.size();
            failedIUIDs.addAll(iuids);
            log.error("Failed to forward MOVE RQ to " + retrieveAET, e);            
        }
    }
    
    private void retrieveLocal() {
        this.stgCmtActionInfo = DcmObjectFactory.getInstance().newDataset();
        this.refSOPSeq = stgCmtActionInfo.putSQ(Tags.RefSOPSeq);
        Set studyInfos = new HashSet();
        byte[] buffer = withoutPixeldata ? null : new byte[service
                .getBufferSize()];
        Association a = storeAssoc.getAssociation();
        Collection localFiles = retrieveInfo.getLocalFiles();
        final Set remainingIUIDs = new HashSet(retrieveInfo.getLocalIUIDs());
        Iterator it = localFiles.iterator();
        while (a.getState() == Association.ASSOCIATION_ESTABLISHED && !canceled
                && it.hasNext()) {
            final List list = (List) it.next();
            final FileInfo fileInfo = (FileInfo) (service
                    .isRetrieveLastReceived() ? Collections.max(list,
                    ASC_FILE_PK) : Collections.min(list, ASC_FILE_PK));
            final String iuid = fileInfo.sopIUID;
            DimseListener storeScpListener = new DimseListener() {

                public void dimseReceived(Association assoc, Dimse dimse) {
                    switch (dimse.getCommand().getStatus()) {
                    case Status.Success:
                        ++completed;
                        updateInstancesAction(fileInfo);
                        updateStgCmtActionInfo(fileInfo);
                        break;
                    case Status.CoercionOfDataElements:
                    case Status.DataSetDoesNotMatchSOPClassWarning:
                    case Status.ElementsDiscarded:
                        ++warnings;
                        updateInstancesAction(fileInfo);
                        updateStgCmtActionInfo(fileInfo);
                        break;
                    default:
                        ++failed;
                        failedIUIDs.add(iuid);
                        break;
                    }
                    remainingIUIDs.remove(iuid);
                    if (--remaining > 0) {
                        notifyMovePending(null);
                    }
                }
            };
            try {
                storeAssoc.invoke(makeCStoreRQ(fileInfo, buffer),
                        storeScpListener);
            } catch (Exception e) {
                log.error("Exception during move of " + iuid, e);
            }
            studyInfos.add(fileInfo.studyIUID + '@' + fileInfo.basedir);
        }
        if (a.getState() == Association.ASSOCIATION_ESTABLISHED) {
            try {
                storeAssoc.release(true);
                // workaround to ensure that last STORE-RSP is processed before
                // finally MOVE-RSP is sent
                Thread.sleep(10);
            } catch (Exception e) {
                log.error("Exception during release:", e);
            }
        } else {
            try {
                a.abort(AssociationFactory.getInstance().newAAbort(
                        AAbort.SERVICE_PROVIDER, AAbort.REASON_NOT_SPECIFIED));
            } catch (IOException ignore) {
            }
        }
        if (!canceled) {
            remaining -= remainingIUIDs.size();
            failed += remainingIUIDs.size();
            failedIUIDs.addAll(remainingIUIDs);
        }
        service.logInstancesSent(remoteNode, instancesAction);
        updateStudyAccessTime(studyInfos);
        String stgCmtAET = service.getStgCmtAET(moveDest);
        if (stgCmtAET != null && refSOPSeq.vm() > 0)
            service
                    .queueStgCmtOrder(moveCalledAET, stgCmtAET,
                            stgCmtActionInfo);
    }

    private FileSystemMgtHome getFileSystemMgtHome()
            throws HomeFactoryException {
        return (FileSystemMgtHome) EJBHomeFactory.getFactory().lookup(
                FileSystemMgtHome.class, FileSystemMgtHome.JNDI_NAME);
    }

    private void updateStudyAccessTime(Set studyInfos) {
        FileSystemMgt fsMgt;
        try {
            fsMgt = getFileSystemMgtHome().create();
        } catch (Exception e) {
            log.fatal("Failed to access FileSystemMgt EJB");
            return;
        }
        try {
            for (Iterator it = studyInfos.iterator(); it.hasNext();) {
                String studyInfo = (String) it.next();
                int delim = studyInfo.indexOf('@');
                try {
                    fsMgt.touchStudyOnFileSystem(studyInfo.substring(0, delim),
                            studyInfo.substring(delim + 1));
                } catch (Exception e) {
                    log.warn("Failed to update access time for study "
                            + studyInfo, e);
                }
            }
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }
    }

    private void updateInstancesAction(final FileInfo info) {
        if (instancesAction == null) {
            AuditLoggerFactory alf = AuditLoggerFactory.getInstance();
            instancesAction = alf.newInstancesAction("Access", info.studyIUID,
                    alf.newPatient(info.patID, info.patName));
            instancesAction.setUser(alf.newRemoteUser(alf.newRemoteNode(
                    moveAssoc.getAssociation().getSocket(), moveAssoc
                            .getAssociation().getCallingAET())));
        } else {
            instancesAction.addStudyInstanceUID(info.studyIUID);
        }
        instancesAction.addSOPClassUID(info.sopCUID);
        instancesAction.incNumberOfInstances(1);
    }

    private void updateStgCmtActionInfo(FileInfo fileInfo) {
        Dataset item = refSOPSeq.addNewItem();
        item.putUI(Tags.RefSOPClassUID, fileInfo.sopCUID);
        item.putUI(Tags.RefSOPInstanceUID, fileInfo.sopIUID);
    }

    private Dimse makeCStoreRQ(FileInfo info, byte[] buffer)
            throws NoPresContextException {
        Association assoc = storeAssoc.getAssociation();
        PresContext presCtx = assoc.getAcceptedPresContext(info.sopCUID,
                info.tsUID);
        if (presCtx == null) {
            presCtx = assoc.getAcceptedPresContext(info.sopCUID,
                    UIDs.ExplicitVRLittleEndian);
            if (presCtx == null) {
                presCtx = assoc.getAcceptedPresContext(info.sopCUID,
                        UIDs.ImplicitVRLittleEndian);
                if (presCtx == null)
                    throw new NoPresContextException(
                            "No Presentation Context for "
                                    + uidDict.toString(info.sopCUID)
                                    + " accepted by " + moveDest);
            }
        }
        Command storeRqCmd = DcmObjectFactory.getInstance().newCommand();
        storeRqCmd.initCStoreRQ(assoc.nextMsgID(), info.sopCUID, info.sopIUID,
                priority);
        storeRqCmd.putUS(Tags.MoveOriginatorMessageID, msgID);
        storeRqCmd.putAE(Tags.MoveOriginatorAET, moveOriginatorAET);
        DataSource ds = new FileDataSource(service.getLog(), info, buffer);
        return AssociationFactory.getInstance().newDimse(presCtx.pcid(),
                storeRqCmd, ds);
    }

    private void notifyMovePending(Command fwdMoveRspCmd) {
        if (service.isSendPendingMoveRSP()) {
            notifyMoveSCU(Status.Pending, null, fwdMoveRspCmd);
        }
    }

    private void notifyMoveFinished() {
        notifyMoveSCU(canceled ? Status.Cancel
                : failed > 0 ? Status.SubOpsOneOrMoreFailures : Status.Success,
                makeMoveRspIdentifier(), null);
    }

    private Dataset makeMoveRspIdentifier() {
        if (failed == 0)
            return null;
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        if (failed == failedIUIDs.size()) {
            String[] a = (String[]) failedIUIDs.toArray(new String[failedIUIDs
                    .size()]);
            ds.putUI(Tags.FailedSOPInstanceUIDList, a);
            // check if 64k limit for UI attribute is reached
            if (ds.get(Tags.FailedSOPInstanceUIDList).length() < 0x10000)
                return ds;
            log
                    .warn("Failed SOP InstanceUID List exceeds 64KB limit - send empty attribute instead");
        }
        ds.putUI(Tags.FailedSOPInstanceUIDList);
        return ds;
    }

    private void notifyMoveSCU(int status, Dataset ds, Command fwdMoveRspCmd) {
        if (moveAssoc != null) {
            Command cmd = fwdMoveRspCmd != null ? makeMoveRsp(fwdMoveRspCmd)
                    : makeMoveRsp(status);
            try {
                moveAssoc.getAssociation().write(
                        AssociationFactory.getInstance().newDimse(movePcid,
                                cmd, ds));
            } catch (Exception e) {
                log.info("Failed to send Move RSP to Move Originator:", e);
                moveAssoc = null;
            }
        }
    }

    private Command makeMoveRsp(Command fwdMoveRspCmd) {
        Command rspCmd = DcmObjectFactory.getInstance().newCommand();
        rspCmd.initCMoveRSP(moveRqCmd.getMessageID(), moveRqCmd
                .getAffectedSOPClassUID(), fwdMoveRspCmd.getStatus());
        rspCmd.putUS(Tags.NumberOfRemainingSubOperations, remaining
                + fwdMoveRspCmd.getInt(Tags.NumberOfRemainingSubOperations, 0));
        rspCmd.putUS(Tags.NumberOfCompletedSubOperations, completed
                + fwdMoveRspCmd.getInt(Tags.NumberOfCompletedSubOperations, 0));
        rspCmd.putUS(Tags.NumberOfWarningSubOperations, warnings
                + fwdMoveRspCmd.getInt(Tags.NumberOfWarningSubOperations, 0));
        rspCmd.putUS(Tags.NumberOfFailedSubOperations, failed
                + fwdMoveRspCmd.getInt(Tags.NumberOfFailedSubOperations, 0));
        return rspCmd;
    }

    private Command makeMoveRsp(int status) {
        Command rspCmd = DcmObjectFactory.getInstance().newCommand();
        rspCmd.initCMoveRSP(moveRqCmd.getMessageID(), moveRqCmd
                .getAffectedSOPClassUID(), status);
        if (remaining > 0) {
            rspCmd.putUS(Tags.NumberOfRemainingSubOperations, remaining);
        } else {
            rspCmd.remove(Tags.NumberOfRemainingSubOperations);
        }
        rspCmd.putUS(Tags.NumberOfCompletedSubOperations, completed);
        rspCmd.putUS(Tags.NumberOfWarningSubOperations, warnings);
        rspCmd.putUS(Tags.NumberOfFailedSubOperations, failed);
        return rspCmd;
    }

}