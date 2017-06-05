/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.archive.web.maverick.model.InstanceModel;
import org.dcm4chex.archive.web.maverick.model.PatientModel;
import org.dcm4chex.archive.web.maverick.model.SeriesModel;
import org.dcm4chex.archive.web.maverick.model.StudyFilterModel;
import org.dcm4chex.archive.web.maverick.model.StudyModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
 * @since 28.01.2004
 */
public class FolderForm {

    static final String FOLDER_ATTRNAME = "folderFrom";

    static FolderForm getFolderForm(HttpServletRequest request) {
        FolderForm form = (FolderForm) request.getSession()
                .getAttribute(FOLDER_ATTRNAME);
        if (form == null) {
            form = new FolderForm();
            request.getSession().setAttribute(FOLDER_ATTRNAME, form);
        }
        return form;
    }

    public static final int LIMIT = 10;

    private String patientID;

    private String patientName;

    private String accessionNumber;

    private String studyID;

    private String studyDateRange;

    private String modality;

    private StudyFilterModel studyFilter = null;

    private List patients;

    private List aets;

    private String destination;

    private final Set stickyPatients = new HashSet();

    private final Set stickyStudies = new HashSet();

    private final Set stickySeries = new HashSet();

    private final Set stickyInstances = new HashSet();

    private int offset;

    private int total;

    public final int getLimit() {
        return LIMIT;
    }

    public final String getAccessionNumber() {
        return accessionNumber;
    }

    public final void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public final String getModality() {
        return modality;
    }

    public final void setModality(String modality) {
        this.modality = modality;
    }

    public final String getPatientID() {
        return patientID;
    }

    public final void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public final String getPatientName() {
        return patientName;
    }

    public final void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public final String getStudyDateRange() {
        return studyDateRange;
    }

    public final void setStudyDateRange(String studyDateRange) {
        this.studyDateRange = studyDateRange;
    }

    public final String getStudyID() {
        return studyID;
    }

    public final void setStudyID(String studyID) {
        this.studyID = studyID;
    }

    public final void setTotal(int total) {
        this.total = total;
    }

    public final Set getStickyInstances() {
        return stickyInstances;
    }

    public final Set getStickyPatients() {
        return stickyPatients;
    }
    
    public final Set getStickySeries() {
        return stickySeries;
    }

    public final Set getStickyStudies() {
        return stickyStudies;
    }

    public final int getOffset() {
        return offset;
    }

    public final List getPatients() {
        return patients;
    }

    public final List getAets() {
        return aets;
    }

    public final void setAets(List aets) {
        this.aets = aets;
    }

    public final String getDestination() {
        return destination;
    }

    public final void setDestination(String destination) {
        this.destination = destination;
    }

    public final void setFilter(String filter) {
        offset = 0;
        total = -1;
        studyFilter = null;
    }

    public final void setNext(String next) {
        offset += LIMIT;
    }

    public final void setPrev(String prev) {
        offset = Math.max(0, offset - LIMIT);
    }

    public final StudyFilterModel getStudyFilter() {
        if (studyFilter == null) {
            studyFilter = new StudyFilterModel();
            studyFilter.setPatientID(patientID);
            studyFilter.setPatientName(patientName);
            studyFilter.setAccessionNumber(accessionNumber);
            studyFilter.setStudyID(studyID);
            studyFilter.setStudyDateRange(studyDateRange);
            studyFilter.setModality(modality);
        }
        return studyFilter;
    }

    public final int getTotal() {
        return total;
    }

    public void updatePatients(List newPatients) {
        List sticky = patients;
        patients = newPatients;
        if (sticky != null) {
            for (int i = sticky.size(); --i >= 0;) {
                PatientModel pat = (PatientModel) sticky.get(i);
                if (keepSticky(pat)) {
                    mergeSticky(pat);
                }
            }
        }
    }

    private void mergeSticky(PatientModel stickyPat) {
        for (int i = patients.size(); --i >= 0;) {
            PatientModel pat = (PatientModel) patients.get(i);
            if (pat.getPk() == stickyPat.getPk()) {
                List stickyStudies = stickyPat.getStudies();
                for (int j = stickyStudies.size(); --j >= 0;) {
                    mergeSticky((StudyModel) stickyStudies.get(j), pat
                            .getStudies());
                }
                return;
            }
        }
        patients.add(0, stickyPat);
    }

    private void mergeSticky(StudyModel stickyStudy, List studies) {
        for (int i = studies.size(); --i >= 0;) {
            StudyModel study = (StudyModel) studies.get(i);
            if (study.getPk() == stickyStudy.getPk()) {
                List stickySeries = stickyStudy.getSeries();
                for (int j = stickySeries.size(); --j >= 0;) {
                    mergeSticky((SeriesModel) stickySeries.get(j), study
                            .getSeries());
                }
                return;
            }
        }
        studies.add(0, stickyStudy);
    }

