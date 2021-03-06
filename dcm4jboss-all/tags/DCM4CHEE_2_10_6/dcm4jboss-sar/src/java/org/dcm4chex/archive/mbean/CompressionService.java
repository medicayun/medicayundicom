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
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.FinderException;
import javax.management.JMException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.dict.UIDs;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.codec.CodecCmd;
import org.dcm4chex.archive.codec.CompressCmd;
import org.dcm4chex.archive.codec.DecompressCmd;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2829 $ $Date: 2006-10-03 17:52:50 +0800 (周二, 03 10月 2006) $
 * @since 12.09.2004
 *
 */
public class CompressionService extends ServiceMBeanSupport {
    
    private static final String _DCM = ".dcm";
    
    private final SchedulerDelegate scheduler = new SchedulerDelegate(this);

    private long taskInterval = 0L;

    private int disabledStartHour;

    private int disabledEndHour;

    private int limitNumberOfFilesPerTask;

    private boolean verifyCompression;

    private Integer listenerID;

    private ObjectName fileSystemMgtName;

    private File tmpDir = new File("tmp");

    private long keepTempFileIfVerificationFails = 0L;    

    private List compressionRuleList = new ArrayList();
    
    private int bufferSize = 8192;
    
    private static final String[] CODEC_NAMES = new String[] { "JPLL", "JLSL",
            "J2KR" };

    private static final String[] COMPRESS_TRANSFER_SYNTAX = new String[] {
            UIDs.JPEGLossless, UIDs.JPEGLSLossless, UIDs.JPEG2000Lossless };

