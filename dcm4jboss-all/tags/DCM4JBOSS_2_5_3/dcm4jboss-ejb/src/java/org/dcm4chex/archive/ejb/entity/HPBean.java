/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.ejb.entity;

import java.util.Collection;

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
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.HPLevel;
import org.dcm4chex.archive.ejb.interfaces.CodeLocal;
import org.dcm4chex.archive.ejb.interfaces.CodeLocalHome;
import org.dcm4chex.archive.ejb.interfaces.HPLocal;
import org.dcm4chex.archive.ejb.interfaces.HPDefinitionLocalHome;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1909 $ $Date: 2005-08-18 01:24:52 +0800 (周四, 18 8月 2005) $
 * @since Aug 2, 2005
 * 
 * @ejb.bean name="HP" type="CMP" view-type="local"
 *           local-jndi-name="ejb/HP" primkey-field="pk"
 * @ejb.persistence table-name="hp"
 * @ejb.transaction type="Required"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * 
 * @ejb.ejb-ref ejb-name="Code" view-type="local" ref-name="ejb/Code"
 * @ejb.ejb-ref ejb-name="HPDefinition" view-type="local" ref-name="ejb/HPDefinition"
 * 
 * @ejb.finder signature="org.dcm4chex.archive.ejb.interfaces.HPLocal findBySopIuid(java.lang.String uid)"
 *             query="SELECT OBJECT(o) FROM HP AS o WHERE o.sopIuid = ?1"
 *             transaction-type="Supports"
 */
public abstract class HPBean implements EntityBean {

    private static final Logger log = Logger.getLogger(HPBean.class);
	
    private EntityContext ejbctx;
    private CodeLocalHome codeHome;
    private HPDefinitionLocalHome defHome;

    public void setEntityContext(EntityContext ctx) {
        ejbctx = ctx;
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            codeHome = (CodeLocalHome)
                    jndiCtx.lookup("java:comp/env/ejb/Code");
			defHome = (HPDefinitionLocalHome) 
                    jndiCtx.lookup("java:comp/env/ejb/HPDefinition");
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
		defHome = null;
        ejbctx = null;
    }
	
    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds) throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds) throws CreateException {
        DcmElement sq;
        Collection c;
        try {
			setHangingProtocolUserIdCode(CodeBean.valueOf(codeHome, ds
                    .getItem(Tags.HangingProtocolUserIdentificationCodeSeq)));
            createHPDefinition(ds.get(Tags.HangingProtocolDefinitionSeq));            
        } catch (CreateException e) {
            throw new CreateException(e.getMessage());
        } catch (FinderException e) {
            throw new CreateException(e.getMessage());
        }
        log.info("Created " + toString());
    }

	private void createHPDefinition(DcmElement sq)
			throws CreateException {
        if (sq == null) return;
        Collection c = getHPDefinition();
		HPLocal hp = (HPLocal) ejbctx.getEJBLocalObject();
        for (int i = 0, n = sq.vm(); i < n; i++) {
            c.add(defHome.create(sq.getItem(i), hp));
        }
	}
	
	public void ejbRemove() throws RemoveException {
        log.info("Deleting " + toString());
    }

    public String toString() {
        return "HP[pk=" + getPk() 
                + ", iuid=" + getSopIuid() + "]";
    }
	
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
     * @ejb.persistence column-name="hp_iuid"
     * @ejb.interface-method
     */
    public abstract String getSopIuid();

    public abstract void setSopIuid(String iuid);
	
    /**
     * @ejb.persistence column-name="hp_cuid"
     * @ejb.interface-method
     */
    public abstract String getSopCuid();

    public abstract void setSopCuid(String cuid);
	
    /**
     * @ejb.persistence column-name="hp_name"
     * @ejb.interface-method
     */
    public abstract String getHangingProtocolName();

    public abstract void setHangingProtocolName(String name);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="hp_level"
     */
    public abstract int getHangingProtocolLevelAsInt();
    public abstract void setHangingProtocolLevelAsInt(int level);

	/**
     * @ejb.interface-method
     */
    public String getHangingProtocolLevel() {
		return HPLevel.toString(getHangingProtocolLevelAsInt());
    }

    public void setHangingProtocolLevel(String level) {
		setHangingProtocolLevelAsInt(HPLevel.toInt(level));
    }

    /**
     * @ejb.persistence column-name="num_priors"
     * @ejb.interface-method
     */
    public abstract int getNumberOfPriorsReferenced();

	public abstract void setNumberOfPriorsReferenced(int priors);

    /**
     * @ejb.persistence column-name="hp_group"
     * @ejb.interface-method
     */
    public abstract String getHangingProtocolUserGroupName();

    public abstract void setHangingProtocolUserGroupName(String name);

    /**
     * @ejb.persistence column-name="num_screens"
     * @ejb.interface-method
     */
    public abstract int getNumberOfScreens();

	public abstract void setNumberOfScreens(int screens);

    /**
     * @ejb.persistence column-name="hp_attrs"
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

	/**
     * @ejb.relation name="hp-useridcode" role-name="hp-for-user"
     *               target-ejb="Code" target-role-name="user-of-hp"
     *               target-multiple="yes"
     * @jboss.relation fk-column="user_fk" related-pk-field="pk"
     */
    public abstract CodeLocal getHangingProtocolUserIdCode();
    public abstract void setHangingProtocolUserIdCode(CodeLocal code);

    /**
     * @ejb.relation name="hp-definition" role-name="hp-with-definition"
     */    
    public abstract java.util.Collection getHPDefinition();
    public abstract void setHPDefinition(java.util.Collection definition);
    
    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes() {
        return DatasetUtils.fromByteArray(
                getEncodedAttributes(), DcmDecodeParam.EVR_LE, null);
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setSopIuid(ds.getString(Tags.SOPInstanceUID));
		setSopCuid(ds.getString(Tags.SOPClassUID));
		setHangingProtocolName(ds.getString(Tags.HangingProtocolName));
		setHangingProtocolLevel(ds.getString(Tags.HangingProtocolLevel));
		setNumberOfPriorsReferenced(ds.getInt(Tags.NumberOfPriorsReferenced, 0));
		setHangingProtocolUserGroupName(ds.getString(Tags.HangingProtocolUserGroupName));
		setNumberOfScreens(ds.getInt(Tags.NumberOfScreens, 0));
        setEncodedAttributes(
                DatasetUtils.toByteArray(ds, DcmDecodeParam.EVR_LE));
    }

}
