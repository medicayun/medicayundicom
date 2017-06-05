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
import java.util.Iterator;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.MPPSLocal;
import org.dcm4chex.archive.ejb.interfaces.MPPSLocalHome;
import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.ejb.interfaces.MediaLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesRequestLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 * @ejb.bean name="Series" type="CMP" view-type="local" primkey-field="pk"
 *           local-jndi-name="ejb/Series"
 * @ejb.transaction type="Required"
 * @ejb.persistence table-name="series"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * @jboss.audit-created-time field-name="createdTime"
 * @jboss.audit-updated-time field-name="updatedTime"
 * 
 * @ejb.finder signature="java.util.Collection findSeriesOnMedia(org.dcm4chex.archive.ejb.interfaces.MediaLocal media)"
 *             query="SELECT DISTINCT OBJECT(s) FROM Series s, IN(s.instances) i WHERE i.media = ?1"
 *             transaction-type="Supports"
 *             
 * @ejb.finder signature="org.dcm4chex.archive.ejb.interfaces.SeriesLocal findBySeriesIuid(java.lang.String uid)"
 *             query="SELECT OBJECT(s) FROM Series AS s WHERE s.seriesIuid = ?1"
 *             transaction-type="Supports"
 * @jboss.query signature="org.dcm4chex.archive.ejb.interfaces.SeriesLocal findBySeriesIuid(java.lang.String uid)"
 *              strategy="on-find"
 *              eager-load-group="*"
 *              
 * @ejb.finder signature="java.util.Collection findByPpsIuid(java.lang.String uid)"
 *             query="SELECT OBJECT(s) FROM Series AS s WHERE s.ppsIuid = ?1"
 *             transaction-type="Supports"
 * 
 * @ejb.finder signature="java.util.Collection findWithNoPpsIuidFromSrcAETReceivedBefore(java.lang.String srcAET, java.sql.Timestamp receivedBefore)"
 *             query="SELECT OBJECT(s) FROM Series AS s WHERE s.ppsIuid IS NULL AND s.sourceAET = ?1 AND s.createdTime < ?2"
 *             transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findWithNoPpsIuidFromSrcAETReceivedBefore(java.lang.String srcAET, java.sql.Timestamp receivedBefore)"
 *              strategy="on-find"
 *              eager-load-group="*"
 * 
 * @jboss.query signature="int ejbSelectNumberOfSeriesRelatedInstancesWithInternalRetrieveAET(java.lang.Integer pk, java.lang.String retrieveAET)"
 *              query="SELECT COUNT(DISTINCT i) FROM Series s, IN(s.instances) i, IN(i.files) f WHERE s.pk = ?1 AND f.fileSystem.retrieveAET = ?2"
 * @jboss.query signature="int ejbSelectNumberOfSeriesRelatedInstancesOnMediaWithStatus(java.lang.Integer pk, int status)"
 *              query="SELECT COUNT(i) FROM Instance i WHERE i.series.pk = ?1 AND i.media.mediaStatus = ?2"
 * @jboss.query signature="int ejbSelectNumberOfSeriesRelatedInstances(java.lang.Integer pk)"
 * 	            query="SELECT COUNT(i) FROM Instance i WHERE i.series.pk = ?1"
 * @jboss.query signature="int ejbSelectAvailability(java.lang.Integer pk)"
 * 	            query="SELECT MAX(i.availability) FROM Instance i WHERE i.series.pk = ?1"
 * 
 * @ejb.ejb-ref ejb-name="MPPS" view-type="local" ref-name="ejb/MPPS"
 * @ejb.ejb-ref ejb-name="SeriesRequest" view-type="local" ref-name="ejb/Request"
 * 
 */
public abstract class SeriesBean implements EntityBean {

    private static final Logger log = Logger.getLogger(SeriesBean.class);

    private static final int[] SUPPL_TAGS = { Tags.RetrieveAET,
            Tags.InstanceAvailability, Tags.NumberOfSeriesRelatedInstances,
            Tags.StorageMediaFileSetID, Tags.StorageMediaFileSetUID};

    private EntityContext ejbctx;
    private MPPSLocalHome mppsHome;
    private SeriesRequestLocalHome reqHome;

