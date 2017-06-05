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
import org.dcm4chex.archive.web.maverick.model.StudyModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1181 $ $Date: 2004-10-15 07:08:49 +0800 (周五, 15 10月 2004) $
 * @since 5.10.2004
 *
 */
public class StudyEditCtrl extends Dcm4JbossController {
    private int patPk;

    private int studyPk;

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

    public PatientModel getPatient() {
        return FolderForm.getFolderForm(getCtx().getRequest()).getPatientByPk(
                patPk);
    }

    public StudyModel getStudy() {
        return FolderForm.getFolderForm(getCtx().getRequest()).getStudyByPk(
                patPk, studyPk);
    }

}