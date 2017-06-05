/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.hsm;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;

import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2Home;
import org.dcm4chex.archive.mbean.SchedulerDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 13945 $ $Date: 2010-08-27 17:35:45 +0800 (周五, 27 8月 2010) $
 * @since Nov 22, 2005
 */
public class SyncFileStatusService extends ServiceMBeanSupport {

    private final SchedulerDelegate scheduler = new SchedulerDelegate(this);

    private static final String NONE = "NONE";

    private String timerIDCheckSyncFileStatus;
    
    private boolean isRunning;

    private long minFileAge = 0L;

    private long taskInterval = 0L;

    private int disabledStartHour;

    private int disabledEndHour;

    private int limitNumberOfFilesPerTask;

    private int checkFileStatus;

    private String fileSystem = null;

    private Integer listenerID;

    private ObjectName hsmModuleServicename = null;
    
    private final NotificationListener timerListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (isDisabled(hour)) {
                if (log.isDebugEnabled())
                    log.debug("SyncFileStatus ignored in time between "
                            + disabledStartHour + " and " + disabledEndHour
                            + " !");
            } else { 
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            check();
                        } catch (Exception e) {
                            log.error("check file status failed!", e);
                        }
                    }
                }).start();
            }
        }

    };

    public ObjectName getSchedulerServiceName() {
        return scheduler.getSchedulerServiceName();
    }

    public void setSchedulerServiceName(ObjectName schedulerServiceName) {
        scheduler.setSchedulerServiceName(schedulerServiceName);
    }

    public final String getFileSystem() {
        return fileSystem == null ? NONE : fileSystem;
    }

    public final void setFileSystem(String fileSystem) {
        this.fileSystem = (NONE.equalsIgnoreCase(fileSystem)) ? null
                : fileSystem;
    }

    public final String getCheckFileStatus() {
        return FileStatus.toString(checkFileStatus);
    }

    public final void setCheckFileStatus(String status) {
        this.checkFileStatus = FileStatus.toInt(status);
    }

    public final String getTaskInterval() {
        String s = RetryIntervalls.formatIntervalZeroAsNever(taskInterval);
        return (disabledEndHour == -1) ? s : s + "!" + disabledStartHour + "-"
                + disabledEndHour;
    }

    public void setTaskInterval(String interval) throws Exception {
        long oldInterval = taskInterval;
        int pos = interval.indexOf('!');
        if (pos == -1) {
            taskInterval = RetryIntervalls.parseIntervalOrNever(interval);
            disabledEndHour = -1;
        } else {
            taskInterval = RetryIntervalls.parseIntervalOrNever(interval
                    .substring(0, pos));
            int pos1 = interval.indexOf('-', pos);
            disabledStartHour = Integer.parseInt(interval.substring(pos + 1,
                    pos1));
            disabledEndHour = Integer.parseInt(interval.substring(pos1 + 1));
        }
        if (getState() == STARTED && oldInterval != taskInterval) {
            scheduler.stopScheduler(timerIDCheckSyncFileStatus, listenerID,
                    timerListener);
            listenerID = scheduler.startScheduler(timerIDCheckSyncFileStatus,
                    taskInterval, timerListener);
        }
    }

    public final String getMinimumFileAge() {
        return RetryIntervalls.formatInterval(minFileAge);
    }

    public final void setMinimumFileAge(String intervall) {
        this.minFileAge = RetryIntervalls.parseInterval(intervall);
    }

    public int getLimitNumberOfFilesPerTask() {
        return limitNumberOfFilesPerTask;
    }

    public void setLimitNumberOfFilesPerTask(int limit) {
        this.limitNumberOfFilesPerTask = limit;
    }

    public final String getHSMModulServicename() {
        return hsmModuleServicename == null ? NONE : hsmModuleServicename.toString();
    }

    public final void setHSMModulServicename(String name) throws MalformedObjectNameException {
        this.hsmModuleServicename = NONE.equals(name) ? null : ObjectName.getInstance(name);
    }
    
    private boolean isDisabled(int hour) {
        if (disabledEndHour == -1)
            return false;
        boolean sameday = disabledStartHour <= disabledEndHour;
        boolean inside = hour >= disabledStartHour && hour < disabledEndHour;
        return sameday ? inside : !inside;
    }

    public boolean isRunning() {
        return isRunning;
    }
    
    protected void startService() throws Exception {
        listenerID = scheduler.startScheduler(timerIDCheckSyncFileStatus,
                taskInterval, timerListener);
    }

    protected void stopService() throws Exception {
        scheduler.stopScheduler(timerIDCheckSyncFileStatus, listenerID,
                timerListener);
        super.stopService();
    }

    public int check() throws Exception  {
        if (fileSystem == null) {
            return 0;
        }
        if (hsmModuleServicename == null) {
            log.warn("HSM Module Servicename not configured! SyncFileStatusService disabled!");
            return 0;
        }
        synchronized(this) {
            if (isRunning) {
                log.info("SyncFileStatus is already running!");
                return -1;
            }
            isRunning = true;
        }
        try {
            FileSystemMgt2 fsmgt = newFileSystemMgt();
            FileDTO[] c = fsmgt.findFilesByStatusAndFileSystem(fileSystem,
                    checkFileStatus, new Timestamp(System.currentTimeMillis()
                            - minFileAge), limitNumberOfFilesPerTask);
            if (log.isDebugEnabled()) log.debug("found "+c.length+" files to check status.");
            if (c.length == 0) {
                return 0;
            }
            int count = 0;
            HashMap<String, Integer> checkedTars = new HashMap<String, Integer>();
            for (int i = 0; i < c.length; i++) {
                if (check(fsmgt, c[i], checkedTars))
                    ++count;
            }
            return count;
        } finally {
            isRunning = false;
        }
    }

    private boolean check(FileSystemMgt2 fsmgt, FileDTO fileDTO,
            HashMap<String, Integer> checkedTars) throws IOException {
        String dirpath = fileDTO.getDirectoryPath();
        String filePath = fileDTO.getFilePath();
        String tarPath = null;
        if (dirpath.startsWith("tar:")) {
            filePath = filePath.substring(0, filePath.indexOf('!'));
            tarPath = dirpath.substring(4) + '/' + filePath;
            Integer status = (Integer) checkedTars.get(tarPath);
            if (status != null) {
                return updateFileStatus(fsmgt, fileDTO, status.intValue());
            }
            if (checkedTars.containsKey(tarPath))
                return false;
        }
        Integer status = queryHSM(dirpath, filePath, fileDTO.getUserInfo());
        if (tarPath != null) {
            checkedTars.put(tarPath, status);
        }
        return status == null ? false : updateFileStatus(fsmgt, fileDTO, status);
    }

    private boolean updateFileStatus(FileSystemMgt2 fsmgt, FileDTO fileDTO,
            int status) {
        if (fileDTO.getFileStatus() != status) {
            log.info("Change status of " + fileDTO + " to " + status);
            try {
                fsmgt.setFileStatus(fileDTO.getPk(), status);
                return true;
            } catch (Exception e) {
                log.error("Failed to update status of file " + fileDTO, e);
            }
        }
        return false;
    }

    private Integer queryHSM(String dirpath, String filePath, String userInfo) throws IOException {
        try {
            return (Integer) server.invoke(hsmModuleServicename, 
                    "queryStatus", new Object[]{dirpath, filePath, userInfo}, 
                    new String[]{String.class.getName(),String.class.getName(),String.class.getName()});
        } catch (Exception x) {
            log.error("queryHSM failed! dirpath:"+dirpath+" filePath:"+
                    filePath+" userInfo:"+userInfo, x);
            IOException iox = new IOException("Query status of HSMFile failed!");
            iox.initCause(x);
            throw iox;
        }
    }

    protected FileSystemMgt2 newFileSystemMgt() throws Exception {
        return ((FileSystemMgt2Home) EJBHomeFactory.getFactory().lookup(
                FileSystemMgt2Home.class, FileSystemMgt2Home.JNDI_NAME)).create();
    }

    public String getTimerIDCheckSyncFileStatus() {
        return timerIDCheckSyncFileStatus;
    }

    public void setTimerIDCheckSyncFileStatus(String timerIDCheckSyncFileStatus) {
        this.timerIDCheckSyncFileStatus = timerIDCheckSyncFileStatus;
    }
    
}
