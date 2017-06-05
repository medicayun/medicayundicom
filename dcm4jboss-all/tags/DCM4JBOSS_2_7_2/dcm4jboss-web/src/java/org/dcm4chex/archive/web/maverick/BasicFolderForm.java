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

package org.dcm4chex.archive.web.maverick;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.web.maverick.model.InstanceModel;
import org.dcm4chex.archive.web.maverick.model.PatientModel;
import org.dcm4chex.archive.web.maverick.model.SeriesModel;
import org.dcm4chex.archive.web.maverick.model.StudyModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2171 $ $Date: 2005-12-23 00:57:17 +0800 (周五, 23 12月 2005) $
 * @since 28.01.2004
 */
public class BasicFolderForm {

    private int limit = 20;
    
    public static final String NO_ERROR ="OK";

    private List patients;

    private final Set stickyPatients = new HashSet();

    private final Set stickyStudies = new HashSet();

    private final Set stickySeries = new HashSet();

    private final Set stickyInstances = new HashSet();

    private int offset;

    private int total;
    
    private final boolean admin;
    
    /** Error code for rendering message. */
    private String errorCode = NO_ERROR;
    
    /** Popup message */
    private String popupMsg = null;
    
	private boolean showWithoutStudies;

	protected static Logger log = Logger.getLogger(BasicFolderForm.class);
    
	protected BasicFolderForm( boolean adm ) {
    	admin = adm;
    }
	
	public String getModelName() { return "FOLDER"; }

	/**
	 * @return Returns the admin.
	 */
	public boolean isAdmin() {
		return admin;
	}
	
    public final int getLimit() {
        return limit;
    }

    public final void setLimit(int limit) {
        this.limit = limit;
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
    
    public final void resetOffset() {
    	offset = 0;
    	total = -1;
    }

    public final List getPatients() {
        return patients;
    }

    public final void setNext(String next) {
        offset += limit;
    }

    public final void setPrev(String prev) {
        offset = offset < limit ? 0 : offset - limit;
    }

    public final int getTotal() {
        return total;
    }
    
    public final String getErrorCode() {
    	return errorCode;
    }
    
    
    public final void setErrorCode( String err ) {
    	errorCode = err;
    }

	/**
	 * @return Returns the popupMsg.
	 */
	public String getPopupMsg() {
		return popupMsg;
	}
	/**
	 * @param popupMsg The popupMsg to set.
	 */
	public void setPopupMsg(String popupMsg) {
		this.popupMsg = popupMsg;
	}

	/**
	 * @return Returns the hideStudyLess.
	 */
	public boolean isShowWithoutStudies() {
		return showWithoutStudies;
	}
	/**
	 * @param hideStudyLess The hideStudyLess to set.
	 */
	public void setShowWithoutStudies(boolean showWithoutStudies) {
		this.showWithoutStudies = showWithoutStudies;
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
    
    public InstanceModel getInstanceByPk(int patPk, int studyPk, int seriesPk, int instancePk) {
        return getInstanceByPk(getSeriesByPk(patPk, studyPk, seriesPk), instancePk);
    }

    public InstanceModel getInstanceByPk(SeriesModel series, int instancePk) {
        if (series == null) { return null; }
        List instances = series.getInstances();
        for (int i = 0, n = instances.size(); i < n; i++) {
            InstanceModel inst = (InstanceModel) instances.get(i);
            if (inst.getPk() == instancePk) { return inst; }
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