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
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.tar.TarEntry;
import org.apache.commons.compress.tar.TarInputStream;
import org.dcm4che.util.Executer;
import org.dcm4che.util.MD5Utils;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.util.CacheJournal;
import org.dcm4chex.archive.util.FileSystemUtils;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 14025 $ $Date: 2010-09-24 19:19:03 +0800 (周五, 24 9月 2010) $
 * @since Mar 13, 2006
 */
public class TarRetrieverService extends ServiceMBeanSupport {

    private static final String NONE = "NONE";
    private static final String DST_PARAM = "%p";
    private static final String FS_PARAM = "%d";
    private static final String FILE_PARAM = "%f";

    private static final Set<String> extracting =
            Collections.synchronizedSet(new HashSet<String>());

    private String dataRootDir;

    private String journalRootDir;

    private CacheJournal journal = new CacheJournal();

    private long minFreeDiskSpace;

    private long prefFreeDiskSpace;

    private String[] tarFetchCmd = null; 
    
    private File tarIncomingDir;
    
    private File absTarIncomingDir;
    
    private int bufferSize = 8192;

    private boolean checkMD5 = true;

    public String getCacheRoot() {
        return dataRootDir;
    }

    public void setCacheRoot(String dataRootDir) {
        journal.setDataRootDir(FileUtils.resolve(new File(dataRootDir)));
        this.dataRootDir = dataRootDir;
    }

    public String getCacheJournalRootDir() {
        return journalRootDir;
    }

    public void setCacheJournalRootDir(String journalRootDir) {
        journal.setJournalRootDir(FileUtils.resolve(new File(journalRootDir)));
        this.journalRootDir = journalRootDir;
    }

    public String getCacheJournalFilePathFormat() {
        return journal.getJournalFilePathFormat();
    }

    public void setCacheJournalFilePathFormat(String journalFilePathFormat) {
        if (getState() == STARTED) {
            if (journalFilePathFormat.equals(
                    getCacheJournalFilePathFormat())) {
                return;
            }
            if (!journal.isEmpty()) {
                throw new IllegalStateException("cache not empty!");
            }
        }
        journal.setJournalFilePathFormat(journalFilePathFormat);
    }

    public boolean isCheckMD5() {
        return checkMD5;
    }

    public void setCheckMD5(boolean checkMD5) {
        this.checkMD5 = checkMD5;
    }

    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public final String getMinFreeDiskSpace() {
        return FileUtils.formatSize(minFreeDiskSpace);
    }

    public final void setMinFreeDiskSpace(String s) {
        this.minFreeDiskSpace = FileUtils.parseSize(s, 0);
    }

    public final String getPreferredFreeDiskSpace() {
        return FileUtils.formatSize(prefFreeDiskSpace);
    }

    public final void setPreferredFreeDiskSpace(String s) {
        this.prefFreeDiskSpace = FileUtils.parseSize(s, 0);
    }

    public String getFreeDiskSpace() throws IOException {
        File dir = journal.getDataRootDir();
        return dir == null || !dir.exists() ? "N/A"
                : FileUtils.formatSize(
                        FileSystemUtils.freeSpace(dir.getPath()));
    }

    public final String getTarFetchCommand() {
        if (tarFetchCmd == null) {
            return NONE;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tarFetchCmd.length; i++) {
            sb.append(tarFetchCmd[i]);
        }
        return sb.toString();
    }

    public final void setTarFetchCommand(String cmd) {
        if (NONE.equalsIgnoreCase(cmd)) {
            this.tarFetchCmd = null;
            return;
        }
        String[] a = StringUtils.split(cmd, '%');
        try {
            String[] b = new String[a.length + a.length - 1];
            b[0] = a[0];
            for (int i = 1; i < a.length; i++) {
                String s = a[i];
                b[2 * i - 1] = ("%" + s.charAt(0)).intern();
                b[2 * i] = s.substring(1);
            }
            this.tarFetchCmd = b;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(cmd);
        }
    }

