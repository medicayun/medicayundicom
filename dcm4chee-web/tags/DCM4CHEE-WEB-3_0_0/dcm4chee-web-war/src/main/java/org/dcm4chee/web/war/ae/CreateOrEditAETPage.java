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

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.security.components.SecureWebPage;
import org.apache.wicket.validation.validator.RangeValidator;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.archive.util.JNDIUtils;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.behaviours.FocusOnLoadBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.common.validators.UrlValidator1;
import org.dcm4chee.web.dao.fs.FileSystemHomeLocal;
import org.dcm4chee.web.war.ae.delegate.AEDelegate;
import org.dcm4chee.web.war.ae.model.CipherModel;
import org.dcm4chee.web.war.util.CyphersuiteUtils;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since June 4, 2009
 */

public class CreateOrEditAETPage extends SecureWebPage {
    
    private static final long serialVersionUID = 1L;

    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    
    private Model<String> resultMessage;
    
    public CreateOrEditAETPage(final ModalWindow window, final AE ae, final AEListPanel panel) {
        super();
        
        if (CreateOrEditAETPage.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(CreateOrEditAETPage.BaseCSS));

        add(new WebMarkupContainer("create-ae-title").setVisible(ae.getPk() == -1));
        add(new WebMarkupContainer("edit-ae-title").setVisible(ae.getPk() != -1));

        setOutputMarkupId(true);
        BaseForm form = new BaseForm("form");
        form.setResourceIdPrefix("ae.");
        add(form);
        CompoundPropertyModel<AE> model = new CompoundPropertyModel<AE>(ae);
        form.setDefaultModel(model);
        form.addLabeledTextField("title").add(new AETitleValidator())
            .setRequired(true).add(FocusOnLoadBehaviour.newFocusAndSelectBehaviour());
        form.addLabeledTextField("hostName").setRequired(true); 
        form.addLabeledNumberTextField("port").add(new RangeValidator<Integer>(1,65535));
        form.add(new Label("ciphers1.label", new StringResourceModel("ae.ciphers", CreateOrEditAETPage.this, null, new Object[]{1} ) ) );
        form.add(new DropDownChoice<String>("ciphersuite1", new CipherModel(ae, 0), CyphersuiteUtils.AVAILABLE_CIPHERSUITES));
        form.add(new Label("ciphers2.label", new StringResourceModel("ae.ciphers", CreateOrEditAETPage.this, null, new Object[]{2} ) ) );
        form.add(new DropDownChoice<String>("ciphersuite2", new CipherModel(ae, 1), CyphersuiteUtils.AVAILABLE_CIPHERSUITES));
        form.add(new Label("ciphers3.label", new StringResourceModel("ae.ciphers", CreateOrEditAETPage.this, null, new Object[]{3} ) ) );
        form.add(new DropDownChoice<String>("ciphersuite3", new CipherModel(ae, 2), CyphersuiteUtils.AVAILABLE_CIPHERSUITES));
        form.addLabeledTextField("description"); 
        form.addLabeledTextField("issuerOfPatientID"); 
        form.addLabeledTextField("issuerOfAccessionNumber"); 
        form.addLabeledDropDownChoice("fileSystemGroupID", null, 
                ((FileSystemHomeLocal) JNDIUtils.lookup(FileSystemHomeLocal.JNDI_NAME)).listGroupIDs()
                ).setNullValid(true);
        form.addLabeledTextField("wadoURL").add(new UrlValidator1()); //Wicket UrlValidator doesn't accept http://hostname:8080/web!
        form.addLabeledTextField("userID"); 
        form.add(new Label("password.label", new ResourceModel("ae.password") ) );
        form.add(new PasswordTextField("password").setRequired(false)); 
        form.addLabeledTextField("stationName"); 
        form.addLabeledTextField("institution"); 
        form.addLabeledTextField("department"); 
        form.add(new Label("installed.label", new ResourceModel("ae.installed") ) );
        form.add(new AjaxCheckBox("installed"){

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        form.add(new AjaxFallbackButton("submit", new ResourceModel("saveBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    AEDelegate.getInstance().updateOrCreate(ae);
                    panel.updateAETList();
                    window.close(target);
                } catch (Exception e) {
                    resultMessage.setObject(e.getLocalizedMessage());
                    if (target != null)
                        target.addComponent(CreateOrEditAETPage.this);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                if (target != null)
                    target.addComponent(form);
            }
        });
        form.add(new AjaxFallbackButton("cancel", new ResourceModel("cancelBtn"), form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                window.close(target);
            }}.setDefaultFormProcessing(false));

        final DicomEchoWindow mw = new DicomEchoWindow("echoPanel", true);
        mw.setWindowClosedCallback(new WindowClosedCallback() {

            private static final long serialVersionUID = 1L;

            public void onClose(AjaxRequestTarget target) {
                target.addComponent(CreateOrEditAETPage.this);
            }
        });
        add(mw);
        form.add(new AjaxButton("echo", new ResourceModel("ae.echoButton")) {

            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                mw.show(target, ae);
            }
            
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                mw.show(target, ae);
            }
        });
        add(new Label("result-message", (resultMessage = new Model<String>(""))));
    }
 }
