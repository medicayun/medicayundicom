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
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.dcm4chee.web.common.ajax.MaskingAjaxCallBehavior;
import org.dcm4chee.web.dao.tc.TCQueryFilter;
import org.dcm4chee.web.war.tc.TCResultPanel.TCListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since April 28, 2011
 */
public class TCPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(TCPanel.class);

    private static final ResourceReference CSS = new CompressedResourceReference(
            TCPanel.class, "tc-style.css");

    public static final String ModuleName = "tc";

    private static final MaskingAjaxCallBehavior macb = new MaskingAjaxCallBehavior();

    public TCPanel(final String id) {
        super(id);

        PopupCloseables.getInstance().clear();

        if (TCPanel.CSS != null) {
            add(CSSPackageResource.getHeaderContribution(TCPanel.CSS));
        }

        final TCDetailsPanel detailsPanel = new TCDetailsPanel("details-panel");

        final TCListModel listModel = new TCListModel();
        final TCResultPanel listPanel = new TCResultPanel("result-panel",
                listModel) {

            private static final long serialVersionUID = 1L;

            @Override
            public Component selectionChanged(TCModel tc) {
                try {
                    if (tc == null) {
                        detailsPanel.clearTCObject(false);
                    } else {
                        detailsPanel.setTCObject(TCDetails.create(tc));
                    }
                } catch (Exception e) {
                    log.error("Parsing TC object failed!", e);

                    detailsPanel.clearTCObject(true);
                }

                return detailsPanel;
            }
        };

        add(macb);
        add(new TCSearchPanel("search-panel") {

            private static final long serialVersionUID = 1L;

            @Override
            public Component[] doSearch(TCQueryFilter filter) {
                detailsPanel.clearTCObject(false);
                listPanel.clearSelected();
                listModel.update(filter);

                return new Component[] { detailsPanel, listPanel };
            }
        });

        add(listPanel);
        add(detailsPanel);

        // add(new PopupCloseBehavior());
    }

    public static String getModuleName() {
        return ModuleName;
    }

    public static MaskingAjaxCallBehavior getMaskingBehaviour() {
        return macb;
    }

    public static class PopupCloseables {
        private static PopupCloseables instance;

        private List<IPopupCloseable> closeables;

        private List<IPopupCloseable> ignoreOnNextClick;

        public static synchronized PopupCloseables getInstance() {
            if (instance == null) {
                instance = new PopupCloseables();
            }
            return instance;
        }

        public void clear() {
            if (closeables != null) {
                closeables.clear();
            }
        }

        public List<IPopupCloseable> getCloseables() {
            if (closeables != null) {
                return Collections.unmodifiableList(closeables);
            }

            return Collections.emptyList();
        }

        public void addCloseable(IPopupCloseable closeable) {
            if (closeables == null) {
                closeables = new ArrayList<IPopupCloseable>();
            }

            if (closeable != null && !closeables.contains(closeable)) {
                closeables.add(closeable);
            }
        }

        public void removeCloseable(IPopupCloseable closeable) {
            if (closeables != null) {
                if (closeables.remove(closeable)) {
                    if (closeables.isEmpty()) {
                        closeables = null;
                    }
                }
            }
        }

        public boolean shouldIgnoreNextClose(IPopupCloseable closeable) {
            return ignoreOnNextClick != null
                    && ignoreOnNextClick.contains(closeable);
        }

        public void setIgnoreNextClose(IPopupCloseable closeable, boolean ignore) {
            if (closeable != null) {
                if (ignore) {
                    if (ignoreOnNextClick == null) {
                        ignoreOnNextClick = new ArrayList<IPopupCloseable>();
                    }

                    if (!ignoreOnNextClick.contains(closeable)) {
                        ignoreOnNextClick.add(closeable);
                    }
                } else {
                    if (ignoreOnNextClick != null
                            && ignoreOnNextClick.remove(closeable)) {
                        if (ignoreOnNextClick.isEmpty()) {
                            ignoreOnNextClick = null;
                        }
                    }
                }
            }
        }

        public void closeAll(AjaxRequestTarget target) {
            List<IPopupCloseable> popups = PopupCloseables.getInstance()
                    .getCloseables();
            if (popups != null) {
                for (IPopupCloseable popup : popups) {
                    if (!popup.isClosed()) {
                        popup.close(target);
                    }
                }
            }
        }

        public static interface IPopupCloseable {
            String getMarkupId();

            boolean isClosed();

            void close(AjaxRequestTarget target);
        }
    }

    /*
     * private class PopupCloseBehavior extends AbstractDefaultAjaxBehavior {
     * 
     * @Override public void renderHead(IHeaderResponse response) {
     * super.renderHead(response);
     * 
     * response.renderJavascriptReference(new ResourceReference(TCPanel.class,
     * "tc-utils.js")); }
     * 
     * @Override protected void onComponentTag(ComponentTag tag) {
     * super.onComponentTag(tag);
     * 
     * tag.put("onclick", createOnClickJavascript()); }
     * 
     * @Override protected void respond(AjaxRequestTarget target) {
     * List<IPopupCloseable> closeables =
     * PopupCloseables.getInstance().getCloseables();
     * 
     * for (IPopupCloseable closeable: closeables) { if (!closeable.isClosed()
     * && !PopupCloseables.getInstance().shouldIgnoreNextClose(closeable)) {
     * Boolean clickedInParent =
     * Boolean.parseBoolean(RequestCycle.get().getRequest
     * ().getParameter(closeable.getMarkupId())); if (!clickedInParent) {
     * closeable.close(target); } }
     * 
     * PopupCloseables.getInstance().setIgnoreNextClose(closeable, false); } }
     * 
     * private String createOnClickJavascript() { StringBuilder sb = new
     * StringBuilder();
     * 
     * sb.append("{"); sb.append("wicketAjaxGet('");
     * sb.append(getCallbackUrl());
     * 
     * List<IPopupCloseable> closeables =
     * PopupCloseables.getInstance().getCloseables();
     * 
     * if (closeables!=null && !closeables.isEmpty()) { int i=0; for
     * (IPopupCloseable closeable : closeables) { if (i>0) { sb.append("+'"); }
     * 
     * sb.append("&"); sb.append(closeables.get(i).getMarkupId());
     * sb.append("='+");
     * sb.append("checkLastOnClickInParent(event || window.event,'");
     * sb.append(closeables.get(i).getMarkupId()); sb.append("')");
     * 
     * i++; } } else { sb.append("'"); }
     * 
     * sb.append(");"); sb.append("return false;"); sb.append("}");
     * 
     * return sb.toString(); } }
     */
}
