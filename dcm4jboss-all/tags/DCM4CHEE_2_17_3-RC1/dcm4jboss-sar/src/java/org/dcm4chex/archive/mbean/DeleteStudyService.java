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
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4chex.archive.mbean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Notification;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.ActionOrder;
import org.dcm4chex.archive.common.BaseJmsOrder;
import org.dcm4chex.archive.common.DeleteStudyOrder;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2Home;
import org.dcm4chex.archive.exceptions.ConcurrentStudyStorageException;
import org.dcm4chex.archive.exceptions.NoSuchSeriesException;
import org.dcm4chex.archive.exceptions.NoSuchStudyException;
import org.dcm4chex.archive.notif.StudyDeleted;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Sep 22, 2008
 */
public class DeleteStudyService extends ServiceMBeanSupport
        implements MessageListener {

    private JMSDelegate jmsDelegate = new JMSDelegate(this);

    private String deleteStudyQueueName;

    private RetryIntervalls retryIntervalsForJmsOrder = new RetryIntervalls();

    private boolean deleteStudyFromDB;

    private boolean deletePatientWithoutObjects;

    private boolean createIANonStudyDelete = false;

    private boolean deleteSeriesBySeries;

    public boolean isDeleteSeriesBySeries() {
        return deleteSeriesBySeries;
    }

    public void setDeleteSeriesBySeries(boolean deleteSeriesBySeries) {
        this.deleteSeriesBySeries = deleteSeriesBySeries;
    }

    public boolean isDeleteStudyFromDB() {
        return deleteStudyFromDB;
    }

    public void setDeleteStudyFromDB(boolean deleteStudyFromDB) {
        this.deleteStudyFromDB = deleteStudyFromDB;
    }

    public boolean isDeletePatientWithoutObjects() {
        return deletePatientWithoutObjects;
    }

    public void setDeletePatientWithoutObjects(boolean deletePatientWithoutObjects) {
        this.deletePatientWithoutObjects = deletePatientWithoutObjects;
    }

    public boolean isCreateIANonStudyDelete() {
        return createIANonStudyDelete;
    }

    public void setCreateIANonStudyDelete(boolean createIANonStudyDelete) {
        this.createIANonStudyDelete = createIANonStudyDelete;
    }

    public final String getRetryIntervalsForJmsOrder() {
        return retryIntervalsForJmsOrder.toString();
    }

    public final void setRetryIntervalsForJmsOrder(String s) {
        this.retryIntervalsForJmsOrder = new RetryIntervalls(s);
    }

    public String getDeleteStudyQueueName() {
        return deleteStudyQueueName;
    }

    public void setDeleteStudyQueueName(String deleteStudyQueueName) {
        this.deleteStudyQueueName = deleteStudyQueueName;
    }

    public ObjectName getJmsServiceName() {
        return jmsDelegate.getJmsServiceName();
    }

    public void setJmsServiceName(ObjectName jmsServiceName) {
        jmsDelegate.setJmsServiceName(jmsServiceName);
    }

    @Override
	protected void startService() throws Exception {
        jmsDelegate.startListening(deleteStudyQueueName, this , 1);
    }

    @Override
	protected void stopService() throws Exception {
        jmsDelegate.stopListening(deleteStudyQueueName);
    }

    public void scheduleDeleteOrder(DeleteStudyOrder order) throws Exception {
        schedule(order, 0);
    }

    protected void schedule(BaseJmsOrder order, long scheduledTime)
            throws Exception {
        if (log.isInfoEnabled()) {
            String scheduledTimeStr = scheduledTime > 0
                    ? new Date(scheduledTime).toString()
                    : "now";
            log.info("Scheduling job [" + order + "] at "
                    + scheduledTimeStr + ". Retry times: "
                    + order.getFailureCount());
        }
        jmsDelegate.queue(deleteStudyQueueName, order,
                Message.DEFAULT_PRIORITY, scheduledTime);
    }

    public void onMessage(Message msg) {
        ObjectMessage message = (ObjectMessage) msg;
        Object o = null;
        try {
            o = message.getObject();
        } catch (JMSException e1) {
            log.error("Processing JMS message failed! message:" + message, e1);
        }
        if (o instanceof BaseJmsOrder) {
            if (log.isDebugEnabled())
                log.debug("Processing JMS message: " + o);

            BaseJmsOrder order = (BaseJmsOrder) o;
            try {
                if (order instanceof ActionOrder) {
                    ActionOrder actionOrder = (ActionOrder) order;
                    Method m = this.getClass().getDeclaredMethod(
                            actionOrder.getActionMethod(),
                            new Class[] { Object.class });
                    m.invoke(this, new Object[] { actionOrder.getData() });
                } else if (order instanceof DeleteStudyOrder) {
                    try {
                        if (deleteSeriesBySeries) {
                            deleteSeries((DeleteStudyOrder) order);
                        } else {
                            deleteStudy((DeleteStudyOrder) order);
                        }
                    } catch (ConcurrentStudyStorageException e) {
                        log.info(e.getMessage());
                    } 
                }
                if (log.isDebugEnabled())
                    log.debug("Finished processing " + order);
            } catch (Exception e) {
                final int failureCount = order.getFailureCount() + 1;
                order.setFailureCount(failureCount);
                final long delay = retryIntervalsForJmsOrder
                        .getIntervall(failureCount);
                if (delay == -1L) {
                    order.setThrowable(e);
                    log.error("Give up to process " + order);
                    try {
                        jmsDelegate.fail(deleteStudyQueueName, order);
                    } catch (Exception e2) {
                        log.error("Failed to notify JMSDelgate of failed job! Give up to process" + order, e2);
                    }
                } else {
                    Throwable thisThrowable = e;
                    if (e instanceof InvocationTargetException)
                        thisThrowable = ((InvocationTargetException) e)
                                .getTargetException();

                    if (order.getFailureCount() == 1
                            || (order.getThrowable() != null && !thisThrowable
                                    .getClass().equals(
                                            order.getThrowable().getClass()))) {
                        // If this happens first time, log as error
                        log.error(
                                "Failed to process JMS job. Will schedule retry ... Dumping - "
                                + order.toString(), e);
                        // Record this exception
                        order.setThrowable(thisThrowable);
                    } else {
                        // otherwise, if it's the same exception as before
                        log.warn("Failed to process "
                                + order
                                + ". Details should have been provided. Will schedule retry.");
                    }
                    try {
                        schedule(order, System.currentTimeMillis() + delay);
                    } catch (Exception e2) {
                        log.error("Failed to schedule retry! Give up to process " + order, e2);
                    }
                }
            }
        }
    }

    private void deleteStudy(DeleteStudyOrder order) throws Exception {
        FileSystemMgt2 fsMgt = fileSystemMgt();
        Collection<Dataset> ians = null;
        // prepare IAN if study may be deleted from DB by fsMgt.deleteStudy()
        if (createIANonStudyDelete && deleteStudyFromDB) {
            ians = fsMgt.createIANforStudy(order.getStudyPk());
        }
        String[] filePaths = fsMgt.deleteStudy(order,
                deleteStudyFromDB, deletePatientWithoutObjects);
        FileSystemDTO fsDto = fsMgt.getFileSystem(order.getFsPk()); 
        for (int i = 0; i < filePaths.length; i++) {
            FileUtils.delete(FileUtils.toFile(filePaths[i]), true, fsDto.getDirectoryPath());
        }
        try {
            fsMgt.removeStudyOnFSRecord(order);
        } catch (Exception x) {
            log.warn("Remove StudyOnFS record failed for "+order, x);
        }
        if (createIANonStudyDelete) {
            try {
                try {
                    ians = fsMgt.createIANforStudy(order.getStudyPk());
                    for (Dataset ian : ians) {
                        updateRetrieveAET(ian, fsMgt.getFileSystem(order.getFsPk()).getRetrieveAET());
                    }
                } catch (NoSuchStudyException e) {
                    // OK, in case of study was deleted from DB
                    if (ians == null) {
                        throw e;
                    }
                    for (Dataset ian : ians) {
                        updateAvailability(ian, "UNAVAILABLE");
                    }
                }
                for (Dataset ian : ians) {
                    sendJMXNotification(new StudyDeleted(ian));
                }
            } catch (Exception e) {
                log.error("Failed to create IAN on Study Delete:", e);
            }
        }
    }

    private void updateRetrieveAET(Dataset ian, String retrieveAET) {
        DcmElement refSerSeq = ian.get(Tags.RefSeriesSeq);
        for (int i = 0, n = refSerSeq.countItems(); i < n; i++) {
            Dataset refSer = refSerSeq.getItem(i);
            DcmElement refSopSeq = refSer.get(Tags.RefSOPSeq);
            for (int j = 0, m = refSopSeq.countItems(); j < m; j++) {
                Dataset refSOP = refSopSeq.getItem(j);
                if (!refSOP.containsValue(Tags.RetrieveAET))
                    refSOP.putCS(Tags.RetrieveAET, retrieveAET);
            }
        }
    }

    private static void updateAvailability(Dataset ian, String availability) {
        DcmElement refSerSeq = ian.get(Tags.RefSeriesSeq);
        for (int i = 0, n = refSerSeq.countItems(); i < n; i++) {
            Dataset refSer = refSerSeq.getItem(i);
            DcmElement refSopSeq = refSer.get(Tags.RefSOPSeq);
            for (int j = 0, m = refSopSeq.countItems(); j < m; j++) {
                Dataset refSOP = refSopSeq.getItem(j);
                refSOP.putCS(Tags.InstanceAvailability, availability);
            }
        }
    }

    private void deleteSeries(DeleteStudyOrder order) throws Exception {
        FileSystemMgt2 fsMgt = fileSystemMgt();
        Collection<Long> seriesPks = fsMgt.getSeriesPks(order);
        FileSystemDTO fsDto = fsMgt.getFileSystem(order.getFsPk()); 
        for (Long seriesPk : seriesPks) {
            Dataset ian = null;
            // prepare IAN if series may be deleted from DB by fsMgt.deleteSeries()
            if (createIANonStudyDelete && deleteStudyFromDB) {
                try {
                    ian = fsMgt.createIANforSeries(seriesPk);
                } catch (NoSuchSeriesException e) {
                    // Series may be already deleted from DB by previous
                    // attempt processing this DeleteStudyOrder
                    continue;
                }
            }
            String[] filePaths = fsMgt.deleteSeries(order, seriesPk, 
                    deleteStudyFromDB, deletePatientWithoutObjects);
            if (filePaths.length == 0) {
                // Files of this Series already deleted by previous attempt 
                // processing this DeleteStudyOrder
                continue;
            }
            for (int i = 0; i < filePaths.length; i++) {
                FileUtils.delete(FileUtils.toFile(filePaths[i]), true, fsDto.getDirectoryPath());
            }
            if (createIANonStudyDelete) {
                try {
                    try {
                        ian = fsMgt.createIANforSeries(seriesPk);
                        updateRetrieveAET(ian, fsMgt.getFileSystem(order.getFsPk()).getRetrieveAET());
                    } catch (NoSuchSeriesException e) {
                        // OK, in case of series was deleted from DB
                        if (ian == null) {
                            throw e;
                        }
                        updateAvailability(ian, "UNAVAILABLE");
                    }
                    sendJMXNotification(new StudyDeleted(ian));
                } catch (Exception e) {
                    log.error("Failed to create IAN on Study Delete:", e);
                }
            }
        }
        try {
            fsMgt.removeStudyOnFSRecord(order);
        } catch (Exception x) {
            log.warn("Remove StudyOnFS record failed for "+order, x);
        }
    }
    
    static FileSystemMgt2 fileSystemMgt() throws Exception {
        FileSystemMgt2Home home = (FileSystemMgt2Home) EJBHomeFactory
                .getFactory().lookup(FileSystemMgt2Home.class,
                        FileSystemMgt2Home.JNDI_NAME);
        return home.create();
    }

    void sendJMXNotification(Object o) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(o.getClass().getName(), this,
                eventID);
        notif.setUserData(o);
        super.sendNotification(notif);
    }
}
