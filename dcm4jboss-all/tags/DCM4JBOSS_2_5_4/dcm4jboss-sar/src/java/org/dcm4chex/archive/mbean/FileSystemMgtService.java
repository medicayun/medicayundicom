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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ejb.FinderException;
import javax.management.Attribute;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DataSource;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.QueryFilesCmd;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.notif.IANNotificationVO;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileDataSource;
import org.dcm4chex.archive.util.FileUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
 * @since 12.09.2004
 *
 */
public class FileSystemMgtService extends TimerSupport {

    public static final String EVENT_TYPE = "org.dcm4chex.archive.mbean.FileSystemMgtService";

    public static final NotificationFilter NOTIF_FILTER = new NotificationFilter() {

		private static final long serialVersionUID = 3257288045601634612L;

		public boolean isNotificationEnabled(Notification notif) {
            return EVENT_TYPE.equals(notif.getType());
        }
    };
	
    private static final long MIN_FREE_DISK_SPACE = 20 * FileUtils.MEGA;    
    private static final String LOCAL = "local";
    
    private long minFreeDiskSpace = MIN_FREE_DISK_SPACE;

    private List dirPathList = Arrays.asList(new File[] { new File("archive")});

    private Set fsPathSet = Collections.singleton("archive");

    private List rodirPathList = Collections.EMPTY_LIST;

    private Set rofsPathSet = Collections.EMPTY_SET;

    private String retrieveAET = "QR_SCP";

    private int curDirIndex = 0;
    
    private String mountFailedCheckFile = "NO_MOUNT";

    private boolean makeStorageDirectory = true;

	private float freeDiskSpaceLowerThreshold = 1.5f;
	
	private float freeDiskSpaceUpperThreshold = 2.5f;
	
	private boolean flushExternalRetrievable = false;
	
	private boolean flushOnMedia = false;
	
	private boolean deleteUncommited = false;
	
	private long studyCacheTimeout = 0L;
    
    private long purgeFilesInterval = 0L;

	private int limitNumberOfFilesPerTask = 1000;
    
    private long freeDiskSpaceInterval = 0L;
    
    private Integer purgeFilesListenerID;

    private Integer freeDiskSpaceListenerID;
    
    private boolean autoPurge = true;
    
    private boolean freeDiskSpaceOnDemand = true;

	private boolean sendIANs = true;
	
	private boolean isPurging = false;

	/** holds available disk space over all file systems. this value is set in getAvailableDiskspace ( and checkFreeDiskSpaceNecessary ). */
	private long availableDiskSpace = 0L;
    
    private final NotificationListener purgeFilesListener = 
        new NotificationListener(){
            public void handleNotification(Notification notif, Object handback) {
                purgeFiles();
            }};

    private final NotificationListener freeDiskSpaceListener = 
        new NotificationListener(){
            public void handleNotification(Notification notif, Object handback) {
                freeDiskSpace();
            }};
            
    private static String null2local(String s) {
        return s == null ? LOCAL : s;
    }

    private static String local2null(String s) {
        return LOCAL.equals(s) ? null : s;
    }

