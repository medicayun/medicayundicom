/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.ppsscp;

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
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1799 $ $Date: 2005-06-22 20:47:51 +0800 (周三, 22 6月 2005) $
 * @since 12.04.2005
 * 
 */

public class PPSScuService extends ServiceMBeanSupport implements
		MessageListener, NotificationListener {

	private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
			UIDs.ImplicitVRLittleEndian };

	private static final int ERR_GPPPS_RJ = -2;

	private static final int ERR_ASSOC_RJ = -1;

	private static final int PCID_GPPPS = 1;

	private static final String NONE = "NONE";

	private static final String[] EMPTY = {};

    private static final int[] EXCLUDE_TAGS = { Tags.SOPClassUID, Tags.SOPInstanceUID};

	private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

	private RetryIntervalls retryIntervalls = new RetryIntervalls();

	private String callingAET;

	private String[] forwardAETs = EMPTY;

	private ObjectName ppsScpServiceName;

	private String queueName;

	private int acTimeout;

	private int dimseTimeout;

	private int soCloseDelay;

	private int concurrency = 1;

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

	public final ObjectName getPpsScpServiceName() {
		return ppsScpServiceName;
	}

	public final void setPpsScpServiceName(ObjectName mppsScpServiceName) {
		this.ppsScpServiceName = mppsScpServiceName;
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
		JMSDelegate.startListening(queueName, this, concurrency);
		server.addNotificationListener(ppsScpServiceName, this,
				PPSScpService.NOTIF_FILTER, null);
	}

	protected void stopService() throws Exception {
		server.removeNotificationListener(ppsScpServiceName, this,
				PPSScpService.NOTIF_FILTER, null);
		JMSDelegate.stopListening(queueName);
	}

	public void handleNotification(Notification notif, Object handback) {
		Dataset mpps = (Dataset) notif.getUserData();
		for (int i = 0; i < forwardAETs.length; i++) {
			PPSOrder order = new PPSOrder(mpps, forwardAETs[i]);
			try {
				log.info("Scheduling " + order);
				JMSDelegate.queue(queueName, order, Message.DEFAULT_PRIORITY,
						0L);
			} catch (JMSException e) {
				log.error("Failed to schedule " + order, e);
			}
		}

	}

	public void onMessage(Message message) {
		ObjectMessage om = (ObjectMessage) message;
		try {
			PPSOrder order = (PPSOrder) om.getObject();
            log.info("Start processing " + order);
			try {
	            sendPPS(order.isCreate(), order.getDataset(), order.getDestination());
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
					JMSDelegate.queue(queueName, order, 0, System
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

	void sendPPS(boolean create, Dataset mpps, String aet)
			throws InterruptedException, IOException, UnkownAETException,
			SQLException, DcmServiceException {
		AEData aeData = new AECmd(aet).getAEData();
		if (aeData == null) {
			throw new UnkownAETException("Unkown Destination AET: "
					+ aet);
		}
		AssociationFactory af = AssociationFactory.getInstance();
		Association a = af.newRequestor(createSocket(aeData));
		a.setAcTimeout(acTimeout);
		a.setDimseTimeout(dimseTimeout);
		a.setSoCloseDelay(soCloseDelay);
		AAssociateRQ rq = af.newAAssociateRQ();
		rq.setCalledAET(aet);
		rq.setCallingAET(callingAET);
		rq.addPresContext(af.newPresContext(PCID_GPPPS,
				UIDs.GeneralPurposePerformedProcedureStepSOPClass, NATIVE_TS));
		PDU ac = a.connect(rq);
		if (!(ac instanceof AAssociateAC)) {
			throw new DcmServiceException(ERR_ASSOC_RJ,
					"Association not accepted by " + aet + ": " + ac);
		}
		ActiveAssociation aa = af.newActiveAssociation(a, null);
		try {
			aa.start();
			if (a.getAcceptedTransferSyntaxUID(PCID_GPPPS) == null) {
				throw new DcmServiceException(ERR_GPPPS_RJ,
						"GPPPS not supported by remote AE: " + aet);
			}
			DcmObjectFactory dof = DcmObjectFactory.getInstance();
			Command cmdRq = dof.newCommand();
			if (create) {
				cmdRq.initNCreateRQ(a.nextMsgID(),
						UIDs.GeneralPurposePerformedProcedureStepSOPClass, mpps
								.getString(Tags.SOPInstanceUID));
			} else {
				cmdRq.initNSetRQ(a.nextMsgID(),
						UIDs.GeneralPurposePerformedProcedureStepSOPClass, mpps
								.getString(Tags.SOPInstanceUID));
			}
			Dimse dimseRq = af.newDimse(PCID_GPPPS, cmdRq, mpps
					.exclude(EXCLUDE_TAGS));
			final Dimse dimseRsp = aa.invoke(dimseRq).get();
			final Command cmdRsp = dimseRsp.getCommand();
			final int status = cmdRsp.getStatus();
			switch (status) {
			case 0x0000:
				break;
			case 0x0116:
				log.warn("Received Warning Status 116H (=Attribute Value Out of Range) from remote AE "
								+ aet);
				break;
			default:
				throw new DcmServiceException(status, cmdRsp
						.getString(Tags.ErrorComment));
			}
		} finally {
			try {
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
