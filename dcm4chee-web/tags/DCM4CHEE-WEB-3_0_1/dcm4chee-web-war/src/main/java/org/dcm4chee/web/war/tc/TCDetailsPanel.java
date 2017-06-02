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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.resource.loader.PackageStringResourceLoader;
import org.dcm4chee.web.dao.tc.TCQueryFilterKey;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogueProvider;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since May 27, 2011
 */
public class TCDetailsPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private WebMarkupContainer errordetailsContainer;

    private WebMarkupContainer nodetailsContainer;

    private WebMarkupContainer detailsContainer;

    public TCDetailsPanel(final String id) {
        super(id, new Model<TCDetails>());

        setOutputMarkupId(true);

        TCKeywordCatalogueProvider catProv = TCKeywordCatalogueProvider
                .getInstance();
        final Model<TCDetails> tabModel = new Model<TCDetails>() {

            private static final long serialVersionUID = 1L;

            @Override
            public TCDetails getObject() {
                return (TCDetails) TCDetailsPanel.this.getDefaultModelObject();
            }
        };

        List<ITab> tabs = new ArrayList<ITab>();
        tabs.add(new AbstractDetailsTab(new ResourceModel(
                "tc.details.tab.info.title.text")) {

            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String id) {
                Panel tab = new TCDetailsInfoTab(id);
                tab.setDefaultModel(tabModel);
                return tab;
            }

            @Override
            public boolean isDataAvailable() {
                return true;
            }
        });

        if (!catProv.hasCatalogue(TCQueryFilterKey.Diagnosis)) {
            tabs.add(new AbstractDetailsTab(new ResourceModel(
                    "tc.details.tab.diagnosis.title.text")) {

                private static final long serialVersionUID = 1L;

                @Override
                public Panel getPanel(String id) {
                    Panel tab = new TCDetailsDiagnosisTab(id);
                    tab.setDefaultModel(tabModel);
                    return tab;
                }

                @Override
                public boolean isDataAvailable() {
                    TCDetails tc = getTCObject();

                    if (tc != null) {
                        return tc.getDiagnosis() != null
                                || tc.getDiagnosisConfirmed() != null;
                    }

                    return false;
                }
            });
        }

        if (!catProv.hasCatalogue(TCQueryFilterKey.DifferentialDiagnosis)) {
            tabs.add(new AbstractDetailsTab(new ResourceModel(
                    "tc.details.tab.differential-diagnosis.title.text")) {

                private static final long serialVersionUID = 1L;

                @Override
                public Panel getPanel(String id) {
                    Panel tab = new TCDetailsDefaultTab(id) {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public TCQueryFilterKey getKey() {
                            return TCQueryFilterKey.DifferentialDiagnosis;
                        }
                    };
                    tab.setDefaultModel(tabModel);
                    return tab;
                }

                @Override
                public boolean isDataAvailable() {
                    TCDetails tc = getTCObject();

                    return tc != null && tc.getDiffDiagnosis() != null;
                }
            });
        }

        if (!catProv.hasCatalogue(TCQueryFilterKey.Finding)) {
            tabs.add(new AbstractDetailsTab(new ResourceModel(
                    "tc.details.tab.finding.title.text")) {

                private static final long serialVersionUID = 1L;

                @Override
                public Panel getPanel(String id) {
                    Panel tab = new TCDetailsDefaultTab(id) {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public TCQueryFilterKey getKey() {
                            return TCQueryFilterKey.Finding;
                        }
                    };
                    tab.setDefaultModel(tabModel);
                    return tab;
                }

                @Override
                public boolean isDataAvailable() {
                    TCDetails tc = getTCObject();

                    return tc != null && tc.getFinding() != null;
                }
            });
        }

        tabs.add(new AbstractDetailsTab(new ResourceModel(
                "tc.details.tab.history.title.text")) {

            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String id) {
                Panel tab = new TCDetailsDefaultTab(id) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public TCQueryFilterKey getKey() {
                        return TCQueryFilterKey.History;
                    }
                };
                tab.setDefaultModel(tabModel);
                return tab;
            }

            @Override
            public boolean isDataAvailable() {
                TCDetails tc = getTCObject();

                return tc != null && tc.getHistory() != null;
            }
        });

        tabs.add(new AbstractDetailsTab(new ResourceModel(
                "tc.details.tab.discussion.title.text")) {

            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String id) {
                Panel tab = new TCDetailsDefaultTab(id) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public TCQueryFilterKey getKey() {
                        return TCQueryFilterKey.Discussion;
                    }
                };
                tab.setDefaultModel(tabModel);
                return tab;
            }

            @Override
            public boolean isDataAvailable() {
                TCDetails tc = getTCObject();

                return tc != null && tc.getDiscussion() != null;
            }
        });

        tabs.add(new AbstractDetailsTab(new ResourceModel(
                "tc.details.tab.organsystem.title.text")) {

            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String id) {
                Panel tab = new TCDetailsDefaultTab(id) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public TCQueryFilterKey getKey() {
                        return TCQueryFilterKey.OrganSystem;
                    }
                };
                tab.setDefaultModel(tabModel);
                return tab;
            }

            @Override
            public boolean isDataAvailable() {
                TCDetails tc = getTCObject();

                return tc != null && tc.getOrganSystem() != null;
            }
        });

        tabs.add(new AbstractDetailsTab(new ResourceModel(
                "tc.details.tab.author.title.text")) {

            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String id) {
                Panel tab = new TCDetailsAuthorTab(id);
                tab.setDefaultModel(tabModel);
                return tab;
            }

            @Override
            public boolean isDataAvailable() {
                TCDetails tc = getTCObject();

                if (tc != null) {
                    return tc.getAuthorName() != null
                            || tc.getAuthorAffiliation() != null
                            || tc.getAuthorContact() != null;
                }

                return false;
            }
        });

        tabs.add(new AbstractDetailsTab(new Model<String>() {

            private static final long serialVersionUID = 1L;

            String title = new PackageStringResourceLoader()
                    .loadStringResource(TCDetailsPanel.class,
                            "tc.details.tab.images.title.text", null, null);

            @Override
            public String getObject() {
                TCDetails tc = getTCObject();
                int nImages = tc != null ? tc.getReferencedInstances().size()
                        : 0;
                StringBuffer sbuf = new StringBuffer(title);
                sbuf.append(" (");
                sbuf.append(nImages);
                sbuf.append(")");
                return sbuf.toString();
            }
        }) {

            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String id) {
                Panel tab = new TCDetailsImagesTab(id);
                tab.setDefaultModel(tabModel);
                return tab;
            }

            @Override
            public boolean isDataAvailable() {
                TCDetails tc = getTCObject();

                return tc != null && tc.getReferencedImages() != null
                        && !tc.getReferencedImages().isEmpty();
            }
        });

        tabs.add(new AbstractDetailsTab(new ResourceModel(
                "tc.details.tab.bibliography.title.text")) {

            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String id) {
                Panel tab = new TCDetailsBibliographyTab(id);
                tab.setDefaultModel(tabModel);
                return tab;
            }

            @Override
            public boolean isDataAvailable() {
                TCDetails tc = getTCObject();

                return tc != null && tc.getBibliographicReferences() != null
                        && !tc.getBibliographicReferences().isEmpty();
            }
        });

        nodetailsContainer = new WebMarkupContainer("no-details-panel");
        nodetailsContainer.setOutputMarkupId(true);

        errordetailsContainer = new WebMarkupContainer("error-details-panel");
        errordetailsContainer.setOutputMarkupId(true);

        detailsContainer = new WebMarkupContainer("details-info-panel");
        detailsContainer.setOutputMarkupId(true);
        detailsContainer.add(new DetailsTabbedPanel("details-tabs", tabs));

        nodetailsContainer.setVisible(true);
        errordetailsContainer.setVisible(false);
        detailsContainer.setVisible(false);

        add(nodetailsContainer);
        add(errordetailsContainer);
        add(detailsContainer);
    }

    public void setTCObject(TCDetails tc) {
        nodetailsContainer.setVisible(tc == null);
        errordetailsContainer.setVisible(false);
        detailsContainer.setVisible(tc != null);
        ((DetailsTabbedPanel) detailsContainer.get(0)).setSelectedTab(0);

        setDefaultModel(new Model<TCDetails>(tc));
    }

    public TCDetails getTCObject() {
        return (TCDetails) getDefaultModelObject();
    }

    public void clearTCObject(boolean error) {
        nodetailsContainer.setVisible(!error);
        errordetailsContainer.setVisible(error);
        detailsContainer.setVisible(false);

        setDefaultModelObject(null);
    }

    private static abstract class AbstractDetailsTab extends AbstractTab {

        private static final long serialVersionUID = 1L;

        public AbstractDetailsTab(IModel<String> titleModel) {
            super(titleModel);
        }

        public abstract boolean isDataAvailable();
    }

    private static class DetailsTabbedPanel extends AjaxTabbedPanel {

        private static final long serialVersionUID = 1L;

        public DetailsTabbedPanel(final String id, List<ITab> tabs) {
            super(id, tabs);
        }

        @Override
        protected WebMarkupContainer newLink(String linkId, final int index) {
            AjaxFallbackLink<Void> link = new AjaxFallbackLink<Void>(linkId) {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    if (((AbstractDetailsTab) getTabs().get(index))
                            .isDataAvailable()) {
                        setSelectedTab(index);
                        if (target != null) {
                            target.addComponent(DetailsTabbedPanel.this);
                        }
                        onAjaxUpdate(target);
                    }
                }
            };
            link.add(new AttributeModifier("class", true,
                    new AbstractReadOnlyModel<String>() {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public String getObject() {
                            if (((AbstractDetailsTab) getTabs().get(index))
                                    .isDataAvailable()) {
                                return "enabled";
                            } else {
                                return "disabled";
                            }
                        }
                    }));
            return link;
        }
    }
}
