/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.hsm;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2Home;
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.dcm4chex.archive.mbean.SchedulerDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 14954 $ $Date: 2011-02-24 20:50:06 +0800 (周四, 24 2月 2011) $
 * @since Nov 22, 2005
 */
public class SyncFileStatusService extends ServiceMBeanSupport {

    private static final String DELETE = "DELETE";

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
    private ObjectName tarRetrieverName;
    
    private Timestamp oldestCreatedTimeOfCheckFileStatus;
    private long nextUpdate;
    
    private boolean verifyTar;
    private int notInTarStatus;
    private int invalidTarStatus;
    private byte[] buf = new byte[8192];
    
    private final NotificationListener timerListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            if (fileSystem == null) {
                log.debug("SyncFileStatus disabled (fileSystem=NONE)!");
            }
            if (oldestCreatedTimeOfCheckFileStatus == null || System.currentTimeMillis() > nextUpdate) {
                updateOldestCreatedTimeOfCheckFileStatus();
            }
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

    public final ObjectName getTarRetrieverName() {
        return tarRetrieverName;
    }

    public final void setTarRetrieverName(ObjectName tarRetrieverName) {
        this.tarRetrieverName = tarRetrieverName;
    }
    
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

    public boolean isVerifyTar() {
        return verifyTar;
    }

    public void setVerifyTar(boolean verifyTar) {
        this.verifyTar = verifyTar;
    }

    public String getInvalidTarStatus() {
        return invalidTarStatus == Integer.MIN_VALUE ? DELETE : FileStatus.toString(invalidTarStatus);
    }
    public void setInvalidTarStatus(String status) {
        this.invalidTarStatus = DELETE.equals(status) ? Integer.MIN_VALUE : FileStatus.toInt(status);
    }
    public String getNotInTarStatus() {
        return notInTarStatus == Integer.MIN_VALUE ? DELETE : FileStatus.toString(notInTarStatus);
    }
    public void setNotInTarStatus(String status) {
        this.notInTarStatus = DELETE.equals(status) ? Integer.MIN_VALUE : FileStatus.toInt(status);
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

    public String getOldestCreatedTimeOfCheckFileStatus() {
        return oldestCreatedTimeOfCheckFileStatus == null ? "UNKNOWN" : 
            new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(oldestCreatedTimeOfCheckFileStatus);
    }

    public void updateOldestCreatedTimeOfCheckFileStatus() {
        try {
            oldestCreatedTimeOfCheckFileStatus = newFileSystemMgt().minCreatedTimeOnFsWithFileStatus(this.fileSystem, this.checkFileStatus);
            if (oldestCreatedTimeOfCheckFileStatus == null) {
                nextUpdate = System.currentTimeMillis() + this.minFileAge;
                log.info("OldestCreatedTimeOfCheckFileStatus is null! -> There is no file with fileStatus="+checkFileStatus+" on filesystem="+fileSystem);
                log.info("Next update of OldestCreatedTimeOfCheckFileStatus in "+getMinimumFileAge()+" (when new files are old enough to be considered)");
            } else {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                nextUpdate = cal.getTimeInMillis();
                log.info("OldestCreatedTimeOfCheckFileStatus updated to "+oldestCreatedTimeOfCheckFileStatus+" ! Next update after midnight.");
            }
        } catch (Exception x) {
            log.warn("Update OldestCreatedTimeOfCheckFileStatus failed!", x);
        }
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
            if (this.nextUpdate == 0L && oldestCreatedTimeOfCheckFileStatus == null)
                this.updateOldestCreatedTimeOfCheckFileStatus();
            if (oldestCreatedTimeOfCheckFileStatus == null) {
               log.info("OldestCreatedTimeOfCheckFileStatus is null! SyncFileStatus skipped!");
               return 0;
            }
            FileSystemMgt2 fsmgt = newFileSystemMgt();
            FileDTO[] c = fsmgt.findFilesByStatusAndFileSystem(fileSystem, checkFileStatus, this.oldestCreatedTimeOfCheckFileStatus,
                    new Timestamp(System.currentTimeMillis() - minFileAge), limitNumberOfFilesPerTask);
            if (log.isDebugEnabled()) log.debug("found "+c.length+" files to check status.");
            if (c.length == 0) {
                return 0;
            }
            int count = 0;
            HashMap<String, Integer> checkedTarsStatus = new HashMap<String, Integer>();
            HashMap<String, Set<String>> checkedTarsMD5 = verifyTar ? new HashMap<String, Set<String>>() : null;
            for (int i = 0; i < c.length; i++) {
                if (check(fsmgt, c[i], checkedTarsStatus, checkedTarsMD5))
                    ++count;
            }
            return count;
        } finally {
            isRunning = false;
        }
    }

