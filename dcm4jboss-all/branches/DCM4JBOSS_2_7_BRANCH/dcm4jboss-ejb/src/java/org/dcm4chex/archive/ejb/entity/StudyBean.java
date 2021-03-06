/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.ejb.entity;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

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
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.CodeLocalHome;
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
 * @jboss.query signature="org.dcm4chex.archive.ejb.interfaces.StudyLocal findByStudyIuid(java.lang.String uid)"
 *              strategy="on-find"
 *              eager-load-group="*"
 * @ejb.finder signature="java.util.Collection findStudyToCheck(java.sql.Timestamp minCreatedTime, java.sql.Timestamp maxCreatedTime, java.sql.Timestamp checkedBefore, int limit)"
 *             query="" transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findStudyToCheck(java.sql.Timestamp minCreatedTime, java.sql.Timestamp maxCreatedTime, java.sql.Timestamp checkedBefore, int limit)"
 *              query="SELECT OBJECT(s) FROM Study AS s WHERE (s.createdTime BETWEEN ?1 AND ?2) AND (s.timeOfLastConsistencyCheck IS NULL OR s.timeOfLastConsistencyCheck < ?3) LIMIT ?4"
 *  
 * @jboss.query signature="int ejbSelectNumberOfStudyRelatedSeries(java.lang.Integer pk)"
 * 	            query="SELECT COUNT(s) FROM Series s WHERE s.study.pk = ?1"
 * @jboss.query signature="int ejbSelectNumberOfStudyRelatedInstances(java.lang.Integer pk)"
 * 	            query="SELECT COUNT(i) FROM Instance i WHERE i.series.study.pk = ?1"
 * @jboss.query signature="int ejbSelectNumberOfStudyRelatedInstancesOnMediaWithStatus(java.lang.Integer pk, int status)"
 *              query="SELECT COUNT(i) FROM Instance i WHERE i.series.study.pk = ?1 AND i.media.mediaStatus = ?2"
 * @jboss.query signature="int ejbSelectNumberOfCommitedInstances(java.lang.Integer pk)"
 * 	            query="SELECT COUNT(i) FROM Instance i WHERE i.series.study.pk = ?1 AND i.commitment = TRUE"
 * @jboss.query signature="int ejbSelectNumberOfExternalRetrieveableInstances(java.lang.Integer pk)"
 *              query="SELECT COUNT(i) FROM Instance i WHERE i.series.study.pk = ?1 AND i.externalRetrieveAET IS NOT NULL"
 * @jboss.query signature="int ejbSelectAvailability(java.lang.Integer pk)"
 * 	            query="SELECT MAX(i.availability) FROM Instance i WHERE i.series.study.pk = ?1"
 * @jboss.query signature="int ejbSelectStudyFileSize(java.lang.Integer pk)"
 * 	            query="SELECT SUM(f.fileSize) FROM File f WHERE f.instance.series.study.pk = ?1"
 * @jboss.query signature="int ejbSelectGenericInt(java.lang.String jbossQl, java.lang.Object[] args)"
 *              dynamic="true"
 *
 * @ejb.ejb-ref ejb-name="Code" view-type="local" ref-name="ejb/Code"
 *
 */
public abstract class StudyBean implements EntityBean {

    private static final Logger log = Logger.getLogger(StudyBean.class);

    private static final int[] SUPPL_TAGS = { Tags.RetrieveAET,
            Tags.InstanceAvailability, Tags.NumberOfStudyRelatedSeries,
            Tags.NumberOfStudyRelatedInstances, Tags.StudyStatusID,
            Tags.StorageMediaFileSetID, Tags.StorageMediaFileSetUID };

    private CodeLocalHome codeHome;

    public void setEntityContext(EntityContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            codeHome = (CodeLocalHome)
                    jndiCtx.lookup("java:comp/env/ejb/Code");
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
     * Study Description
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="study_desc"
     */
    public abstract String getStudyDescription();

    public abstract void setStudyDescription(String description);
    
    /**
     * Study Status ID
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="study_status_id"
     */
    public abstract String getStudyStatusId();

    /**
     * @ejb.interface-method
     */
    public abstract void setStudyStatusId(String statusId);
    
    
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
     * @ejb.interface-method
     * @ejb.persistence column-name="checked_time"
     */
    public abstract java.sql.Timestamp getTimeOfLastConsistencyCheck();

    /**
     * @ejb.interface-method
     */
    public abstract void setTimeOfLastConsistencyCheck(java.sql.Timestamp time);
    
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
     * @ejb.relation name="study-pcode" role-name="study-with-pcode"
     *               target-ejb="Code" target-role-name="pcode-for-study"
     *               target-multiple="yes"
     * @jboss.relation-table table-name="rel_study_pcode"
     * @jboss.relation fk-column="pcode_fk" related-pk-field="pk"     
     * @jboss.target-relation fk-column="study_fk" related-pk-field="pk"     
     */    
    public abstract java.util.Collection getProcedureCodes();
    public abstract void setProcedureCodes(java.util.Collection codes);

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
        try {
        	setPatient(patient);
        	CodeBean.addCodesTo(codeHome, 
        			ds.get(Tags.ProcedureCodeSeq),
        			getProcedureCodes());
        } catch (CreateException e) {
        	throw e;
        } catch (FinderException e) {
        	throw new CreateException(e.getMessage());
        }
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }

