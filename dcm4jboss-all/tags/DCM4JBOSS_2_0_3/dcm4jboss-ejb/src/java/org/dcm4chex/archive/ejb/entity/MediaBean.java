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

import org.apache.log4j.Logger;

/**
 * @ejb.bean name="Media" type="CMP" view-type="local"
 * 	primkey-field="pk" local-jndi-name="ejb/Media"
 * 
 * @ejb.transaction type="Required"
 * 
 * @ejb.persistence table-name="media"
 * 
 * @jboss.entity-command name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interface.MediaLocal findByFilesetIuid(java.lang.String iuid)"
 *  query="SELECT OBJECT(a) FROM Media AS a WHERE a.filesetIuid = ?1"
 *  transaction-type="Supports"
 *
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1237 $ $Date: 2004-11-30 06:28:36 +0800 (周二, 30 11月 2004) $
 * @since 26.11.2004
 */
public abstract class MediaBean implements EntityBean {

    private static final Logger log = Logger.getLogger(MediaBean.class);

    /**
     * Auto-generated Primary Key
     * 
     * @ejb.interface-method 
     * @ejb.pk-field
     * @ejb.persistence column-name="pk"
     * @jboss.persistence auto-increment="true"
     *  
     */
    public abstract Integer getPk();

    public abstract void setPk(Integer pk);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="fileset_iuid"
     */
    public abstract String getFilesetIuid();

    public abstract void setFilesetIuid(String iuid);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="fileset_id"
     */
    public abstract String getFilesetId();

    public abstract void setFilesetId(String id);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="media_status"
     */
    public abstract int getMediaStatus();

    /**
     * @ejb.interface-method
     */
    public abstract void setMediaStatus(int status);
    
    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(String iuid, String id) throws CreateException {
        setMediaStatus(-1);
        setFilesetIuid(iuid);
        setFilesetId(id);
        return null;
    }

    public void ejbPostCreate(String iuid, String id) throws CreateException {
        log.info("Created " + this);
    }

    public String asString() {
        return "Media[fsiuid=" + getFilesetIuid() + "fsid=" + getFilesetId()
                + "]";
    }
}
