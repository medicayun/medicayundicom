/* $Id: SeriesBean.java 1105 2004-05-02 19:13:35Z gunterze $
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.MPPSLocal;
import org.dcm4chex.archive.ejb.interfaces.MPPSLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;

/**
 * @ejb.bean
 *  name="Series"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Series"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="series"
 * 
 * @jboss.entity-command
 *  name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="java.util.Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Series AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.SeriesLocal findBySeriesIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(a) FROM Series AS a WHERE a.seriesIuid = ?1"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="java.util.Collection findByPpsIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(a) FROM Series AS a WHERE a.ppsIuid = ?1"
 *  transaction-type="Supports"
 * 
 * @ejb.finder
 *  signature="java.util.Collection findWithNoPpsIuid()"
 *  query="SELECT OBJECT(a) FROM Series AS a WHERE a.ppsIuid IS NULL"
 *  transaction-type="Supports"
 * 
 * @ejb.ejb-ref
 *  ejb-name="MPPS" 
 *  view-type="local"
 *  ref-name="ejb/MPPS"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class SeriesBean implements EntityBean {

    private static final Logger log = Logger.getLogger(SeriesBean.class);
    private Set retrieveAETSet;
    private MPPSLocalHome mppsHome;

    public void setEntityContext(EntityContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            mppsHome = (MPPSLocalHome) jndiCtx.lookup("java:comp/env/ejb/MPPS");
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
        mppsHome = null;
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
     * Series Instance UID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="series_iuid"
     */
    public abstract String getSeriesIuid();

    public abstract void setSeriesIuid(String uid);

    /**
     * Series Number
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="series_no"
     */
    public abstract String getSeriesNumber();

    public abstract void setSeriesNumber(String no);

    /**
     * Modality
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="modality"
     */
    public abstract String getModality();

    public abstract void setModality(String md);

    /**
     * PPS Start Datetime
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pps_start"
     */
    public abstract java.util.Date getPpsStartDateTime();

    public abstract void setPpsStartDateTime(java.util.Date datetime);

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
     * @ejb.persistence
     *  column-name="num_instances"
     * 
     */
    public abstract int getNumberOfSeriesRelatedInstances();

    public abstract void setNumberOfSeriesRelatedInstances(int num);

    /**
     * Encoded Series Dataset
     *
     * @ejb.persistence
     *  column-name="series_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] attr);

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
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="availability"
     */
    public abstract int getAvailability();

    public abstract void setAvailability(int availability);

    /**
     * Hidden Flag
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="hidden"
     */
    public abstract boolean getHidden();

    public abstract void setHidden(boolean hidden);

    /**
     * @ejb.relation
     *  name="study-series"
     *  role-name="series-of-study"
     *  cascade-delete="yes"
     *
     * @jboss:relation
     *  fk-column="study_fk"
     *  related-pk-field="pk"
     * 
     * @param study study of this series
     */
    public abstract void setStudy(StudyLocal study);

    /**
     * @ejb.interface-method view-type="local"
     * 
     * @return study of this series
     */
    public abstract StudyLocal getStudy();

    /**
     * @ejb.relation
     *  name="mpps-series"
     *  role-name="series-of-mpps"
     *
     * @jboss:relation
     *  fk-column="mpps_fk"
     *  related-pk-field="pk"
     * 
     * @param study study of this series
     */
    public abstract void setMpps(MPPSLocal mpps);

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract MPPSLocal getMpps();

    /**
     * @ejb.interface-method view-type="local"
     *
     * @param series all instances of this series
     */
    public abstract void setInstances(java.util.Collection series);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation
     *  name="series-instance"
     *  role-name="series-has-instance"
     *    
     * @return all instances of this series
     */
    public abstract java.util.Collection getInstances();

    public void ejbLoad() {
        retrieveAETSet = null;
    }

    /**
     * Create series.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, StudyLocal study)
        throws CreateException {
        retrieveAETSet = null;
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds, StudyLocal study)
        throws CreateException {
        updateMpps();
        setStudy(study);
        study.addModalityInStudy(getModality());
        study.incNumberOfStudyRelatedSeries(1);
        log.info("Created " + prompt());
    }

    private void updateMpps() {
        final String ppsiuid = getPpsIuid();
        MPPSLocal mpps = null;
        if (ppsiuid != null)
            try {
                mpps = mppsHome.findBySopIuid(ppsiuid);
            } catch (ObjectNotFoundException ignore) {} catch (FinderException e) {
                throw new EJBException(e);
            }
        setMpps(mpps);
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
        StudyLocal study = getStudy();
        if (study != null) {
            // study.updateModalitiesInStudy();?
            study.incNumberOfStudyRelatedSeries(-1);
            study.incNumberOfStudyRelatedInstances(
                -getNumberOfSeriesRelatedInstances());
        }
    }

    /**
     * 
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        setSeriesIuid(ds.getString(Tags.SeriesInstanceUID));
        setSeriesNumber(ds.getString(Tags.SeriesNumber));
        setModality(ds.getString(Tags.Modality));
        setPpsStartDateTime(
            ds.getDateTime(Tags.PPSStartDate, Tags.PPSStartTime));
        setEncodedAttributes(
            DatasetUtils.toByteArray(ds, DcmDecodeParam.EVR_LE));
        Dataset refPPS = ds.getItem(Tags.RefPPSSeq);
        if (refPPS != null) {
            final String ppsUID = refPPS.getString(Tags.RefSOPInstanceUID);
            setPpsIuid(ppsUID);
        }
    }

    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes() {
        return DatasetUtils.fromByteArray(
            getEncodedAttributes(),
            DcmDecodeParam.EVR_LE);
    }

    /**
     * @ejb.interface-method
     */
    public void incNumberOfSeriesRelatedInstances(int inc) {
        setNumberOfSeriesRelatedInstances(
            getNumberOfSeriesRelatedInstances() + inc);
        if (!getHidden()) {
            getStudy().incNumberOfStudyRelatedInstances(inc);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void hide() {
        if (getHidden())
            return;
        StudyLocal study = getStudy();
        study.incNumberOfStudyRelatedSeries(-1);
        study.incNumberOfStudyRelatedInstances(
            -getNumberOfSeriesRelatedInstances());
        setHidden(true);
    }

    /**
     * @ejb.interface-method
     */
    public void unhide() {
        if (!getHidden())
            return;
        StudyLocal study = getStudy();
        study.incNumberOfStudyRelatedSeries(1);
        study.incNumberOfStudyRelatedInstances(
            getNumberOfSeriesRelatedInstances());
        setHidden(false);
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
            if (aets != null) {
                retrieveAETSet.addAll(
                    Arrays.asList(StringUtils.split(aets, '\\')));
            }
        }
        return retrieveAETSet;
    }

    /**
     * @ejb.interface-method
     */
    public boolean updateRetrieveAETs() {
        Collection c = getInstances();
        HashSet newAETSet = new HashSet();
        Iterator it = c.iterator();
        if (it.hasNext()) {
            newAETSet.addAll(((InstanceLocal) it.next()).getRetrieveAETSet());
            while (it.hasNext()) {
                newAETSet.retainAll(
                    ((InstanceLocal) it.next()).getRetrieveAETSet());
            }
        }
        if (retrieveAETSet().equals(newAETSet)) {
            return false;
        }
        retrieveAETSet = newAETSet;
        String newAETs =
            StringUtils.toString(
                (String[]) retrieveAETSet().toArray(
                    new String[retrieveAETSet.size()]),
                '\\');
        setRetrieveAETs(newAETs);
        return true;
    }

    /**
     * 
     * @ejb.interface-method
     */
    public boolean updateAvailability() {
        Collection c = getInstances();
        int availability = 0;
        for (Iterator it = c.iterator(); it.hasNext();) {
            InstanceLocal instance = (InstanceLocal) it.next();
            availability = Math.max(availability, instance.getAvailability());
        }
        if (availability != getAvailability()) {
            return false;
        }
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
        return "Series[pk="
            + getPk()
            + ", uid="
            + getSeriesIuid()
            + ", study->"
            + getStudy()
            + "]";
    }

}
