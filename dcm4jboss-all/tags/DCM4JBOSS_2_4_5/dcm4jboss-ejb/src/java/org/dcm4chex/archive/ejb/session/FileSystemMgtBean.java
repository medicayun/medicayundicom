/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.session;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
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
 * @version $Revision: 1751 $ $Date: 2005-05-25 21:54:08 +0800 (周三, 25 5月 2005) $
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
    public long releaseStudies(Set fsPathSet, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, long accessedBefore)
            throws IOException, FinderException, EJBException, RemoveException,
            CreateException {
        Timestamp tsBefore = new Timestamp(accessedBefore);
        log.info("Releasing studies not accessed since " + tsBefore);
        return releaseStudies(fsPathSet, checkUncommited, checkOnMedia,
                checkExternal, Long.MAX_VALUE, new Timestamp(accessedBefore));
    }

    /** 
     * @ejb.interface-method
     */
    public long freeDiskSpace(Set fsPathSet, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, long maxSizeToDel)
            throws IOException, FinderException, EJBException, RemoveException,
            CreateException {
        log.info("Releasing " + (maxSizeToDel / 1000000.f) + "MB of DiskSpace");
        return releaseStudies(fsPathSet, checkUncommited, checkOnMedia,
                checkExternal, maxSizeToDel, null);
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
    private long releaseStudies(Set fsPathSet, boolean checkUncommited,
            boolean checkOnMedia, boolean checkExternal, long maxSizeToDel,
            Timestamp tsBefore) throws IOException, FinderException,
            EJBException, RemoveException, CreateException {
        Collection c = getStudiesOnFilesystems(fsPathSet, tsBefore);
        long sizeToDelete = 0L;
        FileSystemMgtSupportLocal spacer = fileSystemMgtSupportHome.create();
        try {
            for (Iterator iter = c.iterator(); iter.hasNext()
                    && sizeToDelete < maxSizeToDel;) {
                StudyOnFileSystemLocal studyOnFs = (StudyOnFileSystemLocal) iter
                        .next();
                sizeToDelete += spacer.releaseStudy(studyOnFs, checkUncommited,
                        checkOnMedia, checkExternal);
            }
        } finally {
            spacer.remove();
        }
        log.info("Released " + (sizeToDelete / 1000000.f) + "MB of DiskSpace");
        return sizeToDelete;
    }
}