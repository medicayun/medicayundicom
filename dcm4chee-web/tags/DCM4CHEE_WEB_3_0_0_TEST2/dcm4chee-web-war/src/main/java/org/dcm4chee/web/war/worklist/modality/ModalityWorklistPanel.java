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

package org.dcm4chee.web.war.worklist.modality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.dcm4chee.archive.common.SPSStatus;
import org.dcm4chee.archive.entity.MWLItem;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.CheckOneDayBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.SimpleDateTimeField;
import org.dcm4chee.web.common.validators.UIDValidator;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.dcm4chee.web.dao.worklist.modality.ModalityWorklist;
import org.dcm4chee.web.dao.worklist.modality.ModalityWorklistFilter;
import org.dcm4chee.web.war.WicketSession;
import org.dcm4chee.web.war.common.EditDicomObjectPage;
import org.dcm4chee.web.war.folder.DicomObjectPanel;
import org.dcm4chee.web.war.worklist.modality.MWLItemListView.MwlActionProvider;
import org.dcm4chee.web.war.worklist.modality.model.MWLItemModel;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 20.04.2010
 */
public class ModalityWorklistPanel extends Panel implements MwlActionProvider {

    private static final ResourceReference CSS = new CompressedResourceReference(ModalityWorklistPanel.class, "modality-worklist-style.css");

    private static final long serialVersionUID = 1L;

    // TODO: put this into .properties file
    private static int PAGESIZE = 10;
    
    private static final String MODULE_NAME = "mw";
    private static List<String> scheduledStationAETs = new ArrayList<String>();
    private static List<String> modalities = new ArrayList<String>();
    private Model<Boolean> showSearchModel = new Model<Boolean>(true);
    private boolean notSearched = true;
    private ViewPort viewport;
    protected final BaseForm form;
    
    private List<WebMarkupContainer> searchTableComponents = new ArrayList<WebMarkupContainer>();
    
    private transient ModalityWorklist dao;

    private WebMarkupContainer listPanel;
    private WebMarkupContainer navPanel;

