/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.dcm.qrscp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.ExtNegotiation;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.exceptions.NoPresContextException;
import org.dcm4chex.archive.notif.InstancesTransferred;
import org.dcm4chex.archive.perf.PerfCounterEnum;
import org.dcm4chex.archive.perf.PerfMonDelegate;
import org.dcm4chex.archive.perf.PerfPropertyEnum;
import org.dcm4chex.archive.util.FileDataSource;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 3083 $
 * @since 16.09.2003
 */
public class MoveTask implements Runnable {

    private static final String SEND_BUFFER = "SEND_BUFFER";

    private static final String[] NATIVE_LE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian, };

    private static final byte[] RELATIONAL_RETRIEVE = { 1 };

    private static final int PCID = 1;

    private static final String IMAGE = "IMAGE";

    private static final UIDDictionary uidDict = DictionaryFactory
            .getInstance().getDefaultUIDDictionary();
    
    private static final Timer pendingRspTimer = new Timer(true);

    protected final QueryRetrieveScpService service;

    protected final Logger log;

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
    
    private InstancesTransferred instancesTransferred;

    private DcmElement refSOPSeq;

    private Command fwdMoveRspCmd;
    
    private PerfMonDelegate perfMon;

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

    private TimerTask sendPendingRsp = new TimerTask() {

        public void run() {
            if (remaining > 0 || fwdMoveRspCmd != null) {
                notifyMoveSCU(Status.Pending, null, fwdMoveRspCmd);
            }
        }};

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
        this.perfMon = service.getMoveScp().getPerfMonDelegate();
        this.callingAET = service.isForwardAsMoveOriginator() ? moveOriginatorAET
                : moveCalledAET;
        this.priority = moveRqCmd.getInt(Tags.Priority, Command.MEDIUM);
        this.msgID = moveRqCmd.getMessageID();
        this.size = fileInfo.length;
        this.remaining = size;
        this.retrieveInfo = new RetrieveInfo(service, fileInfo);
        if (retrieveInfo.isRetrieveFromLocal()) {
            openAssociation();
        }
        moveAssoc.addCancelListener(msgID, cancelListener);
    }

    private void openAssociation() throws DcmServiceException {
        PDU ac = null;
        Association a = null;
        AssociationFactory asf = AssociationFactory.getInstance();
        try {
            a = asf.newRequestor(service.createSocket(aeData));
            a.setAcTimeout(service.getAcTimeout());
            a.setDimseTimeout(service.getDimseTimeout());
            a.setSoCloseDelay(service.getSoCloseDelay());
            a.putProperty("MoveAssociation", moveAssoc);
            AAssociateRQ rq = asf.newAAssociateRQ();
            rq.setCalledAET(moveDest);
            rq.setCallingAET(moveAssoc.getAssociation().getCalledAET());
            int maxOpsInvoked = service.getMaxStoreOpsInvoked();
            if (maxOpsInvoked != 1) {
                rq.setAsyncOpsWindow(asf.newAsyncOpsWindow(maxOpsInvoked, 1));
            }
            retrieveInfo.addPresContext(rq, service
                    .isSendWithDefaultTransferSyntax(moveDest));
            
            perfMon.assocEstStart(a, Command.C_STORE_RQ);
            
            ac = a.connect(rq);

            perfMon.assocEstEnd(a, Command.C_STORE_RQ);
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
        if (a.countAcceptedPresContext() == 0) {
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
                it.remove(); // Use Iterator itself to remove the current
                                // item to avoid ConcurrentModificationException
                remaining -= iuids.size();
                final String prompt = "No Presentation Context for "
                        + uidDict.toString(cuid) + " accepted by " + moveDest
                        + "\n\tCannot send " + iuids.size()
                        + " instances of this class";
                if (!service.isIgnorableSOPClass(cuid, moveDest)) {
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
        if (service.isSendPendingMoveRSP()) {
            pendingRspTimer.schedule(sendPendingRsp , 0, 
                    service.getPendingMoveRSPInterval());
        }
        try {
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
                        int fwdMsgID = msgID;
                        while (off + b.length < a.length) {
                            System.arraycopy(a, off, b, 0, b.length);
                            ds.putUI(Tags.SOPInstanceUID, b);
                            forwardMove(retrieveAET, cmd, ds, Arrays.asList(b));
                            off += b.length;
                            cmd.putUS(Tags.MessageID, ++fwdMsgID);
                        }
                        b = new String[a.length - off];
                        System.arraycopy(a, off, b, 0, b.length);
                        ds.putUI(Tags.SOPInstanceUID, b);
                        forwardMove(retrieveAET, cmd, ds, Arrays.asList(b));
                    }
                }
            }
        } finally {
            sendPendingRsp.cancel();
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
                    .queryAEData(retrieveAET, null);
            Association a = asf.newRequestor(service
                    .createSocket(retrieveAEData));
            a.setAcTimeout(service.getAcTimeout());
            a.setDimseTimeout(service.getDimseTimeout());
            a.setSoCloseDelay(service.getSoCloseDelay());
            AAssociateRQ rq = asf.newAAssociateRQ();
            rq.setCalledAET(retrieveAEData.getTitle());
            rq.setCallingAET(callingAET);
            rq.addPresContext(asf.newPresContext(PCID,
                    UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                    NATIVE_LE_TS));
            rq.addExtNegotiation(asf.newExtNegotiation(
                    UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                    RELATIONAL_RETRIEVE));
            PDU pdu = a.connect(rq);
            if (!(pdu instanceof AAssociateAC)) {
                throw new IOException("Association not accepted by "
                        + retrieveAEData + ":\n" + pdu);
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
                    log.warn("Relational Retrieve not supported by "
                            + retrieveAEData);
                }
                Dimse cmoverq = asf.newDimse(PCID, moveRqCmd, moveRqData);
                DimseListener moveRspListener = new DimseListener() {
                    public void dimseReceived(Association assoc, Dimse dimse) {
                        try {
                            fwdMoveRspCmd = dimse.getCommand();
                            final Dataset ds = dimse.getDataset();
                            final int status = fwdMoveRspCmd.getStatus();
                            switch (status) {
                            case Status.Pending:
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
                                    String[] a = ds
                                            .getStrings(Tags.FailedSOPInstanceUIDList);
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
                            log.error("Failure during receive of C-MOVE_RSP:",
                                    e);
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
        Association a = storeAssoc.getAssociation();
        Collection localFiles = retrieveInfo.getLocalFiles();
        final Set remainingIUIDs = new HashSet(retrieveInfo.removeLocalIUIDs());
        Iterator it = localFiles.iterator();
        while (a.getState() == Association.ASSOCIATION_ESTABLISHED && !canceled
                && it.hasNext()) {
            final List list = (List) it.next();
            final FileInfo fileInfo = (FileInfo) list.get(0);
            final String iuid = fileInfo.sopIUID;
            DimseListener storeScpListener = new DimseListener() {

                public void dimseReceived(Association assoc, Dimse dimse) {
                    switch (dimse.getCommand().getStatus()) {
                    case Status.Success:
                        ++completed;
                        updateInstancesAction(fileInfo);
                        updateStgCmtActionInfo(fileInfo);
                        updateInstancesTransferred(fileInfo);
                        break;
                    case Status.CoercionOfDataElements:
                    case Status.DataSetDoesNotMatchSOPClassWarning:
                    case Status.ElementsDiscarded:
                        ++warnings;
                        updateInstancesAction(fileInfo);
                        updateStgCmtActionInfo(fileInfo);
                        updateInstancesTransferred(fileInfo);
                        break;
                    default:
                        ++failed;
                        failedIUIDs.add(iuid);
                        break;
                    }
                    remainingIUIDs.remove(iuid);
                    --remaining;
                }
            };
            
            try {
            	Dimse rq = makeCStoreRQ(fileInfo, getByteBuffer(a));
            	perfMon.start(storeAssoc, rq, PerfCounterEnum.C_STORE_SCU_OBJ_OUT );
            	perfMon.setProperty(storeAssoc, rq, PerfPropertyEnum.REQ_DIMSE, rq);
            	perfMon.setProperty(storeAssoc, rq, PerfPropertyEnum.DICOM_FILE, getFile(fileInfo));
            	perfMon.setProperty(storeAssoc, rq, PerfPropertyEnum.STUDY_IUID, fileInfo.studyIUID);

            	storeAssoc.invoke(rq, storeScpListener);
            	
            	perfMon.stop(storeAssoc, rq, PerfCounterEnum.C_STORE_SCU_OBJ_OUT);
            } catch (Exception e) {
                log.error("Exception during move of " + iuid, e);
            }
            if (fileInfo.availability == Availability.ONLINE) // only track
                                                                // access on
                                                                // ONLINE FS
                studyInfos.add(fileInfo.studyIUID + '@' + fileInfo.basedir);
        }
        if (a.getState() == Association.ASSOCIATION_ESTABLISHED) {
            try {
            	perfMon.assocRelStart(a, Command.C_STORE_RQ);
            	
                storeAssoc.release(true);
                
                perfMon.assocRelEnd(a, Command.C_STORE_RQ);
                
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
        if (instancesAction != null) {
            service.logInstancesSent(remoteNode, instancesAction);
        }
        if (instancesTransferred != null) {
            service.sendJMXNotification(instancesTransferred);
        }
        service.updateStudyAccessTime(studyInfos);
        String stgCmtAET = service.getStgCmtAET(moveDest);
        if (stgCmtAET != null && refSOPSeq.countItems() > 0)
            service
                    .queueStgCmtOrder(moveCalledAET, stgCmtAET,
                            stgCmtActionInfo);
    }

    private byte[] getByteBuffer(Association assoc) {
        byte[] buf = (byte[]) assoc.getProperty(SEND_BUFFER);
        if (buf == null) {
            buf = new byte[service.getBufferSize()];
            assoc.putProperty(SEND_BUFFER, buf);
        }
        return buf;
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


    private void updateInstancesTransferred(FileInfo fileInfo) {
        if (instancesTransferred == null) {
            instancesTransferred = new InstancesTransferred(
                    storeAssoc.getAssociation(),
                    moveAssoc.getAssociation(),
                    fileInfo.patID,
                    fileInfo.patName);
        }
        instancesTransferred.addInstance(fileInfo.studyIUID, 
                fileInfo.sopCUID, fileInfo.sopIUID);        
    }
    
    private Dimse makeCStoreRQ(FileInfo info, byte[] buffer) throws Exception {
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
        File f = getFile(info);
        Dataset mergeAttrs = DatasetUtils.fromByteArray(info.patAttrs,
                DatasetUtils.fromByteArray(info.studyAttrs, DatasetUtils
                        .fromByteArray(info.seriesAttrs, DatasetUtils
                                .fromByteArray(info.instAttrs))));
        FileDataSource ds = new FileDataSource(f, mergeAttrs, buffer);
        ds.setWithoutPixeldata(withoutPixeldata);
        return AssociationFactory.getInstance().newDimse(presCtx.pcid(),
                storeRqCmd, ds);
    }
    
    protected File getFile(FileInfo info) throws Exception {
    	return info.basedir.startsWith("tar:") ? service.retrieveFileFromTAR(
                info.basedir, info.fileID) : FileUtils.toFile(info.basedir,
                        info.fileID);
    }

    private void notifyMoveFinished() {
        notifyMoveSCU(canceled ? Status.Cancel : failed == 0 ? Status.Success
                : completed == 0 ? Status.UnableToPerformSuboperations
                        : Status.SubOpsOneOrMoreFailures,
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