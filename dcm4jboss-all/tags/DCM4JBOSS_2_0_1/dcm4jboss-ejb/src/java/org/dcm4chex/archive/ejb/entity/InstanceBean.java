/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.entity;

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
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.CodeLocal;
import org.dcm4chex.archive.ejb.interfaces.CodeLocalHome;
import org.dcm4chex.archive.ejb.interfaces.MediaLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;

/**
 * Instance Bean
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1257 $ $Date: 2004-12-02 22:57:02 +0800 (周四, 02 12月 2004) $
 * 
 * @ejb.bean
 *  name="Instance"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Instance"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="instance"
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
 *  signature="org.dcm4chex.archive.ejb.interfaces.InstanceLocal findBySopIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(a) FROM Instance AS a WHERE a.sopIuid = ?1"
 *  transaction-type="Supports"
 * 
 * @jboss.query
 *  signature="org.dcm4chex.archive.ejb.interfaces.InstanceLocal findBySopIuid(java.lang.String uid)"
 *  strategy="on-find"
 *  eager-load-group="*"
 * 
 * @ejb.ejb-ref ejb-name="Code" view-type="local" ref-name="ejb/Code"
 *
 */
public abstract class InstanceBean implements EntityBean {

    private static final Logger log = Logger.getLogger(InstanceBean.class);

    private static final int[] SUPPL_TAGS = { Tags.RetrieveAET, Tags.InstanceAvailability };
        
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
     *  column-name="sop_iuid"
     * 
     * @ejb.interface-method
     *
     */
    public abstract String getSopIuid();

    public abstract void setSopIuid(String iuid);

    /**
     * SOP Class UID
     *
     * @ejb.persistence
     *  column-name="sop_cuid"
     * 
     * @ejb.interface-method
     *
     */
    public abstract String getSopCuid();

    public abstract void setSopCuid(String cuid);

    /**
     * Instance Number
     *
     * @ejb.persistence
     *  column-name="inst_no"
     * 
     * @ejb.interface-method
     *
     */
    public abstract String getInstanceNumber();

    public abstract void setInstanceNumber(String no);

    /**
     * SR Completion Flag
     *
     * @ejb.persistence
     *  column-name="sr_complete"
     * 
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
     * Instance DICOM Attributes
     *
     * @ejb.persistence
     *  column-name="inst_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

    /**
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="ext_retr_aet"
     */
    public abstract String getExternalRetrieveAET();

    public abstract void setExternalRetrieveAET(String aet);

    /**
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
     * Storage Commitment
     *
     * @ejb.persistence
     *  column-name="commitment"
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
     * @ejb.relation
     *  name="series-instance"
     *  role-name="instance-of-series"
     *  cascade-delete="yes"
     *
     * @jboss:relation
     *  fk-column="series_fk"
     *  related-pk-field="pk"
     * 
     * @param series series of this instance
     */
    public abstract void setSeries(SeriesLocal series);

    /**
     * @ejb.interface-method view-type="local"
     * 
     * @return series of this series
     */
    public abstract SeriesLocal getSeries();

    /**
     * @ejb.relation
     *  name="instance-files"
     *  role-name="instance-in-files"
     *    
     * @ejb.interface-method view-type="local"
     * 
     * @return all files of this instance
     */
    public abstract java.util.Collection getFiles();

    public abstract void setFiles(java.util.Collection files);

    /**
     * @ejb.relation
     *  name="instance-media"
     *  role-name="instance-on-media"
     *  target-ejb="Media"
     *  target-role-name="media-with-instance"
     * @jboss:relation fk-column="media_fk" related-pk-field="pk"
     *    
     * @ejb.interface-method view-type="local"
     */
    public abstract MediaLocal getMedia();

    public abstract void setMedia(MediaLocal media);

    /**
     * @ejb.relation
     *  name="instance-srcode"
     *  role-name="sr-with-title"
     *  target-ejb="Code"
     *  target-role-name="title-of-sr"
     *  target-multiple="yes"
     *
     * @jboss:relation
     *  fk-column="srcode_fk"
     *  related-pk-field="pk"
     * 
     * @param srCode code of SR title
     */
    public abstract void setSrCode(CodeLocal srCode);

    /**
     * @ejb.interface-method view-type="local"
     * 
     * @return code of SR title
     */
    public abstract CodeLocal getSrCode();

    /**
     * Create Instance.
     *
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
    public void updateDerivedFields() throws FinderException {
        final Integer pk = getPk();
        Set aetSet = ejbSelectRetrieveAETs(pk);
        String aets = toString(aetSet);
        final String extAet = getExternalRetrieveAET();
        if (extAet != null && extAet.length() != 0)
            aets = aets.length() == 0 ? extAet : aets + '\\' + extAet;
        if (!aets.equals(getRetrieveAETs()))
            setRetrieveAETs(aets);
    }

    private static String toString(Set s) {
        String[] a = (String[]) s.toArray(new String[s.size()]);
        return StringUtils.toString(a, '\\');
    }
    
    /**
     * @ejb.interface-method
     */
    public boolean updateAvailability(int availability) {
        if (availability != getAvailabilitySafe()) {
            setAvailability(availability);
            return true;
        }
        return false;
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
            ds.putAE(Tags.RetrieveAET, StringUtils.split(getRetrieveAETs(),'\\'));
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
        Dataset tmp = ds.exclude(SUPPL_TAGS).excludePrivate();
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