    /**
     * @ejb.select query="SELECT DISTINCT s.retrieveAETs FROM Series s WHERE s.study.pk = ?1"
     */
    public abstract Set ejbSelectSeriesRetrieveAETs(Integer pk)
            throws FinderException;

    /**
     * @ejb.select query="SELECT DISTINCT i.externalRetrieveAET FROM Study st, IN(st.series) s, IN(s.instances) i WHERE st.pk = ?1"
     */
    public abstract java.util.Set ejbSelectExternalRetrieveAETs(Integer pk)
            throws FinderException;

    /**
     * @ejb.select query="SELECT DISTINCT i.media FROM Study st, IN(st.series) s, IN(s.instances) i WHERE st.pk = ?1 AND i.media.mediaStatus = ?2"
     */
    public abstract java.util.Set ejbSelectMediaWithStatus(Integer pk,
            int status) throws FinderException;

    /**
     * @ejb.select query="SELECT DISTINCT s.modality FROM Study st, IN(st.series) s WHERE st.pk = ?1"
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

    /**
     * @ejb.select query=""
     */
    public abstract long ejbSelectStudyFileSize(Integer pk)
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
    private int countStudyRelatedInstancesWithCopyOnROFS( Integer pk, Collection fsPks, Integer fileStatus )
    		throws FinderException {
    	Object[] args = new Object[]{pk,fileStatus};
        StringBuffer jbossQl = new StringBuffer();
        jbossQl.append("SELECT COUNT(DISTINCT i) FROM Instance i, IN(i.files) f");
		jbossQl.append(" WHERE i.series.study.pk = ?1 AND f.fileStatus = ?2");
		jbossQl.append(" AND f.fileSystem.directoryPath IN (");
		for ( Iterator iter = fsPks.iterator() ; iter.hasNext();){
			jbossQl.append("'").append(iter.next()).append("'");
			if ( iter.hasNext() ) jbossQl.append(',');
		}
        jbossQl.append(")");
        // call dynamic-ql query
        return ejbSelectGenericInt(jbossQl.toString(), args);
    }
    /**    
     * @throws FinderException
     * @ejb.home-method
     */
    public long ejbHomeSelectStudySize( Integer pk ) throws FinderException {
    	return ejbSelectStudyFileSize(pk);
    }
    
    private boolean updateRetrieveAETs(Integer pk, int numI)
    		throws FinderException {
    	boolean updated = false;
        String aets = null;
        if (numI > 0) {
	        Set seriesAets = ejbSelectSeriesRetrieveAETs(pk);
            if (!seriesAets.contains(null))
            {
                Iterator it = seriesAets.iterator();
                aets = (String) it.next();
                while (it.hasNext())
                    aets = commonRetrieveAETs(aets, (String) it.next());
            }
    	}
        if (updated = aets == null 
        		? getRetrieveAETs() != null 
        		: !aets.equals(getRetrieveAETs())) {
        	setRetrieveAETs(aets);
        }
        return updated;
    }
    
