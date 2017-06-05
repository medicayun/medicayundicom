/* $Id: InstanceBean.java 1133 2004-06-15 11:13:47Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.entity;

import java.util.Arrays;
import java.util.Collections;
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
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.interfaces.CodeLocal;
import org.dcm4chex.archive.ejb.interfaces.CodeLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;

/**
 * Instance Bean
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1133 $ $Date: 2004-06-15 19:13:47 +0800 (周二, 15 6月 2004) $
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
 * @ejb.ejb-ref ejb-name="Code" view-type="local" ref-name="ejb/Code"
 *
 */
public abstract class InstanceBean implements EntityBean {

    private static final Logger log = Logger.getLogger(InstanceBean.class);

    private CodeLocalHome codeHome;

    private Set retrieveAETSet;

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

    public void ejbLoad() {
        retrieveAETSet = null;
    }

    /**
     * Create Instance.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, SeriesLocal series)
            throws CreateException {
        retrieveAETSet = null;
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
        series.incNumberOfSeriesRelatedInstances(1);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
        SeriesLocal series = getSeries();
        if (series != null) {
            series.incNumberOfSeriesRelatedInstances(-1);
        }
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
    public Dataset getAttributes() {
        return DatasetUtils.fromByteArray(getEncodedAttributes(),
                DcmDecodeParam.EVR_LE);
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
        setEncodedAttributes(DatasetUtils
                .toByteArray(ds, DcmDecodeParam.EVR_LE));
    }

    /**
     * @ejb.interface-method
     */
    public Set getRetrieveAETSet() {
        return Collections.unmodifiableSet(retrieveAETSet());
    }

    private Set retrieveAETSet() {
        if (retrieveAETSet == null) {
            retrieveAETSet = new HashSet();
            String aets = getRetrieveAETs();
            if (aets != null)
                retrieveAETSet.addAll(Arrays.asList(StringUtils.split(aets,
                        '\\')));
        }
        return retrieveAETSet;
    }

    /**
     * @ejb.interface-method
     */
    public boolean addRetrieveAETs(String[] aets) {
        if (!retrieveAETSet().addAll(Arrays.asList(aets))) return false;
        setRetrieveAETs(StringUtils.toString((String[]) retrieveAETSet()
                .toArray(new String[retrieveAETSet.size()]), '\\'));
        return true;
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
