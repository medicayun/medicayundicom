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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.resource.loader.PackageStringResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since July 12, 2009
 */

public class ModuleSelectorPanel extends AjaxTabbedPanel {

    private static final long serialVersionUID = 1L;
    private static Logger log = LoggerFactory.getLogger(ModuleSelectorPanel.class);
    private boolean showLogout = true;

    public ModuleSelectorPanel(String id) {
        super(id, new ArrayList<ITab>());
        add(new AjaxFallbackLink<Object>("logout"){
            
            private static final long serialVersionUID = 1L;
            
            @Override
            public void onClick(final AjaxRequestTarget target) {
                getSession().invalidate();
                setResponsePage(LoginPage.class);
            }
            @Override
            public boolean isVisible() {
                return showLogout;
            }
        }.add(new Label("logoutLabel", new ResourceModel("logout"))));
        
        List<String> languages = new ArrayList<String>();
        languages.add("en");
        languages.add("de");

        final DropDownChoice<String> languageSelector = 
            new DropDownChoice<String>("language", new Model<String>(), languages, new ChoiceRenderer<String>() {

            private static final long serialVersionUID = 1L;
            
            @Override
            public String getDisplayValue(String object) {
                return new Locale(object).getDisplayName();
            }
        }) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSelectionChanged(String newSelection) {
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
        if (Session.get().getAuthorizationStrategy().isInstantiationAuthorized(clazz)) {
            ITab tab = new ModuleTab(clazz);
            super.getTabs().add(tab);
            if (clazz.getResource("base-style.css") != null)
                add(CSSPackageResource.getHeaderContribution(clazz, "base-style.css"));
        }
    }

    public void addInstance(Panel instance) {
        addInstance(instance, null);
    }
    public void addInstance(Panel instance, IModel<String> titleModel) {
        if (Session.get().getAuthorizationStrategy().isInstantiationAuthorized(instance.getClass())) {
            ITab tab = new ModuleTab(instance, titleModel);
            super.getTabs().add(tab);
            if (instance.getClass().getResource("base-style.css") != null)
                add(CSSPackageResource.getHeaderContribution(instance.getClass(), "base-style.css"));
        }
    }
    
    public void setShowLogoutLink(boolean show) {
        showLogout = show;
    }
    
    private final class ModuleTab implements ITab {

        private static final long serialVersionUID = 1L;
        
        private transient ClassStringResourceLoader classStringLoader; 
        private transient PackageStringResourceLoader pkgStringLoader;
        private static final String TAB_TITLE_EXT = ".tabTitle";
        private Class<? extends Panel> clazz;
        private String clazzName;
        private String tabTitlePropertyName;
        private Panel panel;
        private IModel<String> titleModel;
        
        public ModuleTab(Class<? extends Panel> clazz2) {
            clazz = clazz2;
            initialize();
        }
        
        public ModuleTab(Panel instance, IModel<String> titleModel) {
            clazz = instance.getClass();
            panel = instance;
            this.titleModel = titleModel;
            if ( titleModel == null)
                initialize();
        }

        private void initialize() {
            clazzName = clazz.getName().substring(clazz.getName().lastIndexOf('.')+1);            
            pkgStringLoader = new PackageStringResourceLoader();
            try {
                Method m = clazz.getDeclaredMethod("getModuleName");
                tabTitlePropertyName = m.invoke(null)+TAB_TITLE_EXT;
            } catch (Exception e) {
                log.warn("Panel class "+clazz+" has no static getModuleName() method declared! use classname instead!:"+clazzName);
                tabTitlePropertyName = clazzName+TAB_TITLE_EXT;
            }
        }

        private ClassStringResourceLoader getClassStringLoader() {
            if ( classStringLoader == null )
                classStringLoader = new ClassStringResourceLoader(clazz);
            return classStringLoader;
        }
        private PackageStringResourceLoader getPackageStringLoader() {
            if ( pkgStringLoader == null )
                pkgStringLoader = new PackageStringResourceLoader();
            return pkgStringLoader;
        }

        public Panel getPanel(String panelId) {
            try {
                if ( panel == null ) {
                    Constructor<? extends Panel> c = clazz.getConstructor(String.class);
                    panel = c.newInstance(panelId);
                }
            } catch ( Exception x ) {
                log.error("Can't instantiate Panel for "+panelId, x);
                return null;
            }
            return panel;
        }

        /**
         * Get title of the tab.
         * Use ClassStringResourceLoader and PackageStringResourceLoader to get the title String.
         * Using a ResourceModel at this point would fail because the panel isn't instantiated by
         * the TabbedPane. 
         * Therefore the title must be either configured in package.properties or &lt;clazz&gt;.properties! 
         * Other ResourceLoaders will be ignored!
         */
        public IModel<String> getTitle() {
            if ( titleModel == null) {
                String t = getClassStringLoader().loadStringResource(null, tabTitlePropertyName);
                if (t== null)
                    t = getPackageStringLoader().loadStringResource(clazz, tabTitlePropertyName, 
                            getSession().getLocale(), null);
                return new Model<String>(t == null ? clazzName : t);
            } else {
                return titleModel;
            }
        }

        public boolean isVisible() {
            return true;
        }
    }

}
