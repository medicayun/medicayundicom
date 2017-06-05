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

package org.dcm4chex.archive.mbean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Attribute;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DataSource;
import org.dcm4che.util.Executer;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.ActionOrder;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.BaseJmsOrder;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.common.FileSystemStatus;
import org.dcm4chex.archive.config.DeleterThresholds;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocal;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.QueryFilesCmd;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.notif.StudyDeleted;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileDataSource;
import org.dcm4chex.archive.util.FileSystemUtils;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2903 $ $Date: 2006-10-30 16:41:02 +0800 (周一, 30 10月 2006) $
 * @since 12.09.2004
 * 
 */
public class FileSystemMgtService extends ServiceMBeanSupport implements
        MessageListener {

    private static final String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private static final String NONE = "NONE";

    private static final String FROM_PARAM = "%1";

    private static final String TO_PARAM = "%2";

    private final SchedulerDelegate scheduler = new SchedulerDelegate(this);

    private static final long MIN_FREE_DISK_SPACE = 20 * FileUtils.MEGA;

    private long minFreeDiskSpace = MIN_FREE_DISK_SPACE;

    private long checkFreeDiskSpaceInterval = 60000L;

    private float checkFreeDiskSpaceThreshold = 5f;

    private boolean checkStorageFileSystemStatus = true;

    private String retrieveAET = "DCM4CHEE";

    private String defStorageDir = "archive";

    private DeleterThresholds deleterThresholds = new DeleterThresholds(
            "7:1h;19:24h", true);

    private long expectedDataVolumnePerDay = 100000L;

    private boolean flushExternalRetrievable = false;

    private boolean flushOnMedia = false;

    private boolean flushOnROFsAvailable = false;

    private int validFileStatus = 0;

    private boolean deleteUncommited = false;

    private long studyCacheTimeout = 0L;

    private long purgeFilesInterval = 0L;

    private int limitNumberOfFilesPerTask = 1000;

    private long freeDiskSpaceInterval = 0L;

    private Integer purgeFilesListenerID;

    private Integer freeDiskSpaceListenerID;

    private boolean freeDiskSpaceOnDemand = true;

    private boolean isPurging = false;

    private int bufferSize = 8192;

    private String mountFailedCheckFile = "NO_MOUNT";

    private boolean makeStorageDirectory = true;

    private FileSystemDTO storageFileSystem;

    private long checkStorageFileSystem = 0L;

    private String purgeStudyQueueName = null;

    private long adjustExpectedDataVolumnePerDay = 0L;

    protected RetryIntervalls retryIntervalsForJmsOrder = new RetryIntervalls();

    private boolean excludePrivate;

    private boolean deleteFilesWhenUnavailable;

    private String[] onSwitchStorageFSCmd;

    private String timerIDCheckFilesToPurge;

    private String timerIDCheckFreeDiskSpace;

    private final NotificationListener purgeFilesListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            purgePrivateFiles();
        }
    };

    private final NotificationListener freeDiskSpaceListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            freeDiskSpace();
        }
    };

    private JMSDelegate jmsDelegate = new JMSDelegate(this);

    public final ObjectName getJmsServiceName() {
        return jmsDelegate.getJmsServiceName();
    }

    public final void setJmsServiceName(ObjectName jmsServiceName) {
        jmsDelegate.setJmsServiceName(jmsServiceName);
    }

    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }

    public ObjectName getSchedulerServiceName() {
        return scheduler.getSchedulerServiceName();
    }

    public void setSchedulerServiceName(ObjectName schedulerServiceName) {
        scheduler.setSchedulerServiceName(schedulerServiceName);
    }

    public final boolean isMakeStorageDirectory() {
        return makeStorageDirectory;
    }

    public final void setMakeStorageDirectory(boolean makeStorageDirectory) {
        this.makeStorageDirectory = makeStorageDirectory;
    }

    public final String getMountFailedCheckFile() {
        return mountFailedCheckFile;
    }

    public final void setMountFailedCheckFile(String mountFailedCheckFile) {
        this.mountFailedCheckFile = mountFailedCheckFile;
    }

    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public final String getRetrieveAET() {
        return retrieveAET;
    }

    /**
     * Set Retrieve AE title associated with this DICOM Node. There must be at
     * least one configured file system suitable for storage (ONLINE, RW)
     * associated with this AE title.
     * 
     * @param aet
     *            The AE Title to set.
     */
    public final void setRetrieveAET(String aet) throws FinderException,
            IOException {
        this.retrieveAET = aet;
        this.storageFileSystem = null;
    }

    public final String getDefStorageDir() {
        return defStorageDir;
    }

    public final void setDefStorageDir(String defStorageDir) {
        this.defStorageDir = defStorageDir;
    }

    public final String getMinFreeDiskSpace() {
        return FileUtils.formatSize(minFreeDiskSpace);
    }

    public final void setMinFreeDiskSpace(String str) {
        this.minFreeDiskSpace = FileUtils.parseSize(str, MIN_FREE_DISK_SPACE);
    }

    public final String getCheckFreeDiskSpaceInterval() {
        return RetryIntervalls.formatInterval(checkFreeDiskSpaceInterval);
    }

    public final void setCheckFreeDiskSpaceInterval(String s) {
        this.checkFreeDiskSpaceInterval = RetryIntervalls.parseInterval(s);
    }

    public final float getCheckFreeDiskSpaceThreshold() {
        return checkFreeDiskSpaceThreshold;
    }

    public final void setCheckFreeDiskSpaceThreshold(float threshold) {
        if (threshold < 1.0f)
            throw new IllegalArgumentException(
                    "CheckFreeDiskSpaceThreshold must NOT be smaller than 1!");
        this.checkFreeDiskSpaceThreshold = threshold;
    }

    public final boolean isCheckStorageFileSystemStatus() {
        return checkStorageFileSystemStatus;
    }

    public final void setCheckStorageFileSystemStatus(
            boolean checkStorageFileSystemStatus) {
        this.checkStorageFileSystemStatus = checkStorageFileSystemStatus;
    }

    public final String getDeleterThresholds() {
        return deleterThresholds.toString();
    }

    public final void setDeleterThresholds(String s) {
        this.deleterThresholds = new DeleterThresholds(s, true);
    }

    public final String getExpectedDataVolumnePerDay() {
        return FileUtils.formatSize(expectedDataVolumnePerDay);
    }

    public final void setExpectedDataVolumnePerDay(String s) {
        this.expectedDataVolumnePerDay = FileUtils.parseSize(s, FileUtils.MEGA);
    }

    public final boolean isAdjustExpectedDataVolumnePerDay() {
        return adjustExpectedDataVolumnePerDay != 0L;
    }

    public final void setAdjustExpectedDataVolumnePerDay(boolean b) {
        this.adjustExpectedDataVolumnePerDay = b ? nextMidnight() : 0L;
    }

    private long nextMidnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    /**
     * Returns true if the freeDiskSpace policy flushExternalRetrievable is
     * enabled.
     * <p>
     * If this policy is active studies must be external retrievable for
     * deletion.
     * 
     * @return Returns true if flushExternalRetrievable policy is active.
     */
    public boolean isFlushStudiesExternalRetrievable() {
        return flushExternalRetrievable;
    }

    /**
     * Set the freeDiskSpace policy flushExternalRetrievable.
     * <p>
     * Set this policy active if studies must be external retrievable for
     * deletion.
     * 
     * @param b
     *            The flushExternalRetrievable to set.
     */
    public void setFlushStudiesExternalRetrievable(boolean b) {
        this.flushExternalRetrievable = b;
    }

    /**
     * Returns true if the freeDiskSpace policy deleteUncommited is enabled.
     * <p>
     * If this policy is active studies are deleted immedatly without any check.
     * 
     * @return Returns true if deleteUncommited is active.
     */
    public boolean isDeleteStudiesStorageNotCommited() {
        return deleteUncommited;
    }

    /**
     * Set the freeDiskSpace policy deleteUncommited.
     * <p>
     * If this policy is active studies are deleted immedatly without any check.
     * 
     * @param b
     *            The deleteUncommited to set.
     */
    public void setDeleteStudiesStorageNotCommited(boolean b) {
        deleteUncommited = b;
    }

    /**
     * Returns true if the freeDiskSpace policy flushOnMedia is enabled.
     * <p>
     * If this policy is active studies must be stored on media (offline
     * storage) for deletion.
     * 
     * @return Returns true if flushOnMedia policy is active.
     */
    public boolean isFlushStudiesOnMedia() {
        return flushOnMedia;
    }

    /**
     * Set the freeDiskSpace policy flushOnMedia.
     * <p>
     * Set this policy active if studies must be on media (offline storage) for
     * deletion.
     * 
     * @param b
     *            The flushOnMedia to set.
     */
    public void setFlushStudiesOnMedia(boolean b) {
        this.flushOnMedia = b;
    }

    /**
     * @return Returns the flushOnHSM.
     */
    public boolean isFlushOnROFsAvailable() {
        return flushOnROFsAvailable;
    }

    /**
     * Set the freeDiskSpace policy flushOnHSM.
     * <p>
     * Set this policy active if studies must be on media (offline storage) for
     * deletion.
     * 
     * @param flushOnROAvailable
     *            The flushOnHSM to set.
     */
    public void setFlushOnROFsAvailable(boolean flushOnROAvailable) {
        this.flushOnROFsAvailable = flushOnROAvailable;
    }

    /**
     * @return Returns the validFileStatus.
     */
    public String getValidFileStatus() {
        return FileStatus.toString(validFileStatus);
    }

    /**
     * @param validFileStatus
     *            The validFileStatus to set.
     */
    public void setValidFileStatus(String validFileStatus) {
        this.validFileStatus = FileStatus.toInt(validFileStatus);
    }

    /**
     * Return string representation
     * 
     * @return Returns the StudyCacheTimeout.
     */
    public String getStudyCacheTimeout() {
        return RetryIntervalls.formatIntervalZeroAsNever(studyCacheTimeout);
    }

    /**
     * Set number of days a study is not accessed for freeDiskSpace.
     * 
     * @param StudyCacheTimeoutDays
     *            The StudyCacheTimeoutDays to set in days.
     */
    public void setStudyCacheTimeout(String interval) {
        this.studyCacheTimeout = RetryIntervalls.parseIntervalOrNever(interval);
    }

    public final String getFreeDiskSpaceInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(freeDiskSpaceInterval);
    }

    public void setFreeDiskSpaceInterval(String interval) throws Exception {
        this.freeDiskSpaceInterval = RetryIntervalls
                .parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            scheduler.stopScheduler(timerIDCheckFreeDiskSpace,
                    freeDiskSpaceListenerID, freeDiskSpaceListener);
            freeDiskSpaceListenerID = scheduler.startScheduler(
                    timerIDCheckFreeDiskSpace, freeDiskSpaceInterval,
                    freeDiskSpaceListener);
        }
    }

    /**
     * @return Returns the freeDiskSpaceOnDemand.
     */
    public boolean isFreeDiskSpaceOnDemand() {
        return freeDiskSpaceOnDemand;
    }

    /**
     * @param freeDiskSpaceOnDemand
     *            The freeDiskSpaceOnDemand to set.
     */
    public void setFreeDiskSpaceOnDemand(boolean freeDiskSpaceOnDemand) {
        this.freeDiskSpaceOnDemand = freeDiskSpaceOnDemand;
    }

    public final String getPurgeFilesInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(purgeFilesInterval);
    }

    public void setPurgeFilesInterval(String interval) throws Exception {
        this.purgeFilesInterval = RetryIntervalls
                .parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            scheduler.stopScheduler(timerIDCheckFilesToPurge,
                    purgeFilesListenerID, purgeFilesListener);
            purgeFilesListenerID = scheduler.startScheduler(
                    timerIDCheckFilesToPurge, purgeFilesInterval,
                    purgeFilesListener);
        }
    }

    public final int getLimitNumberOfFilesPerTask() {
        return limitNumberOfFilesPerTask;
    }

    public void setLimitNumberOfFilesPerTask(int limit) {
        limitNumberOfFilesPerTask = limit;
    }

    public final String getRetryIntervalsForJmsOrder() {
        return retryIntervalsForJmsOrder.toString();
    }

    public final void setRetryIntervalsForJmsOrder(String s) {
        this.retryIntervalsForJmsOrder = new RetryIntervalls(s);
    }

    public String getPurgeStudyQueueName() {
        return purgeStudyQueueName;
    }

    public void setPurgeStudyQueueName(String purgeStudyQueueName) {
        this.purgeStudyQueueName = purgeStudyQueueName;
    }

    public boolean isWADOExcludePrivateAttributes() {
        return excludePrivate;
    }

    public void setWADOExcludePrivateAttributes(boolean excludePrivate) {
        this.excludePrivate = excludePrivate;
    }

    public final String getOnSwitchStorageFilesystemInvoke() {
        if (onSwitchStorageFSCmd == null) {
            return NONE;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < onSwitchStorageFSCmd.length; i++) {
            sb.append(onSwitchStorageFSCmd[i]);
        }
        return sb.toString();
    }

    private String makeOnSwitchStorageFSCmd(String from, String to) {
        if (onSwitchStorageFSCmd == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < onSwitchStorageFSCmd.length; i++) {
            sb.append(onSwitchStorageFSCmd[i].equals(FROM_PARAM) ? from
                    : onSwitchStorageFSCmd[i].equals(TO_PARAM) ? to
                            : onSwitchStorageFSCmd[i]);
        }
        return sb.toString();
    }

    public final void setOnSwitchStorageFilesystemInvoke(String command) {
        String s = command.trim();
        if (NONE.equalsIgnoreCase(s)) {
            onSwitchStorageFSCmd = null;
            return;
        }
        try {
            String[] a = StringUtils.split(s, '%');
            String[] b = new String[a.length + a.length - 1];
            b[0] = a[0];
            for (int i = 1; i < a.length; i++) {
                b[2 * i - 1] = ("%" + a[i].charAt(0)).intern();
                b[2 * i] = a[i].substring(1);
            }
            this.onSwitchStorageFSCmd = b;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(command);
        }
    }

    /**
     * @return Returns the deleteFilesWhenUnavailable.
     */
    public boolean isDeleteFilesWhenUnavailable() {
        return deleteFilesWhenUnavailable;
    }

    /**
     * @param deleteFilesWhenUnavailable
     *            The deleteFilesWhenUnavailable to set.
     */
    public void setDeleteFilesWhenUnavailable(boolean deleteFilesWhenUnavailable) {
        this.deleteFilesWhenUnavailable = deleteFilesWhenUnavailable;
    }


    public String getTimerIDCheckFilesToPurge() {
        return timerIDCheckFilesToPurge;
    }

    public void setTimerIDCheckFilesToPurge(String timerIDCheckFilesToPurge) {
        this.timerIDCheckFilesToPurge = timerIDCheckFilesToPurge;
    }

    public String getTimerIDCheckFreeDiskSpace() {
        return timerIDCheckFreeDiskSpace;
    }

    public void setTimerIDCheckFreeDiskSpace(String timerIDCheckFreeDiskSpace) {
        this.timerIDCheckFreeDiskSpace = timerIDCheckFreeDiskSpace;
    }
   
    protected void startService() throws Exception {
        freeDiskSpaceListenerID = scheduler.startScheduler(
                timerIDCheckFreeDiskSpace, freeDiskSpaceInterval,
                freeDiskSpaceListener);
        purgeFilesListenerID = scheduler.startScheduler(
                timerIDCheckFilesToPurge, purgeFilesInterval,
                purgeFilesListener);
        jmsDelegate.startListening(purgeStudyQueueName, this, 1);

    }

    private void initStorageFileSystem() throws Exception {
        FileSystemMgt fsmgt = newFileSystemMgt();
        FileSystemDTO[] c = fsmgt.findFileSystems(retrieveAET,
                Availability.ONLINE, FileSystemStatus.DEF_RW);
        if (c.length > 0) {
            storageFileSystem = c[0];
        } else {
            c = fsmgt.findFileSystems(retrieveAET, Availability.ONLINE,
                    FileSystemStatus.RW);
            if (c.length > 0) {
                storageFileSystem = c[0];
                storageFileSystem.setStatus(FileSystemStatus.DEF_RW);
                fsmgt.updateFileSystem(storageFileSystem);
            } else {
                storageFileSystem = addFileSystem(defStorageDir, retrieveAET,
                        Availability.ONLINE, FileSystemStatus.DEF_RW, null);
                log.warn("No writeable Storage Directory configured for retrieve AET "
                        + retrieveAET
                        + "- initalize default "
                        + storageFileSystem);
            }
        }
        checkStorageFileSystem();
    }

    public FileSystemDTO selectStorageFileSystem() throws Exception {
        if (storageFileSystem == null) {
            initStorageFileSystem();
        }
        boolean checkDiskSpace = checkStorageFileSystem == 0L
                || checkStorageFileSystem < System.currentTimeMillis();
        if (checkStorageFileSystemStatus || checkDiskSpace) {
            FileSystemMgt fsmgt = newFileSystemMgt();
            storageFileSystem = fsmgt.getFileSystem(storageFileSystem
                    .getDirectoryPath());
            if (!checkStorageFileSystemStatus(storageFileSystem)
                    || checkDiskSpace
                    && !checkStorageFileSystem(storageFileSystem))
                if (!switchStorageFileSystem(fsmgt, storageFileSystem))
                    return null;
        }
        return storageFileSystem;
    }

    private synchronized boolean switchStorageFileSystem(FileSystemMgt fsmgt,
            FileSystemDTO fsDTO) throws RemoteException, FinderException,
            IOException {
        if (storageFileSystem != fsDTO)
            return true; // already switched by another thread
        String dirPath, dirPath0 = fsDTO.getDirectoryPath();
        do {
            dirPath = fsDTO.getNext();
            if (dirPath == null || dirPath.equals(dirPath0)) {
                log.error("High Water Mark reached on storage directory "
                        + FileUtils.toFile(dirPath0)
                        + " - no alternative storage directory available");
                return false;
            }
            fsDTO = fsmgt.getFileSystem(dirPath);
        } while (!checkStorageFileSystemStatus(fsDTO)
                || !checkStorageFileSystem(fsDTO));
        storageFileSystem.setStatus(FileSystemStatus.RW);
        fsDTO.setStatus(FileSystemStatus.DEF_RW);
        fsmgt.updateFileSystem2(storageFileSystem, fsDTO);
        storageFileSystem = fsDTO;
        if (onSwitchStorageFSCmd != null) {
            final String cmd = makeOnSwitchStorageFSCmd(dirPath0.replace('/',
                    File.separatorChar), dirPath.replace('/',
                    File.separatorChar));
            new Thread(new Runnable() {

                public void run() {
                    execute(cmd);
                }
            }).start();
        }
        return true;
    }

    private void execute(final String cmd) {
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            Executer ex = new Executer(cmd, stdout, null);
            int exit = ex.waitFor();
            if (exit != 0) {
                log.info("Non-zero exit code(" + exit + ") of " + cmd);
            }
        } catch (Exception e) {
            log.error("Failed to execute " + cmd, e);
        }
    }

    private boolean checkStorageFileSystemStatus(FileSystemDTO fsDTO) {
        int availability = fsDTO.getAvailability();
        int status = fsDTO.getStatus();
        if (availability != Availability.ONLINE
                || status != FileSystemStatus.RW
                && status != FileSystemStatus.DEF_RW) {
            log.info("" + fsDTO + " no longer available for storage"
                    + " - try to switch to next configured storage directory");
            return false;
        }
        return true;
    }

    private boolean checkStorageFileSystem(FileSystemDTO fsDTO)
            throws IOException {
        File dir = FileUtils.toFile(fsDTO.getDirectoryPath());
        if (!dir.exists()) {
            if (!makeStorageDirectory) {
                log.warn("No such directory " + dir
                        + " - try to switch to next configured storage directory");
                return false;
            }
            log.info("M-WRITE " + dir);
            if (!dir.mkdirs()) {
                log.warn("Failed to create directory " + dir
                        + " - try to switch to next configured storage directory");
                return false;
            }
        }
        File nomount = new File(dir, mountFailedCheckFile);
        if (nomount.exists()) {
            log.warn("Mount on " + dir
                    + " seems broken - try to switch to next configured storage directory");
            return false;
        }
        final long freeSpace = FileSystemUtils.freeSpace(dir.getPath());
        log.info("Free disk space on " + dir + ": "
                + FileUtils.formatSize(freeSpace));
        if (freeSpace < minFreeDiskSpace) {
            log.info("High Water Mark reached on current storage directory "
                    + dir
                    + " - try to switch to next configured storage directory");
            return false;
        }
        checkStorageFileSystem = (freeSpace > minFreeDiskSpace
                * checkFreeDiskSpaceThreshold) ? (System.currentTimeMillis() + checkFreeDiskSpaceInterval)
                : 0L;
        return true;
    }

    protected void stopService() throws Exception {
        scheduler.stopScheduler(timerIDCheckFreeDiskSpace,
                freeDiskSpaceListenerID, freeDiskSpaceListener);
        scheduler.stopScheduler(timerIDCheckFilesToPurge, purgeFilesListenerID,
                purgeFilesListener);
        jmsDelegate.stopListening(purgeStudyQueueName);
        super.stopService();
    }

    private FileSystemMgt newFileSystemMgt() {
        try {
            FileSystemMgtHome home = (FileSystemMgtHome) EJBHomeFactory
                    .getFactory().lookup(FileSystemMgtHome.class,
                            FileSystemMgtHome.JNDI_NAME);
            return home.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access File System Mgt EJB:",
                    e);
        }
    }

    public String showAvailableDiskSpace() throws IOException, FinderException {
        return FileUtils
                .formatSize(getAvailableDiskSpace(listLocalOnlineRWFileSystems()));
    }

    /**
     * Search for unreferenced private files and delete them.
     * 
     * @return
     */
    public int purgePrivateFiles() {
        log.info("Check for unreferenced private files to delete");
        synchronized (this) {
            if (isPurging) {
                log.info("A purge task is already in progress! Ignore this purge order!");
                return 0;
            }
            isPurging = true;
        }
        FileSystemMgt fsMgt = newFileSystemMgt();
        int deleted, total = 0;
        FileSystemDTO[] list;
        try {
            list = listLocalOnlineRWFileSystems(fsMgt);
        } catch (Exception e) {
            log.error("Failed to query DB for file system configuration:", e);
            return 0;
        }
        int limit = getLimitNumberOfFilesPerTask();
        for (int i = 0; i < list.length; ++i) {
            deleted = purgePrivateFiles(list[i].getDirectoryPath(), fsMgt,
                    limit);
            if (deleted < 0)
                break;
            total += deleted;
            if (total >= limit)
                break;
        }
        isPurging = false;
        return total;
    }

    public int purgePrivateFiles(String purgeDirPath) {
        int total;
        if (purgeDirPath == null) {
            total = purgePrivateFiles();
        } else {
            synchronized (this) {
                if (isPurging) {
                    log.info("A purge task is already in progress! Ignore this purge order!");
                    return 0;
                }
                isPurging = true;
            }
            log.debug("Check for unreferenced (private) files to delete in filesystem:"
                            + purgeDirPath);
            FileSystemMgt fsMgt = newFileSystemMgt();
            int limit = getLimitNumberOfFilesPerTask();
            total = purgePrivateFiles(purgeDirPath, fsMgt, limit);
            isPurging = false;
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }
        return total;
    }

    private int purgePrivateFiles(String purgeDirPath, FileSystemMgt fsMgt,
            int limit) {
        FileDTO[] toDelete;
        try {
            toDelete = fsMgt.getDereferencedPrivateFiles(purgeDirPath, limit);
            if (log.isDebugEnabled())
                log.debug("purgePrivateFiles: found " + toDelete.length
                        + " files to delete on dirPath:" + purgeDirPath);
        } catch (Exception e) {
            log.warn("Failed to query dereferenced files:", e);
            return -1;
        }
        for (int j = 0; j < toDelete.length; j++) {
            FileDTO fileDTO = toDelete[j];
            File file = FileUtils.toFile(fileDTO.getDirectoryPath(), fileDTO
                    .getFilePath());
            try {
                fsMgt.deletePrivateFile(fileDTO.getPk());
            } catch (Exception e) {
                log.warn("Failed to remove File Record[pk=" + fileDTO.getPk()
                        + "] from DB:", e);
                log.info("-> Keep dereferenced file: " + file);
                continue;
            }
            delete(file);
        }
        return toDelete.length;
    }

    private boolean delete(File file) {
        log.info("M-DELETE file: " + file);
        if (!file.exists()) {
            log.warn("File: " + file + " was already deleted");
            return true;
        }
        if (!file.delete()) {
            log.warn("Failed to delete file: " + file);
            return false;
        }
        // purge empty series and study directory
        File seriesDir = file.getParentFile();
        if (seriesDir.delete()) {
            seriesDir.getParentFile().delete();
        }
        return true;
    }

    public Object locateInstance(String iuid) throws SQLException {
        List list = new QueryFilesCmd(iuid).getFileDTOs();
        if (list.isEmpty())
            return null;
        for (int i = 0, n = list.size(); i < n; ++i) {
            FileDTO dto = (FileDTO) list.get(i);
            if (retrieveAET.equals(dto.getRetrieveAET()))
                return FileUtils.toFile(dto.getDirectoryPath(), dto
                        .getFilePath());
        }
        FileDTO dto = (FileDTO) list.get(0);
        AEData aeData = new AECmd(dto.getRetrieveAET()).getAEData();
        return aeData.getHostName();
    }

    public DataSource getDatasourceOfInstance(String iuid) throws SQLException {
        Dataset dsQ = DcmObjectFactory.getInstance().newDataset();
        dsQ.putUI(Tags.SOPInstanceUID, iuid);
        dsQ.putCS(Tags.QueryRetrieveLevel, "IMAGE");
        RetrieveCmd retrieveCmd = RetrieveCmd.create(dsQ);
        FileInfo infoList[][] = retrieveCmd.getFileInfos();
        if (infoList.length == 0)
            return null;
        FileInfo[] fileInfos = infoList[0];
        for (int i = 0; i < fileInfos.length; ++i) {
            final FileInfo info = fileInfos[i];
            if (retrieveAET.equals(info.fileRetrieveAET)) {
                File f = FileUtils.toFile(info.basedir, info.fileID);
                Dataset mergeAttrs = DatasetUtils.fromByteArray(
                        info.patAttrs,
                        DatasetUtils .fromByteArray(
                                info.studyAttrs,
                                DatasetUtils .fromByteArray(
                                        info.seriesAttrs,
                                        DatasetUtils .fromByteArray(
                                                info.instAttrs))));
                FileDataSource ds = new FileDataSource(f, mergeAttrs,
                        new byte[bufferSize]);
                ds.setWriteFile(true);// write FileMetaInfo!
                ds.setExcludePrivate(excludePrivate);
                return ds;
            }
        }
        return null;
    }

    /**
     * Delete studies that fullfill freeDiskSpacePolicy to free disk space.
     * <p>
     * Checks available disk space if free disk space is necessary.
     * <p>
     * Remove old files according configured Deleter Thresholds.
     * <p>
     * The real deletion is done in the purge process! This method removes only
     * the reference to the file system.
     * <p>
     * If PurgeFilesAfterFreeDiskSpace is enabled, the purge process will be
     * called immediately.
     * 
     * @return The number of released studies.
     */
    public void freeDiskSpace() {
        log.info("Check available Disk Space");
        try {
            FileSystemMgt fsMgt = newFileSystemMgt();
            FileSystemDTO[] fs = listLocalOnlineRWFileSystems(fsMgt);
            Calendar now = Calendar.getInstance();
            if (adjustExpectedDataVolumnePerDay != 0
                    && now.getTimeInMillis() > adjustExpectedDataVolumnePerDay) {
                adjustExpectedDataVolumnePerDay(fsMgt, fs);
                adjustExpectedDataVolumnePerDay = nextMidnight();
            }
            long available = getAvailableDiskSpace(fs) - minFreeDiskSpace
                    * fs.length;
            long freeSize = deleterThresholds.getDeleterThreshold(now)
                    .getFreeSize(expectedDataVolumnePerDay);
            long maxSizeToDel = freeSize - available;
            if (maxSizeToDel > 0) {
                freeDiskSpace(retrieveAET, deleteUncommited, flushOnMedia,
                        flushExternalRetrievable, flushOnROFsAvailable,
                        validFileStatus, maxSizeToDel,
                        limitNumberOfFilesPerTask);
            } else if (studyCacheTimeout > 0L) {
                long accessedBefore = System.currentTimeMillis()
                        - studyCacheTimeout;
                releaseStudies(retrieveAET, deleteUncommited, flushOnMedia,
                        flushExternalRetrievable, flushOnROFsAvailable,
                        validFileStatus, accessedBefore);
            } else {
                return;
            }
        } catch (Exception e) {
            log.error("Free Disk Space failed:", e);
            return;
        }
    }

    private void freeDiskSpace(String retrieveAET, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, boolean checkOnRO,
            int validFileStatus, long maxSizeToDel, int deleteStudiesLimit)
            throws Exception {
        Map ians = new HashMap();
        log.info("Free Disk Space: try to release "
                + (maxSizeToDel / 1000000.f) + "MB of DiskSpace");

        releaseStudies(retrieveAET, ians, checkUncommited, checkOnMedia,
                checkExternal, checkOnRO, validFileStatus, maxSizeToDel,
                deleteStudiesLimit);
    }

    private void releaseStudies(String retrieveAET, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, boolean checkOnRO,
            int validFileStatus, long accessedBefore) throws Exception {
        Timestamp tsBefore = new Timestamp(accessedBefore);
        log.info("Releasing studies not accessed since " + tsBefore);
        Map ians = new HashMap();
        releaseStudies(retrieveAET, ians, checkUncommited, checkOnMedia,
                checkExternal, checkOnRO, validFileStatus, Long.MAX_VALUE,
                new Timestamp(accessedBefore));
    }

    private long releaseStudies(String retrieveAET, Map ians,
            boolean checkUncommited, boolean checkOnMedia,
            boolean checkExternal, boolean checkOnRO, int validFileStatus,
            long maxSizeToDel, Timestamp tsBefore) throws Exception {

        Collection c = newFileSystemMgt().getStudiesOnFsByLastAccess(
                retrieveAET, tsBefore);
        if (log.isDebugEnabled()) {
            log.debug("Release studies on filesystem(s) accessed before "
                    + tsBefore + " :" + c.size());
            log.debug(" checkUncommited: " + checkUncommited);
            log.debug(" checkOnMedia: " + checkOnMedia);
            log.debug(" checkExternal: " + checkExternal);
            log.debug(" checkCopyAvailable: " + checkOnRO);
            if (maxSizeToDel != Long.MAX_VALUE)
                log.debug(" maxSizeToDel: " + maxSizeToDel);
        }
        long sizeToDelete = 0L;
        for (Iterator iter = c.iterator(); iter.hasNext()
                && sizeToDelete < maxSizeToDel;) {
            StudyOnFileSystemLocal studyOnFs = (StudyOnFileSystemLocal) iter
                    .next();

            sizeToDelete += releaseStudy(studyOnFs, checkUncommited,
                    checkOnMedia, checkExternal, checkOnRO, validFileStatus);
        }

        log.info("Released " + (sizeToDelete / 1000000.f) + "MB of DiskSpace");
        return sizeToDelete;
    }

    private long releaseStudies(String retrieveAET, Map ians,
            boolean checkUncommited, boolean checkOnMedia,
            boolean checkExternal, boolean checkOnRO, int validFileStatus,
            long maxSizeToDel, int deleteStudiesLimit) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Release oldest studies on " + retrieveAET);
            log.debug(" checkUncommited: " + checkUncommited);
            log.debug(" checkOnMedia: " + checkOnMedia);
            log.debug(" checkExternal: " + checkExternal);
            log.debug(" checkCopyAvailable: " + checkOnRO);
            log.debug(" maxSizeToDel: " + maxSizeToDel);
            log.debug(" deleteStudyBatchSize: " + deleteStudiesLimit);
        }

        // Studies that can't be deleted because they dont' meet criteria
        int notDeleted = 0;
        // Total file size that has been deleted
        long sizeDeleted = 0L;
        for (; sizeDeleted < maxSizeToDel;) {
            // For those studies that can't be deleted, unfortunately they get
            // selected again for subsequent
            // batch, therefore, in order to retrieve more studies to delete, we
            // have to increase the batch size
            int thisBatchSize = deleteStudiesLimit + notDeleted;

            Collection c = newFileSystemMgt().getOldestStudiesOnFs(retrieveAET,
                    thisBatchSize);
            if (c.size() == notDeleted)
                break;

            if (log.isDebugEnabled())
                log.debug("Retrieved the oldest studies on filesystem(s): "
                        + c.size());

            notDeleted = 0;
            for (Iterator iter = c.iterator(); iter.hasNext()
                    && sizeDeleted < maxSizeToDel;) {
                StudyOnFileSystemLocal studyOnFs = (StudyOnFileSystemLocal) iter
                        .next();
                long size = releaseStudy(studyOnFs, checkUncommited,
                        checkOnMedia, checkExternal, checkOnRO, validFileStatus);
                if (size != 0)
                    sizeDeleted += size;
                else
                    notDeleted++;
            }
        }
        log.info("Released " + (sizeDeleted / 1000000.f) + "MB of DiskSpace");
        return sizeDeleted;
    }

    public String adjustExpectedDataVolumnePerDay() throws Exception {
        FileSystemMgt fsMgt = newFileSystemMgt();
        return adjustExpectedDataVolumnePerDay(fsMgt,
                listLocalOnlineRWFileSystems(fsMgt));
    }

    private String adjustExpectedDataVolumnePerDay(FileSystemMgt fsMgt,
            FileSystemDTO[] fs) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.roll(Calendar.DAY_OF_MONTH, false);
        long after = cal.getTimeInMillis();
        long sum = 0L;
        for (int i = 0; i < fs.length; i++) {
            sum = fsMgt.sizeOfFilesCreatedAfter(new Long(fs[i].getPk()), after);
        }
        String size = FileUtils.formatSize(sum);
        if (sum > expectedDataVolumnePerDay) {
            server.setAttribute(super.serviceName, new Attribute(
                    "ExpectedDataVolumnePerDay", size));
        }
        return size;
    }

    private long releaseStudy(StudyOnFileSystemLocal studyOnFs,
            boolean deleteUncommited, boolean flushOnMedia,
            boolean flushExternal, boolean flushOnROFs, int validFileStatus)
            throws Exception {
        long size = 0L;
        StudyLocal study = studyOnFs.getStudy();
        boolean release = flushExternal && study.isStudyExternalRetrievable()
                || flushOnMedia && study.isStudyAvailableOnMedia()
                || flushOnROFs && study.isStudyAvailableOnROFs(validFileStatus);

        deleteUncommited = (deleteUncommited && study
                .getNumberOfCommitedInstances() == 0);
        if (release || deleteUncommited) {

            // Send PurgeStudy JMS message
            FileSystemMgt fsmgt = null;
            try {
                fsmgt = newFileSystemMgt();

                // Get study size for this study stored in this file system
                size = fsmgt.getStudySize(study.getPk(), studyOnFs
                        .getFileSystem().getPk());
                this.schedule(new PurgeStudyOrder(study.getPk(), studyOnFs
                        .getFileSystem().getPk(), deleteUncommited), System
                        .currentTimeMillis());

                // Remove the StudyOnFs record from database immediately to
                // prevent duplicate query
                // when previous jobs are not finished yet
                studyOnFs.remove();
            } finally {
                fsmgt.remove();
            }
        } else {
            log.warn("Study [" + study.getStudyIuid() + "] can not be deleted");
        }
        return size;
    }

    private void releaseStudy(PurgeStudyOrder order) throws Exception {
        FileSystemMgt fsmgt = newFileSystemMgt();

        Collection files = new ArrayList();
        Dataset ian = fsmgt.releaseStudy(order.getStudyPk(), order.getFsPk(),
                order.isDeleteUncommited(), files);

        for (Iterator iter = files.iterator(); iter.hasNext();) {
            File file = FileUtils.toFile((String) iter.next());
            delete(file);
        }

        sendJMXNotification(new StudyDeleted(ian));
    }

    void sendJMXNotification(Object o) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(o.getClass().getName(), this,
                eventID);
        notif.setUserData(o);
        super.sendNotification(notif);
    }

    private long getAvailableDiskSpace(FileSystemDTO[] fs) throws IOException,
            FinderException {
        long result = 0L;
        for (int i = 0; i < fs.length; i++) {
            final File dir = FileUtils.toFile(fs[i].getDirectoryPath());
            if (dir.isDirectory())
                result += FileSystemUtils.freeSpace(dir.getPath());
        }
        return result;
    }

    public FileSystemDTO[] listLocalOnlineRWFileSystems()
            throws FinderException, RemoteException {
        return listLocalOnlineRWFileSystems(newFileSystemMgt());
    }

    private FileSystemDTO[] listLocalOnlineRWFileSystems(FileSystemMgt fsmgt)
            throws FinderException, RemoteException {
        return fsmgt.findFileSystems2(retrieveAET, Availability.ONLINE,
                FileSystemStatus.DEF_RW, FileSystemStatus.RW);
    }

    public long showStudySize(Long pk, Long fsPk) throws RemoteException,
            FinderException {
        FileSystemMgt fsMgt = newFileSystemMgt();
        return fsMgt.getStudySize(pk, fsPk);
    }

    private void sortFileSystems(FileSystemDTO[] dtos) {
        int capacity = dtos.length * 4 / 3 + 1;
        HashMap tmp = new HashMap(capacity);
        HashSet root = new HashSet(capacity);
        HashSet notRoot = new HashSet(capacity);
        for (int i = 0; i < dtos.length; ++i) {
            FileSystemDTO dto = dtos[i];
            String dirPath = dto.getDirectoryPath();
            tmp.put(dirPath, dto);
            if (!notRoot.remove(dirPath)) {
                root.add(dirPath);
            }
            String next = dto.getNext();
            if (next != null && !root.remove(next)) {
                notRoot.add(next);
            }
        }
        int i = 0;
        while (!tmp.isEmpty()) {
            FileSystemDTO dto;
            if (!root.isEmpty()) {
                Iterator iter = root.iterator();
                dto = (FileSystemDTO) tmp.remove(iter.next());
                iter.remove();
            } else {
                Iterator iter = tmp.values().iterator();
                dto = (FileSystemDTO) iter.next();
                iter.remove();
            }
            while (dto != null) {
                dtos[i++] = dto;
                dto = (FileSystemDTO) tmp.remove(dto.getNext());
            }            
        }
    }

    
    private static String toString(FileSystemDTO[] dtos) {
        StringBuffer sb = new StringBuffer();
        String nl = System.getProperty("line.separator", "\n");
        for (int i = 0; i < dtos.length; i++) {
            sb.append(dtos[i]).append(nl);
        }
        return sb.toString();
    }

    public String showAllFileSystems() throws RemoteException, FinderException {
        FileSystemDTO[] dtos = listAllFileSystems();
        sortFileSystems(dtos);
        return toString(dtos);
    }
    
    public FileSystemDTO[] listAllFileSystems() throws RemoteException,
            FinderException {
        return newFileSystemMgt().getAllFileSystems();
    }

    public FileSystemDTO addFileSystem(String dirPath, String retrieveAET,
            String availability, String status, String userInfo)
            throws RemoteException, CreateException {
        return addFileSystem(dirPath, retrieveAET, Availability
                .toInt(availability), FileSystemStatus.toInt(status), userInfo);
    }

    private FileSystemDTO addFileSystem(String dirPath, String retrieveAET,
            int availability, int status, String userInfo)
            throws RemoteException, CreateException {
        FileSystemDTO dto = new FileSystemDTO();
        dto.setDirectoryPath(dirPath);
        dto.setRetrieveAET(retrieveAET);
        dto.setAvailability(availability);
        dto.setStatus(status);
        dto.setUserInfo(userInfo);
        return newFileSystemMgt().addFileSystem(dto);
    }

    public void updateFileSystem(String dirPath, String retrieveAET,
            String availability, String status, String userInfo)
            throws RemoteException, FinderException {
        FileSystemDTO dto = new FileSystemDTO();
        dto.setDirectoryPath(dirPath);
        dto.setRetrieveAET(retrieveAET);
        dto.setAvailability(Availability.toInt(availability));
        dto.setStatus(FileSystemStatus.toInt(status));
        dto.setUserInfo(userInfo);
        newFileSystemMgt().updateFileSystem(dto);
        checkStorageFileSystem();
    }

    public void checkStorageFileSystem() {
        checkStorageFileSystem = 0;
    }

    public boolean updateFileSystemAvailability(String dirPath,
            String availability) throws RemoteException, FinderException {
        FileSystemMgt mgt = newFileSystemMgt();
        int iAvail = Availability.toInt(availability);
        log.info("Update availability of " + dirPath + " to " + availability
                + "(" + iAvail + ")");
        if (mgt.updateFileSystemAvailability(dirPath, iAvail)) {
            checkStorageFileSystem();
            int offset = 0;
            int limit = limitNumberOfFilesPerTask;
            int size;
            log.info("Update availability of all instances of filesystem "
                    + dirPath);
            while ((size = mgt.updateInstanceAvailability(dirPath, offset,
                    limit)) > 0) {
                log.debug("  " + size + " updated for FS availability change!");
                offset += limit;
            }
            log.info("Instances of " + dirPath + " updated");
            if (deleteFilesWhenUnavailable
                    && iAvail == Availability.UNAVAILABLE) {
                log.info("delete files on unavailable filesystem:" + dirPath);
                deleteFilesOnFS(dirPath, mgt);
            }
            return true;
        } else {
            return false;
        }

    }

    public void deleteFilesOnFS(String dirPath) throws RemoteException,
            FinderException {
        FileSystemMgt mgt = newFileSystemMgt();
        int avail = mgt.getFileSystem(dirPath).getAvailability();
        if (avail != Availability.UNAVAILABLE)
            throw new IllegalStateException(
                    "Filesystem must be UNAVAILABLE to perform this method!");
        deleteFilesOnFS(dirPath, mgt);
    }

    /**
     * @param dirPath
     * @param mgt
     * @param iAvail
     * @throws FinderException
     * @throws RemoteException
     */
    private void deleteFilesOnFS(String dirPath, FileSystemMgt mgt)
            throws FinderException, RemoteException {
        int offset = 0;
        Collection files = mgt.getFilesOnFS(dirPath, offset,
                limitNumberOfFilesPerTask);
        FileDTO dto;
        File f;
        while (!files.isEmpty()) {
            for (Iterator iter = files.iterator(); iter.hasNext();) {
                dto = (FileDTO) iter.next();
                f = FileUtils.toFile(dto.getDirectoryPath(), dto.getFilePath());
                delete(f);
            }
            offset += 1000;
            files = mgt
                    .getFilesOnFS(dirPath, offset, limitNumberOfFilesPerTask);
        }
    }

    /**
     * Check if the file of given filesystems are available.
     * 
     * @param dirPath
     * @return 1 if all available, -1 if FS is empty or 0 if some available and
     *         some not.
     * 
     * @throws FinderException
     * @throws RemoteException
     */
    public int checkFilesOnFS(String dirPath) throws FinderException,
            RemoteException {
        FileSystemMgt mgt = newFileSystemMgt();
        int offset = 0;
        Collection files = mgt.getFilesOnFS(dirPath, offset,
                limitNumberOfFilesPerTask);
        FileDTO dto;
        File f;
        int numDBFiles = 0;
        int numFilesNotAvail = 0;
        while (!files.isEmpty()) {
            numDBFiles += files.size();
            for (Iterator iter = files.iterator(); iter.hasNext();) {
                dto = (FileDTO) iter.next();
                f = FileUtils.toFile(dto.getDirectoryPath(), dto.getFilePath());
                if (!f.exists()) {
                    if (log.isDebugEnabled())
                        log.debug("Missing file:" + f);
                    numFilesNotAvail++;
                }
            }
            offset += 1000;
            files = mgt
                    .getFilesOnFS(dirPath, offset, limitNumberOfFilesPerTask);
        }
        if (log.isDebugEnabled())
            log.debug("Files DB entries for filesystem " + dirPath + ":"
                    + numDBFiles + " NOT available:" + numFilesNotAvail);
        return numFilesNotAvail == 0 ? 1 : numFilesNotAvail == numDBFiles ? -1
                : 0;
    }

    public void linkFileSystems(String prev, String next)
            throws RemoteException, FinderException {
        newFileSystemMgt().linkFileSystems(prev, next);
        checkStorageFileSystem();
    }

    public String addOnlineFileSystem(String dirPath, String userInfo)
            throws RemoteException, FinderException, CreateException {
        return addAndLinkFileSystem(dirPath, Availability.ONLINE,
                FileSystemStatus.RW, userInfo);
    }

    public String showOnlineFileSystems() throws RemoteException,
            FinderException {
        return showFileSystemsWithAvailability(Availability.ONLINE);
    }

    private String showFileSystemsWithAvailability(int availability)
            throws RemoteException, FinderException {
        FileSystemDTO[] dtos = newFileSystemMgt()
                .findRWFileSystemByRetieveAETAndAvailability(retrieveAET,
                        availability);
        sortFileSystems(dtos);
        return toString(dtos);
    }
    
    public String addNearlineFileSystem(String dirPath, String userInfo)
            throws RemoteException, FinderException, CreateException {
        return addAndLinkFileSystem(dirPath, Availability.NEARLINE,
                FileSystemStatus.RW, userInfo);
    }

    public String showNearlineFileSystems() throws RemoteException,
            FinderException {
        return showFileSystemsWithAvailability(Availability.NEARLINE);
   }

    public String removeFileSystem(String dirPath) throws RemoteException,
            FinderException, RemoveException {
        return newFileSystemMgt().removeFileSystem(dirPath).toString();
    }

    private String addAndLinkFileSystem(String dirPath, int availability,
            int status, String userInfo) throws FinderException,
            CreateException, RemoteException {
        FileSystemDTO dto = new FileSystemDTO();
        dto.setDirectoryPath(dirPath);
        dto.setRetrieveAET(retrieveAET);
        dto.setAvailability(availability);
        dto.setStatus(status);
        dto.setUserInfo(userInfo);
        FileSystemDTO newFS = newFileSystemMgt().addAndLinkFileSystem(dto);
        checkStorageFileSystem();
        return newFS.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
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
                } else if (order instanceof PurgeStudyOrder) {
                    releaseStudy((PurgeStudyOrder) order);
                }
                if (log.isDebugEnabled())
                    log.debug("Finished processing " + order.toIdString());
            } catch (Exception e) {
                final int failureCount = order.getFailureCount() + 1;
                order.setFailureCount(failureCount);
                final long delay = retryIntervalsForJmsOrder
                        .getIntervall(failureCount);
                if (delay == -1L) {
                    log.error("Give up to process " + order, e);
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
                                + order.toIdString()
                                + ". Details should have been provided. Will schedule retry.");
                    }
                    schedule(order, System.currentTimeMillis() + delay);
                }
            }
        }
    }

    protected void schedule(BaseJmsOrder order, long scheduledTime) {
        try {
            if (scheduledTime > 0 && log.isInfoEnabled()) {
                String scheduledTimeStr = new SimpleDateFormat(DATE_TIME_FORMAT)
                        .format(new Date(scheduledTime));
                log.info("Scheduling job [" + order.toIdString() + "] at "
                        + scheduledTimeStr + ". Retry times: "
                        + order.getFailureCount());
            }
            jmsDelegate.queue(purgeStudyQueueName, order,
                    Message.DEFAULT_PRIORITY, scheduledTime);
        } catch (Exception e) {
            log.error("Failed to schedule " + order, e);
        }
    }
}