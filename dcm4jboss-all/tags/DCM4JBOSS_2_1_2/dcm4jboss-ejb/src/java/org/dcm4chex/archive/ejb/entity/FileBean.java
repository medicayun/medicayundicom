/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.MD5;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1491 $ $Date: 2005-02-07 08:40:46 +0800 (周一, 07 2月 2005) $
 * 
 * @ejb.bean name="File"
 * 	         type="CMP"
 * 	         view-type="local"
 * 	         primkey-field="pk"
 * 	         local-jndi-name="ejb/File"
 * @ejb.persistence table-name="files"
 * @ejb.transaction type="Required"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * 
 * @ejb.finder signature="java.util.Collection findDereferencedInFileSystem(java.lang.String dirPath)"
 *             query="SELECT OBJECT(f) FROM File AS f WHERE f.instance IS NULL AND f.fileSystem.directoryPath = ?1"
 *             transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findDereferencedInFileSystem(java.lang.String dirPath)"
 *              strategy="on-find"
 *              eager-load-group="*"
 */
public abstract class FileBean implements EntityBean {

    private static final Logger log = Logger.getLogger(FileBean.class);

    /**
     * Auto-generated Primary Key
     * 
     * @ejb.interface-method 
     * @ejb.pk-field
     * @ejb.persistence column-name="pk"
     * @jboss.persistence auto-increment="true"
     */
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);

    /**
     * File Path (relative path to Directory).
     * 
     * @ejb.interface-method
     * @ejb.persistence column-name="filepath"
     */
    public abstract String getFilePath();

    public abstract void setFilePath(String path);

    /**
     * Transfer Syntax UID
     * 
     * @ejb.interface-method
     * @ejb.persistence column-name="file_tsuid"
     */
    public abstract String getFileTsuid();

    public abstract void setFileTsuid(String tsuid);

    /**
     * MD5 checksum as hex string
     * 
     * @ejb.interface-method
     * @ejb.persistence column-name="file_md5"
     */
    public abstract String getFileMd5Field();

    public abstract void setFileMd5Field(String md5);

    /**
     * MD5 checksum in binary format
     * 
     * @ejb.interface-method
     */
    public byte[] getFileMd5() {
        return MD5.toBytes(getFileMd5Field());
    }

    public void setFileMd5(byte[] md5) {
        setFileMd5Field(MD5.toString(md5));
    }

    /**
     * File Size
     * 
     * @ejb.interface-method
     * @ejb.persistence column-name="file_size"
     */
    public abstract int getFileSize();

    public abstract void setFileSize(int size);

    /**
     * @ejb.interface-method
     * @ejb.relation name="instance-files"
     * 	             role-name="files-of-instance"
     * @jboss.relation fk-column="instance_fk"
     * 	               related-pk-field="pk"
     */
    public abstract void setInstance(InstanceLocal inst);

    /**
     * @ejb.interface-method
     */
    public abstract InstanceLocal getInstance();

    /**
     * @ejb.interface-method
     * @ejb.relation name="filesystem-files"
     * 	             role-name="files-of-filesystem"
     *               target-role-name="filesystem-of-file"
     *               target-ejb="FileSystem"
     *               target-multiple="yes"
     * @jboss.relation fk-column="filesystem_fk"
     * 	               related-pk-field="pk"
     */
    public abstract void setFileSystem(FileSystemLocal fs);

    /**
     * @ejb.interface-method
     */
    public abstract FileSystemLocal getFileSystem();

    /**
     * @ejb.interface-method
     */
    public boolean isRedundant() {
        InstanceLocal inst = getInstance();
        return inst == null || inst.getFiles().size() > 1;
    }
    
    /**
     * @ejb.interface-method
     * @jboss.method-attributes read-only="true"
     */
    public FileDTO getFileDTO() {
        FileSystemLocal fs = getFileSystem();
        FileDTO retval = new FileDTO();
        retval.setPk(getPk().intValue());
        retval.setRetrieveAET(fs.getRetrieveAET());
        retval.setDirectoryPath(fs.getDirectoryPath());
        retval.setFilePath(getFilePath());
        retval.setFileTsuid(getFileTsuid());
        retval.setFileSize(getFileSize());
        retval.setFileMd5(getFileMd5());
        return retval;
    }

    /**
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        return "File[pk=" + getPk() + ", filepath=" + getFilePath()
                + ", tsuid=" + getFileTsuid() + ", filesystem->"
                + getFileSystem() + ", inst->" + getInstance() + "]";
    }

    /**
     * Create file.
     * 
     * @ejb.create-method
     */
    public Integer ejbCreate(String path, String tsuid, int size, byte[] md5,
            InstanceLocal instance, FileSystemLocal filesystem)
            throws CreateException {
        setFilePath(path);
        setFileTsuid(tsuid);
        setFileSize(size);
        setFileMd5(md5);
        return null;
    }

    public void ejbPostCreate(String path, String tsuid, int size, byte[] md5,
            InstanceLocal instance, FileSystemLocal filesystem)
            throws CreateException {
        setInstance(instance);
        setFileSystem(filesystem);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }
}
