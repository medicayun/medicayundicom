/* $Id: SeriesBean.java 962 2004-02-11 15:04:04Z gunterze $
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;

/**

/**
 * @ejb.bean
 *  name="Series"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Series"
 * 
 * @jboss.container-configuration
 *  name="Standard CMP 2.x EntityBean with cache invalidation"
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
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Series AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="org.dcm4chex.archive.ejb.interfaces.SeriesLocal findBySeriesIuid(java.lang.String uid)"
 *  query="SELECT OBJECT(a) FROM Series AS a WHERE a.seriesIuid = ?1"
 *  transaction-type="Supports"
 *
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class SeriesBean implements EntityBean {

    private static final Logger log = Logger.getLogger(SeriesBean.class);
    private Set retrieveAETSet;

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

    public abstract void setPpsStartDateTime(java.util.Date datetime);

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
        setStudy(study);
        study.addModalityInStudy(getModality());
        study.incNumberOfStudyRelatedSeries(1);
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
        // study.updateModalitiesInStudy();?
        getStudy().incNumberOfStudyRelatedSeries(-1);
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
        getStudy().incNumberOfStudyRelatedInstances(inc);
    }

    /**
     * @ejb.interface-method
     */
    public Set getRetrieveAETSet() {
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
    public boolean addRetrieveAET(String aet) {
        log.debug(
            "series[pk="
                + getPk()
                + "]: update retrieveAETs "
                + getRetrieveAETs()
                + " with "
                + aet);
        if (getRetrieveAETSet().contains(aet)) {
            log.debug(
                "series[pk="
                    + getPk()
                    + "]: no update of retrieveAETs "
                    + getRetrieveAETSet()
                    + " necessary");
            return false;
        }
        if (!areAllInstancesRetrieveableFrom(aet)) {
            log.debug(
                "series[pk="
                    + getPk()
                    + "]: not all Instances retrieveable from "
                    + aet);
            return false;
        }
        retrieveAETSet.add(aet);
        String prev = getRetrieveAETs();
        if (prev == null || prev.length() == 0) {
            setRetrieveAETs(aet);
        } else {
            setRetrieveAETs(prev + '\\' + aet);
        }
        log.debug(
            "series[pk="
                + getPk()
                + "]: updated retrieveAETs to "
                + getRetrieveAETs());
        return true;
    }

    private boolean areAllInstancesRetrieveableFrom(String aet) {
        Collection c = getInstances();
        for (Iterator it = c.iterator(); it.hasNext();) {
            InstanceLocal instance = (InstanceLocal) it.next();
            if (!instance.getRetrieveAETSet().contains(aet)) {
                return false;
            }
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
