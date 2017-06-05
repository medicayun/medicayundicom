/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.stgcmt;

import java.io.IOException;
import java.sql.SQLException;

import javax.jms.JMSException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1799 $ $Date: 2005-06-22 20:47:51 +0800 (周三, 22 6月 2005) $
 * @since Jan 5, 2005
 */
class StgCmtScuScp extends DcmServiceBase {

    private final StgCmtScuScpService service;

    private final Logger log;

    public StgCmtScuScp(StgCmtScuScpService service) {
        this.service = service;
        this.log = service.getLog();
    }

    protected Dataset doNAction(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        Command cmd = rq.getCommand();
        Dataset data = rq.getDataset();
		log.debug("StgCmt Request:\n");
		log.debug(data);
        if (!UIDs.StorageCommitmentPushModelSOPInstance.equals(cmd
                .getRequestedSOPInstanceUID())) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        final int actionTypeID = cmd.getInt(Tags.ActionTypeID, -1);
        if (actionTypeID != 1) {
            throw new DcmServiceException(Status.NoSuchActionType,
                    "ActionTypeID:" + actionTypeID);
        }
        if (data.vm(Tags.TransactionUID) <= 0) {
            throw new DcmServiceException(Status.MissingAttributeValue,
                    "Missing Transaction UID (0008,1195) in Action Information");
        }
        if (data.vm(Tags.RefSOPSeq) <= 0) {
            throw new DcmServiceException(Status.MissingAttributeValue,
                    "Missing Referenced SOP Sequence (0008,1199) in Action Information");
        }
        final Association a = assoc.getAssociation();
        final String aet = a.getCallingAET();
        try {
            AEData aeData = new AECmd(aet).getAEData();
            if (aeData == null) {
                throw new DcmServiceException(Status.ProcessingFailure,
                        "Failed to resolve AET:" + aet);
            }
            service.queueStgCmtOrder(a.getCalledAET(), aet, data, true);
        } catch (SQLException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        } catch (JMSException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        return null;
    }

    protected Dataset doNEventReport(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        Command cmd = rq.getCommand();
        Dataset data = rq.getDataset();
		log.debug("StgCmt Result:\n");
		log.debug(data);
        if (!UIDs.StorageCommitmentPushModelSOPInstance.equals(cmd
                .getRequestedSOPInstanceUID())) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        final int eventTypeID = cmd.getInt(Tags.EventTypeID, -1);
        final DcmElement refSOPSeq = data.get(Tags.RefSOPSeq);
        final DcmElement failedSOPSeq = data.get(Tags.FailedSOPSeq);
        if (eventTypeID == 1) {
            if (refSOPSeq == null) {
                throw new DcmServiceException(Status.MissingAttributeValue,
                        "Missing Referenced SOP Sequence (0008,1199) in Event Information");
            }
            if (failedSOPSeq != null) {
                throw new DcmServiceException(Status.InvalidArgumentValue,
                        "Unexpected Failed SOP Sequence (0008,1198) in Event Information");
            }
        } else if (eventTypeID == 2) {
            if (failedSOPSeq == null) {
                throw new DcmServiceException(Status.MissingAttributeValue,
                        "Missing Failed SOP Sequence (0008,1198) in Event Information");
            }
        } else {
            throw new DcmServiceException(Status.NoSuchEventType,
                    "EventTypeID:" + eventTypeID);
        }
        if (data.vm(Tags.TransactionUID) <= 0) {
            throw new DcmServiceException(Status.MissingAttributeValue,
                    "Missing Transaction UID (0008,1195) in Event Information");
        }
        checkRefSopSeq(refSOPSeq, false);
        checkRefSopSeq(failedSOPSeq, true);
        service.commited(data);
        return null;
    }

    private void checkRefSopSeq(DcmElement sq, boolean failed)
            throws DcmServiceException {
        if (sq == null)
            return;
        for (int i = 0, n = sq.vm(); i < n; ++i) {
            final Dataset refSOP = sq.getItem(i);
            final String iuid = refSOP.getString(Tags.RefSOPInstanceUID);
            final String cuid = refSOP.getString(Tags.RefSOPClassUID);
            if (iuid == null) {
                throw new DcmServiceException(Status.MissingAttributeValue,
                        "Missing Ref. SOP Instance UID >(0008,1155) in Item of "
                                + (failed ? "Failed SOP Sequence (0008,1198)"
                                        : "Ref. SOP Sequence (0008,1199)"));
            }
            if (cuid == null) {
                throw new DcmServiceException(Status.MissingAttributeValue,
                        "Missing Ref. SOP Class UID >(0008,1150) in Item of "
                                + (failed ? "Failed SOP Sequence (0008,1198)"
                                        : "Ref. SOP Sequence (0008,1199)"));
            }
            if (failed) {
                Integer reason = refSOP.getInteger(Tags.FailureReason);
                if (reason == null) {
                    throw new DcmServiceException(Status.MissingAttributeValue,
                            "Missing Failed Reason >(0008,1197) in Item of Failed SOP Sequence (0008,1198)");
                }
                log.warn("Failed Storage Commitment for SOP Instance[iuid=" 
                        + iuid + ", cuid=" + cuid + "], reason: "
                        + Integer.toHexString(reason.intValue()) + "H");
            }
        }
    }
}
