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
package org.dcm4chex.archive.ejb.session;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.common.FileSystemStatus;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocalHome;

/**
 * @ejb.bean name="FileSystemMgt2" type="Stateless" view-type="remote"
 *     jndi-name="ejb/FileSystemMgt2"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb.ejb-ref ejb-name="FileSystem" view-type="local"
 *     ref-name="ejb/FileSystem"
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 13, 2008
 */
public abstract class FileSystemMgt2Bean implements SessionBean {

    private static Logger log = Logger.getLogger(FileSystemMgt2Bean.class);

    private FileSystemLocalHome fileSystemHome;

    public void setSessionContext(SessionContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            this.fileSystemHome = (FileSystemLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/FileSystem");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetSessionContext() {
       fileSystemHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO addFileSystem(FileSystemDTO dto)
            throws CreateException {
        return fileSystemHome.create(dto).toDTO();
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO removeFileSystem(String dirPath)
            throws FinderException, RemoveException {
        FileSystemLocal fs = fileSystemHome.findByDirectoryPath(dirPath);
        FileSystemDTO dto = fs.toDTO();
        removeFileSystem(fs);
        return dto;
    }

    private void removeFileSystem(FileSystemLocal fs)
            throws RemoveException, FinderException {
        FileSystemLocal next = fs.getNextFileSystem();
        if (next != null && fs.isIdentical(next)) {
            next = null;
        }
        Collection c = fs.getPreviousFileSystems();
        FileSystemLocal[] prevs = (FileSystemLocal[])
                c.toArray(new FileSystemLocal[c.size()]);
        for (int i = 0; i < prevs.length; i++) {
            prevs[i].setNextFileSystem(next);
        }
        if (fs.getStatus() == FileSystemStatus.DEF_RW && next != null
                && next.getStatus() == FileSystemStatus.RW) {
            next.setStatus(FileSystemStatus.DEF_RW);
        }
        fs.remove();
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO getFileSystem(long pk) throws FinderException {
        return fileSystemHome.findByPrimaryKey(new Long(pk)).toDTO();
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO getFileSystemOfGroup(String groupID, String path)
            throws FinderException {
        return toDTO(
                fileSystemHome.findByGroupIdAndDirectoryPath(groupID, path));
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO[] getAllFileSystems() throws FinderException {
        return toDTO(fileSystemHome.findAll());
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO[] getFileSystemsOfGroup(String groupId)
            throws FinderException {
        return toDTO(fileSystemHome.findByGroupId(groupId));
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO getDefRWFileSystemsOfGroup(String groupId)
            throws FinderException {
        return toDTO(selectDefRWFileSystemsOfGroup(groupId));
    }

    private FileSystemLocal selectDefRWFileSystemsOfGroup(String groupId)
            throws FinderException {
        Collection c = fileSystemHome.findByGroupIdAndStatus(groupId,
                FileSystemStatus.DEF_RW);
        if (!c.isEmpty()) {
            return (FileSystemLocal) c.iterator().next();
        }
        c = fileSystemHome.findByGroupIdAndStatus(groupId, FileSystemStatus.RW);
        if (c.isEmpty()) {
            return null;
        }
        FileSystemLocal fs = (FileSystemLocal) c.iterator().next();
        log.info("Update status of " + fs.asString() + " to RW+");
        fs.setStatus(FileSystemStatus.DEF_RW);
        return fs;
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO updateFileSystemStatus(long pk, int status)
            throws FinderException {
        return updateFileSystemStatus(
                fileSystemHome.findByPrimaryKey(new Long(pk)), status);
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO updateFileSystemStatus(String groupID, String dirPath,
            int status) throws FinderException {
        return updateFileSystemStatus(
                fileSystemHome.findByGroupIdAndDirectoryPath(groupID, dirPath),
                status);
    }

    private FileSystemDTO updateFileSystemStatus(FileSystemLocal fs, int status)
            throws FinderException {
        if (status != fs.getStatus()) {
            if (status == FileSystemStatus.DEF_RW) {
                // set status of previous default RW file system(s) to RW
                Collection c = fileSystemHome.findByGroupIdAndStatus(
                        fs.getGroupID(), FileSystemStatus.DEF_RW);
                for (Iterator iterator = c.iterator(); iterator.hasNext();) {
                    ((FileSystemLocal) iterator.next())
                            .setStatus(FileSystemStatus.RW);
                }
            }
            fs.setStatus(status);
        }
        return fs.toDTO();
    }

    /**
     * @throws FileSystemsDoesNotBelongToSameGroupException 
     * @ejb.interface-method
     */
    public FileSystemDTO linkFileSystems(String groupID, String dirPath,
            String next) throws FinderException {
        FileSystemLocal prevfs = fileSystemHome
                .findByGroupIdAndDirectoryPath(groupID, dirPath);
        FileSystemLocal nextfs = (next != null && next.length() != 0)
                ? fileSystemHome.findByGroupIdAndDirectoryPath(groupID, next)
                : null;
        prevfs.setNextFileSystem(nextfs);
        return prevfs.toDTO();
    }

    /**
     * @ejb.interface-method
     */
    public FileSystemDTO addAndLinkFileSystem(FileSystemDTO dto)
            throws FinderException, CreateException {
        FileSystemLocal prev = selectDefRWFileSystemsOfGroup(dto.getGroupID());
        if (prev == null) {
            dto.setStatus(FileSystemStatus.DEF_RW);
        }
        FileSystemLocal fs = fileSystemHome.create(dto);
        if (prev != null) {
            FileSystemLocal prev0 = prev;
            FileSystemLocal next;
            while ((next = prev.getNextFileSystem()) != null
                    && !next.isIdentical(prev0)) {
                prev = next;
            }
            prev.setNextFileSystem(fs);
            fs.setNextFileSystem(next);
        }
        return fs.toDTO();
    }

    private static FileSystemDTO toDTO(FileSystemLocal fs) {
        return fs != null ? fs.toDTO() : null;
    }

    private static FileSystemDTO[] toDTO(Collection c) {
        FileSystemDTO[] dto = new FileSystemDTO[c.size()];
        Iterator it = c.iterator();
        for (int i = 0; i < dto.length; i++) {
            dto[i] = ((FileSystemLocal) it.next()).toDTO();
        }
        return dto;
    }
}
