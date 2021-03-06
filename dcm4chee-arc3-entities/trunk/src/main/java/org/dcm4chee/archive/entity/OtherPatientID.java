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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4chee.archive.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author Damien Evans <damien.daddy@gmail.com>
 * @author Justin Falk <jfalkmu@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 28, 2008
 */
@Entity
@Table(name = "other_pid")
@NamedQuery(name="OtherPatientID.findByPatientIdAndIssuer",
  query="select opid from OtherPatientID opid where patientID = :pid and issuerOfPatientID = :issuer"
)
public class OtherPatientID extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -7983218873187437331L;

    @Column(name = "pat_id", nullable = false)
    private String patientID;

    @Column(name = "pat_id_issuer", nullable = false)
    private String issuerOfPatientID;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rel_pat_other_pid", 
            joinColumns = 
                @JoinColumn(name = "other_pid_fk", referencedColumnName = "pk"), 
            inverseJoinColumns = 
                @JoinColumn(name = "patient_fk", referencedColumnName = "pk"))
    private Set<Patient> patients;

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    public void setIssuerOfPatientID(String issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    public Set<Patient> getPatients() {
        return patients;
    }

    public void setPatients(Set<Patient> patients) {
        this.patients = patients;
    }

    @Override
    public String toString() {
        return issuerOfPatientID != null
                ? patientID + "^^^" + issuerOfPatientID
                : patientID;
    }

    public void setAttributes(DicomObject attrs) {
        this.patientID = attrs.getString(Tag.PatientID);
        this.issuerOfPatientID = attrs.getString(Tag.IssuerOfPatientID);
    }

    /**
     * Override hashCode.
     * 
     * @return the Objects hashcode.
     */
    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode = 31 * hashCode + (int) (+pk ^ (pk >>> 32));
        hashCode = 31 * hashCode
                + (patientID == null ? 0 : patientID.hashCode());
        hashCode = 31
                * hashCode
                + (issuerOfPatientID == null ? 0 : issuerOfPatientID.hashCode());
        return hashCode;
    }

    /**
     * Returns <code>true</code> if this <code>OtherPatientID</code> is the
     * same as the o argument.
     * 
     * @return <code>true</code> if this <code>OtherPatientID</code> is the
     *         same as the o argument.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        OtherPatientID castedObj = (OtherPatientID) o;
        return ((this.pk == castedObj.pk)
                && (this.patientID == null ? castedObj.patientID == null
                        : this.patientID.equals(castedObj.patientID)) && (this.issuerOfPatientID == null ? castedObj.issuerOfPatientID == null
                : this.issuerOfPatientID.equals(castedObj.issuerOfPatientID)));
    }

}
