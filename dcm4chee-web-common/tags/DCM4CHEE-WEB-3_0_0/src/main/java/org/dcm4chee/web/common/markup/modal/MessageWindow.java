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

package org.dcm4chee.web.common.markup.modal;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.web.common.markup.modal.AutoOpenModalWindow;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 02, 2009
 */
public class MessageWindow extends AutoOpenModalWindow {

    private static final long serialVersionUID = 1L;
    
    protected static final String TITLE_DEFAULT = "MessageWindow";
    public static final String TITLE_INFO="msgwindow.title.info";
    public static final String TITLE_WARNING="msgwindow.title.warn";
    public static final String TITLE_ERROR="msgwindow.title.error";
    
    private IModel<String> msgModel;
    private IModel<String> colorModel = new Model<String>("");

    public MessageWindow(String id) {
        super(id);
        initContent();
    }

    public MessageWindow(String id, String titleResource) {
        this(id);
        setTitle(new ResourceModel(titleResource));
    }

    protected void initContent() {
        setInitialWidth(300);
        setInitialHeight(200);
        setTitle(new ResourceModel(TITLE_INFO,TITLE_DEFAULT));
        setContent(new MessageWindowPanel("content"));
    }
    
    public void setMessage(String msg) {
        this.msgModel = new Model<String>(msg);
    }
    public void setInfoMessage(String msg) {
        this.msgModel = new Model<String>(msg);
        setTitle(new ResourceModel(TITLE_INFO,TITLE_DEFAULT));
    }
    public void setWarningMessage(String msg) {
        this.msgModel = new Model<String>(msg);
        setTitle(new ResourceModel(TITLE_WARNING,TITLE_DEFAULT));
    }
    public void setErrorMessage(String msg) {
        this.msgModel = new Model<String>(msg);
        setTitle(new ResourceModel(TITLE_ERROR,TITLE_DEFAULT));
    }
    
    /**
     * Called by onBeforeRender to check if window should be opened without AJAX for this request. 
     *
     * @return true when window should be opened. 
     */
    protected boolean needAutoOpen() {
        return msgModel != null;
    }
    
    @Override
    public void show(final AjaxRequestTarget target) {
        super.show(target);
        target.focusComponent(this.get("content:close"));
    }

    public void show(AjaxRequestTarget target, String msg) {
        this.msgModel = new Model<String>(msg);
        show(target);
    }

    public void setColor(String color) {
        this.colorModel.setObject("color: " + color);
    }
    
    public void show(AjaxRequestTarget target, IModel<String> msg) {
        this.msgModel = msg;
        show(target);
    }

    public class MessageWindowPanel extends Panel {
        private static final long serialVersionUID = 0L;

        public MessageWindowPanel(String id) {
            super(id);
            add(new Label("msg", new AbstractReadOnlyModel<String>(){

                private static final long serialVersionUID = 1L;

                @Override
                public String getObject() {
                    return msgModel == null ? null : msgModel.getObject();
                }
            })
            .add(new AttributeModifier("style", true, colorModel)));

            add(new AjaxFallbackLink<Object>("close"){

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget target) {
                    close(target);
                }
            }.add(new Label("closeLabel", new ResourceModel("closeBtn"))) );
        }

        /**
         * Return always true because ModalWindow.beforeRender set visibility of content to false!
         */
        @Override
        public boolean isVisible() {
            return true;
        }
        @Override
        protected void onAfterRender() {
            msgModel = null;
            super.onAfterRender();
        }
    }
}
