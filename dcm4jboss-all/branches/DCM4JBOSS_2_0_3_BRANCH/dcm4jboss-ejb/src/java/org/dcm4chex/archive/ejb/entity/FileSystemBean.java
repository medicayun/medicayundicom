/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;


/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1237 $ $Date: 2004-11-30 06:28:36 +0800 (周二, 30 11月 2004) $
 * @since 31.08.2004
 *
 * @ejb.bean
 * 	name="FileSystem"
 * 	type="CMP"
 * 	view-type="local"
 * 	primkey-field="pk"
 * 	local-jndi-name="ejb/FileSystem"
 * 
 * @ejb.transaction
 * 	type="Required"
 * 
 * @ejb.persistence
 * 	table-name="filesystem"
 * 
 * @jboss.entity-command
 * 	name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="java.util.Collection findAll()"
 *  query="SELECT OBJECT(a) FROM FileSystem AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.FileSystemLocal findByDirectoryPath(java.lang.String path)"
 *  query="SELECT OBJECT(a) FROM FileSystem AS a WHERE a.directoryPath = ?1"
 *  transaction-type="Supports"
 * 
 * @jboss.query 
 *  signature="org.dcm4chex.archive.ejb.interfaces.FileSystemLocal findByDirectoryPath(java.lang.String path)"
 */
public abstract class FileSystemBean implements EntityBean {

    private EntityContext ctx;

    
    private static final Logger log = Logger.getLogger(FileSystemBean.class);

    public void setEntityContext(EntityContext ctx) throws EJBException,
            RemoteException {
        this.ctx = ctx;
    }

    public void unsetEntityContext() throws EJBException, RemoteException {
        this.ctx = null;    
    }
    
    /**
	 * Create File System.
	 * 
	 * @ejb.create-method
	 */
    public Integer ejbCreate(String dirpath, String aet)
        throws CreateException
    {
		setDirectoryPath(dirpath);      
		setRetrieveAET(aet);      
        return null;
    }

    public void ejbPostCreate(String dirpath, String aets)
        throws CreateException
    {
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException
    {
        log.info("Deleting " + prompt());
    }
    
    private String prompt()
    {
        return "FileSystem[pk="
            + getPk()
            + ", dirpath="
            + getDirectoryPath()
            + ", retrieveAET="
            + getRetrieveAET()
            + "]";
    }
    
    /**
     * Auto-generated Primary Key
     *
     * @ejb.interface-method
     * @ejb.pk-field
     * @ejb.persistence
     *  column-name="pk"
     * @jboss.persistence
     *  auto-increment="true"
     *
     */
    public abstract Integer getPk();

    /**
	 * Directory Path
	 * 
	 * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="dirpath"
	 */
    public abstract String getDirectoryPath();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setDirectoryPath(String dirpath);

    /**
	 * Retrieve AET
	 * 
     * @ejb.interface-method
	 * @ejb.persistence
	 * 	column-name="retrieve_aet"
	 */
    public abstract String getRetrieveAET();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setRetrieveAET(String aet);
}
