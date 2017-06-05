/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.model;

import java.util.ArrayList;
import java.util.List;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.PrivateTags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
 * @since 05.10.2004
 *
 */
public class PatientModel extends AbstractModel {

    private List studies = new ArrayList();

    private int pk = -1;

    public PatientModel() {
    }

    public PatientModel(Dataset ds) {
        super(ds);
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        this.pk = ds.getInt(PrivateTags.PatientPk, -1);
    }

    public final int getPk() {
        return pk;
    }

    public final void setPk(int pk) {
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        ds.putUL(PrivateTags.PatientPk, pk);
        this.pk = pk;
    }

    public int hashCode() {
        return pk;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatientModel)) return false;
        PatientModel other = (PatientModel) o;
        return pk == other.pk;
    }

    public final String getIssuerOfPatientID() {
        return ds.getString(Tags.IssuerOfPatientID);
    }

    public final void setIssuerOfPatientID(String issuerOfPatientID) {
        ds.putLO(Tags.IssuerOfPatientID, issuerOfPatientID);
    }

    public final String getPatientBirthDate() {
        return getDate(Tags.PatientBirthDate);
    }

    public final void setPatientBirthDate(String s) {
        setDate(Tags.PatientBirthDate, s);
    }

    public final String getPatientID() {
        return ds.getString(Tags.PatientID);
    }

    public final void setPatientID(String patientID) {
        ds.putLO(Tags.PatientID, patientID);
    }

    public final String getPatientName() {
        return ds.getString(Tags.PatientName);
    }

    public final void setPatientName(String patientName) {
        ds.putPN(Tags.PatientName, patientName);
    }

    public final String getPatientSex() {
        return ds.getString(Tags.PatientSex);
    }

    public final void setPatientSex(String patientSex) {
        ds.putCS(Tags.PatientSex, patientSex);
    }

    public final List getStudies() {
        return studies;
    }

    public final void setStudies(List studies) {
        this.studies = studies;
    }
}