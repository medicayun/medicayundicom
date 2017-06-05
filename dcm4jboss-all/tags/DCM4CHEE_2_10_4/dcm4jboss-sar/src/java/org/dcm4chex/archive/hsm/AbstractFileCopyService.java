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


package org.dcm4chex.archive.hsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.common.SeriesStored;
import org.dcm4chex.archive.config.ForwardingRules;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2628 $ $Date: 2006-07-13 02:05:16 +0800 (周四, 13 7月 2006) $
 * @since Jan 16, 2006
 */
public abstract class AbstractFileCopyService extends ServiceMBeanSupport
implements MessageListener, NotificationListener {

	private static final NotificationFilterSupport seriesStoredFilter = new NotificationFilterSupport();
	static {
		seriesStoredFilter.enableType(SeriesStored.class.getName());
	}

	protected ObjectName storeScpServiceName;

	protected String queueName;

	protected int concurrency = 1;

	protected int fileStatus = FileStatus.TO_ARCHIVE;

	protected boolean verifyCopy;

	protected ForwardingRules copyingRules = new ForwardingRules("");

	protected RetryIntervalls retryIntervalls = new RetryIntervalls();

	protected ObjectName fileSystemMgtName;

	protected int bufferSize = 8192;

	public final int getBufferSize() {
		return bufferSize;
	}
    
	public final void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public final ObjectName getFileSystemMgtName() {
		return fileSystemMgtName;
	}

	public final void setFileSystemMgtName(ObjectName fileSystemMgtName) {
		this.fileSystemMgtName = fileSystemMgtName;
	}
    
    protected String getRetrieveAET() {
        try {
            return (String) server.getAttribute(fileSystemMgtName,
                    "RetrieveAETitle");
        } catch (Exception e) {
            log.error("JMX error: ", e);
            return null;
        }
    }

	public String getEjbProviderURL() {
		return EJBHomeFactory.getEjbProviderURL();
	}

	public void setEjbProviderURL(String ejbProviderURL) {
		EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
	}

	public final ObjectName getStoreScpServiceName() {
		return storeScpServiceName;
	}

	public final void setStoreScpServiceName(ObjectName storeScpServiceName) {
		this.storeScpServiceName = storeScpServiceName;
	}

	public final int getConcurrency() {
		return concurrency;
	}

	public final String getCopyingRules() {
		return copyingRules.toString();
	}

	public final void setCopyingRules(String copyingRules) {
		this.copyingRules = new ForwardingRules(copyingRules.replace('\\', '/'));
	}

	public final String getFileStatus() {
		return FileStatus.toString(fileStatus);
	}

	public final void setFileStatus(String fileStatus) {
		this.fileStatus = FileStatus.toInt(fileStatus);
	}

	public final boolean isVerifyCopy() {
		return verifyCopy;
	}

	public final void setVerifyCopy(boolean verifyCopy) {
		this.verifyCopy = verifyCopy;
	}

	public final String getRetryIntervalls() {
		return retryIntervalls.toString();
	}

	public final void setRetryIntervalls(String s) {
		this.retryIntervalls = new RetryIntervalls(s);
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

	public final String getQueueName() {
		return queueName;
	}

	public final void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	protected void startService() throws Exception {
		JMSDelegate.startListening(queueName, this, concurrency);
		server.addNotificationListener(storeScpServiceName, this,
				seriesStoredFilter, null);
	}

	protected void stopService() throws Exception {
		server.removeNotificationListener(storeScpServiceName, this,
				seriesStoredFilter, null);
		JMSDelegate.stopListening(queueName);
	}

	public void handleNotification(Notification notif, Object handback) {
		SeriesStored seriesStored = (SeriesStored) notif.getUserData();
		Map param = new HashMap();
		param.put("calling", new String[] { seriesStored.getCallingAET() });
		String[] dests = copyingRules.getForwardDestinationsFor(param);
        if (dests.length == 0)
            return;
        List fileInfos;
        try {
            fileInfos = toFileInfos(seriesStored.getIAN());
        } catch (Exception e) {
            log.error("Failed to query DB for fileInfos:", e);
            // TODO Error handling
            return;
        }
		for (int i = 0; i < dests.length; i++) {
			final String dest = ForwardingRules.toAET(dests[i]);
			final long scheduledTime = ForwardingRules
					.toScheduledTime(dests[i]);
			scheduleCopy(fileInfos, dest, scheduledTime);
		}
	}
    
	private List toFileInfos(Dataset ian) throws Exception {
        Dataset refSeriesSeq = ian.getItem(Tags.RefSeriesSeq);
        DcmElement refSOPSeq = refSeriesSeq.get(Tags.RefSOPSeq);
        FileInfo[][] aa = RetrieveCmd.create(refSOPSeq).getFileInfos();
        List list = new ArrayList(aa.length);
        String retrieveAET = getRetrieveAET();
        for (int i = 0; i < aa.length; i++) {
            FileInfo[] a = aa[i];
            for (int j = 0; j < a.length; j++) {
                if (a[j].fileRetrieveAET.equals(retrieveAET)) {
                    list.add(a[j]);
                    break;
                }
            }
        }
	    return list;
	}

	public void scheduleCopy(List fileInfos, String destFsPath,
			long scheduledTime) {
		schedule(new FileCopyOrder(fileInfos, destFsPath), scheduledTime);
	}

	private void schedule(FileCopyOrder order, long scheduledTime) {
		try {
			log.info("Scheduling " + order);
			JMSDelegate.queue(queueName, order, Message.DEFAULT_PRIORITY,
					scheduledTime);
		} catch (JMSException e) {
			log.error("Failed to schedule " + order, e);
		}
	}

	public void onMessage(Message message) {
		ObjectMessage om = (ObjectMessage) message;
		try {
			FileCopyOrder order = (FileCopyOrder) om.getObject();
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
					schedule(order, System.currentTimeMillis() + delay);
				}
			}
		} catch (Throwable e) {
			log.error("unexpected error during processing message: " + message,
					e);
		}
	}

	protected abstract void process(FileCopyOrder order) throws Exception;


    static StorageHome getStorageHome() throws HomeFactoryException {
        return (StorageHome) EJBHomeFactory.getFactory().lookup(
                StorageHome.class, StorageHome.JNDI_NAME);
    }

}
