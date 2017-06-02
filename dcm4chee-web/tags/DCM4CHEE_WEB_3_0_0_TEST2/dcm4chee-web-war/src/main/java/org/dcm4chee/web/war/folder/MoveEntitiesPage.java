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

package org.dcm4chee.web.war.folder;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.time.Duration;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4chee.archive.common.Availability;
import org.dcm4chee.archive.common.PrivateTag;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.exceptions.SelectionException;
import org.dcm4chee.web.dao.common.DicomEditLocal;
import org.dcm4chee.web.war.common.SimpleEditDicomObjectPage;
import org.dcm4chee.web.war.common.SimpleEditDicomObjectPanel;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.common.model.AbstractEditableDicomModel;
import org.dcm4chee.web.war.folder.model.InstanceModel;
import org.dcm4chee.web.war.folder.model.PPSModel;
import org.dcm4chee.web.war.folder.model.PatientModel;
import org.dcm4chee.web.war.folder.model.SeriesModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 11, 2010
 */
public class MoveEntitiesPage extends WebPage {
    private static Logger log = LoggerFactory.getLogger(MoveEntitiesPage.class);

    private static final String MSGID_ERR_SELECTION_MOVE_SOURCE_LEVEL = "move.message.error.moveSelectionSrcLevel";
    private static final String MSGID_ERR_SELECTION_MOVE_DESTINATION = "move.message.error.moveSelectionDest";
    private static final String MSGID_ERR_SELECTION_MOVE_NO_SELECTION = "move.message.error.moveNoSelection";
    private static final String MSGID_ERR_SELECTION_MOVE_NO_SOURCE = "move.message.error.moveNoSource";
    private static final String MSGID_ERR_SELECTION_MOVE_PPS = "move.message.error.movePPS";
    private static final String MSGID_ERR_SELECTION_MOVE_NOT_ONLINE = "move.message.error.moveNotOnline";
    
    private static final int MISSING_NOTHING = 0;
    private static final int MISSING_STUDY = 1;
    private static final int MISSING_SERIES = 2;

    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    private static final ResourceReference EditCSS = new CompressedResourceReference(SimpleEditDicomObjectPage.class, "edit-style.css");
    private static final ResourceReference CSS = new CompressedResourceReference(MoveEntitiesPage.class, "move-style.css");

    private SelectedEntities selected;
    private Page page;
    private List<PatientModel> allPatients;
    
    private int missingState = MISSING_NOTHING;
    private InfoPanel infoPanel;
    private StudyModel studyModel;
    private Panel newStudyPanel;
    private SeriesModel seriesModel;
    private Panel newSeriesPanel;
    
    private AbstractDicomModel destinationModel;
    private HashSet<AbstractDicomModel> modifiedModels = new HashSet<AbstractDicomModel>();
    
    private String infoMsgId;
    private StringResourceModel selectedInfoModel;
    
    public MoveEntitiesPage(Page page, SelectedEntities selectedEntities, List<PatientModel> all) {
        super();
        add(CSSPackageResource.getHeaderContribution(MoveEntitiesPage.BaseCSS));
        add(CSSPackageResource.getHeaderContribution(MoveEntitiesPage.EditCSS));
        add(CSSPackageResource.getHeaderContribution(MoveEntitiesPage.CSS));
        this.selected = selectedEntities;
        this.page = page;
        allPatients = all;
        this.add(infoPanel = new InfoPanel());
        infoMsgId = checkSelection(selected);
        if (infoMsgId == null) {
            infoPanel.setSrcInfo(selected);
        }
        selectPanel();
    }

