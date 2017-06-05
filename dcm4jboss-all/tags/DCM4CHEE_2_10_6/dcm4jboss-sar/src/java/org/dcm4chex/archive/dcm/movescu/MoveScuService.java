/* ***** BEGIN LICENSE BLOCK *****
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

package org.dcm4chex.archive.dcm.movescu;

import java.io.IOException;
import java.sql.SQLException;

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
import org.dcm4chex.archive.mbean.JMSDelegate;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2836 $ $Date: 2006-10-09 22:31:41 +0800 (周一, 09 10月 2006) $
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
        
        private String queueName;

        private JMSDelegate jmsDelegate = new JMSDelegate(this);

        public final ObjectName getJmsServiceName() {
            return jmsDelegate.getJmsServiceName();
        }

        public final void setJmsServiceName(ObjectName jmsServiceName) {
            jmsDelegate.setJmsServiceName(jmsServiceName);
        }
        
        public final String getQueueName() {
            return queueName;
        }
        
        public final void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public final ObjectName getTLSConfigName() {
		return tlsConfig.getTLSConfigName();
	}

	public final void setTLSConfigName(ObjectName tlsConfigName) {
		tlsConfig.setTLSConfigName(tlsConfigName);
	}

    public final int getReceiveBufferSize() {
        return tlsConfig.getReceiveBufferSize();        
    }
    
    public final void setReceiveBufferSize(int size) {
        tlsConfig.setReceiveBufferSize(size);
    }

    public final int getSendBufferSize() {
        return tlsConfig.getSendBufferSize();        
    }
    
    public final void setSendBufferSize(int size) {
        tlsConfig.setSendBufferSize(size);
    }
        
    public final boolean isTcpNoDelay() {
        return tlsConfig.isTcpNoDelay();
    }

    public final void setTcpNoDelay(boolean on) {
        tlsConfig.setTcpNoDelay(on);
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
	
	public void scheduleMoveSeries(String retrieveAET, String destAET,
			int priority, String pid, String studyIUID, String seriesIUID,
			long scheduledTime) {
		scheduleMoveOrder(new MoveOrder(retrieveAET, destAET, priority, pid,
				studyIUID, seriesIUID), scheduledTime);
	}

        public void scheduleMove(String retrieveAET, String destAET,
                int priority, String pid, String[] studyIUIDs,
                String[] seriesIUIDs, String[] sopIUIDs, long scheduledTime) {
            scheduleMoveOrder(new MoveOrder(retrieveAET, destAET, priority, pid,
                        studyIUIDs, seriesIUIDs, sopIUIDs), scheduledTime);
        }
        
	private void scheduleMoveOrder(MoveOrder order, long scheduledTime) {
		try {
			jmsDelegate.queue(queueName, order,
					JMSDelegate.toJMSPriority(order.getPriority()),
					scheduledTime);
		} catch (Exception e) {
			log.error("Failed to schedule order: " + order);
		}
	}
	
	protected void startService() throws Exception {
		jmsDelegate.startListening(queueName, this, concurrency);
	}

	protected void stopService() throws Exception {
		jmsDelegate.stopListening(queueName);
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
					scheduleMoveOrder(order, System.currentTimeMillis() + delay);
				}
			}
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
                UIDs.PatientRootQueryRetrieveInformationModelMOVE,
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
						"Patient Root Query Retrieve IM MOVE not supported by remote AE: " 
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
                UIDs.PatientRootQueryRetrieveInformationModelMOVE,
                order.getPriority(),
                order.getMoveDestination());
        Dataset ds = dof.newDataset();
        ds.putCS(Tags.QueryRetrieveLevel, order.getQueryRetrieveLevel());
        putLO(ds, Tags.PatientID, order.getPatientId());
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

	private static void putLO(Dataset ds, int tag, String s) {
		if (s != null)
			ds.putLO(tag, s);
	}

	private static void putUI(Dataset ds, int tag, String[] uids) {
		if (uids != null)
			ds.putUI(tag, uids);
	}
}