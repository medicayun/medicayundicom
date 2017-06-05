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

import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.RemoveException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.PersonName;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.conf.AttributeFilter;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.util.Convert;

/**
 * @ejb.bean name="Patient" type="CMP" view-type="local"
 *           local-jndi-name="ejb/Patient" primkey-field="pk"
 * @ejb.transaction type="Required"
 * @ejb.persistence table-name="patient"
 * @jboss.entity-command name="hsqldb-fetch-key"
 * @jboss.audit-created-time field-name="createdTime"
 * @jboss.audit-updated-time field-name="updatedTime"
 * 
 * @ejb.finder signature="Collection findAll()"
 *             query="SELECT OBJECT(a) FROM Patient AS a"
 *             transaction-type="Supports"
 * @ejb.finder signature="Collection findAll(int offset, int limit)"
 *             query=""
 *             transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findAll(int offset, int limit)"
 *              query="SELECT OBJECT(a) FROM Patient AS a ORDER BY a.pk OFFSET ?1 LIMIT ?2"
 *              
 * @ejb.finder signature="java.util.Collection findByPatientId(java.lang.String pid)"
 *             query="SELECT OBJECT(a) FROM Patient AS a WHERE a.patientId = ?1"
 *             transaction-type="Supports"
 * @jboss.query signature="java.util.Collection findByPatientId(java.lang.String pid)"
 *              strategy="on-find" eager-load-group="*"
 * 
 * @ejb.finder signature="java.util.Collection findByPatientIdWithIssuer(java.lang.String pid, java.lang.String issuer)"
 *             query="SELECT OBJECT(a) FROM Patient AS a WHERE a.patientId = ?1 AND (a.issuerOfPatientId IS NULL OR a.issuerOfPatientId = ?2)"
 *             transaction-type="Supports"
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
     * @ejb.persistence column-name="pk"
     * @jboss.persistence auto-increment="true"
     */
    public abstract Long getPk();

    public abstract void setPk(Long pk);

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
     * Patient ID
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_id"
     */
    public abstract String getPatientId();

    public abstract void setPatientId(String pid);

    /**
     * Patient ID Issuer
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_id_issuer"
     */
    public abstract String getIssuerOfPatientId();

    /**
     * @ejb.interface-method
     */
    public abstract void setIssuerOfPatientId(String issuer);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_name"
     */
    public abstract String getPatientName();
    public abstract void setPatientName(String name);
        
    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_i_name"
     */
    public abstract String getPatientIdeographicName();
    public abstract void setPatientIdeographicName(String name);

    /**
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_p_name"
     */
    public abstract String getPatientPhoneticName();
    public abstract void setPatientPhoneticName(String name);

    /**
     * Patient Birth Date
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_birthdate"
     */
    public abstract java.sql.Timestamp getPatientBirthDate();

    /**
     * @ejb.interface-method
     */
    public abstract void setPatientBirthDate(java.sql.Timestamp date);

    /**
     * Patient Sex
     *
     * @ejb.interface-method
     * @ejb.persistence column-name="pat_sex"
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
     * @return Patient, with which this Patient was merged.
     *
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="merged-patients"
     *    role-name="dereferenced-patient"
     *    target-role-name="dominant-patient"
     *    target-ejb="Patient"
     *    target-multiple="yes"
     *    cascade-delete="yes"
     *
     * @jboss.relation fk-column="merge_fk" related-pk-field="pk"
     */
    public abstract PatientLocal getMergedWith();

    /**
     * @param mergedWith, Patient, with which this Patient was merged.
     *
     * @ejb.interface-method
     */
    public abstract void setMergedWith(PatientLocal mergedWith);

    /**
     * @ejb.interface-method view-type="local"
     *
     * @param studies all studies of this patient
     */
    public abstract void setStudies(java.util.Collection studies);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="patient-study" role-name="patient-has-studies"
     *    
     * @return all studies of this patient
     */
    public abstract java.util.Collection getStudies();

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract void setMwlItems(java.util.Collection mwlItems);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="patient-mwlitems" role-name="patient-has-mwlitems"
     */
    public abstract java.util.Collection getMwlItems();

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract void setMpps(java.util.Collection mpps);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="patient-mpps" role-name="patient-has-mpps"
     */
    public abstract java.util.Collection getMpps();

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract void setGppps(java.util.Collection mpps);

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="patient-gppps" role-name="patient-has-gppps"
     */
    public abstract java.util.Collection getGppps();

    /**
     * @ejb.interface-method view-type="local"
     * @ejb.relation name="patient-gpsps" role-name="patient-has-gpsps"
     */
    public abstract java.util.Collection getGsps();

    /**
     * @ejb.interface-method view-type="local"
     */
    public abstract void setGsps(java.util.Collection gsps);

    /**
     * Create patient.
     *
     * @ejb.create-method
     */
    public Long ejbCreate(Dataset ds) throws CreateException {
        setAttributes(ds);
        return null;
    }

    public void ejbPostCreate(Dataset ds) throws CreateException {
        log.info("Created " + prompt());
    }

    public void ejbRemove() throws RemoveException {
        log.info("Deleting " + prompt());
// we have to delete studies explicitly here due to an foreign key constrain error 
// if an mpps key is set in one of the series.
        for ( Iterator iter = getStudies().iterator() ; iter.hasNext() ; ) {
        	StudyLocal study = (StudyLocal) iter.next();
        	iter.remove(); 
        	study.remove();
        }
    }

    /**
     * @ejb.interface-method
     */
    public Dataset getAttributes(boolean supplement) {
        Dataset ds = DatasetUtils.fromByteArray(getEncodedAttributes());
        if (supplement) {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putOB(PrivateTags.PatientPk, Convert.toBytes(getPk().longValue()));
        }
        return ds;
    }

    /**
     * @ejb.interface-method
     */
    public void setAttributes(Dataset ds) {
        String cuid = ds.getString(Tags.SOPClassUID);
        AttributeFilter filter = AttributeFilter.getPatientAttributeFilter(cuid);
        setAttributesInternal(filter.filter(ds), filter.getTransferSyntaxUID());
    }

    private void setAttributesInternal(Dataset ds, String tsuid) {
        setPatientId(ds.getString(Tags.PatientID));
        setIssuerOfPatientId(ds.getString(Tags.IssuerOfPatientID));
        PersonName pn = ds.getPersonName(Tags.PatientName);
        if (pn != null) {
            setPatientName(toUpperCase(pn.toComponentGroupString(false)));
            PersonName ipn = pn.getIdeographic();
            if (ipn != null) {
                setPatientIdeographicName(ipn.toComponentGroupString(false));
            }
            PersonName ppn = pn.getPhonetic();
            if (ppn != null) {
                setPatientPhoneticName(ppn.toComponentGroupString(false));
            }
        }
        try {
	        setPatientBirthDate(ds.getDate(Tags.PatientBirthDate));
	    } catch (IllegalArgumentException e) {
	        log.warn("Illegal Patient Birth Date format: " + e.getMessage());
	    }
        setPatientSex(ds.getString(Tags.PatientSex));
        byte[] b = DatasetUtils.toByteArray(ds, tsuid);
        if (log.isDebugEnabled()) {
            log.debug("setEncodedAttributes(byte[" + b.length + "])");
        }
        setEncodedAttributes(b);
    }
    
    /**
     * @throws DcmServiceException 
     * @ejb.interface-method
     */
    public void coerceAttributes(Dataset ds, Dataset coercedElements)
    throws DcmServiceException {
        Dataset attrs = getAttributes(false);
        String cuid = ds.getString(Tags.SOPClassUID);
        AttributeFilter filter = AttributeFilter.getPatientAttributeFilter(cuid);
        AttrUtils.coerceAttributes(attrs, ds, coercedElements, filter, log);
        if (AttrUtils.updateAttributes(attrs, filter.filter(ds), log)) {
            setAttributesInternal(attrs, filter.getTransferSyntaxUID());
        }
    }

    private static String toUpperCase(String s) {
        return s != null ? s.toUpperCase() : null;
    }

    /**
     * @ejb.interface-method
     */
    public void setPatientBirthDate(java.util.Date date) {
        setPatientBirthDate(date != null ? new java.sql.Timestamp(date
                .getTime()) : null);
    }

    /**
     * 
     * @ejb.interface-method
     */
    public String asString() {
        return prompt();
    }

    private String prompt() {
        return "Patient[pk=" + getPk() + ", pid=" + getPatientId() + ", name="
                + getPatientName() + "]";
    }
}