    public String checkSelection(SelectedEntities selected) {
        if (selected.getPpss().size() > 0) {
            return MSGID_ERR_SELECTION_MOVE_PPS;
        }
        int pats = selected.getPatients().size();
        if (pats > 1) {
            return MSGID_ERR_SELECTION_MOVE_DESTINATION;
        } 
        if( pats == 1) {
            PatientModel patModel = selected.getPatients().iterator().next();
            if (selected.getStudies().size() < 1) {
                if ( selected.hasSeries() && selected.hasInstances()) {
                    return MSGID_ERR_SELECTION_MOVE_SOURCE_LEVEL;
                } else if (selected.hasSeries()) {
                    for ( SeriesModel m : selected.getSeries()) {
                        if (Availability.valueOf(m.getAvailability()) != Availability.ONLINE) {
                            return MSGID_ERR_SELECTION_MOVE_NOT_ONLINE;
                        }
                    }
                    SeriesModel sm = selected.getSeries().iterator().next();
                    needNewStudy(sm.getPPS().getStudy(), patModel);
                } else if (selected.hasInstances()) {
                    for ( InstanceModel m : selected.getInstances()) {
                        if (Availability.valueOf(m.getAvailability()) != Availability.ONLINE) {
                            return MSGID_ERR_SELECTION_MOVE_NOT_ONLINE;
                        }
                    }
                    InstanceModel im = selected.getInstances().iterator().next();
                    needNewStudy(im.getSeries().getPPS().getStudy(), patModel);
                    needNewSeries(im.getSeries(), this.studyModel);
                } else {
                    return MSGID_ERR_SELECTION_MOVE_NO_SOURCE;
                }
            } else if (selected.hasSeries() || selected.hasInstances()) {
            } else {
                for ( StudyModel m : selected.getStudies()) {
                    if (Availability.valueOf(m.getAvailability()) != Availability.ONLINE) {
                        return MSGID_ERR_SELECTION_MOVE_NOT_ONLINE;
                    }
                }
            }
            destinationModel = patModel;
            return null;
        } 
        // series(inst) -> study
        int nrOfStudies = selected.getStudies().size();
        if ( nrOfStudies > 1) {
            return MSGID_ERR_SELECTION_MOVE_DESTINATION;
        } 
        if( nrOfStudies == 1) {
            if (selected.getSeries().size() < 1) {
                if ( selected.hasInstances()) {
                    for ( InstanceModel m : selected.getInstances()) {
                        if (Availability.valueOf(m.getAvailability()) != Availability.ONLINE) {
                            return MSGID_ERR_SELECTION_MOVE_NOT_ONLINE;
                        }
                    }
                    needNewSeries(selected.getInstances().iterator().next().getSeries(), 
                            selected.getStudies().iterator().next());
                } else {
                    return MSGID_ERR_SELECTION_MOVE_NO_SOURCE;
                }
            } else {
                for ( SeriesModel m : selected.getSeries()) {
                    if (Availability.valueOf(m.getAvailability()) != Availability.ONLINE) {
                        return MSGID_ERR_SELECTION_MOVE_NOT_ONLINE;
                    }
                }
            }
            destinationModel = selected.getStudies().iterator().next();
            return null;
        }
        // instances -> series
        int nrOfSeries = selected.getSeries().size();
        if ( nrOfSeries > 1) {
            return MSGID_ERR_SELECTION_MOVE_DESTINATION;
        } 
        if( nrOfSeries == 1) {
            if (selected.getInstances().size() < 1) {
                return MSGID_ERR_SELECTION_MOVE_NO_SOURCE;
            }
            destinationModel = selected.getSeries().iterator().next();
            return null;
        }
        return MSGID_ERR_SELECTION_MOVE_NO_SELECTION;
    }
    
