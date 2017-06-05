/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

import java.util.Iterator;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;

/**
 * @ejb.bean
 *  name="Study"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Study"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="study"
 * 
 * @jboss.entity-command
 *  name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Study AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.StudyLocal findByStudyIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(a) FROM Study AS a WHERE a.studyIuid = ?1"
 *  transaction-type="Supports"
 *
 * @jboss.query
 *  signature="org.dcm4chex.archive.ejb.interfaces.StudyLocal findByStudyIuid(java.lang.String uid)"
 *  strategy="on-find"
 *  eager-load-group="*"
 * 
 * @jboss.query 
 * 	signature="int ejbSelectNumberOfStudyRelatedSeries(java.lang.Integer pk)"
 * 	query="SELECT COUNT(s) FROM Series s WHERE s.hidden = FALSE AND s.study.pk = ?1"
 * 
 * @jboss.query 
 * 	signature="int ejbSelectNumberOfStudyRelatedInstances(java.lang.Integer pk)"
 * 	query="SELECT COUNT(i) FROM Instance i WHERE i.series.hidden = FALSE AND i.series.study.pk = ?1"
 * 
 * @jboss.query 
 * 	signature="int ejbSelectAvailability(java.lang.Integer pk)"
 * 	query="SELECT MAX(i.availability) FROM Instance i WHERE i.series.hidden = FALSE AND i.series.study.pk = ?1"
 * 
 * @jboss.audit-created-time field-name="createdTime"
 * @jboss.audit-updated-time field-name="updatedTime"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class StudyBean implements EntityBean {

    private static final Logger log = Logger.getLogger(StudyBean.class);

    private static final int[] SUPPL_TAGS = { Tags.RetrieveAET,
            Tags.InstanceAvailability, Tags.NumberOfSeriesRelatedInstances};

//    private Set retrieveAETSet;

//    private Set modalitySet;

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
     * Study Instance UID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="study_iuid"
     */
    public abstract String getStudyIuid();

    public abstract void setStudyIuid(String uid);

    /**
     * Study ID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="study_id"
     */
    public abstract String getStudyId();

    public abstract void setStudyId(String uid);

    /**
     * Study Datetime
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="study_datetime"
     */
    public abstract java.sql.Timestamp getStudyDateTime();

    public abstract void setStudyDateTime(java.sql.Timestamp dateTime);

    /**
     * Accession Number
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="accession_no"
     */
    public abstract String getAccessionNumber();

    public abstract void setAccessionNumber(String no);

    /**
     * Referring Physician
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="ref_physician"
     */
    public abstract String getReferringPhysicianName();

    public abstract void setReferringPhysicianName(String physician);

    /**
     * Number Of Study Related Series
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="num_series"
     * 
     */
    public abstract int getNumberOfStudyRelatedSeries();

    public abstract void setNumberOfStudyRelatedSeries(int num);

    /**
     * Number Of Study Related Instances
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="num_instances"
     * 
     */
    public abstract int getNumberOfStudyRelatedInstances();

    public abstract void setNumberOfStudyRelatedInstances(int num);

    /**
     * Study DICOM Attributes
     *
     * @ejb.persistence
     *  column-name="study_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

    /**
     * Retrieve AETs
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="retrieve_aets"
     */
    public abstract String getRetrieveAETs();

    public abstract void setRetrieveAETs(String aets);

    /**
     * Instance Availability
     *
     * @ejb.persistence
     *  column-name="availability"
     */
    public abstract int getAvailability();

    /**
     * @ejb.interface-method
     */
    public int getAvailabilitySafe() {
        try {
            return getAvailability();
        } catch (NullPointerException npe) {
            return 0;
        }
    }

    public abstract void setAvailability(int availability);

    /**
     * Modalities In Study
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="mods_in_study"
     */
    public abstract String getModalitiesInStudy();

    public abstract void setModalitiesInStudy(String mds);


    /**
     * @ejb.interface-method view-type="local"
     * 
     * @ejb.relation
     *  name="patient-study"
     *  role-name="study-of-patient"
     *  cascade-delete="yes"
     *
     * @jboss:relation
     *  fk-column="patient_fk"
     *  related-pk-field="pk"
     * 
     * @param patient patient of this study
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
     *
     * @param series all series of this study
     */
    public abstract void setSeries(java.util.Collection series);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation
     *  name="study-series"
     *  role-name="study-has-series"
     *    
     * @return all series of this study
     */
    public abstract java.util.Collection getSeries();
