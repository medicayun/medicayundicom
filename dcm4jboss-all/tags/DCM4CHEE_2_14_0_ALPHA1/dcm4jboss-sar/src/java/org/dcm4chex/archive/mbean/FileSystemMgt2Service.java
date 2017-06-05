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
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

import javax.ejb.FinderException;

import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.FileSystemStatus;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2Home;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileSystemUtils;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 8, 2008
 */
public class FileSystemMgt2Service extends ServiceMBeanSupport {

    private static final String NONE = "NONE";

    private static final String GROUP = "group";

    private static final long MIN_FREE_DISK_SPACE = 20 * FileUtils.MEGA;

    private String defRetrieveAET;

    private int defAvailability;

    private String defUserInfo;

    private String defStorageDir;

    private boolean checkStorageFileSystemStatus = true;

    private boolean makeStorageDirectory = true;

    private String mountFailedCheckFile = "NO_MOUNT";

    private long minFreeDiskSpace = MIN_FREE_DISK_SPACE;

    private long checkFreeDiskSpaceMinInterval;

    private long checkFreeDiskSpaceMaxInterval;

    private long checkFreeDiskSpaceTime;

    private FileSystemDTO storageFileSystem;

    public String getFileSystemGroupID() {
        return serviceName.getKeyProperty(GROUP);
    }

    public String getDefRetrieveAET() {
        return defRetrieveAET;
    }

    public void setDefRetrieveAET(String defRetrieveAET) {
        this.defRetrieveAET = defRetrieveAET;
    }

    public String getDefAvailability() {
        return Availability.toString(defAvailability);
    }

    public void setDefAvailability(String availability) {
        this.defAvailability = Availability.toInt(availability);
    }

    public String getDefUserInfo() {
        return defUserInfo;
    }

    public void setDefUserInfo(String defUserInfo) {
        this.defUserInfo = defUserInfo;
    }

    public String getDefStorageDir() {
        return defStorageDir != null ? defStorageDir : NONE;
    }

    public void setDefStorageDir(String defStorageDir) {
        this.defStorageDir = defStorageDir.equalsIgnoreCase(NONE)
                ? null : defStorageDir;
    }

    public final boolean isCheckStorageFileSystemStatus() {
        return checkStorageFileSystemStatus;
    }

    public final void setCheckStorageFileSystemStatus(
            boolean checkStorageFileSystemStatus) {
        this.checkStorageFileSystemStatus = checkStorageFileSystemStatus;
    }

    public boolean isMakeStorageDirectory() {
        return makeStorageDirectory;
    }

    public void setMakeStorageDirectory(boolean makeStorageDirectory) {
        this.makeStorageDirectory = makeStorageDirectory;
    }

    public final String getMountFailedCheckFile() {
        return mountFailedCheckFile;
    }

    public final void setMountFailedCheckFile(String mountFailedCheckFile) {
        this.mountFailedCheckFile = mountFailedCheckFile;
    }

    public final String getMinFreeDiskSpace() {
        return FileUtils.formatSize(minFreeDiskSpace);
    }

    public final void setMinFreeDiskSpace(String str) {
        this.minFreeDiskSpace = FileUtils.parseSize(str, MIN_FREE_DISK_SPACE);
    }

    public long getFreeDiskSpaceOnCurFS() throws IOException {
        if (storageFileSystem == null)
            return -1L;
        File dir = FileUtils.toFile(storageFileSystem.getDirectoryPath());
        return dir.isDirectory() ? FileSystemUtils.freeSpace(dir.getPath())
                                 : -1L;
    }

    public String getFreeDiskSpaceOnCurFSString() throws IOException {
        return FileUtils.formatSize(getFreeDiskSpaceOnCurFS());
    }

    public long getUsableDiskSpaceOnCurFS() throws IOException {
        long free = getFreeDiskSpaceOnCurFS();
        return free == -1L ? -1L : Math.max(0, free - minFreeDiskSpace);
    }

    public String getUsableDiskSpaceOnCurFSString() throws IOException {
        return FileUtils.formatSize(getFreeDiskSpaceOnCurFS());
    }

