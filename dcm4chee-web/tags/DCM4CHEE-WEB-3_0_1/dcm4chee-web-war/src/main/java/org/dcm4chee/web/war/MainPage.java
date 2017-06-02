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

package org.dcm4chee.web.war;

import java.util.Properties;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.dcm4chee.dashboard.ui.DashboardPanel;
import org.dcm4chee.usr.ui.usermanagement.ChangePasswordPanel;
import org.dcm4chee.usr.ui.usermanagement.role.RolePanel;
import org.dcm4chee.usr.ui.usermanagement.user.UserListPanel;
import org.dcm4chee.web.common.base.BaseWicketApplication;
import org.dcm4chee.web.common.base.ExternalWebApp;
import org.dcm4chee.web.common.base.ExternalWebApplications;
import org.dcm4chee.web.common.base.ModuleSelectorPanel;
import org.dcm4chee.web.common.license.ae.AELicenseProviderManager;
import org.dcm4chee.web.common.license.ae.spi.AELicenseProviderSPI;
import org.dcm4chee.web.common.secure.SecureSession;
import org.dcm4chee.web.common.secure.SecureWicketPage;
import org.dcm4chee.web.war.ae.AEPanel;
import org.dcm4chee.web.war.folder.StudyListPage;
import org.dcm4chee.web.war.tc.TCPanel;
import org.dcm4chee.web.war.trash.TrashListPage;
import org.dcm4chee.web.war.worklist.modality.ModalityWorklistPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision: 16027 $ $Date: 2011-09-29 20:56:14 +0800 (周四, 29 9月 2011) $
 * @since July 7, 2009
 */
public class MainPage extends SecureWicketPage {
    
    protected static Logger log = LoggerFactory.getLogger(MainPage.class);
    
    public MainPage() {
        super();
        
        if (StudyPermissionHelper.get().isSSO())
            add (new AbstractAjaxTimerBehavior(Duration.milliseconds(1)) {
    
                private static final long serialVersionUID = 1L;
    
                @Override
                protected void onTimer(AjaxRequestTarget arg0) {
                    try {
                        StudyPermissionHelper.get().doDicomAuthentication();
                    } catch (Exception e) {
                        log.error(getClass().getName() + ": error doing dicom authentication: ", e);
                    } finally {
                        this.stop();
                    }
                }
            });
        addModules(getModuleSelectorPanel());
    }

    private void addModules(ModuleSelectorPanel selectorPanel) {
        
        AELicenseProviderSPI provider = AELicenseProviderManager.get(null).getProvider();

        selectorPanel.addModule(StudyListPage.class);
        selectorPanel.addModule(TrashListPage.class);
        selectorPanel.addModule(AEPanel.class);
        selectorPanel.addModule(ModalityWorklistPanel.class);
        if (provider.allowFeature("Teachingfiles"))
            selectorPanel.addModule(TCPanel.class);
        if (provider.allowFeature("Dashboard"))
            selectorPanel.addModule(DashboardPanel.class);
        selectorPanel.addModule(RolePanel.class, null);

        if (((SecureSession) RequestCycle.get().getSession()).getManageUsers()) {
            selectorPanel.addModule(UserListPanel.class, null);
            selectorPanel.addModule(ChangePasswordPanel.class, null);
        }
        for (ExternalWebApp p : new ExternalWebApplications().getExternalWebAppPanels()) {
            selectorPanel.addModule(p.getPanel(), p.getTitle());
        }
        try {
            Properties properties = new Properties();
            properties.load(((BaseWicketApplication) getApplication()).getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
            selectorPanel.get("img_logo").add(new AttributeModifier("title", true, 
                    new Model<String>(
                            properties.getProperty("Implementation-Title", "")
                            + " : " + properties.getProperty("Implementation-Build", "")
                            + " (" + properties.getProperty("SCM-Revision", "?")+")"
                            )));            
        } catch (Exception ignore) {}
    }    
}