    public ModalityWorklistPanel(final String id) {
        super(id);
        viewport = initViewPort();
        if (ModalityWorklistPanel.CSS != null)
            add(CSSPackageResource.getHeaderContribution(ModalityWorklistPanel.CSS));

        final ModalityWorklistFilter filter = viewport.getFilter();
        add(form = new BaseForm("form", new CompoundPropertyModel<Object>(filter)));
        form.setResourceIdPrefix("mw.");
        AjaxFallbackLink<Object> toggleLink;
        toggleLink = new AjaxFallbackLink<Object>("searchToggle") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                boolean b = !showSearchModel.getObject();
                showSearchModel.setObject(b);
                for (WebMarkupContainer wmc : searchTableComponents) {
                    wmc.setVisible(b);               
                    target.addComponent(wmc);
                }
                target.addComponent(this);
            }
        };
        form.add(toggleLink);
        toggleLink.add((new Image("searchToggleImg", new AbstractReadOnlyModel<ResourceReference>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ResourceReference getObject() {
                    return showSearchModel.getObject() ? ImageManager.IMAGE_COMMON_COLLAPSE : 
                        ImageManager.IMAGE_COMMON_EXPAND;
                }
        })
        .add(new TooltipBehaviour("mw.", "searchToggleImg", showSearchModel)))
        .add(new ImageSizeBehaviour()));

        addQueryFields(filter, form);
        addQueryOptions(form);
        addNavigation(form);
        
        form.setResourceIdPrefix("mw.");
        listPanel = new WebMarkupContainer("listPanel");
        add(listPanel);
        listPanel.setOutputMarkupId(true);
        listPanel.add(new MWLItemListView("mwlitems", viewport.getMWLItemModels(), this));
        initModalitiesAndStationAETs();
    }
    
    protected ViewPort initViewPort() {
        return ((WicketSession) getSession()).getMwViewPort();
    }
    
    protected ViewPort getViewPort() {
        return viewport;
    }

    protected void addQueryFields(final ModalityWorklistFilter filter, final BaseForm form) {
        final IModel<Boolean> enabledModel = new AbstractReadOnlyModel<Boolean>(){

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return (!filter.isExtendedQuery() || QueryUtil.isUniversalMatch(filter.getStudyInstanceUID()));
            }
        };
        
        searchTableComponents.add(form.createAjaxParent("searchLabels"));
        
        form.addInternalLabel("patientName");
        form.addInternalLabel("patientIDDescr");
        form.addInternalLabel("startDate");
        form.addInternalLabel("accessionNumber");
        
        searchTableComponents.add(form.createAjaxParent("searchFields"));
        
        form.addTextField("patientName", enabledModel, false);
        form.addTextField("patientID", enabledModel, true);
        form.addTextField("issuerOfPatientID", enabledModel, true);
        SimpleDateTimeField dtf = form.addDateTimeField("startDateMin", null, enabledModel, false, true);
        SimpleDateTimeField dtfMax = form.addDateTimeField("startDateMax", null, enabledModel, true, true);
        dtf.addToDateField(new CheckOneDayBehaviour(dtf, dtfMax, "onchange"));
        
        form.addTextField("accessionNumber", enabledModel, false);
        
        searchTableComponents.add(form.createAjaxParent("searchDropdowns"));

        form.addInternalLabel("modality");
        form.addInternalLabel("scheduledStationAET");
        form.addInternalLabel("scheduledStationName");
        form.addInternalLabel("SPSStatus");

        form.addDropDownChoice("modality", null, modalities, enabledModel, false);
        List<String> choices = viewport.getStationAetChoices(scheduledStationAETs);
        if (choices.size() > 0)
            filter.setScheduledStationAET(choices.get(0));
        form.addDropDownChoice("scheduledStationAET", null, choices, enabledModel, false);
        form.addDropDownChoice("scheduledStationName", null, getStationNameChoices(), enabledModel, false);
        form.addDropDownChoice("SPSStatus", null, getSpsStatusChoices(), enabledModel, false);

        final WebMarkupContainer extendedFilter = new WebMarkupContainer("extendedFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return showSearchModel.getObject() && filter.isExtendedQuery();
            }
        };

        extendedFilter.add( new Label("birthDate.label", new ResourceModel("mw.extendedFilter.birthDate.label")));
        extendedFilter.add( new Label("birthDateMin.label", new ResourceModel("mw.extendedFilter.birthDateMin.label")));
        extendedFilter.add( new Label("birthDateMax.label", new ResourceModel("mw.extendedFilter.birthDateMax.label")));
        SimpleDateTimeField dtfB = form.getDateTextField("birthDateMin", null, "extendedFilter.", enabledModel);
        SimpleDateTimeField dtfBEnd = form.getDateTextField("birthDateMax", null, "extendedFilter.", enabledModel);
        dtfB.addToDateField(new CheckOneDayBehaviour(dtfB, dtfBEnd, "onchange"));
        extendedFilter.add(dtfB);
        extendedFilter.add(dtfBEnd);
        extendedFilter.add( new Label("studyInstanceUID.label", new ResourceModel("mw.extendedFilter.studyInstanceUID.label")));
        extendedFilter.add( new TextField<String>("studyInstanceUID").add(new UIDValidator()));
        extendedFilter.setOutputMarkupId(true);
        extendedFilter.setOutputMarkupPlaceholderTag(true);
        form.add(extendedFilter);
        
        searchTableComponents.add(form.createAjaxParent("searchFooter"));
        
        AjaxFallbackLink<?> link = new AjaxFallbackLink<Object>("showExtendedFilter") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                filter.setExtendedQuery(!filter.isExtendedQuery());
                target.addComponent(extendedFilter);
                target.addComponent(this);
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
        .add(new TooltipBehaviour("mw.search.", "showExtendedFilterImg", new AbstractReadOnlyModel<Boolean>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Boolean getObject() {
                return filter.isExtendedQuery();
            }
        }))
        .add(new ImageSizeBehaviour())));
        link.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.addComponent(link);
    }

    protected void addQueryOptions(BaseForm form) {
        form.addLabeledCheckBox("latestItemsFirst", null);
    }

    protected void addNavigation(BaseForm form) {
        
        Button resetBtn = new AjaxButton("resetBtn") {
            
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target, Form<?> form) {

                form.clearInput();
                viewport.clear();
                BaseForm.addFormComponentsToAjaxRequestTarget(target, form);
                target.addComponent(navPanel);
                target.addComponent(listPanel);
            }
        };
        resetBtn.setDefaultFormProcessing(false);
        resetBtn.add(new Image("resetImg",ImageManager.IMAGE_COMMON_RESET)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        resetBtn.add(new Label("resetText", new ResourceModel("mw.searchFooter.resetBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        form.addComponent(resetBtn);
        
        Button searchBtn = new AjaxButton("searchBtn") {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                viewport.setOffset(0);
                queryMWLItems();
                target.addComponent(navPanel);
                target.addComponent(listPanel);
            }
            
            @Override
            public void onError(AjaxRequestTarget target, Form<?> form) {
                BaseForm.addInvalidComponentsToAjaxRequestTarget(target, form);
            }
        };
        searchBtn.add(new Image("searchImg",ImageManager.IMAGE_COMMON_SEARCH)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        searchBtn.add(new Label("searchText", new ResourceModel("mw.searchFooter.searchBtn.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle;")))
        );
        form.addComponent(searchBtn);
        form.setDefaultButton(searchBtn);
        
        navPanel = form.createAjaxParent("navPanel");
        
        form.addComponent(new AjaxFallbackLink<Object>("prev") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                viewport.setOffset(Math.max(0, viewport.getOffset() - PAGESIZE));
                queryMWLItems();
                target.addComponent(navPanel);
                target.addComponent(listPanel);
            }
            
            @Override
            public boolean isVisible() {
                return (!notSearched && !(viewport.getOffset() == 0));
            }
        }
        .add(new Image("prevImg", ImageManager.IMAGE_COMMON_BACK)
        .add(new ImageSizeBehaviour("vertical-align: middle;"))
        .add(new TooltipBehaviour("mw.search.")))
        );
 
        form.addComponent(new AjaxFallbackLink<Object>("next") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                viewport.setOffset(viewport.getOffset() + PAGESIZE);
                queryMWLItems();
                target.addComponent(navPanel);
                target.addComponent(listPanel);
            }

            @Override
            public boolean isVisible() {
                return (!notSearched && !(viewport.getTotal() - viewport.getOffset() <= PAGESIZE));
            }
        }
        .add(new Image("nextImg", ImageManager.IMAGE_COMMON_FORWARD)
        .add(new ImageSizeBehaviour("vertical-align: middle;"))
        .add(new TooltipBehaviour("mw.search.")))
        .setVisible(!notSearched)
        );

        //viewport label: use StringResourceModel with key substitution to select 
        //property key according notSearched and getTotal.
        Model<?> keySelectModel = new Model<Serializable>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Serializable getObject() {
                return notSearched ? "mw.search.notSearched" :
                        viewport.getTotal() == 0 ? "mw.search.noMatchingMppsFound" : 
                            "mw.search.mppsFound";
            }
        };
        form.addComponent(new Label("viewport", new StringResourceModel("${}", ModalityWorklistPanel.this, keySelectModel,new Object[]{"dummy"}){

            private static final long serialVersionUID = 1L;

            @Override
            protected Object[] getParameters() {
                return new Object[]{viewport.getOffset()+1,
                        Math.min(viewport.getOffset()+PAGESIZE, viewport.getTotal()),
                        viewport.getTotal()};
            }
        }));
        form.clearParent();
    }

    private void initModalitiesAndStationAETs() {
        if (modalities.isEmpty() || scheduledStationAETs.isEmpty()) {
            StudyListLocal dao = (StudyListLocal)
                    JNDIUtils.lookup(StudyListLocal.JNDI_NAME);
            modalities.clear();
            modalities.add("*");
            modalities.addAll(dao.selectDistinctModalities());
            scheduledStationAETs.clear();
            scheduledStationAETs.addAll(dao.selectDistinctSourceAETs());
        }
    }

    protected void queryMWLItems() {
        ModalityWorklist dao = lookupMwlDAO();
        viewport.getMWLItemModels().clear();
        viewport.setTotal(dao.countMWLItems(viewport.getFilter()));
             
        List<MWLItemModel> current = viewport.getMWLItemModels();
        for (MWLItem mwlItem : dao.findMWLItems(viewport.getFilter(), PAGESIZE, viewport.getOffset()))
            current.add(new MWLItemModel(mwlItem, new Model<Boolean>(false)));
        notSearched = false;
    }

    private ModalityWorklist lookupMwlDAO() {
        if (dao == null)
            dao = (ModalityWorklist) JNDIUtils.lookup(ModalityWorklist.JNDI_NAME);
        return dao;
    }

    public static String getModuleName() {
        return MODULE_NAME;
    }

    protected List<String> getModalityChoices() {
        ModalityWorklist dao = lookupMwlDAO();
        List<String> modalities = new ArrayList<String>();
        modalities.add("*");
        modalities.addAll(dao.selectDistinctModalities());
        return modalities;
    }

    protected List<String> getStationAETChoices() {
        if (scheduledStationAETs == null) {
            ModalityWorklist dao = lookupMwlDAO();
            scheduledStationAETs = new ArrayList<String>();
            scheduledStationAETs.addAll(dao.selectDistinctStationAETs());
        }
        return scheduledStationAETs;
    }

    protected List<String> getStationNameChoices() {
        ModalityWorklist dao = lookupMwlDAO();
        List<String> scheduledStationNames = new ArrayList<String>();
        scheduledStationNames.add("*");
        scheduledStationNames.addAll(dao.selectDistinctStationNames());
        return scheduledStationNames;
    }

    protected List<String> getSpsStatusChoices() {
        List<String> status = new ArrayList<String>();
        status.add("*");
        for (SPSStatus spsStatus : SPSStatus.values())
            status.add(spsStatus.toString());
        return status;
    }

    //MwlActionProvider (details and edit)
    public void addMwlActions(final ListItem<MWLItemModel> item, WebMarkupContainer valueContainer, final MWLItemListView mwlListView) {

        final MWLItemModel mwlItemModel = item.getModelObject();
        valueContainer.add(new WebMarkupContainer("cell") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(ComponentTag tag) {
               super.onComponentTag(tag);
               if (item.getModelObject().isDetails()) 
                   tag.put("rowspan", "2");
            }
        });

        WebMarkupContainer details = new WebMarkupContainer("details") {
            
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return mwlItemModel.isDetails();
            }
        };
        item.add((details).add(new DicomObjectPanel("dicomobject", mwlItemModel.getDataset(), false)));

        valueContainer.add(new AjaxFallbackLink<Object>("toggledetails") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                mwlItemModel.setDetails(!mwlItemModel.isDetails());
                form.setOutputMarkupId(true);
                if (target != null) {
                    target.addComponent(navPanel);
                    target.addComponent(listPanel);
                }
            }

        }.add(new Image("detailImg", ImageManager.IMAGE_COMMON_DICOM_DETAILS)
        .add(new ImageSizeBehaviour())
        .add(new TooltipBehaviour("mw.", "detailImg"))))
        .add( new Link<Object>("edit") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                setResponsePage(new EditDicomObjectPage(mwlListView.getPage(), mwlItemModel));
            }
        }.add(new Image("editImg",ImageManager.IMAGE_COMMON_DICOM_EDIT)
        .add(new ImageSizeBehaviour())
        .add(new TooltipBehaviour("mw.", "editImg"))));
    }
}
