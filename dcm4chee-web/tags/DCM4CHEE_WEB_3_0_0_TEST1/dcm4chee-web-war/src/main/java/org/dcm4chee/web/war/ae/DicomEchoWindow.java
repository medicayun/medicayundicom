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

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.dcm4chee.archive.entity.AE;
import org.dcm4chee.icons.ImageManager;
import org.dcm4chee.web.common.behaviours.FocusOnLoadBehaviour;
import org.dcm4chee.web.common.markup.BaseForm;
import org.dcm4chee.web.war.ae.model.CipherModel;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 18, 2009
 */
public class DicomEchoWindow extends ModalWindow {

    private static final long serialVersionUID = 1L;

    private boolean echoOnShow;
    private AE aeOri;
    private final AE aeEcho = new AE();
    private Integer nrOfTests = 1;
    private boolean echoPerformed = false;
    private String result;
    private boolean echoRunning = false;
    private Image hourglassImage;
    private boolean saveFailed;
    
    private transient EchoDelegate delegate;
    
    public DicomEchoWindow(String id, boolean echoOnShow) {
        super(id);
        this.echoOnShow = echoOnShow;
        setTitle(new ResourceModel("aet.echoPanelTitle"));
        setContent(new DicomEchoPanel("content"));

        setCloseButtonCallback(new CloseButtonCallback() {

            private static final long serialVersionUID = 1L;

            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                return !echoRunning;
            }
        });
    }
    
    private EchoDelegate getDelegate() {
        if (delegate == null) {
            delegate = new EchoDelegate();
        }
        return delegate;
    }

    public void show(AjaxRequestTarget target, AE ae) {
        setAE(ae);
        echoPerformed = false;
        result = "";
        nrOfTests = 1;
        saveFailed = false;
        super.show(target);
        target.focusComponent(this.get("content:form:cancel"));        
    }
    
    public void setAE(final AE ae) {
        aeOri = ae;
        copyNetCfg(aeOri, aeEcho);
    }
    
    private AE copyNetCfg(final AE aeSrc, final AE aeDest) {
        aeDest.setTitle(aeSrc.getTitle());
        aeDest.setHostName(aeSrc.getHostName());
        aeDest.setPort(aeSrc.getPort());
        aeDest.setCipherSuites(aeSrc.getCipherSuites());
        return aeDest;
    }
    private boolean isSameNetCfg(final AE aeSrc, final AE aeDest) {
        if ( aeDest.getTitle().equals(aeSrc.getTitle()) &&
                aeDest.getHostName().equals(aeSrc.getHostName()) &&
                aeDest.getPort()== aeSrc.getPort() ) {
            List<String> c1 = aeSrc.getCipherSuites();
            List<String> c2 = aeDest.getCipherSuites();
            if ( c1 == null ) {
                return c2 == null;
            } else if ( c2 != null ) {
                if ( c1.size() == c2.size() ) {
                    Iterator<String> it1 = c1.iterator();
                    Iterator<String> it2 = c2.iterator();
                    while (it1.hasNext()) {
                        if ( !it1.next().equals(it2.next()) ) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    public class DicomEchoPanel extends Panel {
    
    private static final long serialVersionUID = 1L;
    private BaseForm form;
    private Component timerComponent;
    private AjaxButton saveBtn = new SaveButton("save");
    private AjaxButton echoBtn = new EchoButton("echo");
    private AjaxButton pingBtn = new PingButton("ping");
    private AjaxButton cancelBtn;
    private AbstractAjaxTimerBehavior timer;
    
    private IModel<Integer> nrOfTestsModel = new IModel<Integer>() {

        private static final long serialVersionUID = 1L;
        
        public Integer getObject() {
            return nrOfTests;
        }
        public void setObject(Integer object) {
            nrOfTests = (Integer)object;
        }
        public void detach() {}
    };
    
    private AbstractAjaxTimerBehavior getTimer() {
        AbstractAjaxTimerBehavior timer = new AbstractAjaxTimerBehavior(Duration.milliseconds(200)) {
    
            private static final long serialVersionUID = 1L;
            private String lastResult = null;
            private boolean lastRunningState = false;
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                if (!echoRunning && !echoPerformed && echoOnShow) {
                    doEcho(aeEcho);
                } else if (echoRunning^lastRunningState) {
                    addToTarget(target);
                    if (!echoRunning) {
                        stop();
                    }
                    lastRunningState = echoRunning;
                } else if (!result.equals(lastResult)) {
                    lastResult = result;
                    target.addComponent(resultLabel);
                }
            }
        }; 
        return timer;
    }
    
    private Label resultLabel = new Label("result", 
            new AbstractReadOnlyModel<Object>() {
        
                private static final long serialVersionUID = 1L;

                @Override
                public Object getObject() {
                    return result;
                }
            })
        {
            private static final long serialVersionUID = 1L;
    
            @Override
            public void onComponentTag(ComponentTag tag) {
                tag.getAttributes().put("class", saveFailed ? "ae_save_failed" : 
                         ((!echoPerformed || echoRunning) ? "ae_echo_pending" : 
                         (result.indexOf("success") != -1 ? "ae_echo_succeed" : "ae_echo_failed")));
                super.onComponentTag(tag);
            }
        };
        
    public DicomEchoPanel(String id) {
        super(id);
        form = new BaseForm("form");
        form.setResourceIdPrefix("aet.");
        add(CSSPackageResource.getHeaderContribution(DicomEchoPanel.class, "ae-style.css"));
        add(form);
        CompoundPropertyModel<AE> model = new CompoundPropertyModel<AE>(aeEcho);
        setDefaultModel(model);
        form.add(timerComponent = new Label("aetLabel", new ResourceModel("aet.echoAETitle")).setOutputMarkupId(true));
        form.add(new Label("ciphersLabel", new ResourceModel("aet.echoCiphers")));
        form.add(new Label("nrOfTestsLabel", new ResourceModel("aet.echoNrOfTests")));
        form.add(new Label("echoResultLabel", new ResourceModel("aet.echoResult")));
        form.add(new TextField<String>("title").add(new AETitleValidator()).setRequired(true).setOutputMarkupId(true)); 
        form.add(new TextField<String>("hostName").add(StringValidator.minimumLength(1)).setRequired(true).setOutputMarkupId(true)); 
        form.add(new TextField<Integer>("port").add(new RangeValidator<Integer>(1,65535)).setOutputMarkupId(true));
        form.add(new DropDownChoice<String>("ciphersuite1", new CipherModel(aeEcho, 0), AEMgtDelegate.AVAILABLE_CIPHERSUITES).setOutputMarkupId(true));
        form.add(new DropDownChoice<String>("ciphersuite2", new CipherModel(aeEcho, 1), AEMgtDelegate.AVAILABLE_CIPHERSUITES).setOutputMarkupId(true));
        form.add(new DropDownChoice<String>("ciphersuite3", new CipherModel(aeEcho, 2), AEMgtDelegate.AVAILABLE_CIPHERSUITES).setOutputMarkupId(true));
        form.add(new TextField<Integer>("nrOfTests", nrOfTestsModel, Integer.class).add(new RangeValidator<Integer>(1,2000)).setOutputMarkupId(true));
        resultLabel.setOutputMarkupId(true).setEnabled(false);
        form.add(resultLabel);
        form.add(cancelBtn = new AjaxButton("cancel", new ResourceModel("cancelBtn")) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                close(target);
            }
            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                close(target);
            }
        }).setOutputMarkupId(true).add(FocusOnLoadBehaviour.newSimpleFocusBehaviour());
        saveBtn.setEnabled(false);
        MetaDataRoleAuthorizationStrategy.authorize(saveBtn, RENDER, "WebAdmin");
        form.add(saveBtn);
        form.add(echoBtn = new EchoButton("echo"));
        form.add(pingBtn = new PingButton("ping"));
        form.add((hourglassImage = new Image("hourglass-image", ImageManager.IMAGE_COMMON_AJAXLOAD) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return echoRunning;
            }
        }).setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true));
        setOutputMarkupId(true);
    }


    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        
        if (echoOnShow && !echoPerformed) {
            result = getString("aet.echoResult.default");
            getDelegate();
            timerComponent.add(timer = getTimer());
        }
    }

    private void addToTarget(AjaxRequestTarget target) {
        target.addComponent(resultLabel);
        target.addComponent(hourglassImage);
        target.addComponent(echoBtn);
        target.addComponent(pingBtn);
        target.addComponent(cancelBtn);
    }
    
    public void doEcho(final AE ae) {
        echoRunning = true;
        getDelegate();//ensure that delegate is initialized - will fail in new thread! 
        new Thread(new Runnable(){
            public void run() {
                try {
                    result = getDelegate().echo(ae, nrOfTests);
                } catch (Throwable t) {
                    t.printStackTrace();
                    result = "Echo failed! Reason:"+t.getMessage();
                } finally {
                    echoPerformed = true;
                    echoRunning = false;
                }
            }}).start();
    }

    public void doPing(final AE ae) {
        echoRunning = true;
        result = getString("aet.echoResult.ping", new Model<AE>(ae));
        getDelegate();
        final String success = getString("aet.ping_success");
        final String failed = getString("aet.ping_failed");
        new Thread(new Runnable(){
            public void run() {
                try {
                    for (int i = 0 ; i < nrOfTests ; i++) {
                        result += "\n";
                        result += delegate.ping(ae.getHostName()) ? success : failed;
                    }
                } catch (UnknownHostException x) {
                    result = "Ping failed! Unknown host:"+x.getMessage();
                } finally {
                    echoPerformed = true;
                    echoRunning = false;
                }
            }}).start();
    }

    private void newTimer() {
        if (timer != null)
            timerComponent.remove(timer);
        timerComponent.add(timer = getTimer());
    }

    class EchoButton extends AjaxButton {

        private static final long serialVersionUID = 1L;
        
        private EchoButton(String id) {
            super(id, new ResourceModel("aet.echoButton"));
            setOutputMarkupId(true);
        }
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
            newTimer();
            result = getString("aet.echoResult.default");
            doEcho(aeEcho);
            boolean chgd = !isSameNetCfg( aeOri, aeEcho);
            if ( chgd != saveBtn.isEnabled()) {
                saveBtn.setEnabled(chgd);
                target.addComponent(saveBtn);
            }
            target.addComponent(resultLabel);
            target.addComponent(timerComponent);
       }
        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form) {
            target.addComponent(resultLabel);
            BaseForm.addInvalidComponentsToAjaxRequestTarget(target, form);
        }
        @Override
        public boolean isEnabled() {
            return !echoRunning;
        }
    }

    class PingButton extends AjaxButton {

        private static final long serialVersionUID = 1L;
        
        private PingButton(String id) {
            super(id, new ResourceModel("aet.pingButton"));
            setOutputMarkupId(true);
        }
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
            newTimer();
            doPing(aeEcho);
            target.addComponent(resultLabel);
            target.addComponent(timerComponent);
        }
        
        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form) {
            target.addComponent(resultLabel);
            BaseForm.addInvalidComponentsToAjaxRequestTarget(target, form);
        }
        @Override
        public boolean isEnabled() {
            return !echoRunning;
        }
    }
    
    class SaveButton extends AjaxButton {

        private static final long serialVersionUID = 1L;
        
        private SaveButton(String id) {
            super(id, new ResourceModel("saveBtn"));
        }
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
            try {
                AEMgtDelegate.getInstance().update(copyNetCfg(aeEcho, aeOri));
                saveFailed = false;
                saveBtn.setEnabled(false);
                close(target);
            } catch (Exception x) {
                result = (String) getString("aet.titleAlreadyExist");
                saveFailed = true;
                target.addComponent(resultLabel);
                target.addComponent(saveBtn);
            }
        }
    }
    }
}