    public void setEntityContext(EntityContext ctx) {
        ejbctx = ctx;
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            mppsHome = (MPPSLocalHome) 
                    jndiCtx.lookup("java:comp/env/ejb/MPPS");
            reqHome = (SeriesRequestLocalHome)
                    jndiCtx.lookup("java:comp/env/ejb/Request");
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
        mppsHome = null;
        reqHome = null;
        ejbctx = null;
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
     * Series Instance UID
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="series_iuid"
     */
    public abstract String getSeriesIuid();

    public abstract void setSeriesIuid(String uid);

    /**
     * Series Number
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="series_no"
     */
    public abstract String getSeriesNumber();

    public abstract void setSeriesNumber(String no);

    /**
     * Modality
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="modality"
     */
    public abstract String getModality();

    public abstract void setModality(String md);

    /**
     * PPS Start Datetime
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="pps_start"
     */
    public abstract java.sql.Timestamp getPpsStartDateTime();

    public abstract void setPpsStartDateTime(java.sql.Timestamp datetime);

    /**
     * PPS Instance UID
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="pps_iuid"
     */
    public abstract String getPpsIuid();

    /**
     * @ejb.interface-method
     */
    public abstract void setPpsIuid(String uid);

    /**
     * Number Of Series Related Instances
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="num_instances"
     * 
     */
    public abstract int getNumberOfSeriesRelatedInstances();

    public abstract void setNumberOfSeriesRelatedInstances(int num);
    
    /**
     * Encoded Series Dataset
     *
     * @ejb.persistence column-name="series_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] attr);

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
     * @ejb.persistence column-name="src_aet"
     */
    public abstract String getSourceAET();
    public abstract void setSourceAET(String aet);

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
     * Hidden Flag
     *
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
     * @ejb.interface-method
     * @ejb.relation name="study-series" role-name="series-of-study"
     *               cascade-delete="yes"
     * @jboss.relation fk-column="study_fk" related-pk-field="pk"
     */
    public abstract StudyLocal getStudy();
    public abstract void setStudy(StudyLocal study);

    /**
     * @ejb.relation name="mpps-series" role-name="series-of-mpps"
     * @jboss.relation fk-column="mpps_fk" related-pk-field="pk"
     */
    public abstract MPPSLocal getMpps();
    public abstract void setMpps(MPPSLocal mpps);

    /**
     * @ejb.interface-method
     * @ejb.relation name="series-request-attributes"
     *               role-name="series-has-request-attributes"
     */
    public abstract java.util.Collection getRequestAttributes();
    public abstract void setRequestAttributes(java.util.Collection series);

    /**
     * @ejb.interface-method
     * @ejb.relation name="series-instance" role-name="series-has-instance"
     */
    public abstract java.util.Collection getInstances();
    public abstract void setInstances(java.util.Collection insts);


    /**
     * Create series.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, StudyLocal study)
            throws CreateException {
    	ds.setPrivateCreatorID(PrivateTags.CreatorID);
    	setSourceAET(ds.getString(PrivateTags.CallingAET));
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds, StudyLocal study)
            throws CreateException {
        createRequestAttributes(ds.get(Tags.RequestAttributesSeq));
        updateMpps();
        setStudy(study);
        log.info("Created " + prompt());
    }

    private void createRequestAttributes(DcmElement sq) throws CreateException {
        if (sq == null) return;
        Collection c = getRequestAttributes();
        SeriesLocal series = (SeriesLocal) ejbctx.getEJBLocalObject();
        for (int i = 0, n = sq.vm(); i < n; i++) {
            c.add(reqHome.create(sq.getItem(i), series));
        }
        
    }

    /**
     * @ejb.select query="SELECT DISTINCT f.fileSystem.retrieveAET FROM Series s, IN(s.instances) i, IN(i.files) f WHERE s.pk = ?1"
     */ 
    public abstract java.util.Set ejbSelectInternalRetrieveAETs(Integer pk) throws FinderException;

    /**
     * @ejb.select query="SELECT DISTINCT i.externalRetrieveAET FROM Series s, IN(s.instances) i WHERE s.pk = ?1"
     */ 
    public abstract java.util.Set ejbSelectExternalRetrieveAETs(Integer pk) throws FinderException;
    
    /**
     * @ejb.select query="SELECT DISTINCT i.media FROM Series s, IN(s.instances) i WHERE s.pk = ?1 AND i.media.mediaStatus = ?2"
     */ 
    public abstract java.util.Set ejbSelectMediaWithStatus(Integer pk, int status) throws FinderException;

    /**
     * @ejb.select query=""
     */ 
    public abstract int ejbSelectNumberOfSeriesRelatedInstancesOnMediaWithStatus(Integer pk, int status) throws FinderException;

    /**
     * @ejb.select query=""
     */ 
    public abstract int ejbSelectNumberOfSeriesRelatedInstancesWithInternalRetrieveAET(Integer pk, String retrieveAET) throws FinderException;

    /**
     * @ejb.select query=""
     */ 
    public abstract int ejbSelectNumberOfSeriesRelatedInstances(Integer pk) throws FinderException;
    
    /**
     * @ejb.select query=""
     */ 
    public abstract int ejbSelectAvailability(Integer pk) throws FinderException;
    
    private boolean updateRetrieveAETs(Integer pk, int numI) throws FinderException {
    	boolean updated = false;
        String aets = null;
        if (numI > 0) {
	        StringBuffer sb = new StringBuffer();
	        Set iAetSet = ejbSelectInternalRetrieveAETs(pk);
	        if (iAetSet.remove(null))
	            log.warn("Series[iuid=" + getSeriesIuid()
	                    + "] contains Instance(s) with unspecified Retrieve AET");
	        for (Iterator it = iAetSet.iterator(); it.hasNext();) {
	            final String aet = (String) it.next();
	            if (ejbSelectNumberOfSeriesRelatedInstancesWithInternalRetrieveAET(pk, aet) == numI)
	                sb.append(aet).append('\\');
	        }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
                aets = sb.toString();
            }
    	}
        if (updated = aets == null 
        		? getRetrieveAETs() != null 
        		: !aets.equals(getRetrieveAETs())) {
        	setRetrieveAETs(aets);
        }
        return updated;
    }
    
