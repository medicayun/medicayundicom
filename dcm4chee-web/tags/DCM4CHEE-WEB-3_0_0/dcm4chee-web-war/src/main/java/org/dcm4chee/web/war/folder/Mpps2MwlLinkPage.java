 /* Version: MPL 1.1/GPL 2.0/LGPL 2.1
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.DateTimeLabel;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.vo.MppsToMwlLinkResult;
import org.dcm4chee.web.war.AuthenticatedWebSession;
import org.dcm4chee.web.war.StudyPermissionHelper;
import org.dcm4chee.web.war.config.delegate.WebCfgDelegate;
import org.dcm4chee.web.war.folder.delegate.ContentEditDelegate;
import org.dcm4chee.web.war.folder.model.PPSModel;
import org.dcm4chee.web.war.folder.model.PatientModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.dcm4chee.web.war.worklist.modality.MWLItemListView;
import org.dcm4chee.web.war.worklist.modality.ModalityWorklistPanel;
import org.dcm4chee.web.war.worklist.modality.ViewPort;
import org.dcm4chee.web.war.worklist.modality.model.MWLItemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since May 07, 2010
 */
public class Mpps2MwlLinkPage extends ModalWindow {

    private static final long serialVersionUID = 1L;
    
    private static final long ONE_DAY_IN_MILLIS = 60000*60*24;
    private static final ResourceReference CSS = new CompressedResourceReference(Mpps2MwlLinkPage.class, "mpps-link-style.css");
    
    private Mpps2MwlLinkPanelM panel = new Mpps2MwlLinkPanelM("content");
    private List<PPSModel> ppsModels;
    private PPSModel ppsModelForInfo;
    private PatientModel ppsPatModelForInfo;
    private Component comp;
    
    private static Logger log = LoggerFactory.getLogger(Mpps2MwlLinkPage.class);
    
    StudyListLocal dao = (StudyListLocal) JNDIUtils.lookup(StudyListLocal.JNDI_NAME);

    public Mpps2MwlLinkPage(String id) {
        super(id);
        
        if (Mpps2MwlLinkPage.CSS != null)
            add(CSSPackageResource.getHeaderContribution(Mpps2MwlLinkPage.CSS));
        
        setContent(panel);
    }

    public Button getSearchButton() {
        return panel.getSearchButton();
    }
    public void show(AjaxRequestTarget target, PPSModel ppsModel, Component c) {
        ppsModels  = toList(ppsModel);
        ppsModelForInfo = ppsModels.get(0);
        ppsPatModelForInfo = ppsModelForInfo.getStudy().getPatient();
        panel.presetSearchfields();
        panel.getViewPort().clear();
        comp = c;
        if (WebCfgDelegate.getInstance().isMpps2mwlAutoQuery() ) {
            target.appendJavascript("hideMask();document.getElementById('" + panel.getSearchButton().getMarkupId() +
                "').click();");
        }
        super.show(target);
    }
    
    private void hideLinkedPpsInFolder(org.dcm4chee.web.war.folder.ViewPort viewport) {
        StudyModel study;
        for (PPSModel mpps : ppsModels) {
            log.debug("hideLinkedPpsInFolder! pps:"+mpps);
            study  = mpps.getStudy();
            study.getPPSs().remove(mpps);
            if (study.isCollapsed()) {
                study.getPatient().getStudies().remove(study);
                if (study.getPatient().isCollapsed()) {
                    viewport.getPatients().remove(study.getPatient());
                }
            }
        }
    }

    private static List<PPSModel> toList(PPSModel ppsModel) {
        ArrayList<PPSModel> l = new ArrayList<PPSModel>(1);
        l.add(ppsModel);
        return l;
    }

    public class Mpps2MwlLinkPanelM extends ModalityWorklistPanel {

        private static final long serialVersionUID = 1L;

        public Mpps2MwlLinkPanelM(final String id) {
            super(id);
            addMppsInfoPanel();
        }
        
        public Button getSearchButton() {
            return searchBtn;
        }

        protected ViewPort initViewPort() {
            return new ViewPort();
        }

