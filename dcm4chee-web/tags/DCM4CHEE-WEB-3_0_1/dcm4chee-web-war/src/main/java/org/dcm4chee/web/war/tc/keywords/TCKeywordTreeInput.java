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
package org.dcm4chee.web.war.tc.keywords;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree.LinkType;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.dcm4chee.web.war.tc.TCPanel;
import org.dcm4chee.web.war.tc.TCPanel.PopupCloseables;
import org.dcm4chee.web.war.tc.TCPanel.PopupCloseables.IPopupCloseable;
import org.dcm4chee.web.war.tc.keywords.TCKeywordCatalogue.TCKeywordInput;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since June 20, 2011
 */
public class TCKeywordTreeInput extends Panel implements TCKeywordInput {

    private static final long serialVersionUID = 1L;

    public TCKeywordTreeInput(final String id, final TCKeywordNode root) {
        this(id, null, root);
    }

    public TCKeywordTreeInput(final String id, TCKeyword selectedKeyword,
            final TCKeywordNode root) {
        super(id, selectedKeyword != null ? new Model<TCKeyword>(
                selectedKeyword) : new Model<TCKeyword>());

        final AutoCompleteTextField<TCKeyword> text = new AutoCompleteTextField<TCKeyword>(
                "text", getModel(), TCKeyword.class, new AutoCompleteSettings()) {

            private static final long serialVersionUID = 1L;

            @Override
            public IConverter getConverter(Class<?> type) {
                if (TCKeyword.class.equals(type)) {
                    return new IConverter() {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public String convertToString(Object o, Locale locale) {
                            return o != null ? o.toString() : null;
                        }

                        @Override
                        public TCKeyword convertToObject(String s, Locale locale) {
                            if (s != null) {
                                TCKeywordNode node = findNode(root, s);
                                if (node != null) {
                                    return node.getKeyword();
                                } else if (s.length() > 0) {
                                    return new TCKeyword(s, null, false);
                                }
                            }

                            return null;
                        }
                    };
                }
                return super.getConverter(type);
            }

            @Override
            protected Iterator<TCKeyword> getChoices(String s) {
                List<TCKeyword> keywords = new ArrayList<TCKeyword>();
                findMatchingKeywords(root, s, keywords);
                return keywords.iterator();
            }
        };
        text.setOutputMarkupId(true);

        final TreePopupCloseable popup = new TreePopupCloseable(text);

        final Tree tree = new Tree("keyword-tree", new DefaultTreeModel(root)) {

            private static final long serialVersionUID = 1L;
            @Override
            protected void populateTreeItem(WebMarkupContainer item, int level) {
                super.populateTreeItem(item, level);
                
                //(WEB-429) workaround: disable browser-native drag and drop
                item.add(new AttributeModifier("onmousedown", true, new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject()
                    {
                        return "return false;";
                    }
                }));
            }
            @Override
            public void onNodeLinkClicked(AjaxRequestTarget target,
                    TreeNode node) {
                popup.close(target, node, true);
            }
        };

        tree.setOutputMarkupId(true);
        tree.setRootLess(true);
        tree.setLinkType(LinkType.AJAX);
        tree.getTreeState().setAllowSelectMultiple(false);

        PopupCloseables.getInstance().addCloseable(popup);