    private boolean updateExternalRetrieveAET(Integer pk, int numI) throws FinderException {
    	boolean updated = false;
    	String aet = null;
        if (numI > 0) {
	        Set eAetSet = ejbSelectExternalRetrieveAETs(getPk());
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
        int availability = numI > 0 ? ejbSelectAvailability(getPk()) 
        		: Availability.UNAVAILABLE;
        boolean updated;
		if (updated = availability != getAvailabilitySafe()) {
            setAvailability(availability);
        }
        return updated;
    }
    
    private boolean updateNumberOfInstances(Integer pk) throws FinderException {
    	boolean updated = false;
        final int numI = ejbSelectNumberOfSeriesRelatedInstances(pk);
        if (getNumberOfSeriesRelatedInstances() != numI) {
            setNumberOfSeriesRelatedInstances(numI);
            updated = true;
        }
        return updated;
    }
    
    private boolean updateFilesetId(Integer pk, int numI) throws FinderException {
    	boolean updated = false;
       	String fileSetId = null;
       	String fileSetIuid = null;
        if (numI > 0) {
	        if (ejbSelectNumberOfSeriesRelatedInstancesOnMediaWithStatus(pk, MediaDTO.COMPLETED) == numI) {
	            Set c = ejbSelectMediaWithStatus(pk, MediaDTO.COMPLETED);
	            if (c.size() == 1) {
	                MediaLocal media = (MediaLocal) c.iterator().next();
	                fileSetId = media.getFilesetId();
	                fileSetIuid = getFilesetIuid();
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

    /**
     * @ejb.interface-method
     */
    public boolean updateDerivedFields(boolean numOfInstances,
    		boolean retrieveAETs, boolean externalRettrieveAETs,
            boolean filesetId, boolean availibility) throws FinderException {
    	boolean updated = false;
    	final Integer pk = getPk();
		if (numOfInstances)
			if (updateNumberOfInstances(pk)) updated = true;
    	final int numI = getNumberOfSeriesRelatedInstances();
		if (retrieveAETs)
			if (updateRetrieveAETs(pk, numI)) updated = true;
		if (externalRettrieveAETs)
			if (updateExternalRetrieveAET(pk, numI)) updated = true;
		if (filesetId)
			if (updateFilesetId(pk, numI)) updated = true;
		if (availibility)
			if (updateAvailability(pk, numI)) updated = true;
		return updated;
    }
    
    private void updateMpps() {
        final String ppsiuid = getPpsIuid();
        MPPSLocal mpps = null;
        if (ppsiuid != null) try {
            mpps = mppsHome.findBySopIuid(ppsiuid);
            setHidden(mpps.isIncorrectWorklistEntrySelected());
        } catch (ObjectNotFoundException ignore) {
        } catch (FinderException e) {
            throw new EJBException(e);
        }
        setMpps(mpps);
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }

    /**
     * 
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setSeriesIuid(ds.getString(Tags.SeriesInstanceUID));
        setSeriesNumber(ds.getString(Tags.SeriesNumber));
        setModality(ds.getString(Tags.Modality));
        try {
	        setPpsStartDateTime(ds
	                .getDateTime(Tags.PPSStartDate, Tags.PPSStartTime));
        } catch (IllegalArgumentException e) {
            log.warn("Illegal Pps Date/Time format: " + e.getMessage());
        }
        Dataset refPPS = ds.getItem(Tags.RefPPSSeq);
        if (refPPS != null) {
            final String ppsUID = refPPS.getString(Tags.RefSOPInstanceUID);
            setPpsIuid(ppsUID);
        }
        Dataset tmp = ds.subSet(SUPPL_TAGS, true, true);
        setEncodedAttributes(DatasetUtils.toByteArray(tmp,
                DcmDecodeParam.EVR_LE));
    }

    /**
     * @ejb.interface-method
     */
    public void setPpsStartDateTime(java.util.Date date) {
        setPpsStartDateTime(date != null ? new java.sql.Timestamp(date
                .getTime()) : null);
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
            ds.putUL(PrivateTags.SeriesPk, getPk().intValue());
            ds.putAE(PrivateTags.CallingAET, getSourceAET());
            ds.putIS(Tags.NumberOfSeriesRelatedInstances,
                    getNumberOfSeriesRelatedInstances());
            ds.putSH(Tags.StorageMediaFileSetID, getFilesetId());
            ds.putUI(Tags.StorageMediaFileSetUID, getFilesetIuid());
            DatasetUtils.putRetrieveAET(ds, getRetrieveAETs(),
            		getExternalRetrieveAET());
            ds.putCS(Tags.InstanceAvailability,
            		Availability.toString(getAvailabilitySafe()));
        }
        return ds;
    }

    /**
     * 
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        return "Series[pk=" + getPk() + ", uid=" + getSeriesIuid()
                + ", study->" + getStudy() + "]";
    }

}