/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import java.util.ArrayList;

import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.web.maverick.model.PatientModel;

/**
 * @author umberto.cappellini@tiani.com
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
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

    private void executeMerge() throws Exception {
        ArrayList list = new ArrayList();
        for (int i = 0; i < to_be_merged.length; i++) {
            if (to_be_merged[i] != pk)
                    list.add(getPatient(to_be_merged[i]).toDataset());
        }
        Dataset[] priors = (Dataset[]) list.toArray(new Dataset[list.size()]);
        lookupPatientUpdate().mergePatient(getPatient(pk).toDataset(), priors);
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