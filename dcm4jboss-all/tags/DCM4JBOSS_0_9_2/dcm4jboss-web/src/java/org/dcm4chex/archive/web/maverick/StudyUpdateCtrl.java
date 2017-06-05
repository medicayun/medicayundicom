/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.PatientModel;
import org.dcm4chex.archive.web.maverick.model.StudyModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1181 $ $Date: 2004-10-15 07:08:49 +0800 (周五, 15 10月 2004) $
 * @since 5.10.2004
 *
 */
public class StudyUpdateCtrl extends Dcm4JbossController {

    private int patPk;

    private int studyPk;

    private String placerOrderNumber;

    private String fillerOrderNumber;

    private String accessionNumber;

    private String studyID;

    private String studyDateTime;

    private String studyDescription;

    private String referringPhysician;

    private String submit = null;

    private String cancel = null;

    public final void setPatPk(int patPk) {
        this.patPk = patPk;
    }

    public final void setStudyPk(int studyPk) {
        this.studyPk = studyPk;
    }

    public final void setPlacerOrderNumber(String s) {
        this.placerOrderNumber = s.trim();
    }

    public final void setFillerOrderNumber(String s) {
        this.fillerOrderNumber = s.trim();
    }

    public final void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public final void setReferringPhysician(String s) {
        this.referringPhysician = s.trim();
    }

    public final void setStudyDateTime(String s) {
        this.studyDateTime = s.trim();
    }

    public final void setStudyDescription(String s) {
        this.studyDescription = s.trim();
    }

    public final void setStudyID(String s) {
        this.studyID = s.trim();
    }

    protected String perform() throws Exception {
        if (submit != null) executeUpdate();
        return SUCCESS;
    }

    private ContentEdit lookupContentEdit() throws Exception {
        ContentEditHome home = (ContentEditHome) EJBHomeFactory.getFactory()
                .lookup(ContentEditHome.class, ContentEditHome.JNDI_NAME);
        return home.create();
    }

    private void executeUpdate() {
        try {
            StudyModel study = FolderForm.getFolderForm(getCtx().getRequest())
                    .getStudyByPk(patPk, studyPk);
            //updating data model
            study.setPlacerOrderNumber(placerOrderNumber);
            study.setFillerOrderNumber(fillerOrderNumber);
            study.setAccessionNumber(accessionNumber);
            study.setReferringPhysician(referringPhysician);
            study.setStudyDateTime(studyDateTime);
            study.setStudyDescription(studyDescription);
            study.setStudyID(studyID);
            ContentEdit ce = lookupContentEdit();
            ce.updateStudy(study.toDataset());
            FolderForm form = FolderForm.getFolderForm(getCtx().getRequest());
            PatientModel pat = form.getPatientByPk(patPk);
            AuditLoggerDelegate.logProcedureRecord(getCtx(),
                    AuditLoggerDelegate.MODIFY,
                    pat.getPatientID(),
                    pat.getPatientName(),
                    study.getPlacerOrderNumber(),
                    study.getFillerOrderNumber(),
                    study.getStudyIUID(),
                    study.getAccessionNumber());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public final void setSubmit(String update) {
        this.submit = update;
    }

    public final void setCancel(String cancel) {
        this.cancel = cancel;
    }
}