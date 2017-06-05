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
import java.util.StringTokenizer;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
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
import org.dcm4chex.archive.ejb.interfaces.AEDTO;
import org.dcm4chex.archive.ejb.interfaces.AEManager;
import org.dcm4chex.archive.ejb.interfaces.AEManagerHome;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocal;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.QueryCmd;
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
 * @version $Revision: 6943 $ $Date: 2008-09-05 01:32:49 +0800 (周五, 05 9月 2008) $
 * @since 12.09.2004
 * 
 */
public class FileSystemMgtService extends ServiceMBeanSupport implements
        MessageListener {

    private static final String _STORAGE = "_STORAGE";

    private static final String ONLINE_STORAGE = "ONLINE_STORAGE";

    private static final String NEARLINE_STORAGE = "NEARLINE_STORAGE";

    private static final String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private static final String NONE = "NONE";

    private static final String FROM_PARAM = "%1";

    private static final String TO_PARAM = "%2";

    private final SchedulerDelegate scheduler = new SchedulerDelegate(this);

    private static final long MIN_FREE_DISK_SPACE = 20 * FileUtils.MEGA;
    
    private ObjectName aeServiceName;

    private long minFreeDiskSpace = MIN_FREE_DISK_SPACE;

    private boolean checkStorageFileSystemStatus = true;

    private String retrieveAET = "DCM4CHEE";

    private String defStorageDir = "archive";

    private DeleterThresholds deleterThresholds = new DeleterThresholds(
            "7:1h;19:24h", true);

    private long expectedDataVolumePerDay = 100000L;

    private boolean flushExternalRetrievable = false;

    private boolean flushOnMedia = false;

    private boolean flushOnNearline = false;
    
    private boolean flushOnROFsAvailable = false;

    private int validFileStatus = 0;

    private boolean deleteUncommited = false;
    private boolean deleteEmptyPatient = false;
    
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

    private long checkFreeDiskSpaceMinInterval;

    private long checkFreeDiskSpaceMaxInterval;

    private long checkStorageFileSystem = 0L;

    private String purgeStudyQueueName = null;

    private long adjustExpectedDataVolumePerDay = 0L;

    protected RetryIntervalls retryIntervalsForJmsOrder = new RetryIntervalls();

    private boolean excludePrivate;

    private boolean deleteFilesWhenUnavailable;

    private String[] onSwitchStorageFSCmd;

    private String timerIDCheckFilesToPurge;

    private String timerIDCheckFreeDiskSpace;
    
    private ObjectName[] otherServiceNames = {};

    private String[] otherServiceAETAttrs = {};

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

    public final ObjectName getAEServiceName() {
        return aeServiceName;
    }

    public final void setAEServiceName(ObjectName aeServiceName) {
        this.aeServiceName = aeServiceName;
    }
    
    public final ObjectName getJmsServiceName() {
        return jmsDelegate.getJmsServiceName();
    }

    public final void setJmsServiceName(ObjectName jmsServiceName) {
        jmsDelegate.setJmsServiceName(jmsServiceName);
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
    
    public final String getDFCommand() {
        return FileSystemUtils.getDFCommand();
    }

    public final void setDFCommand(String dfCommand) {
        FileSystemUtils.setDFCommand(dfCommand);
    }

    public final String getDFCommandOption() {
        return FileSystemUtils.getDFCommandOption();
    }

    public final void setDFCommandOption(String dfCommandOption) {
        FileSystemUtils.setDFCommandOption(dfCommandOption);
    }    

    public final String getCheckFreeDiskSpaceMinimalInterval() {
        return RetryIntervalls.formatInterval(checkFreeDiskSpaceMinInterval);
    }

    public final void setCheckFreeDiskSpaceMinimalInterval(String s) {
        this.checkFreeDiskSpaceMinInterval = RetryIntervalls.parseInterval(s);
    }

    public final String getCheckFreeDiskSpaceMaximalInterval() {
        return RetryIntervalls.formatInterval(checkFreeDiskSpaceMaxInterval);
    }

    public final void setCheckFreeDiskSpaceMaximalInterval(String s) {
        this.checkFreeDiskSpaceMaxInterval = RetryIntervalls.parseInterval(s);
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

    public final String getExpectedDataVolumePerDay() {
        return FileUtils.formatSize(expectedDataVolumePerDay);
    }

    public final void setExpectedDataVolumePerDay(String s) {
        this.expectedDataVolumePerDay = FileUtils.parseSize(s, FileUtils.MEGA);
    }

    public final boolean isAdjustExpectedDataVolumePerDay() {
        return adjustExpectedDataVolumePerDay != 0L;
    }

    public final void setAdjustExpectedDataVolumePerDay(boolean b) {
        this.adjustExpectedDataVolumePerDay = b ? nextMidnight() : 0L;
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
     * Returns true if empty patients should be deleted.
     * <p>
     * This option is only effective if a study is deleted on the basis 
     * of 'deleteUncommited' policy!
     * 
     * @return
     */
    public boolean isDeleteEmptyPatient() {
        return deleteEmptyPatient;
    }

    /**
     * Set 'delete empty patient' option for 'deleteUncommited' policy.
     * <p>
     * This option is only effective if a study is deleted on the basis 
     * of 'deleteUncommited' policy!
     * <p>
     * Only empty patients (No studies, MWL, MPPS, GPWL and GPPPS) will be deleted!
     * 
     * @param deleteEmptyPatient
     */
    public void setDeleteEmptyPatient(boolean deleteEmptyPatient) {
        this.deleteEmptyPatient = deleteEmptyPatient;
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
     * @return Returns the flushOnNearline.
     */
    public boolean isFlushOnNearline() {
        return flushOnNearline;
    }

    /**
     * Set the freeDiskSpace policy flushOnNearline.
     * <p>
     * Set this policy active if studies must be on nearline media for
     * deletion.
     * 
     * @param flushOnNearline
     *            The value to set.
     */
    public void setFlushOnNearline(boolean flushOnNearline) {
        this.flushOnNearline = flushOnNearline;
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
   
    public final String getOtherServiceAETAttrs() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < otherServiceNames.length; i++) {
            sb.append(otherServiceNames[i].toString()).append('#').append(
                    otherServiceAETAttrs[i]).append("\r\n");
        }
        return sb.toString();
    }

    public final void setOtherServiceAETAttrs(String s) {
        StringTokenizer stk = new StringTokenizer(s, "\r\n\t ");
        int count = stk.countTokens();
        ObjectName[] names = new ObjectName[count];
        String[] attrs = new String[count];
        String tk = null;
        try {
            int endName;
            for (int i = 0; i < names.length; i++) {
                tk = stk.nextToken();
                endName = tk.indexOf('#');
                names[i] = new ObjectName(tk.substring(0, endName));
                attrs[i] = tk.substring(endName+1);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(tk);
        }
        otherServiceNames = names;
        otherServiceAETAttrs = attrs;
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

    /**
     * Initialize file system to make sure there's a DEF_RW one ready
     * 
     * @param availability the file system availability, ONLINE, NEARLINE
     * @return the default RW file system
     * @throws Exception
     */
    protected FileSystemDTO initFileSystem(int availability) throws Exception {
        FileSystemMgt fsmgt = newFileSystemMgt();
        FileSystemDTO[] c = fsmgt.findFileSystems(retrieveAET,
        		availability, FileSystemStatus.DEF_RW);
        FileSystemDTO fsDTO = null;
        if (c.length > 0) {
            fsDTO = c[0];
        } else {
            c = fsmgt.findFileSystems(retrieveAET, availability, FileSystemStatus.RW);
            if (c.length > 0) {
            	fsDTO = c[0];
                c[0].setStatus(FileSystemStatus.DEF_RW);
                fsmgt.updateFileSystem(c[0]);
            }   
        }     
        return fsDTO;
    }
    
    protected void initStorageFileSystem() throws Exception {
    	storageFileSystem = initFileSystem(Availability.ONLINE);
    	if(storageFileSystem == null) {
    		if(defStorageDir != null && !defStorageDir.equals("NONE")){
                storageFileSystem = addFileSystem(defStorageDir, ONLINE_STORAGE,
                        retrieveAET, Availability.ONLINE,
                        FileSystemStatus.DEF_RW, null);
                log.warn("No writeable Storage Directory configured for retrieve AET "
                        + retrieveAET + "- initalize default " + storageFileSystem);
            } else  {
            	log.warn("No writeable Storage Directory configured for retrieve AET " +
            			retrieveAET + "- online storage is not available at this moment but should be configured later." );
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
            FileSystemDTO fsDTO = fsmgt.getFileSystem(new Long(storageFileSystem.getPk()));
            if (!checkStorageFileSystemStatus(fsDTO)
                    || checkDiskSpace
                    && !checkStorageFileSystem(fsDTO))
                if (!switchStorageFileSystem(fsmgt, fsDTO))
                    return null;
        }
        return storageFileSystem;
    }

    private synchronized boolean switchStorageFileSystem(FileSystemMgt fsmgt,
            FileSystemDTO fsDTO) throws RemoteException, FinderException,
            IOException {
        if (storageFileSystem.getPk() != fsDTO.getPk()) {
        	log.info("Storage file system has already been switched. Initially was "
                    + fsDTO.toString() + ", now it is "
                    + storageFileSystem.toString());
            return true; // already switched by another thread
        }
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
                || !checkStorageFileSystem(fsDTO, false));
        // reload from database to get most recent data before updating it
		storageFileSystem = fsmgt.getFileSystem(storageFileSystem.getPk());
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
    	return checkStorageFileSystem(fsDTO, true);
    }
    
    private boolean checkStorageFileSystem(FileSystemDTO fsDTO,
    		boolean updateCheckTime)
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
        
        if (updateCheckTime) {
            checkStorageFileSystem = System.currentTimeMillis() + Math.min(
                    freeSpace * checkFreeDiskSpaceMinInterval / minFreeDiskSpace,
                    checkFreeDiskSpaceMaxInterval);
        }
        
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

    protected FileSystemMgt newFileSystemMgt() {
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
        int deleted, total = 0;
        try {
            FileSystemMgt fsMgt = newFileSystemMgt();
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
        } finally {
            isPurging = false;
        }
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
            try {
                log.debug("Check for unreferenced (private) files to delete in filesystem:"
                            + purgeDirPath);
                FileSystemMgt fsMgt = newFileSystemMgt();
                int limit = getLimitNumberOfFilesPerTask();
                total = purgePrivateFiles(purgeDirPath, fsMgt, limit);
            } finally {
                isPurging = false;
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

    public Object locateInstance(String iuid) throws Exception {
        FileDTO[] fileDTOs = null;
        String aet = null;
        try {
            fileDTOs = newFileSystemMgt().getFilesOfInstance(iuid);
            if (fileDTOs.length == 0) {
                aet = newFileSystemMgt().getExternalRetrieveAET(iuid);
            } else {
                FileDTO dto;
                for (int i = 0; i < fileDTOs.length; ++i) {
                    dto = fileDTOs[i];
                    if (retrieveAET.equals(dto.getRetrieveAET()))
                        return FileUtils.toFile(dto.getDirectoryPath(), dto
                                .getFilePath());
                }
                aet = fileDTOs[0].getRetrieveAET();
            }
        } catch (FinderException ignore) {}
        if ( aet == null ) return null;
        AEDTO aeData = aeMgt().findByAET(aet);
        return aeData.getHostName();
    }

    private AEManager aeMgt() throws Exception {
        AEManagerHome home = (AEManagerHome) EJBHomeFactory.getFactory()
                .lookup(AEManagerHome.class, AEManagerHome.JNDI_NAME);
        return home.create();
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
            if (adjustExpectedDataVolumePerDay != 0
                    && now.getTimeInMillis() > adjustExpectedDataVolumePerDay) {
                adjustExpectedDataVolumePerDay(fsMgt, fs);
                adjustExpectedDataVolumePerDay = nextMidnight();
            }
            long available = getAvailableDiskSpace(fs) - minFreeDiskSpace
                    * fs.length;
            long freeSize = deleterThresholds.getDeleterThreshold(now)
                    .getFreeSize(expectedDataVolumePerDay);
            long maxSizeToDel = freeSize - available;
            if (maxSizeToDel > 0) {
                freeDiskSpace(retrieveAET, deleteUncommited, flushOnMedia,
                        flushExternalRetrievable, flushOnNearline, 
                        flushOnROFsAvailable, validFileStatus, maxSizeToDel,
                        limitNumberOfFilesPerTask);
            } else if (studyCacheTimeout > 0L) {
                long accessedBefore = System.currentTimeMillis()
                        - studyCacheTimeout;
                releaseStudies(retrieveAET, deleteUncommited, flushOnMedia,
                        flushExternalRetrievable, flushOnNearline, 
                        flushOnROFsAvailable, validFileStatus, accessedBefore);
            } else {
                return;
            }
        } catch (Exception e) {
            log.error("Free Disk Space failed:", e);
            return;
        }
    }

    private void freeDiskSpace(String retrieveAET, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, boolean checkOnNearline, 
            boolean checkOnRO, int validFileStatus, long maxSizeToDel, 
            int deleteStudiesLimit)
            throws Exception {
        Map ians = new HashMap();
        log.info("Free Disk Space: try to release "
                + (maxSizeToDel / 1000000.f) + "MB of DiskSpace");

        releaseStudies(retrieveAET, ians, checkUncommited, checkOnMedia,
                checkExternal, checkOnNearline, checkOnRO, validFileStatus, 
                maxSizeToDel, deleteStudiesLimit);
    }

    private void releaseStudies(String retrieveAET, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, boolean checkOnNearline, 
            boolean checkOnRO, int validFileStatus, long accessedBefore) 
    		throws Exception {
        Timestamp tsBefore = new Timestamp(accessedBefore);
        log.info("Releasing studies not accessed since " + tsBefore);
        Map ians = new HashMap();
        releaseStudies(retrieveAET, ians, checkUncommited, checkOnMedia,
                checkExternal, checkOnNearline, checkOnRO, validFileStatus, 
                Long.MAX_VALUE, new Timestamp(accessedBefore));
    }

    private long releaseStudies(String retrieveAET, Map ians,
            boolean checkUncommited, boolean checkOnMedia,
            boolean checkExternal, boolean checkOnNearline, boolean checkOnRO, int validFileStatus,
            long maxSizeToDel, Timestamp tsBefore) throws Exception {

        Collection c = newFileSystemMgt().getStudiesOnFsByLastAccess(
                retrieveAET, tsBefore);
        if (log.isDebugEnabled()) {
            log.debug("Release studies on filesystem(s) accessed before "
                    + tsBefore + " :" + c.size());
            log.debug(" checkUncommited: " + checkUncommited);
            log.debug(" checkOnMedia: " + checkOnMedia);
            log.debug(" checkExternal: " + checkExternal);
            log.debug(" checkOnNearline: " + checkOnNearline);
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
                    checkOnMedia, checkExternal, checkOnNearline, checkOnRO, validFileStatus);
        }

        log.info("Released " + (sizeToDelete / 1000000.f) + "MB of DiskSpace");
        return sizeToDelete;
    }

    private long releaseStudies(String retrieveAET, Map ians,
            boolean checkUncommited, boolean checkOnMedia,
            boolean checkExternal, boolean checkOnNearline, boolean checkOnRO, int validFileStatus,
            long maxSizeToDel, int deleteStudiesLimit) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Release oldest studies on " + retrieveAET);
            log.debug(" checkUncommited: " + checkUncommited);
            log.debug(" checkOnMedia: " + checkOnMedia);
            log.debug(" checkExternal: " + checkExternal);
            log.debug(" checkOnNearline: " + checkOnNearline);
            log.debug(" checkCopyAvailable: " + checkOnRO);
            log.debug(" maxSizeToDel: " + maxSizeToDel);
            log.debug(" deleteStudyBatchSize: " + deleteStudiesLimit);
        }

        // Latest StudyOnFs record ineligible for deletion
        Timestamp tsAfter = new Timestamp(0);

        // Total file size that has been deleted
        long sizeDeleted = 0L;
        for (; sizeDeleted < maxSizeToDel;) {

            Collection c = newFileSystemMgt().getStudiesOnFsAfterAccessTime(retrieveAET,
                    tsAfter, deleteStudiesLimit );
            if (c.size() == 0)
                break;

            if ( log.isDebugEnabled())
                log.debug("Evaluating batch of " + c.size()
                        + " studies with accessTime later than " + tsAfter);

            for (Iterator iter = c.iterator(); iter.hasNext()
                    && sizeDeleted < maxSizeToDel;) {
                StudyOnFileSystemLocal studyOnFs = (StudyOnFileSystemLocal) iter
                        .next();
                long size = releaseStudy(studyOnFs, checkUncommited,
                        checkOnMedia, checkExternal, checkOnNearline, checkOnRO, validFileStatus);
                if (size != 0)
                    sizeDeleted += size;
                else
                    tsAfter = studyOnFs.getAccessTime();
            }
        }
        log.info("Released " + (sizeDeleted / 1000000.f) + "MB of DiskSpace");
        return sizeDeleted;
    }

    public String adjustExpectedDataVolumePerDay() throws Exception {
        FileSystemMgt fsMgt = newFileSystemMgt();
        return adjustExpectedDataVolumePerDay(fsMgt,
                listLocalOnlineRWFileSystems(fsMgt));
    }

    private String adjustExpectedDataVolumePerDay(FileSystemMgt fsMgt,
            FileSystemDTO[] fs) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.roll(Calendar.DAY_OF_MONTH, false);
        long after = cal.getTimeInMillis();
        long sum = 0L;
        for (int i = 0; i < fs.length; i++) {
            sum = fsMgt.sizeOfFilesCreatedAfter(new Long(fs[i].getPk()), after);
        }
        String size = FileUtils.formatSize(sum);
        if (sum > expectedDataVolumePerDay) {
            server.setAttribute(super.serviceName, new Attribute(
                    "ExpectedDataVolumePerDay", size));
        }
        return size;
    }

    private long releaseStudy(StudyOnFileSystemLocal studyOnFs,
            boolean deleteUncommited, boolean flushOnMedia,
            boolean flushExternal, boolean flushOnNearline, 
            boolean flushOnROFs, int validFileStatus)
            throws Exception {
        long size = 0L;
        StudyLocal study = studyOnFs.getStudy();
        boolean release = flushExternal && study.isStudyExternalRetrievable()
                || flushOnMedia && study.isStudyAvailableOnMedia()
                || flushOnNearline && study.isStudyAvailable(Availability.NEARLINE)
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
                        .getFileSystem().getPk(), deleteUncommited, deleteEmptyPatient), System
                        .currentTimeMillis());

                // Remove the StudyOnFs record from database immediately to
                // prevent duplicate query
                // when previous jobs are not finished yet
                studyOnFs.remove();
            } finally {
                fsmgt.remove();
            }
        } else {
            if ( log.isTraceEnabled() )
                log.trace("Study [" + study.getStudyIuid() + "] doesnt fulfil any deleter rule!");
        }
        return size;
    }

    private void releaseStudy(PurgeStudyOrder order) throws Exception {
        FileSystemMgt fsmgt = newFileSystemMgt();

        Collection files = new ArrayList();
        Dataset ian = fsmgt.releaseStudy(order.getStudyPk(), order.getFsPk(),
                order.isDeleteUncommited(), order.isDeleteEmptyPatient(), files);

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
    
    /**
     * Show all file systems in unsorted format given the availability and status. 
     * 
     * @param availability The availability of file system, e.g., ONLINE, NEARLINE. If it's negative, ignored.
     * @param status The status of file system, e.g., RW+, RW, RO. If it's negative, ignored.
     * @return
     * @throws RemoteException
     * @throws FinderException
     */
    public String showAllFileSystems(int availability, int status) 
    	throws RemoteException, FinderException {
        FileSystemDTO[] dtos = listAllFileSystems();
        if(availability >= -1 || status >= -1) {
        	ArrayList dtoa = new ArrayList();
        	for(int i = 0; i < dtos.length; i++) {
        		if( (availability < -1 || availability >= -1 && dtos[i].getAvailability() == availability)
        				&& (status < -1 || status >= -1 && dtos[i].getStatus() == status) )
        			dtoa.add(dtos[i]);
        	}
        	if(dtoa.size() == 0)
        		return "NONE";
        	dtos = (FileSystemDTO[])dtoa.toArray(new FileSystemDTO[dtoa.size()]);
        }
        
        return toString(dtos);
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
        if(validateStoragePath(dirPath, Availability.toInt(availability)))
            return addFileSystem(dirPath, availability + _STORAGE,
                    retrieveAET, Availability.toInt(availability),
                    FileSystemStatus.toInt(status), userInfo);
        else
            throw new IllegalArgumentException(
                    "Failed to validate filesystem: " + dirPath);
    }

    private FileSystemDTO addFileSystem(String dirPath, String groupId,
            String retrieveAET, int availability, int status, String userInfo)
            throws RemoteException, CreateException {
        FileSystemDTO dto = new FileSystemDTO();
        dto.setDirectoryPath(dirPath);
        dto.setGroupId(groupId);
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
        dto.setGroupId(availability + _STORAGE);
        dto.setRetrieveAET(retrieveAET);
        dto.setAvailability(Availability.toInt(availability));
        dto.setStatus(FileSystemStatus.toInt(status));
        dto.setUserInfo(userInfo);
        newFileSystemMgt().updateFileSystem(dto);
        checkStorageFileSystem();
    }

    public boolean updateFileSystemStatus(String dirPath, String status)
            throws RemoteException, FinderException {
        if (!newFileSystemMgt().updateFileSystemStatus(dirPath, 
                FileSystemStatus.toInt(status))) {
            return false;
        }
        checkStorageFileSystem();
        return true;
    }
    
    public void checkStorageFileSystem() {
        checkStorageFileSystem = 0;
    }

    public boolean updateFileSystemAvailability(String dirPath,
            String availability) throws Exception {
        FileSystemMgt mgt = newFileSystemMgt();
        int iAvail = Availability.toInt(availability);
        log.info("Update availability of " + dirPath + " to " + availability
                + "(" + iAvail + ")");
        if (mgt.updateFileSystemAvailability(dirPath, iAvail)) {
            checkStorageFileSystem();
            log.info("Update availability of all instances of filesystem "
                    + dirPath);
            updateDerivedFields(dirPath, false, true, mgt);
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
    
    public int updateAETitle(String prevAET, String newAET) 
            throws Exception {
        int count = 0;
        for (int i = 0; i < otherServiceNames.length; i++) {
            if (updateAETitle(otherServiceNames[i], otherServiceAETAttrs[i],
                    prevAET, newAET)) {
                ++count;
            }
        }
        server.invoke(aeServiceName, "updateAETitle", 
                new Object[]{ prevAET, newAET },
                new String[]{ 
                    String.class.getName(), 
                    String.class.getName()});
        return count;
    }
    
    private boolean updateAETitle(ObjectName name, String attr,
            String prevAET, String newAET) throws Exception {
        try {
            String val = (String) server.getAttribute(name, attr);
            String[] aets = StringUtils.split(val, '\\');
            boolean modified = false;
            for (int i = 0; i < aets.length; i++) {
                if (aets[i].equals(prevAET)) {
                    aets[i] = newAET;
                    modified = true;
                }
            }
            if (modified) {
                server.setAttribute(name, 
                        new Attribute(attr, StringUtils.toString(aets, '\\')));
                log.info("Update AETitle in attribute: " + name + "#" + attr);
            }
            return modified;
        } catch (AttributeNotFoundException e) {
            log.info("No such attribute: " + name + "#" + attr);
            return false;
        }
    }

    public boolean updateRetrieveAETitle(String newAET) throws Exception {
        if ( newAET.equals(retrieveAET)) return false;
        FileSystemMgt mgt = newFileSystemMgt();
        FileSystemDTO[] dtos = mgt.findFileSystems(retrieveAET);
        for ( int i = 0 ; i < dtos.length ; i++ ) {
            updateFileSystemRetrieveAET( dtos[i].getDirectoryPath(), newAET, mgt);
        }
        updateAETitle(retrieveAET, newAET);
        return true;
    }

    public boolean updateFileSystemRetrieveAET(String dirPath, String retrieveAET)
    throws RemoteException, FinderException {
        return updateFileSystemRetrieveAET( dirPath, retrieveAET, newFileSystemMgt());
    }
    
    private boolean updateFileSystemRetrieveAET(String dirPath,
            String retrieveAET, FileSystemMgt mgt)
    throws RemoteException, FinderException {
        log.info("Update retrieveAET of " + dirPath + " to " + retrieveAET);
        if (mgt.updateFileSystemRetrieveAET(dirPath, retrieveAET)) {
            updateDerivedFields(dirPath, true, false, mgt);
            return true;
        } else {
            return false;
        }
    }
    public int updateDerivedFields(String dirPath,
            boolean retrieveAET, boolean availability) throws RemoteException, FinderException {
        return updateDerivedFields(dirPath, retrieveAET, availability, newFileSystemMgt());
    }
    private int updateDerivedFields(String dirPath,
                boolean retrieveAET, boolean availability, FileSystemMgt mgt) throws RemoteException, FinderException {
            checkStorageFileSystem();
            int offset = 0;
            int limit = limitNumberOfFilesPerTask;
            int size;
            log.info("Update Derived Fields of all instances of filesystem "
                    + dirPath);
            while ((size = mgt.updateInstanceDerivedFields(dirPath, retrieveAET, availability, offset,
                    limit)) > 0) {
                log.info("  " + size + " instances updated!");
                offset += size;
            }
            log.info(offset+" Instances of " + dirPath + " updated");
            return offset;
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
    	if(validateStoragePath(dirPath, Availability.ONLINE))
    	    return addAndLinkFileSystem(dirPath, ONLINE_STORAGE,
    	            Availability.ONLINE, FileSystemStatus.RW, userInfo);
    	else
    	    throw new IllegalArgumentException(
                    "Failed to validate online filesystem: " + dirPath);
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
    	if(validateStoragePath(dirPath, Availability.NEARLINE))
    	    return addAndLinkFileSystem(dirPath, NEARLINE_STORAGE,
    	            Availability.NEARLINE, FileSystemStatus.RW, userInfo);
    	else
    	    throw new IllegalArgumentException(
                    "Failed to validate nearline filesystem: " + dirPath);
    }

    public String showNearlineFileSystems() throws RemoteException,
            FinderException {
        return showFileSystemsWithAvailability(Availability.NEARLINE);
   }

    public String removeFileSystem(String dirPath) throws RemoteException,
            FinderException, RemoveException {
        return newFileSystemMgt().removeFileSystem(dirPath).toString();
    }

    private String addAndLinkFileSystem(String dirPath, String groupId,
            int availability, int status, String userInfo)
            throws FinderException, CreateException, RemoteException {
        FileSystemDTO dto = new FileSystemDTO();
        dto.setDirectoryPath(dirPath);
        dto.setGroupId(groupId);
        dto.setRetrieveAET(retrieveAET);
        dto.setAvailability(availability);
        dto.setStatus(status);
        dto.setUserInfo(userInfo);
        FileSystemDTO newFS = newFileSystemMgt().addAndLinkFileSystem(dto);
        checkStorageFileSystem();
        return newFS.toString();
    }
    
    /**
     * Valide storage path. Customized FileSystemService can provide logic to
     * validate the path
     * 
     * @param path
     * @param availability
     * @return
     */
    protected boolean validateStoragePath(String path, int availability) {
    	// By default, it's always valid
    	return true;
    }
    
    public int deleteWholeStudy(String studyIUID, boolean deleteEmptyPatient) throws RemoteException, FinderException, RemoveException {
        FileDTO[] fileDTOs = newFileSystemMgt().deleteWholeStudy(studyIUID, deleteEmptyPatient);
        File file;
        for ( int i = 0 ; i < fileDTOs.length ; i++ ) {
            file = FileUtils.toFile(fileDTOs[i].getDirectoryPath(),fileDTOs[i].getFilePath());
            delete(file);
        }
        return fileDTOs.length;
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