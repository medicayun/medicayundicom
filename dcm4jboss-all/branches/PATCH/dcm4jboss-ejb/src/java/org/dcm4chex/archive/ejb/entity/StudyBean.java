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
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.ejb.interfaces.MediaLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * 
 * @ejb.bean name="Study" 
 *           type="CMP" 
 *           view-type="local"
 *           local-jndi-name="ejb/Study" 
 *           primkey-field="pk"
 * @ejb.persistence table-name="study"
 * @ejb.transaction type="Required"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * @jboss.audit-created-time field-name="createdTime"
 * @jboss.audit-updated-time field-name="updatedTime"
 *
 * @ejb.finder transaction-type="Supports"
 *             signature="org.dcm4chex.archive.ejb.interfaces.StudyLocal findByStudyIuid(java.lang.String uid)"
 *             query="SELECT OBJECT(a) FROM Study AS a WHERE a.studyIuid = ?1"
 * @ejb.finder signature="java.util.Collection findStudiesOnMedia(org.dcm4chex.archive.ejb.interfaces.MediaLocal media)"
 *             query="SELECT DISTINCT OBJECT(st) FROM Study st, IN(st.series) s, IN(s.instances) i WHERE i.media = ?1"
 *             transaction-type="Supports"
 *  
 * @jboss.query signature="org.dcm4chex.archive.ejb.interfaces.StudyLocal findByStudyIuid(java.lang.String uid)"
 *              strategy="on-find"
 *              eager-load-group="*"
 * @jboss.query signature="int ejbSelectNumberOfStudyRelatedSeries(java.lang.Integer pk)"
 * 	            query="SELECT COUNT(s) FROM Series s WHERE s.hidden = FALSE AND s.study.pk = ?1"
 * @jboss.query signature="int ejbSelectNumberOfStudyRelatedInstances(java.lang.Integer pk)"
 * 	            query="SELECT COUNT(i) FROM Instance i WHERE i.series.hidden = FALSE AND i.series.study.pk = ?1"
 * @jboss.query signature="int ejbSelectNumberOfStudyRelatedInstancesWithInternalRetrieveAET(java.lang.Integer pk, java.lang.String retrieveAET)"
 *              query="SELECT COUNT(DISTINCT i) FROM Instance i, IN(i.files) f WHERE i.series.hidden = FALSE AND i.series.study.pk = ?1" AND f.fileSystem.retrieveAET = ?2"
 * @jboss.query signature="int ejbSelectNumberOfStudyRelatedInstancesOnMediaWithStatus(java.lang.Integer pk, int status)"
 *              query="SELECT COUNT(i) FROM Instance i WHERE i.series.hidden = FALSE AND i.series.study.pk = ?1 AND i.media.mediaStatus = ?2"
 * @jboss.query signature="int ejbSelectNumberOfCommitedInstances(java.lang.Integer pk)"
 * 	            query="SELECT COUNT(i) FROM Instance i WHERE i.series.hidden = FALSE AND i.series.study.pk = ?1 AND i.commitment = TRUE"
 * @jboss.query signature="int ejbSelectNumberOfExternalRetrieveableInstances(java.lang.Integer pk)"
 *              query="SELECT COUNT(i) FROM Instance i WHERE i.series.hidden = FALSE AND i.series.study.pk = ?1 AND i.externalRetrieveAET IS NOT NULL"
 * @jboss.query signature="int ejbSelectAvailability(java.lang.Integer pk)"
 * 	            query="SELECT MAX(i.availability) FROM Instance i WHERE i.series.hidden = FALSE AND i.series.study.pk = ?1"
 * 
 *
 */
public abstract class StudyBean implements EntityBean {

    private static final Logger log = Logger.getLogger(StudyBean.class);

    private static final int[] SUPPL_TAGS = { Tags.RetrieveAET,
            Tags.InstanceAvailability, Tags.NumberOfStudyRelatedSeries,
            Tags.NumberOfStudyRelatedInstances, Tags.StorageMediaFileSetID,
            Tags.StorageMediaFileSetUID };

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
     * Study Instance UID
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="study_iuid"
     */
    public abstract String getStudyIuid();

    public abstract void setStudyIuid(String uid);

    /**
     * Study ID
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="study_id"
     */
    public abstract String getStudyId();

    public abstract void setStudyId(String uid);

    /**
     * Study Datetime
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="study_datetime"
     */
    public abstract java.sql.Timestamp getStudyDateTime();

    public abstract void setStudyDateTime(java.sql.Timestamp dateTime);

    /**
     * Accession Number
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="accession_no"
     */
    public abstract String getAccessionNumber();

    public abstract void setAccessionNumber(String no);

    /**
     * Referring Physician
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="ref_physician"
     */
    public abstract String getReferringPhysicianName();

    public abstract void setReferringPhysicianName(String physician);

    /**
     * Number Of Study Related Series
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="num_series"
     * 
     */
    public abstract int getNumberOfStudyRelatedSeries();

    public abstract void setNumberOfStudyRelatedSeries(int num);

    /**
     * Number Of Study Related Instances
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="num_instances"
     * 
     */
    public abstract int getNumberOfStudyRelatedInstances();

