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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1185 $ $Date: 2004-10-18 15:58:30 +0800 (周一, 18 10月 2004) $
 * @since 12.09.2004
 *
 */
public class FileSystemMgtService extends ServiceMBeanSupport {

    private static final String LOCAL = "local";

    private static final long MEGA = 1000000L;

    private static final long GIGA = 1000 * MEGA;

    private long defHWM = 10 * GIGA;

    private List dirPathList = Arrays.asList(new File[] { new File("archive")});

    private Set fsPathSet = Collections.singleton("archive");

    private String retrieveAETs = "QR_SCP";

    private File curDir = new File("archive");

    private static String null2local(String s) {
        return s == null ? LOCAL : s;
    }

    public final String getDefaultHighWaterMark() {
        return defHWM > GIGA ? "" + (((float) defHWM) / GIGA) + "GB" : ""
                + (((float) defHWM) / MEGA) + "MB";
    }

    public final void setDefaultHighWaterMark(String s) {
        this.defHWM = parseSize(s);
    }

    private static long parseSize(String s) {
        final int len = s.length();
        if (len > 2) {
            final float f = Float.parseFloat(s.substring(0, len - 2));
            if (s.endsWith("GB")) return (long) (f * GIGA);
            if (s.endsWith("MB")) return (long) (f * MEGA);
        }
        throw new IllegalArgumentException(s);
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

    public final String getRetrieveAETs() {
        return retrieveAETs;
    }

    public final void setRetrieveAETs(String aets) {
        this.retrieveAETs = aets;
    }

    public final String getStorageDirectory() {
        return curDir.getPath();
    }

    public final void setStorageDirectory(String str) {
        this.curDir = new File(str);
    }

    public final String getDirectoryPathList() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, n = dirPathList.size(); i < n; i++) {
            sb.append(dirPathList.get(i)).append(File.pathSeparatorChar);
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public final void setDirectoryPathList(String str) {
        StringTokenizer st = new StringTokenizer(str, File.pathSeparator);
        ArrayList list = new ArrayList();
        HashSet set = new HashSet();
        while (st.hasMoreTokens()) {
            String tk = st.nextToken();
            set.add(tk.replace(File.separatorChar, '/'));
            list.add(new File(tk));
        }
        if (list.isEmpty())
                throw new IllegalArgumentException(
                        "DirectoryPathList must NOT be emtpy");
        dirPathList = list;
        fsPathSet = set;
    }

    public final boolean isLocalFileSystem(String fsdir) {
        return fsPathSet.contains(fsdir);
    }

    private static String toFsPath(File dir) {
        return dir.getPath().replace(File.separatorChar, '/');
    }

    private static File resolve(File f) {
        if (f.isAbsolute()) return f;
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath());
    }

    public void purgeFiles() {
        FileSystemMgt fsMgt = newFileSystemMgt();
        try {
            FileDTO[] toDelete;
            for (int i = 0, n = dirPathList.size(); i < n; ++i) {
                try {
                    File f = (File) dirPathList.get(i);
                    toDelete = fsMgt.getDereferencedFiles(toFsPath(f));
                } catch (Exception e) {
                    log.warn("Failed to query dereferenced files:", e);
                    break;
                }
                for (int j = 0; j < toDelete.length; j++) {
                    FileDTO fileDTO = toDelete[j];
                    if (delete(resolve(fileDTO.getFile()))) {
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
        return true;
    }

    public String listFileSystems() throws RemoteException, FinderException {
        StringBuffer sb = new StringBuffer();
        FileSystemDTO[] fss;
        FileSystemMgt fsMgt = newFileSystemMgt();
        try {
            fss = fsMgt.getAllFileSystems();
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }
        for (int i = 0; i < fss.length; i++) {
            fss[i].toString(sb).append("\r\n");
        }
        return sb.toString();
    }

    public String addFileSystem(String dirPath, String retrieveAETs,
            String highWaterMark) throws RemoteException, CreateException,
            FinderException {
        FileSystemMgt fsMgt = newFileSystemMgt();
        try {
            FileSystemDTO fs = new FileSystemDTO();
            fs.setDirectoryPath(dirPath);
            fs.setRetrieveAETs(retrieveAETs);
            fs.setHighWaterMark(parseSize(highWaterMark));
            return fsMgt.addFileSystem(fs).toString();
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }
    }

    public FileSystemDTO updateHighWaterMark(String dirPath, String highWaterMark) throws RemoteException,
            CreateException, FinderException {
        FileSystemMgt fsMgt = newFileSystemMgt();
        try {
            return fsMgt.updateHighWaterMark(dirPath, parseSize(highWaterMark));
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }
    }


    public FileSystemDTO updateRetrieveAETs(String dirPath, String retrieveAETs) throws RemoteException,
            CreateException, FinderException {
        FileSystemMgt fsMgt = newFileSystemMgt();
        try {
            return fsMgt.updateRetrieveAETs(dirPath, retrieveAETs);
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }
    }

    public FileSystemDTO updateDiskUsage(String dirPath)
            throws RemoteException, FinderException {
        FileSystemMgt fsMgt = newFileSystemMgt();
        try {
            return fsMgt.updateDiskUsage(dirPath);
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }
    }
    
    public void removeFileSystem(String dirPath) throws RemoteException,
            FinderException, RemoveException {
        FileSystemMgt fsMgt = newFileSystemMgt();
        try {
            fsMgt.removeFileSystem(dirPath);
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }
    }

    public FileSystemDTO getStorageFileSystem() throws RemoteException {
        FileSystemMgt fsMgt = newFileSystemMgt();
        try {
            FileSystemDTO dto = new FileSystemDTO();
            dto.setDirectoryPath(toFsPath(curDir));
            dto.setRetrieveAETs(retrieveAETs);
            dto.setHighWaterMark(defHWM);
            return fsMgt.probeFileSystem(dto);
        } finally {
            try {
                fsMgt.remove();
            } catch (Exception ignore) {
            }
        }
    }

    public boolean nextStorageDirectory() {
        int index = dirPathList.indexOf(curDir) + 1;
        if (index == dirPathList.size())
            return false;
        curDir = (File) dirPathList.get(index);
        log.info("Switch to next Storage Directory: " + curDir);
        return true;
    }

}