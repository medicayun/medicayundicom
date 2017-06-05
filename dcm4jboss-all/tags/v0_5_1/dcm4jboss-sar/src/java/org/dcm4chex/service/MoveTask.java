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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;

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
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 936 $
 * @since 16.09.2003
 */
class MoveTask implements Runnable {

    private static final String[] NATIVE_TS =
        { UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian };
    private static final String[][] PROPOSED_TS = { NATIVE_TS,
        //            new String[] { UIDs.JPEG2000Lossless },
        //            new String[] { UIDs.JPEGLossless },
        //            new String[] { UIDs.JPEG2000Lossy },
        //            new String[] { UIDs.JPEGBaseline },
        //            new String[] { UIDs.JPEGExtended },
    };

    private static final AssociationFactory af =
        AssociationFactory.getInstance();
    private static final DcmObjectFactory of = DcmObjectFactory.getInstance();
    private static final UIDDictionary uidDict =
        DictionaryFactory.getInstance().getDefaultUIDDictionary();

    private static int defaultBufferSize = 2048;

    private final Logger log;
    private final byte[] buffer = new byte[defaultBufferSize];
    private final String moveDest;
    private final boolean sendPendingMoveRSP;
    private final int acTimeout;
    private final AEData aeData;
    private final int movePcid;
    private final Command moveRqCmd;
    private final String moveOriginatorAET;
    private final String retrieveAET;
    private ActiveAssociation moveAssoc;
    private final ArrayList failedIUIDs = new ArrayList();
    private int warnings = 0;
    private int completed = 0;
    private int remaining = 0;
    private boolean canceled = false;
    private ActiveAssociation storeAssoc;

    private final ArrayList toRetrieve = new ArrayList();

    public MoveTask(
        Logger log,
        ActiveAssociation moveAssoc,
        int movePcid,
        Command moveRqCmd,
        FileInfo[][] fileInfo,
        AEData aeData,
        String moveDest,
        boolean sendPendingMoveRSP,
        int acTimeout)
        throws DcmServiceException {
        this.log = log;
        this.moveAssoc = moveAssoc;
        this.movePcid = movePcid;
        this.moveRqCmd = moveRqCmd;
        this.aeData = aeData;
        this.moveDest = moveDest;
        this.sendPendingMoveRSP = sendPendingMoveRSP;
        this.acTimeout = acTimeout;
        this.moveOriginatorAET = moveAssoc.getAssociation().getCallingAET();
        this.retrieveAET = moveAssoc.getAssociation().getCalledAET();
        if ((remaining = fileInfo.length) > 0) {
            notifyMovePending();
            openAssociation(fileInfo);
            prepareRetrieveInfo(fileInfo);
            moveAssoc
                .addCancelListener(
                    moveRqCmd.getMessageID(),
                    new DimseListener() {
                public void dimseReceived(Association assoc, Dimse dimse) {
                    canceled = true;
                }
            });
        }
    }

    private void openAssociation(FileInfo[][] fileInfo)
        throws DcmServiceException {
        PDU ac = null;
        Association a = null;
        try {
            a = af.newRequestor(createSocket());
            a.setAcTimeout(acTimeout);
            ac = a.connect(createAAssociateRQ(fileInfo));
        } catch (IOException e) {
            final String prompt = "Failed to connect " + moveDest;
            log.error(prompt, e);
            throw new DcmServiceException(
                Status.UnableToPerformSuboperations,
                prompt,
                e);
        }
        if (!(ac instanceof AAssociateAC)) {
            final String prompt =
                "Association not accepted by " + moveDest + ":\n" + ac;
            log.error(prompt);
            throw new DcmServiceException(
                Status.UnableToPerformSuboperations,
                prompt);
        }
        storeAssoc = af.newActiveAssociation(a, null);
        storeAssoc.start();
    }

    private AAssociateRQ createAAssociateRQ(FileInfo[][] fileInfo) {
        AAssociateRQ rq = af.newAAssociateRQ();
        rq.setCalledAET(moveDest);
        rq.setCallingAET(moveAssoc.getAssociation().getCalledAET());

        HashSet cuidSet = new HashSet();
        for (int i = 0; i < fileInfo.length; i++) {
            final String cuid = fileInfo[i][0].sopCUID;
            if (cuidSet.add(cuid)) {
                for (int j = 0; j < PROPOSED_TS.length; j++) {
                    rq.addPresContext(
                        af.newPresContext(rq.nextPCID(), cuid, PROPOSED_TS[j]));
                }
            }
        }
        return rq;
    }

    private Socket createSocket()
        throws DcmServiceException, UnknownHostException, IOException {
        return new Socket(aeData.getHostName(), aeData.getPort());
    }