/*
    public void ejbLoad() {
        retrieveAETSet = null;
        modalitySet = null;
    }
*/
    /**
     * Create study.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, PatientLocal patient)
            throws CreateException {
//        retrieveAETSet = null;
//        modalitySet = null;
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds, PatientLocal patient)
            throws CreateException {
        setPatient(patient);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }

    /**
     * @ejb.select query="SELECT DISTINCT f.fileSystem.retrieveAET FROM Study st, IN(st.series) s, IN(s.instances) i, IN(i.files) f WHERE st.pk = ?1 AND s.hidden = FALSE"
     */ 
    public abstract Set ejbSelectRetrieveAETs(Integer pk) throws FinderException;
    
    /**
     * @ejb.select query="SELECT DISTINCT OBJECT(i) FROM Study st, IN(st.series) s, IN(s.instances) i, IN(i.files) f WHERE st.pk = ?1 AND s.hidden = FALSE AND f.fileSystem.retrieveAET = ?2"
     */ 
    public abstract java.util.Set ejbSelectInstancesWithRetrieveAET(Integer pk, String retrieveAET) throws FinderException;

    /**
     * @ejb.select query="SELECT DISTINCT s.modality FROM Study st, IN(st.series) s WHERE s.hidden = FALSE AND st.pk = ?1"
     */ 
    public abstract Set ejbSelectModalityInStudies(Integer pk) throws FinderException;

    /**
     * @ejb.select query=""
     */ 
    public abstract int ejbSelectNumberOfStudyRelatedInstances(Integer pk) throws FinderException;

    /**
     * @ejb.select query=""
     */ 
    public abstract int ejbSelectNumberOfStudyRelatedSeries(Integer pk) throws FinderException;

    /**
     * @ejb.select query=""
     */ 
    public abstract int ejbSelectAvailability(Integer pk) throws FinderException;
    
    /**
     * @ejb.interface-method
     */
    public void updateDerivedFields() throws FinderException {
        final Integer pk = getPk();
        final int numS = ejbSelectNumberOfStudyRelatedSeries(pk);
        if (getNumberOfStudyRelatedSeries() != numS)
            setNumberOfStudyRelatedSeries(numS);
        final int numI = ejbSelectNumberOfStudyRelatedInstances(pk);
        if (getNumberOfStudyRelatedInstances() != numI)
            setNumberOfStudyRelatedInstances(numI);
        Set aetSet = ejbSelectRetrieveAETs(pk);
        for (Iterator it = aetSet.iterator(); it.hasNext();) {
            final String aet = (String) it.next();
            if (ejbSelectInstancesWithRetrieveAET(pk, aet).size() < numI)
                it.remove();
        }
        final String aets = toString(aetSet);
        if (!aets.equals(getRetrieveAETs()))
            setRetrieveAETs(aets);
        final String mds = toString(ejbSelectModalityInStudies(pk));
        if (!mds.equals(getModalitiesInStudy()))
            setModalitiesInStudy(mds);
        final int availability = ejbSelectAvailability(pk);
        if (getAvailability() != availability)
            setAvailability(availability);
    }

    private static String toString(Set s) {
        String[] a = (String[]) s.toArray(new String[s.size()]);
        return StringUtils.toString(a, '\\');
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
            ds.putUL(PrivateTags.StudyPk, getPk().intValue());
            ds.putCS(Tags.ModalitiesInStudy, StringUtils
                    .split(getModalitiesInStudy(), '\\'));
            ds.putIS(Tags.NumberOfStudyRelatedSeries,
                    getNumberOfStudyRelatedSeries());
            ds.putIS(Tags.NumberOfStudyRelatedInstances,
                    getNumberOfStudyRelatedInstances());
            ds.putAE(Tags.RetrieveAET, StringUtils.split(getRetrieveAETs(),
                    '\\'));
            ds.putCS(Tags.InstanceAvailability, Availability
                    .toString(getAvailabilitySafe()));
        }
        return ds;
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setStudyIuid(ds.getString(Tags.StudyInstanceUID));
        setStudyId(ds.getString(Tags.StudyID));
        try {
	        setStudyDateTime(ds.getDateTime(Tags.StudyDate, Tags.StudyTime));
	    } catch (IllegalArgumentException e) {
	        log.warn("Illegal Study Date/Time format: " + e.getMessage());
	    }
        setAccessionNumber(ds.getString(Tags.AccessionNumber));
        setReferringPhysicianName(ds.getString(Tags.ReferringPhysicianName));
        Dataset tmp = ds.exclude(SUPPL_TAGS).excludePrivate();
        setEncodedAttributes(DatasetUtils.toByteArray(tmp,
                DcmDecodeParam.EVR_LE));
    }

    /**
     * @ejb.interface-method
     */
    public void setStudyDateTime(java.util.Date date) {
        setStudyDateTime(date != null ? new java.sql.Timestamp(date.getTime())
                : null);
    }

    /*
     * ejb.interface-method
     *
    public void incNumberOfStudyRelatedSeries(int inc) {
        setNumberOfStudyRelatedSeries(getNumberOfStudyRelatedSeries() + inc);
    }

    /**
     * ejb.interface-method
     *
    public void incNumberOfStudyRelatedInstances(int inc) {
        setNumberOfStudyRelatedInstances(getNumberOfStudyRelatedInstances()
                + inc);
    }

    /**
     * ejb.interface-method
     *
    public Set getRetrieveAETSet() {
        return Collections.unmodifiableSet(retrieveAETSet());
    }

    private Set retrieveAETSet() {
        if (retrieveAETSet == null) {
            retrieveAETSet = new HashSet();
            String aets = getRetrieveAETs();
            if (aets != null) {
                retrieveAETSet.addAll(Arrays.asList(StringUtils.split(aets,
                        '\\')));
            }
        }
        return retrieveAETSet;
    }
    
    /**
     * ejb.interface-method
     *
    public boolean updateRetrieveAETs() {
        
        Collection c = getSeries();
        HashSet newAETSet = null;
        Iterator it = c.iterator();
        while (it.hasNext()) {
            SeriesLocal series = (SeriesLocal) it.next();
            // consider only series with instances
            if (series.getNumberOfSeriesRelatedInstances() > 0) {
                if (newAETSet == null) {
                    newAETSet = new HashSet(series.getRetrieveAETSet());
                } else {
                    newAETSet.retainAll(series.getRetrieveAETSet());
                }
            }
        }
        if (newAETSet == null) {
            newAETSet = new HashSet();
        }
        if (retrieveAETSet().equals(newAETSet)) { return false; }
        retrieveAETSet = newAETSet;
        String newAETs = StringUtils.toString((String[]) retrieveAETSet()
                .toArray(new String[retrieveAETSet.size()]), '\\');
        setRetrieveAETs(newAETs);
        return true;
    }

    /**
     * 
     * ejb.interface-method
     *
    public boolean updateAvailability() {
        Collection c = getSeries();
        int availability = 0;
        for (Iterator it = c.iterator(); it.hasNext();) {
            SeriesLocal series = (SeriesLocal) it.next();
            availability = Math.max(availability, series.getAvailabilitySafe());
        }
        if (availability != getAvailabilitySafe()) {
            setAvailability(availability);
            return true;
        }
        return false;
    }

    /**
     * ejb.interface-method
     *
    public boolean updateModalitiesInStudy() {
        Collection c = getSeries();
        HashSet newModalitySet = new HashSet();
        for (Iterator it = c.iterator(); it.hasNext();) {
            SeriesLocal series = (SeriesLocal) it.next();
            if (!series.getHiddenSafe())
                newModalitySet.add(series.getModality());
        }
        if (!newModalitySet.equals(getModalitySet())) {
            StringBuffer sb = new StringBuffer();
            Iterator it = newModalitySet.iterator();
            if (it.hasNext()) {
                sb.append(it.next());
                while (it.hasNext())
                    sb.append('\\').append(it.next());
            }
            setModalitiesInStudy(sb.toString());
            modalitySet = newModalitySet;
            return true;
        }
        return false;
    }
    
    /**
     * ejb.interface-method
     *
    public Set getModalitySet() {
        if (modalitySet == null) {
            modalitySet = new HashSet();
            String mds = getModalitiesInStudy();
            if (mds != null) {
                modalitySet.addAll(Arrays.asList(StringUtils.split(mds, '\\')));
            }
        }
        return modalitySet;
    }

    /**
     * ejb.interface-method
     *
    public boolean addModalityInStudy(String md) {
        if (getModalitySet().contains(md)) { return false; }
        modalitySet.add(md);
        String prev = getModalitiesInStudy();
        if (prev == null || prev.length() == 0) {
            setModalitiesInStudy(md);
        } else {
            setModalitiesInStudy(prev + '\\' + md);
        }
        return true;
    }
    */

    /**
     * 
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        return "Study[pk=" + getPk() + ", uid=" + getStudyIuid()
                + ", patient->" + getPatient() + "]";
    }
}