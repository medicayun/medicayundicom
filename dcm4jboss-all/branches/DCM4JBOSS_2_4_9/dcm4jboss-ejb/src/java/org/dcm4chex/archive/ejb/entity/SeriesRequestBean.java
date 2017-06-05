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
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-04-02 22:55:27 +0800 (周六, 02 4月 2005) $
 * @since 01.04.2005
 * 
 * @ejb.bean name="SeriesRequest" type="CMP" view-type="local"
 *           local-jndi-name="ejb/SeriesRequest" primkey-field="pk"
 * @ejb.persistence table-name="series_req"
 * @ejb.transaction type="Required"
 * @jboss.entity-command name="hsqldb-fetch-key"
 */

public abstract class SeriesRequestBean implements EntityBean {

    private static final Logger log = Logger.getLogger(SeriesRequestBean.class);

    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, SeriesLocal series)
            throws CreateException {
        setRequestedProcedureId(ds.getString(Tags.RequestedProcedureID));
        setSpsId(ds.getString(Tags.SPSID));
        return null;
    }
    
    public void ejbPostCreate(Dataset ds, SeriesLocal series)
            throws CreateException {
        setSeries(series);
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
     * @ejb.persistence column-name="sps_id"
     */
    public abstract String getSpsId();

    public abstract void setSpsId(String no);

    /**
     * @ejb.relation name="series-request-attributes"
     *               role-name="request-attributes-of-series"
     *               cascade-delete="yes"
     * @jboss.relation fk-column="series_fk" related-pk-field="pk"
     */
    public abstract void setSeries(SeriesLocal series);

    /**
     * @ejb.interface-method
     */
    public abstract SeriesLocal getSeries();

    private String prompt() {
        return "SeriesRequestAttribute[pk=" + getPk() 
                + ", rpid=" + getRequestedProcedureId()
                + ", spsid=" + getSpsId()
                + ", series->" + getSeries() + "]";
    }
    
}
