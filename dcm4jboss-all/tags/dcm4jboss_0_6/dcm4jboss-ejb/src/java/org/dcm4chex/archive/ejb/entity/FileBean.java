/*
 * $Id: FileBean.java 1026 2004-03-03 18:22:28Z gunterze $ Copyright (c)
 * 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.entity;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1026 $ $Date: 2004-03-04 02:22:28 +0800 (周四, 04 3月 2004) $
 * 
 * @ejb.bean
 * 	name="File"
 * 	type="CMP"
 * 	view-type="local"
 * 	primkey-field="pk"
 * 	local-jndi-name="ejb/File"
 * 
 * @jboss.container-configuration
 *  name="Standard CMP 2.x EntityBean with cache invalidation"
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
 *  signature="java.util.Collection findDereferenced()"
 *  query="SELECT OBJECT(a) FROM File AS a WHERE a.instance IS NULL"
 *  transaction-type="Supports"
 * @jboss.query
 *  signature="java.util.Collection findDereferenced()"
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
	 * Directory Path
	 * 
	 * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="dirpath"
	 */
    public abstract String getDirectoryPath();

    public abstract void setDirectoryPath(String dirpath);

    /**
	 * Retrieve AETs
	 * 
     * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="retrieve_aets"
	 */
    public abstract String getRetrieveAETs();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setRetrieveAETs(String aets);


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
     * @ejb.interface-method
     * @jboss.method-attributes
     *   read-only=true
     */
    public FileDTO getFileDTO() {
        FileDTO retval = new FileDTO();
        retval.setPk(getPk().intValue());
        retval.setRetrieveAETs(getRetrieveAETs());      
        retval.setDirectoryPath(getDirectoryPath());      
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
            + ", retrieveAETs="
            + getRetrieveAETs()
            + ", dirpath="
            + getDirectoryPath()
            + ", filepath="
            + getFilePath()
            + ", tsuid="
            + getFileTsuid()
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
        String aets,
        String basedir,
        String path,
        String tsuid,
        int size,
        byte[] md5,
        InstanceLocal instance)
        throws CreateException
    {
		setRetrieveAETs(aets);      
		setDirectoryPath(basedir);      
        setFilePath(path);
        setFileTsuid(tsuid);
        setFileSize(size);
        setFileMd5(md5);
        return null;
    }

    public void ejbPostCreate(
        String aets,
        String basedir,
        String path,
        String tsuid,
        int size,
        byte[] md5,
        InstanceLocal instance)
        throws CreateException
    {
        setInstance(instance);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException
    {
        log.info("Deleting " + prompt());
    }
}
