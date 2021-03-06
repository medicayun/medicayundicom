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

package org.dcm4chee.dashboard.ui.report.display;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.dcm4chee.dashboard.model.ReportModel;
import org.dcm4chee.dashboard.ui.DashboardPanel;
import org.dcm4chee.dashboard.ui.util.DatabaseUtils;
import org.dcm4chee.web.common.base.BaseWicketPage;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 28.01.2010
 */
public class DynamicDisplayPage extends WebPage {

    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    private static final ResourceReference DashboardCSS = new CompressedResourceReference(DashboardPanel.class, "dashboard-style.css");
    private static final ResourceReference PrintCSS = new CompressedResourceReference(DashboardPanel.class, "dashboard-print.css");

    public DynamicDisplayPage(ReportModel report, Map<String, String> parameters, boolean displayDiagram, boolean displayTable) {

        if (DynamicDisplayPage.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(DynamicDisplayPage.BaseCSS));
        if (DynamicDisplayPage.DashboardCSS != null)
            add(CSSPackageResource.getHeaderContribution(DynamicDisplayPage.DashboardCSS));
        if (DynamicDisplayPage.PrintCSS != null)
            add(CSSPackageResource.getHeaderContribution(DynamicDisplayPage.PrintCSS, "print"));

        add(new Label("title", report.getTitle()));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(report.getCreated());
        add(new Label("date", new SimpleDateFormat("dd.MM.yyyy hh:mm").format(calendar.getTime())));
        add(new Label("statement", DatabaseUtils.createSQLStatement(report.getStatement())));
        add(displayDiagram ? new DisplayReportDiagramPanel("diagramPanel", report, parameters) : new Label("diagramPanel", "").setVisible(false));
        add(displayTable ? new DisplayReportTablePanel("tablePanel", report, parameters) : new Label("tablePanel", "").setVisible(false));
    }
    
    class PlaceholderLink extends Link<Object> {
        
        private static final long serialVersionUID = 1L;

        public PlaceholderLink(String id) {
            super(id);
            
            this.setVisible(false);
        }
        
        @Override
        public void onClick() {}
    }
}
