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
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
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

    private static final ResourceReference LAYOUT_CSS = new CompressedResourceReference(
            TCPanel.class, "css/tc-layout.css");
    
    private static final ResourceReference BASE_CSS = new CompressedResourceReference(
            TCPanel.class, "css/tc-style.css");
    
    private static final ResourceReference THEME_CSS = new CompressedResourceReference(
            TCPanel.class, "css/theme/theme.css");
    
    public static final String ModuleName = "tc";

    private static final MaskingAjaxCallBehavior macb = new MaskingAjaxCallBehavior();

    private TCPopupManager popupManager;
    
    private TCSearchPanel searchPanel;
    private TCResultPanel listPanel;
    
    public TCPanel(final String id) {
        super(id);

        if (TCPanel.LAYOUT_CSS != null) {
            add(CSSPackageResource.getHeaderContribution(TCPanel.LAYOUT_CSS));
        }
        
        if (TCPanel.BASE_CSS != null) {
            add(CSSPackageResource.getHeaderContribution(TCPanel.BASE_CSS));
        }
        
        if (TCPanel.THEME_CSS != null) {
            add(CSSPackageResource.getHeaderContribution(TCPanel.THEME_CSS));
        }

        final ModalWindow viewDialog = new ModalWindow("tc-view-dialog") {
            private static final long serialVersionUID = 1L;

            @Override
            public void show(AjaxRequestTarget target)
            {
                if (isShown()==false)
                {
                    Component content = getContent();
                    
                    content.setVisible(true);
                    target.addComponent(this);
                    target.appendJavascript(getWindowOpenJavascript().replace(
                            "Wicket.Window.create", "createTCViewDialog"));
                    target.appendJavascript("updateTCViewDialog();");
                    
                    if (content instanceof TCViewPanel)
                    {
                        //disable tabs
                        TCViewPanel viewPanel = (TCViewPanel) content;
                        if (!viewPanel.isEditable())
                        {
                            List<Integer> disabledIndices = viewPanel.getDisabledTabIndices();
                            if (disabledIndices!=null && !disabledIndices.isEmpty())
                            {
                                StringBuffer sbuf = new StringBuffer();
                                sbuf.append("[").append(disabledIndices.get(0));
                                for (int i=1; i<disabledIndices.size(); i++)
                                {
                                    sbuf.append(",").append(disabledIndices.get(i));
                                }
                                sbuf.append("]");

                                target.appendJavascript("disableTCViewTabs(" + sbuf.toString() + ");");
                            }
                        }
                        
                        //hide tabs
                        List<Integer> hiddenIndices = viewPanel.getHiddenTabIndices();
                        if (hiddenIndices!=null)
                        {
                            for (Integer index : hiddenIndices)
                            {
                                target.appendJavascript("hideTCViewTab(" + index + ");");
                            }
                        }
                    }
                    
                    try
                    {
                        Field shown = ModalWindow.class.getDeclaredField("shown");
                        shown.setAccessible(true);
                        shown.set(this, true);
                    }
                    catch (Exception e)
                    {
                        log.warn(null, e);
                    }
                }
            }
        };
        final TCDetailsPanel detailsPanel = new TCDetailsPanel("details-panel");
        final TCListModel listModel = new TCListModel();
        listPanel = new TCResultPanel("result-panel",
                listModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component[] selectionChanged(TCModel tc) {
                try {
                    if (tc == null) {
                        detailsPanel.clearTCObject(false);
                    } else {
                        detailsPanel.setTCObject(TCObject.create(tc));
                    }
                } catch (Exception e) {
                    log.error("Parsing TC object failed!", e);
                    detailsPanel.clearTCObject(true);
                }

                return new Component[] {detailsPanel};
            }
            
            @Override
            protected void openTC(final TCModel tc, boolean edit, AjaxRequestTarget target)
            {
                try
                {
                    final IModel<TCEditableObject> model = new Model<TCEditableObject>(TCEditableObject.create(tc));
                    final TCViewPanel viewPanel = !edit ?
                            new TCViewPanel(viewDialog.getContentId(), model, tc) :
                            new TCViewEditablePanel(viewDialog.getContentId(), model, tc) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onClose(AjaxRequestTarget target, boolean save)
				{
				    viewDialog.close(target);

				    if (save)
				    {
				        try
				        {
				            TCStoreDelegate storeDelegate = TCStoreDelegate.getInstance();

				            //store new SR
				            DicomObject dataset = model.getObject().toDataset();
				            if (storeDelegate.storeImmediately(dataset))
				            {
				                //delete old SR
				                storeDelegate.store(model.getObject().toRejectionNoteDataset());

				                //trigger new search and select new SR
				                listModel.addToFilter(tc);
				                searchPanel.redoSearch(target, dataset.getString(Tag.SOPInstanceUID));
				            }
				        }
				        catch (Exception e)
				        {
				            log.error("Saving teaching-file failed!", e);
				        }
				    }
				}
                            };

                    viewDialog.setContent(viewPanel);
                    
                    openTCDialog(viewDialog, null, target);
                }
                catch (Exception e)
                {
                    log.error("Showing teaching-file dialog failed!", e);
                }
            }
        };
        viewDialog.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
            private static final long serialVersionUID = 1L;
            
            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                target.addComponent(listPanel);
                return true;
            }
        });

        add(macb);
        add((searchPanel=new TCSearchPanel("search-panel") {

            private static final long serialVersionUID = 1L;

            @Override
            public Component[] doSearch(TCQueryFilter filter) {
                detailsPanel.clearTCObject(false);
                listPanel.clearSelected();
                listModel.update(filter);

                return new Component[] { detailsPanel, listPanel };
            }
            
            @Override
            public void redoSearch(AjaxRequestTarget target, String iuid)
            {
                Component[] toUpdate = doSearch((TCQueryFilter)getDefaultModel().getObject());

                TCModel tc = iuid!=null ? listModel.findByIUID(iuid):null;
                if (tc!=null)
                {
                    listPanel.selectTC(tc);
                }
                
                if (toUpdate != null && target != null) {
                    for (Component c : toUpdate) {
                        target.addComponent(c);
                    }
                }
            }
        }));

        add(listPanel);
        add(detailsPanel);
        add(viewDialog);
        
        add((popupManager=new TCPopupManager()).getGlobalHideOnOutsideClickHandler());
    }
    
    public void openTCDialog(ModalWindow dlg, String title, AjaxRequestTarget target)
    {
        if (target==null)
        {
            target = AjaxRequestTarget.get();
        }
        
        if (target!=null)
        {
            dlg.setTitle(title==null?"":title);
            dlg.setInitialWidth(900);
            dlg.setInitialHeight(780);
            dlg.setResizable(true);
            dlg.show(target);
        }
    }
    
    public TCPopupManager getPopupManager()
    {
        return popupManager;
    }

    public static String getModuleName() {
        return ModuleName;
    }

    public static MaskingAjaxCallBehavior getMaskingBehaviour() {
        return macb;
    }
}
