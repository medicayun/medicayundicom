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

package org.dcm4chee.web.war.folder.webviewer;

import org.apache.wicket.PageMap;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.ResourceModel;
import org.dcm4chee.web.common.base.BaseWicketPage;
import org.dcm4chee.web.common.webview.link.WebviewerLinkProvider;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision: 15833 $ $Date: 2011-08-23 19:20:27 +0800 (周二, 23 8月 2011) $
 * @since May 24, 2011
 */
public class WebviewerSelectionPage extends WebPage {
    
    private static final ResourceReference BaseCSS = new CompressedResourceReference(BaseWicketPage.class, "base-style.css");
    
    public WebviewerSelectionPage(AbstractDicomModel model, WebviewerLinkProvider[] providers) {
        super();        
        if (WebviewerSelectionPage.BaseCSS != null)
            add(CSSPackageResource.getHeaderContribution(WebviewerSelectionPage.BaseCSS));
        
        add(new Label("header", new ResourceModel("webviewer.selection.header")));
        add(new Label("info", model.toString()));
        RepeatingView rv = new RepeatingView("repeater");
        add(rv);
        WebMarkupContainer mc;
        for (int i = 0 ; i < providers.length ; i++) {
            String url = Webviewer.getUrlForModel(model, providers[i]);
            if (url != null) {
                mc = new WebMarkupContainer(rv.newChildId());
                ExternalLink link = new ExternalLink("link", url, providers[i].getName())
                    .setPopupSettings(new PopupSettings(PageMap.forName(providers[i].getName()), 
                        PopupSettings.RESIZABLE|PopupSettings.SCROLLBARS));
                mc.add(link);
                rv.add(mc);
            }
        }
    }
}