        protected WebMarkupContainer addExtendedStudySearch(final Form<?> form) {
            return null;
        }

        @Override
        public void addMwlActions(final ListItem<MWLItemModel> item, WebMarkupContainer valueContainer, final MWLItemListView mwlListView) {
            final MWLItemModel mwlItemModel = item.getModelObject();

            valueContainer.add(new WebMarkupContainer("cell"));
            valueContainer.add(new AjaxFallbackLink<Object>("link") {
                private static final long serialVersionUID = 1L;
                @Override
                public void onClick(AjaxRequestTarget target) {
                    log.info("Link MPPS to MWL!:"+mwlItemModel);
                    try {
                        MppsToMwlLinkResult result = ContentEditDelegate.getInstance().linkMppsToMwl(ppsModels, mwlItemModel);
                        org.dcm4chee.web.war.folder.ViewPort viewport = ((AuthenticatedWebSession) getSession()).getFolderViewPort();
                        int nrOfStudies = result.getStudiesToMove().size();
                        boolean hideLinkedPps = ((AuthenticatedWebSession) getSession()).getFolderViewPort().getFilter().isPpsWithoutMwl();
                        if (nrOfStudies == 0) {
                            if (hideLinkedPps) {
                                hideLinkedPpsInFolder(viewport);
                            } else {
                                for (PPSModel mpps : ppsModels) {
                                    mpps.getStudy().refresh().expand();
                                }
                            }
                        } else {
                            hideLinkedPpsInFolder(viewport);
                            if (!hideLinkedPps) {
                                List<PatientModel> pats = viewport.getPatients();
                                PatientModel patModel = new PatientModel(result.getMwl().getPatient(), new Model<Boolean>(false));
                                int pos = pats.indexOf(patModel);
                                if (pos == -1) {
                                    pats.add(patModel);
                                } else {
                                    patModel = pats.get(pos);
                                }
                                StudyModel sm;
                                List<StudyModel> studies = patModel.getStudies();
                                for (Study s : result.getStudiesToMove()) {
                                    sm = new StudyModel(s, patModel, 
                                            dao.findStudyPermissionActions(s.getStudyInstanceUID(), 
                                                    StudyPermissionHelper.get().getStudyPermissionRight().equals(StudyPermissionHelper.StudyPermissionRight.ALL) ?
                                                            null : StudyPermissionHelper.get().getDicomRoles()));
                                    sm.refresh().expand();
                                    studies.add(sm);
                                }
                            }
                        }
                        target.addComponent(comp);
                        close(target);
                    } catch (Exception e) {
                        log.error("MPPS to MWL link failed!", e);
                    }
                }
            }.add(new Image("linkImg",ImageManager.IMAGE_COMMON_LINK)
            .add(new ImageSizeBehaviour())
            .add(new TooltipBehaviour("mpps2mwl.", "link"))));
        }    

        private void addMppsInfoPanel() {
            WebMarkupContainer p = new WebMarkupContainer("mppsInfo", new CompoundPropertyModel<Object>(new PpsInfoModel()));
            p.add(new Label("mppsInfoTitle", new ResourceModel("link.ppsInfoTitle")));
            p.add(new Label("patientName.label", new ResourceModel("link.patNameLabel")));
            p.add(new Label("patientName"));
            p.add(new Label("patientID.label", new ResourceModel("link.patIdLabel")));
            p.add(new Label("patientID"));
            p.add(new Label("patientIssuer.label", new ResourceModel("link.patIssuerLabel")));
            p.add(new Label("patientIssuer"));
            p.add(new Label("birthdate.label", new ResourceModel("link.birthdateLabel")));
            p.add(new DateTimeLabel("birthdate").setWithoutTime(true));
            p.add(new Label("modality.label", new ResourceModel("link.modalityLabel")));
            p.add(new Label("modality"));
            p.add(new Label("startDate.label", new ResourceModel("link.startDateLabel")));
            p.add(new DateTimeLabel("datetime"));
            p.add(new Label("studyDescription.label", new ResourceModel("link.studyDescriptionLabel")));
            p.add(new Label("studyDescription"));
            add(p);
        }
        
