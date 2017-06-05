/*
 * $Id: PatientUpdateCtrl.java 1181 2004-10-14 23:08:49Z gunterze $ Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.web.maverick;

import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.PatientModel;

/**
 * @author umberto.cappellini@tiani.com
 */
public class PatientUpdateCtrl extends Dcm4JbossController {
    private int pk;

    private String patientID = "";

    private String issuerOfPatientID = "";

    private String patientName = "";

    private String patientSex = "";

    private String patientBirthDate = "";

    private String submit = null;

    private String cancel = null;

    protected String perform() throws Exception {
        if (submit != null)
            if (pk == -1)
                executeCreate();
            else
                executeUpdate();
        return SUCCESS;
    }

    private void executeCreate() {
        try {
	        PatientModel pat = new PatientModel();
	        pat.setPk(-1);
	        pat.setSpecificCharacterSet("ISO_IR 100");        
	        pat.setPatientID(patientID);
	        pat.setIssuerOfPatientID(issuerOfPatientID);
	        pat.setPatientSex(patientSex);
	        pat.setPatientName(patientName);
	        pat.setPatientBirthDate(patientBirthDate);
	        ContentEdit ce = lookupContentEdit();
	        ce.createPatient(pat.toDataset());        
            AuditLoggerDelegate.logPatientRecord(getCtx(), AuditLoggerDelegate.CREATE, pat
                    .getPatientID(), pat.getPatientName());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private ContentEdit lookupContentEdit() throws Exception {
        ContentEditHome home = (ContentEditHome) EJBHomeFactory.getFactory()
                .lookup(ContentEditHome.class, ContentEditHome.JNDI_NAME);
        return home.create();
    }

    private void executeUpdate() {
        try {
            PatientModel pat = FolderForm.getFolderForm(
                    getCtx().getRequest()).getPatientByPk(pk);
            pat.setPatientSex(patientSex);
            pat.setPatientName(patientName);

            pat.setPatientBirthDate(patientBirthDate);
            //updating data model
            ContentEdit ce = lookupContentEdit();
            ce.updatePatient(pat.toDataset());
            AuditLoggerDelegate.logPatientRecord(getCtx(), AuditLoggerDelegate.MODIFY, pat
                    .getPatientID(), pat.getPatientName());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public final void setIssuerOfPatientID(String issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID.trim();
    }

    public final void setPatientID(String patientID) {
        this.patientID = patientID.trim();
    }

    public final void setPatientName(String patientName) {
        this.patientName = patientName.trim();
    }

    public final void setPatientSex(String patientSex) {
        this.patientSex = patientSex.trim();
    }

    public final void setPatientBirthDate(String date) {
        this.patientBirthDate = date.trim();
    }

    public final void setPk(int pk) {
        this.pk = pk;
    }

    public final void setSubmit(String update) {
        this.submit = update;
    }

    public final void setCancel(String cancel) {
        this.cancel = cancel;
    }
}