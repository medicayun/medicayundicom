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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.DateTimeLabel;
import org.dcm4chee.web.war.common.SelectAllLink;
import org.dcm4chee.web.war.common.WadoImage;
import org.dcm4chee.web.war.folder.delegate.WADODelegate;
import org.dcm4chee.web.war.folder.model.InstanceModel;
import org.dcm4chee.web.war.folder.model.PPSModel;
import org.dcm4chee.web.war.folder.model.SeriesModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since 05.07.2010
 */
public class ImageSelectionWindow extends ModalWindow {
    
    private static final long serialVersionUID = 1L;
    private List<SeriesModel> seriesList = new ArrayList<SeriesModel>();
    private Model<Integer> maxSeries = new Model<Integer>(5);
    private Model<Integer> maxInstances = new Model<Integer>(10);
    private int numCols = 5;
    private int numRows;
    private Model<Integer> imgSizeModel = new Model<Integer>(128);
    private boolean selectionChanged;
    
    private static Logger log = LoggerFactory.getLogger(ImageSelectionWindow.class);

    public ImageSelectionWindow(String id, String titleResource) {
        this(id);
        setTitle(new ResourceModel(titleResource));
    }
    
    public ImageSelectionWindow(String id) {
        super(id);
        initWadoBaseUrl();
        setContent(new ImageSelectionPanel());
    }
    
    public void initWadoBaseUrl() {
        WadoImage.setDefaultWadoBaseUrl(WADODelegate.getInstance().getWadoBaseUrl());
    }
    
    private void calcNumRows() {
        numRows = seriesList.size() / numCols;
        if (seriesList.size() % numCols != 0) {
            numRows++;
        }
    }
    
    public void show(final AjaxRequestTarget target, StudyModel study) {
        seriesList.clear();
        if (study.isCollapsed())
            study.expand();
        List<PPSModel> ppss = study.getPPSs();
        List<SeriesModel> seriess;
        SeriesModel series;
        PPSModel pps;
        for (int i = 0 ; i < ppss.size() ; i++) {
            pps = ppss.get(i);
            if (pps.isCollapsed())
                pps.expand();
            seriess = pps.getSeries();
            for (int j = 0 ; j < seriess.size() ; j++) {
                series = seriess.get(j);
                if (series.isCollapsed())
                    series.expand();
                addInstances(series);
            }
        }
        show(target);
    }

    private void addInstances(SeriesModel series) {
        if (series.getInstances().size() > 0) {
            seriesList.add(series);
        }
    }
    
    public void show(final AjaxRequestTarget target, SeriesModel series) {
        if (series.isCollapsed())
            series.expand();
        seriesList.clear();
        addInstances(series);
        show(target);
    }

    @Override
    public void show(final AjaxRequestTarget target) {
        this.calcNumRows();
        selectionChanged = false;
        super.show(target);
    }
    
    private class ImageSelectionPanel extends Panel {

        private static final long serialVersionUID = 1L;

