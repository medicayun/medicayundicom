/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import org.dcm4chex.archive.web.maverick.model.PatientModel;
import org.dcm4chex.archive.web.maverick.model.SeriesModel;
import org.dcm4chex.archive.web.maverick.model.StudyModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1246 $ $Date: 2004-12-02 17:42:25 +0800 (周四, 02 12月 2004) $
 * @since 7.10.2004
 *
 */
public class SeriesEditCtrl extends Dcm4JbossController {

    private int patPk;

    private int studyPk;

    private int seriesPk;

    public final int getPatPk() {
        return patPk;
    }

    public final void setPatPk(int pk) {
        this.patPk = pk;
    }

    public final int getStudyPk() {
        return studyPk;
    }

    public final void setStudyPk(int pk) {
        this.studyPk = pk;
    }

    public final int getSeriesPk() {
        return seriesPk;
    }

    public final void setSeriesPk(int seriesPk) {
        this.seriesPk = seriesPk;
    }

    public PatientModel getPatient() {
        return FolderForm.getFolderForm(getCtx().getRequest()).getPatientByPk(
                patPk);
    }

    public StudyModel getStudy() {
        return FolderForm.getFolderForm(getCtx().getRequest()).getStudyByPk(
                patPk, studyPk);
    }
    
    public SeriesModel getSeries() {
    	return seriesPk == -1 ? newSeries() :  FolderForm.getFolderForm(getCtx().getRequest())
                .getSeriesByPk(patPk, studyPk, seriesPk);
    }

    private SeriesModel newSeries() {
    	SeriesModel seriesModel = new SeriesModel();
    	seriesModel.setSpecificCharacterSet("ISO_IR 100");
    	return seriesModel;
    }
    
}