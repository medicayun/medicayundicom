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

package org.dcm4chex.archive.ejb.session;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.FileLocalHome;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocalHome;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtSupportLocal;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgtSupportLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocalHome;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
 * @since 12.09.2004
 * 
 * @ejb.bean name="FileSystemMgt" type="Stateless" view-type="remote"
 *           jndi-name="ejb/FileSystemMgt"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb.ejb-ref ejb-name="File" view-type="local" ref-name="ejb/File"
 * @ejb.ejb-ref ejb-name="FileSystem" ref-name="ejb/FileSystem"
 *              view-type="local"
 * @ejb.ejb-ref ejb-name="Study" ref-name="ejb/Study" view-type="local"
 * @ejb.ejb-ref ejb-name="StudyOnFileSystem" ref-name="ejb/StudyOnFileSystem"
 *              view-type="local"
 * @ejb.ejb-ref ejb-name="FileSystemMgtSupport" ref-name="ejb/FileSystemMgtSupport"
 *              view-type="local"
 */
public abstract class FileSystemMgtBean implements SessionBean {

	private static Logger log = Logger.getLogger(FileSystemMgtBean.class);

	private StudyLocalHome studyHome;

	private StudyOnFileSystemLocalHome sofHome;

	private FileLocalHome fileHome; 

	private FileSystemLocalHome fileSystemHome;

    private FileSystemMgtSupportLocalHome fileSystemMgtSupportHome;
    
