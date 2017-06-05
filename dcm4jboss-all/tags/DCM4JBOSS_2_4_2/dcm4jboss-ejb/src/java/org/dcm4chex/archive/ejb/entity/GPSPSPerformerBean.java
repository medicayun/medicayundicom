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
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.CodeLocal;
import org.dcm4chex.archive.ejb.interfaces.CodeLocalHome;
import org.dcm4chex.archive.ejb.interfaces.GPSPSLocal;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-04-02 22:55:27 +0800 (周六, 02 4月 2005) $
 * @since 01.04.2005
 * 
 * @ejb.bean name="GPSPSPerformer" type="CMP" view-type="local"
 *           local-jndi-name="ejb/GPSPSPerformer" primkey-field="pk"
 * @ejb.persistence table-name="gpsps_perf"
 * @ejb.transaction type="Required"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * @ejb.ejb-ref ejb-name="Code" view-type="local" ref-name="ejb/Code"
 */

public abstract class GPSPSPerformerBean implements EntityBean {

    private static final Logger log = Logger.getLogger(GPSPSPerformerBean.class);

    private CodeLocalHome codeHome;

    public void setEntityContext(EntityContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            codeHome = (CodeLocalHome) jndiCtx.lookup("java:comp/env/ejb/Code");
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

    public void unsetEntityContext() {
        codeHome = null;
    }
    
    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, GPSPSLocal gpsps)
            throws CreateException {
        setHumanPerformerName(ds.getString(Tags.HumanPerformerName));
        return null;
    }
    
    public void ejbPostCreate(Dataset ds, GPSPSLocal gpsps)
            throws CreateException {
        try {
            setHumanPerformerCode(CodeBean.valueOf(codeHome, ds
                    .getItem(Tags.HumanPerformerCodeSeq)));
        } catch (CreateException e) {
            throw new CreateException(e.getMessage());
        } catch (FinderException e) {
            throw new CreateException(e.getMessage());
        }
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
     * @ejb.persistence column-name="human_perf_name"
     */
    public abstract String getHumanPerformerName();
    public abstract void setHumanPerformerName(String id);

    /**
     * @ejb.relation name="human-performer-code"
     *               role-name="human-performer-with-code"
     *               target-ejb="Code"
     *               target-role-name="code-of-human-performer"
     *               target-multiple="yes"
     * @jboss.relation fk-column="code_fk" related-pk-field="pk"
     */
    public abstract CodeLocal getHumanPerformerCode();
    public abstract void setHumanPerformerCode(CodeLocal id);

    /**
     * @ejb.interface-method
     * @ejb.relation name="gpsps-human-performer" role-name="human-performer-for-gpsps"
     *               cascade-delete="yes"
     * @jboss.relation fk-column="gpsps_fk" related-pk-field="pk"
     */
    public abstract GPSPSLocal getGpsps();
    public abstract void setGpsps(GPSPSLocal gpsps);

    private String prompt() {
        return "GPSPSHumanPerformer[pk=" + getPk() 
                + ", name=" + getHumanPerformerName()
                + ", code->" + getHumanPerformerCode()
                + ", gpsps->" + getGpsps() + "]";
    }
    
}
