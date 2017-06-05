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
import java.rmi.RemoteException;
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
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1623 $ $Date: 2005-03-17 09:44:07 +0800 (周四, 17 3月 2005) $
 * @since 12.09.2004
 *
 */
public class CompressionService extends TimerSupport {

    private long taskInterval = 0L;

    private int disabledStartHour;

    private int disabledEndHour;

    private int limitNumberOfFilesPerTask;

    private boolean verifyCompression;

    private List compressionRuleList = new ArrayList();

    private static final String[] CODEC_NAMES = new String[] { "JPLL", "JLSL",
            "J2KR" };

    private static final String[] COMPRESS_TRANSFER_SYNTAX = new String[] {
            UIDs.JPEGLossless, UIDs.JPEGLSLossless, UIDs.JPEG2000Lossless };

    private final NotificationListener delayedCompressionListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
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

    private Integer listenerID;

    private ObjectName fileSystemMgtName;

    public final ObjectName getFileSystemMgtName() {
        return fileSystemMgtName;
    }

    public final void setFileSystemMgtName(ObjectName fileSystemMgtName) {
        this.fileSystemMgtName = fileSystemMgtName;
    }

    public final String getTaskInterval() {
        String s = RetryIntervalls.formatIntervalZeroAsNever(taskInterval);
        if (disabledEndHour != -1)
            s += "!" + disabledStartHour + "-" + disabledEndHour;
        return s;
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
            stopScheduler(listenerID, delayedCompressionListener);
            listenerID = startScheduler(taskInterval,
                    delayedCompressionListener);
        }
    }

    public final void setCompressionRules(String rules) {
        this.compressionRuleList.clear();
        if (rules == null || rules.trim().length() < 1)
            return;
        StringTokenizer st = new StringTokenizer(rules, ",");
        while (st.hasMoreTokens()) {
            compressionRuleList.add(new CompressionRule(st.nextToken()));
        }
    }

    public final String getCompressionRules() {
        StringBuffer sb = new StringBuffer();
        Iterator iter = this.compressionRuleList.iterator();
        if (iter.hasNext())
            sb.append(((CompressionRule) iter.next()).toString());
        while (iter.hasNext()) {
            sb.append(",").append(((CompressionRule) iter.next()).toString());
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

    /**
     * @throws FinderException
     * @throws RemoteException
     * 
     */
    public void checkForFilesToCompress() throws RemoteException,
            FinderException {
        log.info("Check For Files To Compress on attached filesystems!");
        Timestamp before;
        CompressionRule info;
        FileDTO[] files;
        int limit = limitNumberOfFilesPerTask;
        String[] fsdir = getFileSystemDirPaths();
        FileSystemMgt fsMgt = newFileSystemMgt();
        try {
            for (int j = 0, len = compressionRuleList.size(); j < len && limit > 0; j++) {
                info = (CompressionRule) compressionRuleList.get(j);
                before = new Timestamp(System.currentTimeMillis() - info.getDelay());
                files = fsMgt.findToCompress(info.getAETs(), fsdir, before, limit);
                if (files != null) {
                    log.info("Compress " + files.length + " files for " + info);
                    doCompress(fsMgt, files, info);
                    limit -= files.length;
                } else {
                    log.info("No files to compress for " + info);
                }
            }
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {}
        }
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

     private void doCompress(FileSystemMgt fsMgt, FileDTO[] files, CompressionRule info) {
        if (files.length < 1)
            return;
        File srcFile, destFile;
        String destPath;
        File tmpDir = FileUtils.toFile("tmp", "checks");// tmp directory in ServerHomeDir
        tmpDir.mkdir();
        File tmpFile;
        int[] ia = new int[1];
        byte[] md5;
        for (int i = 0, len = files.length; i < len; i++) {
            srcFile = FileUtils.toFile(files[i].getDirectoryPath(), files[i]
                    .getFilePath());
            destFile = getDestFile(srcFile);
            if (log.isDebugEnabled())
                log.debug("Compress file " + srcFile + " to " + destFile
                        + " with CODEC:" + info.getCodec() + "("
                        + info.getTransferSyntax() + ")");
            try {
                md5 = CompressCmd.compressFile(srcFile, destFile, info
                        .getTransferSyntax(), ia);
                boolean check = true;
                if (verifyCompression) {
                    tmpFile = File.createTempFile("check", null, tmpDir);
                    tmpFile.deleteOnExit();
                    byte[] dec_md5 = DecompressCmd.decompressFile(destFile,
                            tmpFile, files[i].getFileTsuid(), ia[0]);
                    if (!Arrays.equals(dec_md5, files[i].getFileMd5())) {
                        log.warn("File MD5 check failed for src file "
                                + srcFile + "! Check pixel matrix now.");
                        if (!FileUtils.equalsPixelData(srcFile, tmpFile)) {
                            check = false;
                        }
                    }
                    if (tmpFile.exists())
                        tmpFile.delete();
                }
                if (check) {
                    destPath = new File(files[i].getFilePath()).getParent()
                            + File.separatorChar + destFile.getName();
                    if (log.isDebugEnabled())
                        log.debug("replaceFile " + srcFile + " with "
                                + destFile + " ! destPath:" + destPath);
                    fsMgt.replaceFile(files[i].getPk(),
                            destPath, info.getTransferSyntax(),
                            (int) destFile.length(), md5);
                } else {
                    log.error("Pixel matrix of compressed file differs from original ("
                                    + srcFile + ")! compressed file removed!");
                    destFile.delete();
                    fsMgt.setFileStatus(files[i].getPk(),
                            FileDTO.VERIFY_COMPRESS_FAILED);
                }
            } catch (Exception x) {
                log.error("Can't compress file:" + srcFile, x);
                if (destFile.exists())
                    destFile.delete();
                try {
                    fsMgt.setFileStatus(files[i].getPk(),
                            FileDTO.COMPRESS_FAILED);
                } catch (Exception x1) {
                    log.error("Failed to set FAILED_TO_COMPRESS for file "
                            + srcFile);
                }
            }
        }

    }

    /**
     * @param srcFile
     * @return
     */
    private File getDestFile(File src) {
        File path = src.getParentFile();
        long fnAsInt = Long.parseLong(src.getName(), 16);
        File f = new File(path, Long.toHexString(++fnAsInt).toUpperCase());
        while (f.exists()) {
            f = new File(path, Long.toHexString(++fnAsInt).toUpperCase());
        }
        return f;
    }

    /**
     * @param hour
     * @return
     */
    protected boolean isDisabled(int hour) {
        if (disabledEndHour >= disabledStartHour) {
            return hour >= disabledStartHour && hour <= disabledEndHour;
        } else {
            return !(hour > disabledEndHour && hour < disabledStartHour);
        }
    }

    protected void startService() throws Exception {
        super.startService();
        listenerID = startScheduler(taskInterval, delayedCompressionListener);
    }

    protected void stopService() throws Exception {
        stopScheduler(listenerID, delayedCompressionListener);
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
        String[] aets;
        long delay;
        int codec;
        public CompressionRule(String s) {
            int pos = s.indexOf('/');
            int pos1 = s.indexOf(']');
            if (pos == -1 || pos1 < pos || s.charAt(0) != '[') {
                throw new IllegalArgumentException(
                        "Wrong format! Use [<AET>|<AET2>/<delay>]<codec>");
            }
            aets = StringUtils.split(s.substring(1, pos), '|');
            delay = RetryIntervalls.parseInterval(s.substring(pos + 1, pos1));
            codec = Arrays.asList(CODEC_NAMES).indexOf(
                    s.substring(pos1 + 1).trim());
            if (codec == -1)
                throw new IllegalArgumentException("Wrong CODEC name "
                        + s.substring(pos1 + 1).trim()
                        + "! Use JPLL, JLSL or J2KR!");
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
        public String[] getAETs() {
            return aets;
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
            return "[" + StringUtils.toString(aets, '|') + "/"
                    + RetryIntervalls.formatInterval(delay) + "]" + getCodec();
        }
    }
}