/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.mppsscu;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
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
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.dcm.mppsscp.MPPSScpService;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1577 $ $Date: 2005-03-02 02:22:52 +0800 (周三, 02 3月 2005) $
 * @since 15.11.2004
 *
 */
public class MPPSScuService extends ServiceMBeanSupport implements
        MessageListener, NotificationListener {

    private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian};

    private static final int ERR_SQL = -1;

    private static final int ERR_UNKOWN_DEST = -2;

    private static final int ERR_IO = -3;

    private static final int ERR_ASSOC_NOT_ACCEPTED = -4;

    private static final int ERR_SERVICE_NOT_SUPPORTED = -5;

    private static final int ERR_THREAD = -6;

    private static final int PCID_MPPS = 1;

    private static final String NONE = "NONE";

    private static final String[] EMPTY = {};

    private static final int[] IUID = { Tags.SOPInstanceUID};

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private RetryIntervalls retryIntervalls = new RetryIntervalls();

    private String callingAET;

    private String[] forwardAETs = EMPTY;

    private ObjectName mppsScpServiceName;
    
    private String queueName;

    private int acTimeout;

    private int dimseTimeout;

    private int soCloseDelay;

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

    public final String getForwardAETs() {
        return forwardAETs.length > 0 ? StringUtils.toString(forwardAETs, '\\')
                : NONE;
    }

    public final void setForwardAETs(String forwardAETs) {
        this.forwardAETs = NONE.equalsIgnoreCase(forwardAETs) ? EMPTY
                : StringUtils.split(forwardAETs, '\\');
    }

    public final ObjectName getMppsScpServiceName() {
        return mppsScpServiceName;
    }

    public final void setMppsScpServiceName(ObjectName mppsScpServiceName) {
        this.mppsScpServiceName = mppsScpServiceName;
    }

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }
    
    public final String getQueueName() {
        return queueName;
    }
    
    public final void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    
    public final String getCallingAET() {
        return callingAET;
    }

    public final void setCallingAET(String callingAET) {
        this.callingAET = callingAET;
    }

    public String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public void setRetryIntervalls(String text) {
        retryIntervalls = new RetryIntervalls(text);
    }

    protected void startService() throws Exception {
        JMSDelegate.startListening(queueName, this);
        server.addNotificationListener(mppsScpServiceName,
                this,
                MPPSScpService.NOTIF_FILTER,
                null);
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(mppsScpServiceName,
                this,
                MPPSScpService.NOTIF_FILTER,
                null);
        JMSDelegate.stopListening(queueName);
    }

    public void handleNotification(Notification notif, Object handback) {
        Dataset mpps = (Dataset) notif.getUserData();
        for (int i = 0; i < forwardAETs.length; i++) {
            MPPSOrder order = new MPPSOrder(mpps, forwardAETs[i]);
            try {
                log.info("Scheduling " + order);
                JMSDelegate.queue(queueName,
                        order,
                        Message.DEFAULT_PRIORITY,
                        0L);
            } catch (JMSException e) {
                log.error("Failed to schedule " + order, e);
            }
        }

    }

    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        try {
            MPPSOrder order = (MPPSOrder) om.getObject();
            log.info("Start processing " + order);
            final int status = sendMPPS(order.getDataset(), order.getDestination());
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
                JMSDelegate.queue(queueName, order, 0, System
                        .currentTimeMillis()
                        + delay);
            }
        } catch (JMSException e) {
            log.error("jms error during processing message: " + message, e);
        } catch (Throwable e) {
            log.error("unexpected error during processing message: " + message,
                    e);
        }
    }

    public int sendMPPS(Dataset mpps, String destination) {
        AEData aeData = null;
        try {
            aeData = new AECmd(destination).execute();
        } catch (SQLException e1) {
            log.error("Failed to access DB for resolving AET: " + destination,
                    e1);
            return ERR_SQL;
        }
        if (aeData == null) {
            log.error("Unkown Destination AET: " + destination);
            return ERR_UNKOWN_DEST;
        }
        ActiveAssociation aa = null;
        try {
        	AssociationFactory af = AssociationFactory.getInstance();        
            Association a = af.newRequestor(createSocket(aeData));
            a.setAcTimeout(acTimeout);
            a.setDimseTimeout(dimseTimeout);
            a.setSoCloseDelay(soCloseDelay);
            AAssociateRQ rq = af.newAAssociateRQ();
            rq.setCalledAET(destination);
            rq.setCallingAET(callingAET);
            rq.addPresContext(af.newPresContext(PCID_MPPS,
                    UIDs.ModalityPerformedProcedureStep,
                    NATIVE_TS));
            PDU ac = a.connect(rq);
            if (!(ac instanceof AAssociateAC)) {
                log.error("Association not accepted by " + destination + ": " + ac);
                return ERR_ASSOC_NOT_ACCEPTED;
            }
            aa = af.newActiveAssociation(a, null);
            aa.start();
            if (a.getAcceptedTransferSyntaxUID(PCID_MPPS) == null) {
                log.error("MPPS Service not supported by remote AE: " + destination);
                return ERR_SERVICE_NOT_SUPPORTED;
            }
	        DcmObjectFactory dof = DcmObjectFactory.getInstance();
	        Command cmdRq = dof.newCommand();
            if (mpps.contains(Tags.ScheduledStepAttributesSeq)) {
	            cmdRq.initNCreateRQ(a.nextMsgID(),
	                    UIDs.ModalityPerformedProcedureStep,
	                    mpps.getString(Tags.SOPInstanceUID));
            } else {
                cmdRq.initNSetRQ(a.nextMsgID(),
	                    UIDs.ModalityPerformedProcedureStep,
	                    mpps.getString(Tags.SOPInstanceUID));
            }
            Dimse dimseRq = af.newDimse(PCID_MPPS, cmdRq, mpps.exclude(IUID));
            final Dimse dimseRsp;
            try {
                dimseRsp = aa.invoke(dimseRq).get();
            } catch (InterruptedException ie) {
                log.error("Threading error during waiting for response of " + cmdRq);
                return ERR_THREAD;
            }
            final Command cmdRsp = dimseRsp.getCommand();
            final int status = cmdRsp.getStatus();
            switch (status) {
            case 0x0000:
                return 0;
            case 0x0116:
                log.warn("Received Warning Status 116H (=Attribute Value Out of Range) from remote AE "
                                + destination);
                return 0;
            default:
                return status;
            }
        } catch (IOException e) {
            log.error("i/o exception during sending MPPS to " + destination, e);
            return ERR_IO;
        } finally {
            if (aa != null) try {
                aa.release(true);
            } catch (Exception e) {
                log.warn("Failed to release " + aa.getAssociation());
            }
        }
    }

    Socket createSocket(AEData aeData) throws IOException {
        return tlsConfig.createSocket(aeData);
    }
}