/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

import java.sql.Timestamp;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.FinderException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.common.Availability;

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
 * @jboss.audit-created-time field-name="createdTime"
 * @jboss.audit-updated-time field-name="updatedTime"
 * 
 * @ejb.finder
 *  signature="java.util.Collection findByStatus(int status)"
 *  query="SELECT OBJECT(m) FROM Media AS m WHERE m.mediaStatus = ?1"
 *  transaction-type="Supports"
 *
 * @jboss.query
 *  signature="java.util.Collection findByStatus(int status)"
 *  strategy="on-find"
 *  eager-load-group="*"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interface.MediaLocal findByFilesetIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(m) FROM Media AS m WHERE m.filesetIuid = ?1"
 *  transaction-type="Supports"
 *
 * @jboss.query
 *  signature="org.dcm4chex.archive.ejb.interface.MediaLocal findByFilesetIuid(java.lang.String uid)"
 *  strategy="on-find"
 *  eager-load-group="*"
 *
 * @jboss.query 
 * 	signature="java.util.Collection ejbSelectGeneric(java.lang.String jbossQl, java.lang.Object[] args)"
 *  dynamic="true"
 *  strategy="on-load"
 *  page-size="20"
 *  eager-load-group="*"
 *
 * @jboss.query 
 * 	signature="int ejbSelectGenericInt(java.lang.String jbossQl, java.lang.Object[] args)"
 *  dynamic="true"
 *
 * @jboss.query signature="int ejbSelectInstanceAvailability(java.lang.Integer pk)"
 * 	            query="SELECT MAX(i.availability) FROM Instance i WHERE i.media.pk = ?1"
 *
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1877 $ $Date: 2005-08-04 17:49:56 +0800 (周四, 04 8月 2005) $
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
     * @ejb.persistence
     *  column-name="created_time"
     */
    public abstract java.sql.Timestamp getCreatedTime();

    public abstract void setCreatedTime(java.sql.Timestamp time);

    /**
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="updated_time"
     */
    public abstract java.sql.Timestamp getUpdatedTime();

    public abstract void setUpdatedTime(java.sql.Timestamp time);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="fileset_id"
     */
    public abstract String getFilesetId();

    /**
     * @ejb.interface-method
     */
    public abstract void setFilesetId(String id);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="fileset_iuid"
     */
    public abstract String getFilesetIuid();

    public abstract void setFilesetIuid(String iuid);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="media_rq_iuid"
     */
    public abstract String getMediaCreationRequestIuid();

    /**
     * @ejb.interface-method
     */
    public abstract void setMediaCreationRequestIuid(String iuid);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="media_usage"
     */
    public abstract long getMediaUsage();

    /**
     * @ejb.interface-method
     */
    public abstract void setMediaUsage(long mediaUsage);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="media_status"
     */
    public abstract int getMediaStatus();

    /**
     * @ejb.interface-method
     */
    public abstract void setMediaStatus(int mediaStatus);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="media_status_info"
     */
    public abstract String getMediaStatusInfo();

    /**
     * @ejb.interface-method
     */
    public abstract void setMediaStatusInfo(String info);

    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public boolean checkInstancesAvailable() throws FinderException {
    	return ejbSelectInstanceAvailability( getPk() ) == Availability.ONLINE;
    }
    
    /**
     * @ejb.relation
     * 	name="instance-media"
     * 	role-name="media-with-instance"
     *    
     * @ejb.interface-method
     */
    public abstract java.util.Collection getInstances();

    public abstract void setInstances(java.util.Collection insts);

    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(String fsIuid) throws CreateException {
        setFilesetIuid(fsIuid);
        return null;
    }

    public void ejbPostCreate(String fsIuid) throws CreateException {
    }

    /**    
     * @ejb.home-method
     */
    public java.util.Collection ejbHomeListByCreatedTime(int[] status,
            Timestamp after, Timestamp before, Integer offset, Integer limit,
            boolean desc) throws FinderException {
        return findBy("m.createdTime", status, after, before, offset, limit,
                desc);
    }

    /**    
     * @ejb.home-method
     */
    public java.util.Collection ejbHomeListByUpdatedTime(int[] status,
            Timestamp after, Timestamp before, Integer offset, Integer limit,
            boolean desc) throws FinderException {
        return findBy("m.updatedTime", status, after, before, offset, limit,
        		desc);
    }

    /**    
     * @ejb.home-method
     */
    public int ejbHomeCountByCreatedTime(int[] status,
            Timestamp after, Timestamp before) throws FinderException {
        return countBy("m.createdTime", status, after, before);
    }

	/**    
     * @ejb.home-method
     */
    public int ejbHomeCountByUpdatedTime(int[] status,
            Timestamp after, Timestamp before) throws FinderException {
        return countBy("m.updatedTime", status, after, before);
    }
    
	private static boolean isNull(int[] status) {
		return status == null || status.length == 0;
	}

    private int appendWhere(StringBuffer jbossQl, Object[] args, String attrName,
            int[] status, Timestamp after, Timestamp before) {
        if (!isNull(status)) {
            jbossQl.append(" WHERE m.mediaStatus IN (").append(status[0]);
            for (int i = 1; i < status.length; i++) {
				jbossQl.append(", ").append(status[i]);
			}            
			jbossQl.append(")");
        }
        int i = 0;
        if (after != null || before != null) {
            jbossQl.append(isNull(status) ? " WHERE " : " AND ");
            jbossQl.append(attrName);
            if (after != null) {
                args[i++] = after;
                if (before == null) {
                    jbossQl.append(" > ?").append(i);
                } else {
                    jbossQl.append(" BETWEEN ?").append(i);
                    args[i++] = before;
                    jbossQl.append(" AND ?").append(i);
                }
            } else if (before != null) {
                args[i++] = before;
                jbossQl.append(" < ?").append(i);
            }
        }
        return i;
    }
    
    private Collection findBy(String attrName, int[] status, Timestamp after,
            Timestamp before, Integer offset, Integer limit,
            boolean desc) throws FinderException {
        // generate JBossQL query
        int argsCount = 0;
        if (after != null)
            ++argsCount;
        if (before != null)
            ++argsCount;
        if (offset != null)
            ++argsCount;
        if (limit != null)
            ++argsCount;
        Object[] args = new Object[argsCount];
        StringBuffer jbossQl = new StringBuffer("SELECT OBJECT(m) FROM Media m");
        int i = appendWhere(jbossQl, args, attrName, status, after, before);
        jbossQl.append(" ORDER BY ");
        jbossQl.append(attrName);
        jbossQl.append(desc ? " DESC" : " ASC");
        if (offset != null) {
            args[i++] = offset;
            jbossQl.append(" OFFSET ?").append(i);
        }
        if (limit != null) {
            args[i++] = limit;
            jbossQl.append(" LIMIT ?").append(i);
        }
        if (log.isDebugEnabled())
            log.debug("Execute JBossQL: " + jbossQl);
        // call dynamic-ql query
        return ejbSelectGeneric(jbossQl.toString(), args);
    }

    private int countBy(String attrName, int[] status, Timestamp after,
            Timestamp before) throws FinderException {
        // generate JBossQL query
        int argsCount = 0;
        if (after != null)
            ++argsCount;
        if (before != null)
            ++argsCount;
        Object[] args = new Object[argsCount];
        StringBuffer jbossQl = new StringBuffer("SELECT COUNT(m) FROM Media m");
        appendWhere(jbossQl, args, attrName, status, after, before);
        // call dynamic-ql query
        return ejbSelectGenericInt(jbossQl.toString(), args);
    }
    
    /**
     * @ejb.select query=""
     *  transaction-type="Supports"
     */ 
    public abstract Collection ejbSelectGeneric(String jbossQl, Object[] args)
    		throws FinderException;

    /**
     * @ejb.select query=""
     *  transaction-type="Supports"
     */ 
    public abstract int ejbSelectGenericInt(String jbossQl, Object[] args)
    		throws FinderException;
    
    /**
     * @ejb.select query=""
     */
    public abstract int ejbSelectInstanceAvailability(Integer pk)
            throws FinderException;
    
}
