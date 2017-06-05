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
import java.util.HashSet;
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
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.CodeLocal;
import org.dcm4chex.archive.ejb.interfaces.CodeLocalHome;
import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.ejb.interfaces.MediaLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;

/**
 * Instance Bean
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1491 $ $Date: 2005-02-07 08:40:46 +0800 (周一, 07 2月 2005) $
 * 
 * @ejb.bean name="Instance"
 *           type="CMP"
 *           view-type="local"
 *  		 primkey-field="pk"
 *  		 local-jndi-name="ejb/Instance"
 * @ejb.transaction type="Required"
 * @ejb.persistence table-name="instance"
 * @jboss.load-group name="most"
 * @jboss.eager-load-group name="most"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * 
 * @ejb.finder signature="org.dcm4chex.archive.ejb.interfaces.InstanceLocal findBySopIuid(java.lang.String uid)"
 *             query="SELECT OBJECT(i) FROM Instance AS i WHERE i.sopIuid = ?1"
 *             transaction-type="Supports"
 * @jboss.query signature="org.dcm4chex.archive.ejb.interfaces.InstanceLocal findBySopIuid(java.lang.String uid)"
 *              strategy="on-find"
 *              eager-load-group="most"
 * 
 * @ejb.finder signature="java.util.Collection findNotOnMediaAndStudyReceivedBefore(java.sql.Timestamp receivedBefore)"
 *             query="SELECT OBJECT(i) FROM Instance AS i WHERE i.media IS NULL AND i.series.hidden = false AND i.series.study.createdTime < ?1"
 *             transaction-type="Supports"
 *
 * @ejb.finder signature="java.util.Collection findByPatientAndSopCuid(org.dcm4chex.archive.ejb.interfaces.PatientLocal patient, java.lang.String uid)"
 *             query="SELECT OBJECT(i) FROM Instance AS i WHERE i.series.hidden = false AND i.series.study.patient = ?1 AND i.sopCuid = ?2"
 *             transaction-type="Supports"
 *
 * @ejb.finder signature="java.util.Collection findByPatientAndSrCode(org.dcm4chex.archive.ejb.interfaces.PatientLocal patient, org.dcm4chex.archive.ejb.interfaces.CodeLocal srcode)"
 *             query="SELECT OBJECT(i) FROM Instance AS i WHERE i.series.hidden = false AND i.series.study.patient = ?1 AND i.srCode = ?2"
 *             transaction-type="Supports"
 *
 * @ejb.ejb-ref ejb-name="Code"
 *              view-type="local"
 *              ref-name="ejb/Code"
 *
 */
public abstract class InstanceBean implements EntityBean {

    private static final Logger log = Logger.getLogger(InstanceBean.class);

    private static final int[] SUPPL_TAGS = { Tags.RetrieveAET,
    	Tags.InstanceAvailability, Tags.StorageMediaFileSetID,
    	Tags.StorageMediaFileSetUID };
        
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
     * SOP Instance UID
     *
     * @ejb.persistence column-name="sop_iuid" 
     * @ejb.interface-method
     * @jboss.load-group name="most"
     */
    public abstract String getSopIuid();

    public abstract void setSopIuid(String iuid);

    /**
     * SOP Class UID
     *
     * @ejb.persistence column-name="sop_cuid"
     * @ejb.interface-method
     * @jboss.load-group name="most"
     *
     */
    public abstract String getSopCuid();

    public abstract void setSopCuid(String cuid);

    /**
     * Instance Number
     *
     * @ejb.persistence column-name="inst_no"
     * @ejb.interface-method
     */
    public abstract String getInstanceNumber();

    public abstract void setInstanceNumber(String no);

    /**
     * SR Completion Flag
     *
     * @ejb.persistence column-name="sr_complete"
     * @ejb.interface-method
     *
     */
    public abstract String getSrCompletionFlag();

    public abstract void setSrCompletionFlag(String flag);

    /**
     * SR Verification Flag
     *
     * @ejb.persistence
     *  column-name="sr_verified"
     * 
     * @ejb.interface-method
     *
     */
    public abstract String getSrVerificationFlag();

    public abstract void setSrVerificationFlag(String flag);

    /**
     * @ejb.persistence column-name="inst_attrs"
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="ext_retr_aet"
     * @jboss.load-group name="most"
     */
    public abstract String getExternalRetrieveAET();

    /**
     * @ejb.interface-method
     */ 
    public abstract void setExternalRetrieveAET(String aet);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="retrieve_aets"
     * @jboss.load-group name="most"
     */
    public abstract String getRetrieveAETs();

    public abstract void setRetrieveAETs(String aets);

    /**
     * @ejb.persistence column-name="availability"
     * @jboss.load-group name="most"
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
    
    /**
     * @ejb.interface-method
     */
    public abstract void setAvailability(int availability);

    /**
     * @ejb.persistence column-name="commitment"
     */
    public abstract boolean getCommitment();

    /**
     * @ejb.interface-method
     */
    public boolean getCommitmentSafe() {
        try {
            return getCommitment();
        } catch (NullPointerException npe) {
            return false;
        }
    }
    
    /**
     * @ejb.interface-method
     */
    public abstract void setCommitment(boolean commitment);

    /**
     * @ejb.relation name="series-instance"
     *               role-name="instance-of-series"
     *               cascade-delete="yes"
     * @jboss.relation fk-column="series_fk"
     *                 related-pk-field="pk"
     * @jboss.load-group name="most"
     * 
     * @param series series of this instance
     */
    public abstract void setSeries(SeriesLocal series);

