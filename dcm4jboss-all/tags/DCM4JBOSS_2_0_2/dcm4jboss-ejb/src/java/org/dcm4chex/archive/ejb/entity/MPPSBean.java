/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

import java.util.Arrays;

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
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4chex.archive.ejb.interfaces.CodeLocal;
import org.dcm4chex.archive.ejb.interfaces.CodeLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1221 $ $Date: 2004-11-25 04:49:59 +0800 (周四, 25 11月 2004) $
 * @since 21.03.2004
 *
 * @ejb.bean
 *  name="MPPS"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/MPPS"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="mpps"
 * 
 * @jboss.entity-command
 *  name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="java.util.Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Instance AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.MPPSLocal findBySopIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(a) FROM MPPS AS a WHERE a.sopIuid = ?1"
 *  transaction-type="Supports"
 * 
 * @jboss.query
 *  signature="org.dcm4chex.archive.ejb.interfaces.MPPSLocal findBySopIuid(java.lang.String uid)"
 *  strategy="on-find"
 *  eager-load-group="*"
 * 
 * @ejb.ejb-ref ejb-name="Series" view-type="local" ref-name="ejb/Series"
 * @ejb.ejb-ref ejb-name="Code" view-type="local" ref-name="ejb/Code"
 */
public abstract class MPPSBean implements EntityBean {
    private static final Logger log = Logger.getLogger(MPPSBean.class);
    private EntityContext ctx;
    private SeriesLocalHome seriesHome;
    private CodeLocalHome codeHome;
    private static final String[] STATUS =
        { "IN PROGRESS", "COMPLETED", "DISCONTINUED" };
    private static final int IN_PROGRESS = 0;
    private static final int COMPLETED = 0;
    private static final int DISCONTINUED = 0;
    
    public void setEntityContext(EntityContext ctx) {
        this.ctx = ctx;
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            seriesHome =
                (SeriesLocalHome) jndiCtx.lookup("java:comp/env/ejb/Series");
            codeHome = (CodeLocalHome) jndiCtx.lookup("java:comp/env/ejb/Code");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {}
            }
        }
    }

    public void unsetEntityContext() {
        ctx = null;
        seriesHome = null;
        codeHome = null;
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

    public abstract void setPk(Integer pk);

    /**
     * SOP Instance UID
     *
     * @ejb.persistence
     *  column-name="mpps_iuid"
     * 
     * @ejb.interface-method
     *
     */
    public abstract String getSopIuid();

    public abstract void setSopIuid(String iuid);

    /**
     * MPPS Status
     *
     * @ejb.persistence
     *  column-name="mpps_status"
     */
    public abstract int getPpsStatusAsInt();

    public abstract void setPpsStatusAsInt(int status);

    /**
     * MPPS DICOM Attributes
     *
     * @ejb.persistence
     *  column-name="mpps_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

    /**
     * @ejb.interface-method view-type="local"
     * 
     * @ejb.relation
     *  name="patient-mpps"
     *  role-name="mpps-of-patient"
     *  cascade-delete="yes"
     *
     * @jboss:relation
     *  fk-column="patient_fk"
     *  related-pk-field="pk"
     */
    public abstract void setPatient(PatientLocal patient);

    /**
     * @ejb.interface-method view-type="local"
     * 
     * @return patient of this study
     */
    public abstract PatientLocal getPatient();

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract void setSeries(java.util.Collection series);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation
     *  name="mpps-series"
     *  role-name="mpps-has-series"
     */
    public abstract java.util.Collection getSeries();

    /**
     * @ejb.relation name="mpps-drcode"
     *  role-name="mpps-with-drcode"
     *  target-ejb="Code"
     *  target-role-name="drcode-of-mpps"
     *  target-multiple="yes"
     *
     * @jboss:relation fk-column="drcode_fk" related-pk-field="pk"
     */
    public abstract void setDrCode(CodeLocal srCode);

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract CodeLocal getDrCode();

    /**
     * Create Instance.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(String iuid, Dataset ds, PatientLocal patient)
        throws CreateException {
        setSopIuid(iuid);
        return null;
    }

    public void ejbPostCreate(String iuid, Dataset ds, PatientLocal patient)
        throws CreateException {
        setPatient(patient);
        setAttributes(ds);
        try {
            setSeries(seriesHome.findByPpsIuid(iuid));
        } catch (FinderException e) {
            throw new EJBException(e);
        }
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }

    /**
     * @ejb.interface-method
     */
    public boolean isInProgress() {
        return getPpsStatusAsInt() == IN_PROGRESS;
    }

    /**
     * @ejb.interface-method
     */
    public boolean isCompleted() {
        return getPpsStatusAsInt() == COMPLETED;
    }
    
    /**
     * @ejb.interface-method
     */
    public boolean isDiscontinued() {
        return getPpsStatusAsInt() == DISCONTINUED;
    }
    
    /**
     * @ejb.interface-method
     */
    public String getPpsStatus() {
        return STATUS[getPpsStatusAsInt()];
    }

    public void setPpsStatus(String status) {
        int index = Arrays.asList(STATUS).indexOf(status);
        if (index == -1) {
            throw new IllegalArgumentException("status:" + status);
        }
        setPpsStatusAsInt(index);
    }
    
    private String prompt() {
        return "MPPS[pk="
            + getPk()
            + ", iuid="
            + getSopIuid()
            + ", status="
            + getPpsStatus()
            + ", patient->"
            + getPatient()
            + "]";
    }

    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes() {
        return DatasetUtils.fromByteArray(
            getEncodedAttributes(),
            DcmDecodeParam.EVR_LE, null);
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setPpsStatus(ds.getString(Tags.PPSStatus));
        try {
            setDrCode(
                CodeBean.valueOf(
                    codeHome,
                    ds.getItem(Tags.PPSDiscontinuationReasonCodeSeq)));
        } catch (CreateException e) {
            throw new EJBException(e);
        } catch (FinderException e) {
            throw new EJBException(e);
        }
        setEncodedAttributes(
            DatasetUtils.toByteArray(ds, DcmDecodeParam.EVR_LE));
    }

    /**
     * @ejb.interface-method
     */
    public boolean isIncorrectWorklistEntrySelected() {
        CodeLocal drcode = getDrCode();
        return drcode != null
            && "110514".equals(drcode.getCodeValue())
            && "DCM".equals(drcode.getCodingSchemeDesignator());
    }
}
