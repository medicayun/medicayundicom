/*
 * $Id: PatientUpdateCtrl.java 1595 2005-03-14 12:28:39Z javawilli $ Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
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

import org.dcm4che.data.Dataset;
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
	        Dataset ds = FolderSubmitCtrl.getDelegate().createPatient(pat.toDataset());
	        
	        //add new patient to model (as first element) and set sticky flag!
	        pat = new PatientModel( ds );
	        FolderForm form = FolderForm.getFolderForm( getCtx().getRequest() );
	        form.getStickyPatients().add( String.valueOf( pat.getPk() ) );
	        form.getPatients().add(0, pat);
	        
            AuditLoggerDelegate.logPatientRecord(getCtx(), AuditLoggerDelegate.CREATE, pat
                    .getPatientID(), pat.getPatientName(), null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
    private void executeUpdate() {
        try {
            PatientModel pat = FolderForm.getFolderForm(
                    getCtx().getRequest()).getPatientByPk(pk);
            StringBuffer sb = new StringBuffer();
            boolean modified = false;            
            if (AuditLoggerDelegate.isModified("Patient Name",
                    pat.getPatientName(), patientName, sb)) {
                pat.setPatientName(patientName);
                modified = true;
            }
            if (AuditLoggerDelegate.isModified("Patient Sex",
                    pat.getPatientSex(), patientSex, sb)) {
                pat.setPatientSex(patientSex);
                modified = true;
            }
            if (AuditLoggerDelegate.isModified("Birth Date",
                    pat.getPatientBirthDate(), patientBirthDate, sb)) {
                pat.setPatientBirthDate(patientBirthDate);
                modified = true;
            }
            if (modified) {
	            //updating data model
	            FolderSubmitCtrl.getDelegate().updatePatient(pat.toDataset());
	            AuditLoggerDelegate.logPatientRecord(getCtx(), 
	                    AuditLoggerDelegate.MODIFY, pat.getPatientID(),
	                    pat.getPatientName(), AuditLoggerDelegate.trim(sb));
            }
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