/* $Id: StudyBean.java 1021 2004-02-29 21:49:36Z gunterze $
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
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;

/**
 * @ejb.bean
 *  name="Study"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Study"
 * 
 * @jboss.container-configuration
 *  name="Standard CMP 2.x EntityBean with cache invalidation"
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
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class StudyBean implements EntityBean {

    private static final Logger log = Logger.getLogger(StudyBean.class);
    private Set retrieveAETSet;
    private Set modalitySet;

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
    public abstract java.util.Date getStudyDateTime();

    public abstract void setStudyDateTime(java.util.Date dateTime);

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
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="availability"
     */
    public abstract int getAvailability();

    public abstract void setAvailability(int availability);
    
    /**
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

    public void ejbLoad() {
        retrieveAETSet = null;
        modalitySet = null;
    }

    /**
     * Create study.
     *
     * @ejb.create-method
     */
    public Integer ejbCreate(Dataset ds, PatientLocal patient)
        throws CreateException {
        retrieveAETSet = null;
        modalitySet = null;
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
    public void setAttributes(Dataset ds) {
        setStudyIuid(ds.getString(Tags.StudyInstanceUID));
        setStudyId(ds.getString(Tags.StudyID));
        setStudyDateTime(ds.getDateTime(Tags.StudyDate, Tags.StudyTime));
        setAccessionNumber(ds.getString(Tags.AccessionNumber));
        setReferringPhysicianName(ds.getString(Tags.ReferringPhysicianName));
        setEncodedAttributes(
            DatasetUtils.toByteArray(ds, DcmDecodeParam.EVR_LE));
    }

    /**
     * @ejb.interface-method
     */
    public void incNumberOfStudyRelatedSeries(int inc) {
        setNumberOfStudyRelatedSeries(getNumberOfStudyRelatedSeries() + inc);
    }

    /**
     * @ejb.interface-method
     */
    public void incNumberOfStudyRelatedInstances(int inc) {
        setNumberOfStudyRelatedInstances(
            getNumberOfStudyRelatedInstances() + inc);
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
    public boolean addRetrieveAET(String aet) {
        log.debug(
            "study[pk="
                + getPk()
                + "]: update retrieveAETs "
                + getRetrieveAETs()
                + " with "
                + aet);
        if (retrieveAETSet().contains(aet)) {
            log.debug(
                "study[pk="
                    + getPk()
                    + "]: no update of retrieveAETs "
                    + retrieveAETSet()
                    + " necessary");
            return false;
        }
        if (!areAllSeriesRetrieveableFrom(aet)) {
            log.debug(
                "study[pk="
                    + getPk()
                    + "]: not all Series retrieveable from "
                    + aet);
            return false;
        }
        retrieveAETSet().add(aet);
        String prev = getRetrieveAETs();
        if (prev == null || prev.length() == 0) {
            setRetrieveAETs(aet);
        } else {
            setRetrieveAETs(prev + '\\' + aet);
        }
        log.debug(
            "study[pk="
                + getPk()
                + "]: updated retrieveAETs to "
                + getRetrieveAETs());
        return true;
    }

    private boolean areAllSeriesRetrieveableFrom(String aet) {
        Collection c = getSeries();
        for (Iterator it = c.iterator(); it.hasNext();) {
            SeriesLocal series = (SeriesLocal) it.next();
            if (!series.getRetrieveAETSet().contains(aet)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * @ejb.interface-method
     */
    public boolean updateAvailability() {
        Collection c = getSeries();
        int availability = 0;
        for (Iterator it = c.iterator(); it.hasNext();) {
            SeriesLocal series = (SeriesLocal) it.next();
            availability = Math.max(availability, series.getAvailability());            
        }
        if (availability != getAvailability()) {
            setAvailability(availability);
            return true;
        }
        return false;
    }

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
     * @ejb.interface-method
     */
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
     * @ejb.interface-method
     */
    public boolean addModalityInStudy(String md) {
        if (getModalitySet().contains(md)) {
            return false;
        }
        modalitySet.add(md);
        String prev = getModalitiesInStudy();
        if (prev == null || prev.length() == 0) {
            setModalitiesInStudy(md);
        } else {
            setModalitiesInStudy(prev + '\\' + md);
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
        return "Study[pk="
            + getPk()
            + ", uid="
            + getStudyIuid()
            + ", patient->"
            + getPatient()
            + "]";
    }
}