    private final NotificationListener delayedCompressionListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            checkForTempFilesToDelete();
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (isDisabled(hour)) {
                if (log.isDebugEnabled())
                    log.debug("trigger ignored in time between "
                            + disabledStartHour + " and " + disabledEndHour
                            + " !");
            } else {
                try {
                    checkForFilesToCompress();
                } catch (Exception e) {
                    log.error("Delayed compression failed!", e);
                }
            }
        }
    };

    public final int getBufferSize() {
        return bufferSize ;
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

    public ObjectName getSchedulerServiceName() {
        return scheduler.getSchedulerServiceName();
    }

    public void setSchedulerServiceName(ObjectName schedulerServiceName) {
        scheduler.setSchedulerServiceName(schedulerServiceName);
    }
        
    public final String getKeepTempFileIfVerificationFails() {
        return RetryIntervalls.formatInterval(keepTempFileIfVerificationFails);
    }
    
    public final void setKeepTempFileIfVerificationFails(String interval) {
        this.keepTempFileIfVerificationFails = 
                RetryIntervalls.parseInterval(interval);
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
            scheduler.stopScheduler("CheckFilesToCompress", listenerID, delayedCompressionListener);
            listenerID = scheduler.startScheduler("CheckFilesToCompress", 
            		taskInterval, delayedCompressionListener);
        }
    }

    public final void setCompressionRules(String rules) {
        this.compressionRuleList.clear();
        if (rules == null || rules.trim().length() < 1)
            return;
        StringTokenizer st = new StringTokenizer(rules, ",;\n\r\t ");
        while (st.hasMoreTokens()) {
            compressionRuleList.add(new CompressionRule(st.nextToken()));
        }
    }

    public final String getCompressionRules() {
        StringBuffer sb = new StringBuffer();
        Iterator iter = this.compressionRuleList.iterator();
        while (iter.hasNext()) {
            sb.append(((CompressionRule) iter.next())).append("\r\n");
        }
        return sb.toString();
    }

    public int getLimitNumberOfFilesPerTask() {
        return limitNumberOfFilesPerTask;
    }

    public void setLimitNumberOfFilesPerTask(int limit) {
        this.limitNumberOfFilesPerTask = limit;
    }

    public boolean isVerifyCompression() {
        return verifyCompression;
    }

    public void setVerifyCompression(boolean checkCompression) {
        this.verifyCompression = checkCompression;
    }

    public final int getMaxConcurrentCodec() {
        return CodecCmd.getMaxConcurrentCodec();
    }

    public final void setMaxConcurrentCodec(int maxConcurrentCodec) {
        CodecCmd.setMaxConcurrentCodec(maxConcurrentCodec);
    }

    public final void setTempDir( String dirPath ) {
        tmpDir = new File(dirPath);
    }
    
    public final String getTempDir() {
        return tmpDir.toString();
    }
        
    public void checkForTempFilesToDelete() {
        if (keepTempFileIfVerificationFails <= 0)
            return;
        File absTmpDir = FileUtils.resolve(tmpDir);
        if (!absTmpDir.isDirectory())
            return;
        final long before = System.currentTimeMillis() - keepTempFileIfVerificationFails;
        File[] files = absTmpDir.listFiles(new FileFilter(){
            public boolean accept(File f) {
                return f.getName().endsWith(_DCM) && f.lastModified() < before;
            }});
        for (int i = 0; i < files.length; i++) {
            if (log.isDebugEnabled())
                log.debug("M-DELETE " + files[i]);
            if (!files[i].delete())
                log.warn("Failed to M-DELETE " + files[i]);
        }        
    }
    
    public void checkForFilesToCompress() throws FinderException, IOException {
        log.info("Check For Files To Compress on attached filesystems!");
		long minDiskFree = getMinDiskFreeFromFsmgt();
        String cuid;
        Timestamp before;
        CompressionRule info;
        FileDTO[] files;
        int limit = limitNumberOfFilesPerTask;
        FileSystemDTO[] fsDTOs = listLocalOnlineRWFileSystems();
        FileSystemMgt fsMgt = newFileSystemMgt();
        byte[] buffer = null;
        for (int i = 0, len = compressionRuleList.size(); i < len && limit > 0; i++) {
            info = (CompressionRule) compressionRuleList.get(i);
            cuid = info.getCUID();
            before = new Timestamp(System.currentTimeMillis() - info.getDelay());
            for (int j = 0; j < fsDTOs.length; j++) {
                files = fsMgt.findFilesToCompress(fsDTOs[j].getDirectoryPath(),
                        cuid, before, limit);
                if (files.length > 0) {
                	if (buffer == null)
                		buffer = new byte[bufferSize];
                    log.debug("Compress " + files.length + " files on filesystem "
                            + fsDTOs[j] + " triggered by " + info);
                    for (int k = 0; k < files.length; k++) {
                        doCompress(fsMgt, files[k], info, buffer);
                    }
                    limit -= files.length;
                }
            }
        }
    }

	private FileSystemDTO[] listLocalOnlineRWFileSystems() {
        try {
            return (FileSystemDTO[]) server.invoke(fileSystemMgtName,
                    "listLocalOnlineRWFileSystems", null, null);
        } catch (JMException e) {
            throw new RuntimeException(
                    "Failed to invoke listLocalOnlineRWFileSystems", e);
        }
    }
    
    private long getMinDiskFreeFromFsmgt() {
        try {
            return FileUtils.parseSize((String) server.getAttribute(fileSystemMgtName,"MinimumFreeDiskSpace"),-1);
        } catch (JMException e) { 
            throw new RuntimeException(
                    "Failed to invoke getMinDiskFree", e);
        }
    	
    }

    public boolean compress(FileDTO fileDTO) throws IOException {
        if (!isUncompressed(fileDTO.getFileTsuid())) {
            log.debug( "The image is already compressed, file = " + fileDTO.getFilePath() );
            return false;
        }
        CompressionRule rule = getCompressionRule(fileDTO.getSopClassUID());
        if (rule == null) {
            log.debug( "There is no compression rule for this image SOP Class: " + fileDTO.getSopClassUID() );
            return false;
        }
        doCompress(newFileSystemMgt(), fileDTO, rule, new byte[bufferSize]);
        return true;
    }
    
    private CompressionRule getCompressionRule(String cuid) {
        for (Iterator iter = compressionRuleList.iterator(); iter.hasNext();) {
            CompressionRule rule = (CompressionRule) iter.next();
            if (cuid.equals(rule.getCUID())) {
                return rule;
            }
        }
        return null;
    }

    private boolean isUncompressed(String tsuid) {
        return tsuid.equals(UIDs.ExplicitVRLittleEndian)
                || tsuid.equals(UIDs.ExplicitVRBigEndian)
                || tsuid.equals(UIDs.ImplicitVRLittleEndian);
    }

    private void doCompress(FileSystemMgt fsMgt, FileDTO fileDTO,
            CompressionRule info, byte[] buffer) {
        File baseDir = FileUtils.toFile(fileDTO.getDirectoryPath());
        File srcFile = FileUtils.toFile(fileDTO.getDirectoryPath(), fileDTO
                .getFilePath());
		File destFile = null;
        try {
	        destFile = FileUtils.createNewFile(srcFile.getParentFile(),
					(int) Long.parseLong(srcFile.getName(), 16)+1);
	        if (log.isDebugEnabled())
	            log.debug("Compress file " + srcFile + " to " + destFile
	                    + " with CODEC:" + info.getCodec() + "("
	                    + info.getTransferSyntax() + ")");
            int[] pxvalVR = new int[1];
			byte[] md5 = CompressCmd.compressFile(srcFile, destFile, info
                    .getTransferSyntax(), pxvalVR, buffer);
            if (verifyCompression && fileDTO.getFileMd5() != null) {
                File absTmpDir = FileUtils.resolve(tmpDir);
                if (absTmpDir.mkdirs())
                    log.info("Create directory for decompressed files");
                File decFile = new File(absTmpDir,
                		fileDTO.getFilePath().replace('/', '-') 
                		+ _DCM);
                byte[] dec_md5 = DecompressCmd.decompressFile(destFile,
                        decFile, fileDTO.getFileTsuid(), pxvalVR[0], buffer);
                if (!Arrays.equals(dec_md5, fileDTO.getFileMd5())) {
                    log.info("MD5 sum after compression+decompression of " + srcFile
                            + " differs - compare pixel matrix");
                    if (!FileUtils.equalsPixelData(srcFile, decFile)) {
                        log.warn("Pixel matrix after decompression differs from original file "
                                        + srcFile
                                        + "! Keep original uncompressed file.");
                        destFile.delete();
                        fsMgt.setFileStatus(fileDTO.getPk(),
                        		FileStatus.VERIFY_COMPRESS_FAILED);
                        if (keepTempFileIfVerificationFails <= 0L)
                            decFile.delete();
                        return;
                    }
                }
                decFile.delete();
            }
            final int baseDirPathLength = baseDir.getPath().length();
            final String destFilePath = destFile.getPath().substring(
                    baseDirPathLength + 1).replace(File.separatorChar, '/');
            if (log.isDebugEnabled())
                log.debug("replace File " + srcFile + " with " + destFile);
            fsMgt.replaceFile(fileDTO.getPk(), destFilePath, info
                    .getTransferSyntax(), (int) destFile.length(), md5);
            fileDTO.setPk( 0 );
            fileDTO.setFilePath(destFilePath );
            fileDTO.setFileSize((int)destFile.length());
            fileDTO.setFileMd5(md5);
            srcFile.delete();
        } catch (Exception x) {
            log.error("Can't compress file:" + srcFile, x);
            if (destFile != null && destFile.exists())
                destFile.delete();
            try {
                fsMgt.setFileStatus(fileDTO.getPk(), FileStatus.COMPRESS_FAILED);
            } catch (Exception x1) {
                log.error("Failed to set FAILED_TO_COMPRESS for file "
                        + srcFile);
            }
        }
    }

    private boolean isDisabled(int hour) {
        if (disabledEndHour == -1) return false;
        boolean sameday = disabledStartHour <= disabledEndHour;
        boolean inside = hour >= disabledStartHour && hour < disabledEndHour; 
        return sameday ? inside : !inside;
    }

    protected void startService() throws Exception {
        listenerID = scheduler.startScheduler("CheckFilesToCompress", taskInterval,
        		delayedCompressionListener);
    }

    protected void stopService() throws Exception {
        scheduler.stopScheduler("CheckFilesToCompress", listenerID,
        		delayedCompressionListener);
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

    public class CompressionRule {
        String cuid;
        int codec;
        long delay;
        public CompressionRule(String s) {
            String[] a = StringUtils.split(s, ':');
            if (a.length != 3)
                throw new IllegalArgumentException("Wrong format - " + s);
            cuid = a[0];
            getCUID(); // check cuid
            codec = Arrays.asList(CODEC_NAMES).indexOf(a[1]);
            if (codec == -1)
                throw new IllegalArgumentException("Unrecognized codec - " + a[1]);
            delay = RetryIntervalls.parseInterval(a[2]);
        }

        /**
         * @return
         */
        public String getCodec() {
            return CODEC_NAMES[codec];
        }

        /**
         * @return
         */
        public String getCUID() {
            return Character.isDigit(cuid.charAt(0)) ? cuid : UIDs.forName(cuid);
        }

        /**
         * @return Returns the before.
         */
        public long getDelay() {
            return delay;
        }

        /**
         * @return Returns the codec.
         */
        public String getTransferSyntax() {
            return COMPRESS_TRANSFER_SYNTAX[codec];
        }

        public String toString() {
            return cuid + ":" + getCodec() + ':'
                    + RetryIntervalls.formatInterval(delay);
        }
    }
}