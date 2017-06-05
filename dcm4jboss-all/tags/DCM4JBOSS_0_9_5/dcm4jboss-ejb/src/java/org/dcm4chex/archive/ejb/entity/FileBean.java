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

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1214 $ $Date: 2004-11-23 19:12:27 +0800 (周二, 23 11月 2004) $
 * 
 * @ejb.bean
 * 	name="File"
 * 	type="CMP"
 * 	view-type="local"
 * 	primkey-field="pk"
 * 	local-jndi-name="ejb/File"
 * 
 * @ejb.transaction
 * 	type="Required"
 * 
 * @ejb.persistence
 * 	table-name="files"
 * 
 * @jboss.entity-command
 * 	name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 * 	signature="java.util.Collection findAll()"
 * 	query="SELECT OBJECT(a) FROM File AS a"
 *  transaction-type="Supports"
 * 
 * @ejb.finder
 *  signature="java.util.Collection findDereferencedInFileSystem(java.lang.String dirPath)"
 *  query="SELECT OBJECT(a) FROM File AS a WHERE a.instance IS NULL AND a.fileSystem.directoryPath = ?1"
 *  transaction-type="Supports"
 * @jboss.query
 *  signature="java.util.Collection findDereferencedInFileSystem(java.lang.String dirPath)"
 *  strategy="on-find"
 *  eager-load-group="*"
 */
public abstract class FileBean implements EntityBean
{

    private static final Logger log = Logger.getLogger(FileBean.class);

    /**
	 * Auto-generated Primary Key
	 * 
	 * @ejb.interface-method 
	 * @ejb.pk-field
	 * @ejb.persistence
	 * 	column-name="pk"
	 * @jboss.persistence
	 * 	auto-increment="true"
	 *  
	 */
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);

    /**
	 * File Path (relative path to Directory).
	 * 
	 * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="filepath"
	 */
    public abstract String getFilePath();
    public abstract void setFilePath(String path);

    /**
	 * Transfer Syntax UID
	 * 
	 * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="file_tsuid"
	 */
    public abstract String getFileTsuid();
    public abstract void setFileTsuid(String tsuid);

    /**
	 * MD5 checksum as hex string
	 * 
	 * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="file_md5"
	 */
    public abstract String getFileMd5Field();
    public abstract void setFileMd5Field(String md5);

    /**
	 * MD5 checksum in binary format
	 * 
	 * @ejb.interface-method
	 */
    public byte[] getFileMd5()
    {
        char[] md5Hex = getFileMd5Field().toCharArray();
        byte[] md5 = new byte[16];
        for (int i = 0; i < md5.length; i++)
        {
            md5[i] =
                (byte) ((Character.digit(md5Hex[i << 1], 16) << 4)
                    + Character.digit(md5Hex[(i << 1) + 1], 16));
        }
        return md5;
    }

    public void setFileMd5(byte[] md5)
    {
        if (md5.length != 16)
        {
            throw new IllegalArgumentException("md5.length=" + md5.length);
        }
        char[] md5Hex = new char[32];
        for (int i = 0; i < md5.length; i++)
        {
            md5Hex[i << 1] = Character.forDigit((md5[i] >> 4) & 0xf, 16);
            md5Hex[(i << 1) + 1] = Character.forDigit(md5[i] & 0xf, 16);
        }
        setFileMd5Field(new String(md5Hex));
    }

    /**
	 * File Size
	 * 
	 * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="file_size"
	 */
    public abstract int getFileSize();
    public abstract void setFileSize(int size);

    /**
	 * @ejb.relation
	 * 	name="instance-files"
	 * 	role-name="files-of-instance"
	 * 
	 * @jboss:relation
	 * 	fk-column="instance_fk"
	 * 	related-pk-field="pk"
	 * 
	 * @ejb.interface-method
	 * 	view-type="local"
	 */
    public abstract void setInstance(InstanceLocal inst);

    /**
	 * @ejb.interface-method
	 * 	view-type="local"
	 */
    public abstract InstanceLocal getInstance();

    /**
	 * @ejb.relation
	 * 	name="filesystem-files"
	 * 	role-name="files-of-filesystem"
     *  target-role-name="filesystem-of-file"
     *  target-ejb="FileSystem"
     *  target-multiple="yes"
	 * 
	 * @jboss:relation
	 * 	fk-column="filesystem_fk"
	 * 	related-pk-field="pk"
	 * 
	 * @ejb.interface-method
	 * 	view-type="local"
	 */
    public abstract void setFileSystem(FileSystemLocal fs);

    /**
	 * @ejb.interface-method
	 * 	view-type="local"
	 */
    public abstract FileSystemLocal getFileSystem();
    
    /**
     * @ejb.interface-method
     * @jboss.method-attributes
     *   read-only="true"
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
	 * 
	 * @ejb.interface-method
	 */
    public String asString()
    {
        return prompt();
    }

    private String prompt()
    {
        return "File[pk="
            + getPk()
            + ", filepath="
            + getFilePath()
            + ", tsuid="
            + getFileTsuid()
            + ", filesystem->"
            + getFileSystem()
            + ", inst->"
            + getInstance()
            + "]";
    }

    /**
	 * Create file.
	 * 
	 * @ejb.create-method
	 */
    public Integer ejbCreate(
        String path,
        String tsuid,
        int size,
        byte[] md5,
        InstanceLocal instance,
        FileSystemLocal filesystem)
        throws CreateException
    {
        setFilePath(path);
        setFileTsuid(tsuid);
        setFileSize(size);
        setFileMd5(md5);
        return null;
    }

    public void ejbPostCreate(
        String path,
        String tsuid,
        int size,
        byte[] md5,
        InstanceLocal instance,
        FileSystemLocal filesystem)
        throws CreateException
    {
        setInstance(instance);
        instance.updateAvailability(0);
        setFileSystem(filesystem);
        filesystem.setDiskUsage(filesystem.getDiskUsage() + size);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException
    {
        final FileSystemLocal fs = getFileSystem();
        fs.setDiskUsage(fs.getDiskUsage() - getFileSize());
        log.info("Deleting " + prompt());
    }
}