    private String makeCommand(String fsParam, String fileParam,
            String dstParam) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tarFetchCmd.length; i++) {
            sb.append(tarFetchCmd[i] == FS_PARAM ? fsParam
                    : tarFetchCmd[i] == FILE_PARAM ? fileParam
                    : tarFetchCmd[i] == DST_PARAM ? dstParam
                    : tarFetchCmd[i]);
        }
        return sb.toString();
    }
    
    public final String getTarIncomingDir() {
        return tarIncomingDir.getPath();
    }

    public final void setTarIncomingDir(String tarIncomingDir) {
        this.tarIncomingDir = new File(tarIncomingDir);
        this.absTarIncomingDir = FileUtils.resolve(this.tarIncomingDir);
    }
        
    public File retrieveFileFromTAR(String fsID, String fileID)
            throws IOException, VerifyTarException {
        if (!fsID.startsWith("tar:")) {
            throw new IllegalArgumentException(
                    "Not a tar file system: " + fsID);
        }
        int tarEnd = fileID.indexOf('!');
        if (tarEnd == -1) {
            throw new IllegalArgumentException("Missing ! in " + fileID);
        }
        String tarPath = fileID.substring(0, tarEnd);
        File cacheDir = new File(journal.getDataRootDir(),
                tarPath.replace('/', File.separatorChar));
        String tarName = cacheDir.getName();
        String fpath = fileID.substring(tarEnd + 1)
                            .replace('/', File.separatorChar);
        File f = new File(cacheDir, fpath);
        if (f.exists()) {
            journal.record(cacheDir, true);
            return f;
        }
        boolean extracted = false;
        while (!f.exists()) {
            if (extracting.add(tarName)) {
                try {
                    fetchAndExtractTar(fsID, tarPath, tarName, cacheDir);
                    extracted = true;
                } finally {
                    synchronized (extracting) {
                        extracting.remove(tarName);
                        extracting.notifyAll();
                    }
                }
            } else {
                if (log.isDebugEnabled())
                    log.debug("Wait for concurrent fetch and extract of tar: "
                            + tarName);
                synchronized (extracting) {
                    while (extracting.contains(tarName))
                        try {
                            extracting.wait();
                        } catch (InterruptedException e) {
                            log.warn("Wait for concurrent fetch and extract of tar: "
                            + tarName + " interrupted:", e);
                        }
                }
            }
        }
        journal.record(cacheDir, !extracted);
        return f;
    }

    private void fetchAndExtractTar(String fsID, String tarPath,
            String tarName, File cacheDir) throws IOException,
            VerifyTarException {
        if (tarFetchCmd == null) {
            File tarFile = FileUtils.toFile(fsID.substring(4), tarPath);
            extractTar(tarFile, cacheDir);
        } else {
            if (absTarIncomingDir.mkdirs()) {
                log.info("M-WRITE " + absTarIncomingDir);
            }
            File tarFile = new File(absTarIncomingDir, tarName);
            String cmd = makeCommand(fsID, tarPath, tarFile.getPath());
            try {
                log.info("Fetch from HSM: " + cmd);
                Executer ex = new Executer(cmd);
                int exit = -1;
                try {
                    exit = ex.waitFor();
                } catch (InterruptedException e) {
                }
                if (exit != 0) {
                    throw new IOException("Non-zero exit code(" + exit
                            + ") of " + cmd);
                }
                log.info("M-WRITE " + tarFile);
                extractTar(tarFile, cacheDir);
            } finally {
                log.info("M-DELETE " + tarFile);
                tarFile.delete();
            }
        }
    }

    private void extractTar(File tarFile, File cacheDir)
            throws IOException, VerifyTarException {
        int count = 0;
        long totalSize = 0;
        long free = FileSystemUtils.freeSpace(
                journal.getDataRootDir().getPath());
        long fsize = tarFile.length();
        long toDelete = fsize + minFreeDiskSpace - free;
        if (toDelete > 0)
            free += free(toDelete);
        byte[] buf = new byte[bufferSize];
        TarInputStream tar = new TarInputStream(new FileInputStream(tarFile));
        InputStream in = tar;
        try {
            TarEntry entry = skipDirectoryEntries(tar);
            if (entry == null)
                throw new IOException("No entries in " + tarFile);
            String entryName = entry.getName();
            Map<String, byte[]> md5sums = null;
            MessageDigest digest = null;
            if ("MD5SUM".equals(entryName)) {
                if (checkMD5) {
                    try {
                        digest = MessageDigest.getInstance("MD5");
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                    md5sums = new HashMap<String, byte[]>();
                    BufferedReader lineReader = new BufferedReader(
                            new InputStreamReader(tar));
                    String line;
                    while ((line = lineReader.readLine()) != null) {
                        md5sums.put(line.substring(34), 
                                MD5Utils.toBytes(line.substring(0, 32)));
                    }
                }
                entry = skipDirectoryEntries(tar);
            } else if (checkMD5 ) {
                getLog().warn("Missing MD5SUM entry in " + tarFile);
            }
            for (; entry != null; entry = skipDirectoryEntries(tar)) {
                entryName = entry.getName();
                // Retrieve saved MD5 checksum
                byte[] md5sum = null;
                if (md5sums != null && digest != null) {
                    md5sum = md5sums.remove(entryName);
                    if (md5sum == null)
                        throw new VerifyTarException("Unexpected TAR entry: "
                                + entryName + " in " + tarFile);
                    digest.reset();
                    in = new DigestInputStream(tar, digest);
                }

                File f = new File(cacheDir, 
                        entryName.replace('/', File.separatorChar));
                File dir = f.getParentFile();
                if (dir.mkdirs()) {
                    log.info("M-WRITE " + dir);
                }
                log.info("M-WRITE " + f);
                // Write the stream to file
                FileOutputStream out = new FileOutputStream(f);
                boolean cleanup = true;
                try {
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    cleanup = false;
                } finally {
                    try {
                        out.close();
                    } catch (Exception ignore) {
                    }
                    if (cleanup) {
                        log.info("M-DELETE " + f);
                        f.delete();
                    }
                }

                // Verify MD5
                if (md5sums != null && digest != null) {
                    if (!Arrays.equals(digest.digest(), md5sum)) {
                        log.info("M-DELETE " + f);
                        f.delete();
                        throw new VerifyTarException(
                                "Failed MD5 check of TAR entry: " + entryName
                                        + " in " + tarFile);
                    } else
                        log.info("MD5 check is successful for " + entryName
                                + " in " + tarFile);
                }
                free -= f.length();
                count++;
                totalSize += f.length();
            }
        } finally {
            tar.close();
        }
        toDelete = prefFreeDiskSpace - free;
        if (toDelete > 0) {
            freeNonBlocking(toDelete);
        }
    }

    private TarEntry skipDirectoryEntries(TarInputStream tar)
            throws IOException {
        for (TarEntry entry = tar.getNextEntry(); entry != null;
            entry = tar.getNextEntry()) { 
            if (!entry.isDirectory())
                return entry;
        }
        return null;
    }

    private void freeNonBlocking(final long toDelete) {
        new Thread(new Runnable() {

            public void run() {
                try {
                    free(toDelete);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }).start();
    }

    public long free(long size) throws IOException {
        log.info("Start deleting LRU directories of at least " + size
                + " bytes from TAR cache");
        long deleted = journal.free(size);
        log.info("Finished deleting LRU directories with " + deleted
                + " bytes from TAR cache");
        return deleted;
    }


}
