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
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
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

package org.dcm4chee.usr.ui.usermanagement.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.model.Role;
import org.dcm4chee.usr.model.Group;
import org.dcm4chee.usr.ui.config.delegate.UsrCfgDelegate;
import org.dcm4chee.usr.ui.util.CSSUtils;
import org.dcm4chee.usr.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketApplication;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.ModalWindowLink;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;
import org.dcm4chee.web.common.secure.SecureSession;
import org.dcm4chee.web.common.secure.SecurityBehavior;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since Jul. 01, 2010
 */
public class RoleListPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    
    UserAccess userAccess;
    
    private ListModel<Role> allRoles;
    private Map<String,Group> allGroups;
    private List<Group> roleGroups;
    
    private ConfirmationWindow<Role> confirmationWindow;
    private ModalWindow roleWindow;
    private ModalWindow webroleWindow;
    private Map<String,int[]> windowsizeMap = new LinkedHashMap<String, int[]>();
    
    public RoleListPanel(String id) {
        super(id);

        if (RoleListPanel.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(RoleListPanel.BaseCSS));

        windowsizeMap.put("editRole", UsrCfgDelegate.getInstance().getWindowSize("editRole"));
        windowsizeMap.put("webPermissions", UsrCfgDelegate.getInstance().getWindowSize("webPermissions"));
        
        userAccess = (UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME);        
        setOutputMarkupId(true);

        this.allRoles = new ListModel<Role>(getAllRoles());
        this.allGroups = getAllGroups();
        this.roleGroups = userAccess.getAllGroups();

        add(this.confirmationWindow = new ConfirmationWindow<Role>("confirmation-window") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onConfirmation(AjaxRequestTarget target, Role role) {
                userAccess.removeRole(role);
                target.addComponent(RoleListPanel.this);
                allRoles.setObject(getAllRoles());
            }
        });

        add(roleWindow = new ModalWindow("role-window"));
        add(webroleWindow = new ModalWindow("webrole-window"));

        int[] winSize = windowsizeMap.get("editRole");
        add(new ModalWindowLink("toggle-role-form-link", roleWindow, winSize[0], winSize[1]) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                roleWindow
                .setPageCreator(new ModalWindow.PageCreator() {
                    
                    private static final long serialVersionUID = 1L;
                      
                    @Override
                    public Page createPage() {
                        return new CreateOrEditRolePage(roleWindow, allRoles, null, allGroups);
                    }
                });
                super.onClick(target);
            }
        }
      .add(new Image("toggle-role-form-image", ImageManager.IMAGE_USER_ROLE_ADD)
      .add(new ImageSizeBehaviour("vertical-align: middle;")))
      .add(new Label("rolelist.add-role-form.title", new ResourceModel("rolelist.add-role-form.title")))
      .add(new TooltipBehaviour("rolelist."))
      .add(new SecurityBehavior(getModuleName() + ":newRoleLink"))
      );
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        this.allRoles.setObject(getAllRoles());
        this.allGroups = getAllGroups();
        this.roleGroups = userAccess.getAllGroups();
        
        RepeatingView roleRows = new RepeatingView("role-rows");
        addOrReplace(roleRows);
        
        RepeatingView groupHeaderCells = new RepeatingView("group-header-cells");
        addOrReplace(groupHeaderCells);

        for (Group group : this.roleGroups) 
            groupHeaderCells
                .add(new WebMarkupContainer(groupHeaderCells.newChildId())
                .add(new Label("groupname", group.getGroupname())));
        
        for (int i = 0; i < this.allRoles.getObject().size(); i++) {
            final Role role = this.allRoles.getObject().get(i);

            WebMarkupContainer rowParent;
            roleRows.add((rowParent = new WebMarkupContainer(roleRows.newChildId()))
                    .add(new Label("rolename", role.getRolename())
                    .add(new AttributeModifier("title", true, new Model<String>(role.getDescription()))))
            );
            if (role.isSuperuser())
                rowParent.add(new SecurityBehavior(getModuleName() + ":superuserRoleRow"));
            if (role.getGroupUuid() != null && 
                    !role.getGroupUuid().equals("") && 
                    allGroups.get(role.getGroupUuid()) != null) {
                Group group = allGroups.get(role.getGroupUuid());
                rowParent.add(new AttributeModifier("style", true, new Model<String>("background-color: " + group.getColor())));
            }

            int[] winSize = windowsizeMap.get("editRole");
            rowParent.add((new ModalWindowLink("edit-role-link", roleWindow, winSize[0], winSize[1]) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    roleWindow
                    .setPageCreator(new ModalWindow.PageCreator() {
                        
                        private static final long serialVersionUID = 1L;
                          
                        @Override
                        public Page createPage() {
                            return new CreateOrEditRolePage(roleWindow, allRoles, role, allGroups);
                        }
                    });
                    super.onClick(target);
                }
            })
            .add(new Image("rolelist.edit.image", ImageManager.IMAGE_COMMON_DICOM_EDIT)
            .add(new TooltipBehaviour("rolelist.", "edit-role-link", new Model<String>(role.getRolename())))
            .add(new ImageSizeBehaviour("vertical-align: middle;")))
            .add(new SecurityBehavior(getModuleName() + ":editRoleLink"))
            );

            rowParent.add((new AjaxFallbackLink<Object>("remove-role-link") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    confirmationWindow.confirm(target, new Model<String>(new ResourceModel("rolelist.remove-role-link.confirmation").wrapOnAssignment(this.getParent()).getObject()), role);
                }
            }
            .add(new Image("rolelist.delete.image", ImageManager.IMAGE_COMMON_REMOVE)
            .add(new TooltipBehaviour("rolelist.", "remove-role-link", new Model<String>(role.getRolename()))))
            .add(new ImageSizeBehaviour()))
            .setVisible(!userAccess.getUserRoleName().equals(role.getRolename())
                    && !userAccess.getAdminRoleName().equals(role.getRolename()))
            .add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(i))))
            .add(new SecurityBehavior(getModuleName() + ":removeRoleLink")));

            winSize = windowsizeMap.get("webPermissions");
            rowParent.add((new ModalWindowLink("webrole-link", webroleWindow, winSize[0], winSize[1]) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    webroleWindow
                        .setPageCreator(new ModalWindow.PageCreator() {
                      
                            private static final long serialVersionUID = 1L;
                        
                                @Override
                                public Page createPage() {
                                    return new WebPermissionsPage(
                                            webroleWindow, 
                                            role
                                    );
                                }
                        });
                    super.onClick(target);
                }
            }).add(new Image("rolelist.webrole.image", ImageManager.IMAGE_USER_WEB_PERMISSIONS)
                .add(new TooltipBehaviour("rolelist.", "webrole-link", new Model<String>(role.getRolename()))))
                .setVisible(role.isWebRole())
                .add(new SecurityBehavior(getModuleName() + ":webroleLink"))
            );
                    
            rowParent.add((new AjaxCheckBox("dicomrole-checkbox", new Model<Boolean>(role.isDicomRole())) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                }}.setEnabled(false))
            );

            RepeatingView groupContentCells = new RepeatingView("group-content-cells");
            rowParent.add(groupContentCells);
            for (Group group : this.roleGroups) {
                if (group.getGroupname().equalsIgnoreCase("Web")
                        ||group.getGroupname().equalsIgnoreCase("Dicom"))
                    continue;
                CheckBox groupCheckbox = new CheckBox("group-checkbox");
                groupCheckbox.setEnabled(false);
                groupCheckbox.setModel(new Model<Boolean>(role.getRoleTypes().contains(group.getGroupname())));
                groupContentCells
                    .add(new WebMarkupContainer(groupContentCells.newChildId())
                    .add(groupCheckbox));
            }
        }
    }

    private ArrayList<Role> getAllRoles() {
        ArrayList<Role> allRoles = new ArrayList<Role>(2);
        allRoles.addAll(userAccess.getAllRoles());
        return allRoles;
    }
    
    private Map<String, Group> getAllGroups() {
        Map<String, Group> groups = new HashMap<String,Group>();
        for (Group group : userAccess.getAllGroups())
            groups.put(group.getUuid(), group);
        return groups;
    }
    
    public static String getModuleName() {
        return "rolelist";
    }
}
