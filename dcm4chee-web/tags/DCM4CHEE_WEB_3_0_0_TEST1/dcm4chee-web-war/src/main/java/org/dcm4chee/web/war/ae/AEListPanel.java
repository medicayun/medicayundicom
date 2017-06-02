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

package org.dcm4chee.web.war.ae;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.OddEvenListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 5, 2009
 */
public class AEListPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private AEMgtPanel page;
    private DicomEchoWindow mw;
    public AEListPanel(String id, AEMgtPanel p) {
        super(id);
        
        add(CSSPackageResource.getHeaderContribution(AEListPanel.class, "ae-style.css"));
        
        setOutputMarkupId(true);
        mw = new DicomEchoWindow("echoPanel", true);
        mw.setWindowClosedCallback(new WindowClosedCallback(){

            private static final long serialVersionUID = 1L;

            public void onClose(AjaxRequestTarget target) {
                AEMgtDelegate.getInstance().updateAEList();
                target.addComponent(AEListPanel.this);
            }});
        add(mw);
        final ConfirmationWindow<AE> confirm = new ConfirmationWindow<AE>("confirm"){

            private static final long serialVersionUID = 1L;
            
            @Override
            public void onConfirmation(AjaxRequestTarget target, AE ae) {
                AEMgtDelegate.getInstance().removeAET(ae);
                target.addComponent(AEListPanel.this);
            }
        };
        add(confirm);
        add( new Label("titleHdrLabel", new ResourceModel("aet.titleHdr")));
        add( new Label("hostHdrLabel", new ResourceModel("aet.hostHdr")));
        add( new Label("portHdrLabel", new ResourceModel("aet.portHdr")));
        add( new Label("cipherHdrLabel", new ResourceModel("aet.cipherHdr")));
        add( new Label("descriptionHdrLabel", new ResourceModel("aet.descriptionHdr")));
        add( new Label("issuerHdrLabel", new ResourceModel("aet.issuerHdr")));
        add( new Label("fsgrpHdrLabel", new ResourceModel("aet.fsgrpHdr")));
        add( new Label("wadoHdrLabel", new ResourceModel("aet.wadoHdr")));
        add( new Label("userHdrLabel", new ResourceModel("aet.userHdr")));
        add( new Label("stationHdrLabel", new ResourceModel("aet.stationHdr")));
        add( new Label("institutionHdrLabel", new ResourceModel("aet.institutionHdr")));
        add( new Label("departmentHdrLabel", new ResourceModel("aet.departmentHdr")));
        add( new Label("installedHdrLabel", new ResourceModel("aet.installedHdr")));
        page = p;
        add(new PropertyListView<AE>("list", AEMgtDelegate.getInstance().getAEList() ) {

            private static final long serialVersionUID = 1L;

            @Override
            protected ListItem<AE> newItem(final int index) {
                return new OddEvenListItem<AE>(index, getListItemModel(getModel(), index));
            }

            @Override
            protected void populateItem(final ListItem<AE> item) {
                item.add(new Label("title"));
                item.add(new Label("hostName"));
                item.add(new Label("port"));
                item.add(new ListView<Object>("cipherSuites", item.getModelObject().getCipherSuites()) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void populateItem(final ListItem<Object> item1) {
                        item1.add(new Label("ciphersuite", item1.getModel()));
                    }
                });
                item.add(new Label("description"));
                item.add(new Label("issuerOfPatientID"));
                item.add(new Label("fileSystemGroupID"));
                item.add(new Label("wadoURL"));
                item.add(new Label("userID"));
                item.add(new Label("stationName"));
                item.add(new Label("institution"));
                item.add(new Label("department"));
                item.add(new Label("installed"));
                Link<?> editAET = new Link<Object>("editAET") {
                    
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick() {
                        page.setEditPage(item.getModelObject());
                    }
                };
                editAET.add(new Image("img-editAET",ImageManager.IMAGE_AE_EDIT)
                .add(new ImageSizeBehaviour()));
                editAET.add(new TooltipBehaviour("aet."));
                item.add(editAET);
                MetaDataRoleAuthorizationStrategy.authorize(editAET, RENDER, "WebAdmin");
                AjaxLink<?> removeAET = new AjaxLink<Object>("removeAET") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        AE ae = item.getModelObject();
                        confirm.confirm(target, new StringResourceModel("aet.confirmDelete",AEListPanel.this, null,new Object[]{ae}), ae);
                    }
                };
                removeAET.add(new Image("img-removeAET", ImageManager.IMAGE_COMMON_REMOVE)
                .add(new ImageSizeBehaviour()));
                removeAET.add(new TooltipBehaviour("aet."));
                item.add(removeAET);
                MetaDataRoleAuthorizationStrategy.authorize(removeAET, RENDER, "WebAdmin");
                item.add(new AjaxLink<Object>("echo") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        mw.show(target, item.getModelObject());
                    }
                }.add(new Image("img-echoAET", ImageManager.IMAGE_AE_ECHO)
                .add(new ImageSizeBehaviour()))
                .add(new TooltipBehaviour("aet."))
                );
            }
            
        });
        Link<?> newAET = new Link<Object>("newAET") {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                page.setEditPage(new AE());
            }
        };
        newAET.add(new Image("newAETImg",ImageManager.IMAGE_COMMON_ADD)
            .add(new ImageSizeBehaviour("vertical-align: middle;"))
        );
        newAET.add(new TooltipBehaviour("aet."));
        newAET.add(new Label("newAETText", new ResourceModel("aet.newAET.text"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))
        );
        add(newAET);
        MetaDataRoleAuthorizationStrategy.authorize(newAET, RENDER, "WebAdmin");
    }
}