	public void setSessionContext(SessionContext ctx) {
		Context jndiCtx = null;
		try {
			jndiCtx = new InitialContext();
			this.studyHome = (StudyLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/Study");
			this.sofHome = (StudyOnFileSystemLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/StudyOnFileSystem");
			this.fileHome = (FileLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/File");
			this.fileSystemHome = (FileSystemLocalHome) jndiCtx
					.lookup("java:comp/env/ejb/FileSystem");
            this.fileSystemMgtSupportHome = (FileSystemMgtSupportLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/FileSystemMgtSupport");
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
		studyHome = null;
		sofHome = null;
		fileHome = null;
		fileSystemHome = null;
        fileSystemMgtSupportHome = null;
	}

	/**
	 * @ejb.interface-method
	 */
	public void deleteFile(int file_pk) throws RemoteException, EJBException,
			RemoveException {
		fileHome.remove(new Integer(file_pk));
	}

	/**
	 * @ejb.interface-method
	 */
	public FileDTO[] getDereferencedFiles(String dirPath, int limit)
			throws FinderException {
		log.debug("Querying for dereferenced files in " + dirPath);
		Collection c = fileHome.findDereferencedInFileSystem(dirPath, limit);
		log.debug("Found " + c.size() + " dereferenced files in " + dirPath);
		return toFileDTOs(c);
	}

	private FileDTO[] toFileDTOs(Collection c) {
		FileDTO[] dto = new FileDTO[c.size()];
		Iterator it = c.iterator();
		for (int i = 0; i < dto.length; ++i) {
			dto[i] = toDTO((FileLocal) it.next());
		}
		return dto;
	}

    /**
     * @throws FinderException 
     * @ejb.interface-method
     */
    public FileDTO[] findFilesToCompress(String dirPath, String cuid,
            Timestamp before, int limit) throws FinderException {
        if (log.isDebugEnabled())
            log.debug("Querying for files to compress in " + dirPath);
        Collection c = fileHome.findFilesToCompress(dirPath, cuid, before, limit);
        if (log.isDebugEnabled())
            log.debug("Found " + c.size()+ " files to compress in " + dirPath);
        return toFileDTOs(c);
        
    }

    /**
     * @throws FinderException 
     * @ejb.interface-method
     */
    public FileDTO[] findFilesForMD5Check(String dirPath,
            Timestamp before, int limit) throws FinderException {
        if (log.isDebugEnabled())
            log.debug("Querying for files to check md5 in " + dirPath);
        Collection c = fileHome.findToCheckMd5(dirPath, before, limit);
        if (log.isDebugEnabled())
            log.debug("Found " + c.size()+ " files to check md5 in " + dirPath);
        return toFileDTOs(c);
        
    }

    /**
     * @throws FinderException 
     * @ejb.interface-method
     */
    public void updateTimeOfLastMd5Check(int pk) throws FinderException {
    	Timestamp ts = new Timestamp( System.currentTimeMillis() );
        if (log.isDebugEnabled())
            log.debug("update time of last md5 check to " + ts);
        FileLocal fl = fileHome.findByPrimaryKey(new Integer(pk));
        fl.setTimeOfLastMd5Check(ts);
    }
    
	/**
	 * @ejb.interface-method
	 */
	public void replaceFile(int pk, String path, String tsuid, int size,
			byte[] md5) throws FinderException, CreateException {
		FileLocal oldFile = fileHome.findByPrimaryKey(new Integer(pk));
		FileLocal newFile = fileHome.create(path, tsuid, size, md5, 
				oldFile.getInstance(), oldFile.getFileSystem());
		oldFile.setInstance(null);
	}
	
	/**
	 * @ejb.interface-method
	 */
	public void setFileStatus(int pk, int status) throws FinderException {
		fileHome.findByPrimaryKey(new Integer(pk)).setFileStatus(status);
	}

	/**
	 * @ejb.interface-method
	 */
	public FileSystemDTO addFileSystem(FileSystemDTO dto)
			throws CreateException {
		return toDTO(fileSystemHome.create(dto.getDirectoryPath(), dto
				.getRetrieveAET()));
	}

	/**
	 * @throws FinderException
	 * @ejb.interface-method
	 */
	public FileSystemDTO getFileSystem(String dirPath) throws FinderException {
		return toDTO(fileSystemHome.findByDirectoryPath(dirPath));
	}

	/**
	 * @ejb.interface-method
	 */
	public void removeFileSystem(String dirPath) throws FinderException,
			RemoveException {
		fileSystemHome.findByDirectoryPath(dirPath).remove();
	}

	/**
	 * @ejb.interface-method
	 */
	public void removeFileSystem(int pk) throws RemoveException {
		fileSystemHome.remove(new Integer(pk));
	}

	/**
	 * @ejb.interface-method
	 */
	public FileSystemDTO[] getAllFileSystems() throws FinderException {
		Collection c = fileSystemHome.findAll();
		FileSystemDTO[] dto = new FileSystemDTO[c.size()];
		Iterator it = c.iterator();
		for (int i = 0; i < dto.length; i++) {
			dto[i] = toDTO((FileSystemLocal) it.next());
		}
		return dto;
	}

	/**
	 * @ejb.interface-method
	 */
	public void touchStudyOnFileSystem(String siud, String dirPath)
			throws FinderException, CreateException {
		try {
			sofHome.findByStudyAndFileSystem(siud, dirPath).touch();
		} catch (ObjectNotFoundException e) {
			sofHome.create(studyHome.findByStudyIuid(siud), fileSystemHome
					.findByDirectoryPath(dirPath));
		}
	}
	
	private FileSystemDTO toDTO(FileSystemLocal fs) {
		FileSystemDTO dto = new FileSystemDTO();
		dto.setPk(fs.getPk().intValue());
		dto.setDirectoryPath(fs.getDirectoryPath());
		dto.setRetrieveAET(fs.getRetrieveAET());
		return dto;
	}

	private FileDTO toDTO(FileLocal file) {
		FileSystemLocal fs = file.getFileSystem();
		FileDTO dto = new FileDTO();
		dto.setPk(file.getPk().intValue());
		dto.setRetrieveAET(fs.getRetrieveAET());
		dto.setDirectoryPath(fs.getDirectoryPath());
		dto.setFilePath(file.getFilePath());
		dto.setFileTsuid(file.getFileTsuid());
		dto.setFileMd5(file.getFileMd5());
		dto.setFileSize(file.getFileSize());
		dto.setFileStatus(file.getFileStatus());
		return dto;
	}
	
	/**
	 * @param tsBefore
	 * @ejb.interface-method
	 */
	public Collection getStudiesOnFilesystems( Set dirPaths, Timestamp tsBefore ) throws FinderException {
		return sofHome.listOnFileSystems( dirPaths, tsBefore );
	}

    /** 
     * @ejb.interface-method
     */
    public Map releaseStudies(Set fsPathSet, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, long accessedBefore)
            throws IOException, FinderException, EJBException, RemoveException,
            CreateException {
        Timestamp tsBefore = new Timestamp(accessedBefore);
        log.info("Releasing studies not accessed since " + tsBefore);
        Map ians = new HashMap();
        releaseStudies(fsPathSet, ians, checkUncommited, checkOnMedia,
                checkExternal, Long.MAX_VALUE, new Timestamp(accessedBefore));
        return ians;
    }

    /** 
     * @ejb.interface-method
     */
    public Map freeDiskSpace(Set fsPathSet, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, long maxSizeToDel)
            throws IOException, FinderException, EJBException, RemoveException,
            CreateException {
    	Map ians = new HashMap();
        log.info("Free Disk Space: try to release " + (maxSizeToDel / 1000000.f) + "MB of DiskSpace");
        releaseStudies(fsPathSet, ians, checkUncommited, checkOnMedia,
                checkExternal, maxSizeToDel, null);
        return ians;
   }
    
    /**
     * Release studies that fullfill freeDiskSpacePolicy to free disk space.
     * <p>
     * Remove old files until the <code>maxSizeToDel</code> is reached.<p>
     * The real deletion is done in the purge process! This method removes only the reference to the file system.  
     * <DL>
     * <DD>1) Get a list of studies for all filesystems.</DD>
     * <DD>2) Dereference files of studies that fullfill the freeDiskSpacePolicy.</DD>
     * </DL>
     * 
     * @param fsPathSet 	Set with path of filesystems.
     * @param maxSizeToDel	Size that should be released.
     * @param checkUncommited	Flag of freeDiskSpacePolicy: Check if storage of study was not commited.
     * @param checkOnMedia	Flag of freeDiskSpacePolicy: Check if study is stored on media (offline storage).
     * @param checkExternal	Flag of freeDiskSpacePolicy: Check if study is on an external AET.
     * @param tsBefore date study have to be accessed.
     *  
     * @return Total size of released studies.
     *  
     * @throws IOException
     * @throws FinderException
     * @throws RemoveException
     * @throws EJBException
     * @throws CreateException 
     */
    private long releaseStudies(Set fsPathSet, Map ians, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, long maxSizeToDel,
            Timestamp tsBefore) throws IOException, FinderException,
            EJBException, RemoveException, CreateException {
        Collection c = getStudiesOnFilesystems(fsPathSet, tsBefore);
        if ( log.isDebugEnabled() ) {
        	log.debug("Studies on filesystem(s) accessed before "+tsBefore+" :"+c.size() );
        	log.debug(" checkUncommited: "+checkUncommited);
        	log.debug(" checkOnMedia: "+checkOnMedia);
        	log.debug(" checkExternal: "+checkExternal);
        	log.debug(" maxSizeToDel: "+maxSizeToDel);
        }
        long sizeToDelete = 0L;
        FileSystemMgtSupportLocal spacer = fileSystemMgtSupportHome.create();
        Dataset ianDS;
        try {
            for (Iterator iter = c.iterator(); iter.hasNext()
                    && sizeToDelete < maxSizeToDel;) {
                StudyOnFileSystemLocal studyOnFs = (StudyOnFileSystemLocal) iter
                        .next();
                sizeToDelete += spacer.releaseStudy(studyOnFs, ians, checkUncommited,
                        checkOnMedia, checkExternal);
                
            }
        } finally {
            spacer.remove();
        }
        log.info("Released " + (sizeToDelete / 1000000.f) + "MB of DiskSpace");
        return sizeToDelete;
    }
    
    /** 
     * @throws FinderException
     * @ejb.interface-method
     */
    public long getStudySize(Integer studyPk) throws FinderException {
    	return studyHome.selectStudySize(studyPk);
    }
}