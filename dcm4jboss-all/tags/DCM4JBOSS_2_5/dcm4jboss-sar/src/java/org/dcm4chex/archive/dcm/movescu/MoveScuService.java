/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.movescu;

import java.io.IOException;
import java.sql.SQLException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.ObjectName;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
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
 * @since 17.12.2003
 */
public class MoveScuService extends ServiceMBeanSupport implements
		MessageListener {

	private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
			UIDs.ImplicitVRLittleEndian };

	private static final int ERR_MOVE_RJ = -2;

	private static final int ERR_ASSOC_RJ = -1;

	private static final int PCID_MOVE = 1;

	private static final String DEF_CALLING_AET = "MOVE_SCU";

	private static final String DEF_CALLED_AET = "QR_SCP";

	private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

	private String callingAET = DEF_CALLING_AET;

	private String calledAET = DEF_CALLED_AET;

	private int acTimeout;

	private int dimseTimeout;

	private int soCloseDelay;

	private RetryIntervalls retryIntervalls = new RetryIntervalls();

	private int concurrency = 1;

	public final ObjectName getTLSConfigName() {
		return tlsConfig.getTLSConfigName();
	}

	public final void setTLSConfigName(ObjectName tlsConfigName) {
		tlsConfig.setTLSConfigName(tlsConfigName);
	}

	public final String getCalledAET() {
		return calledAET;
	}

	public final void setCalledAET(String retrieveAET) {
		this.calledAET = retrieveAET;
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

	public String getRetryIntervalls() {
		return retryIntervalls.toString();
	}

	public void setRetryIntervalls(String text) {
		retryIntervalls = new RetryIntervalls(text);
	}

	public String getCallingAET() {
		return callingAET;
	}

	public void setCallingAET(String aet) {
		this.callingAET = aet;
	}

	protected void startService() throws Exception {
		JMSDelegate.startListening(MoveOrder.QUEUE, this, concurrency);
	}

	protected void stopService() throws Exception {
		JMSDelegate.stopListening(MoveOrder.QUEUE);
	}

	void queueFailedMoveOrder(MoveOrder order) {
		final long delay = retryIntervalls
				.getIntervall(order.getFailureCount());
		if (delay == -1L) {
			log.error("Give up to process move order: " + order);
		} else {
			log.warn("Failed to process " + order + ". Scheduling retry.");
			try {
				JMSDelegate.queue(MoveOrder.QUEUE, order, 0, System
						.currentTimeMillis()
						+ delay);
			} catch (JMSException e) {
				log.error("Failed to reschedule order: " + order);
			}
		}
	}

	public void onMessage(Message message) {
		ObjectMessage om = (ObjectMessage) message;
		try {
			MoveOrder order = (MoveOrder) om.getObject();
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
					JMSDelegate.queue(MoveOrder.QUEUE, order, 0, System
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

	private void process(MoveOrder order) throws SQLException,
			UnkownAETException, IOException, DcmServiceException,
			InterruptedException {
		String aet = order.getRetrieveAET();
		if (aet == null) {
			aet = calledAET;
		}
		AEData aeData = new AECmd(aet).getAEData();
		if (aeData == null) {
			throw new UnkownAETException("Unkown Retrieve AET: " + aet);
		}
		AssociationFactory af = AssociationFactory.getInstance();
		Association a = af.newRequestor(tlsConfig.createSocket(aeData));
		a.setAcTimeout(acTimeout);
		a.setDimseTimeout(dimseTimeout);
		a.setSoCloseDelay(soCloseDelay);
		AAssociateRQ rq = af.newAAssociateRQ();
		rq.setCalledAET(aet);
		rq.setCallingAET(callingAET);
        rq.addPresContext(af.newPresContext(PCID_MOVE,
                UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                NATIVE_TS));
		PDU ac = a.connect(rq);
		if (!(ac instanceof AAssociateAC)) {
			throw new DcmServiceException(ERR_ASSOC_RJ,
					"Association not accepted by " + aet + ": " + ac);
		}
		ActiveAssociation aa = af.newActiveAssociation(a, null);
		aa.start();
		try {
	        if (a.getAcceptedTransferSyntaxUID(PCID_MOVE) == null)
				throw new DcmServiceException(ERR_MOVE_RJ, 
						"Study Root Query Retrieve IM MOVE not supported by remote AE: " 
						+ aet);
			invokeDimse(aa, order);
		} finally {
			try {
				aa.release(true);
                // workaround to ensure that the final MOVE-RSP is processed
                // before to continue 
                Thread.sleep(10);
			} catch (Exception e) {
				log.warn(
						"Failed to release association " + aa.getAssociation(),
						e);
			}
		}
	}

	private void invokeDimse(ActiveAssociation aa, MoveOrder order)
			throws InterruptedException, IOException, DcmServiceException  {
		AssociationFactory af = AssociationFactory.getInstance();
		DcmObjectFactory dof = DcmObjectFactory.getInstance();
		Command cmd = dof.newCommand();
		cmd.initCMoveRQ(aa.getAssociation().nextMsgID(),
                UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                order.getPriority(),
                order.getMoveDestination());
        Dataset ds = dof.newDataset();
        ds.putCS(Tags.QueryRetrieveLevel, order.getQueryRetrieveLevel());
        putUI(ds, Tags.StudyInstanceUID, order.getStudyIuids());
        putUI(ds, Tags.StudyInstanceUID, order.getStudyIuids());
        putUI(ds, Tags.SeriesInstanceUID, order.getSeriesIuids());
        putUI(ds, Tags.SOPInstanceUID, order.getSopIuids());
		log.debug("Move Identifier:\n");
		log.debug(ds);
        Dimse dimseRsp = aa.invoke(af.newDimse(PCID_MOVE, cmd, ds)).get();
        Command cmdRsp = dimseRsp.getCommand();
        int status = cmdRsp.getStatus();
		if (status != 0) {
	        if (status == Status.SubOpsOneOrMoreFailures 
					&& order.getSopIuids() != null) {
	            Dataset moveRspData = dimseRsp.getDataset();
	            if (moveRspData != null) {
	                String[] failedUIDs = 
							ds.getStrings(Tags.FailedSOPInstanceUIDList);
	                if (failedUIDs != null && failedUIDs.length != 0) {
	                    order.setSopIuids(failedUIDs);
	                }
	            }
	        }
			throw new DcmServiceException(status, cmdRsp
					.getString(Tags.ErrorComment));
		}
	}

	private static void putUI(Dataset ds, int tag, String[] uids) {
		if (uids != null)
			ds.putUI(tag, uids);
	}
}