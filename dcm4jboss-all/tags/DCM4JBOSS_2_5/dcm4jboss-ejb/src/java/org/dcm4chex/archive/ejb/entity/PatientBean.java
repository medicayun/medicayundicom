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
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;

/**
 * @ejb.bean name="Patient" type="CMP" view-type="local"
 *           local-jndi-name="ejb/Patient" primkey-field="pk"
 * @ejb.transaction type="Required"
 * @ejb.persistence table-name="patient"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * @jboss.audit-created-time field-name="createdTime"
 * @jboss.audit-updated-time field-name="updatedTime"
 * 
 * @ejb.finder signature="Collection findAll()"
 *             query="SELECT OBJECT(a) FROM Patient AS a"
 *             transaction-type="Supports"
 * @ejb.finder signature="Collection findAll(int offset, int limit)"
 *             query=""
 *             transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findAll(int offset, int limit)"
 *              query="SELECT OBJECT(a) FROM Patient AS a ORDER BY a.pk OFFSET ?1 LIMIT ?2"
 *              
 * @ejb.finder signature="java.util.Collection findByPatientId(java.lang.String pid)"
 *             query="SELECT OBJECT(a) FROM Patient AS a WHERE a.patientId = ?1"
 *             transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findByPatientId(java.lang.String pid)"
 *              strategy="on-find" eager-load-group="*"
 * 
 * @ejb.finder signature="java.util.Collection findByPatientIdWithIssuer(java.lang.String pid, java.lang.String issuer)"
 *             query="SELECT OBJECT(a) FROM Patient AS a WHERE a.patientId = ?1 AND (a.issuerOfPatientId IS NULL OR a.issuerOfPatientId = ?2)"
 *             transaction-type="Supports"
 *
 * @ejb.finder signature="java.util.Collection findByNameAndBirthdate(java.lang.String name, java.sql.Timestamp birthdate)"
 *             query="" transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findByNameAndBirthdate(java.lang.String name, java.sql.Timestamp birthdate)"
 *              query="SELECT OBJECT(a) FROM Patient AS a WHERE a.patientName = ?1 AND a.patientBirthDate = ?2"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class PatientBean implements EntityBean {

    private static final Logger log = Logger.getLogger(PatientBean.class);

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
     * @ejb.interface-method
     * @ejb.persistence column-name="created_time"
     */
    public abstract java.sql.Timestamp getCreatedTime();

    public abstract void setCreatedTime(java.sql.Timestamp time);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="updated_time"
     */
    public abstract java.sql.Timestamp getUpdatedTime();

    public abstract void setUpdatedTime(java.sql.Timestamp time);
	
    /**
     * Patient ID
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_id"
     */
    public abstract String getPatientId();

    public abstract void setPatientId(String pid);

    /**
     * Patient ID Issuer
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_id_issuer"
     */
    public abstract String getIssuerOfPatientId();

    /**
     * @ejb.interface-method
     */
    public abstract void setIssuerOfPatientId(String issuer);

    /**
     * Patient Name
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_name"
     */
    public abstract String getPatientName();

    /**
     * @ejb.interface-method
     */
    public abstract void setPatientName(String name);

    /**
     * Patient Birth Date
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_birthdate"
     */
    public abstract java.sql.Timestamp getPatientBirthDate();

    /**
     * @ejb.interface-method
     */
    public abstract void setPatientBirthDate(java.sql.Timestamp date);

    /**
     * Patient Sex
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_sex"
     */
    public abstract String getPatientSex();

    /**
     * @ejb.interface-method
     *
     */
    public abstract void setPatientSex(String sex);

    /**
     * @ejb.persistence column-name="hidden"
     */
    public abstract boolean getHidden();

    /**
     * @ejb.interface-method
     */
    public boolean getHiddenSafe() {
        try {
            return getHidden();
        } catch (NullPointerException npe) {
            return false;
        }
    }

    /**
     * @ejb.interface-method
     */
    public abstract void setHidden(boolean hidden);

	/**
     * Patient DICOM Attributes
     *
     * @ejb.persistence
     *  column-name="pat_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

    /**
     * @return Patient, with which this Patient was merged.
     *
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="merged-patients"
     *    role-name="dereferenced-patient"
     *    target-role-name="dominant-patient"
     *    target-ejb="Patient"
     *    target-multiple="yes"
     *    cascade-delete="yes"
     *
     * @jboss.relation fk-column="merge_fk" related-pk-field="pk"
     */
    public abstract PatientLocal getMergedWith();

    /**
     * @param mergedWith, Patient, with which this Patient was merged.
     *
     * @ejb.interface-method
     */
    public abstract void setMergedWith(PatientLocal mergedWith);

    /**
     * @ejb.interface-method view-type="local"
     *
     * @param studies all studies of this patient
     */
    public abstract void setStudies(java.util.Collection studies);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="patient-study" role-name="patient-has-studies"
     *    
     * @return all studies of this patient
     */
    public abstract java.util.Collection getStudies();

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract void setMwlItems(java.util.Collection mwlItems);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="patient-mwlitems" role-name="patient-has-mwlitems"
     */
    public abstract java.util.Collection getMwlItems();

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract void setMpps(java.util.Collection mpps);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="patient-mpps" role-name="patient-has-mpps"
     */
    public abstract java.util.Collection getMpps();

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="patient-gpsps" role-name="patient-has-gpsps"
     */
    public abstract java.util.Collection getGsps();

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract void setGsps(java.util.Collection gsps);

    /**
     * Create patient.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds) throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds) throws CreateException {
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }

    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes(boolean supplement) {
        Dataset ds = DatasetUtils.fromByteArray(getEncodedAttributes(),
                DcmDecodeParam.EVR_LE,
                null);
        if (supplement) {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putUL(PrivateTags.PatientPk, getPk().intValue());
        }
        return ds;
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setPatientId(ds.getString(Tags.PatientID));
        setIssuerOfPatientId(ds.getString(Tags.IssuerOfPatientID));
        setPatientName(ds.getString(Tags.PatientName));
        try {
	        setPatientBirthDate(ds.getDate(Tags.PatientBirthDate));
	    } catch (IllegalArgumentException e) {
	        log.warn("Illegal Patient Birth Date format: " + e.getMessage());
	    }
        setPatientSex(ds.getString(Tags.PatientSex));
        Dataset tmp = ds.excludePrivate();
        setEncodedAttributes(DatasetUtils.toByteArray(tmp,
                DcmDecodeParam.EVR_LE));
    }

    /**
     * @ejb.interface-method
     */
    public void setPatientBirthDate(java.util.Date date) {
        setPatientBirthDate(date != null ? new java.sql.Timestamp(date
                .getTime()) : null);
    }

    /**
     * 
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        return "Patient[pk=" + getPk() + ", pid=" + getPatientId() + ", name="
                + getPatientName() + "]";
    }
}