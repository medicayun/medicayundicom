/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;

import javax.ejb.FinderException;
import javax.management.JMException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4che.util.MD5Utils;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 1694 $ $Date: 2005-04-14 23:16:19 +0800 (周四, 14 4月 2005) $
 * @since 35.03.2005
 *
 */
public class MD5CheckService extends TimerSupport {

    private static final int BUF_SIZE = 512;
	
    private long taskInterval = 0L;
	private long maxCheckedBefore;

    private int disabledStartHour;
    private int disabledEndHour;
    private int limitNumberOfFilesPerTask;

    private Integer listenerID;

    private ObjectName fileSystemMgtName;
    
    
    private static final Logger log = Logger.getLogger(MD5CheckService.class);
    private static final Logger md5log = Logger.getLogger(MD5CheckService.class.getName()+".corrupted");

    private final NotificationListener timerListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (isDisabled(hour)) {
                if (log.isDebugEnabled())
                    log.debug("MD5Check ignored in time between "
                            + disabledStartHour + " and " + disabledEndHour
                            + " !");
            } else {
                try {
                	check();
                } catch (Exception e) {
                    log.error("MD5 check failed!", e);
                }
            }
        }
    };

    public final String getTaskInterval() {
        String s = RetryIntervalls.formatIntervalZeroAsNever(taskInterval);
        return (disabledEndHour == -1) ? s : s + "!" + disabledStartHour + "-"
                + disabledEndHour;
    }

    public void setTaskInterval(String interval) {
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
            stopScheduler("CheckMD5", listenerID, timerListener);
            listenerID = startScheduler("CheckMD5", taskInterval,
            		timerListener);
        }
    }


    public int getLimitNumberOfFilesPerTask() {
        return limitNumberOfFilesPerTask;
    }

    public void setLimitNumberOfFilesPerTask(int limit) {
        this.limitNumberOfFilesPerTask = limit;
    }

    
    /**
	 * Getter for maxCheckedBefore. 
	 * <p>
	 * This value is used to limit check not recently checked files only.
     * 
     * @return ##w (in weeks), ##d (in days), ##h (in hours).
     */
    public String getMaxCheckedBefore() {
        return RetryIntervalls.formatInterval(maxCheckedBefore);
    }
    /**
	 * Setter for maxCheckedBefore. 
	 * <p>
	 * This value is used to check not recently checked files only.
     *  
     * @param maxCheckedBefore The maxCheckedBefore to set.
     */
    public void setMaxCheckedBefore(String maxCheckedBefore) {
        this.maxCheckedBefore = RetryIntervalls.parseInterval(maxCheckedBefore);
    }
    
    public String check() throws FinderException, IOException, NoSuchAlgorithmException {
    	if ( log.isDebugEnabled() ) log.debug("MD5 check started!");
    	int corrupted = 0;
    	int total = 0;
    	long l = System.currentTimeMillis();
        Timestamp before = new Timestamp( System.currentTimeMillis() - this.maxCheckedBefore );
        FileDTO[] files;
        int limit = limitNumberOfFilesPerTask;
        FileSystemMgt fsMgt = newFileSystemMgt();
        FileSystemDTO[] fsdirs =fsMgt.getAllFileSystems();
        for (int j = 0; j < fsdirs.length; j++) {
            files = fsMgt.findFilesForMD5Check(fsdirs[j].getDirectoryPath(), before, limit);
        	if ( log.isDebugEnabled() ) log.debug("Check MD5 for " + files.length + " files on filesystem " + fsdirs[j]);
            if (files.length > 0) {
                total += files.length;
                for (int k = 0; k < files.length; k++) {
                    if ( ! doCheck(fsMgt, files[k]) ) 
                    	corrupted++;
                }
                limit -= files.length;
            }
        }
        if ( corrupted > 0 ) 
        	log.warn( corrupted + " files are corrupted!");
    	return corrupted + " of "+ total + " files corrupted!";
    }
    
    /**
	 * @param fsMgt
	 * @param fileDTO
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws FinderException
	 */
	private boolean doCheck(FileSystemMgt fsMgt, FileDTO fileDTO) throws IOException, NoSuchAlgorithmException, FinderException {
		if ( log.isDebugEnabled() ) log.debug("check md5 for file "+fileDTO );
        MessageDigest digest = MessageDigest.getInstance("MD5");
        char[] storedMD5 = MD5Utils.toHexChars(fileDTO.getFileMd5());
        final char[] fileMD5 = new char[32];
        File file = FileUtils.toFile(fileDTO.getDirectoryPath(), fileDTO
                .getFilePath());
        
		MD5Utils.md5sum(file, fileMD5, digest, new byte[ BUF_SIZE ]);
        fsMgt.updateTimeOfLastMd5Check( fileDTO.getPk() );
        if (!Arrays.equals(fileMD5, storedMD5 ) ) {
        	md5log.warn("File (pk="+fileDTO.getPk()+") " + file + " corrupted! MD5 of file:"+new String(fileMD5)+" should be "+new String(storedMD5) );
            return false;
        }
        return true;
	}

	private boolean isDisabled(int hour) {
        if (disabledEndHour == -1) return false;
        boolean sameday = disabledStartHour <= disabledEndHour;
        boolean inside = hour >= disabledStartHour && hour < disabledEndHour; 
        return sameday ? inside : !inside;
    }

    protected void startService() throws Exception {
        super.startService();
        listenerID = startScheduler("CheckMD5", taskInterval, timerListener);
    }

    protected void stopService() throws Exception {
        stopScheduler("CheckMD5", listenerID, timerListener);
        super.stopService();
    }
    
    private String[] getFileSystemDirPaths() {
        try {
            return (String[]) server.invoke(fileSystemMgtName,
                    "getFileSystemDirPaths", null, null);
        } catch (JMException e) {
            throw new RuntimeException(
                    "Failed to invoke getFileSystemDirPaths", e);
        }
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

}