    public abstract void setNumberOfStudyRelatedInstances(int num);

    /**
     * Study DICOM Attributes
     *
     * @ejb.persistence column-name="study_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

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
     * @ejb.persistence column-name="ext_retr_aet"
     */
    public abstract String getExternalRetrieveAET();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setExternalRetrieveAET(String aet);

    /**
     * Retrieve AETs
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="retrieve_aets"
     */
    public abstract String getRetrieveAETs();

    public abstract void setRetrieveAETs(String aets);

    /**
     * Instance Availability
     *
     * @ejb.persistence column-name="availability"
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
     * @ejb.persistence column-name="mods_in_study"
     */
    public abstract String getModalitiesInStudy();

    public abstract void setModalitiesInStudy(String mds);

    /**
     * @ejb.interface-method view-type="local"
     * 
     * @ejb.relation name="patient-study"
     *               role-name="study-of-patient"
     *               cascade-delete="yes"
     *
     * @jboss.relation fk-column="patient_fk"
     *                 related-pk-field="pk"
     * 
     * @param patient patient of this study
     */
    public abstract void setPatient(PatientLocal patient);

    /**
     * @ejb.interface-method
     * 
     * @return patient of this study
     */
    public abstract PatientLocal getPatient();

    /**
     * @ejb.interface-method
     *
     * @param series all series of this study
     */
    public abstract void setSeries(java.util.Collection series);

    /**
     * @ejb.interface-method
     * @ejb.relation name="study-series"
     *               role-name="study-has-series"
     *    
     * @return all series of this study
     */
    public abstract java.util.Collection getSeries();

    /**
     * Create study.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, PatientLocal patient)
            throws CreateException {
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
    public abstract Set ejbSelectInternalRetrieveAETs(Integer pk)
            throws FinderException;

    /**
     * @ejb.select query="SELECT DISTINCT i.externalRetrieveAET FROM Study st, IN(st.series) s, IN(s.instances) i WHERE st.pk = ?1 AND s.hidden = FALSE"
     */
    public abstract java.util.Set ejbSelectExternalRetrieveAETs(Integer pk)
            throws FinderException;

    /**
     * @ejb.select query="SELECT DISTINCT i.media FROM Study st, IN(st.series) s, IN(s.instances) i WHERE st.pk = ?1 AND i.media.mediaStatus = ?2"
     */
    public abstract java.util.Set ejbSelectMediaWithStatus(Integer pk,
            int status) throws FinderException;

    /**
     * @ejb.select query="SELECT DISTINCT s.modality FROM Study st, IN(st.series) s WHERE s.hidden = FALSE AND st.pk = ?1"
     */
    public abstract Set ejbSelectModalityInStudies(Integer pk)
            throws FinderException;

    /**
     * @ejb.select query=""
     */
    public abstract int ejbSelectNumberOfStudyRelatedInstancesOnMediaWithStatus(
            Integer pk, int status) throws FinderException;

    /**
     * @ejb.select query=""
     */
    public abstract int ejbSelectNumberOfStudyRelatedInstancesWithInternalRetrieveAET(
            Integer pk, String retrieveAET) throws FinderException;

    /**
     * @ejb.select query=""
     */
    public abstract int ejbSelectNumberOfStudyRelatedInstances(Integer pk)
            throws FinderException;

    /**
     * @ejb.select query=""
     */
    public abstract int ejbSelectNumberOfStudyRelatedSeries(Integer pk)
            throws FinderException;

    /**
     * @ejb.select query=""
     */ 
    public abstract int ejbSelectNumberOfCommitedInstances(Integer pk) throws FinderException;

    /**
     * @ejb.select query=""
     */ 
    public abstract int ejbSelectNumberOfExternalRetrieveableInstances(Integer pk) throws FinderException;

    /**
     * @ejb.interface-method
     */
    public int getNumberOfCommitedInstances() throws FinderException {
        return ejbSelectNumberOfCommitedInstances(getPk());
    }

    /**
     * @ejb.interface-method
     */
    public boolean isStudyExternalRetrievable() throws FinderException {
        return ejbSelectNumberOfExternalRetrieveableInstances(getPk()) 
                == getNumberOfStudyRelatedInstances();
    }
    
    /**
     * @ejb.select query=""
     */
    public abstract int ejbSelectAvailability(Integer pk)
            throws FinderException;