    private void needNewStudy(StudyModel sourceStudy, final PatientModel pat) {
        missingState = missingState | MISSING_STUDY;
        studyModel = new StudyModel(null, pat);
        presetStudy(sourceStudy);
        newStudyPanel = new SimpleEditDicomObjectPanel("panel", new ResourceModel("move.newStudyForMove.text"),
                studyModel.getDataset(), getStudyEditAttributes() ) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onCancel() {
                doCancel();
            }

            @Override
            protected void onSubmit() {
                studyModel.update(getDicomObject());
                pat.getStudies().add(studyModel);
                selected.getPatients().clear();
                selected.getStudies().add(studyModel);
                missingState = missingState & ~MISSING_STUDY;
                selectPanel();
            }
        };
    }

    private void presetStudy(StudyModel sourceStudy) {
        DicomObject srcAttrs = sourceStudy.getDataset();
        DicomObject attrs = studyModel.getDataset();
        attrs.putString(Tag.AccessionNumber, VR.SH, srcAttrs.getString(Tag.AccessionNumber));
        attrs.putString(Tag.StudyID, VR.SH, srcAttrs.getString(Tag.StudyID));
        attrs.putString(Tag.StudyDescription, VR.LO, srcAttrs.getString(Tag.StudyDescription));
        attrs.putString(Tag.StudyDate, VR.DA, srcAttrs.getString(Tag.StudyDate));
        attrs.putString(Tag.StudyTime, VR.TM, srcAttrs.getString(Tag.StudyTime));
    }

    private void needNewSeries(SeriesModel sourceSeries, final StudyModel study) {
        missingState = missingState | MISSING_SERIES;
        seriesModel = new SeriesModel(null, null);
        new PPSModel(null, seriesModel, study);
        presetSeries(sourceSeries);
        newSeriesPanel = new SimpleEditDicomObjectPanel("panel", new ResourceModel("move.newSeriesForMove.text"),
                seriesModel.getDataset(), getSeriesEditAttributes() ) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onCancel() {
                doCancel();
            }

            @Override
            protected void onSubmit() {
                seriesModel.update(getDicomObject());
                selected.getStudies().clear();
                selected.getSeries().add(seriesModel);
                missingState = missingState & ~MISSING_SERIES;
                selectPanel();
            }
        };
    }

    private void presetSeries(SeriesModel sourceSeries) {
        DicomObject srcAttrs = sourceSeries.getDataset();
        DicomObject attrs = seriesModel.getDataset();
        attrs.putString(Tag.SeriesDescription, VR.LO, srcAttrs.getString(Tag.SeriesDescription));
        attrs.putString(Tag.SeriesNumber, VR.IS, srcAttrs.getString(Tag.SeriesNumber));
        attrs.putString(Tag.Modality, VR.CS, srcAttrs.getString(Tag.Modality));
        attrs.putString(Tag.SeriesDate, VR.DA, srcAttrs.getString(Tag.SeriesDate));
        attrs.putString(Tag.SeriesTime, VR.TM, srcAttrs.getString(Tag.SeriesTime));
        attrs.putString(attrs.resolveTag(PrivateTag.CallingAET, PrivateTag.CreatorID), VR.AE, 
                sourceSeries.getSourceAET());
    }
    
    private void doCancel() {
        DicomEditLocal dao = (DicomEditLocal) JNDIUtils.lookup(DicomEditLocal.JNDI_NAME);
        if (seriesModel != null && seriesModel.getPk() != -1) {
            dao.removeSeries(seriesModel.getPk());
            seriesModel.getPPS().getSeries().remove(seriesModel);
        }
        if (studyModel != null && studyModel.getPk() != -1) {
            dao.removeStudy(studyModel.getPk());
            studyModel.getPatient().getStudies().remove(studyModel);
        }
        setResponsePage(page);
    }

    private void selectPanel() {
        if ((missingState & MISSING_STUDY) != 0) {
            this.addOrReplace(this.newStudyPanel);
        } else if ((missingState & MISSING_SERIES) != 0) {
            this.addOrReplace(this.newSeriesPanel);
        } else {
            this.addOrReplace(this.infoPanel);
        }
    }
    
    private int[][] getStudyEditAttributes() {
        return new int[][]{{Tag.StudyInstanceUID},
                {Tag.StudyID},
                {Tag.StudyDescription},
                {Tag.AccessionNumber},
                {Tag.StudyDate, Tag.StudyTime}};
    }
    private int[][] getSeriesEditAttributes() {
        return new int[][]{{Tag.SeriesInstanceUID},
        {Tag.SeriesNumber},
        {Tag.Modality},
        {Tag.SeriesDate, Tag.SeriesTime},
        {Tag.SeriesDescription},
        {Tag.BodyPartExamined},{Tag.Laterality}};
    }

    private class InfoPanel extends Panel {

        private static final long serialVersionUID = 1L;
        private Label infoLabel;
        private AjaxFallbackLink<Object> moveBtn;
        private AjaxFallbackLink<Object> okBtn;
        private AjaxFallbackLink<Object> cancelBtn;
        private boolean ajaxRunning = false;
        private Image hourglassImage;
        
        private IModel<String> infoModel = new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                if (infoMsgId == null) {
                    return selectedInfoModel.getString();
                } else {
                    return MoveEntitiesPage.this.getString(infoMsgId);
                }
            }            
        };
        
        public InfoPanel() {
            super("panel");
            add( new Label("infoTitle", new ResourceModel("move.pageTitle")));
            add( infoLabel = new Label("info", infoModel));
            infoLabel.setOutputMarkupId(true);
            add((hourglassImage = new Image("hourglass-image", ImageManager.IMAGE_COMMON_AJAXLOAD) {

                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return ajaxRunning;
                }
            }).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));
            
            okBtn = new AjaxFallbackLink<Object>("moveFinishedBtn") {
                private static final long serialVersionUID = 1L;
                @Override
                public void onClick(AjaxRequestTarget target) {
                    selected.clear();
                    SelectedEntities.deselectAll(allPatients);
                    if (studyModel == null && seriesModel == null) {
                        refreshStudyAndSeries(destinationModel);
                        destinationModel.expand();
                    } else {
                        if (studyModel != null) {
                            studyModel.expand();
                            if (seriesModel != null) {//new study && new Series -> only one pps and one series
                                studyModel.getPPSs().iterator().next().getSeries().iterator().next().expand();
                            }
                        } else if (seriesModel != null) {
                            seriesModel.getPPS().getStudy().getPPSs().add(seriesModel.getPPS());
                            seriesModel.expand();
                        }
                    }
                    
                    for (AbstractDicomModel m : modifiedModels) {
                        if (m.levelOfModel() == AbstractDicomModel.PPS_LEVEL) {
                            m.getParent().expand();
                        } else {
                            m.expand();
                        }
                        refreshStudyAndSeries(m);
                    }
                    setResponsePage(page);
                }
            };
            okBtn.add(new TooltipBehaviour("folder.", "moveFinishedBtn"));
            okBtn.add(new Label("moveFinishedText", new ResourceModel("folder.moveFinishedBtn.text"))
                .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle"))));
            okBtn.setVisible(false).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
            add(okBtn);
            moveBtn = new AjaxFallbackLink<Object>("moveBtn") {
                private static final long serialVersionUID = 1L;
                @Override
                public void onClick(AjaxRequestTarget target) {
                    try {
                        infoMsgId = "move.message.move.running";
                        okBtn.setVisible(false);
                        cancelBtn.setVisible(false);
                        ajaxRunning = true;
                        
                        infoLabel.add(new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
                            
                            private static final long serialVersionUID = 1L;

                            @Override
                            protected void onTimer(AjaxRequestTarget target) {
                                try {
                                    int nrOfMovedInstances = ContentEditDelegate.getInstance().moveEntities(selected);
                                    if (nrOfMovedInstances > 0) {
                                        infoMsgId = "move.message.moveDone";
                                    } else if (nrOfMovedInstances == 0) {
                                        infoMsgId = "move.message.moveNothing";
                                    } else {
                                        infoMsgId = "move.message.moveFailed";
                                        cancelBtn.setVisible(true);
                                    }
                                    okBtn.setVisible(true);
                                    moveBtn.setVisible(false);
                                    
                                } catch (SelectionException x) {
                                    log.warn(x.getMessage());
                                    infoMsgId = x.getMsgId();
                                }
                                //queryStudies();
                                this.stop();
                                ajaxRunning = false;
                                addToTarget(target);
                            }
                        });
                    } catch (Throwable t) {
                        log.error("Can not move selected entities!", t);
                        infoMsgId = "move.message.moveFailed";
                    }
                    addToTarget(target);
                }
                @Override
                public boolean isEnabled() {
                    return infoMsgId == null;
                }

            };
            moveBtn.add(new Image("moveImg",ImageManager.IMAGE_FOLDER_MOVE)
            .add(new ImageSizeBehaviour("vertical-align: middle;")));
            moveBtn.add(new TooltipBehaviour("folder.", "moveBtn"));
            moveBtn.add(new Label("moveText", new ResourceModel("folder.moveBtn.text"))
                .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
            );
            add(moveBtn.setOutputMarkupId(true));
            cancelBtn = new AjaxFallbackLink<Object>("cancelBtn") {
                private static final long serialVersionUID = 1L;
                @Override
                public void onClick(AjaxRequestTarget target) {
                    doCancel();
                }
            };
            add(cancelBtn.add(new Label("cancelLabel", new ResourceModel("cancelBtn"))).setOutputMarkupId(true) );
        }

        private void refreshStudyAndSeries(AbstractDicomModel parent) {
            int level = parent.levelOfModel();
            if ( level == AbstractDicomModel.INSTANCE_LEVEL) {
                parent = parent.getParent();
                level--;
            }
            for ( ; level > 0 ; level-- ) {
                ((AbstractEditableDicomModel)parent).refresh();
                parent = parent.getParent();
            }
        }
        
        public void setSrcInfo(SelectedEntities selected) {
            SelectedInfo info = null;
            if (selected.hasInstances()) {
                addModifiedModels(selected.getInstances());
                info = new SelectedInfo(selected.getInstances().iterator().next(),
                        selected.getInstances().size());
            } else if (selected.hasSeries()) {
                addModifiedModels(selected.getSeries());
                info = new SelectedInfo(selected.getSeries().iterator().next(),
                        selected.getSeries().size());
            } else if (selected.hasStudies()) {
                addModifiedModels(selected.getStudies());
                info = new SelectedInfo(selected.getStudies().iterator().next(),
                        selected.getStudies().size());
            }
            selectedInfoModel = getSelectedInfoModel("move.selectedToMove_${level}.text", info, "Selected:?");
        }
        
        private void addModifiedModels(Set<? extends AbstractDicomModel> models) {
            for (AbstractDicomModel m : models) {
                modifiedModels.add(m.getParent());
            }
        }

        @SuppressWarnings("unchecked")
        private StringResourceModel getSelectedInfoModel(String key, SelectedInfo info, String def) {
            if (info == null ) {
                return new StringResourceModel(key, MoveEntitiesPage.this, null, null, def);
            } else {
                Model m = new Model(info);
                Object[] params = new Object[]{
                    new PropertyModel<String>(info, "patientName"),
                    new PropertyModel<String>(info, "patientId"),
                    new PropertyModel<String>(info, "birthdate"),
                    new PropertyModel<String>(info, "sex"),
                    new PropertyModel<String>(info, "studyIUID"),
                    new PropertyModel<String>(info, "accessionNumber"),
                    new PropertyModel<String>(info, "description"),
                    new PropertyModel<String>(info, "studyId"),
                    new PropertyModel<String>(info, "studyDate"),
                    new PropertyModel<String>(info, "level"),
                    new PropertyModel<String>(info, "count")
                };
                return new StringResourceModel(key, MoveEntitiesPage.this, m, params, def);
            }
        }

        private void addToTarget(AjaxRequestTarget target) {
            target.addComponent(infoLabel);
            target.addComponent(hourglassImage);
            target.addComponent(okBtn);
            target.addComponent(moveBtn);
            target.addComponent(cancelBtn);
        }

    }
    //used in a PropertyModel
    @SuppressWarnings("unused")
    private class SelectedInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private PatientModel pat;
        private StudyModel study;
        String level;
        int count;
        
        private SelectedInfo(String level, int count) {
            this.level = level;
            this.count = count;
            
        }
        SelectedInfo(PatientModel m) {
            this("patient", 1);
            pat = m;
        }
        public SelectedInfo(StudyModel m, int count) {
            this("study", count);
            pat = m.getPatient();
            study = m;
        }
        public SelectedInfo(SeriesModel m, int count) {
            this("series", count);
            study = m.getPPS().getStudy();
            pat = study.getPatient();
        }
        public SelectedInfo(InstanceModel m, int count) {
            this("instance", count);
            study = m.getSeries().getPPS().getStudy();
            pat = study.getPatient();
        }
        
        public String getLevel() {
            return level;
        }
        public int getCount() {
            return count;
        }
        
        public String getPatientName() {
            return pat.getName();
        }
        public String getPatientId() {
            return pat.getIssuer() == null ? pat.getId() : pat.getId()+"/"+pat.getIssuer();
        }
        public Date getBirthdate() {
            return pat.getBirthdate();
        }
        public String getSex() {
            return pat.getSex();
        }
        public String getStudyIUID() {
            return study == null ? null : study.getStudyInstanceUID();
        }
        public String getAccessionNumber() {
            return study == null ? null : study.getAccessionNumber();
        }
        public String getDescription() {
            return study == null ? null : study.getDescription();
        }
        public String getStudyId() {
            return study == null ? null : study.getId();
        }
        public Date getStudyDate() {
            return study == null ? null : study.getDatetime();
        }
    }

}
