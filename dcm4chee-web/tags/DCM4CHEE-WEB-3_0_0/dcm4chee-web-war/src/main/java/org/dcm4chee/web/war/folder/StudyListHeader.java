package org.dcm4chee.web.war.folder;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.war.AuthenticatedWebSession;
import org.dcm4chee.web.war.common.SelectAllLink;
import org.dcm4chee.web.war.folder.model.PatientModel;

public class StudyListHeader extends Panel {

    private static final long serialVersionUID = 1L;
    
    private int headerExpandLevel = 1;
    private int expandAllLevel = 5;
    private IModel<Boolean> autoExpand = new Model<Boolean>(false);
 
    private final class Row extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        
        private final int entityLevel;

        public Row(String id, int entityLevel) {
            super(id);
            this.entityLevel = entityLevel;
        }

        @Override
        public boolean isVisible() {
            return StudyListHeader.this.headerExpandLevel >= Row.this.entityLevel;
        }
    }

    private final class Cell extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        
        private final int entityLevel;

        public Cell(String id, int entityLevel) {
            super(id);
            this.entityLevel = entityLevel;
            add(new AjaxFallbackLink<Object>("expand"){

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    headerExpandLevel = headerExpandLevel > Cell.this.entityLevel ?
                            Cell.this.entityLevel : Cell.this.entityLevel + 1;
                    if (target != null) {
                        target.addComponent(StudyListHeader.this);
                    }
                }
            }.add( new Image("expandImg", new AbstractReadOnlyModel<ResourceReference>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ResourceReference getObject() {
                    return StudyListHeader.this.headerExpandLevel <= Cell.this.entityLevel ? 
                            ImageManager.IMAGE_COMMON_EXPAND : ImageManager.IMAGE_COMMON_COLLAPSE;
                }
            })
            .add(new ImageSizeBehaviour())));
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
           super.onComponentTag(tag);
           tag.put("rowspan", 1 + headerExpandLevel - entityLevel);
        }
    }

    public StudyListHeader(String id, Component toUpd) {
        super(id);
        setOutputMarkupId(true);
        
        add(new AjaxCheckBox("autoExpand", autoExpand) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (autoExpand.getObject()) {
                    headerExpandLevel = expandAllLevel;
                    target.addComponent(StudyListHeader.this);
                }
            }
        }
        .add(new TooltipBehaviour("folder.search.","autoExpand")));

        Cell patCell = new Cell("cell", 0);
        List<PatientModel> patients = ((AuthenticatedWebSession) getSession()).getFolderViewPort().getPatients();
        toUpd.setOutputMarkupId(true);
        add(new Row("patient", PatientModel.PATIENT_LEVEL).add(patCell)
                .add(new SelectAllLink("selectAll", patients,PatientModel.PATIENT_LEVEL, true, toUpd, true)
                .add(new TooltipBehaviour("folder.studyview.", "selectAllPatients")))
                .add(new SelectAllLink("deselectAll", patients,PatientModel.PATIENT_LEVEL, false, toUpd, true)
                .add(new TooltipBehaviour("folder.studyview.", "deselectAllPatients")))
                );
        add(new Row("study", PatientModel.STUDY_LEVEL).add(new Cell("cell", 1))
                .add(new SelectAllLink("selectAll", patients,PatientModel.STUDY_LEVEL, true, toUpd, true)
                .add(new TooltipBehaviour("folder.studyview.", "selectAllStudies")))
                .add(new SelectAllLink("deselectAll", patients,PatientModel.STUDY_LEVEL, false, toUpd, true)
                .add(new TooltipBehaviour("folder.studyview.", "deselectAllStudies")))
                );
        add(new Row("pps", PatientModel.PPS_LEVEL).add(new Cell("cell", 2)));
        add(new Row("series", PatientModel.SERIES_LEVEL).add(new Cell("cell", 3))
                .add(new SelectAllLink("selectAll", patients,PatientModel.SERIES_LEVEL, true, toUpd, true)
                .add(new TooltipBehaviour("folder.studyview.", "selectAllSeries")))
                .add(new SelectAllLink("deselectAll", patients,PatientModel.SERIES_LEVEL, false, toUpd, true)
                .add(new TooltipBehaviour("folder.studyview.", "deselectAllSeries")))
                );
        add(new Row("instance", PatientModel.INSTANCE_LEVEL).add(new Cell("cell", 4)));
        add(new Row("file", 5));
    }

    public void setExpandAllLevel(int expandAllLevel) {
        this.expandAllLevel = expandAllLevel;
        if (autoExpand.getObject())
            this.headerExpandLevel = expandAllLevel;
    }
    
    public int getExpandAllLevel() {
        return expandAllLevel;
    }

    public void expandToLevel(int level) {
        headerExpandLevel = level;
    }
}
