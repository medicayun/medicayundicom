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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.CloseButtonCallback;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.security.hive.authorization.Principal;
import org.apache.wicket.security.swarm.strategies.SwarmStrategy;
import org.apache.wicket.util.time.Duration;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4chee.archive.common.PrivateTag;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.StudyPermission;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.CheckOneDayBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.DateTimeLabel;
import org.dcm4chee.web.common.markup.ModalWindowLink;
import org.dcm4chee.web.common.markup.PopupLink;
import org.dcm4chee.web.common.markup.SimpleDateTimeField;
import org.dcm4chee.web.common.markup.ModalWindowLink.DisableDefaultConfirmBehavior;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.web.common.secure.SecureSession;
import org.dcm4chee.web.common.secure.SecurityBehavior;
import org.dcm4chee.web.common.validators.UIDValidator;
import org.dcm4chee.web.common.webview.link.WebviewerLinkProvider;
import org.dcm4chee.web.dao.folder.StudyListFilter;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.dcm4chee.web.war.AuthenticatedWebSession;
import org.dcm4chee.web.war.StudyPermissionHelper;
import org.dcm4chee.web.war.StudyPermissionHelper.StudyPermissionRight;
import org.dcm4chee.web.war.ajax.MaskingAjaxCallDecorator;
import org.dcm4chee.web.war.common.DisplayDicomObjectPanel;
import org.dcm4chee.web.war.common.EditDicomObjectPanel;
import org.dcm4chee.web.war.common.IndicatingAjaxFormSubmitBehavior;
import org.dcm4chee.web.war.common.SimpleEditDicomObjectPanel;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.common.model.AbstractEditableDicomModel;
import org.dcm4chee.web.war.config.delegate.WebCfgDelegate;
import org.dcm4chee.web.war.folder.delegate.ContentEditDelegate;
import org.dcm4chee.web.war.folder.delegate.MppsEmulateDelegate;
import org.dcm4chee.web.war.folder.model.FileModel;
import org.dcm4chee.web.war.folder.model.InstanceModel;
import org.dcm4chee.web.war.folder.model.PPSModel;
import org.dcm4chee.web.war.folder.model.PatientModel;
import org.dcm4chee.web.war.folder.model.SeriesModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.dcm4chee.web.war.folder.studypermissions.StudyPermissionsPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudyListPage extends Panel {

    private static final ResourceReference CSS = new CompressedResourceReference(StudyListPage.class, "folder-style.css");
    
    private static ModalWindow modalWindow;
    
    private IModel<Integer> pagesize = new IModel<Integer>() {

        private static final long serialVersionUID = 1L;

        private int pagesize = WebCfgDelegate.getInstance().getDefaultFolderPagesize();

        public Integer getObject() {
            return pagesize;
        }
        
        public void setObject(Integer object) {
            if (object != null)
                pagesize = object;
        }
        
        public void detach() {}
    };

    private static final String MODULE_NAME = "folder";
    private static final long serialVersionUID = 1L;
    private static Logger log = LoggerFactory.getLogger(StudyListPage.class);
    private ViewPort viewport = ((AuthenticatedWebSession) AuthenticatedWebSession.get()).getFolderViewPort();
    private StudyListHeader header;
    private SelectedEntities selected = new SelectedEntities();

    private IModel<Boolean> latestStudyFirst = new AbstractReadOnlyModel<Boolean>() {
        private static final long serialVersionUID = 1L;
        @Override
        public Boolean getObject() {
            return viewport.getFilter().isLatestStudiesFirst();
        }
    };
    private boolean showSearch = true;
    private boolean notSearched = true;
    private BaseForm form;
    private MessageWindow msgWin = new MessageWindow("msgWin");
    private Mpps2MwlLinkPage mpps2MwlLinkWindow = new Mpps2MwlLinkPage("linkPage");
    private ConfirmationWindow<PPSModel> confirmUnlinkMpps;
    private ConfirmationWindow<PPSModel> confirmEmulateMpps;
    private ImageSelectionWindow imageSelectionWindow = new ImageSelectionWindow("imgSelection");
    private ModalWindow wadoImageWindow = new ModalWindow("wadoImageWindow");
    
    private WebviewerLinkProvider webviewerLinkProvider;
    
    private List<WebMarkupContainer> searchTableComponents = new ArrayList<WebMarkupContainer>();
     
    StudyListLocal dao = (StudyListLocal) JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
    
    StudyPermissionHelper studyPermissionHelper;
    
    public StudyListPage(final String id) {
        super(id);        
        
        if (StudyListPage.CSS != null)
            add(CSSPackageResource.getHeaderContribution(StudyListPage.CSS));

        studyPermissionHelper = StudyPermissionHelper.get();
        
        add(modalWindow = new ModalWindow("modal-window"));
        modalWindow.setWindowClosedCallback(new WindowClosedCallback() {
            private static final long serialVersionUID = 1L;

            public void onClose(AjaxRequestTarget target) {
                getPage().setOutputMarkupId(true);
                target.addComponent(getPage());
            }            
        });
        webviewerLinkProvider = new WebviewerLinkProvider(WebCfgDelegate.getInstance().getWebviewerName());
        webviewerLinkProvider.setBaseUrl(WebCfgDelegate.getInstance().getWebviewerBaseUrl());
        
        final StudyListFilter filter = viewport.getFilter();
        add(form = new BaseForm("form", new CompoundPropertyModel<Object>(filter)));
        form.setResourceIdPrefix("folder.");
        form.add(new AjaxFallbackLink<Object>("searchToggle") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                showSearch = !showSearch;
                for (WebMarkupContainer wmc : searchTableComponents)
                    wmc.setVisible(showSearch);               
                target.addComponent(form);
            }
        }
        .add((new Image("searchToggleImg", new AbstractReadOnlyModel<ResourceReference>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ResourceReference getObject() {
                    return showSearch ? ImageManager.IMAGE_COMMON_COLLAPSE : 
                        ImageManager.IMAGE_COMMON_EXPAND;
                }
        })
        .add(new TooltipBehaviour("folder.", "searchToggleImg", new AbstractReadOnlyModel<Boolean>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return showSearch;
            }
        })))
        .add(new ImageSizeBehaviour())));

        addQueryFields(filter, form);
        addQueryOptions(form);
        addNavigation(form);
        addActions(form);
        
        header = new StudyListHeader("thead", form);
        form.add(header);
        form.add(new PatientListView("patients", viewport.getPatients()));
        msgWin.setTitle("");
        add(msgWin);
        Form<Object> form1 = new Form<Object>("modalForm");
        add(form1);
        form1.add(mpps2MwlLinkWindow);
        add(imageSelectionWindow);
        imageSelectionWindow.setWindowClosedCallback(new WindowClosedCallback(){
            private static final long serialVersionUID = 1L;

            public void onClose(AjaxRequestTarget target) {
                if (imageSelectionWindow.isSelectionChanged())
                    target.addComponent(form);
            }            
        });
        imageSelectionWindow.add(new SecurityBehavior(getModuleName() + ":imageSelectionWindow"));
        add(wadoImageWindow);
        wadoImageWindow.add(new SecurityBehavior(getModuleName() + ":wadoImageWindow"));
    }

    @SuppressWarnings("unchecked")
    private void addQueryFields(final StudyListFilter filter, final BaseForm form) {
        final IModel<Boolean> enabledModelPat = new AbstractReadOnlyModel<Boolean>(){
            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return (!filter.isExtendedQuery() || 
                        (QueryUtil.isUniversalMatch(filter.getStudyInstanceUID()) && 
                         QueryUtil.isUniversalMatch(filter.getSeriesInstanceUID())));
            }
        };
        final IModel<Boolean> enabledModel = new AbstractReadOnlyModel<Boolean>(){
            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return !filter.isPatientQuery() && (!filter.isExtendedQuery() || 
                        (QueryUtil.isUniversalMatch(filter.getStudyInstanceUID()) && 
                         QueryUtil.isUniversalMatch(filter.getSeriesInstanceUID())));
            }
        };
        
        searchTableComponents.add(form.createAjaxParent("searchLabels"));
        
        form.addInternalLabel("patientName");
        form.addInternalLabel("patientIDDescr");
        form.addInternalLabel("studyDate");
        form.addInternalLabel("accessionNumber");
        
        searchTableComponents.add(form.createAjaxParent("searchFields"));
        
        form.addPatientNameField("patientName", new PropertyModel<String>(filter, "patientName"),
                    WebCfgDelegate.getInstance().useFamilyAndGivenNameQueryFields(), enabledModelPat, false);
        form.addTextField("patientID", enabledModelPat, true);
        form.addTextField("issuerOfPatientID", enabledModelPat, true);
        SimpleDateTimeField dtf = form.addDateTimeField("studyDateMin", new PropertyModel<Date>(filter, "studyDateMin"), 
                enabledModel, false, true);
        SimpleDateTimeField dtfEnd = form.addDateTimeField("studyDateMax", new PropertyModel<Date>(filter, "studyDateMax"), enabledModel, true, true);
        dtf.addToDateField(new CheckOneDayBehaviour(dtf, dtfEnd, "onchange"));
        form.addTextField("accessionNumber", enabledModel, false);

        searchTableComponents.add(form.createAjaxParent("searchDropdowns"));
        
        form.addInternalLabel("modality");
        form.addInternalLabel("sourceAET");
        
        form.addDropDownChoice("modality", null, WebCfgDelegate.getInstance().getModalityList(), 
                enabledModel, false).setModelObject("*");
        form.addDropDownChoice("sourceAET", null, viewport.getSourceAetChoices(
                WebCfgDelegate.getInstance().getSourceAETList()), enabledModel, false).setModelObject("*");

        final WebMarkupContainer extendedFilter = new WebMarkupContainer("extendedFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return showSearch && filter.isExtendedQuery();
            }
        };
        extendedFilter.add( new Label("birthDate.label", new ResourceModel("folder.extendedFilter.birthDate.label")));
        extendedFilter.add( new Label("birthDateMin.label", new ResourceModel("folder.extendedFilter.birthDateMin.label")));
        extendedFilter.add( new Label("birthDateMax.label", new ResourceModel("folder.extendedFilter.birthDateMax.label")));
        SimpleDateTimeField dtfB = form.getDateTextField("birthDateMin", null, "extendedFilter.", enabledModelPat);
        SimpleDateTimeField dtfBEnd = form.getDateTextField("birthDateMax", null, "extendedFilter.", enabledModelPat);
        dtfB.addToDateField(new CheckOneDayBehaviour(dtfB, dtfBEnd, "onchange"));
        extendedFilter.add(dtfB);
        extendedFilter.add(dtfBEnd);
        extendedFilter.add( new Label("studyInstanceUID.label", new ResourceModel("folder.extendedFilter.studyInstanceUID.label")));
        extendedFilter.add( new TextField<String>("studyInstanceUID"){
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled() {
                return !filter.isPatientQuery();
            }            
        }.add(new UIDValidator()));

        extendedFilter.add( new Label("seriesInstanceUID.label", new ResourceModel("folder.extendedFilter.seriesInstanceUID.label")));
        extendedFilter.add( new TextField<String>("seriesInstanceUID") {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isEnabled() {
                return !filter.isPatientQuery() && QueryUtil.isUniversalMatch(filter.getStudyInstanceUID());
            }
        }.add(new UIDValidator()));
        form.add(extendedFilter);
        
        searchTableComponents.add(form.createAjaxParent("searchFooter"));
        
        AjaxFallbackLink<?> link = new AjaxFallbackLink<Object>("showExtendedFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                filter.setExtendedQuery(!filter.isExtendedQuery());
                target.addComponent(form);
            }
        };
        link.add((new Image("showExtendedFilterImg", new AbstractReadOnlyModel<ResourceReference>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ResourceReference getObject() {
                    return filter.isExtendedQuery() ? ImageManager.IMAGE_COMMON_COLLAPSE : 
                        ImageManager.IMAGE_COMMON_EXPAND;
                }
        })
        .add(new TooltipBehaviour("folder.search.", "showExtendedFilterImg", new AbstractReadOnlyModel<Boolean>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return filter.isExtendedQuery();
            }
        })))
        .add(new ImageSizeBehaviour()));
        form.addComponent(link);        
    }

    private void addQueryOptions(final BaseForm form) {

        final CheckBox chkLatestStudyFirst = form.addLabeledCheckBox("latestStudiesFirst", null);
        final CheckBox chkPpsWoMwl = form.addLabeledCheckBox("ppsWithoutMwl", null);
        
        final List<String> searchOptions = new ArrayList<String>(2);
        searchOptions.add(new ResourceModel("folder.searchOptions.patient").wrapOnAssignment(this).getObject());
        searchOptions.add(new ResourceModel("folder.searchOptions.study").wrapOnAssignment(this).getObject());
        final Model<String> searchOptionSelected = new Model<String>(searchOptions.get(1));
        form.addDropDownChoice("queryType", searchOptionSelected, searchOptions, 
                new Model<Boolean>(true), true)
                .add(new AjaxFormComponentUpdatingBehavior("onchange") {
                    
                    private static final long serialVersionUID = 1L;

                        protected void onUpdate(AjaxRequestTarget target) {
                            boolean b = searchOptionSelected.getObject().equals(searchOptions.get(0));
                            viewport.getFilter().setPatientQuery(b);
                            chkLatestStudyFirst.setEnabled(!b);
                            chkPpsWoMwl.setEnabled(!b);
                            header.expandToLevel(b ? PatientModel.PATIENT_LEVEL : PatientModel.STUDY_LEVEL);
                            BaseForm.addFormComponentsToAjaxRequestTarget(target, form);
                        }
                });
    }

    private void addNavigation(final BaseForm form) {

        Button resetBtn = new AjaxButton("resetBtn") {
            
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                form.clearInput();
                viewport.clear();
                ((DropDownChoice) ((WebMarkupContainer) form.get("searchDropdowns")).get("modality")).setModelObject("*");
                ((DropDownChoice) ((WebMarkupContainer) form.get("searchDropdowns")).get("sourceAET")).setModelObject("*");
                pagesize.setObject(WebCfgDelegate.getInstance().getDefaultFolderPagesize());
                notSearched = true;
                form.setOutputMarkupId(true);
                target.addComponent(form);
            }
        };
        resetBtn.setDefaultFormProcessing(false);
        resetBtn.add(new Image("resetImg",ImageManager.IMAGE_COMMON_RESET)
        .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        resetBtn.add(new Label("resetText", new ResourceModel("folder.searchFooter.resetBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.addComponent(resetBtn);

        IndicatingAjaxButton searchBtn = new IndicatingAjaxButton("searchBtn") {

            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    viewport.setOffset(0);
                    queryStudies();
                } catch (Throwable t) {
                    log.error("search failed: ", t);
                }
                target.addComponent(form);
            }
            
            @Override
            public void onError(AjaxRequestTarget target, Form<?> form) {
                BaseForm.addInvalidComponentsToAjaxRequestTarget(target, form);
            }
            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new MaskingAjaxCallDecorator();
            }

        };
        searchBtn.setOutputMarkupId(true);
        searchBtn.add(new Image("searchImg",ImageManager.IMAGE_COMMON_SEARCH)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        searchBtn.add(new Label("searchText", new ResourceModel("folder.searchFooter.searchBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle;")))
            .setOutputMarkupId(true)
        );
        
        form.addComponent(searchBtn);
        form.setDefaultButton(searchBtn);
        form.clearParent();
        
        form.addDropDownChoice("pagesize", pagesize, 
                WebCfgDelegate.getInstance().getPagesizeList(), 
                new Model<Boolean>(true), 
                true)
         .setNullValid(false)
        .add(new IndicatingAjaxFormSubmitBehavior(form, "onchange", searchBtn) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (!WebCfgDelegate.getInstance().isQueryAfterPagesizeChange())
                    return;
                try {
                    queryStudies();
                } catch (Throwable t) {
                    log.error("search failed: ", t);
                }
                target.addComponent(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
            }
            
            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new MaskingAjaxCallDecorator();
            }
        });

        form.add(new Link<Object>("prev") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                viewport.setOffset(Math.max(0, viewport.getOffset() - pagesize.getObject()));
                queryStudies();               
            }
            
            @Override
            public boolean isVisible() {
                return (!notSearched && !(viewport.getOffset() == 0));
            }
        }
        .add(new Image("prevImg", ImageManager.IMAGE_COMMON_BACK)
        .add(new ImageSizeBehaviour("vertical-align: middle;"))
        .add(new TooltipBehaviour("folder.search.")))
        );

        form.add(new Link<Object>("next") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                viewport.setOffset(viewport.getOffset() + pagesize.getObject());
                queryStudies();
            }

            @Override
            public boolean isVisible() {
                return (!notSearched && !(viewport.getTotal() - viewport.getOffset() <= pagesize.getObject()));
            }
        }
        .add(new Image("nextImg", ImageManager.IMAGE_COMMON_FORWARD)
        .add(new ImageSizeBehaviour("vertical-align: middle;"))
        .add(new TooltipBehaviour("folder.search.")))
        .setVisible(!notSearched)
        );

        //viewport label: use StringResourceModel with key substitution to select 
        //property key according notSearched and getTotal.
        Model<?> keySelectModel = new Model<Serializable>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Serializable getObject() {
                return notSearched ? "folder.search.notSearched" : 
                    viewport.getFilter().isPatientQuery() ? 
                            (viewport.getTotal() == 0 ? "folder.search.noMatchingPatientsFound" : 
                            "folder.search.patientsFound")
                            : (viewport.getTotal() == 0 ? "folder.search.noMatchingStudiesFound" : 
                                "folder.search.studiesFound");
            }
        };
        form.add(new Label("viewport", new StringResourceModel("${}", StudyListPage.this, keySelectModel,new Object[]{"dummy"}){

            private static final long serialVersionUID = 1L;

            @Override
            protected Object[] getParameters() {
                return new Object[]{viewport.getOffset()+1,
                        Math.min(viewport.getOffset() + pagesize.getObject(), viewport.getTotal()),
                        viewport.getTotal()};
            }
        }));
    }

    private void addActions(final BaseForm form) {
        
        final ConfirmationWindow<SelectedEntities> confirmDelete = new ConfirmationWindow<SelectedEntities>("confirmDelete") {

            private static final long serialVersionUID = 1L;

            private transient ContentEditDelegate delegate;
            
            private ContentEditDelegate getDelegate() {
                if (delegate == null) {
                    delegate = ContentEditDelegate.getInstance();
                }
                return delegate;
            }

            @Override
            public void onOk(AjaxRequestTarget target) {
                target.addComponent(form);
            }

            @Override
            public void close(AjaxRequestTarget target) {
                target.addComponent(form);
                super.close(target);
            }

            @Override
            public void onConfirmation(AjaxRequestTarget target, final SelectedEntities selected) {
                
                this.setStatus(new StringResourceModel("folder.message.delete.running", StudyListPage.this, null));
                okBtn.setVisible(false);
                remarkLabel.setVisible(false);
                
                try {
                    if (getDelegate().moveToTrash(selected)) {
                        setStatus(new StringResourceModel("folder.message.deleteDone", StudyListPage.this,null));
                        if (selected.hasPatients()) {
                            viewport.getPatients().clear();
                            queryStudies();
                        } else
                            selected.refreshView(true);
                    } else
                        setStatus(new StringResourceModel("folder.message.deleteFailed", StudyListPage.this,null));
                } catch (Throwable t) {
                    log.error("moveToTrash failed: ", t);
                }
                target.addComponent(msgLabel);
                target.addComponent(okBtn);
            }
            
            @Override
            public void onDecline(AjaxRequestTarget target, SelectedEntities selected) {
                if (selected.getPpss().size() != 0) {
                    if (ContentEditDelegate.getInstance().deletePps(selected)) {
                        this.setStatus(new StringResourceModel("folder.message.deleteDone", StudyListPage.this,null));
                        selected.refreshView(true);
                    } else 
                        this.setStatus(new StringResourceModel("folder.message.deleteFailed", StudyListPage.this,null));
                }
            }
        };
        confirmDelete.setInitialHeight(150);
        form.add(confirmDelete);

        AjaxButton deleteBtn = new AjaxButton("deleteBtn") {
                    
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                boolean hasIgnored = selected.update(studyPermissionHelper.isUseStudyPermissions(), 
                        viewport.getPatients(), StudyPermission.DELETE_ACTION);
                selected.deselectChildsOfSelectedEntities();
                confirmDelete.setRemark(hasIgnored ? new StringResourceModel("folder.message.deleteNotAllowed",this, null) : null);
                if (selected.hasPPS()) {
                    confirmDelete.confirmWithCancel(target, new StringResourceModel("folder.message.confirmPpsDelete",this, null,new Object[]{selected}), selected);
                } else if (selected.hasDicomSelection()) {
                    confirmDelete.confirm(target, new StringResourceModel("folder.message.confirmDelete",this, null,new Object[]{selected}), selected);
                } else { 
                    if (hasIgnored) {
                        msgWin.setInfoMessage(getString("folder.message.deleteNotAllowed"));
                        msgWin.setColor("#FF0000");
                    } else {
                        msgWin.setInfoMessage(getString("folder.message.noSelection"));
                        msgWin.setColor("");
                    }
                    msgWin.show(target);
                }
            }
        };
        deleteBtn.add(new Image("deleteImg", ImageManager.IMAGE_FOLDER_DELETE)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        deleteBtn.add(new Label("deleteText", new ResourceModel("folder.deleteBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.add(deleteBtn);
        deleteBtn.add(new SecurityBehavior(getModuleName() + ":deleteButton"));
        
        AjaxFallbackButton moveBtn = new AjaxFallbackButton("moveBtn", form) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                selected.update(false, viewport.getPatients(), StudyPermission.UPDATE_ACTION, true);
                log.info("Selected Entities:"+selected);
                if (selected.hasDicomSelection()) {
                    modalWindow
                    .setPageCreator(new ModalWindow.PageCreator() {
                        
                        private static final long serialVersionUID = 1L;
                          
                        @Override
                        public Page createPage() {
                            return new MoveEntitiesPage(
                                  modalWindow, 
                                  selected, 
                                  viewport.getPatients()
                          );
                        }
                    });
                    int[] winSize = WebCfgDelegate.getInstance().getWindowSize("move");
                    modalWindow.setInitialWidth(winSize[0]).setInitialHeight(winSize[1]);
                    modalWindow.show(target);
                } else
                    msgWin.show(target, getString("folder.message.noSelection"));
            }
        };
        moveBtn.add(new Image("moveImg",ImageManager.IMAGE_FOLDER_MOVE)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        moveBtn.add(new Label("moveText", new ResourceModel("folder.moveBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.add(moveBtn);
        moveBtn.add(new SecurityBehavior(getModuleName() + ":moveButton"));
        
        PopupLink exportBtn = new PopupLink("exportBtn", "exportPage") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                this.setResponsePage(new ExportPage(viewport.getPatients()));
            }
        };
        int[] winSize = WebCfgDelegate.getInstance().getWindowSize("export");
        exportBtn.setPopupWidth(winSize[0]);
        exportBtn.setPopupHeight(winSize[1]);
        exportBtn.add(new Image("exportImg",ImageManager.IMAGE_FOLDER_EXPORT)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        exportBtn.add(new Label("exportText", new ResourceModel("folder.exportBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.add(exportBtn);
        exportBtn.add(new SecurityBehavior(getModuleName() + ":exportButton"));

        confirmUnlinkMpps = new ConfirmationWindow<PPSModel>("confirmUnlink") {
 
            private static final long serialVersionUID = 1L;

            private transient ContentEditDelegate delegate;
                       
            private ContentEditDelegate getDelegate() {
                if (delegate == null) {
                    delegate = ContentEditDelegate.getInstance();
                }
                return delegate;
            }

            @Override
            public void onOk(AjaxRequestTarget target) {
                target.addComponent(form);
            }

            @Override
            public void close(AjaxRequestTarget target) {
                target.addComponent(form);
                super.close(target);
            }

            @Override
            public void onConfirmation(AjaxRequestTarget target, final PPSModel ppsModel) {
                           
                this.setStatus(new StringResourceModel("folder.message.unlink.running", StudyListPage.this, null));
                okBtn.setVisible(false);

                try {
                    if (ContentEditDelegate.getInstance().unlink(ppsModel)) {
                        setStatus(new StringResourceModel("folder.message.unlinkDone", StudyListPage.this,null));
                        if (selected.hasPatients()) {
                            viewport.getPatients().clear();
                            queryStudies();
                        } else
                            selected.refreshView(true);
                    } else 
                        setStatus(new StringResourceModel("folder.message.unlinkFailed", StudyListPage.this,null));
                } catch (Throwable t) {
                    log.error("Unlink of MPPS failed:"+ppsModel, t);
                }
                target.addComponent(msgLabel);
                target.addComponent(okBtn);
            }
        };
        confirmUnlinkMpps.setInitialHeight(150);
        form.add(confirmUnlinkMpps);
        confirmEmulateMpps = new ConfirmationWindow<PPSModel>("confirmEmulate") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onOk(AjaxRequestTarget target) {
                target.addComponent(form);
            }

            @Override
            public void close(AjaxRequestTarget target) {
                target.addComponent(form);
                super.close(target);
            }
            
            @Override
            public void onConfirmation(AjaxRequestTarget target, final PPSModel ppsModel) {
                log.info("Emulate MPPS for Study:"+ppsModel.getStudy().getStudyInstanceUID());
                int success = -1;
                try {
                    if (ppsModel.hasForeignPpsInfo()) {
                        ContentEditDelegate.getInstance().removeForeignPpsInfo(ppsModel.getStudy().getPk());
                    }
                    success = MppsEmulateDelegate.getInstance().emulateMpps(ppsModel.getStudy().getPk());
                } catch (Throwable t) {
                    log.error("Emulate MPPS failed!", t);
                }
                setStatus(new StringResourceModel(success < 0 ? "folder.message.emulateFailed" : "folder.message.emulateDone", 
                        StudyListPage.this, null, new Object[]{new Integer(success)}));
                if (success > 0) {
                    StudyModel st = ppsModel.getStudy();
                    st.collapse();
                    st.expand();
                }
            }
        };
        confirmEmulateMpps.setInitialHeight(150);
        form.add(confirmEmulateMpps);
    }

    private void queryStudies() {
        List<String> dicomSecurityRoles = studyPermissionHelper.applyStudyPermissions() ? 
                    studyPermissionHelper.getDicomRoles() : null;
        viewport.setTotal(dao.count(viewport.getFilter(), dicomSecurityRoles));
        updatePatients(dao.findPatients(viewport.getFilter(), pagesize.getObject(), viewport.getOffset(), dicomSecurityRoles));
        notSearched = false;
    }

    private void updateStudyPermissions() {
        for (PatientModel patient : viewport.getPatients()) {
            for (StudyModel study : patient.getStudies()) 
                study.setStudyPermissionActions(dao.findStudyPermissionActions((study).getStudyInstanceUID(), 
                        studyPermissionHelper.getDicomRoles()
                ));
        }
    }
    
    private void updatePatients(List<Patient> patients) {
        retainSelectedPatients();
        boolean forceExpandable = WebCfgDelegate.getInstance().forcePatientExpandableForPatientQuery();
        for (Patient patient : patients) {
            PatientModel patientModel = addPatient(patient);   
            if (!viewport.getFilter().isPatientQuery()) {
                for (Study study : patient.getStudies()) {
                    List<String> actions = dao.findStudyPermissionActions((study).getStudyInstanceUID(), studyPermissionHelper.getDicomRoles());
                    if (!studyPermissionHelper.applyStudyPermissions()
                        || actions.contains("Q")) {  
                        addStudy(study, patientModel, actions);
                        patientModel.setExpandable(true);
                    }
                }
            } else if (forceExpandable) {
                patientModel.setExpandable(true);
            }
        }
    }

    private void retainSelectedPatients() {
        for (int i = 0; i < viewport.getPatients().size(); i++) {
            PatientModel patient = viewport.getPatients().get(i);
            patient.retainSelectedStudies();
            if (patient.isCollapsed() && !patient.isSelected()) { 
                viewport.getPatients().remove(i);
                i--;
            }
        }
    }

    private PatientModel addPatient(Patient patient) {
        long pk = patient.getPk();
        for (PatientModel patientModel : viewport.getPatients()) {
            if (patientModel.getPk() == pk) {
                return patientModel;
            }
        }
        PatientModel patientModel = new PatientModel(patient, latestStudyFirst);
        viewport.getPatients().add(patientModel);
        return patientModel;
    }
    
    private boolean addStudy(Study study, PatientModel patient, List<String> studyPermissionActions) {
        List<StudyModel> studies = patient.getStudies();
        for (StudyModel studyModel : studies) {
            if (studyModel.getPk() == study.getPk()) {
                return false;
            }
        }
        StudyModel m = new StudyModel(study, patient, studyPermissionActions);
        if (viewport.getFilter().isPpsWithoutMwl()) {
            m.expand();
            PPSModel pps;
            for (Iterator<PPSModel> it = m.getPPSs().iterator() ; it.hasNext() ; ) {
                pps = it.next();
                if (pps.getDataset() == null || pps.getAccessionNumber()!=null) {
                    it.remove();
                } else {
                    pps.collapse();
                }
            }
        }
        studies.add(m);
        return true;
    }

    private boolean expandLevelChanged(AbstractDicomModel model) {
        int currLevel = header.getExpandAllLevel();
        int level = model.levelOfModel();
        if (model.isCollapsed() || currLevel > level) {
            level = getExpandedLevel( 0, viewport.getPatients());
        } else {
            level = getExpandedLevel( ++level, model.getDicomModelsOfNextLevel());
        }
        header.setExpandAllLevel(level);
        return level != currLevel;
    }
    
    private int getExpandedLevel(int startLevel, List<? extends AbstractDicomModel> list) {
        int level = startLevel; 
        if (list != null) {
            startLevel++;
            int l;
            for ( AbstractDicomModel m1 : list ) {
                if (!m1.isCollapsed()) {
                    l = getExpandedLevel( startLevel, m1.getDicomModelsOfNextLevel());
                    if ( l > level) 
                        level = l;
                }
            }
        }
        return level;
    }
    
    public static String getModuleName() {
        return MODULE_NAME;
    }

    private final class PatientListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;

        private PatientListView(String id, List<?> list) {
            super(id, list);
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            
            final PatientModel patModel = (PatientModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", patModel.getRowspan());
                }
            };
            cell.add(new ExpandCollapseLink("expand", patModel, item)
                .setVisible(patModel.isExpandable()));
            item.add(cell);
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.patient.");
            item.add(new Label("name").add(tooltip));        
            item.add(new Label("id", new AbstractReadOnlyModel<String>(){

                private static final long serialVersionUID = 1L;

                @Override
                public String getObject() {
                    return patModel.getIssuer() == null ? patModel.getId() :
                        patModel.getId()+" / "+patModel.getIssuer();
                }
            })
            .add(tooltip));
            DateTimeLabel dtl = new DateTimeLabel("birthdate").setWithoutTime(true);
            dtl.add(tooltip.newWithSubstitution(new PropertyModel<String>(dtl, "textFormat")));
            item.add(dtl);
            item.add(new Label("sex").add(tooltip));
            item.add(new Label("comments").add(tooltip));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    patModel.setDetails(!patModel.isDetails());
                    if (target != null) {
                        target.addComponent(item);
                    }
                }
            }.add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
            .add(new ImageSizeBehaviour())
            .add(tooltip)));
            item.add(getEditLink(modalWindow, patModel, tooltip)
                    .add(new SecurityBehavior(getModuleName() + ":editPatientLink"))
                    .add(tooltip));
            item.add(getStudyPermissionLink(modalWindow, patModel, tooltip)
                    .add(new SecurityBehavior(getModuleName() + ":studyPermissionsPatientLink"))
                    .add(tooltip));    
            item.add(new ExternalLink("webview", webviewerLinkProvider.getUrlForPatient(patModel.getId(), patModel.getIssuer())) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return !studyPermissionHelper.isUseStudyPermissions() 
                        && webviewerLinkProvider.supportPatientLevel();
                }
            }
            .setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                    PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
            .add(new Image("webviewImg",ImageManager.IMAGE_FOLDER_VIEWER).add(new ImageSizeBehaviour())
                    .add(tooltip))
                    .add(new SecurityBehavior(getModuleName() + ":webviewerPatientLink"))
            );
            item.add(new AjaxCheckBox("selected") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true)
                .add(tooltip));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return patModel.isDetails();
                }
            };
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", patModel, false));
            item.add(new StudyListView("studies", patModel.getStudies(), item));
        }
    }

    private final class StudyListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> patientListItem;

        private StudyListView(String id, List<StudyModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);

            final StudyModel studyModel = (StudyModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", studyModel.getRowspan());
                }
            };
            cell.add(new ExpandCollapseLink("expand", studyModel, patientListItem));
            item.add(cell);
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.study.");
            
            item.add(new DateTimeLabel("datetime").add(tooltip));
            item.add(new Label("id").add(tooltip));            
            item.add(new Label("accessionNumber").add(tooltip));
            item.add(new Label("modalities").add(tooltip));
            item.add(new Label("description").add(tooltip));
            item.add(new Label("numberOfSeries").add(tooltip));
            item.add(new Label("numberOfInstances").add(tooltip));
            item.add(new Label("availability").add(tooltip));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    studyModel.setDetails(!studyModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }
            }
                .add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
                .add(new ImageSizeBehaviour())
                .add(tooltip))
            );
            item.add(getEditLink(modalWindow, studyModel, tooltip)
                    .add(new SecurityBehavior(getModuleName() + ":editStudyLink"))
            );
            item.add(getStudyPermissionLink(modalWindow, studyModel, tooltip)
                    .add(new SecurityBehavior(getModuleName() + ":studyPermissionsStudyLink"))
                    .add(tooltip));
            item.add(new IndicatingAjaxLink<Object>("imgSelect") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    int[] winSize = WebCfgDelegate.getInstance().getWindowSize("imgSelect");
                    imageSelectionWindow.setInitialWidth(winSize[0]).setInitialHeight(winSize[1]);
                    imageSelectionWindow.show(target, studyModel);
                }
                @Override
                protected IAjaxCallDecorator getAjaxCallDecorator() {
                    return new MaskingAjaxCallDecorator();
                }
            }
                .add(new Image("selectImg",ImageManager.IMAGE_COMMON_SEARCH)
                .add(new ImageSizeBehaviour()))
                .setVisible(studyPermissionHelper.checkPermission(studyModel, StudyPermission.READ_ACTION))
                .add(new SecurityBehavior(getModuleName() + ":imageSelectionStudyLink"))
            );
            item.add( new AjaxCheckBox("selected") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true).add(tooltip));
            
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return studyModel.isDetails();
                }
            };
            item.add( new ExternalLink("webview", webviewerLinkProvider.getUrlForStudy(studyModel.getStudyInstanceUID())) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return !studyPermissionHelper.isUseStudyPermissions() 
                        && webviewerLinkProvider.supportStudyLevel();
                }
            }
            .setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                    PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
                .add(new Image("webviewImg",ImageManager.IMAGE_FOLDER_VIEWER).add(new ImageSizeBehaviour())
                .add(tooltip))
                .setVisible(studyPermissionHelper.checkPermission(studyModel, StudyPermission.READ_ACTION))
                .add(new SecurityBehavior(getModuleName() + ":webviewerStudyLink"))
            );
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", studyModel, false));
            details.setVisible(studyPermissionHelper.checkPermission(studyModel, StudyPermission.QUERY_ACTION));
            item.add(new PPSListView("ppss",
                    studyModel.getPPSs(), patientListItem));
        }
    }

    private final class PPSListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> ppsListItem;

        private PPSListView(String id, List<PPSModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.ppsListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            
            final PPSModel ppsModel = (PPSModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", ppsModel.getRowspan());
                }
            };
            cell.add(new ExpandCollapseLink("expand", ppsModel, ppsListItem) {

                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return ppsModel.getUid() != null;
                }
            });
            item.add(cell);
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.pps.");
            
            item.add(new DateTimeLabel("datetime").add(tooltip));
            item.add(new Label("id").add(tooltip));
            item.add(new Label("spsid").add(tooltip));
            item.add(new Label("modality").add(tooltip));
            item.add(new Label("description").add(tooltip));
            item.add(new Label("numberOfSeries").add(tooltip));
            item.add(new Label("numberOfInstances").add(tooltip));
            item.add(new Label("status").add(tooltip));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    ppsModel.setDetails(!ppsModel.isDetails());
                    if (target != null) {
                        target.addComponent(StudyListPage.this.get("form"));
                    }
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null;
                }
            }.add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
            .add(new ImageSizeBehaviour())
                .add(tooltip))
            );
            item.add(getEditLink(modalWindow, ppsModel, tooltip)
                    .add(new SecurityBehavior(getModuleName() + ":editPPSLink"))
            );
            
            IndicatingAjaxFallbackLink<?> linkBtn = new IndicatingAjaxFallbackLink<Object>("linkBtn") {
                
                private static final long serialVersionUID = 1L;
                
                @Override
                public void onClick(AjaxRequestTarget target) {
                    log.debug("#### linkBtn clicked!");
                    mpps2MwlLinkWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {              
                        
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClose(AjaxRequestTarget target) {
                            mpps2MwlLinkWindow.setOutputMarkupId(true);
                            target.addComponent(mpps2MwlLinkWindow);
                        }
                    });
                    
                    int[] winSize = WebCfgDelegate.getInstance().getWindowSize("mpps2mwl");
                    ((Mpps2MwlLinkPage) mpps2MwlLinkWindow
                            .setInitialWidth(winSize[0]).setInitialHeight(winSize[1])
                    )
                    .show(target, ppsModel, form);
                    log.debug("#### linkBtn onClick finished!");
                }

                @Override
                protected IAjaxCallDecorator getAjaxCallDecorator() {
                    return new MaskingAjaxCallDecorator();
                }
                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null && ppsModel.getAccessionNumber()==null;
                }
            };
            linkBtn.add(new Image("linkImg", ImageManager.IMAGE_COMMON_LINK)
            .add(new ImageSizeBehaviour())
            .add(tooltip));
            item.add(linkBtn);
            linkBtn.setVisible(studyPermissionHelper.checkPermission(ppsModel, StudyPermission.UPDATE_ACTION));
            linkBtn.add(new SecurityBehavior(getModuleName() + ":linkPPSLink"));

            item.add(new AjaxFallbackLink<Object>("unlinkBtn") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    confirmUnlinkMpps.confirm(target, new StringResourceModel("folder.message.confirmUnlink",this, null,new Object[]{ppsModel}), ppsModel);
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null && ppsModel.getAccessionNumber()!=null;
                }
            }
		.add(new Image("unlinkImg",ImageManager.IMAGE_FOLDER_UNLINK)
            	.add(new ImageSizeBehaviour()).add(tooltip))
            	.setVisible(studyPermissionHelper.checkPermission(ppsModel, StudyPermission.UPDATE_ACTION))
            	.add(new SecurityBehavior(getModuleName() + ":unlinkPPSLink"))
            );
            
            item.add(new AjaxFallbackLink<Object>("emulateBtn") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    confirmEmulateMpps.confirm(target, 
                            new StringResourceModel(ppsModel.hasForeignPpsInfo() ? 
                                    "folder.message.confirmEmulate" : "folder.message.confirmForcedEmulate"
                                    ,this, null), ppsModel);
                    
                }

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() == null;
                }
            }
                .add(new Image("emulateImg",ImageManager.IMAGE_FOLDER_MPPS)
                .add(new ImageSizeBehaviour()).add(tooltip))
                .setVisible(studyPermissionHelper.checkPermission(ppsModel, StudyPermission.APPEND_ACTION))
                .add(new SecurityBehavior(getModuleName() + ":emulatePPSLink"))
            );
             
            item.add(new AjaxCheckBox("selected"){
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return ppsModel.getDataset() != null;
                }
                
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true)
                .add(tooltip)
            );
            
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return ppsModel.isDetails();
                }
            };
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", ppsModel, false));
            details.setVisible(studyPermissionHelper.checkPermission(ppsModel, StudyPermission.QUERY_ACTION));
            item.add(new SeriesListView("series",
                    ppsModel.getSeries(), ppsListItem));
        }
    }

    private final class SeriesListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> patientListItem;

        private SeriesListView(String id, List<SeriesModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            
            final SeriesModel seriesModel = (SeriesModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", seriesModel.getRowspan());
                }
            };
            cell.add(new ExpandCollapseLink("expand", seriesModel, patientListItem));
            item.add(cell);
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.series.");
            
            item.add(new DateTimeLabel("datetime").add(tooltip));
            item.add(new Label("seriesNumber").add(tooltip));
            item.add(new Label("sourceAET").add(tooltip));
            item.add(new Label("modality").add(tooltip));
            item.add(new Label("description").add(tooltip));
            item.add(new Label("numberOfInstances").add(tooltip));
            item.add(new Label("availability").add(tooltip));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    seriesModel.setDetails(!seriesModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
            .add(new ImageSizeBehaviour())
                .add(tooltip))
            );
            item.add(getEditLink(modalWindow, seriesModel, tooltip)
                    .add(new SecurityBehavior(getModuleName() + ":editSeriesLink"))
            );
            item.add(new AjaxCheckBox("selected") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }
            }.setOutputMarkupId(true)
            .add(tooltip));
            item.add(new IndicatingAjaxLink<Object>("imgSelect") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    int[] winSize = WebCfgDelegate.getInstance().getWindowSize("imgSelect");
                    imageSelectionWindow.setInitialWidth(winSize[0]).setInitialHeight(winSize[1]);
                    imageSelectionWindow.show(target, seriesModel);
                }
                @Override
                protected IAjaxCallDecorator getAjaxCallDecorator() {
                    return new MaskingAjaxCallDecorator();
                }
            }
                .add(new Image("selectImg",ImageManager.IMAGE_COMMON_SEARCH)
                .add(new ImageSizeBehaviour())
                .add(tooltip))
                .setVisible(studyPermissionHelper.checkPermission(seriesModel, StudyPermission.READ_ACTION))
                .add(new SecurityBehavior(getModuleName() + ":imageSelectionSeriesLink"))
            );
            final WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return seriesModel.isDetails();
                }
            };
            item.add( new ExternalLink("webview", webviewerLinkProvider.getUrlForSeries(seriesModel.getSeriesInstanceUID())) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return !studyPermissionHelper.isUseStudyPermissions() 
                        && webviewerLinkProvider.supportSeriesLevel();
                }
            }
                .setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                    PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
                    .add(new Image("webviewImg",ImageManager.IMAGE_FOLDER_VIEWER).add(new ImageSizeBehaviour())
                            .add(tooltip))
                 .setVisible(studyPermissionHelper.checkPermission(seriesModel, StudyPermission.READ_ACTION))
                .add(new SecurityBehavior(getModuleName() + ":webviewerSeriesLink"))
            );
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", seriesModel, false));
            details.setVisible(studyPermissionHelper.checkPermission(seriesModel, StudyPermission.QUERY_ACTION));
            item.add(new InstanceListView("instances",
                    seriesModel.getInstances(), patientListItem));
        }
    }

    private final class InstanceListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> patientListItem;

        private InstanceListView(String id, List<InstanceModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            
            final InstanceModel instModel = (InstanceModel) item.getModelObject();
            WebMarkupContainer cell = new WebMarkupContainer("cell"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(ComponentTag tag) {
                   super.onComponentTag(tag);
                   tag.put("rowspan", instModel.getRowspan());
                }
            };
            cell.add(new ExpandCollapseLink("expand", instModel, patientListItem));
            item.add(cell);
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.instance.");
            
            item.add(new DateTimeLabel("datetime").add(tooltip));
            item.add(new Label("instanceNumber").add(tooltip));
            item.add(new Label("sopClassUID").add(tooltip));
            item.add(new Label("description").add(tooltip));
            item.add(new Label("availability").add(tooltip));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    instModel.setDetails(!instModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
            .add(new ImageSizeBehaviour())
            .add(tooltip)));
            item.add(getEditLink(modalWindow, instModel, tooltip)
                    .setVisible(studyPermissionHelper.checkPermission(instModel, StudyPermission.UPDATE_ACTION))
                    .add(new SecurityBehavior(getModuleName() + ":editInstanceLink"))
            );
            item.add( new ExternalLink("webview", webviewerLinkProvider.getUrlForInstance(instModel.getSOPInstanceUID())) {
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return !studyPermissionHelper.isUseStudyPermissions() 
                        && webviewerLinkProvider.supportInstanceLevel();
                }
            }
                .setPopupSettings(new PopupSettings(PageMap.forName("webviewPage"), 
                        PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS))
                .add(new Image("webviewImg",ImageManager.IMAGE_FOLDER_VIEWER).add(new ImageSizeBehaviour())
                        .add(tooltip)
                )
                .setVisible(studyPermissionHelper.checkPermission(instModel, StudyPermission.READ_ACTION))
                .add(new SecurityBehavior(getModuleName() + ":webviewerInstanceLink"))
            );

            item.add(new AjaxLink<Object>("wado") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    int[] winSize = WebCfgDelegate.getInstance().getWindowSize("wado");
                    wadoImageWindow.setInitialWidth(winSize[0]).setInitialHeight(winSize[1]);
                    wadoImageWindow.setPageCreator(new ModalWindow.PageCreator() {
                          
                        private static final long serialVersionUID = 1L;
                          
                        @Override
                        public Page createPage() {
                            return new WadoImagePage(wadoImageWindow, instModel);                        
                        }
                    });
                    wadoImageWindow.show(target);
                }
            }
                .add(new Image("wadoImg",ImageManager.IMAGE_FOLDER_WADO)
                .add(new ImageSizeBehaviour())
                .add(tooltip))
                .setVisible(studyPermissionHelper.checkPermission(instModel, StudyPermission.READ_ACTION))
                .add(new SecurityBehavior(getModuleName() + ":wadoImageInstanceLink"))
            );

            item.add(new AjaxCheckBox("selected"){

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true)
                .add(tooltip));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return instModel.isDetails();
                }
            };
            item.add(details);
            details.add(new DicomObjectPanel("dicomobject", instModel, false));
            details.setVisible(studyPermissionHelper.checkPermission(instModel, StudyPermission.QUERY_ACTION));
            item.add(new FileListView("files", instModel.getFiles(), patientListItem));
        }
    }

    private final class FileListView extends PropertyListView<Object> {

        private static final long serialVersionUID = 1L;
        
        private final ListItem<?> patientListItem;

        private FileListView(String id, List<FileModel> list,
                ListItem<?> patientListItem) {
            super(id, list);
            this.patientListItem = patientListItem;
        }

        @Override
        protected void populateItem(final ListItem<Object> item) {
            item.setOutputMarkupId(true);
            
            TooltipBehaviour tooltip = new TooltipBehaviour("folder.content.data.file.");
            
            final FileModel fileModel = (FileModel) item.getModelObject();
            item.add(new DateTimeLabel("fileObject.createdTime").add(tooltip));
            item.add(new Label("fileObject.fileSize").add(tooltip));
            item.add(new Label("fileObject.transferSyntaxUID").add(tooltip));
            item.add(new Label("fileObject.fileSystem.directoryPath").add(tooltip));
            item.add(new Label("fileObject.filePath").add(tooltip));
            item.add(new Label("fileObject.fileSystem.availability").add(tooltip));
            item.add(new AjaxFallbackLink<Object>("toggledetails") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    fileModel.setDetails(!fileModel.isDetails());
                    if (target != null) {
                        target.addComponent(patientListItem);
                    }
                }

            }.add(new Image("detailImg",ImageManager.IMAGE_COMMON_DICOM_DETAILS)
            .add(new ImageSizeBehaviour())
            .add(tooltip)));
            item.add(getFileDisplayLink(modalWindow, fileModel, tooltip));
            item.add(new AjaxCheckBox("selected") {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return false;//no action on file level at the moment
                }
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(this);
                }}.setOutputMarkupId(true)
                .add(tooltip));
            WebMarkupContainer details = new WebMarkupContainer("details") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isVisible() {
                    return fileModel.isDetails();
                }
            };
            item.add(details);
            details.add(new FilePanel("file", fileModel));
        }
    }
    
    private Link<Object> getEditLink(final ModalWindow modalWindow, final AbstractEditableDicomModel model, TooltipBehaviour tooltip) {
       
        int[] winSize = WebCfgDelegate.getInstance().getWindowSize("dcmEdit");
        ModalWindowLink editLink = new ModalWindowLink("edit", modalWindow, winSize[0], winSize[1]) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                modalWindow.setContent(new EditDicomObjectPanel(
                        "content", 
                        modalWindow, 
                        (DicomObject) model.getDataset(), 
                        model.getClass().getSimpleName()
                ) {
                   private static final long serialVersionUID = 1L;

                   @Override
                   protected void onSubmit() {
                       model.update(getDicomObject());
                       super.onCancel();
                   }
                });
                modalWindow.show(target);
                super.onClick(target);
            }
            
            @Override
            public boolean isVisible() {
                return model.getDataset()!= null && (!studyPermissionHelper.isUseStudyPermissions() 
                    || checkEditStudyPermission(model));
            }
        };
        Image image = new Image("editImg",ImageManager.IMAGE_COMMON_DICOM_EDIT);
        image.add(new ImageSizeBehaviour("vertical-align: middle;"));
        if (tooltip != null) image.add(tooltip);
        editLink.add(image);
        return editLink;
    }

    private Link<Object> getFileDisplayLink(final ModalWindow modalWindow, final FileModel fileModel, TooltipBehaviour tooltip) {

        int[] winSize = WebCfgDelegate.getInstance().getWindowSize("dcmFileDisplay");
        final File file = new File(
                fileModel.getFileObject().getFileSystem().getDirectoryPath() + 
                "/" + 
                fileModel.getFileObject().getFilePath());
        ModalWindowLink displayLink = new ModalWindowLink("displayFile", modalWindow, winSize[0], winSize[1]) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    modalWindow.setContent(new DisplayDicomObjectPanel(
                            "content", 
                            modalWindow, 
                            new DicomInputStream(file).readDicomObject(), 
                            fileModel.getClass().getSimpleName()
                    ));
                } catch (IOException e) {
                    log.error("Error requesting dicom object: ", e);
                }
                modalWindow.show(target);
                super.onClick(target);
            }            
        };
        Image image = new Image("displayFileImg",ImageManager.IMAGE_FOLDER_DICOM_FILE);
        image.add(new ImageSizeBehaviour("vertical-align: middle;"));
        if (tooltip != null) image.add(tooltip);
        displayLink.add(image);
        displayLink.setVisible(file.exists());
        return displayLink;
    }

    private Link<Object> getStudyPermissionLink(final ModalWindow modalWindow, final AbstractEditableDicomModel model, TooltipBehaviour tooltip) {
        
        int[] winSize = WebCfgDelegate.getInstance().getWindowSize("studyPerm");
        ModalWindowLink studyPermissionLink
         = new ModalWindowLink("studyPermissions", modalWindow, winSize[0], winSize[1]) {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {

                modalWindow
                .setPageCreator(new ModalWindow.PageCreator() {
                    
                    private static final long serialVersionUID = 1L;
                      
                    @Override
                    public Page createPage() {
                        return new StudyPermissionsPage(model);
                    }
                });

                modalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {              
                    
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClose(AjaxRequestTarget target) {
                        updateStudyPermissions();

                        modalWindow.getPage().setOutputMarkupId(true);
                        target.addComponent(modalWindow.getPage());
                    }
                });
                modalWindow.add(new ModalWindowLink.DisableDefaultConfirmBehavior());
                modalWindow.show(target);
            }
            
            @Override
            public boolean isVisible() {
                return studyPermissionHelper.isManageStudyPermissions() 
                    && model.getDataset() != null
                    && !(model instanceof PatientModel && !((PatientModel) model).isExpandable());
            }
        };
        Image image = new Image("studyPermissionsImg",ImageManager.IMAGE_FOLDER_STUDY_PERMISSIONS);
        image.add(new ImageSizeBehaviour("vertical-align: middle;"));
        if (tooltip != null) image.add(tooltip);
        studyPermissionLink.add(image);
        return studyPermissionLink;
    }

    private boolean checkEditStudyPermission(AbstractDicomModel model) {
        if (!studyPermissionHelper.isUseStudyPermissions()
                || (model instanceof PatientModel))
            return true;
        return studyPermissionHelper.checkPermission(model, StudyPermission.UPDATE_ACTION);
    }

    private class ExpandCollapseLink extends AjaxFallbackLink<Object> {

        private static final long serialVersionUID = 1L;
        
        private AbstractDicomModel model;
        private ListItem<?> patientListItem;
        
        private ExpandCollapseLink(String id, AbstractDicomModel m, ListItem<?> patientListItem) {
            super(id);
            this.model = m;
            this.patientListItem = patientListItem;
            add( new Image(id+"Img", new AbstractReadOnlyModel<ResourceReference>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ResourceReference getObject() {
                    return model.isCollapsed() ? ImageManager.IMAGE_COMMON_EXPAND : 
                        ImageManager.IMAGE_COMMON_COLLAPSE;
                }
            })
            .add(new ImageSizeBehaviour()));
        }
        
        @Override
        public void onClick(AjaxRequestTarget target) {
            if (model.isCollapsed()) model.expand();
            else model.collapse();
            if (target != null) {
                target.addComponent(patientListItem);
                if (expandLevelChanged(model))
                    target.addComponent(header);
            }
        }
    }
}