    private boolean check(FileSystemMgt2 fsmgt, FileDTO fileDTO,
            HashMap<String, Integer> checkedTarsStatus, HashMap<String, Set<String>> checkedTarsMD5) throws IOException, VerifyTarException {
        String fsId = fileDTO.getDirectoryPath();
        String filePath = fileDTO.getFilePath();
        String tarPathKey = null;
        Integer status;
        if (fsId.startsWith("tar:")) {
            String tarfilePath = filePath.substring(0, filePath.indexOf('!'));
            tarPathKey = fsId.substring(4) + '/' + tarfilePath;
            if (!checkedTarsStatus.containsKey(tarPathKey)) {
                status = queryHSM(fsId, tarfilePath, fileDTO.getUserInfo());
                checkedTarsStatus.put(tarPathKey, status);
            }
            status = verifyTar(fileDTO, tarPathKey, checkedTarsMD5);
            if (status == null) {
                status = (Integer) checkedTarsStatus.get(tarPathKey);
            }
        } else {
            status = queryHSM(fsId, filePath, fileDTO.getUserInfo());
        }
        return (status == null || status == Integer.MIN_VALUE) ? 
                false : updateFileStatus(fsmgt, fileDTO, status);
    }

    private Integer verifyTar(FileDTO dto, String tarPathKey, HashMap<String, Set<String>> checkedTarsMD5) {
        if (verifyTar) {
            log.info("Verify tar file "+tarPathKey);
            String filePath = dto.getFilePath();
            String filepathInTar = filePath.substring(filePath.indexOf('!')+1);
            String tarfilePath = filePath.substring(0, filePath.indexOf('!'));
            Set<String> entries = null;
            if (checkedTarsMD5.containsKey(tarPathKey)) {
                entries = checkedTarsMD5.get(tarPathKey);
                if (log.isDebugEnabled()) log.debug("entries of checked tar file "+tarPathKey+" :"+entries);
            } else {
                try {
                    File tarFile = fetchTarFile(dto.getDirectoryPath(), tarfilePath);
                    entries = VerifyTar.verify(tarFile, buf);
                } catch (Exception x) {
                    log.error("Verification of tar file "+tarPathKey+" failed! Reason:"+x.getMessage());
                    if (invalidTarStatus == Integer.MIN_VALUE) {
                        try {
                            log.error("Delete file entities of invalid tar file :"+tarfilePath);
                            newFileSystemMgt().deleteFilesOfInvalidTarFile(dto.getDirectoryPath(), tarfilePath);
                        } catch (Exception e) {
                            log.error("Failed to delete files of invalid tar file! tarFile:"+tarfilePath);
                        }
                    }
                }
                checkedTarsMD5.put(tarPathKey, entries);
            }
            if (entries == null) {
                log.error("TAR file "+tarPathKey+" not valid -> " + (invalidTarStatus == Integer.MIN_VALUE ?
                        "File is deleted!" : "set status to "+FileStatus.toString(invalidTarStatus)));
                return invalidTarStatus;
            } else if (!entries.contains(filepathInTar)) {
                log.error("Tar File "+tarPathKey+" does NOT contain File "+filepathInTar);
                if (notInTarStatus == Integer.MIN_VALUE) {
                    log.error("Delete file entity that is missing in tar file:"+filePath);
                    try {
                        newFileSystemMgt().deleteFileOnTarFs(dto.getDirectoryPath(), dto.getPk());
                    } catch (Exception e) {
                        log.error("Failed to delete file entity! filePath:"+filePath);
                    }
                } else {
                    log.error("Set file status to "+FileStatus.toString(notInTarStatus));
                }
                return notInTarStatus;
            }
        }
        return null;
    }
    
    private File fetchTarFile(String fsID, String tarPath) throws Exception {
        try {
            return (File) server.invoke(tarRetrieverName, "fetchTarFile",
                    new Object[] { fsID, tarPath }, new String[] {
                            String.class.getName(), String.class.getName() });
        } catch (InstanceNotFoundException e) {
            throw new ConfigurationException(e.getMessage(), e);
        } catch (MBeanException e) {
            throw e.getTargetException();
        } catch (ReflectionException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
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

    private Integer queryHSM(String fsID, String filePath, String userInfo) throws IOException {
        try {
            return (Integer) server.invoke(hsmModuleServicename, 
                    "queryStatus", new Object[]{fsID, filePath, userInfo}, 
                    new String[]{String.class.getName(),String.class.getName(),String.class.getName()});
        } catch (Exception x) {
            log.error("queryHSM failed! fsID:"+fsID+" filePath:"+
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
 
    public int syncArchivedStatusOfInstances(String fsID, String limitStr) throws Exception {
        if (fsID == null || fsID.trim().length() < 1)
            return 0;
        int limit = (limitStr == null || limitStr.trim().length() < 1) ? 1000 : Integer.parseInt(limitStr);
        return newFileSystemMgt().syncArchivedFlag(fsID, limit < 1 ? 1000 : limit);
    }
}
