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

package org.dcm4chee.web.war.folder.studypermissions;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.security.components.SecureWebPage;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.model.Role;
import org.dcm4chee.usr.ui.validator.RoleValidator;
import org.dcm4chee.usr.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.dao.folder.StudyPermissionsLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 06.12.2010
 */
public class CreateDicomRolePage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    
    private static Logger log = LoggerFactory.getLogger(CreateDicomRolePage.class);

    public CreateDicomRolePage(final ModalWindow window, ListModel<Role> allDicomRolenames) {
        super();
        
        if (CreateDicomRolePage.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(CreateDicomRolePage.BaseCSS));

        add(new WebMarkupContainer("add-dicom-role-title"));
        add(new CreateDicomRoleForm("add-dicom-role-form", window, allDicomRolenames));
    }

    private final class CreateDicomRoleForm extends BaseForm {
        
        private static final long serialVersionUID = 1L;

        private Model<String> rolename = new Model<String>();       
        private TextField<String> rolenameTextField = new TextField<String>("studypermission.add-dicom-role-form.rolename.input", rolename);
        
        public CreateDicomRoleForm(String id, final ModalWindow window, final ListModel<Role> allDicomRolenames) {
            super(id);

            ListModel<Role> r = new ListModel<Role>();
            r.setObject(((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME)).getAllRoles());
            add(rolenameTextField
                    .setRequired(true)
                    .add(new DicomRoleValidator(r))
            );
            
            add(new AjaxFallbackButton("add-dicom-role-submit", CreateDicomRoleForm.this) {
                
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    try {
                        StudyPermissionsLocal dao = (StudyPermissionsLocal) JNDIUtils.lookup(StudyPermissionsLocal.JNDI_NAME);
                        dao.addDicomRole(rolename.getObject());
                        allDicomRolenames.setObject(dao.getAllDicomRoles());
                        target.addComponent(form);
                        window.close(target);
                    } catch (final Exception e) {
                        log.error(this.getClass().toString() + ": " + "onSubmit: " + e.getMessage());
                        log.debug("Exception: ", e);
                    }
                }
                
                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    target.addComponent(form);
                }
            });
        }
    };
    
    private class DicomRoleValidator extends AbstractValidator<String> {

        private static final long serialVersionUID = 1L;

        private final Logger log = LoggerFactory.getLogger(RoleValidator.class);
        
        private ListModel<Role> allDicomRoles;
        
        public DicomRoleValidator(ListModel<Role> allDicomRoles) {
            this.allDicomRoles = allDicomRoles;
        }

        @Override
        protected void onValidate(IValidatable<String> validatable) {
            try {
                for (Role aDicomRole : this.allDicomRoles.getObject()) {
                    if (aDicomRole.getRolename().equals(validatable.getValue())) 
                        error(validatable, "studypermission.add-dicom-role-form.rolename.input.RoleValidator");
                }
            } catch (Exception e) {
                log.error(this.getClass().toString() + ": " + "onValidate: " + e.getMessage());
                log.debug("Exception: ", e);
            }
        }
    }
}
