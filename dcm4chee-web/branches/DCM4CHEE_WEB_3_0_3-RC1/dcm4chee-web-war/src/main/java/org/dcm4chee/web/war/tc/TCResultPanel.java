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
package org.dcm4chee.web.war.tc;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.security.components.SecureComponentHelper;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converters.DateConverter;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.modal.MessageWindow;
import org.dcm4chee.web.common.secure.SecurityBehavior;
import org.dcm4chee.web.common.webview.link.WebviewerLinkProvider;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.tc.TCQueryFilter;
import org.dcm4chee.web.dao.tc.TCQueryLocal;
import org.dcm4chee.web.war.StudyPermissionHelper;
import org.dcm4chee.web.war.config.delegate.WebCfgDelegate;
import org.dcm4chee.web.war.folder.StudyListPage;
import org.dcm4chee.web.war.folder.ViewPort;
import org.dcm4chee.web.war.folder.webviewer.Webviewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since April 28, 2011
 */
public class TCResultPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory
            .getLogger(TCResultPanel.class);

    private WebviewerLinkProvider[] webviewerLinkProviders;

    private StudyPermissionHelper stPermHelper;

    private TCModel selected;

    private MessageWindow msgWin = new MessageWindow("msgWin");
    
    public TCResultPanel(final String id, final TCListModel model) {
        super(id, model != null ? model : new TCListModel());

        setOutputMarkupId(true);

        stPermHelper = StudyPermissionHelper.get();
        
        initWebviewerLinkProvider();
        
        add(msgWin);
        final ModalWindow modalWindow = new ModalWindow("modal-window");
        add(modalWindow);

        final TCStudyListPage studyPage = new TCStudyListPage();
        final ModalWindow studyWindow = new ModalWindow("study-window");
        studyWindow.setPageCreator(new ModalWindow.PageCreator() {
            private static final long serialVersionUID = 1L;
              
            public Page createPage() {
                return studyPage;
            }
        });

        add(studyWindow);
                  
        final SortableTCListProvider tclistProvider = new SortableTCListProvider(
                (TCListModel) getDefaultModel());

        final DataView<TCModel> dataView = new DataView<TCModel>("row",
                tclistProvider) {

            private static final long serialVersionUID = 1L;
            
            private final StudyListLocal dao = (StudyListLocal) JNDIUtils
                    .lookup(StudyListLocal.JNDI_NAME);

            private final Map<String, List<String>> studyActions = new HashMap<String, List<String>>();

            @Override
            protected void populateItem(final Item<TCModel> item) {
                final TCModel tc = item.getModelObject();

                final StringBuilder jsStopEventPropagationInline = new StringBuilder(
                		"var event=arguments[0] || window.event; if (event.stopPropagation) {event.stopPropagation();} else {event.cancelBubble=True;};");
                
                item.setOutputMarkupId(true);
                item.add(new TCMultiLineLabel("title", tc.getTitle(), 80));
                item.add(new TCMultiLineLabel("abstract", tc.getAbstract(), 80));
                item.add(new TCMultiLineLabel("author", tc.getAuthor(), 80));
                item.add(new Label("date",
                        new Model<Date>(tc.getCreationDate())) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public IConverter getConverter(Class<?> type) {
                        return new DateConverter() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public DateFormat getDateFormat(Locale locale) {
                                if (locale == null) {
                                    locale = Locale.getDefault();
                                }

                                return DateFormat.getDateInstance(
                                        DateFormat.MEDIUM, locale);
                            }
                        };
                    }
                });

                final String stuid = tc.getStudyInstanceUID();
                if (dao != null && !studyActions.containsKey(stuid)) {
                    studyActions.put(
                            stuid,
                            dao.findStudyPermissionActions(stuid,
                                    stPermHelper.getDicomRoles()));
                }
                
                item.add(Webviewer.getLink(tc, webviewerLinkProviders,
                        stPermHelper,
                        new TooltipBehaviour("tc.result.table.", "webviewer"), modalWindow)
                        .add(new SecurityBehavior(TCPanel.getModuleName()
                                + ":webviewerInstanceLink")));

                final Component viewLink = new IndicatingAjaxLink<String>("tc-view") {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        selectTC(item, tc, target);
                        openTC(tc, false, target);
                    }
                    protected void onComponentTag(ComponentTag tag)
                    {
                    	super.onComponentTag(tag);
                    	tag.put("ondblclick",jsStopEventPropagationInline);
                    }
                    @Override
                    protected IAjaxCallDecorator getAjaxCallDecorator() {
                        try {
                            return TCPanel.getMaskingBehaviour().getAjaxCallDecorator();
                        } catch (Exception e) {
                            log.error("Failed to get IAjaxCallDecorator: ", e);
                        }
                        return null;
                    }
                }
               .add(new Image("tcViewImg", ImageManager.IMAGE_COMMON_DICOM_DETAILS)
               .add(new ImageSizeBehaviour("vertical-align: middle;")))
               .add(new TooltipBehaviour("tc.result.table.","view"))
               .setOutputMarkupId(true);
               
               final Component editLink = new IndicatingAjaxLink<String>("tc-edit") {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        selectTC(item, tc, target);
                        openTC(tc, true, target);
                    }
                    protected void onComponentTag(ComponentTag tag)
                    {
                    	super.onComponentTag(tag);
                    	tag.put("ondblclick",jsStopEventPropagationInline);
                    }
                    @Override
                    protected IAjaxCallDecorator getAjaxCallDecorator() {
                        try {
                            return TCPanel.getMaskingBehaviour().getAjaxCallDecorator();
                        } catch (Exception e) {
                            log.error("Failed to get IAjaxCallDecorator: ", e);
                        }
                        return null;
                    }
                }
               .add(new Image("tcEditImg", ImageManager.IMAGE_COMMON_DICOM_EDIT)
               .add(new ImageSizeBehaviour("vertical-align: middle;")))
               .add(new TooltipBehaviour("tc.result.table.","edit"))
               .add(new SecurityBehavior(TCPanel.getModuleName() + ":editTC"))
               .setOutputMarkupId(true);
               
               final Component studyLink = new IndicatingAjaxLink<String>("tc-study") {
                   private static final long serialVersionUID = 1L;
                   @Override
                   public void onClick(AjaxRequestTarget target) { 
                       selectTC(item, tc, target);
                       try
                       {
                           TCObject tcObject = TCObject.create(tc);
                           List<TCReferencedStudy> refStudies = tcObject.getReferencedStudies();
                           if (refStudies!=null && !refStudies.isEmpty())
                           {
                               if (refStudies.size()==1)
                               {
                                   studyPage.setStudyInstanceUID(refStudies.get(0).getStudyUID());
                               }
                               else
                               {
                                   studyPage.setPatientIdAndIssuer(tc.getPatientId(), 
                                           tc.getIssuerOfPatientId());
                               }
                           }
                           if (studyPage.getStudyInstanceUID() != null || studyPage.getPatientId() != null) {
                               studyPage.getStudyViewPort().clear();
                               studyWindow.setTitle(new StringResourceModel("tc.result.studywindow.title", this, null,
                                       new Object[]{maskNull(cutAtISOControl(tc.getTitle(), 40),"?"), 
                                                   maskNull(cutAtISOControl(tc.getAbstract(),25),"?"),
                                                   maskNull(cutAtISOControl(tc.getAuthor(), 20),"?"), 
                                                   maskNull(tc.getCreationDate(),tc.getCreatedTime())})); 
                               studyWindow.setInitialWidth(1200);
                               studyWindow.setInitialHeight(600);
                               studyWindow.setMinimalWidth(800);
                               studyWindow.setMinimalHeight(400);
                               if (studyWindow.isShown()) {
                                   log.warn("###### StudyView is already shown ???!!!");
                                   try {
                                       Field showField = ModalWindow.class.getDeclaredField("shown");
                                       showField.setAccessible(true);
                                       showField.set(studyWindow, false);
                                   } catch (Exception e) {
                                       log.error("Failed to reset shown Field from ModalWindow!");
                                   }
                                   log.info("###### studyWindow.isShown():"+studyWindow.isShown());
                               }
                               studyWindow.show(target);
                           } else {
                               log.warn("Showing TC referenced studies discarded: No referened study found!");
                               msgWin.setInfoMessage(getString("tc.result.studywindow.noStudies"));
                               msgWin.show(target);
                           }
                       } catch (Exception e) {
                           msgWin.setErrorMessage(getString("tc.result.studywindow.failed"));
                           msgWin.show(target);
                           log.error("Unable to show TC referenced studies!", e);
                       }
                   }
                   @Override
                   protected void onComponentTag(ComponentTag tag) {
                       super.onComponentTag(tag);
                       tag.put("ondblclick",jsStopEventPropagationInline);
                   }
                   @Override
                   protected IAjaxCallDecorator getAjaxCallDecorator() {
                       try {
                           return TCPanel.getMaskingBehaviour().getAjaxCallDecorator();
                       } catch (Exception e) {
                           log.error("Failed to get IAjaxCallDecorator: ", e);
                       }
                       return null;
                   }
               }
              .add(new Image("tcStudyImg", ImageManager.IMAGE_COMMON_SEARCH)
              .add(new ImageSizeBehaviour("vertical-align: middle;")))
              .add(new TooltipBehaviour("tc.result.table.","showStudy"))
              .add(new SecurityBehavior(TCPanel.getModuleName() + ":showTCStudy"))
              .setOutputMarkupId(true);
               
              item.add(viewLink);
              item.add(editLink);
              item.add(studyLink);
                
                item.add(new AttributeModifier("class", true,
                        new AbstractReadOnlyModel<String>() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public String getObject() {
                                if (selected != null && selected.equals(tc)) {
                                    return "mouse-out-selected";
                                } else {
                                    return item.getIndex() % 2 == 1 ? 
                                            "even-mouse-out" : "odd-mouse-out";
                                }
                            }
                        }));
                
                item.add(new AttributeModifier("selected", true,
                        new AbstractReadOnlyModel<String>() {
                            private static final long serialVersionUID = 1L;
                            @Override
                            public String getObject() {
                                if (selected != null && selected.equals(tc)) {
                                    return "selected";
                                } else {
                                    return null;
                                }
                            }
                        }));
                
                item.add(new AttributeModifier("onmouseover", true,
                        new AbstractReadOnlyModel<String>() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public String getObject() {
                                StringBuffer sbuf = new StringBuffer();
                                sbuf.append("if ($(this).attr('selected')==null) {");
                                sbuf.append("   $(this).removeClass();");
                                sbuf.append("   if (").append(item.getIndex()).append("%2==1) $(this).addClass('even-mouse-over');");
                                sbuf.append("   else $(this).addClass('odd-mouse-over');");
                                sbuf.append("}");
                                return sbuf.toString();
                            }
                        }));

                item.add(new AttributeModifier("onmouseout", true,
                        new AbstractReadOnlyModel<String>() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public String getObject() {
                                StringBuffer sbuf = new StringBuffer();
                                sbuf.append("if ($(this).attr('selected')==null) {");
                                sbuf.append("   $(this).removeClass();");
                                sbuf.append("   if (").append(item.getIndex()).append("%2==1) $(this).addClass('even-mouse-out');");
                                sbuf.append("   else $(this).addClass('odd-mouse-out');");
                                sbuf.append("}");
                                return sbuf.toString();
                            }
                        }));
                
                item.add(new AjaxEventBehavior("onclick") {
					private static final long serialVersionUID = 1L;
					@Override
                    protected void onEvent(AjaxRequestTarget target)
                    {
                        selectTC(item, tc, target);
                    }
                });
                
                item.add(new AjaxEventBehavior("ondblclick") {
					private static final long serialVersionUID = 1L;
					@Override
                    protected void onEvent(AjaxRequestTarget target)
                    {
                        boolean edit = WebCfgDelegate.getInstance().getTCEditOnDoubleClick();
                        if (edit)
                        {
                            edit = SecureComponentHelper.isActionAuthorized(editLink,"render");
                        }
                        
                        openTC(selected, edit, target);
                    }
                    @Override
                    protected IAjaxCallDecorator getAjaxCallDecorator() {
                        try {
                            return new IAjaxCallDecorator() {
                                private static final long serialVersionUID = 1L;
                                public final CharSequence decorateScript(CharSequence script) {
                                    return "if(typeof showMask == 'function') { showMask(); $('body').css('cursor','wait'); };"+script;
                                }
                                public final CharSequence decorateOnSuccessScript(CharSequence script) {
                                    return "hideMask();$('body').css('cursor','');"+script;
                                }
                                public final CharSequence decorateOnFailureScript(CharSequence script) {
                                    return "hideMask();$('body').css('cursor','');"+script;
                                }
                            };
                        } catch (Exception e) {
                            log.error("Failed to get IAjaxCallDecorator: ", e);
                        }
                        return null;
                    }
                });
            }
        };
        
        dataView.setItemReuseStrategy(new ReuseIfModelsEqualStrategy());
        dataView.setItemsPerPage(WebCfgDelegate.getInstance()
                .getDefaultFolderPagesize());
        dataView.setOutputMarkupId(true);

        OrderByBorder titleBorder = new OrderByBorder("titleColumn", "Title",
                tclistProvider) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                if (selected == null) {
                    dataView.setCurrentPage(0);
                } else {
                    dataView.setCurrentPage(tclistProvider.getCurrentPageIndex(
                            selected, dataView.getItemsPerPage()));
                }
            }
        };

        OrderByBorder abstractBorder = new OrderByBorder("abstractColumn",
                "Abstract", tclistProvider) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                if (selected == null) {
                    dataView.setCurrentPage(0);
                } else {
                    dataView.setCurrentPage(tclistProvider.getCurrentPageIndex(
                            selected, dataView.getItemsPerPage()));
                }
            }
        };

        OrderByBorder authorBorder = new OrderByBorder("authorColumn",
                "Author", tclistProvider) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                if (selected == null) {
                    dataView.setCurrentPage(0);
                } else {
                    dataView.setCurrentPage(tclistProvider.getCurrentPageIndex(
                            selected, dataView.getItemsPerPage()));
                }
            }
        };

        OrderByBorder dateBorder = new OrderByBorder("dateColumn", "Date",
                tclistProvider) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSortChanged() {
                if (selected == null) {
                    dataView.setCurrentPage(0);
                } else {
                    dataView.setCurrentPage(tclistProvider.getCurrentPageIndex(
                            selected, dataView.getItemsPerPage()));
                }
            }
        };

        titleBorder.getBodyContainer().add(
                new Label("titleTitle", new ResourceModel(
                        "tc.result.table.header.title.text")));
        abstractBorder.getBodyContainer().add(
                new Label("abstractTitle", new ResourceModel(
                        "tc.result.table.header.abstract.text")));
        authorBorder.getBodyContainer().add(
                new Label("authorTitle", new ResourceModel(
                        "tc.result.table.header.author.text")));
        dateBorder.getBodyContainer().add(
                new Label("dateTitle", new ResourceModel(
                        "tc.result.table.header.date.text")));

        add(titleBorder);
        add(abstractBorder);
        add(authorBorder);
        add(dateBorder);
        add(dataView);

        add(new Label("numberOfMatchingInstances", new StringResourceModel(
                "tc.list.numberOfMatchingInstances", this, null,
                new Object[] { new Model<Integer>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Integer getObject() {
                        return ((TCListModel) TCResultPanel.this
                                .getDefaultModel()).getObject().size();
                    }
                } })));

        add(new PagingNavigator("navigator", dataView));
    }
    
    public void clearSelected() {
        selected = null;
    }
    
    
    protected Component[] selectionChanged(TCModel tc)
    {
        return null;
    }
    
    protected Component[] selectTC(TCModel tc)
    {
        if (selected==null || 
            !selected.getSOPInstanceUID().equals(tc.getSOPInstanceUID()))
        {
            selected = tc;
            
            return selectionChanged(tc);
        }
        
        return null;
    }
    
    protected void openTC(TCModel tc, boolean edit, AjaxRequestTarget target)
    {
        /* do nothing by default */
    }

    private void initWebviewerLinkProvider() {
        List<String> names = WebCfgDelegate.getInstance()
                .getWebviewerNameList();
        if (names == null) {
            names = WebCfgDelegate.getInstance()
                    .getInstalledWebViewerNameList();
        }
        if (names == null || names.isEmpty()) {
            webviewerLinkProviders = null;
        } else {
            webviewerLinkProviders = new WebviewerLinkProvider[names.size()];
            Map<String, String> baseUrls = WebCfgDelegate.getInstance()
                    .getWebviewerBaseUrlMap();
            for (int i = 0; i < webviewerLinkProviders.length; i++) {
                webviewerLinkProviders[i] = new WebviewerLinkProvider(
                        names.get(i));
                webviewerLinkProviders[i]
                        .setBaseUrl(baseUrls.get(names.get(i)));
            }
        }
    }
    
    private void selectTC(final Item<TCModel> item, final TCModel tc,
            AjaxRequestTarget target) {
        Component[] toUpdate = selectTC(tc);
        
        if (toUpdate!=null && toUpdate.length>0)
        {
            for (Component c : toUpdate)
            {
                target.addComponent(c);
            }
        }
        
        target.appendJavascript("selectTC('"+item.getMarkupId()+
                "','mouse-out-selected','even-mouse-out','odd-mouse-out')");
    }

    private static Object maskNull(Object val, Object def) {
        return val != null ? val : def;
    }
    
    private static String cutAtISOControl(String s, int maxlen) {
        if (s == null)
            return null;
        for (int i = 0, len = Math.min(s.length(), maxlen) ; i < len ; i++) {
            if (Character.isISOControl(s.charAt(i))) {
                return s.substring(0, i)+"..";
            }
        }
        return s.length() > maxlen ? s.substring(0, maxlen)+".." : s;
    }

    private static class TCStudyListPage extends WebPage {
        private String stuid;
        private String patid;
        private String issuerOfPatId;
        private StudyListPage studyListPage;
        private ViewPort viewPort;
        private static final ResourceReference baseCSS = new CompressedResourceReference(
                BaseWicketPage.class, "base-style.css");
        private static final ResourceReference folderCSS = new CompressedResourceReference(
                StudyListPage.class, "folder-style.css");
        
        public TCStudyListPage() {
            studyListPage = new StudyListPage("tcStudyList") {
                private static final long serialVersionUID = 1L;

                @Override
                protected ViewPort getViewPort() {
                    return getStudyViewPort();
                }
                
                @Override
                protected PageParameters getPageParameters() {
                    PageParameters params = null;
                    
                    if (stuid!=null) {
                        params = new PageParameters();
                        params.put("studyIUID", stuid);
                        params.put("disableSearch", "true");
                        params.put("query", "true");
                    } else if (patid!=null) {
                        params = new PageParameters();
                        params.put("patID", patid);
                        if (issuerOfPatId!=null) {
                            params.put("issuer", issuerOfPatId);
                        }
                        params.put("latestStudiesFirst", "true");
                        params.put("disableSearch", "true");
                        params.put("query", "true");
                    }
                    return params;
                }
                
            };
            add(studyListPage);
            if (baseCSS != null) {
                add(CSSPackageResource.getHeaderContribution(baseCSS));
            }
            if (folderCSS != null) {
                add(CSSPackageResource.getHeaderContribution(folderCSS));
            }
        }
        
        protected ViewPort getStudyViewPort() {
            if (viewPort==null) {
                viewPort = new ViewPort();
            }
            return viewPort;
        }
        
        public String getStudyInstanceUID()
        {
            return stuid;
        }
        
        public String getPatientId()
        {
            return patid;
        }
        
        public String getIssuerOfPatientId()
        {
            return issuerOfPatId;
        }
        
        public void setStudyInstanceUID(String stuid)
        {
            this.stuid = stuid;
            this.patid=null;
            this.issuerOfPatId=null;
        }
        
        public void setPatientIdAndIssuer(String patId, String issuerOfPatId)
        {
            this.stuid=null;
            this.patid=patId;
            this.issuerOfPatId=issuerOfPatId;
        }
        
    }
    

    public static class TCListModel extends Model<ArrayList<TCModel>> {

        private static final long serialVersionUID = 1L;

        private TCQueryLocal dao = (TCQueryLocal) JNDIUtils
                .lookup(TCQueryLocal.JNDI_NAME);

        private TCQueryFilter filter;

        public TCListModel(TCQueryFilter filter) {
            super(new ArrayList<TCModel>(0));

            this.filter = filter != null ? filter : new TCQueryFilter();
        }

        public TCListModel() {
            this(new TCQueryFilter());
        }

        public ArrayList<TCModel> load() {
            List<Instance> instances = doSearch(filter);

            if (instances != null && !instances.isEmpty()) {
                ArrayList<TCModel> models = new ArrayList<TCModel>(
                        instances.size());
                List<String> iuids = new ArrayList<String>(instances.size());
                for (Instance instance : instances) {
                    iuids.add(instance.getSOPInstanceUID());
                    if (!GlobalTCFilter.getInstance().isFiltered(instance))
                    {
                        TCModel model = new TCModel(instance);
                        models.add(model);
                    }
                }
                return models;
            } else {
                return new ArrayList<TCModel>(0);
            }
        }

        public void update(TCQueryFilter filter) {
            if (filter != null) {
                this.filter = filter;

                setObject(load());
            }
        }
        
        public void addToFilter(TCModel tc)
        {
            GlobalTCFilter.getInstance().addByUID(
                    tc.getSOPInstanceUID(), true);
        }
        
        public void removeFromFilter(TCModel tc)
        {
            GlobalTCFilter.getInstance().removeByUID(
                    tc.getSOPInstanceUID());
        }
        
        public TCModel findByIUID(String iuid)
        {
            List<TCModel> list = getObject();
            if (list!=null)
            {
                for (TCModel tc : list)
                {
                    if (iuid.equals(tc.getSOPInstanceUID()))
                    {
                        return tc;
                    }
                }
            }
            return null;
        }

        private List<Instance> doSearch(TCQueryFilter filter) {
            try {
                log.info(filter.toString());

                List<String> roles = StudyPermissionHelper.get()
                        .applyStudyPermissions() ? StudyPermissionHelper.get()
                        .getDicomRoles() : null;

                return dao.findMatchingInstances(filter, roles, WebCfgDelegate
                        .getInstance().getTCRestrictedSourceAETList());
            } catch (Exception e) {
                log.error("TC query failed!", e);

                return Collections.emptyList();
            }
        }
    }

    
    public class SortableTCListProvider extends SortableDataProvider<TCModel> {

        private static final long serialVersionUID = 1L;

        private TCListModel model;

        public SortableTCListProvider(TCListModel model) {
            this.model = model;

            // set default sort
            setSort(TCModel.Sorter.Date.name(), false);
        }

        @SuppressWarnings({ "unchecked" })
        @Override
        public Iterator<TCModel> iterator(int first, int count) {
            List<TCModel> items = model.getObject();

            if (items != null && !items.isEmpty()) {
                Comparator<TCModel> comparator = getComparator(getSort());
                if (comparator != null) {
                    Collections.sort(items, comparator);
                }

                return items.subList(first, first + count).iterator();
            }

            return (Iterator) Collections.emptyList().iterator();
        }

        @Override
        public int size() {
            List<TCModel> items = model.getObject();

            return items == null ? 0 : items.size();
        }

        @Override
        public IModel<TCModel> model(TCModel object) {
            return new Model<TCModel>(object);
        }

        public int getCurrentIndex(TCModel tcModel) {
            List<TCModel> items = model.getObject();

            if (items != null && !items.isEmpty()) {
                Comparator<TCModel> comparator = getComparator(getSort());
                if (comparator != null) {
                    Collections.sort(items, comparator);
                }

                return items.indexOf(tcModel);
            }

            return -1;
        }

        public int getCurrentPageIndex(TCModel tcModel, int pagesize) {
            try {
                return getCurrentIndex(tcModel) / pagesize;
            } catch (Exception e) {
                return 0;
            }
        }

        private Comparator<TCModel> getComparator(SortParam p) {
            if (p != null) {
                TCModel.Sorter sorter = TCModel.Sorter.valueOf(p.getProperty());

                if (sorter != null) {
                    return sorter.getComparator(p.isAscending());
                }
            }

            return null;
        }
    }
    
    private static class GlobalTCFilter
    {
        private static GlobalTCFilter instance;
        
        //after a period of 5min. expired instances are no longer filtered out
        //and are going to be autom. removed from the map
        private static final long IUID_EXPIRE_PERIOD = 300000;
        
        private Map<String, Long> iuidsExpire;
        private List<String> iuids;
        
        private GlobalTCFilter()
        {
            iuids = new ArrayList<String>();
            iuidsExpire = new HashMap<String, Long>(10);
         }
        
        public static synchronized GlobalTCFilter getInstance()
        {
            if (instance==null)
            {
                instance = new GlobalTCFilter();
            }
            return instance;
        }
        
        public Collection<String> getFilteredOutUIDs()
        {
            List<String> list = new ArrayList<String>();
            if (iuids!=null)
            {
                list.addAll(iuids);
            }
            if (iuidsExpire!=null)
            {
                list.addAll(iuidsExpire.keySet());
            }
            return list;
        }
        
        public synchronized boolean isFiltered(Instance instance)
        {
            //check by instance uid
            String iuid = instance.getSOPInstanceUID();
            if (iuidsExpire!=null && iuidsExpire.containsKey(iuid))
            {
                Long timestamp = iuidsExpire.get(iuid);
                if (timestamp+IUID_EXPIRE_PERIOD>System.currentTimeMillis())
                {
                    return true;
                }
                else
                {
                    iuidsExpire.remove(iuid);
                    if (iuidsExpire.isEmpty())
                    {
                        iuidsExpire=null;
                    }
                }
            }
            if (iuids!=null && iuids.contains(iuid))
            {
                return true;
            }
            
            return false;
        }
        
        public synchronized void addByUID(String iuid, boolean expire)
        {
            if (expire)
            {
                if (iuidsExpire==null)
                {
                    iuidsExpire = new HashMap<String, Long>();
                }
                
                iuidsExpire.put(iuid, System.currentTimeMillis());   
                
                if (iuids!=null)
                {
                    iuids.remove(iuid);
                    
                    if (iuids.isEmpty())
                    {
                        iuids = null;
                    }
                }
            }
            else
            {
                if (iuids==null)
                {
                    iuids = new ArrayList<String>(10);
                }
                if (!iuids.contains(iuid))
                {
                    iuids.add(iuid);
                    
                    if (iuidsExpire!=null)
                    {
                        iuidsExpire.remove(iuid);
                        
                        if (iuidsExpire.isEmpty())
                        {
                            iuidsExpire=null;
                        }
                    }
                }
            }
        }
        
        public synchronized void removeByUID(String iuid)
        {
            if (iuidsExpire!=null)
            {
                iuidsExpire.remove(iuid);
                if (iuidsExpire.isEmpty())
                {
                    iuidsExpire = null;
                }
            }
            if (iuids!=null)
            {
                iuids.remove(iuid);
                if (iuids.isEmpty())
                {
                    iuids = null;
                }
            }
        }
    }
}