    /**
     * @ejb.interface-method 
     * 
     * @return series of this series
     */
    public abstract SeriesLocal getSeries();

    /**
     * @ejb.relation name="instance-files"
     *               role-name="instance-in-files"
     *    
     * @ejb.interface-method
     * 
     * @return all files of this instance
     */
    public abstract java.util.Collection getFiles();

    public abstract void setFiles(java.util.Collection files);

    /**
     * @ejb.relation name="instance-media"
     *               role-name="instance-on-media"
     * @jboss.relation fk-column="media_fk" 
     *                 related-pk-field="pk"
     *    
     * @ejb.interface-method
     */
    public abstract MediaLocal getMedia();

    /**
     * @ejb.interface-method
     */
    public abstract void setMedia(MediaLocal media);

    /**
     * @ejb.relation name="instance-srcode"
     *               role-name="sr-with-title"
     *               target-ejb="Code"
     *               target-role-name="title-of-sr"
     *               target-multiple="yes"
     * @jboss.relation fk-column="srcode_fk"
     *                 related-pk-field="pk"
     * 
     * @param srCode code of SR title
     */
    public abstract void setSrCode(CodeLocal srCode);

    /**
     * @ejb.interface-method
     * 
     * @return code of SR title
     */
    public abstract CodeLocal getSrCode();

    /**
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, SeriesLocal series)
            throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds, SeriesLocal series)
            throws CreateException {
        try {
            setSrCode(CodeBean.valueOf(codeHome, ds
                    .getItem(Tags.ConceptNameCodeSeq)));
        } catch (CreateException e) {
            throw new CreateException(e.getMessage());
        } catch (FinderException e) {
            throw new CreateException(e.getMessage());
        }
        setSeries(series);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
    }

    /**
     * @ejb.select query="SELECT DISTINCT f.fileSystem.retrieveAET FROM Instance i, IN(i.files) f WHERE i.pk = ?1"
     */ 
    public abstract Set ejbSelectRetrieveAETs(Integer pk) throws FinderException;
    
    /**
     * @ejb.interface-method
     */
    public void addRetrieveAET(String aet) {
    	String s = getRetrieveAETs();
    	if (s == null) {
    		setRetrieveAETs(aet);
    	} else {
    		final Set aetSet = new HashSet(Arrays.asList(StringUtils.split(s, '\\')));
    		if (aetSet.add(aet))
    			setRetrieveAETs(toString(aetSet));
    	}   		
    }
    
    private String updateRetrieveAETs(Integer pk) throws FinderException {
        final Set aetSet = ejbSelectRetrieveAETs(pk);
        if (aetSet.remove(null))
            log.warn("Instance[iuid=" + getSopIuid()
                    + "] reference File(s) with unspecified Retrieve AET");
        final String aets = toString(aetSet);
        if (aets == null ? getRetrieveAETs() != null : !aets.equals(getRetrieveAETs()))
            setRetrieveAETs(aets);
        return aets;
    }
    
    private void updateAvailability(Integer pk, String retrieveAETs) throws FinderException {
        int availability = Availability.UNAVAILABLE;
        MediaLocal media;
        if (retrieveAETs != null)
            availability = Availability.ONLINE;
        else if (getExternalRetrieveAET() != null)
            availability = Availability.NEARLINE;
        else if ((media = getMedia()) != null 
                && media.getMediaStatus() == MediaDTO.COMPLETED)
            availability = Availability.OFFLINE;
        if (availability != getAvailabilitySafe()) {
            setAvailability(availability);
        }
    }
    
    /**
     * @ejb.interface-method
     */
    public void updateDerivedFields(boolean retrieveAETs, boolean availability) 
    		throws FinderException {
        final Integer pk = getPk();
        final String aets = retrieveAETs ? updateRetrieveAETs(pk) : getRetrieveAETs();
        if (availability)
        	updateAvailability(pk, aets);
    }

    private static String toString(Set s) {
    	if (s.isEmpty())
    		return null;
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
            ds.putUL(PrivateTags.InstancePk, getPk().intValue());
            MediaLocal media = getMedia();
            if (media != null && media.getMediaStatus() == MediaDTO.COMPLETED) {
                ds.putSH(Tags.StorageMediaFileSetID, media.getFilesetId());
                ds.putUI(Tags.StorageMediaFileSetUID, media.getFilesetIuid());            	
            }
            DatasetUtils.putRetrieveAET(ds, getRetrieveAETs(),
            		getExternalRetrieveAET());
            ds.putCS(Tags.InstanceAvailability, Availability
                    .toString(getAvailabilitySafe()));
        }
        return ds;
    }

    /**
     * 
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setSopIuid(ds.getString(Tags.SOPInstanceUID));
        setSopCuid(ds.getString(Tags.SOPClassUID));
        setInstanceNumber(ds.getString(Tags.InstanceNumber));
        setSrCompletionFlag(ds.getString(Tags.CompletionFlag));
        setSrVerificationFlag(ds.getString(Tags.VerificationFlag));
        Dataset tmp = ds.subSet(SUPPL_TAGS, true, true);
        setEncodedAttributes(DatasetUtils
                .toByteArray(tmp, DcmDecodeParam.EVR_LE));
    }

    /**
     * 
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        return "Instance[pk=" + getPk() + ", iuid=" + getSopIuid() + ", cuid="
                + getSopCuid() + ", series->" + getSeries() + "]";
    }
}
