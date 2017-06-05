/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import java.util.List;

import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.PatientModel;
import org.dcm4chex.archive.web.maverick.model.StudyModel;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.UIDGenerator;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1254 $ $Date: 2004-12-02 20:09:58 +0800 (周四, 02 12月 2004) $
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
        if (submit != null)
            if (studyPk == -1)
                executeCreate();
            else
                executeUpdate();
        return SUCCESS;
    }

    private ContentEdit lookupContentEdit() throws Exception {
        ContentEditHome home = (ContentEditHome) EJBHomeFactory.getFactory()
                .lookup(ContentEditHome.class, ContentEditHome.JNDI_NAME);
        return home.create();
    }
    
    private ContentManager lookupContentManager() throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory.getFactory()
                .lookup(ContentManagerHome.class, ContentManagerHome.JNDI_NAME);
        return home.create();
    }

    private void executeCreate() {
        try {
	        StudyModel study = new StudyModel();
	        study.setPk( -1 );
            study.setStudyIUID( UIDGenerator.getInstance().createUID() );
	        study.setSpecificCharacterSet( "ISO_IR 100" );        
            study.setPlacerOrderNumber(placerOrderNumber);
            study.setFillerOrderNumber(fillerOrderNumber);
            study.setAccessionNumber(accessionNumber);
            study.setReferringPhysician(referringPhysician);
            study.setStudyDateTime(studyDateTime);
            study.setStudyDescription(studyDescription);
            study.setStudyID(studyID);
            ContentEdit ce = lookupContentEdit();
            ce.createStudy( study.toDataset(), patPk );
            FolderForm form = FolderForm.getFolderForm(getCtx().getRequest());
            PatientModel pat = form.getPatientByPk(patPk);

            ContentManager cm = lookupContentManager();
            List studies = cm.listStudiesOfPatient(patPk);
            for (int i = 0, n = studies.size(); i < n; i++)
                studies.set(i, new StudyModel((Dataset) studies.get(i)));
            pat.setStudies(studies);
            
            AuditLoggerDelegate.logProcedureRecord(getCtx(),
                    AuditLoggerDelegate.CREATE,
                    pat.getPatientID(),
                    pat.getPatientName(),
                    study.getPlacerOrderNumber(),
                    study.getFillerOrderNumber(),
                    study.getStudyIUID(),
                    study.getAccessionNumber(),
                    "new study:"+study.getStudyIUID() );
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
 
    private void executeUpdate() {
        try {
            StudyModel study = FolderForm.getFolderForm(getCtx().getRequest())
                    .getStudyByPk(patPk, studyPk);
            //updating data model
            StringBuffer sb = new StringBuffer();
            boolean modified = false;            
            if (AuditLoggerDelegate.isModified("Placer Order Number",
                    study.getPlacerOrderNumber(), placerOrderNumber, sb)) {
                study.setPlacerOrderNumber(placerOrderNumber);
                modified = true;
            }
            if (AuditLoggerDelegate.isModified("Filler Order Number",
                    study.getFillerOrderNumber(), fillerOrderNumber, sb)) {
                study.setFillerOrderNumber(fillerOrderNumber);
                modified = true;
            }
            if (AuditLoggerDelegate.isModified("Accession Number",
                    study.getAccessionNumber(), accessionNumber, sb)) {
                study.setAccessionNumber(accessionNumber);
                modified = true;
            }
            if (AuditLoggerDelegate.isModified("Referring Physician",
                    study.getReferringPhysician(), referringPhysician, sb)) {
                study.setReferringPhysician(referringPhysician);
                modified = true;
            }
            if (AuditLoggerDelegate.isModified("Study Date/Time",
                    study.getStudyDateTime(), studyDateTime, sb)) {
                study.setStudyDateTime(studyDateTime);
                modified = true;
            }
            if (AuditLoggerDelegate.isModified("Study Description",
                    study.getStudyDescription(), studyDescription, sb)) {
                study.setStudyDescription(studyDescription);
                modified = true;
            }
            if (AuditLoggerDelegate.isModified("Study ID",
                    study.getStudyID(), studyID, sb)) {
                study.setStudyID(studyID);
                modified = true;
            }
            if (modified) {
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
	                    study.getAccessionNumber(),
	                    AuditLoggerDelegate.trim(sb));
            }
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