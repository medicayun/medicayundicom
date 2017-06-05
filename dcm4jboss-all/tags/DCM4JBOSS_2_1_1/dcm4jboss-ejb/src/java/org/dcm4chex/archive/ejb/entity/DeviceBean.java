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
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1491 $ $Date: 2005-02-07 08:40:46 +0800 (周一, 07 2月 2005) $
 * @since 27.12.2004
 * 
 * @ejb.bean
 * 	name="Device"
 * 	type="CMP"
 * 	view-type="local"
 * 	primkey-field="pk"
 * 	local-jndi-name="ejb/Device"
 * 
 * @ejb.transaction
 * 	type="Required"
 * 
 * @ejb.persistence
 * 	table-name="device"
 * 
 * @jboss.entity-command
 * 	name="hsqldb-fetch-key"
 */
public abstract class DeviceBean implements EntityBean {
    
    private static final Logger log = Logger.getLogger(DeviceBean.class);

    /**
	 * @ejb.create-method
	 */
    public Integer ejbCreate(String stationName, String aet, String md)
        throws CreateException
    {
        setStationName(stationName);
        setStationAET(aet);
        setModality(md);
        return null;
    }

    public void ejbPostCreate(String stationName, String aet, String md)
    	throws CreateException
    {
    }
    
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
	 * @ejb.persistence column-name="station_name"
	 */
    public abstract String getStationName();
    public abstract void setStationName(String stationName);

    /**
	 * @ejb.interface-method
	 * @ejb.persistence column-name="station_aet"
	 */
    public abstract String getStationAET();
    public abstract void setStationAET(String aet);
    
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="modality"
     */
    public abstract String getModality();
    public abstract void setModality(String md);
    
    /**
     * @ejb.interface-method
     * @ejb.relation name="device-protocol-codes"
     * 	role-name="devices-for-protocol-code"
     *  target-ejb="Code"
     *  target-role-name="protocol-codes-for-device"
     *  target-multiple="yes"
     *
     * @jboss.relation-table table-name="rel_dev_proto"
     *
     * @jboss.relation
     *  fk-column="prcode_fk"
     *  related-pk-field="pk"     
     *
     * @jboss.target-relation
     *  fk-column="device_fk"
     *  related-pk-field="pk"     
     */
    public abstract java.util.Collection getProtocolCodes();
    public abstract void setProtocolCodes(java.util.Collection codes);
}