        public ImageSelectionPanel() {
            super("content");
            final WebMarkupContainer datacontainer = new WebMarkupContainer("data");
            datacontainer.setOutputMarkupId(true);
            add(datacontainer);
            IModel<List<SeriesModel>> seriesModel = new IModel<List<SeriesModel>>() {
                private static final long serialVersionUID = 1L;

                public List<SeriesModel> getObject() {
                    return seriesList;
                }
                public void setObject(List<SeriesModel> object) {
                }
                public void detach() {}
                
            };
            final PageableListView<SeriesModel> seriesListView = new PageableListView<SeriesModel>("seriesListView", seriesModel, maxSeries.getObject()) {
                private static final long serialVersionUID = 1L;

                protected void populateItem(final ListItem<SeriesModel> item) {
                    final SeriesModel sm = item.getModelObject();
                    item.add(new DateTimeLabel("datetime", new PropertyModel<Date>(sm,"datetime")).setOutputMarkupId(true));
                    item.add(new Label("seriesDesc", sm.getDescription()).setOutputMarkupId(true));
                    item.add(new Label("modality", sm.getModality()).setOutputMarkupId(true));
                    item.add(new SelectAllLink("selectAll", sm.getInstances(),SeriesModel.INSTANCE_LEVEL, true, item)
                        .add(new TooltipBehaviour("folder.imageselect.", "selectAllInstances")));
                    item.add(new SelectAllLink("deselectAll", sm.getInstances(),SeriesModel.INSTANCE_LEVEL, false, item)
                        .add(new TooltipBehaviour("folder.imageselect.", "deselectAllInstances")));
                    IModel<List<InstanceModel>> instListModel = new IModel<List<InstanceModel>>() {
                        private static final long serialVersionUID = 1L;

                        public List<InstanceModel> getObject() {
                            return sm.getInstances();
                        }

                        public void setObject(List<InstanceModel> object) {
                        }

                        public void detach() {
                        }
                    };
                    final InstanceListView instListView = new InstanceListView("instances", instListModel, maxInstances.getObject());
                    item.setOutputMarkupId(true);
                    item.add(instListView.setOutputMarkupId(true));
                    item.add(new Label("showInstances", new AbstractReadOnlyModel<String>(){
                        private static final long serialVersionUID = 1L;

                        @Override
                        public String getObject() {
                            int from = instListView.getCurrentPage()*instListView.getRowsPerPage();
                            int to = from + instListView.getRowsPerPage();
                            to = Math.min(to, sm.getInstances().size());
                            from++;
                            return "Show "+from+" - "+to+" of "+sm.getInstances().size()+" Instances";
                        }
                    }));
                    item.add(new AjaxPagingNavigator("instanceNavigator", instListView){
                        private static final long serialVersionUID = 1L;
                        @Override
                        public boolean isVisible() {
                            return sm.getInstances().size() > instListView.getRowsPerPage();
                        }
                    }.setOutputMarkupId(true));
                }
            };

            add(new Label("showSeries", new AbstractReadOnlyModel<String>(){
                private static final long serialVersionUID = 1L;

                @Override
                public String getObject() {
                    int from = seriesListView.getCurrentPage()*seriesListView.getRowsPerPage();
                    int to = from + seriesListView.getRowsPerPage();
                    to = Math.min(to, seriesList.size());
                    from++;
                    return "Show "+from+" - "+to+" of "+seriesList.size()+" Series";
                }
            }));
            add(new SelectAllLink("selectAll", seriesList,SeriesModel.INSTANCE_LEVEL, true, datacontainer)
                .add(new TooltipBehaviour("folder.imageselect.", "selectInAllSeries")));
            add(new SelectAllLink("deselectAll", seriesList,SeriesModel.INSTANCE_LEVEL, false, datacontainer)
                .add(new TooltipBehaviour("folder.imageselect.", "deselectInAllSeries")));
            datacontainer.add(seriesListView.setOutputMarkupId(true));
            add(new AjaxPagingNavigator("seriesNavigator", seriesListView){
                private static final long serialVersionUID = 1L;
                @Override
                public boolean isVisible() {
                    return seriesList.size() > seriesListView.getRowsPerPage();
                }
            }.setOutputMarkupId(true));
            datacontainer.setVersioned(false);
        }
        private final class InstanceListView extends PageableListView<InstanceModel> {
            private static final long serialVersionUID = 1L;

            public InstanceListView(String id, IModel<List<InstanceModel>> instances, int maxRows) {
                super(id, instances, maxRows);
            }

            @Override
            protected void populateItem(final ListItem<InstanceModel> item) {
                InstanceModel im = item.getModelObject();
                if ( WADODelegate.getInstance().getRenderType(im.getSopClassUID()) == WADODelegate.IMAGE) {
                    item.add(new WadoImage("wadoimg", im, imgSizeModel).setOutputMarkupId(true));
                    item.add(new Label("sopclass", im.getInstanceNumber()).setOutputMarkupId(true));
                } else {
                    item.add(new Image("wadoimg", new ResourceReference(ImageSelectionWindow.class, "images/no_image.png")).setOutputMarkupId(true));
                    item.add(new Label("sopclass", getNoImageDescription(im)).setOutputMarkupId(true));
                }
                item.add( new AjaxCheckBox("selected", new PropertyModel<Boolean>(item.getModelObject(), "selected")){

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        log.info("selectionChanged!");
                        selectionChanged = true;
                        target.addComponent(this);
                    }}.setOutputMarkupId(true));
                item.add(new AbstractBehavior(){
                    private static final long serialVersionUID = 1L;
                    public void onRendered(Component c) {
                        super.onRendered(c);
                        if ((item.getIndex()+1) % numCols == 0)
                            c.getResponse().write("</tr><tr>");
                    }
                });
            }
        }
        public String getNoImageDescription(InstanceModel im) {
            StringBuilder sb = new StringBuilder();
            DicomObject attrs = im.getDataset();
            DicomElement codeSq = attrs.get(Tag.ConceptNameCodeSequence);
            if (codeSq != null) {
                DicomObject item = codeSq.getDicomObject(0);
                if (item.containsValue(Tag.CodeMeaning)) {
                    sb.append(codeSq.getDicomObject(0).getString(Tag.CodeMeaning));
                } else {
                    sb.append(codeSq.getDicomObject(0).getString(Tag.CodeValue));
                }
            } else {
                if (!attrs.containsValue(Tag.ContentDescription))
                    sb.append(attrs.getString(Tag.ContentDescription));
            }
            return sb.toString();
        }
    }

    public boolean isSelectionChanged() {
        return selectionChanged;
    }        
}
