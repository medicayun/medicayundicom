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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.web.war.trash.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dcm4che2.data.Tag;
import org.dcm4chee.archive.entity.PrivatePatient;
import org.dcm4chee.archive.entity.PrivateStudy;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.dao.trash.TrashListLocal;
import org.dcm4chee.web.war.StudyPermissionHelper;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since May 10, 2010
 */
public class PrivPatientModel extends AbstractDicomModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private List<PrivStudyModel> studies = new ArrayList<PrivStudyModel>();

    TrashListLocal dao = (TrashListLocal) JNDIUtils.lookup(TrashListLocal.JNDI_NAME);

    private boolean expandable;

    public PrivPatientModel(PrivatePatient patient) {
        setPk(patient.getPk());
        this.dataset = patient.getAttributes();
    }

    public String getName() {
        return dataset.getString(Tag.PatientName);
    }

    public String getId() {
        return dataset.getString(Tag.PatientID);
    }

    public String getIssuer() {
        return dataset.getString(Tag.IssuerOfPatientID);
    }

    public String getSex() {
        return dataset.getString(Tag.PatientSex);
    }

    public Date getBirthdate() {
        return toDate(Tag.PatientBirthDate);
    }

    public String getComments() {
        return dataset.getString(Tag.PatientComments);
    }

    public List<PrivStudyModel> getStudies() {
        return studies;
    }

    @Override
    public int getRowspan() {
        int rowspan = isDetails() ? 2 : 1;
        for (PrivStudyModel study : studies) {
            rowspan += study.getRowspan();
        }
        return rowspan;
    }

    @Override
    public void collapse() {
        studies.clear();
    }

    @Override
    public boolean isCollapsed() {
        return studies.isEmpty();
    }

    public void retainSelectedStudies() {
        for (Iterator<PrivStudyModel> it = studies.iterator(); it.hasNext();) {
            PrivStudyModel study = it.next();
            study.retainSelectedSeries();
            if (study.isCollapsed() && !study.isSelected()) {
                it.remove();
            }
        }
    }

    @Override
    public void expand() {
        this.studies.clear();
        for (PrivateStudy study : dao.findStudiesOfPatient(getPk(), 
                StudyPermissionHelper.get().getStudyPermissionRight().equals(StudyPermissionHelper.StudyPermissionRight.ALL) ?
                        null : StudyPermissionHelper.get().getDicomRoles())) {
            this.studies.add(new PrivStudyModel(study, this));
        }
    }

    @Override
    public int levelOfModel() {
        return PATIENT_LEVEL;
    }
   
    @Override
    public List<? extends AbstractDicomModel> getDicomModelsOfNextLevel() {
        return studies;
    }

    public boolean isExpandable() {
        return expandable;
    }
    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }
}
