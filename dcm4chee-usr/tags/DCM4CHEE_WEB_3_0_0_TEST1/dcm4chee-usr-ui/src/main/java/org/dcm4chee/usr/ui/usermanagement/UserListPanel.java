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

package org.dcm4chee.usr.ui.usermanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.icons.behaviours.ImageSizeBehaviour;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.entity.Role;
import org.dcm4chee.usr.entity.User;
import org.dcm4chee.usr.ui.util.CSSUtils;
import org.dcm4chee.usr.ui.util.JNDIUtils;
import org.dcm4chee.usr.ui.util.SecurityUtils;
import org.dcm4chee.usr.ui.validator.RoleValidator;
import org.dcm4chee.usr.ui.validator.UserValidator;
import org.dcm4chee.usr.ui.validator.ValidatorMessageLabel;
import org.dcm4chee.web.common.behaviours.TooltipBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.markup.modal.ConfirmationWindow;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 28.09.2009
 */
@AuthorizeInstantiation({Roles.ADMIN, "WebAdmin"})
public class UserListPanel extends Panel {

    private static final long serialVersionUID = 1L;
    
    private static final ResourceReference CSS = new CompressedResourceReference(UserListPanel.class, "usr-style.css");
    
    private ListModel<User> allUsers;
    private ListModel<String> allRolenames;

    private String userId;

    private ModalWindow changePasswordWindow;
    private ConfirmationWindow<User> confirmationWindow;