        @SuppressWarnings("unchecked")
        private void presetSearchfields() {
            PPSModel ppsModel = ppsModels.get(0);
            String patPreset = WebCfgDelegate.getInstance().getMpps2mwlPresetPatientname();
            if ("delete".equals(patPreset)) {
                getViewPort().getFilter().setPatientName(null);
            } else if (patPreset != null) {
                PatientModel patModel = ppsModel.getStudy().getPatient();
                String name = patModel.getName();
                if ( !"*".equals(patPreset)) {
                    int nrofChars = Integer.parseInt(patPreset);
                    if (name != null && name.length() > nrofChars)
                        name = name.substring(0,nrofChars);
                }
                getViewPort().getFilter().setPatientName(name);
            }
            String modPreset = WebCfgDelegate.getInstance().getMpps2mwlPresetModality();
            if ("delete".equals(modPreset)) {
                getViewPort().getFilter().setModality(null);
            } else if ("mpps".equals(modPreset)){
                String mod = ppsModel.getModality();
                DropDownChoice<String> modChoice = (DropDownChoice<String>) this.get("form:searchDropdowns:modality");
                if(modChoice.getChoices().contains(mod))
                    getViewPort().getFilter().setModality(mod);
            }
            String startPreset = WebCfgDelegate.getInstance().getMpps2mwlPresetStartDate();
            if ("delete".equals(startPreset)) {
                getViewPort().getFilter().setStartDateMin(null);
                getViewPort().getFilter().setStartDateMax(null);
            } else if (startPreset != null) {
                Calendar calMin = Calendar.getInstance();
                Calendar calMax = Calendar.getInstance();
                if (startPreset.startsWith("mpps")) {
                    calMin.setTime(ppsModel.getDatetime());
                    calMax.setTime(ppsModel.getDatetime());
                }
                extendRange(startPreset, calMin, calMax);
                calMin.set(Calendar.HOUR_OF_DAY, 0);
                calMin.set(Calendar.MINUTE, 0);
                calMin.set(Calendar.MILLISECOND, 0);
                getViewPort().getFilter().setStartDateMin(calMin.getTime());
                calMax.set(Calendar.HOUR_OF_DAY, 23);
                calMax.set(Calendar.MINUTE, 59);
                calMax.set(Calendar.MILLISECOND, 999);
                getViewPort().getFilter().setStartDateMax(calMax.getTime());
            }
        }

        private void extendRange(String s, Calendar calMin, Calendar calMax) {
            int pos = s.indexOf('(');
            if (pos != -1) {
                try {
                    int pos1 = s.indexOf(',', ++pos);
                    int startOffset = Integer.parseInt(s.substring(pos, pos1));
                    int endOffset = Integer.parseInt(s.substring(++pos1, s.indexOf(')',pos1)));
                    if (startOffset != 0)
                        calMin.setTimeInMillis(calMin.getTimeInMillis() + startOffset * ONE_DAY_IN_MILLIS);
                    if (endOffset != 0)
                        calMax.setTimeInMillis(calMax.getTimeInMillis() + endOffset * ONE_DAY_IN_MILLIS);
                } catch (Exception x) {
                    log.warn("Configuration Error! Can not extend date range! config:"+s);
                }
            }
        }
    }

    @SuppressWarnings("unused") //used in a PropertyModel
    private class PpsInfoModel implements Serializable{
        private static final long serialVersionUID = 1L;

        public String getPatientName() {
            return ppsPatModelForInfo.getName();
        }
        public String getPatientID() {
            return ppsPatModelForInfo.getId();
        }
        public Date getBirthdate() {
            return ppsPatModelForInfo.getBirthdate();
        }
        public String getPatientIssuer() {
            return ppsPatModelForInfo.getIssuer();
        }
        public String getModality() {
            return ppsModelForInfo.getModality();
        }
        public Date getDatetime() {
            return ppsModelForInfo.getDatetime();
        }
        public String getStudyDescription() {
            return ppsModelForInfo.getStudy().getDescription();
        }
        public String getStudyId() {
            return ppsModelForInfo.getStudy().getId();
        }
    }
}