    public long getFreeDiskSpace() throws Exception {
        FileSystemDTO[] fsDTOs =
            fileSystemMgt().getFileSystemsOfGroup(getFileSystemGroupID());
        long free = 0L;
        for (FileSystemDTO fsDTO : fsDTOs) {
            int status = fsDTO.getStatus();
            if (status == FileSystemStatus.RW
                    || status == FileSystemStatus.DEF_RW) {
                File dir = FileUtils.toFile(fsDTO.getDirectoryPath());
                if (dir.isDirectory()) {
                    free += FileSystemUtils.freeSpace(dir.getPath());
                }
            }
        }
        return free;
    }

    public String getFreeDiskSpaceString() throws Exception {
        return FileUtils.formatSize(getFreeDiskSpace());
    }

    public long getUsableDiskSpace() throws Exception {
        FileSystemDTO[] fsDTOs =
            fileSystemMgt().getFileSystemsOfGroup(getFileSystemGroupID());
        long free = 0L;
        for (FileSystemDTO fsDTO : fsDTOs) {
            int status = fsDTO.getStatus();
            if (status == FileSystemStatus.RW
                    || status == FileSystemStatus.DEF_RW) {
                File dir = FileUtils.toFile(fsDTO.getDirectoryPath());
                if (dir.isDirectory()) {
                    free += Math.max(0,
                            FileSystemUtils.freeSpace(dir.getPath())
                            - minFreeDiskSpace);
                }
            }
        }
        return free;
    }

