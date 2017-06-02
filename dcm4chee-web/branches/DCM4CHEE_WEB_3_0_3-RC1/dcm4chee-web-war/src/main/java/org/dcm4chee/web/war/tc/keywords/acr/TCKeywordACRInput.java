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
package org.dcm4chee.web.war.tc.keywords.acr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree.LinkType;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.web.war.common.AutoSelectInputTextBehaviour;
import org.dcm4chee.web.war.tc.TCPopupManager.AbstractTCPopup;
import org.dcm4chee.web.war.tc.TCPopupManager.TCPopupPosition;
import org.dcm4chee.web.war.tc.TCPopupManager.TCPopupPosition.PopupAlign;
import org.dcm4chee.web.war.tc.TCUtilities;
import org.dcm4chee.web.war.tc.keywords.AbstractTCKeywordInput;
import org.dcm4chee.web.war.tc.keywords.TCKeyword;
import org.dcm4chee.web.war.tc.keywords.TCKeywordNode;

/**
 * @author Bernhard Ableitinger <bernhard.ableitinger@agfa.com>
 * @version $Revision$ $Date$
 * @since June 20, 2011
 */
public class TCKeywordACRInput extends AbstractTCKeywordInput {

    private static final long serialVersionUID = 1L;

    private TextField<TCKeyword> text;
    
    public TCKeywordACRInput(final String id) {
        this(id, null);
    }

