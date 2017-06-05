/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DataSource;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1110 $
 * @since 16.09.2003
 */
class MoveTask implements Runnable {

    private final static AuditLoggerFactory alf = AuditLoggerFactory
            .getInstance();

    private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian};

    private static final String[][] PROPOSED_TS = { NATIVE_TS,
    //            new String[] { UIDs.JPEG2000Lossless },
    //            new String[] { UIDs.JPEGLossless },
    //            new String[] { UIDs.JPEG2000Lossy },
    //            new String[] { UIDs.JPEGBaseline },
    //            new String[] { UIDs.JPEGExtended },
    };

    private static final AssociationFactory af = AssociationFactory
            .getInstance();

    private static final DcmObjectFactory of = DcmObjectFactory.getInstance();

    private static final UIDDictionary uidDict = DictionaryFactory
            .getInstance().getDefaultUIDDictionary();

    private static int defaultBufferSize = 2048;

    private final QueryRetrieveScpService service;

    private final byte[] buffer = new byte[defaultBufferSize];

    private final String moveDest;

    private final AEData aeData;

    private final int movePcid;

    private final Command moveRqCmd;

    private final String moveOriginatorAET;

    private ActiveAssociation moveAssoc;

    private final ArrayList failedIUIDs = new ArrayList();

    private int warnings = 0;

    private int completed = 0;

    private int remaining = 0;

    private boolean canceled = false;

    private ActiveAssociation storeAssoc;

    private InstancesAction instancesAction;

    private RemoteNode remoteNode;

    private final ArrayList toRetrieve = new ArrayList();

    private final ArrayList toForward = new ArrayList();

    private MoveForwardCmd moveForwardCmd;

    private DimseListener cancelListener = new DimseListener() {

        public void dimseReceived(Association assoc, Dimse dimse) {
            canceled = true;
            if (moveForwardCmd != null) {
                try {
                    moveForwardCmd.cancel();
                } catch (IOException e) {
                    service.getLog().warn("Failed to forward C-CANCEL-RQ:", e);
                }
            }
        }
    };

    public MoveTask(QueryRetrieveScpService service,
            ActiveAssociation moveAssoc, int movePcid, Command moveRqCmd,
            FileInfo[][] fileInfo, AEData aeData, String moveDest)
            throws DcmServiceException {
        this.service = service;
        this.moveAssoc = moveAssoc;
        this.movePcid = movePcid;
        this.moveRqCmd = moveRqCmd;
        this.aeData = aeData;
        this.moveDest = moveDest;
        this.moveOriginatorAET = moveAssoc.getAssociation().getCallingAET();
        if ((remaining = fileInfo.length) > 0) {
            notifyMovePending(null);
            prepareRetrieveInfo(fileInfo);
            if (!toRetrieve.isEmpty()) {
                openAssociation();
                initInstancesAction(fileInfo[0][0]);
            }
            moveAssoc.addCancelListener(moveRqCmd.getMessageID(),
                    cancelListener);
        }
    }

    private void initInstancesAction(FileInfo info) {
        instancesAction = alf.newInstancesAction("Access", info.studyIUID, alf
                .newPatient(info.patID, info.patName));
        instancesAction.setUser(alf.newRemoteUser(alf.newRemoteNode(moveAssoc
                .getAssociation().getSocket(), moveAssoc.getAssociation()
                .getCallingAET())));
        remoteNode = alf.newRemoteNode(storeAssoc.getAssociation().getSocket(),
                storeAssoc.getAssociation().getCalledAET());
    }

    private void openAssociation() throws DcmServiceException {
        PDU ac = null;
        Association a = null;
        try {
            a = af.newRequestor(service.createSocket(aeData));
            a.setAcTimeout(service.getAcTimeout());
            ac = a.connect(createAAssociateRQ());
        } catch (IOException e) {
            final String prompt = "Failed to connect " + moveDest;
            service.getLog().error(prompt, e);
            throw new DcmServiceException(Status.UnableToPerformSuboperations,
                    prompt, e);
        }
        if (!(ac instanceof AAssociateAC)) {
            final String prompt = "Association not accepted by " + moveDest
                    + ":\n" + ac;
            service.getLog().error(prompt);
            throw new DcmServiceException(Status.UnableToPerformSuboperations,
                    prompt);
        }
        storeAssoc = af.newActiveAssociation(a, null);
        storeAssoc.start();
    }

    private AAssociateRQ createAAssociateRQ() {
        AAssociateRQ rq = af.newAAssociateRQ();
        rq.setCalledAET(moveDest);
        rq.setCallingAET(moveAssoc.getAssociation().getCalledAET());

        HashSet cuidSet = new HashSet();
        for (int i = 0, n = toRetrieve.size(); i < n; i++) {
            final String cuid = ((FileInfo) toRetrieve.get(i)).sopCUID;
            if (cuidSet.add(cuid)) {
                for (int j = 0; j < PROPOSED_TS.length; j++) {
                    rq.addPresContext(af.newPresContext(rq.nextPCID(), cuid,
                            PROPOSED_TS[j]));
                }
            }
        }
        return rq;
    }

    public void run() {
        if (!toRetrieve.isEmpty()) {
            retrieveLocal();
        }
        for (int i = 0, n = toForward.size(); !canceled && i < n; ++i) {
            Map.Entry entry = (Entry) toForward.get(i);
            final String retrieveAET = (String) entry.getKey();
            final Set iuids = (Set) entry.getValue();
            final int size = iuids.size();
            remaining -= iuids.size();
            DimseListener fwdmoveRspListener = new DimseListener() {

                public void dimseReceived(Association assoc, Dimse dimse) {
                    try {
                        final Command fwdMoveRspCmd = dimse.getCommand();
                        final Dataset ds = dimse.getDataset();
                        final int status = fwdMoveRspCmd.getStatus();
                        switch (status) {
                        case Status.Pending:
                            if (fwdMoveRspCmd.getInt(
                                    Tags.NumberOfRemainingSubOperations, 0) < size) {
                                notifyMovePending(fwdMoveRspCmd);
                            }
                            break;
                        case Status.Cancel:
                            remaining += fwdMoveRspCmd.getInt(
                                    Tags.NumberOfRemainingSubOperations, 0);
                        case Status.Success:
                        case Status.SubOpsOneOrMoreFailures:
                            completed += fwdMoveRspCmd.getInt(
                                    Tags.NumberOfCompletedSubOperations, 0);
                            warnings += fwdMoveRspCmd.getInt(
                                    Tags.NumberOfWarningSubOperations, 0);
                            if (ds != null) {
                                failedIUIDs
                                        .addAll(Arrays
                                                .asList(ds
                                                        .getStrings(Tags.FailedSOPInstanceUIDList)));
                            }
                            break;
                        default:
                            // General error
                            service.getLog().error(
                                    "Forwarded MOVE RQ to " + retrieveAET
                                            + " failed: " + fwdMoveRspCmd);
                            failedIUIDs.addAll(iuids);
                        }
                    } catch (IOException e) {
                        service.getLog().error(
                                "Failure during receive of C-MOVE_RSP:", e);
                    }
                }
            };
            try {
                moveForwardCmd = new MoveForwardCmd(service, service
                        .isForwardAsMoveOriginator() ? moveOriginatorAET
                        : service.getAET(), retrieveAET, moveRqCmd.getInt(
                        Tags.Priority, 0), moveDest, (String[]) iuids
                        .toArray(new String[iuids.size()]));
                moveForwardCmd.execute(fwdmoveRspListener);
            } catch (Exception e) {
                service.getLog().error(
                        "Failed to forward MOVE RQ to " + retrieveAET, e);
                failedIUIDs.addAll(iuids);
            }
        }
        notifyMoveFinished();
    }

    private void retrieveLocal() {
        for (int i = 0, n = toRetrieve.size(); !canceled && i < n; ++i) {
            final FileInfo fileInfo = (FileInfo) toRetrieve.get(i);
            final String iuid = fileInfo.sopIUID;
            DimseListener storeScpListener = new DimseListener() {

                public void dimseReceived(Association assoc, Dimse dimse) {
                    switch (dimse.getCommand().getStatus()) {
                    case Status.Success:
                        ++completed;
                        updateInstancesAction(fileInfo);
                        break;
                    case Status.CoercionOfDataElements:
                    case Status.DataSetDoesNotMatchSOPClassWarning:
                    case Status.ElementsDiscarded:
                        ++warnings;
                        updateInstancesAction(fileInfo);
                        break;
                    default:
                        failedIUIDs.add(iuid);
                        break;
                    }
                    if (--remaining > 0) {
                        notifyMovePending(null);
                    }
                }
            };
            try {
                storeAssoc.invoke(makeCStoreRQ(fileInfo), storeScpListener);
            } catch (Exception e) {
                service.getLog().error("Failed to move " + iuid, e);
                failedIUIDs.add(iuid);
            }
        }
        try {
            storeAssoc.release(true);
            // workaround to ensure that last STORE-RSP is processed before
            // finally MOVE-RSP is sent
            Thread.sleep(10);
        } catch (Exception ignore) {
        }
        logInstancesSent();
    }

    private void logInstancesSent() {
        if (service.getAuditLogger() != null) {
            service.getAuditLogger().logInstancesSent(remoteNode,
                    instancesAction);
        }
    }

    private void updateInstancesAction(final FileInfo fileInfo) {
        instancesAction.addSOPClassUID(fileInfo.sopCUID);
        instancesAction.addStudyInstanceUID(fileInfo.studyIUID);
        instancesAction.incNumberOfInstances(1);
    }

    private Dimse makeCStoreRQ(FileInfo info) throws NoPresContextException {
        Association assoc = storeAssoc.getAssociation();
        PresContext presCtx = assoc.getAcceptedPresContext(info.sopCUID,
                info.tsUID);
        if (presCtx == null) {
            presCtx = assoc.getAcceptedPresContext(info.sopCUID,
                    UIDs.ExplicitVRLittleEndian);
            if (presCtx == null) {
                presCtx = assoc.getAcceptedPresContext(info.sopCUID,
                        UIDs.ImplicitVRLittleEndian);
                if (presCtx == null) { throw new NoPresContextException(
                        "No Presentation Context for " + uidDict.toString()
                                + " accepted by " + moveDest); }
            }
        }
        Command storeRqCmd = of.newCommand();
        storeRqCmd.initCStoreRQ(assoc.nextMsgID(), info.sopCUID, info.sopIUID,
                moveRqCmd.getInt(Tags.Priority, Command.MEDIUM));
        storeRqCmd
                .putUS(Tags.MoveOriginatorMessageID, moveRqCmd.getMessageID());
        storeRqCmd.putAE(Tags.MoveOriginatorAET, moveOriginatorAET);
        DataSource ds = new FileDataSource(service, info, buffer);
        return af.newDimse(presCtx.pcid(), storeRqCmd, ds);
    }

    private void notifyMovePending(Command fwdMoveRspCmd) {
        if (service.isSendPendingMoveRSP()) {
            notifyMoveSCU(Status.Pending, null, fwdMoveRspCmd);
        }
    }

    private void notifyMoveFinished() {
        final int status = canceled ? Status.Cancel
                : !failedIUIDs.isEmpty() ? Status.SubOpsOneOrMoreFailures
                        : Status.Success;
        Dataset ds = null;
        if (!failedIUIDs.isEmpty()) {
            ds = of.newDataset();
            ds.putUI(Tags.FailedSOPInstanceUIDList, (String[]) failedIUIDs
                    .toArray(new String[failedIUIDs.size()]));
        }
        notifyMoveSCU(status, ds, null);
    }

    private void notifyMoveSCU(int status, Dataset ds, Command fwdMoveRspCmd) {
        if (moveAssoc != null) {
            Command cmd = fwdMoveRspCmd != null ? makeMoveRsp(fwdMoveRspCmd)
                    : makeMoveRsp(status);
            try {
                moveAssoc.getAssociation()
                        .write(af.newDimse(movePcid, cmd, ds));
            } catch (Exception e) {
                service.getLog().info(
                        "Failed to send Move RSP to Move Originator:", e);
                moveAssoc = null;
            }
        }
    }

    private Command makeMoveRsp(Command fwdMoveRspCmd) {
        Command rspCmd = of.newCommand();
        rspCmd.initCMoveRSP(moveRqCmd.getMessageID(), moveRqCmd
                .getAffectedSOPClassUID(), fwdMoveRspCmd.getStatus());
        rspCmd.putUS(Tags.NumberOfRemainingSubOperations, remaining
                + fwdMoveRspCmd.getInt(Tags.NumberOfRemainingSubOperations, 0));
        rspCmd.putUS(Tags.NumberOfCompletedSubOperations, completed
                + fwdMoveRspCmd.getInt(Tags.NumberOfCompletedSubOperations, 0));
        rspCmd.putUS(Tags.NumberOfWarningSubOperations, warnings
                + fwdMoveRspCmd.getInt(Tags.NumberOfWarningSubOperations, 0));
        rspCmd.putUS(Tags.NumberOfFailedSubOperations, failedIUIDs.size()
                + fwdMoveRspCmd.getInt(Tags.NumberOfFailedSubOperations, 0));
        return rspCmd;
    }

    private Command makeMoveRsp(int status) {
        Command rspCmd = of.newCommand();
        rspCmd.initCMoveRSP(moveRqCmd.getMessageID(), moveRqCmd
                .getAffectedSOPClassUID(), status);
        if (remaining > 0) {
            rspCmd.putUS(Tags.NumberOfRemainingSubOperations, remaining);
        } else {
            rspCmd.remove(Tags.NumberOfRemainingSubOperations);
        }
        rspCmd.putUS(Tags.NumberOfCompletedSubOperations, completed);
        rspCmd.putUS(Tags.NumberOfWarningSubOperations, warnings);
        rspCmd.putUS(Tags.NumberOfFailedSubOperations, failedIUIDs.size());
        return rspCmd;
    }

    private Set getRemoteRetrieveAETs(FileInfo[] instFiles) {
        Set aets = new HashSet();
        for (int i = 0; i < instFiles.length; ++i) {
            Set tmp = instFiles[i].getRetrieveAETSet();
            if (tmp.contains(service.getAET())) { // local accessable
                toRetrieve.add(instFiles[i]);
                return null;
            }
            aets.addAll(tmp);
        }
        return aets;
    }

    private void prepareRetrieveInfo(FileInfo[][] fileInfo) {
        HashMap iuidsAtAE = new HashMap();
        for (int i = 0; i < fileInfo.length; ++i) {
            FileInfo[] instFiles = fileInfo[i];
            Set aets = getRemoteRetrieveAETs(instFiles);
            if (aets != null) {
                for (Iterator it = aets.iterator(); it.hasNext();) {
                    final String aet = (String) it.next();
                    Set iuids = (Set) iuidsAtAE.get(aet);
                    if (iuids == null) {
                        iuids = new HashSet();
                        iuidsAtAE.put(aet, iuids);
                    }
                    iuids.add(instFiles[0].sopIUID);
                }
            }
        }
        while (!iuidsAtAE.isEmpty()) {
            // select AE with most number of instances
            Iterator it = iuidsAtAE.entrySet().iterator();
            Map.Entry select = (Map.Entry) it.next();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                if (((Set) select.getValue()).size() < ((Set) entry.getValue())
                        .size()) {
                    select = entry;
                }
            }
            // mark to forward
            toForward.add(select);
            // update iuidsAtAE
            iuidsAtAE.remove(select.getKey());
            for (Iterator it2 = iuidsAtAE.values().iterator(); it2.hasNext();) {
                Set iuids = (Set) it2.next();
                iuids.removeAll((Collection) select.getValue());
                if (iuids.isEmpty()) {
                    it2.remove();
                }
            }
        }
    }
}
