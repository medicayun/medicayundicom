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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree.LinkType;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.web.war.common.AutoSelectInputTextBehaviour;
import org.dcm4chee.web.war.tc.TCPopupManager.AbstractTCPopup;
import org.dcm4chee.web.war.tc.TCPopupManager.TCPopupPosition;
import org.dcm4chee.web.war.tc.TCPopupManager.TCPopupPosition.PopupAlign;
import org.dcm4chee.web.war.tc.TCUtilities;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since June 20, 2011
 */
public class TCKeywordTreeInput extends AbstractTCKeywordInput {

    private static final long serialVersionUID = 1L;

    private AutoCompleteTextField<TCKeyword> text;
    
    public TCKeywordTreeInput(final String id, final TCKeywordNode root) {
        this(id, null, root);
    }

    public TCKeywordTreeInput(final String id, TCKeyword selectedKeyword,
            final TCKeywordNode root) {
        super(id);

        setDefaultModel(new Model<TCKeyword>(selectedKeyword) {
            @Override
            public void setObject(TCKeyword keyword)
            {
                if (!TCUtilities.equals(getObject(),keyword))
                {
                    super.setObject(keyword);
                    
                    fireValueChanged();
                }
            }
        });
        
        text = new AutoCompleteTextField<TCKeyword>(
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
        text.add(new AutoSelectInputTextBehaviour());
        text.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            public void onUpdate(AjaxRequestTarget target)
            {
                text.updateModel();
            }
        });
        
        final KeywordTreePopup popup = new KeywordTreePopup(text);
        final Button chooserBtn = new Button("chooser-button", new Model<String>("..."));
        chooserBtn.add(new Image("chooser-button-img", ImageManager.IMAGE_TC_ARROW_DOWN)
        .setOutputMarkupId(true));

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
                popup.setSelectedNode((TCKeywordNode)node);
                popup.hide(target);
            }
        };

        tree.setOutputMarkupId(true);
        tree.setRootLess(true);
        tree.setLinkType(LinkType.AJAX);
        tree.getTreeState().setAllowSelectMultiple(false);

        popup.setTree(root, tree);
        popup.add(tree);
        popup.installPopupTrigger(chooserBtn, new TCPopupPosition(
                chooserBtn.getMarkupId(),
                popup.getMarkupId(), 
                PopupAlign.BottomLeft, PopupAlign.TopLeft));
        
        add(text);
        add(chooserBtn);
        add(popup);
    }

    @Override
    public TCKeyword getKeyword() {
        return getModel().getObject();
    }

    @Override
    public void resetKeyword() {
        getModel().setObject(null);
    }
    
    @Override
    public boolean isExclusive()
    {
        return text.isEnabled();
    }
    
    @Override
    public void setExclusive(boolean exclusive)
    {
        text.setEnabled(!exclusive);
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

    private class KeywordTreePopup extends AbstractTCPopup 
    {
        private TCKeywordNode root;
        private TCKeywordNode lastSelectedNode;
        private Tree tree;
        private TextField<TCKeyword> text;

        public KeywordTreePopup(TextField<TCKeyword> text) 
        {
            super("tree-keyword-popup", true, true, true, true);
            this.text = text;
        }

        public void setTree(TCKeywordNode root, Tree tree) {
            this.root = root;
            this.tree = tree;
        }
        
        public void setSelectedNode(TCKeywordNode node)
        {
            this.lastSelectedNode = node;
        }
        
        @Override
        public void afterShowing(AjaxRequestTarget target)
        {
            TCKeyword keyword = TCKeywordTreeInput.this.getKeyword();
            TCKeywordNode toSelect = findNode(root, keyword);

            if (toSelect != null) 
            {
                lastSelectedNode = toSelect;
                tree.getTreeState().selectNode(toSelect, true);
                ensurePathExpanded(tree, toSelect);
            } 
            else 
            {
                Collection<Object> selectedNodes = tree.getTreeState().getSelectedNodes();
                if (selectedNodes != null && !selectedNodes.isEmpty()) 
                {
                    for (Object node : selectedNodes) 
                    {
                        tree.getTreeState().selectNode(node, false);
                        lastSelectedNode = (TCKeywordNode) node;
                    }
                }
            }

            target.addComponent(tree);
        }

        @Override
        public void beforeHiding(AjaxRequestTarget target) 
        {
            TCKeyword keyword = lastSelectedNode != null ? 
                    lastSelectedNode.getKeyword() : null;

            if (keyword != null && keyword.isAllKeywordsPlaceholder()) {
                keyword = null;
            }

            TCKeywordTreeInput.this.getModel().setObject(keyword);

            target.addComponent(text);
        }
    }
}
