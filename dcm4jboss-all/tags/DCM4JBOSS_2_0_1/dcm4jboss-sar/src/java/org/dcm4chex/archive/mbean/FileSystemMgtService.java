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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1266 $ $Date: 2004-12-03 01:56:16 +0800 (周五, 03 12月 2004) $
 * @since 12.09.2004
 *
 */
public class FileSystemMgtService extends ServiceMBeanSupport {

    private static final String LOCAL = "local";

    private static final long MEGA = 1000000L;

    private long highWaterMark = 50000000L;

    private List dirPathList = Arrays.asList(new File[] { new File("archive")});

    private Set fsPathSet = Collections.singleton("archive");

    private String retrieveAET = "QR_SCP";

    private int curDirIndex = 0;
    
    private String mountFailedCheckFile = "NO_MOUNT";

    private boolean makeStorageDirectory = true;

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

    public final String getRetrieveAET() {
        return retrieveAET;
    }

    public final void setRetrieveAET(String aet) {
        this.retrieveAET = aet;
    }

    public final int getHighWaterMark() {
        return (int) (highWaterMark / MEGA);
    }

    public final void setHighWaterMark(int highWaterMark) {
        this.highWaterMark = highWaterMark * MEGA;
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

    public final boolean isLocalFileSystem(String fsdir) {
        return fsPathSet.contains(fsdir);
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
        if (info.getAvailable() > highWaterMark)
            return info;
        for (int i = 1, n = dirPathList.size(); i < n; ++i) {
            int dirIndex = (curDirIndex + i) % n;
            File dir = (File) dirPathList.get(dirIndex);
            info = initFileSystemInfo(dir);
            if (info.getAvailable() > highWaterMark) {
                log.info("High Water Mark reached on current Storage Directory "
                        + curDir + " - switch Storage Directory to " + dir);
                curDirIndex = dirIndex;
                return info;
            }
        }
        log.error("High Water Mark reached on Storage Directory " + curDir
                + " - no alternative Storage Directory available");
        return info;
    }

    public void purgeFiles() {
        FileSystemMgt fsMgt = newFileSystemMgt();
        try {
            FileDTO[] toDelete;
            for (int i = 0, n = dirPathList.size(); i < n; ++i) {
                try {
                    File f = (File) dirPathList.get(i);
                    toDelete = fsMgt.getDereferencedFiles(FileUtils.slashify(f));
                } catch (Exception e) {
                    log.warn("Failed to query dereferenced files:", e);
                    break;
                }
                for (int j = 0; j < toDelete.length; j++) {
                    FileDTO fileDTO = toDelete[j];
                    if (delete(FileUtils.toFile(fileDTO.getDirectoryPath(),
                            fileDTO.getFilePath()))) {
                        try {
                            fsMgt.deleteFile(fileDTO.getPk());
                        } catch (Exception e) {
                            log
                                    .warn("Failed to remove entry from list of dereferenced files:",
                                            e);
                        }
                    }
                }
            }
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }

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
}