    private void prepareRetrieveInfo(FileInfo[][] fileInfoArray) {
        FileSelector selector = new FileSelector(storeAssoc.getAssociation());
        for (int i = 0; i < fileInfoArray.length; i++) {
            FileInfo[] fileInfo = fileInfoArray[i];
            FileSelection selection = selector.select(fileInfo, retrieveAET);
            if (selection != null) {
                toRetrieve.add(selection);
            } else {
                log.warn(
                    "No apropriate transfer capability to transfer "
                        + uidDict.toString(fileInfo[0].sopCUID));
                failedIUIDs.add(fileInfo[0].sopIUID);
            }
        }
    }

    public void run() {
        remaining = toRetrieve.size();
        for (int i = 0, n = toRetrieve.size(); !canceled && i < n; ++i) {
            final FileSelection sel = (FileSelection) toRetrieve.get(i);
            final String iuid = sel.fileInfo.sopIUID;
            DimseListener storeScpListener = new DimseListener() {
                public void dimseReceived(Association assoc, Dimse dimse) {
                    switch (dimse.getCommand().getStatus()) {
                        case Status.Success :
                            ++completed;
                            break;
                        case Status.CoercionOfDataElements :
                        case Status.DataSetDoesNotMatchSOPClassWarning :
                        case Status.ElementsDiscarded :
                            ++warnings;
                            break;
                        default :
                            failedIUIDs.add(iuid);
                            break;
                    }
                    if (--remaining > 0) {
                        notifyMovePending();
                    }
                }
            };
            try {
                storeAssoc.invoke(
                    makeCStoreRQ(sel.fileInfo, sel.presContext),
                    storeScpListener);
            } catch (Exception e) {
                log.error("Failed to move " + iuid, e);
                failedIUIDs.add(iuid);
            }
        }
        try {
            storeAssoc.release(true);
        } catch (Exception ignore) {
        }
        notifyMoveFinished();
    }

    private Dimse makeCStoreRQ(FileInfo info, PresContext presCtx) {
        Association assoc = storeAssoc.getAssociation();
        Command storeRqCmd = of.newCommand();
        storeRqCmd.initCStoreRQ(
            assoc.nextMsgID(),
            info.sopCUID,
            info.sopIUID,
            moveRqCmd.getInt(Tags.Priority, Command.MEDIUM));
        storeRqCmd.putUS(
            Tags.MoveOriginatorMessageID,
            moveRqCmd.getMessageID());
        storeRqCmd.putAE(Tags.MoveOriginatorAET, moveOriginatorAET);
        DataSource ds = new FileDataSource(info, buffer);
        return af.newDimse(presCtx.pcid(), storeRqCmd, ds);
    }

    private void notifyMovePending() {
        if (sendPendingMoveRSP) {
            notifyMoveSCU(Status.Pending, null);
        }
    }

    private void notifyMoveFinished() {
        final int status =
            canceled
                ? Status.Cancel
                : !failedIUIDs.isEmpty()
                ? Status.SubOpsOneOrMoreFailures
                : Status.Success;
        Dataset ds = null;
        if (!failedIUIDs.isEmpty()) {
            ds = of.newDataset();
            ds.putUI(
                Tags.FailedSOPInstanceUIDList,
                (String[]) failedIUIDs.toArray(new String[failedIUIDs.size()]));
        }
        notifyMoveSCU(status, ds);
    }

    private void notifyMoveSCU(int status, Dataset ds) {
        if (moveAssoc != null) {
            try {
                moveAssoc.getAssociation().write(
                    af.newDimse(movePcid, makeMoveRsp(status), ds));
            } catch (Exception e) {
                log.info("Failed to send Move RSP to Move Originator:", e);
                moveAssoc = null;
            }
        }
    }

    private Command makeMoveRsp(int status) {
        Command rspCmd = of.newCommand();
        rspCmd.initCMoveRSP(
            moveRqCmd.getMessageID(),
            moveRqCmd.getAffectedSOPClassUID(),
            status);
        if (remaining > 0) {
            rspCmd.putUS(Tags.NumberOfRemainingSubOperations, remaining);
        } else {
            rspCmd.remove(Tags.NumberOfRemainingSubOperations);
        }
        rspCmd.putUS(Tags.NumberOfCompletedSubOperations, completed);
        rspCmd.putUS(Tags.NumberOfWarningSubOperations, warnings);
        rspCmd.putUS(Tags.NumberOfFailedSubOperations, failedIUIDs.size());
        rspCmd.putUS(Tags.Status, status);
        return rspCmd;
    }
}