    public String getEjbProviderURL() {
        return null2local(EJBHomeFactory.getEjbProviderURL());
    }

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(local2null(ejbProviderURL));
    }

    public final String getDirectoryPathList() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, n = dirPathList.size(); i < n; i++) {
            sb.append(dirPathList.get(i));
            if (i == curDirIndex)
                sb.append('*');
            sb.append(File.pathSeparatorChar);
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public final void setDirectoryPathList(String str) {
        StringTokenizer st = new StringTokenizer(str, File.pathSeparator);
        ArrayList list = new ArrayList();
        HashSet set = new HashSet();
        int dirIndex = 0;
        for (int i = 0; st.hasMoreTokens(); ++i) {
            String tk = st.nextToken();
            int len = tk.length();
            if (tk.charAt(len-1) == '*') {
                dirIndex = i;
                tk = tk.substring(0, len-1);
            }                
            set.add(tk.replace(File.separatorChar, '/'));
            list.add(new File(tk));
        }
        if (list.isEmpty())
                throw new IllegalArgumentException(
                        "DirectoryPathList must NOT be emtpy");
        dirPathList = list;
        fsPathSet = set;
        curDirIndex = dirIndex;
    }

    private void storeDirectoryPathList() {
		Attribute a = new Attribute("DirectoryPaths", getDirectoryPathList());
		try {
			server.setAttribute(super.getServiceName(), a);
		} catch (Exception e) {
			log.warn("Failed to store DirectoryPaths", e);
		}
    }

    public final String getReadOnlyDirectoryPathList() {
        if (rodirPathList.isEmpty())
            return "NONE";
        StringBuffer sb = new StringBuffer();
        for (int i = 0, n = rodirPathList.size(); i < n; i++) {
            sb.append(rodirPathList.get(i)).append(File.pathSeparatorChar);
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public final void setReadOnlyDirectoryPathList(String str) {
        if ("NONE".equals(str)) {
            rodirPathList = Collections.EMPTY_LIST;
            rofsPathSet = Collections.EMPTY_SET;
            return;
        }
        StringTokenizer st = new StringTokenizer(str, File.pathSeparator);
        ArrayList list = new ArrayList();
        HashSet set = new HashSet();
        int dirIndex = 0;
        for (int i = 0; st.hasMoreTokens(); ++i) {
            String tk = st.nextToken();
            set.add(tk.replace(File.separatorChar, '/'));
            list.add(new File(tk));
        }
        rodirPathList = list;
        rofsPathSet = set;
    }
    
    public final String getRetrieveAET() {
        return retrieveAET;
    }

    public final void setRetrieveAET(String aet) {
        this.retrieveAET = aet;
    }

    public final String getMinFreeDiskSpace() {
        return FileUtils.formatSize(minFreeDiskSpace);
    }

    public final void setMinFreeDiskSpace(String str) {
        this.minFreeDiskSpace = FileUtils.parseSize(str, MIN_FREE_DISK_SPACE);
    }

	/**
	 * Return the factor to calculate watermark for free disk space process.
	 * <p>
	 * The threshold for freeDiskSpace process is calculated: <code>minFreeDiskSpace * freeDiskSpaceLowerThreshold * numberOfFilesystems</code>
	 * 
	 * @return Returns the cleanWaterMarkFactor.
	 */
	public float getFreeDiskSpaceLowerThreshold() {
		return freeDiskSpaceLowerThreshold;
	}
	/**
	 * Set the factor to calculate watermark for free disk space process.
	 * @param freeDiskSpaceLowerThreshold The freeDiskSpaceLowerThreshold to set.
	 */
	public void setFreeDiskSpaceLowerThreshold(float threshold) {
		if ( threshold < 1.0f ) throw new IllegalArgumentException("FreeDiskSpaceLowerThreshold must NOT be smaller than 1!");
		this.freeDiskSpaceLowerThreshold = threshold;
	}
	
	/**
	 * Returns the factor to calculate the watermark to stop free disk space process.
	 * <p>
	 * The threshold to stop freeDiskSpace process is calculated: <code>minFreeDiskSpace * freeDiskSpaceUpperThreshold * numberOfFileSytsems</code>
	 * 
	 * @return Returns the stopCleanWaterMarkFactor.
	 */
	public float getFreeDiskSpaceUpperThreshold() {
		return freeDiskSpaceUpperThreshold;
	}
	/**
	 * Set the factor to calculate the watermark to stop free disk space process.
	 * <p>
	 * The watermark to stop freeDiskSpace process is calculated: <code>minFreeDiskSpace * freeDiskSpaceUpperThreshold * numberOfFileSytsems</code>
	 * 
	 * @param stopCleanWaterMarkFactor The stopCleanWaterMarkFactor to set.
	 */
	public void setFreeDiskSpaceUpperThreshold(float threshold) {
		if ( threshold < freeDiskSpaceLowerThreshold ) throw new IllegalArgumentException("FreeDiskSpaceUpperThreshold must be higher than FreeDiskSpaceLowerThreshold");
		this.freeDiskSpaceUpperThreshold = threshold;
	}

	/**
	 * Returns true if the freeDiskSpace policy flushExternalRetrievable is enabled.
	 * <p>
	 * If this policy is active studies must be external retrievable for deletion.
	 * 
	 * @return Returns true if flushExternalRetrievable policy is active.
	 */
	public boolean isFlushStudiesExternalRetrievable() {
		return flushExternalRetrievable;
	}
	/**
	 * Set the freeDiskSpace policy flushExternalRetrievable.
	 * <p>
	 * Set this policy active if studies must be external retrievable for deletion.
	 * 
	 * @param b The flushExternalRetrievable to set.
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
	 * @param b The deleteUncommited to set.
	 */
	public void setDeleteStudiesStorageNotCommited(boolean b) {
		deleteUncommited = b;
	}
	
	/**
	 * Returns true if the freeDiskSpace policy flushOnMedia is enabled.
	 * <p>
	 * If this policy is active studies must be stored on media (offline storage) for deletion.
	 * 
	 * @return Returns true if flushOnMedia policy is active.
	 */
	public boolean isFlushStudiesOnMedia() {
		return flushOnMedia;
	}
	
	/**
	 * Set the freeDiskSpace policy flushOnMedia.
	 * <p>
	 * Set this policy active if studies must be on media (offline storage) for deletion.
	 * 
	 * @param b The flushOnMedia to set.
	 */
	public void setFlushStudiesOnMedia(boolean b) {
		this.flushOnMedia = b;
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
	 * @param StudyCacheTimeoutDays The StudyCacheTimeoutDays to set in days.
	 */
	public void setStudyCacheTimeout(String interval) {
        this.studyCacheTimeout = RetryIntervalls.parseIntervalOrNever(interval);
	}
	
    public final String getFreeDiskSpaceInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(freeDiskSpaceInterval);
    }
    
    public void setFreeDiskSpaceInterval(String interval) {
        this.freeDiskSpaceInterval = RetryIntervalls.parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            stopScheduler("CheckFreeDiskSpace", freeDiskSpaceListenerID,
            		freeDiskSpaceListener);
            freeDiskSpaceListenerID = startScheduler("CheckFreeDiskSpace",
            		freeDiskSpaceInterval, freeDiskSpaceListener);
        }
    }
    
	/**
	 * @return Returns the freeDiskSpaceOnDemand.
	 */
	public boolean isFreeDiskSpaceOnDemand() {
		return freeDiskSpaceOnDemand;
	}
	/**
	 * @param freeDiskSpaceOnDemand The freeDiskSpaceOnDemand to set.
	 */
	public void setFreeDiskSpaceOnDemand(boolean freeDiskSpaceOnDemand) {
		this.freeDiskSpaceOnDemand = freeDiskSpaceOnDemand;
	}
	/**
	 * @return Returns the autoPurge.
	 */
	public boolean isPurgeFilesAfterFreeDiskSpace() {
		return autoPurge;
	}
	/**
	 * @param autoPurge The autoPurge to set.
	 */
	public void setPurgeFilesAfterFreeDiskSpace(boolean autoPurge) {
		this.autoPurge = autoPurge;
	}
    public final String getPurgeFilesInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(purgeFilesInterval);
    }
    
    public void setPurgeFilesInterval(String interval) {
        this.purgeFilesInterval = RetryIntervalls.parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            stopScheduler("CheckFilesToPurge", purgeFilesListenerID,
            		purgeFilesListener);
            purgeFilesListenerID = startScheduler("CheckFilesToPurge",
            		purgeFilesInterval, purgeFilesListener);
        }
    }
    
    public final int getLimitNumberOfFilesPerTask() {
    	return limitNumberOfFilesPerTask;
    }
    
    public void setLimitNumberOfFilesPerTask( int limit ) {
    	limitNumberOfFilesPerTask = limit;
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
    
	/**
	 * @return Returns the sendIANs.
	 */
	public boolean isSendIANs() {
		return sendIANs;
	}
	/**
	 * @param sendIANs The sendIANs to set.
	 */
	public void setSendIANs(boolean sendIANs) {
		this.sendIANs = sendIANs;
	}
    public final boolean isLocalFileSystem(String fsdir) {
        return fsPathSet.contains(fsdir) || rofsPathSet.contains(fsdir);
    }
    
    public final String[] fileSystemDirPaths() {
        return (String[]) fsPathSet.toArray(new String[fsPathSet.size()]);
    }    

    protected void startService() throws Exception {
         super.startService();
         freeDiskSpaceListenerID = startScheduler("CheckFreeDiskSpace",
         		freeDiskSpaceInterval, freeDiskSpaceListener);
         purgeFilesListenerID = startScheduler("CheckFilesToPurge",
         		purgeFilesInterval, purgeFilesListener);
         
    }
    
    protected void stopService() throws Exception {
        stopScheduler("CheckFreeDiskSpace", freeDiskSpaceListenerID,
        		freeDiskSpaceListener);
        stopScheduler("CheckFilesToPurge", purgeFilesListenerID,
        		purgeFilesListener);
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

    public String showAvailableDiskSpace() throws IOException {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, n = dirPathList.size(); i < n; i++) {
            FileSystemInfo info = initFileSystemInfo((File) dirPathList.get(i));
            sb.append(info).append("\r\n");
        }
        return sb.toString();
    }
    
    private FileSystemInfo initFileSystemInfo(File dir) throws IOException {
        File d = FileUtils.resolve(dir);
        if (!d.isDirectory()) {
            if (!makeStorageDirectory) {
                throw new IOException("Storage Directory " + d
	                    + " does not exists.");
            } else {
                if (d.mkdirs()) {
                    log.warn("M-CREATE Storage Directory: " + d);
                } else {
                    throw new IOException("Failed to create Storage Directory " + d);
                }
            }
        } else {
            if (new File(d, mountFailedCheckFile).exists()) {
	            throw new IOException("Mount check of Storage Directory " + d
	                    + " failed: Found " + mountFailedCheckFile);
            }
        }
        long available = new se.mog.io.File(d).getDiskSpaceAvailable();
        return new FileSystemInfo(FileUtils.slashify(dir), d, available, retrieveAET);
    }

    public FileSystemInfo selectStorageFileSystem() throws IOException {
        File curDir = (File) dirPathList.get(curDirIndex);
        FileSystemInfo info = initFileSystemInfo(curDir);
        if (info.getAvailable() > minFreeDiskSpace)
            return info;
        for (int i = 1, n = dirPathList.size(); i < n; ++i) {
            int dirIndex = (curDirIndex + i) % n;
            File dir = (File) dirPathList.get(dirIndex);
            info = initFileSystemInfo(dir);
            if (info.getAvailable() > minFreeDiskSpace) {
                log.info("High Water Mark reached on current Storage Directory "
                        + curDir + " - switch Storage Directory to " + dir);
                curDirIndex = dirIndex;
				storeDirectoryPathList();
				return info;
            }
        }
        log.error("High Water Mark reached on Storage Directory " + curDir
                + " - no alternative Storage Directory available");
        return info;
    }

    public void purgeFiles() {
        log.info("Check for unreferenced files to delete");
        synchronized (this) {  
	        if ( isPurging ) {
	        	log.info("A purge task is already in progress! Ignore this purge order!");
	        	return;
	        }
	        isPurging = true;
        }
        FileSystemMgt fsMgt = newFileSystemMgt();
        FileDTO[] toDelete;
        for (int i = 0, n = dirPathList.size(); i < n; ++i) {
            File dirPath = (File) dirPathList.get(i);
            if ( ! purgeFiles(dirPath,fsMgt) ) break;
        }
        
        try {
            fsMgt.remove();
        } catch (Exception ignore) {
        }
        isPurging = false;
    }
    public void purgeFiles( String purgeDirPath ) {
    	if ( purgeDirPath == null ) {
    		purgeFiles();
    	} else {
            synchronized (this) {  
    	        if ( isPurging ) {
    	        	log.info("A purge task is already in progress! Ignore this purge order!");
    	        	return;
    	        }
    	        isPurging = true;
            }
            log.info("Check for unreferenced files to delete in filesystem:"+purgeDirPath);
		    FileSystemMgt fsMgt = newFileSystemMgt();
			purgeFiles(new File(purgeDirPath),fsMgt);
			isPurging = false;
		    try {
		        fsMgt.remove();
		    } catch (Exception ignore) {
		    }
    	}   	
    }
    
    private boolean purgeFiles( File purgeDirPath, FileSystemMgt fsMgt ) {
        FileDTO[] toDelete;
    	try {
    		toDelete = fsMgt.getDereferencedFiles(
					FileUtils.slashify(purgeDirPath), getLimitNumberOfFilesPerTask() );
            if ( log.isDebugEnabled()) log.debug("purgeFiles: found "+toDelete.length+" files to delete on dirPath:"+purgeDirPath);
        } catch (Exception e) {
            log.warn("Failed to query dereferenced files:", e);
            return false;
        }
        for (int j = 0; j < toDelete.length; j++) {
            FileDTO fileDTO = toDelete[j];
			File file = FileUtils.toFile(fileDTO.getDirectoryPath(),
                    fileDTO.getFilePath());
            try {
                fsMgt.deleteFile(fileDTO.getPk());
            } catch (Exception e) {
                log.warn("Failed to remove File Record[pk=" 
						+ fileDTO.getPk() + "] from DB:", e);
                log.info("-> Keep dereferenced file: " + file);
				continue;
            }
            delete(file);
        }
        return true;
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
        List list = new ArrayList();
		QueryFilesCmd cmd = new QueryFilesCmd(iuid);
		try {
			while (cmd.next())
				list.add(cmd.getFileDTO());
		} finally {
			cmd.close();
		}
        if (list.isEmpty())
            return null;
        for (int i = 0, n = list.size(); i < n; ++i) {
            FileDTO dto = (FileDTO) list.get(i);
            String dirPath = dto.getDirectoryPath();
            if (isLocalFileSystem(dirPath))
                return FileUtils.toFile(dirPath, dto.getFilePath());
        }
        FileDTO dto = (FileDTO) list.get(0);
        AEData aeData = new AECmd(dto.getRetrieveAET()).getAEData();
        return aeData.getHostName();
    }
    
    public DataSource getDatasourceOfInstance(String iuid) throws SQLException, IOException {
    	Object o = locateInstance(iuid);
    	if ( o == null || o instanceof String ) return null;
    	File file = (File) o;
    	Dataset dsQ = DcmObjectFactory.getInstance().newDataset();
    	dsQ.putUI(Tags.SOPInstanceUID, iuid);
    	dsQ.putCS(Tags.QueryRetrieveLevel, "IMAGE");
    	RetrieveCmd retrieveCmd = RetrieveCmd.create(dsQ);
    	FileInfo infoList[][] = retrieveCmd.getFileInfos();
    	final FileInfo fileInfo = infoList[0][0];
    	FileDataSource ds = new FileDataSource(log, fileInfo, new byte[512]);
    	ds.setWriteFile(true);//write FileMetaInfo!
    	return ds;
    }
    
  
	/**
     * Delete studies that fullfill freeDiskSpacePolicy to free disk space.
     * <p>
     * Checks available disk space if free disk space is necessary.
     * <p>
     * Remove old files until the stopFreeDiskSpaceWatermark is reached.
     * <p>
     * The real deletion is done in the purge process! This method removes only the reference to the file system.  
     * <p>
     * If PurgeFilesAfterFreeDiskSpace is enabled, the purge process will be called immediately.(if checkFreeDiskSpaceNecessary() is true)
     * 
     * @return The number of released studies.
     */
    public long freeDiskSpace() {
        log.info("Check available Disk Space");
        try {
            if (checkFreeDiskSpaceNecessary()) {
                long maxSizeToDel = (long) ((float) this.minFreeDiskSpace * freeDiskSpaceUpperThreshold)
                    * dirPathList.size() - availableDiskSpace;
                FileSystemMgt fsMgt = newFileSystemMgt();
                try {
                	Map ians = fsMgt.freeDiskSpace(fsPathSet, deleteUncommited, flushOnMedia,
                            flushExternalRetrievable, maxSizeToDel);
                    if ( sendIANs ) sendIANs(ians);
                    if ( autoPurge ) {
                    	if ( log.isDebugEnabled() ) log.debug("call purgeFiles after freeDiskSpace");
                    	this.purgeFiles();
                    }
                    return ians.size();
                } finally {
                    fsMgt.remove();
                }            
            } else if (studyCacheTimeout > 0L) {
                long accessedBefore = System.currentTimeMillis() - studyCacheTimeout;
                FileSystemMgt fsMgt = newFileSystemMgt();
                try {
                	Map ians = fsMgt.releaseStudies(fsPathSet, deleteUncommited, flushOnMedia,
                            flushExternalRetrievable, accessedBefore);
                    if ( sendIANs ) sendIANs(ians);
                    return ians.size();
                } finally {
                    fsMgt.remove();
                }
            } else {
                return 0L;
            }
        } catch (Exception e) {
            log.error("Free Disk Space failed:", e);
            return -1L;
        }
    }
    
	/**
	 * @param ians
	 */
	private void sendIANs(Map ians) {
	        Notification notif = new Notification(EVENT_TYPE, this, super.getNextNotificationSequenceNumber());
	        notif.setUserData(new IANNotificationVO(ians,null,null));
	        super.sendNotification(notif);
	}

	/**
     * Check if a cleaning process is ncessary.
     * <p>
     * <OL>
     * <LI>Calculate the total space that should be available on all file systems. (<code>minAvail = minFreeDiskSpace * cleanWaterMarkFactor * dirPathList.size() </code>)</LI>
     * <LI>Cumulate available space from all file systems to get current available space on all file systems (=currAvail).</LI>
     * </OL>
     * <p>
     * Creates a directory if a defined file system path doesnt exist and makeStorageDirectory is true.
     * <p>
     * This method doesnt check if the defined file systems are on different disk/partitions!
     * 
     * @return True if clean is necessary ( currAvail < minAvail )
     * @throws IOException
     */
    public boolean checkFreeDiskSpaceNecessary() throws IOException {
    	long minAvail = (long) ( (float) this.minFreeDiskSpace * freeDiskSpaceLowerThreshold ) * dirPathList.size();
    	long currAvail = getAvailableDiskSpace();
    	if ( log.isDebugEnabled() ) log.debug( "currAvail:"+currAvail+" < minAvail:"+minAvail);
    	return currAvail < minAvail; 
    }
    
    public long getAvailableDiskSpace() throws IOException {
    	Iterator iter = dirPathList.iterator();
    	FileSystemInfo info;
    	availableDiskSpace = 0L;
    	while ( iter.hasNext() ) {
    		info = initFileSystemInfo( (File) iter.next() );
    		availableDiskSpace += info.getAvailable();
    	}
    	return availableDiskSpace;
    }
    
    public long showStudySize( Integer pk ) throws RemoteException, FinderException {
        FileSystemMgt fsMgt = newFileSystemMgt();
    	return fsMgt.getStudySize(pk);
    }
}