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

import java.text.DateFormat;
import java.util.ArrayList;
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
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converters.DateConverter;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.secure.SecurityBehavior;
import org.dcm4chee.web.common.webview.link.WebviewerLinkProvider;
import org.dcm4chee.web.dao.folder.StudyListLocal;
import org.dcm4chee.web.dao.tc.TCQueryFilter;
import org.dcm4chee.web.dao.tc.TCQueryLocal;
import org.dcm4chee.web.war.StudyPermissionHelper;
import org.dcm4chee.web.war.config.delegate.WebCfgDelegate;
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

    public TCResultPanel(final String id, TCListModel model) {
        super(id, model != null ? model : new TCListModel());

        setOutputMarkupId(true);

        stPermHelper = StudyPermissionHelper.get();

        initWebviewerLinkProvider();
        final ModalWindow modalWindow = new ModalWindow("modal-window");
        add(modalWindow);
        
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

                item.add(new AbstractDefaultAjaxBehavior() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void respond(AjaxRequestTarget target) {
                        target.addComponent(item);
                    }

                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);

                        if (selected != null && selected.equals(tc)) {
                            tag.put("class", "mouse-out-selected");
                        } else {
                            tag.put("class",
                                    item.getIndex() % 2 == 1 ? "even-mouse-out"
                                            : "odd-mouse-out");
                        }
                    }
                });

                item.add(new AttributeModifier("onmouseover", true,
                        new AbstractReadOnlyModel<String>() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public String getObject() {
                                if (selected != null && selected.equals(tc)) {
                                    return "this.className='mouse-over-selected'";
                                } else {
                                    return (item.getIndex() % 2 == 1) ? "this.className='even-mouse-over'"
                                            : "this.className='odd-mouse-over'";
                                }
                            }
                        }));

                item.add(new AttributeModifier("onmouseout", true,
                        new AbstractReadOnlyModel<String>() {

                            private static final long serialVersionUID = 1L;

                            @Override
                            public String getObject() {
                                if (selected != null && selected.equals(tc)) {
                                    return "this.className='mouse-out-selected'";
                                } else {
                                    return (item.getIndex() % 2 == 1) ? "this.className='even-mouse-out'"
                                            : "this.className='odd-mouse-out'";
                                }
                            }
                        }));

                item.add(new AjaxEventBehavior("onclick") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onEvent(AjaxRequestTarget target) {
                        selected = tc;

                        Component toUpdate = selectionChanged(tc);

                        if (toUpdate != null && target != null) {
                            target.addComponent(toUpdate);
                        }

                        target.addComponent(TCResultPanel.this);
                    }
                });
            }
        };

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

    protected Component selectionChanged(TCModel tc) {
        /* do nothing by default */
        return null;
    }

    public void clearSelected() {
        selected = null;
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
                for (Instance instance : instances) {
                    models.add(new TCModel(instance));
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
}
