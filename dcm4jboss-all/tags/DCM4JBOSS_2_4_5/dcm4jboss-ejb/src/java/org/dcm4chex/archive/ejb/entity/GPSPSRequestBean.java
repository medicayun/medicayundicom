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
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.GPSPSLocal;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-04-02 22:55:27 +0800 (周六, 02 4月 2005) $
 * @since 01.04.2005
 * 
 * @ejb.bean name="GPSPSRequest" type="CMP" view-type="local"
 *           local-jndi-name="ejb/GPSPSRequest" primkey-field="pk"
 * @ejb.persistence table-name="gpsps_req"
 * @ejb.transaction type="Required"
 * @jboss.entity-command name="hsqldb-fetch-key"
 */

public abstract class GPSPSRequestBean implements EntityBean {

    private static final Logger log = Logger.getLogger(GPSPSRequestBean.class);

    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, GPSPSLocal gpsps)
            throws CreateException {
        setRequestedProcedureId(ds.getString(Tags.RequestedProcedureID));
        setAccessionNumber(ds.getString(Tags.AccessionNumber));
        return null;
    }
    
    public void ejbPostCreate(Dataset ds, GPSPSLocal gpsps)
            throws CreateException {
        setGpsps(gpsps);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
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
     * @ejb.persistence column-name="req_proc_id"
     */
    public abstract String getRequestedProcedureId();

    public abstract void setRequestedProcedureId(String id);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="accession_no"
     */
    public abstract String getAccessionNumber();

    public abstract void setAccessionNumber(String no);

    /**
     * @ejb.relation name="gpsps-request" role-name="request-for-gpsps"
     *               cascade-delete="yes"
     * @jboss.relation fk-column="gpsps_fk" related-pk-field="pk"
     */
    public abstract void setGpsps(GPSPSLocal gpsps);

    /**
     * @ejb.interface-method
     */
    public abstract GPSPSLocal getGpsps();

    private String prompt() {
        return "GPSPSRequest[pk=" + getPk() 
                + ", rpid=" + getRequestedProcedureId()
                + ", accno=" + getAccessionNumber()
                + ", gpsps->" + getGpsps() + "]";
    }
    
}