    private void updateRetrieveAETs(Integer pk, int numI) throws FinderException {
        String aets = null;
        if (numI > 0) {
	        StringBuffer sb = new StringBuffer();
	        Set iAetSet = ejbSelectInternalRetrieveAETs(pk);
	        if (iAetSet.remove(null))
	            log.warn("Study[iuid=" + getStudyIuid()
	                    + "] contains File(s) with unspecified Retrieve AET");
	        for (Iterator it = iAetSet.iterator(); it.hasNext();) {
	            final String aet = (String) it.next();
	            if (ejbSelectNumberOfStudyRelatedInstancesWithInternalRetrieveAET(pk, aet) == numI)
	                sb.append(aet).append('\\');
	        }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
                aets = sb.toString();
            }
    	}
        if (aets == null 
        		? getRetrieveAETs() != null 
        		: !aets.equals(getRetrieveAETs())) {
        	setRetrieveAETs(aets);
        }
    }
    
    private void updateExternalRetrieveAET(Integer pk, int numI) throws FinderException {
    	String aet = null;
        if (numI > 0) {
	        Set eAetSet = ejbSelectExternalRetrieveAETs(pk);
	        if (eAetSet.size() == 1)
	        	aet = (String) eAetSet.iterator().next();
        }
        if (aet == null 
        		? getExternalRetrieveAET() != null 
        		: !aet.equals(getExternalRetrieveAET())) {
        	setExternalRetrieveAET(aet);
        }    	
    }
    

    private void updateAvailability(Integer pk, int numI) throws FinderException {
        int availability = getNumberOfStudyRelatedInstances() > 0
        			? ejbSelectAvailability(getPk())
        			: Availability.UNAVAILABLE;
        if (availability != getAvailabilitySafe()) {
            setAvailability(availability);
        }
    }
    
    private int updateNumberOfInstances(Integer pk) throws FinderException {
        final int numS = ejbSelectNumberOfStudyRelatedSeries(pk);
        if (getNumberOfStudyRelatedSeries() != numS)
            setNumberOfStudyRelatedSeries(numS);
        final int numI = numS > 0 ? ejbSelectNumberOfStudyRelatedInstances(pk)
                : 0;
        if (getNumberOfStudyRelatedInstances() != numI)
            setNumberOfStudyRelatedInstances(numI);
        return numI;
    }
    
    private void updateFilesetId(Integer pk, int numI) throws FinderException {
        if (numI > 0) {
	        if (ejbSelectNumberOfStudyRelatedInstancesOnMediaWithStatus(pk, MediaDTO.COMPLETED) == numI) {
	            Set c = ejbSelectMediaWithStatus(pk, MediaDTO.COMPLETED);
	            if (c.size() == 1) {
	                MediaLocal media = (MediaLocal) c.iterator().next();
	                setFilesetId(media.getFilesetId());
	                setFilesetIuid(media.getFilesetIuid());
	                return;
	            }
	        }
        }
        setFilesetId(null);
        setFilesetIuid(null);
    }

    private void updateModalitiesInStudy(Integer pk, int numI) throws FinderException {
        String mds = "";
        if (numI > 0) {
            Set c = ejbSelectModalityInStudies(pk);
            if (c.remove(null))
                log.warn("Study[iuid=" + getStudyIuid()
                        + "] contains Series with unspecified Modality");
            if (!c.isEmpty()) {
                Iterator it = c.iterator();
                StringBuffer sb = new StringBuffer((String) it.next());
                while (it.hasNext())
                    sb.append('\\').append(it.next());
                mds = sb.toString();
            }
        }
        if (!mds.equals(getModalitiesInStudy()))
            setModalitiesInStudy(mds);
    	
    }

    /**
     * @ejb.interface-method
     */
    public void updateDerivedFields(boolean numOfInstances,
    		boolean retrieveAETs, boolean externalRettrieveAETs, 
            boolean filesetId, boolean availibility, boolean modsInStudies)
            throws FinderException {
    	final Integer pk = getPk();
		final int numI = numOfInstances ? updateNumberOfInstances(pk) 
				: getNumberOfStudyRelatedInstances();
		if (retrieveAETs)
			updateRetrieveAETs(pk, numI);
		if (externalRettrieveAETs)
			updateExternalRetrieveAET(pk, numI);
		if (filesetId)
			updateFilesetId(pk, numI);
		if (availibility)
			updateAvailability(pk, numI);
		if (modsInStudies)
			updateModalitiesInStudy(pk, numI);
    }
     
    /**
     * @ejb.interface-method
     */
    public boolean isStudyAvailableOnMedia() throws FinderException {
        String fsuid = getFilesetIuid();
        return (fsuid != null && fsuid.length() != 0)
                || ejbSelectNumberOfStudyRelatedInstancesOnMediaWithStatus(
                        getPk(), MediaDTO.COMPLETED) == getNumberOfStudyRelatedInstances();
    }

    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes(boolean supplement) {
        Dataset ds = DatasetUtils.fromByteArray(getEncodedAttributes(),
                DcmDecodeParam.EVR_LE, null);
        if (supplement) {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putUL(PrivateTags.StudyPk, getPk().intValue());
            ds.putCS(Tags.ModalitiesInStudy, StringUtils.split(
                    getModalitiesInStudy(), '\\'));
            ds.putIS(Tags.NumberOfStudyRelatedSeries,
                    getNumberOfStudyRelatedSeries());
            ds.putIS(Tags.NumberOfStudyRelatedInstances,
                    getNumberOfStudyRelatedInstances());
            ds.putSH(Tags.StorageMediaFileSetID, getFilesetId());
            ds.putUI(Tags.StorageMediaFileSetUID, getFilesetIuid());
            DatasetUtils.putRetrieveAET(ds, getRetrieveAETs(),
            		getExternalRetrieveAET());
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
        Dataset tmp = ds.subSet(SUPPL_TAGS, true, true);
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