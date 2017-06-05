/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.web.maverick.model.PatientModel;

/**
 * @author umberto.cappellini@tiani.com
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1595 $ $Date: 2005-03-14 20:28:39 +0800 (周一, 14 3月 2005) $
 */
public class PatientMergeCtrl extends Errable {

    private int pk;

    private int[] to_be_merged;

    private String merge = null;

    private String cancel = null;

    protected String perform() {
        try {
            if (merge != null) executeMerge();
            return SUCCESS;
        } catch (Exception e1) {
            this.errorType = e1.getClass().getName();
            this.message = e1.getMessage();
            this.backURL = "folder.m";
            return ERROR_VIEW;
        }
    }
    
    private String makeMergeDesc(Dataset ds) {
        return "Merged with [" + ds.getString(Tags.PatientID) +
        	"]" +  ds.getString(Tags.PatientName);
    }

    
    private void executeMerge() throws Exception {
        int[] priors = new int[to_be_merged.length-1];
        for (int i = 0, j = 0; i < to_be_merged.length; i++) {
            if (to_be_merged[i] != pk)
                priors[j++] = to_be_merged[i];
        }        
        FolderSubmitCtrl.getDelegate().mergePatients( pk, priors);
        Dataset dominant = getPatient(pk).toDataset();
        for (int i = 0; i < priors.length; i++) {
            Dataset prior = getPatient(priors[i]).toDataset();
            AuditLoggerDelegate.logPatientRecord(getCtx(),
                    AuditLoggerDelegate.MODIFY,
                    dominant.getString(Tags.PatientID),
                    dominant.getString(Tags.PatientName),
                    makeMergeDesc(prior));
            AuditLoggerDelegate.logPatientRecord(getCtx(),
                    AuditLoggerDelegate.DELETE,
                    prior.getString(Tags.PatientID),
                    prior.getString(Tags.PatientName),
                    makeMergeDesc(dominant));
        }
    }

    public final void setPk(int pk) {
        this.pk = pk;
    }

    public final void setToBeMerged(int[] tbm) {
        this.to_be_merged = tbm;
    }

    public final void setMerge(String merge) {
        this.merge = merge;
    }

    public PatientModel getPatient(int ppk) {
        return FolderForm.getFolderForm(getCtx().getRequest())
                .getPatientByPk(ppk);
    }

}