        popup.add(new AjaxFallbackLink<String>("popup-close-button") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                popup.close(target, null, false);
            }
        });

        popup.setTree(tree);
        popup.add(tree);

        add(text);
        add(new AjaxButton("chooser-button", new Model<String>("...")) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                TCKeyword keyword = TCKeywordTreeInput.this
                        .getSelectedKeyword();
                TCKeywordNode toSelect = findNode(root, keyword);

                if (toSelect != null) {
                    tree.getTreeState().selectNode(toSelect, true);

                    ensurePathExpanded(tree, toSelect);
                } else {
                    Collection<Object> selectedNodes = tree.getTreeState()
                            .getSelectedNodes();
                    if (selectedNodes != null && !selectedNodes.isEmpty()) {
                        for (Object node : selectedNodes) {
                            tree.getTreeState().selectNode(node, false);
                        }
                    }
                }

                PopupCloseables.getInstance().closeAll(target);
                // PopupCloseables.getInstance().setIgnoreNextClose(popup,
                // true);

                popup.setVisible(true);
                target.addComponent(popup);
                target.addComponent(tree);

                target.appendJavascript("setPositionRelativeToParent('"
                        + getMarkupId() + "','" + popup.getMarkupId() + "')");
            }
        });
        add(popup);
    }

    @Override
    public TCKeyword getSelectedKeyword() {
        return getModel().getObject();
    }

    @Override
    public void resetSelectedKeyword() {
        getModel().setObject(null);
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @SuppressWarnings({ "unchecked" })
    private Model<TCKeyword> getModel() {
        return (Model) getDefaultModel();
    }

    private void ensurePathExpanded(Tree tree, TCKeywordNode node) {
        if (node != null) {
            List<TCKeywordNode> path = node.getPath();
            if (path != null) {
                for (TCKeywordNode n : path) {
                    tree.getTreeState().expandNode(n);
                }
            }
        }
    }

    private TCKeywordNode findNode(TCKeywordNode root, String s) {
        if (root != null) {
            TCKeyword keyword1 = root.getKeyword();

            if (keyword1 != null && keyword1.toString().equals(s)) {
                return root;
            } else if (root.getChildCount() > 0) {
                for (TCKeywordNode child : root.getChildren()) {
                    TCKeywordNode node = findNode(child, s);
                    if (node != null) {
                        return node;
                    }
                }
            }
        }

        return null;
    }

    private void findMatchingKeywords(TCKeywordNode root, String s,
            List<TCKeyword> matching) {
        if (root != null) {
            TCKeyword keyword = root.getKeyword();

            if (keyword != null
                    && keyword.toString().toUpperCase()
                            .contains(s.toUpperCase())) {
                matching.add(keyword);
            }

            if (root.getChildCount() > 0) {
                for (TCKeywordNode child : root.getChildren()) {
                    findMatchingKeywords(child, s, matching);
                }
            }
        }
    }

    private TCKeywordNode findNode(TCKeywordNode root, TCKeyword keyword) {
        if (root != null) {
            TCKeyword keyword1 = root.getKeyword();

            if (keyword1 != null) {
                if ((keyword == null && keyword1.isAllKeywordsPlaceholder())
                        || (keyword != null && keyword1.equals(keyword))) {
                    return root;
                }
            }

            if (root.getChildCount() > 0) {
                for (TCKeywordNode child : root.getChildren()) {
                    TCKeywordNode node = findNode(child, keyword);
                    if (node != null) {
                        return node;
                    }
                }
            }
        }

        return null;
    }

    private class TreePopupCloseable extends WebMarkupContainer implements
            IPopupCloseable {
        private Tree tree;

        private TextField<TCKeyword> text;

        public TreePopupCloseable(TextField<TCKeyword> text) {
            super("popup-keyword-tree");

            this.text = text;

            setOutputMarkupId(true);
            setOutputMarkupPlaceholderTag(true);
            setVisible(false);
        }

        public void setTree(Tree tree) {
            this.tree = tree;
        }

        @Override
        public boolean isClosed() {
            return !isVisible();
        }

        @Override
        public void close(AjaxRequestTarget target) {
            Collection<Object> selNodes = tree != null ? tree.getTreeState()
                    .getSelectedNodes() : null;
            close(target, selNodes != null && !selNodes.isEmpty() ? selNodes
                    .iterator().next() : null, true);
        }

        public void close(AjaxRequestTarget target, Object node,
                boolean updateSelection) {
            if (updateSelection) {
                TCKeywordNode n = node instanceof TCKeywordNode ? (TCKeywordNode) node
                        : null;
                TCKeyword keyword = n != null ? n.getKeyword() : null;

                if (keyword != null && keyword.isAllKeywordsPlaceholder()) {
                    keyword = null;
                }

                TCKeywordTreeInput.this.getModel().setObject(keyword);
            }

            MarkupContainer parent = TCKeywordTreeInput.this.getParent();

            setVisible(false);
            target.addComponent(this);

            if (parent != null) {
                target.addComponent(parent);
            } else {
                target.addComponent(text);
            }
        }
    }
}
