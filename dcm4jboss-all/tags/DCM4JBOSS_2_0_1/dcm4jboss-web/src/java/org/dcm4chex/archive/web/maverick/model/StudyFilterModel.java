/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.model;


import org.dcm4che.dict.Tags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1227 $ $Date: 2004-11-27 01:49:22 +0800 (周六, 27 11月 2004) $
 * @since 05.10.2004
 *
 */
public class StudyFilterModel extends AbstractModel {

    public StudyFilterModel() {
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

    public final String getAccessionNumber() {
        return ds.getString(Tags.AccessionNumber);
    }

    public final void setAccessionNumber(String s) {
        ds.putSH(Tags.AccessionNumber, s);
    }

    public final String getStudyDateRange() {
        return getDateRange(Tags.StudyDate);
    }

    public final void setStudyDateRange(String s) {
        setDateRange(Tags.StudyDate, s);
    }

    public final String getStudyDescription() {
        return ds.getString(Tags.StudyDescription);
    }

    public final void setStudyDescription(String s) {
        ds.putLO(Tags.StudyDescription, s);
    }

    public final String getStudyID() {
        return ds.getString(Tags.StudyID);
    }

    public final void setStudyID(String s) {
        ds.putSH(Tags.StudyID, s);
    }

    public final String getModality() {
        return ds.getString(Tags.ModalitiesInStudy);
    }

    public final void setModality(String s) {
        ds.putCS(Tags.ModalitiesInStudy, s);
    }
    /**
     * Returns -1 because pk isnt use here.
     */
    public int getPk() {
    	return -1;
    }

}