    private String commonRetrieveAETs(String aets1, String aets2)
    {
        if (aets1.equals(aets2))
            return aets1;
        String[] a1 = StringUtils.split(aets1, '\\');
        String[] a2 = StringUtils.split(aets2, '\\');
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < a1.length; i++)
            for (int j = 0; j < a2.length; j++)
                if (a1[i].equals(a2[j]))
                    sb.append(a1[i]).append('\\');
        int l = sb.length();
        if (l == 0)
            return null;
        sb.setLength(l-1);
        return sb.toString();
    }

    private boolean updateExternalRetrieveAET(Integer pk, int numI)
    	throws FinderException {
    	boolean updated = false;
    	String aet = null;
        if (numI > 0) {
	        Set eAetSet = ejbSelectExternalRetrieveAETs(pk);
	        if (eAetSet.size() == 1)
	        	aet = (String) eAetSet.iterator().next();
        }
        if (updated = aet == null 
        		? getExternalRetrieveAET() != null 
        		: !aet.equals(getExternalRetrieveAET())) {
        	setExternalRetrieveAET(aet);
        }
        return updated;
    }
    

    private boolean updateAvailability(Integer pk, int numI) throws FinderException {
    	boolean updated = false;
        int availability = getNumberOfStudyRelatedInstances() > 0
        			? ejbSelectAvailability(getPk())
        			: Availability.UNAVAILABLE;
        if (updated = availability != getAvailabilitySafe()) {
            setAvailability(availability);
        }
        return updated;
    }
    
    private boolean updateNumberOfInstances(Integer pk) throws FinderException {
    	boolean updated = false;
        final int numS = ejbSelectNumberOfStudyRelatedSeries(pk);
        if (getNumberOfStudyRelatedSeries() != numS) {
            setNumberOfStudyRelatedSeries(numS);
        	updated = true;
        }
        final int numI = numS > 0 ? ejbSelectNumberOfStudyRelatedInstances(pk)
                : 0;
        if (getNumberOfStudyRelatedInstances() != numI) {
            setNumberOfStudyRelatedInstances(numI);
        	updated = true;
        }
        return updated;
    }
    
    private boolean updateFilesetId(Integer pk, int numI) throws FinderException {
       	boolean updated = false;
       	String fileSetId = null;
       	String fileSetIuid = null;
        if (numI > 0) {
	        if (ejbSelectNumberOfStudyRelatedInstancesOnMediaWithStatus(pk, MediaDTO.COMPLETED) == numI) {
	            Set c = ejbSelectMediaWithStatus(pk, MediaDTO.COMPLETED);
	            if (c.size() == 1) {
	                MediaLocal media = (MediaLocal) c.iterator().next();
	                fileSetId = media.getFilesetId();
	                fileSetIuid = media.getFilesetIuid();
	            }
	        }
        }
        if (fileSetId == null ? getFilesetId() != null
        		: !fileSetId.equals(getFilesetId())) {
        	setFilesetId(fileSetId);
        	updated = true;
        }
        if (fileSetIuid == null ? getFilesetIuid() != null
        		: !fileSetIuid.equals(getFilesetIuid())) {
        	setFilesetIuid(fileSetIuid);
        	updated = true;
        }
        return updated;
    }

    private boolean updateModalitiesInStudy(Integer pk, int numI) throws FinderException {
      	boolean updated = false;
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
        if (!mds.equals(getModalitiesInStudy())) {
            setModalitiesInStudy(mds);
            updated = true;
        }
    	return updated;
    }
    
    /**
     * @ejb.interface-method
     */
    public boolean updateDerivedFields(boolean numOfInstances,
    		boolean retrieveAETs, boolean externalRettrieveAETs, 
            boolean filesetId, boolean availibility, boolean modsInStudies)
            throws FinderException {
    	boolean updated = false;
    	final Integer pk = getPk();
		if (numOfInstances)
			if (updateNumberOfInstances(pk)) updated = true;
    	final int numI = getNumberOfStudyRelatedInstances();
		if (retrieveAETs)
			if (updateRetrieveAETs(pk, numI)) updated = true;
		if (externalRettrieveAETs)
			if (updateExternalRetrieveAET(pk, numI)) updated = true;
		if (filesetId)
			if (updateFilesetId(pk, numI)) updated = true;
		if (availibility)
			if (updateAvailability(pk, numI)) updated = true;
		if (modsInStudies)
			if (updateModalitiesInStudy(pk, numI)) updated = true;
		return updated;
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
    public boolean isStudyAvailableOnROFs(Collection listOfROFsPks, int validFileStatus) throws FinderException {
        return ( countStudyRelatedInstancesWithCopyOnROFS(
                        getPk(), listOfROFsPks, new Integer(validFileStatus)) == getNumberOfStudyRelatedInstances() );
    }
    
    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes(boolean supplement) {
        Dataset ds = DatasetUtils.fromByteArray(getEncodedAttributes());
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
            ds.putCS(Tags.StudyStatusID, getStudyStatusId());
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
        setStudyDescription(ds.getString(Tags.StudyDescription));
        Dataset tmp = ds.subSet(SUPPL_TAGS, true, true);
        setEncodedAttributes(DatasetUtils.toByteArray(tmp));
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