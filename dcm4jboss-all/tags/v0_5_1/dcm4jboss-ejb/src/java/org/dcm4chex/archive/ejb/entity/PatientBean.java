/* $Id: PatientBean.java 973 2004-02-17 13:36:58Z umbertoc $
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

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.DatasetUtils;

/**
 * @ejb.bean
 *  name="Patient"
 *  type="CMP"
 *  view-type="local"
 *  primkey-field="pk"
 *  local-jndi-name="ejb/Patient"
 * 
 * @jboss.container-configuration
 *  name="Standard CMP 2.x EntityBean with cache invalidation"
 *  
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.persistence
 *  table-name="patient"
 * 
 * @jboss.entity-command
 *  name="hsqldb-fetch-key"
 * 
 * @ejb.finder
 *  signature="Collection findAll()"
 *  query="SELECT OBJECT(a) FROM Patient AS a"
 *  transaction-type="Supports"
 *
 * @ejb.finder
 *  signature="java.util.Collection findByPatientId(java.lang.String pid)"
 *  query="SELECT OBJECT(a) FROM Patient AS a WHERE a.patientId = ?1"
 *  transaction-type="Supports"
 *
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class PatientBean implements EntityBean {

    private static final Logger log = Logger.getLogger(PatientBean.class);

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
     * Patient ID
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pat_id"
     */
    public abstract String getPatientId();

    public abstract void setPatientId(String pid);

    /**
     * Patient ID Issuer
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pat_id_issuer"
     */
    public abstract String getIssuerOfPatientId();

    public abstract void setIssuerOfPatientId(String issuer);

    /**
     * Patient Name
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pat_name"
     */
    public abstract String getPatientName();

    /**
     * @ejb.interface-method
     * @param name
     */
    public abstract void setPatientName(String name);

    /**
     * Patient Birth Date
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pat_birthdate"
     */
    public abstract java.util.Date getPatientBirthDate();

    /**
     * @ejb.interface-method
     */
    public abstract void setPatientBirthDate(java.util.Date date);

    /**
     * Patient Sex
     *
     * @ejb.interface-method
     * @ejb.persistence
     *  column-name="pat_sex"
     */
    public abstract String getPatientSex();

    /**
     * @ejb.interface-method
     *
     */
    public abstract void setPatientSex(String sex);

    /**
     * Patient DICOM Attributes
     *
     * @ejb.persistence
     *  column-name="pat_attrs"
     * 
     */
    public abstract byte[] getEncodedAttributes();

    public abstract void setEncodedAttributes(byte[] bytes);

    /**
     * @ejb.interface-method view-type="local"
     *
     * @param studies all studies of this patient
     */
    public abstract void setStudies(java.util.Collection studies);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation
     *  name="patient-study"
     *  role-name="patient-has-studies"
     *    
     * @return all studies of this patient
     */
    public abstract java.util.Collection getStudies();

    /**
      * Create patient.
      *
      * @ejb.create-method
      */
    public Integer ejbCreate(Dataset ds) throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds) throws CreateException {
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
        setPatientId(ds.getString(Tags.PatientID));
        setIssuerOfPatientId(ds.getString(Tags.IssuerOfPatientID));
        setPatientName(ds.getString(Tags.PatientName));
        setPatientBirthDate(ds.getDate(Tags.PatientBirthDate));
        setPatientSex(ds.getString(Tags.PatientSex));
        setEncodedAttributes(
            DatasetUtils.toByteArray(ds, DcmDecodeParam.EVR_LE));
    }

    /**
     * 
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        return "Patient[pk="
            + getPk()
            + ", pid="
            + getPatientId()
            + ", name="
            + getPatientName()
            + "]";
    }
}
