/* ***** BEGIN LICENSE BLOCK *****#ß
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

package org.dcm4chex.archive.dcm.ianscu;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.ejb.ObjectNotFoundException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PresContext;
import org.dcm4che.util.UIDGenerator;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.SeriesStored;
import org.dcm4chex.archive.config.DicomPriority;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.dcm.AbstractScuService;
import org.dcm4chex.archive.dcm.mppsscp.MPPSScpService;
import org.dcm4chex.archive.ejb.interfaces.MPPSManagerHome;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.mbean.JMSDelegate;
import org.dcm4chex.archive.notif.StudyDeleted;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 3310 $ $Date: 2007-05-07 07:42:06 +0800 (周一, 07 5月 2007) $
 * @since 27.08.2004
 */
public class IANScuService extends AbstractScuService implements
        MessageListener {

    public static final String EVENT_TYPE = "org.dcm4chex.archive.dcm.ianscu";
    public static final NotificationFilter NOTIF_FILTER = new NotificationFilter() {

        private static final long serialVersionUID = -6323628592613659041L;

        public boolean isNotificationEnabled(Notification notif) {
            return EVENT_TYPE.equals(notif.getType());
        }
    };
    
    private static final String NONE = "NONE";

    private static final String[] EMPTY = {};

    private static final String[] IAN_ONLY = { UIDs.InstanceAvailabilityNotificationSOPClass };

    private static final String[] IAN_AND_SCN = { UIDs.InstanceAvailabilityNotificationSOPClass,
            UIDs.BasicStudyContentNotification };
    
    private static final UIDGenerator uidGen = UIDGenerator.getInstance();

    private static final NotificationFilterSupport seriesStoredFilter = 
        new NotificationFilterSupport();

    private static final NotificationFilterSupport studyDeletedFilter = 
        new NotificationFilterSupport();
    
    static {
        seriesStoredFilter.enableType(SeriesStored.class.getName());
        studyDeletedFilter.enableType(StudyDeleted.class.getName());
    }

    private final NotificationListener seriesStoredListener = 
        new NotificationListener() {
            public void handleNotification(Notification notif, Object handback) {
                onSeriesStored((SeriesStored) notif.getUserData());
            }
        };

    private final NotificationListener mppsReceivedListener = 
        new NotificationListener() {
            public void handleNotification(Notification notif, Object handback) {
                onMPPSReceived((Dataset) notif.getUserData());
            }
        };

    private final NotificationListener studyDeletedListener = 
        new NotificationListener() {
            public void handleNotification(Notification notif, Object handback) {
                onStudyDeleted((StudyDeleted) notif.getUserData());
            }
        };

    private ObjectName storeScpServiceName;

    private ObjectName mppsScpServiceName;

    private ObjectName fileSystemMgtName;

    private String queueName;

    private boolean preferInstanceAvailableNotification = true;

    private String[] offeredSOPClasses = IAN_AND_SCN;

    private int scnPriority = 0;

    private String[] notifiedAETs = EMPTY;
    
    private boolean notifyOtherServices;

    private boolean onStudyDeleted;

    private RetryIntervalls retryIntervalls = new RetryIntervalls();

    private boolean sendOneIANforEachMPPS;

    private int concurrency = 1;

    private JMSDelegate jmsDelegate = new JMSDelegate(this);

    public final ObjectName getJmsServiceName() {
        return jmsDelegate.getJmsServiceName();
    }

    public final void setJmsServiceName(ObjectName jmsServiceName) {
        jmsDelegate.setJmsServiceName(jmsServiceName);
    }
    
    public final int getConcurrency() {
        return concurrency;
    }

    public final void setConcurrency(int concurrency) throws Exception {
        if (concurrency <= 0)
            throw new IllegalArgumentException("Concurrency: " + concurrency);
        if (this.concurrency != concurrency) {
            final boolean restart = getState() == STARTED;
            if (restart)
                stop();
            this.concurrency = concurrency;
            if (restart)
                start();
        }
    }

    public final String getNotifiedAETs() {
        return notifiedAETs.length > 0 ? StringUtils.toString(notifiedAETs,
                '\\') : NONE;
    }

    public final void setNotifiedAETs(String notifiedAETs) {
        this.notifiedAETs = NONE.equalsIgnoreCase(notifiedAETs) ? EMPTY
                : StringUtils.split(notifiedAETs, '\\');
    }

    public final void setNotifyOtherServices(boolean notifyOtherServices) {
        this.notifyOtherServices = notifyOtherServices;
    }

    public final boolean isNotifyOtherServices() {
        return notifyOtherServices;
    }

    
    public final boolean isOnStudyDeleted() {
        return onStudyDeleted;
    }

    public final void setOnStudyDeleted(boolean onStudyDeleted) {
        this.onStudyDeleted = onStudyDeleted;
    }

    public final boolean isSendOneIANforEachMPPS() {
        return sendOneIANforEachMPPS;
    }

    public final void setSendOneIANforEachMPPS(boolean sendOneIANforEachMPPS) {
        this.sendOneIANforEachMPPS = sendOneIANforEachMPPS;
    }

    public final String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public final void setRetryIntervalls(String s) {
        this.retryIntervalls = new RetryIntervalls(s);
    }

    public final boolean isOfferStudyContentNotification() {
        return offeredSOPClasses == IAN_AND_SCN;
    }

    public final void setOfferStudyContentNotification(boolean offerSCN) {
        this.offeredSOPClasses = offerSCN ? IAN_AND_SCN : IAN_ONLY;
    }

    public final boolean isPreferInstanceAvailableNotification() {
        return preferInstanceAvailableNotification;
    }

    public final void setPreferInstanceAvailableNotification(boolean preferIAN) {
        this.preferInstanceAvailableNotification = preferIAN;
    }

    public final String getScnPriority() {
        return DicomPriority.toString(scnPriority);
    }

    public final void setScnPriority(String scnPriority) {
        this.scnPriority = DicomPriority.toCode(scnPriority);
    }

    public final ObjectName getStoreScpServiceName() {
        return storeScpServiceName;
    }

    public final void setStoreScpServiceName(ObjectName storeScpServiceName) {
        this.storeScpServiceName = storeScpServiceName;
    }

    public final ObjectName getMppsScpServiceName() {
        return mppsScpServiceName;
    }

    public final void setMppsScpServiceName(ObjectName name) {
        this.mppsScpServiceName = name;
    }

    public ObjectName getFileSystemMgtName() {
        return fileSystemMgtName;
    }

    public void setFileSystemMgtName(ObjectName fileSystemMgtName) {
        this.fileSystemMgtName = fileSystemMgtName;
    }

    public final String getQueueName() {
        return queueName;
    }

    public final void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    protected void startService() throws Exception {
        jmsDelegate.startListening(queueName, this, concurrency);
        server.addNotificationListener(storeScpServiceName,
                seriesStoredListener, seriesStoredFilter, null);
        server.addNotificationListener(fileSystemMgtName,
                studyDeletedListener, studyDeletedFilter, null);
        server.addNotificationListener(mppsScpServiceName,
                mppsReceivedListener, MPPSScpService.NOTIF_FILTER, null);

    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(storeScpServiceName,
                seriesStoredListener, seriesStoredFilter, null);
        server.removeNotificationListener(fileSystemMgtName,
                studyDeletedListener, studyDeletedFilter, null);
        server.removeNotificationListener(mppsScpServiceName,
                mppsReceivedListener, MPPSScpService.NOTIF_FILTER, null);
        jmsDelegate.stopListening(queueName);
    }

    private void onSeriesStored(SeriesStored stored) {
        if (!notifyOtherServices && notifiedAETs.length == 0)
            return;
        Dataset ian = stored.getIAN();
        Dataset pps = ian.getItem(Tags.RefPPSSeq);
        String mppsiuid = pps != null ? pps.getString(Tags.RefSOPInstanceUID)
                : null;
        if (!sendOneIANforEachMPPS) {
            schedule(ian);
        } else if (mppsiuid != null) {
            try {
                onMPPSReceived(getMPPSManagerHome().create().getMPPS(mppsiuid));
            } catch (ObjectNotFoundException e) {
                log.debug("No such MPPS - " + mppsiuid);
            } catch (Exception e) {
                log.error("Failed to access MPPS - " + mppsiuid, e);
            }
        }

    }

    private MPPSManagerHome getMPPSManagerHome() throws HomeFactoryException {
        return (MPPSManagerHome) EJBHomeFactory.getFactory().lookup(
                MPPSManagerHome.class, MPPSManagerHome.JNDI_NAME);
    }

    private void onMPPSReceived(Dataset mpps) {
        if (isIgnoreMPPS(mpps) || !notifyOtherServices 
                && (notifiedAETs.length == 0 || !sendOneIANforEachMPPS)) {
            return;
        }
        Dataset ian = makeIAN(mpps);
        if (ian != null) {
            if (notifyOtherServices) {
                sendMPPSInstancesAvailableNotification(mpps);
            }
            if (sendOneIANforEachMPPS) {
                schedule(ian);
            }
        }
    }

    void sendMPPSInstancesAvailableNotification(Dataset mpps) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(EVENT_TYPE, this, eventID);
        notif.setUserData(mpps);
        super.sendNotification(notif);
    }
    
    private boolean isIgnoreMPPS(Dataset mpps) {
        String status = mpps.getString(Tags.PPSStatus);
        if ("COMPLETED".equals(status)) {
            return false;
        }
        if (!"DISCONTINUE".equals(status)) {
            if (log.isInfoEnabled()) {
                log.info("Ignore MPPS with status " + status);
            }
            return true;
        }
        Dataset item = mpps.getItem(Tags.PPSDiscontinuationReasonCodeSeq);
        if (item != null && "110514".equals(item.getString(Tags.CodeValue))
                && "DCM".equals(item.getString(Tags.CodingSchemeDesignator))) {
            log.info("Ignore MPPS with Discontinuation Reason Code: " +
                        "Wrong Worklist Entry Selected");
            return true;
        }
        return false;
    }

    private Dataset makeIAN(Dataset mpps) {
        DcmElement perfSeriesSq = mpps.get(Tags.PerformedSeriesSeq);
        if (perfSeriesSq == null) {
            return null;
        }
        Dataset ian = DcmObjectFactory.getInstance().newDataset();
        String cuid = mpps.getString(Tags.SOPClassUID);
        String iuid = mpps.getString(Tags.SOPInstanceUID);
        DcmElement refSeriesSq = ian.putSQ(Tags.RefSeriesSeq);
        String suid = null;
        for (int i = 0, n = perfSeriesSq.countItems(); i < n; i++) {
            Dataset perfSeries = perfSeriesSq.getItem(i);
            DcmElement refImageSeq = perfSeries.get(Tags.RefImageSeq);
            if (refImageSeq == null) {
                refImageSeq = perfSeries
                        .get(Tags.RefNonImageCompositeSOPInstanceSeq);
            }
            if (refImageSeq != null && !refImageSeq.isEmpty()) {
                FileInfo[][] aa;
                try {
                    aa = RetrieveCmd.create(refImageSeq).getFileInfos();
                } catch (SQLException e) {
                    log.error("Failed to check availability of Instances " +
                                "referenced in MPPS", e);
                    return null;
                }
                String seruid = perfSeries.getString(Tags.SeriesInstanceUID);
                if (log.isInfoEnabled()) {
                    log
                            .info("Series[" + seruid + "]: " + aa.length
                                    + " from " + refImageSeq.countItems()
                                    + " Instances available");
                }
                if (aa.length != refImageSeq.countItems()) {
                    return null;
                }
                Dataset refSeries = refSeriesSq.addNewItem();
                refSeries.putUI(Tags.SeriesInstanceUID, seruid);
                DcmElement refSOPSeq = refSeries.putSQ(Tags.RefSOPSeq);
                for (int j = 0; j < aa.length; j++) {
                    FileInfo info = aa[j][0];
                    if (!info.seriesIUID.equals(seruid)) {
                        log.error("Series Instance UID of " + info
                                + " differs from value [" + seruid
                                + "] in MPPS[iuid=" + iuid + "]");
                        return null;
                    }
                    if (suid != null && !suid.equals(info.studyIUID)) {
                        log.error("Different Study Instance UIDs of "
                                + "instances referenced in MPPS[iuid=" + iuid
                                + "] detected");
                        return null;
                    }
                    suid = info.studyIUID;
                    Dataset refSOP = refSOPSeq.addNewItem();
                    refSOP.putUI(Tags.RefSOPClassUID, info.sopCUID);
                    refSOP.putUI(Tags.RefSOPInstanceUID, info.sopIUID);
                    refSOP.putCS(Tags.InstanceAvailability, Availability
                            .toString(info.availability));
                    refSOP.putAE(Tags.RetrieveAET, info.fileRetrieveAET);
                }
            }
        }
        if (suid == null) {
            if (log.isInfoEnabled()) {
                log.info("MPPS[" + iuid + "] does not reference any Instances");
            }
            return null;
        }
        Dataset refPPS = ian.putSQ(Tags.RefPPSSeq).addNewItem();
        refPPS.putUI(Tags.RefSOPClassUID, cuid);
        refPPS.putUI(Tags.RefSOPInstanceUID, iuid);
        refPPS.putSQ(Tags.PerformedWorkitemCodeSeq);
        ian.putUI(Tags.StudyInstanceUID, suid);
        return ian;
    }

    private void onStudyDeleted(StudyDeleted deleted) {
        if (notifiedAETs.length == 0)
            return;
        if (onStudyDeleted)
            schedule(deleted.getInstanceAvailabilityNotification());
    }

    private void schedule(Dataset ian) {
        if (log.isDebugEnabled()) {
            log.debug("IAN Dataset:");
            log.debug(ian);
        }
        for (int i = 0; i < notifiedAETs.length; ++i) {
            IANOrder order = new IANOrder(notifiedAETs[i], ian);
            try {
                log.info("Scheduling " + order);
                jmsDelegate.queue(queueName, order, Message.DEFAULT_PRIORITY,
                        0L);
            } catch (Exception e) {
                log.error("Failed to schedule " + order, e);
            }
        }
    }

    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        try {
            IANOrder order = (IANOrder) om.getObject();
            log.info("Start processing " + order);
            try {
                process(order);
                log.info("Finished processing " + order);
            } catch (Exception e) {
                final int failureCount = order.getFailureCount() + 1;
                order.setFailureCount(failureCount);
                final long delay = retryIntervalls.getIntervall(failureCount);
                if (delay == -1L) {
                    log.error("Give up to process " + order, e);
                } else {
                    log.warn("Failed to process " + order
                            + ". Scheduling retry.", e);
                    jmsDelegate.queue(queueName, order, 0, System
                            .currentTimeMillis()
                            + delay);
                }
            }
        } catch (JMSException e) {
            log.error("jms error during processing message: " + message, e);
        } catch (Throwable e) {
            log.error("unexpected error during processing message: " + message,
                    e);
        }
    }

    private void process(IANOrder order) throws Exception {
        ActiveAssociation aa = openAssociation(order.getDestination(),
                offeredSOPClasses);
        try {
            invokeDimse(aa, order);
        } finally {
            try {
                aa.release(true);
            } catch (Exception e) {
                log.warn(
                        "Failed to release association " + aa.getAssociation(),
                        e);
            }
        }
    }

    private void invokeDimse(ActiveAssociation aa, IANOrder order)
            throws DcmServiceException, IOException, InterruptedException {
        Association a = aa.getAssociation();
        List ianPC = a.listAcceptedPresContext(
                UIDs.InstanceAvailabilityNotificationSOPClass);
        boolean ianAccepted = !ianPC.isEmpty();
        List scnPC = a.listAcceptedPresContext(
                UIDs.BasicStudyContentNotification);
        boolean scnAccepted = !scnPC.isEmpty();
        AssociationFactory af = AssociationFactory.getInstance();
        Command cmdRq = DcmObjectFactory.getInstance().newCommand();
        final Dimse dimseRq;
        if (ianAccepted
                && (preferInstanceAvailableNotification || !scnAccepted)) {
            cmdRq.initNCreateRQ(a.nextMsgID(),
                    UIDs.InstanceAvailabilityNotificationSOPClass, uidGen
                            .createUID());
            dimseRq = af.newDimse(((PresContext) ianPC.get(0)).pcid(), cmdRq,
                    order.getDataset());
        } else {
            cmdRq.initCStoreRQ(a.nextMsgID(),
                    UIDs.BasicStudyContentNotification, uidGen.createUID(),
                    scnPriority);
            dimseRq = af.newDimse(((PresContext) scnPC.get(0)).pcid(), cmdRq,
                    toSCN(order.getDataset()));
        }
        log.debug("Dataset:\n");
        log.debug(dimseRq.getDataset());
        final Dimse dimseRsp = aa.invoke(dimseRq).get();
        final Command cmdRsp = dimseRsp.getCommand();
        final int status = cmdRsp.getStatus();
        switch (status) {
        case 0x0000:
        case 0x0001:
        case 0x0002:
        case 0x0003:
            break;
        case 0x0116:
            log.warn("Received Warning Status 116H "
                    + "(=Attribute Value Out of Range) from remote AE "
                    + order.getDestination());
            break;
        default:
            throw new DcmServiceException(status, cmdRsp
                    .getString(Tags.ErrorComment));
        }
    }

    private Dataset toSCN(Dataset ian) {
        Dataset scn = DcmObjectFactory.getInstance().newDataset();
        scn.putLO(Tags.PatientID, ian.getString(Tags.PatientID));
        scn.putPN(Tags.PatientName, ian.getString(Tags.PatientName));
        scn.putSH(Tags.StudyID, ian.getString(Tags.StudyID));
        scn.putUI(Tags.StudyInstanceUID, ian.getString(Tags.StudyInstanceUID));
        DcmElement ianSeriesSeq = ian.get(Tags.RefSeriesSeq);
        DcmElement scnSeriesSeq = scn.putSQ(Tags.RefSeriesSeq);
        for (int i = 0, n = ianSeriesSeq.countItems(); i < n; ++i) {
            Dataset ianSeries = ianSeriesSeq.getItem(i);
            Dataset scnSeries = scnSeriesSeq.addNewItem();
            scnSeries.putUI(Tags.SeriesInstanceUID, ianSeries
                    .getString(Tags.SeriesInstanceUID));
            DcmElement ianSOPSeq = ianSeries.get(Tags.RefSOPSeq);
            DcmElement scnSOPSeq = scnSeries.putSQ(Tags.RefImageSeq);
            for (int j = 0, m = ianSOPSeq.countItems(); j < m; ++j) {
                scnSOPSeq.addItem(ianSOPSeq.getItem(j));
            }
        }
        return scn;
    }
    
}