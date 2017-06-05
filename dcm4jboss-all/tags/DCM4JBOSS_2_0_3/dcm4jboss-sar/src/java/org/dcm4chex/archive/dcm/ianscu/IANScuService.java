/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.ianscu;

import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.QueueSession;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.util.UIDGenerator;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.dcm.storescp.StoreScpService;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1275 $ $Date: 2004-12-07 02:14:03 +0800 (周二, 07 12月 2004) $
 * @since 27.08.2004
 *
 */
public class IANScuService extends ServiceMBeanSupport implements
        MessageListener, NotificationListener {

    private static final Map dumpParam = new HashMap(5);
    static {
        dumpParam.put("maxlen", new Integer(128));
        dumpParam.put("vallen", new Integer(64));
        dumpParam.put("prefix", "\t");
    }
    
    private static final String NONE = "NONE";

    private static final String[] EMPTY = {};

    private static final String QUEUE = "IANScu";

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final AssociationFactory af = AssociationFactory
            .getInstance();

    private static final UIDGenerator uidGen = UIDGenerator.getInstance();

    private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian};

    private static final int ERR_SQL = -1;

    private static final int ERR_UNKOWN_DEST = -2;

    private static final int ERR_IO = -3;

    private static final int ERR_ASSOC_NOT_ACCEPTED = -4;

    private static final int ERR_SERVICE_NOT_SUPPORTED = -5;

    private static final int ERR_THREAD = -6;

    private static final int PCID_IAN = 1;

    private static final int PCID_SCN = 3;

    private static final String DEFAULT_CALLING_AET = "IAN_SCU";

    private ObjectName storeScpServiceName;

    private String callingAET = DEFAULT_CALLING_AET;

    private int acTimeout;

    private int dimseTimeout;

    private int soCloseDelay;

    private boolean preferInstanceAvailableNotification = true;

    private boolean offerInstanceAvailableNotification = true;

    private boolean offerStudyContentNotification = true;

    private int scnPriority = 0;

    private QueueSession jmsSession;

    private String[] notifiedAETs = EMPTY;

    public final String getNotifiedAETs() {
        return notifiedAETs.length > 0 ? StringUtils
                .toString(notifiedAETs, '\\') : NONE;
    }

    public final void setNotifiedAETs(String notifiedAETs) {
        this.notifiedAETs = NONE.equalsIgnoreCase(notifiedAETs) ? EMPTY
                : StringUtils.split(notifiedAETs, '\\');
    }

    public final String getCallingAET() {
        return callingAET;
    }

    public final void setCallingAET(String callingAET) {
        this.callingAET = callingAET;
    }

    public final int getAcTimeout() {
        return acTimeout;
    }

    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }

    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    public final void setSoCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }
    
    public final String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public final void setRetryIntervalls(String s) {
        this.retryIntervalls = new RetryIntervalls(s);
    }

    public final boolean isOfferInstanceAvailableNotification() {
        return offerInstanceAvailableNotification;
    }

    public final void setOfferInstanceAvailableNotification(boolean offerIAN) {
        this.offerInstanceAvailableNotification = offerIAN;
    }

    public final boolean isOfferStudyContentNotification() {
        return offerStudyContentNotification;
    }

    public final void setOfferStudyContentNotification(boolean offerSCN) {
        this.offerStudyContentNotification = offerSCN;
    }

    public final boolean isPreferInstanceAvailableNotification() {
        return preferInstanceAvailableNotification;
    }

    public final void setPreferInstanceAvailableNotification(boolean preferIAN) {
        this.preferInstanceAvailableNotification = preferIAN;
    }

    public final int getScnPriority() {
        return scnPriority;
    }

    public final void setScnPriority(int scnPriority) {
        this.scnPriority = scnPriority;
    }

    private RetryIntervalls retryIntervalls = new RetryIntervalls();

    public final ObjectName getStoreScpServiceName() {
        return storeScpServiceName;
    }

    public final void setStoreScpServiceName(ObjectName storeScpServiceName) {
        this.storeScpServiceName = storeScpServiceName;
    }

    protected void startService() throws Exception {
        JMSDelegate.startListening(QUEUE, this);
        server.addNotificationListener(storeScpServiceName,
                this,
                StoreScpService.NOTIF_FILTER,
                null);
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(storeScpServiceName,
                this,
                StoreScpService.NOTIF_FILTER,
                null);
        JMSDelegate.stopListening(QUEUE);
    }

    public void handleNotification(Notification notif, Object handback) {
        Association assoc = (Association) notif.getUserData();
        Map ians = (Map) assoc.getProperty(StoreScpService.IANS_KEY);
        if (ians == null) return;
        for (Iterator it = ians.values().iterator(); it.hasNext();) {
            Dataset ian = (Dataset) it.next();
            for (int i = 0; i < notifiedAETs.length; ++i) {
                IANOrder order = new IANOrder(notifiedAETs[i], ian);
                try {
                    log.info("Scheduling " + order);
                    JMSDelegate.queue(QUEUE,
                            order,
                            Message.DEFAULT_PRIORITY,
                            0L);
                } catch (JMSException e) {
                    log.error("Failed to schedule " + order, e);
                }
            }
        }
    }

    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        try {
            IANOrder order = (IANOrder) om.getObject();
            log.info("Start processing " + order);
            final int status = process(order);
            if (status == 0) {
                log.info("Finished processing " + order);
                return;
            }
            order.setFailureStatus(status);
            final int failureCount = order.getFailureCount() + 1;
            order.setFailureCount(failureCount);
            final long delay = retryIntervalls.getIntervall(failureCount);
            if (delay == -1L) {
                log.error("Give up to process " + order);
            } else {
                log.warn("Failed to process " + order + ". Scheduling retry.");
                JMSDelegate.queue(QUEUE, order, 0, System.currentTimeMillis()
                        + delay);
            }
        } catch (JMSException e) {
            log.error("jms error during processing message: " + message, e);
        } catch (Throwable e) {
            log.error("unexpected error during processing message: " + message,
                    e);
        }
    }

    private int process(IANOrder order) {
        final String called = order.getDestination();
        ActiveAssociation aa = null;
        try {
            AEData aeData = null;
            try {
                aeData = new AECmd(called).execute();
            } catch (SQLException e1) {
                log.error("Failed to access DB for resolving AET: " + called,
                        e1);
                return ERR_SQL;
            }
            if (aeData == null) {
                log.error("Unkown Destination AET: " + called);
                return ERR_UNKOWN_DEST;
            }
            Association a = af.newRequestor(createSocket(aeData));
            a.setAcTimeout(acTimeout);
            a.setDimseTimeout(dimseTimeout);
            a.setSoCloseDelay(soCloseDelay);
            AAssociateRQ rq = af.newAAssociateRQ();
            rq.setCalledAET(called);
            rq.setCallingAET(callingAET);
            if (offerInstanceAvailableNotification)
                    rq.addPresContext(af.newPresContext(PCID_IAN,
                            UIDs.InstanceAvailabilityNotificationSOPClass,
                            NATIVE_TS));
            if (offerStudyContentNotification)
                    rq.addPresContext(af.newPresContext(PCID_SCN,
                            UIDs.BasicStudyContentNotification,
                            NATIVE_TS));
            PDU ac = a.connect(rq);
            if (!(ac instanceof AAssociateAC)) {
                log.error("Association not accepted by " + called + ": " + ac);
                return ERR_ASSOC_NOT_ACCEPTED;
            }
            aa = af.newActiveAssociation(a, null);
            aa.start();
            final boolean ianAccepted = a
                    .getAcceptedTransferSyntaxUID(PCID_IAN) != null;
            final boolean scnAccepted = a
                    .getAcceptedTransferSyntaxUID(PCID_SCN) != null;
            if (!ianAccepted && !scnAccepted) {
                log.error("Notification Service not supported by remote AE: "
                        + called);
                return ERR_SERVICE_NOT_SUPPORTED;
            }
            Command cmdRq = dof.newCommand();
            final Dimse dimseRq;
            if (ianAccepted
                    && (preferInstanceAvailableNotification || !scnAccepted)) {
                cmdRq.initNCreateRQ(a.nextMsgID(),
                        UIDs.InstanceAvailabilityNotificationSOPClass,
                        uidGen.createUID());
                dimseRq = af.newDimse(PCID_IAN, cmdRq, order.getDataset());
            } else {
                cmdRq.initCStoreRQ(a.nextMsgID(),
                        UIDs.BasicStudyContentNotification,
                        uidGen.createUID(),
                        scnPriority);
                dimseRq = af.newDimse(PCID_SCN,
                        cmdRq,
                        toSCN(order.getDataset()));
            }
            logDataset("Dataset:\n", dimseRq.getDataset());
            final Dimse dimseRsp;
            try {
                dimseRsp = aa.invoke(dimseRq).get();
            } catch (InterruptedException ie) {
                log.error("Threading error during performing " + order);
                return ERR_THREAD;
            }
            final Command cmdRsp = dimseRsp.getCommand();
            final int status = cmdRsp.getStatus();
            switch (status) {
            case 0x0000:
            case 0x0001:
            case 0x0002:
            case 0x0003:
                return 0;
            case 0x0116:
                log
                        .warn("Received Warning Status 116H (=Attribute Value Out of Range) from remote AE "
                                + called);
                return 0;
            default:
                return status;
            }
        } catch (IOException e) {
            log.error("i/o exception during processing " + order, e);
            return ERR_IO;
        } finally {
            if (aa != null) try {
                aa.release(true);
            } catch (Exception e) {
                log.warn("Failed to release " + aa.getAssociation());
            }
        }
    }

    private Dataset toSCN(Dataset ian) {
        Dataset scn = dof.newDataset();
        scn.putLO(Tags.PatientID, ian.getString(Tags.PatientID));
        scn.putPN(Tags.PatientName, ian.getString(Tags.PatientName));
        scn.putSH(Tags.StudyID, ian.getString(Tags.StudyID));
        scn.putUI(Tags.StudyInstanceUID, ian.getString(Tags.StudyInstanceUID));
        DcmElement ianSeriesSeq = ian.get(Tags.RefSeriesSeq);
        DcmElement scnSeriesSeq = scn.putSQ(Tags.RefSeriesSeq);
        for (int i = 0, n = ianSeriesSeq.vm(); i < n; ++i) {
            Dataset ianSeries = ianSeriesSeq.getItem(i);
            Dataset scnSeries = scnSeriesSeq.addNewItem();
            scnSeries.putUI(Tags.SeriesInstanceUID, ianSeries
                    .getString(Tags.SeriesInstanceUID));
            DcmElement ianSOPSeq = ianSeries.get(Tags.RefSOPSeq);
            DcmElement scnSOPSeq = scnSeries.putSQ(Tags.RefImageSeq);
            for (int j = 0, m = ianSOPSeq.vm(); j < m; ++j) {
                scnSOPSeq.addItem(ianSOPSeq.getItem(i));
            }
        }
        return scn;
    }

    private Socket createSocket(AEData ianSCP) throws IOException {
        return new Socket(ianSCP.getHostName(), ianSCP.getPort());
    }

    void logDataset(String prompt, Dataset ds) {
        if (!log.isDebugEnabled()) { return; }
        try {
            StringWriter w = new StringWriter();
            w.write(prompt);
            ds.dumpDataset(w, dumpParam);
            log.debug(w.toString());
        } catch (Exception e) {
            log.warn("Failed to dump dataset", e);
        }
    }
}