    private void mergeSticky(SeriesModel stickySerie, List series) {
        for (int i = series.size(); --i >= 0;) {
            SeriesModel serie = (SeriesModel) series.get(i);
            if (serie.getPk() == stickySerie.getPk()) {
                List stickyInstances = stickySerie.getInstances();
                for (int j = stickyInstances.size(); --j >= 0;) {
                    mergeSticky((InstanceModel) stickyInstances.get(j), serie
                            .getInstances());
                }
                return;
            }
        }
        series.add(0, stickySerie);
    }

    private void mergeSticky(InstanceModel stickyInst, List instances) {
        for (int i = instances.size(); --i >= 0;) {
            InstanceModel inst = (InstanceModel) instances.get(i);
            if (inst.getPk() == stickyInst.getPk()) { return; }
        }
        instances.add(0, stickyInst);
    }

    private boolean keepSticky(PatientModel patient) {
        boolean sticky = isSticky(patient);
        for (Iterator it = patient.getStudies().iterator(); it.hasNext();) {
            if (keepSticky((StudyModel) it.next())) {
                sticky = true;
            } else {
                it.remove();
            }
        }
        return sticky;
    }

    private boolean keepSticky(StudyModel study) {
        boolean sticky = isSticky(study);
        for (Iterator it = study.getSeries().iterator(); it.hasNext();) {
            if (keepSticky((SeriesModel) it.next())) {
                sticky = true;
            } else {
                it.remove();
            }
        }
        return sticky;
    }

    private boolean keepSticky(SeriesModel series) {
        boolean sticky = isSticky(series);
        for (Iterator it = series.getInstances().iterator(); it.hasNext();) {
            if (isSticky((InstanceModel) it.next())) {
                sticky = true;
            } else {
                it.remove();
            }
        }
        return sticky;
    }

    public final boolean isSticky(PatientModel patient) {
        return  stickyPatients.contains("" + patient.getPk());
    }

    public final boolean isSticky(StudyModel study) {
        return stickyStudies.contains("" + study.getPk());
    }

    public final boolean isSticky(SeriesModel series) {
        return stickySeries.contains("" + series.getPk());
    }

    public final boolean isSticky(InstanceModel instance) {
        return stickyInstances.contains("" + instance.getPk());
    }

    public PatientModel getPatientByPk(int patPk) {
        for (int i = 0, n = patients.size(); i < n; i++) {
            PatientModel pat = (PatientModel) patients.get(i);
            if (pat.getPk() == patPk) { return pat; }
        }
        return null;
    }

    public StudyModel getStudyByPk(int patPk, int studyPk) {
        return getStudyByPk(getPatientByPk(patPk), studyPk);
    }

    public StudyModel getStudyByPk(PatientModel patient, int studyPk) {
        if (patient == null) { return null; }
        List studies = patient.getStudies();
        for (int i = 0, n = studies.size(); i < n; i++) {
            StudyModel study = (StudyModel) studies.get(i);
            if (study.getPk() == studyPk) { return study; }
        }
        return null;
    }

    public SeriesModel getSeriesByPk(int patPk, int studyPk, int seriesPk) {
        return getSeriesByPk(getStudyByPk(patPk, studyPk), seriesPk);
    }

    public SeriesModel getSeriesByPk(StudyModel study, int seriesPk) {
        if (study == null) { return null; }
        List series = study.getSeries();
        for (int i = 0, n = series.size(); i < n; i++) {
            SeriesModel serie = (SeriesModel) series.get(i);
            if (serie.getPk() == seriesPk) { return serie; }
        }
        return null;
    }

    public void removeStickies() {
        PatientModel patient;
        StudyModel study;
        SeriesModel series;
        InstanceModel instance;
        for (Iterator patient_iter = patients.iterator(); patient_iter
                .hasNext();) {
            patient = (PatientModel) patient_iter.next();
            if (stickyPatients.contains(String.valueOf(patient.getPk()))) {
                patient_iter.remove();
                stickyPatients.remove(String.valueOf(patient.getPk()));
            } else
                for (Iterator study_iter = patient.getStudies().iterator(); study_iter
                        .hasNext();) {
                    study = (StudyModel) study_iter.next();
                    if (stickyStudies.contains(String.valueOf(study.getPk()))) {
                        study_iter.remove();
                        stickyStudies.remove(String.valueOf(study.getPk()));
                    } else
                        for (Iterator series_iter = study.getSeries()
                                .iterator(); series_iter.hasNext();) {
                            series = (SeriesModel) series_iter.next();
                            if (stickySeries.contains(String.valueOf(series
                                    .getPk()))) {
                                series_iter.remove();
                                stickySeries.remove(String.valueOf(series
                                        .getPk()));
                            } else
                                for (Iterator instance_iter = series
                                        .getInstances().iterator(); instance_iter
                                        .hasNext();) {
                                    instance = (InstanceModel) instance_iter
                                            .next();
                                    if (isSticky(instance)) {
                                        instance_iter.remove();
                                        stickyInstances.remove(String
                                                .valueOf(instance.getPk()));
                                    }
                                }
                        }
                }
        }
    }
}