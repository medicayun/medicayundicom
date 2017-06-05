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
import java.net.Socket;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
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
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;
import org.dcm4che.util.UIDGenerator;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.dcm.storescp.StoreScpService;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.dcm4chex.archive.mbean.FileSystemMgtService;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.notif.IANNotificationVO;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1913 $ $Date: 2005-08-25 16:32:49 +0800 (周四, 25 8月 2005) $
 * @since 27.08.2004
 */
public class IANScuService extends ServiceMBeanSupport implements
		MessageListener, NotificationListener {

	private static final String NONE = "NONE";

	private static final String[] EMPTY = {};

	private static final UIDGenerator uidGen = UIDGenerator.getInstance();

	private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
			UIDs.ImplicitVRLittleEndian };

	private static final int ERR_SOP_RJ = -2;

	private static final int ERR_ASSOC_RJ = -1;

	private static final int PCID_IAN = 1;

	private static final int PCID_SCN = 3;

	private static final int MSG_ID = 1;

	private static final String DEFAULT_CALLING_AET = "IAN_SCU";

	private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

	private ObjectName storeScpServiceName;
	private ObjectName fileSystemMgtServiceName;

	private String queueName;

	private String callingAET = DEFAULT_CALLING_AET;

	private int acTimeout;

	private int dimseTimeout;

	private int soCloseDelay;

	private boolean preferInstanceAvailableNotification = true;

	private boolean offerInstanceAvailableNotification = true;

	private boolean offerStudyContentNotification = true;

	private int scnPriority = 0;

	private String[] notifiedAETs = EMPTY;

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

	public final String getNotifiedAETs() {
		return notifiedAETs.length > 0 ? StringUtils.toString(notifiedAETs,
				'\\') : NONE;
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

	public ObjectName getFileSystemMgtServiceName() {
		return fileSystemMgtServiceName;
	}
	public void setFileSystemMgtServiceName(ObjectName fileSystemMgtServiceName) {
		this.fileSystemMgtServiceName = fileSystemMgtServiceName;
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

	protected void startService() throws Exception {
		JMSDelegate.startListening(queueName, this, concurrency);
		server.addNotificationListener(storeScpServiceName, this,
				StoreScpService.NOTIF_FILTER, null);
		server.addNotificationListener(fileSystemMgtServiceName, this,
				FileSystemMgtService.NOTIF_FILTER, null);
	}

	protected void stopService() throws Exception {
		server.removeNotificationListener(storeScpServiceName, this,
				StoreScpService.NOTIF_FILTER, null);
		server.removeNotificationListener(fileSystemMgtServiceName, this,
				FileSystemMgtService.NOTIF_FILTER, null);
		JMSDelegate.stopListening(queueName);
	}

	public void handleNotification(Notification notif, Object handback) {
    	IANNotificationVO ianVO = (IANNotificationVO) notif.getUserData();
        Map ians = ianVO.getIANs();
		if (ians == null)
			return;
		for (Iterator it = ians.values().iterator(); it.hasNext();) {
			Dataset ian = (Dataset) it.next();
			if (log.isDebugEnabled()) { log.debug("IAN Dataset:");log.debug(ian); }
			for (int i = 0; i < notifiedAETs.length; ++i) {
				IANOrder order = new IANOrder(notifiedAETs[i], ian);
				try {
					log.info("Scheduling " + order);
					JMSDelegate.queue(queueName, order,
							Message.DEFAULT_PRIORITY, 0L);
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

	private void process(IANOrder order) throws SQLException,
			UnkownAETException, IOException, DcmServiceException,
			InterruptedException {
		final String aet = order.getDestination();
		AEData aeData = new AECmd(aet).getAEData();
		if (aeData == null) {
			throw new UnkownAETException("Unkown Destination AET: " + aet);
		}
		AssociationFactory af = AssociationFactory.getInstance();
		Association a = af.newRequestor(createSocket(aeData));
		a.setAcTimeout(acTimeout);
		a.setDimseTimeout(dimseTimeout);
		a.setSoCloseDelay(soCloseDelay);
		AAssociateRQ rq = af.newAAssociateRQ();
		rq.setCalledAET(aet);
		rq.setCallingAET(callingAET);
		if (offerInstanceAvailableNotification)
			rq.addPresContext(af.newPresContext(PCID_IAN,
					UIDs.InstanceAvailabilityNotificationSOPClass, NATIVE_TS));
		if (offerStudyContentNotification)
			rq.addPresContext(af.newPresContext(PCID_SCN,
					UIDs.BasicStudyContentNotification, NATIVE_TS));
		PDU ac = a.connect(rq);
		if (!(ac instanceof AAssociateAC)) {
			throw new DcmServiceException(ERR_ASSOC_RJ,
					"Association not accepted by " + aet + ": " + ac);
		}
		ActiveAssociation aa = af.newActiveAssociation(a, null);
		aa.start();
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
		final boolean ianAccepted = a.getAcceptedTransferSyntaxUID(PCID_IAN) != null;
		final boolean scnAccepted = a.getAcceptedTransferSyntaxUID(PCID_SCN) != null;
		if (!ianAccepted && !scnAccepted) {
			throw new DcmServiceException(ERR_SOP_RJ,
					"Notification Service not supported by remote AE: "
							+ order.getDestination());
		}
		AssociationFactory af = AssociationFactory.getInstance();
		Command cmdRq = DcmObjectFactory.getInstance().newCommand();
		final Dimse dimseRq;
		if (ianAccepted
				&& (preferInstanceAvailableNotification || !scnAccepted)) {
			cmdRq.initNCreateRQ(a.nextMsgID(),
					UIDs.InstanceAvailabilityNotificationSOPClass, uidGen
							.createUID());
			dimseRq = af.newDimse(PCID_IAN, cmdRq, order.getDataset());
		} else {
			cmdRq.initCStoreRQ(a.nextMsgID(),
					UIDs.BasicStudyContentNotification, uidGen.createUID(),
					scnPriority);
			dimseRq = af.newDimse(PCID_SCN, cmdRq, toSCN(order.getDataset()));
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
			log.warn("Received Warning Status 116H " +
					"(=Attribute Value Out of Range) from remote AE "
							+ order.getDestination());
			break;
		default:
			throw new DcmServiceException(status,
					cmdRsp.getString(Tags.ErrorComment));
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

	Socket createSocket(AEData aeData) throws IOException {
		return tlsConfig.createSocket(aeData);
	}

}