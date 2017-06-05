/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.stymgt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
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
import org.dcm4chex.archive.config.ForwardingRules;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.exceptions.UnkownAETException;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1815 $ $Date: 2005-06-23 19:21:45 +0800 (周四, 23 6月 2005) $
 * @since 15.11.2004
 *
 */
public class StudyMgtScuService extends ServiceMBeanSupport implements
        MessageListener, NotificationListener {

	private static final int ERR_STYMGT_RJ = -2;

	private static final int ERR_ASSOC_RJ = -1;

	private static final int MSG_ID = 1;

	private static final int PCID_STYMGT = 1;

	private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian};

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private RetryIntervalls retryIntervalls = new RetryIntervalls();

    private String callingAET;

    private ForwardingRules forwardingRules = new ForwardingRules("");

    private ObjectName scpServiceName;
    
    private String queueName;

    private int acTimeout;

    private int dimseTimeout;

    private int soCloseDelay;
	
	private boolean retryIfNoSuchSOPInstance = false;

	private int concurrency = 1;

	public final int getConcurrency() {
		return concurrency;
	}

	public final void setConcurrency(int concurrency) throws Exception {
		if (concurrency <= 0)
			throw new IllegalArgumentException("Concurrency: " + concurrency);
		if (this.concurrency  != concurrency) {
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

    public final String getForwardingRules() {
        return forwardingRules.toString();
    }

    public final void setForwardingRules(String forwardingRules) {
        this.forwardingRules = new ForwardingRules(forwardingRules);
    }

    public final ObjectName getStudyMgtScpServiceName() {
        return scpServiceName;
    }

	public boolean isRetryIfNoSuchSOPInstance() {
		return retryIfNoSuchSOPInstance;
	}

	public void setRetryIfNoSuchSOPInstance(boolean retry) {
		this.retryIfNoSuchSOPInstance = retry;
	}

	public final void setStudyMgtScpServiceName(ObjectName scpServiceName) {
        this.scpServiceName = scpServiceName;
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
        server.addNotificationListener(scpServiceName,
                this,
                StudyMgtScpService.NOTIF_FILTER,
                null);
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(scpServiceName,
                this,
				StudyMgtScpService.NOTIF_FILTER,
                null);
        JMSDelegate.stopListening(queueName);
    }

    public void handleNotification(Notification notif, Object handback) {
		StudyMgtOrder order = (StudyMgtOrder) notif.getUserData();
		forward(order.getCallingAET(), order.getCalledAET(),
				order.getSOPInstanceUID(), order.getCommandField(),
				order.getActionTypeID(), order.getDataset());
    }

 	public int forward(String origCallingAET, String origCalledAET,
			String iuid, int commandField, int actionTypeID, Dataset dataset) {
		int count = 0;
		Map keys = new HashMap();
		keys.put("calling", new String[]{origCallingAET});
		keys.put("called", new String[]{origCalledAET});
		keys.put("command", new String[]{StudyMgtOrder.commandAsString(commandField, actionTypeID)});
		String[] forwardAETs = forwardingRules.getForwardDestinationsFor(keys);
		for (int i = 0; i < forwardAETs.length; i++) {
			StudyMgtOrder order = new StudyMgtOrder(
					origCallingAET, forwardAETs[i],
					commandField, actionTypeID,
					iuid, dataset);
            try {
                log.info("Scheduling " + order);
                JMSDelegate.queue(queueName,
                        order,
                        Message.DEFAULT_PRIORITY,
                        0L);
				++count;
            } catch (JMSException e) {
                log.error("Failed to schedule " + order, e);
            }
        }
		return count;
	}
	
    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        try {
            StudyMgtOrder order = (StudyMgtOrder) om.getObject();
            log.info("Start processing " + order);
			try {
				try {
					switch (order.getCommandField()) {
					case Command.N_ACTION_RQ:
						naction(order
						.getCalledAET(), order.getSOPInstanceUID(), order
										.getActionTypeID(), order.getDataset());
						break;
					case Command.N_CREATE_RQ:
						ncreate(order.getCalledAET(), order.getSOPInstanceUID(),
								order.getDataset());
						break;
					case Command.N_DELETE_RQ:
						ndelete(order.getCalledAET(), order.getSOPInstanceUID());
						break;
					case Command.N_SET_RQ:
						nset(order.getCalledAET(), order.getSOPInstanceUID(),
								order.getDataset());
						break;
					}
				} catch (DcmServiceException e) {
					if (e.getStatus() != Status.NoSuchObjectInstance
							|| retryIfNoSuchSOPInstance)
						throw e;
					log.info("No such SOP Instance for " + order);
				}
				order.setException(null);
				log.info("Finished processing " + order);
			} catch (Exception e) {
	            final int failureCount = order.getFailureCount() + 1;
	            order.setFailureCount(failureCount);
				order.setException(e);
	            final long delay = retryIntervalls.getIntervall(failureCount);
	            if (delay == -1L) {
	                log.error("Give up to process " + order);
	            } else {
	                log.warn("Failed to process " + order + ". Scheduling retry.");
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

	public void ncreate(String aet, String iuid, Dataset ds)
			throws DcmServiceException, UnkownAETException, IOException, 
					SQLException, InterruptedException {
		Command cmd = DcmObjectFactory.getInstance().newCommand();
		cmd.initNCreateRQ(MSG_ID, UIDs.TianiStudyManagement, iuid);
		checkStatus(invoke(aet, cmd, ds).getCommand());
	}

	public void nset(String aet, String iuid, Dataset ds)
		throws DcmServiceException, UnkownAETException, IOException, 
				SQLException, InterruptedException {
		Command cmd = DcmObjectFactory.getInstance().newCommand();
		cmd.initNSetRQ(MSG_ID, UIDs.TianiStudyManagement, iuid);
		checkStatus(invoke(aet, cmd, ds).getCommand());
	}
	
	public void naction(String aet, String iuid, int actionTypeID,
			Dataset ds) 
			throws DcmServiceException, UnkownAETException, IOException, 
					SQLException, InterruptedException {
		Command cmd = DcmObjectFactory.getInstance().newCommand();
		cmd.initNActionRQ(MSG_ID, UIDs.TianiStudyManagement, iuid, actionTypeID);
		checkStatus(invoke(aet, cmd, ds).getCommand());
	}

	public void ndelete(String aet, String iuid)
		throws DcmServiceException, UnkownAETException, IOException, 
				SQLException, InterruptedException {
		Command cmd = DcmObjectFactory.getInstance().newCommand();
		cmd.initNDeleteRQ(MSG_ID, UIDs.TianiStudyManagement, iuid);
		checkStatus(invoke(aet, cmd, null).getCommand());
	}

	private Dimse invoke(String aet, Command cmd, Dataset ds)
			throws DcmServiceException, UnkownAETException, IOException, 
				SQLException, InterruptedException {
        AEData aeData = new AECmd(aet).getAEData();
        if (aeData == null) {
			throw new UnkownAETException("Unkown Destination AET: " + aet);
        }
     	AssociationFactory af = AssociationFactory.getInstance();        
        Association a = af.newRequestor(tlsConfig.createSocket(aeData));
        a.setAcTimeout(acTimeout);
        a.setDimseTimeout(dimseTimeout);
        a.setSoCloseDelay(soCloseDelay);
        AAssociateRQ rq = af.newAAssociateRQ();
        rq.setCalledAET(aet);
        rq.setCallingAET(callingAET);
        rq.addPresContext(af.newPresContext(PCID_STYMGT,
                UIDs.TianiStudyManagement,
                NATIVE_TS));
        PDU ac = a.connect(rq);
        if (!(ac instanceof AAssociateAC)) {
			throw new DcmServiceException(ERR_ASSOC_RJ, 
					"Association not accepted by " + aet + ": " + ac);
        }
		ActiveAssociation aa = af.newActiveAssociation(a, null);
        aa.start();
		try {
	        if (a.getAcceptedTransferSyntaxUID(PCID_STYMGT) == null)
				throw new DcmServiceException(ERR_STYMGT_RJ, 
						"Tiani Study Mgt Service not supported by remote AE: " 
						+ aet);
			return aa.invoke(af.newDimse(PCID_STYMGT, cmd, ds)).get();
		} finally {
			try {
				aa.release(true);
			} catch (Exception e) {
				log.warn("Failed to release association " + aa.getAssociation(), e);
			}
		}
	}

	private void checkStatus(Command cmdRsp) throws DcmServiceException {
		if (cmdRsp.getStatus() != 0) {
			throw new DcmServiceException(cmdRsp.getStatus(), 
					cmdRsp.getString(Tags.ErrorComment));
		}
	}
	
}