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

package org.dcm4chee.web.war.folder.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.common.model.AbstractEditableDicomModel;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 19, 2008
 */
public class PPSModel extends AbstractEditableDicomModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private List<SeriesModel> seriess = new ArrayList<SeriesModel>();
    
    private SeriesModel series1;
    private int numberOfInstances;
    private int numberOfSeries;
    private Boolean hasForeignPpsInfo;
    
    public PPSModel(MPPS mpps, SeriesModel series1, StudyModel studyModel) {
        if (mpps != null) {
            setPk(mpps.getPk());
            this.dataset = mpps.getAttributes();
            hasForeignPpsInfo = false;
        }
        setParent(studyModel);
        series1.setPPS(this);
        this.series1 = series1;
        seriess.add(series1);
    }
    
    public void setStudy(StudyModel m) {
        setParent(m);
    }
    
    public StudyModel getStudy() {
        return (StudyModel) getParent();
    }


    public List<SeriesModel> getSeries() {
        return seriess;
    }

    public SeriesModel getSeries1() {
        return series1;
    }

    public Date getDatetime() {
        return dataset != null 
                ? toDate(Tag.PerformedProcedureStepStartDate, Tag.PerformedProcedureStepStartTime)
                : series1.getPPSStartDatetime();
    }

    public String getAccessionNumber() {
        if (dataset == null)
            return null;
        String accNo = dataset.getString(new int[] { 
                        Tag.ScheduledStepAttributesSequence, 0,
                        Tag.AccessionNumber });
        if (accNo == null) {
            DicomElement ssaSq = dataset.get(Tag.ScheduledStepAttributesSequence);
            if (ssaSq != null && ssaSq.countItems() > 1) {
                for (int i = 1, len = ssaSq.countItems() ; i < len ; i++) {
                    accNo = ssaSq.getDicomObject(i).getString(Tag.AccessionNumber);
                    if (accNo != null) {
                        log.warn("ScheduledStepAttributesSequence contains item(s) without Accession Number! Found Accession Number in item "+i);
                        return accNo;
                    }
                }
            }
        }
        return accNo;
    }

    public String getSpsid() {
        return dataset != null
                ? dataset.getString(new int[] { 
                        Tag.ScheduledStepAttributesSequence, 0,
                        Tag.ScheduledProcedureStepID })
                : null;
    }

    public String getId() {
        return dataset != null 
                ? dataset.getString(Tag.PerformedProcedureStepID)
                : series1.getPPSId();
    }

    public String getUid() {
        return dataset != null 
                ? dataset.getString(Tag.SOPInstanceUID)
                : series1.getPPSUid();
    }

    public String getDescription() {
        return dataset != null 
                ? dataset.getString(Tag.PerformedProcedureStepDescription)
                : series1.getPPSDescription();
    }

    public String getModality() {
        return dataset != null 
                ? dataset.getString(Tag.Modality)
                : series1.getModality();
    }

    public String getStationName() {
        return dataset != null 
                ? dataset.getString(Tag.PerformedStationName)
                : series1.getStationName();
    }

    public String getStationAET() {
        return dataset != null 
                ? dataset.getString(Tag.PerformedStationAETitle)
                : series1.getSourceAET();
    }

    public int getNumberOfSeries() {
        if (numberOfSeries == 0) {
            if (dataset != null) {
                DicomElement sersq = dataset.get(Tag.PerformedSeriesSequence);
                if (sersq != null) {
                    numberOfSeries = sersq.countItems();
                } else {
                    log.warn("Missing PerformedSeriesSequence in PPS! PpsID:"+dataset.getString(Tag.PerformedProcedureStepID));
                }
            } else {
                numberOfSeries = seriess.size();
            }
        }
        return numberOfSeries;
    }
    
    public boolean hasForeignPpsInfo() {
        if (hasForeignPpsInfo == null) {
            hasForeignPpsInfo = false;
            for (int i = 0, len = seriess.size() ; i < len ; i++) {
                if (seriess.get(i).getPPSUid() != null) {
                    hasForeignPpsInfo = true;
                    break;
                }
            }
        }
        return hasForeignPpsInfo.booleanValue();
    }

    public int getNumberOfInstances() {
        if (numberOfInstances == 0) {
            if (dataset != null) {
                DicomElement sersq = dataset.get(Tag.PerformedSeriesSequence);
                if (sersq != null) {
                    for (int i = 0, n = sersq.countItems(); i < n; i++) {
                        DicomObject ser = sersq.getDicomObject(i);
                        DicomElement imgsq = ser.get(Tag.ReferencedImageSequence);
                        DicomElement nonimgsq = ser.get(
                                Tag.ReferencedNonImageCompositeSOPInstanceSequence);
                        if (imgsq != null) {
                            numberOfInstances += imgsq.countItems();
                        }
                        if (nonimgsq != null) {
                            numberOfInstances += nonimgsq.countItems();
                        }
                    }
                }
            } else {
                for (SeriesModel ser : seriess) {
                    numberOfInstances += ser.getNumberOfInstances();
                }
            }
        }
        return numberOfInstances;
    }

    public String getStatus() {
        return dataset != null 
                ? dataset.getString(Tag.PerformedProcedureStepStatus)
                : null;
    }
    
    public int getRowspan() {
        int rowspan = isDetails() ? 2 : 1;
        for (SeriesModel ser : seriess) {
            rowspan += ser.getRowspan();
        }
        return rowspan;
    }

    public void collapse() {
        seriess.clear();
    }

    public boolean isCollapsed() {
        return seriess.isEmpty();
    }

    public void retainSelectedSeries() {
        for (int i = 0; i < seriess.size(); i++) {
            SeriesModel series = seriess.get(i);
            series.retainSelectedInstances();
            if (series.isCollapsed() && !series.isSelected()) {
                seriess.remove(i);
                i--;
            }
        }
    }

    public void expand() {
        String uid = getUid();
        if (uid != null) {
            seriess.clear();
            StudyListLocal dao = (StudyListLocal)
                    JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
            for (Series ser : dao.findSeriesOfMpps(uid)) 
                seriess.add(new SeriesModel(ser, this));
        }
    }

    @Override
    public int levelOfModel() {
        return PPS_LEVEL;
    }
   
    @Override
    public List<? extends AbstractDicomModel> getDicomModelsOfNextLevel() {
        return seriess;
    }
    
    @Override
    public void update(DicomObject dicomObject) {
        StudyListLocal dao = (StudyListLocal)
                JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        dataset = dao.updateMPPS(getPk(), dicomObject).getAttributes();
    }
    
    @Override
    public AbstractEditableDicomModel refresh() {
        StudyListLocal dao = (StudyListLocal) JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
        if (dataset != null) {
            dataset = dao.getMPPS(getPk()).getAttributes();
        } else {
            ArrayList<SeriesModel> currentSeries = new ArrayList<SeriesModel>();
            for (Series s : dao.findSeriesOfStudy(getStudy().getPk())) {
                SeriesModel seriesModel = new SeriesModel(s,null);
                MPPS mpps = s.getModalityPerformedProcedureStep();
                if (mpps == null && seriesModel.containedBySamePPS(getSeries1())) {
                    currentSeries.add(seriesModel);
                }
            }
            seriess.retainAll(currentSeries);
        }
        numberOfSeries = 0;
        numberOfInstances = 0;
        return this;
    }    
}
