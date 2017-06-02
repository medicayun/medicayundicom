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

package org.dcm4chee.web.common.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.dcm4chee.web.common.secure.SecureAjaxTabbedPanel;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since July 12, 2009
 */

public class ModuleSelectorPanel extends SecureAjaxTabbedPanel {

    private static final long serialVersionUID = 1L;
    
    public boolean showLogout = true;

    public ModuleSelectorPanel(String id) {
        super(id);

        boolean found = false;
        Cookie[] cs = ((WebRequest) RequestCycle.get().getRequest()).getHttpServletRequest().getCookies();
        if (cs != null)
            for (Cookie c : cs) {
                if (c.getName().equals("WEB3LOCALE")) {
                    getSession().setLocale(new Locale(c.getValue()));
                    found = true;
                    break;
                }
            }

        if (!found) {
            ((WebResponse) RequestCycle.get().getResponse()).addCookie(new Cookie("WEB3LOCALE", "en"));
            getSession().setLocale(new Locale("en"));
        }

        add(new AjaxFallbackLink<Object>("logout") {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick(final AjaxRequestTarget target) {
                getSession().invalidate();
                setResponsePage(getApplication().getHomePage());
            }
            @Override
            public boolean isVisible() {
                return showLogout;
            }
        }.add(new Label("logoutLabel", new ResourceModel("logout"))));

        List<String> languages = new ArrayList<String>();
        languages.add("en");
        languages.add("de");
        languages.add("ja");

        final DropDownChoice<String> languageSelector = 
            new DropDownChoice<String>("language", new Model<String>(), languages, new ChoiceRenderer<String>() {

            private static final long serialVersionUID = 1L;
            
            @Override
            public String getDisplayValue(String object) {
                Locale l = new Locale(object);
                return l.getDisplayName(l);
            }
        }) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSelectionChanged(String newSelection) {
                ((WebResponse) RequestCycle.get().getResponse()).addCookie(new Cookie("WEB3LOCALE", newSelection));
                getSession().setLocale(new Locale(newSelection));
            }
        };
        languageSelector.setDefaultModelObject(getSession().getLocale().getLanguage());
        languageSelector.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = 1L;

            protected void onUpdate(AjaxRequestTarget target) {
                languageSelector.onSelectionChanged();
                target.addComponent(getPage().setOutputMarkupId(true));
            }
        });
        add(languageSelector);

        add(new Image("img_logo", new ResourceReference(ModuleSelectorPanel.class, 
                "images/logo.gif"))
        );
    }

    public void addModule(final Class<? extends Panel> clazz) {
        super.addModule(clazz, null);
        if (clazz.getResource("base-style.css") != null)
            add(CSSPackageResource.getHeaderContribution(clazz, "base-style.css"));
    }

    public void addInstance(Panel instance) {
        addInstance(instance, null);
    }

    public void addInstance(Panel instance, IModel<String> titleModel) {
        super.addModule(instance.getClass(), titleModel);
        if (instance.getClass().getResource("base-style.css") != null)
            add(CSSPackageResource.getHeaderContribution(instance.getClass(), "base-style.css"));
    }

    public ModuleSelectorPanel setShowLogoutLink(boolean show) {
        showLogout = show;
        return this;
    }
}