    public TCKeywordACRInput(final String id, TCKeyword selectedKeyword) {
        super(id);

        setDefaultModel(new Model<TCKeyword>() {
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
        
        final ACRChooser chooser = new ACRChooser("keyword-acr");

        text = new TextField<TCKeyword>("text",
                selectedKeyword != null ? new Model<TCKeyword>(selectedKeyword)
                        : new Model<TCKeyword>(), TCKeyword.class) {
            @Override
            public IConverter getConverter(Class<?> type) {
                if (TCKeyword.class.isAssignableFrom(type)) {
                    return new IConverter() {
                        @Override
                        public String convertToString(Object o, Locale locale) {
                            if (o instanceof TCKeyword) {
                                return ((TCKeyword) o).getName();
                            }

                            return o != null ? o.toString() : null;
                        }

                        @Override
                        public TCKeyword convertToObject(String s, Locale locale) {
                            if (s != null) {
                                TCKeyword keyword = new TCKeyword(s, null,
                                        false);

                                TCKeyword curKeyword = TCKeywordACRInput.this
                                        .getModel().getObject();

                                if (curKeyword == null
                                        || !curKeyword.getName().equals(
                                                keyword.getName())) {
                                    TCKeywordACRInput.this.getModel()
                                            .setObject(keyword);
                                }

                                return keyword;
                            }

                            return null;
                        }
                    };
                }
                return super.getConverter(type);
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
        
        Button chooserBtn = new Button("chooser-button", new Model<String>("..."));
        chooserBtn.add(new Image("chooser-button-img", ImageManager.IMAGE_TC_ARROW_DOWN)
            .setOutputMarkupId(true));

        ACRPopup popup = new ACRPopup(chooser, text);
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
        text.getModel().setObject(null);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Model<TCKeyword> getModel() {
        return (Model) getDefaultModel();
    }
    
    public class ACRChooser extends Fragment {
        private TCKeyword anatomyKeyword;

        private TCKeyword pathologyKeyword;

        private String curPathologyTreeId;

        public ACRChooser(String id) {
            super(id, "acr-chooser", TCKeywordACRInput.this);

            setOutputMarkupId(true);

            ACRKeywordNode[] pathologyRoots = ACRCatalogue.getInstance()
                    .getPathologyRoots();
            final Map<ACRKeywordNode, Tree> pathologyTrees = new HashMap<ACRKeywordNode, Tree>(
                    pathologyRoots.length);
            for (int i = 0; i < pathologyRoots.length; i++) {
                final ACRKeywordNode pathologyRoot = pathologyRoots[i];
                final Tree pathologyTree = new Tree("pathology-tree-" + i,
                        new DefaultTreeModel(pathologyRoot)) {
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
                        boolean shouldSelect = node != null
                                && node instanceof ACRKeywordNode
                                && getTreeState().isNodeSelected(node);

                        if (shouldSelect) {
                            TCKeyword keyword = ((ACRKeywordNode) node)
                                    .getKeyword();

                            if (keyword != null
                                    && keyword.isAllKeywordsPlaceholder()) {
                                keyword = null;
                            }

                            pathologyKeyword = keyword;
                        } else {
                            pathologyKeyword = null;
                        }
                    }
                };
                pathologyTree.setOutputMarkupId(true);
                pathologyTree.setOutputMarkupPlaceholderTag(true);
                pathologyTree.setRootLess(true);
                pathologyTree.setLinkType(LinkType.AJAX);
                pathologyTree.getTreeState().setAllowSelectMultiple(false);
                pathologyTree.setVisible(false);

                pathologyTrees.put(pathologyRoots[i], pathologyTree);

                ACRKeywordNode node = pathologyRoots[i]
                        .findNode(pathologyKeyword);

                Tree tree = getCurrentPathologyTree();

                if (tree == null || node != null) {
                    setPathologyTreeVisible(pathologyTree);
                }

                add(pathologyTree);
            }

            final Tree anatomyTree = new Tree("anatomy-tree",
                    new DefaultTreeModel(ACRCatalogue.getInstance()
                            .getAnatomyRoot())) {
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
                    boolean shouldSelect = node != null
                            && node instanceof ACRKeywordNode
                            && getTreeState().isNodeSelected(node);

                    if (shouldSelect) {
                        TCKeyword keyword = ((ACRKeywordNode) node)
                                .getKeyword();

                        if (keyword != null
                                && keyword.isAllKeywordsPlaceholder()) {
                            keyword = null;
                        }

                        anatomyKeyword = keyword;
                        pathologyKeyword = null;

                        ACRKeywordNode pathologyRoot = ACRCatalogue
                                .getInstance().getPathologyRoot(
                                        (ACRKeywordNode) node);
                        if (pathologyRoot != null) {
                            Tree pathologyTree = pathologyTrees
                                    .get(pathologyRoot);
                            Tree curPathologyTree = getCurrentPathologyTree();
                            if (pathologyTree!=null && 
                                    pathologyTree != curPathologyTree) {
                                setPathologyTreeVisible(pathologyTree);
                                setNodeSelected(pathologyTree, null);

                                target.addComponent(curPathologyTree);
                                target.addComponent(pathologyTree);
                            }
                        }
                    } else {
                        anatomyKeyword = null;
                    }
                }
            };
            anatomyTree.setOutputMarkupId(true);
            anatomyTree.setLinkType(LinkType.AJAX);
            anatomyTree.setRootLess(true);
            anatomyTree.getTreeState().setAllowSelectMultiple(false);

            add(anatomyTree);
        }

        public TCKeyword getKeyword() {

            if (anatomyKeyword != null && pathologyKeyword != null
                    && anatomyKeyword.getCode() != null
                    && pathologyKeyword.getCode() != null) {
                return new ACRKeyword(anatomyKeyword, pathologyKeyword);
            } else if (pathologyKeyword != null) {
                return pathologyKeyword;
            } else if (anatomyKeyword != null) {
                return anatomyKeyword;
            }

            return null;
        }

        public Component[] setKeyword(TCKeyword keyword) {
            anatomyKeyword = null;
            pathologyKeyword = null;

            if (keyword != null) {
                if (keyword.getCode() == null) {
                    anatomyKeyword = keyword;
                } else if (ACRCatalogue.getInstance().isCompositeKeyword(
                        keyword)) {
                    if (keyword instanceof ACRKeyword) {
                        anatomyKeyword = ((ACRKeyword) keyword)
                                .getAnatomyKeyword();
                        pathologyKeyword = ((ACRKeyword) keyword)
                                .getPathologyKeyword();
                    }
                } else {
                    if (ACRCatalogue.getInstance().isAnatomyKeyword(keyword)) {
                        anatomyKeyword = keyword;
                    } else if (ACRCatalogue.getInstance().isPathologyKeyword(
                            keyword)) {
                        pathologyKeyword = keyword;
                    }
                }
            }

            Tree anatomyTree = (Tree) get("anatomy-tree");
            Tree pathologyTree = curPathologyTreeId != null ? (Tree) get(curPathologyTreeId)
                    : null;

            if (anatomyTree != null) {
                setNodeSelected(anatomyTree, ((ACRKeywordNode) anatomyTree
                        .getModelObject().getRoot()).findNode(anatomyKeyword));
            }

            if (pathologyTree != null) {
                setNodeSelected(pathologyTree, ((ACRKeywordNode) pathologyTree
                        .getModelObject().getRoot()).findNode(pathologyKeyword));
            }

            return new Component[] { get("anatomy-tree"),
                    get(curPathologyTreeId) };
        }

        private void setPathologyTreeVisible(Tree tree) {
            Tree curTree = getCurrentPathologyTree();

            if (curTree != null && curTree != tree) {
                curTree.setVisible(false);
            }

            curPathologyTreeId = tree.getId();

            tree.setVisible(true);
        }

        private Tree getCurrentPathologyTree() {
            return curPathologyTreeId != null ? (Tree) get(curPathologyTreeId)
                    : null;
        }

        private void ensurePathExpanded(Tree tree, ACRKeywordNode node) {
            if (node != null) {
                List<TCKeywordNode> path = node.getPath();
                if (path != null) {
                    for (TCKeywordNode n : path) {
                        tree.getTreeState().expandNode(n);
                    }
                }
            }
        }

        private void setNodeSelected(Tree tree, ACRKeywordNode node) {
            if (node != null) {
                tree.getTreeState().selectNode(node, true);

                ensurePathExpanded(tree, node);
            } else {
                Collection<Object> selectedNodes = tree.getTreeState() != null ? tree
                        .getTreeState().getSelectedNodes() : Collections
                        .emptyList();
                if (selectedNodes != null && !selectedNodes.isEmpty()) {
                    for (Object n : selectedNodes) {
                        tree.getTreeState().selectNode(n, false);
                    }
                }
            }
        }
    }

    private class ACRPopup extends AbstractTCPopup
    {
        private ACRChooser chooser;
        private TextField<TCKeyword> text;

        public ACRPopup(ACRChooser chooser, TextField<TCKeyword> text) {
            super("acr-keyword-popup", true, false, true, true);

            this.chooser = chooser;
            this.text = text;
            
            add(chooser);
        }
        
        @Override
        public void afterShowing(AjaxRequestTarget target)
        {
            chooser.setKeyword(
                    TCKeywordACRInput.this.getModel().getObject());
            
            target.addComponent(chooser);
        }

        @Override
        public void beforeHiding(AjaxRequestTarget target) {
            // apply keyword(s)
            TCKeyword keyword = chooser.getKeyword();
            if (keyword != null && keyword.isAllKeywordsPlaceholder()) {
                keyword = null;
            }

            TCKeywordACRInput.this.getModel().setObject(keyword);
            text.setModelObject(keyword);
            
            target.addComponent(text);
        }
    }

}