    public UserListPanel(String id, String userId) {
        super(id);

        setOutputMarkupId(true);
        
        this.userId = userId;
        add(this.changePasswordWindow = new ModalWindow("change-password-window"));
        
        if (UserListPanel.CSS != null)
            add(CSSPackageResource.getHeaderContribution(UserListPanel.CSS));
        
        this.allUsers = new ListModel<User>(getAllUsers());
        this.allRolenames = new ListModel<String>(getAllRolenames());

        add(this.confirmationWindow = new ConfirmationWindow<User>("confirmation-window") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onConfirmation(AjaxRequestTarget target, User userObject) {
                ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).deleteUser(userObject.getUserID());
                target.addComponent(UserListPanel.this);
                allUsers = new ListModel<User>(getAllUsers());
            }

        });
        
        addOrReplace(new ToggleFormLink("toggle-user-form-link", 
                new AddUserForm("add-user-form", this.allUsers),
                this, 
                "toggle-form-image-user", 
                Arrays.asList("userlist.toggle-user-form-link.title.tooltip", "userlist.toggle-user-form-link.close.tooltip"))
        );
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        
        RepeatingView roleHeaders = new RepeatingView("role-headers");
        for (String rolename : this.allRolenames.getObject())
            roleHeaders.add(new Label(roleHeaders.newChildId(), rolename));
        addOrReplace(roleHeaders);

        RepeatingView roleRows = new RepeatingView("role-rows");
        addOrReplace(roleRows);
        
        for (int i = 0; i < this.allUsers.getObject().size(); i++) {
            
            final User user = this.allUsers.getObject().get(i);
            
            WebMarkupContainer row_parent = new WebMarkupContainer(roleRows.newChildId());
            Label userIDLabel = new Label("userID", user.getUserID());
            row_parent.add(userIDLabel);
            roleRows.add(row_parent);

            RemoveUserLink removeUserLink = new RemoveUserLink("remove-user-link", user);
            removeUserLink.add(new TooltipBehaviour("userlist.", "remove-user-link", new PropertyModel<String>(user,"userID")));
            removeUserLink.add(new Image("img-delete", ImageManager.IMAGE_COMMON_REMOVE)
            .add(new ImageSizeBehaviour()));

            row_parent.add(removeUserLink);
            row_parent.add(new AttributeModifier("class", true, new Model<String>(CSSUtils.getRowClass(i))));
            removeUserLink.setVisible(!this.userId.equals(user.getUserID()));
            
            row_parent.add(
                    new ChangePasswordLink("change-password-link", this.changePasswordWindow, this.userId, user)
                    .add(new Image("img-change-password", ImageManager.IMAGE_USER_CHANGE_PASSWORD)
                    .add(new ImageSizeBehaviour()))
                    .add(new AttributeModifier("title", true, new Model<String>(new ResourceModel("userlist.change_password.tooltip").wrapOnAssignment(this).getObject())))
            );
            
            RepeatingView roleDividers = new RepeatingView("role-dividers");
            row_parent.add(roleDividers);
            roleRows.add(row_parent);

            for (final String rolename : this.allRolenames.getObject()) {
                HasRoleCheckBox roleCheckbox = new HasRoleCheckBox("role-checkbox", new HasRoleModel(user, rolename));
                if (this.userId.equals(user.getUserID())) {
                    AuthenticatedWebApplication awa = (AuthenticatedWebApplication) getApplication(); 
                    if (rolename.equals(awa.getInitParameter("userRoleName")) || rolename.equals(awa.getInitParameter("adminRoleName"))) {
                        for (Role role : user.getRoles()) {
                            if (role.getRole().equals(rolename))
                                roleCheckbox.setEnabled(false)
                                .add(new AttributeModifier("title", true, new ResourceModel("userlist.add-role-form.change_denied.tooltip").wrapOnAssignment(this)));
                        }
                    }                        
                }
                roleDividers.add(
                        new WebMarkupContainer(roleRows.newChildId())
                        .add(roleCheckbox)              
                );
            }
            row_parent.add(new ToggleFormLink("toggle-role-form-link", 
                    new AddRoleForm("add-role-form", user.getUserID(), this.allRolenames), 
                    row_parent, 
                    "toggle-form-image-role", 
                    Arrays.asList("userlist.add-role-form.title.tooltip", "userlist.add-role-form.close.tooltip"))
            );
        }
    }

    private final class AddUserForm extends BaseForm {
        
        private static final long serialVersionUID = 1L;
        
        private Model<String> newUsername = new Model<String>();
        private Model<String> password = new Model<String>();
        
        public AddUserForm(String id, ListModel<User> currentUserList) {
            super(id);

            newAjaxComponent(this)
                .setVisible(false);
            
            this.add(newAjaxComponent(
                    new Label("new-username-label", new ResourceModel("userlist.add-user-form.username.label"))));
            final TextField<String> usernameTf;
            this.add(newAjaxComponent(
                    (usernameTf = new TextField<String>("userlist.add-user-form.username.input", newUsername))
                    .setRequired(true)
                    .add(new UserValidator(currentUserList)))
            );
            
            this.add(newAjaxComponent(
                    new Label("password-label-1", new ResourceModel("userlist.add-user-form.password_1.label"))));
            PasswordTextField passwordTf1 = null;
            this.add(newAjaxComponent(
                    (passwordTf1 = new PasswordTextField("userlist.add-user-form.password_1.input", password))));
         
            this.add(newAjaxComponent(
                    new Label("password-label-2", new ResourceModel("userlist.add-user-form.password_2.label"))));
            PasswordTextField passwordTf2 = null;
            this.add(newAjaxComponent(
                    (passwordTf2 = new PasswordTextField("userlist.add-user-form.password_2.input", new Model<String>("")))));

            this.add(new EqualPasswordInputValidator(passwordTf1, passwordTf2));
        
            this.add(new Button("add-user-submit") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    User user = new User();
                    user.setUserID(newUsername.getObject());
                    user.setPassword(SecurityUtils.encodePassword(password.getObject()));
                    ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).createUser(user);
                    
                    this.getParent().setVisible(false);
                    usernameTf.setModelObject("");
                    allUsers = new ListModel<User>(getAllUsers());
                }
            });
        }
    };
    
    private final class AddRoleForm extends BaseForm {
        
        private static final long serialVersionUID = 1L;
        
        private Model<String> newRolename = new Model<String>();
        
        public AddRoleForm(String id, final String userId, ListModel<String> currentRolenameList) {
            super(id);
            
            newAjaxComponent(this)
                .setVisible(false);
            setTooltipBehaviour(new TooltipBehaviour(""));
            
            this.add(newAjaxComponent(
                    new Label("new-rolename-label", new ResourceModel("userlist.add-role-form.rolename.label"))));
            final TextField<String> rolenameTf;
            this.add(newAjaxComponent(
                    (rolenameTf = new TextField<String>("userlist.add-role-form.rolename.input", newRolename))
                    .setRequired(true)
                    .add(new RoleValidator(currentRolenameList)))
            );
            
            this.add(new Button("add-role-submit") {
                
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    Role role = new Role();
                    role.setUserID(userId);
                    role.setRole(newRolename.getObject());
                    ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).addRole(role);
                    
                    this.getParent().setVisible(false);
                    rolenameTf.setModelObject("");
                    allUsers = new ListModel<User>(getAllUsers());
                    allRolenames = new ListModel<String>(getAllRolenames());
                }
            });
        }
    };

    private final class ToggleFormImage extends Image {
        
        private static final long serialVersionUID = 1L;
        
        private Form<?> forForm;
        
        public ToggleFormImage(String id, Form<?> forForm) {
            super(id);
            this.forForm = forForm;
            this.add(new ImageSizeBehaviour());
        }
        
        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            tag.put("src", this.forForm.isVisible() ? 
                    this.getRequestCycle().urlFor(ImageManager.IMAGE_COMMON_COLLAPSE)
                    : this.getRequestCycle().urlFor(ImageManager.IMAGE_COMMON_EXPAND));
        }
    };
    
    private final class ToggleFormLink extends AjaxFallbackLink<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private Form<?> forForm;
        private ToggleFormImage toggleFormImage;
        private List<String> open_close_tooltip_texts; 
        
        public ToggleFormLink(String id, Form<?> forForm, MarkupContainer container, String toggleFormImageId, List<String> open_close_tooltip_texts) {
            super(id);

            newAjaxComponent(this);

            container.addOrReplace(this.forForm = forForm);
            this.open_close_tooltip_texts = open_close_tooltip_texts;
            this.add(newAjaxComponent(
                    (this.toggleFormImage = new ToggleFormImage(toggleFormImageId, forForm))));
            this.toggleFormImage.add(new ImageSizeBehaviour("vertical-align: middle;"));
            this.add(newAjaxComponent(new Label("userlist.add-user-form.title", new ResourceModel("userlist.add-user-form.title"))
            .add(new AttributeModifier("style", true, new Model<String>("vertical-align: middle")))));
        }

        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            if ((this.open_close_tooltip_texts != null) && (this.open_close_tooltip_texts.size() == 2))
                tag.put("title", new ResourceModel(this.open_close_tooltip_texts.get(forForm.isVisible() ? 1 : 0)).wrapOnAssignment(this).getObject());            
        }
        
        @Override
        public void onClick(AjaxRequestTarget target) {
            this.forForm.setVisible(!this.forForm.isVisible()); 
            target.addComponent(this);
            target.addComponent(this.toggleFormImage);
            target.addComponent(this.forForm);
        }
    };

    private final class HasRoleCheckBox extends AjaxCheckBox {
        
        private static final long serialVersionUID = 1L;
        
        private HasRoleCheckBox(String id, HasRoleModel model) {
            super(id, model);
        }
        
        @Override
        protected void onUpdate(AjaxRequestTarget target) {
            target.addComponent(this);
        }
        
        @Override
        protected void onComponentTag(ComponentTag tag) {
            super.onComponentTag(tag);
            tag.put("title", new ResourceModel(((HasRoleModel) this.getModel()).getObject().booleanValue() ? "userlist.has-role-checkbox.remove.tooltip" : "userlist.has-role-checkbox.add.tooltip").wrapOnAssignment(this).getObject());
        }
    }
    
    private final class HasRoleModel implements IModel<Boolean> {
        
        private static final long serialVersionUID = 1L;
        
        private User user;
        private String rolename;
        
        public HasRoleModel(User user, String rolename) {
            this.user = user;
            this.rolename = rolename;
        }
        
        @Override
        public Boolean getObject() {
            for (Role role : this.user.getRoles())
                if (role.getRole().equals(this.rolename)) return true;
            return false;
        }
        
        @Override
        public void setObject(Boolean hasRole) {
            if (hasRole) {
                Role role = new Role();
                role.setUserID(this.user.getUserID());
                role.setRole(this.rolename);
                ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).addRole(role);
                this.user.getRoles().add(role);
            } else {
                for (Role role : this.user.getRoles()) {
                    if (role.getRole().equals(this.rolename)) {
                        ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).removeRole(role);
                        this.user.getRoles().remove(role);
                        break;
                    }
                }
            }
        }
        
        @Override
        public void detach() {
        }
    }
    
    private final class RemoveUserLink extends AjaxFallbackLink<Object> {
        
        private static final long serialVersionUID = 1L;
        
        private User user;
        public RemoveUserLink(String id, User user) {
            super(id);
            this.user = user;
        }
        
        @Override
        public void onClick(AjaxRequestTarget target) {
            confirmationWindow.confirm(target, new Model<String>(new ResourceModel("userlist.remove-user-link.confirmation").wrapOnAssignment(this.getParent()).getObject()), this.user);
        }
    };

    private List<User> getAllUsers() {
        
        List<User> allUsers = new ArrayList<User>();
        allUsers.addAll(((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).findAll());
        return allUsers;
    }
    
    private ArrayList<String> getAllRolenames() {

        ArrayList<String> allRolenames = new ArrayList<String>(2);
        AuthenticatedWebApplication awa = (AuthenticatedWebApplication) getApplication();
        allRolenames.addAll(((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).getAllRolenames());
        if (!allRolenames.contains(awa.getInitParameter("userRoleName"))) allRolenames.add(awa.getInitParameter("userRoleName"));
        if (!allRolenames.contains(awa.getInitParameter("adminRoleName"))) allRolenames.add(awa.getInitParameter("adminRoleName"));
        return allRolenames;
    }
    
    private Component newAjaxComponent(Component component) {
        component.setOutputMarkupId(true);
        component.setOutputMarkupPlaceholderTag(true);
        return component;
    }
    
    public static String getModuleName() {
        return "userlist";
    }
}