    public String getUsableDiskSpaceString() throws Exception {
        return FileUtils.formatSize(getUsableDiskSpace());
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

    
    public String listAllFileSystems() throws Exception {
        FileSystemDTO[] fss = fileSystemMgt().getAllFileSystems();
        sortFileSystems(fss);
        return toString(fss);
    }

    public String listFileSystems() throws Exception {
        FileSystemDTO[] fss = fileSystemMgt()
                .getFileSystemsOfGroup(getFileSystemGroupID());
        sortFileSystems(fss);
        return toString(fss);
    }

    public FileSystemDTO addRWFileSystem(String dirPath) throws Exception {
        return fileSystemMgt().addAndLinkFileSystem(
                mkFileSystemDTO(dirPath, FileSystemStatus.RW));
    }

    private FileSystemDTO mkFileSystemDTO(String dirPath, int status) {
        FileSystemDTO fs = new FileSystemDTO();
        fs.setDirectoryPath(dirPath);
        fs.setGroupID(getFileSystemGroupID());
        fs.setRetrieveAET(defRetrieveAET);
        fs.setAvailability(defAvailability);
        fs.setUserInfo(defUserInfo);
        fs.setStatus(status);
        return fs;
    }

    public FileSystemDTO removeFileSystem(String dirPath) throws Exception {
        return fileSystemMgt().removeFileSystem(dirPath);
    }

    public FileSystemDTO linkFileSystems(String dirPath, String next)
            throws Exception {
        return fileSystemMgt().linkFileSystems(getFileSystemGroupID(), dirPath,
                next);
    }

    public FileSystemDTO updateFileSystemStatus(String dirPath, String status)
            throws Exception {
        return fileSystemMgt().updateFileSystemStatus(getFileSystemGroupID(),
                dirPath, FileSystemStatus.toInt(status));
    }

    public FileSystemDTO selectStorageFileSystem() throws Exception { 
        FileSystemMgt2 fsMgt = fileSystemMgt();
        if (storageFileSystem == null) {
            initStorageFileSystem(fsMgt);
        } else if (checkStorageFileSystemStatus) {
            checkStorageFileSystemStatus(fsMgt);
        }
        if (storageFileSystem == null) {
            log.warn("No writeable storage file system configured in group "
                    + getFileSystemGroupID() + " - storage will fail until "
                    + "a writeable storage file system is configured.");
            return null;
        }
        if (checkFreeDiskSpaceTime < System.currentTimeMillis()) {
            if (!(checkFreeDiskSpace(storageFileSystem)
                    || switchFileSystem(fsMgt, storageFileSystem))) {
                log.error("High Water Mark reached on storage file system "
                        + storageFileSystem + " - no alternative storage file "
                        + "system configured for file system group "
                        + getFileSystemGroupID());
                return null;
            }
        }
        return storageFileSystem;
    }

    private synchronized boolean switchFileSystem(FileSystemMgt2 fsMgt,
            FileSystemDTO fsDTO) throws Exception {
        if (storageFileSystem.getPk() != fsDTO.getPk()) {
            log.info("Storage file system has already been switched from "
                    + fsDTO + " to " + storageFileSystem
                    + " by another thread.");
            return true; 
        }
        FileSystemDTO tmp = storageFileSystem;
        String next;
        while ((next = tmp.getNext()) != null &&
                next != storageFileSystem.getDirectoryPath()) {
            tmp = fsMgt.getFileSystemOfGroup(getFileSystemGroupID(), next);
            if (tmp.getStatus() == FileSystemStatus.RW
                    && checkFreeDiskSpace(tmp)) {
                storageFileSystem = fsMgt.updateFileSystemStatus(
                        tmp.getPk(), FileSystemStatus.DEF_RW);
                log.info("Switch storage file system from " + fsDTO + " to "
                        + storageFileSystem);
                return true;
            }
        }
        return false;
    }

    private boolean checkFreeDiskSpace(FileSystemDTO fsDTO) throws IOException {
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
        checkFreeDiskSpaceTime = System.currentTimeMillis() + Math.min(
                freeSpace * checkFreeDiskSpaceMinInterval / minFreeDiskSpace,
                checkFreeDiskSpaceMaxInterval);
        return true;
    }

    private void checkStorageFileSystemStatus(FileSystemMgt2 fsMgt)
            throws FinderException, RemoteException {
        storageFileSystem = fsMgt.getFileSystem(storageFileSystem.getPk());
        if (storageFileSystem.getStatus() == FileSystemStatus.DEF_RW) {
            return;
        }
        log.info("Status of previous storage file system changed: "
                + storageFileSystem);
        storageFileSystem = fsMgt.getDefRWFileSystemsOfGroup(
                getFileSystemGroupID());
        if (storageFileSystem != null) {
            log.info("New storage file system: " + storageFileSystem);
        }
        checkFreeDiskSpaceTime = 0;
    }

    private void initStorageFileSystem(FileSystemMgt2 fsMgt) throws Exception {
        storageFileSystem = fsMgt.getDefRWFileSystemsOfGroup(
                getFileSystemGroupID());
        if (storageFileSystem == null) {
            if (defStorageDir != null) {
                storageFileSystem = fsMgt.addFileSystem(
                        mkFileSystemDTO(defStorageDir, FileSystemStatus.DEF_RW));
                log.info("No writeable storage file system configured in group "
                        + getFileSystemGroupID() + " - auto configure "
                        + storageFileSystem);
                return;
            }
        }
        checkFreeDiskSpaceTime = 0;
    }

    static FileSystemMgt2 fileSystemMgt() throws Exception {
        FileSystemMgt2Home home = (FileSystemMgt2Home) EJBHomeFactory
                .getFactory().lookup(FileSystemMgt2Home.class,
                        FileSystemMgt2Home.JNDI_NAME);
        return home.create();
    }

    private static String toString(FileSystemDTO[] fss) {
        StringBuilder sb = new StringBuilder();
        for (FileSystemDTO fs : fss) {
            sb.append(fs).append("\r\n");
        }
        String s = sb.toString();
        return s;
    }

    private static void sortFileSystems(FileSystemDTO[] fss) {
        for (int i = 0; i < fss.length; i++) {
            selectRoot(fss, i);
            while (selectNext(fss, i)) {
                i++;
            }
        }
    }

    private static boolean selectRoot(FileSystemDTO[] fss, int index) {
        for (int i = index; i < fss.length; i++) {
            if (!hasPrevious(fss, i)) {
                swap(fss, index, i);
                return true;
            }
        }
        return false;
    }

    private static boolean selectNext(FileSystemDTO[] fss, int index) {
        String next = fss[index].getNext();
        if (next == null) {
            return false;
        }
        for (int i = index+1; i < fss.length; i++) {
            if (next.equals(fss[i].getDirectoryPath())) {
                swap(fss, index+1, i);
                return true;
            }
        }
        return false;
    }

    private static void swap(FileSystemDTO[] fss, int i, int j) {
        if (i == j) return;
        FileSystemDTO tmp = fss[i];
        fss[i] = fss[j];
        fss[j] = tmp;
    }

    private static boolean hasPrevious(FileSystemDTO[] fss, int index) {
        String next = fss[index].getDirectoryPath();
        for (int i = 0; i < fss.length; i++) {
            if (i != index && next.equals(fss[i].getNext())) {
                return true;
            }
        }
        return false;
    }
}
