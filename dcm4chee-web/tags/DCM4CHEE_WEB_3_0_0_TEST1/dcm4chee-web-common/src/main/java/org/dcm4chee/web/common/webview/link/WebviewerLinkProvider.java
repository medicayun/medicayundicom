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

package org.dcm4chee.web.common.webview.link;

import java.io.Serializable;
import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;

import org.dcm4chee.web.common.webview.link.spi.WebviewerLinkProviderSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since May 15, 2010
 */
public class WebviewerLinkProvider implements Serializable {
    private static final long serialVersionUID = 1L;

    private WebviewerLinkProviderSPI provider;
    
    private static Logger log = LoggerFactory.getLogger(WebviewerLinkProvider.class);
    
    public WebviewerLinkProvider(String webviewerName) {
        Iterator<WebviewerLinkProviderSPI> iter = ServiceRegistry.lookupProviders(WebviewerLinkProviderSPI.class);
        WebviewerLinkProviderSPI p;
        while (iter.hasNext()) {
            p = iter.next();
            log.debug("Found WebviewerLinkProvider: {}",p.getName());
            if (webviewerName == null || webviewerName.equals(p.getName())) {
                provider = p;
                break;
            }
        }
        if (provider == null) {
            log.warn("No WebviewerLinkProvider found for name: {}", webviewerName);
            provider = new DummyProvider();
        }
        log.debug("Selected WebviewerLinkProvider: {}",provider.getName());
    }
    
    public void setBaseUrl(String baseUrl) {
        provider.setBaseURL(baseUrl);
    }
    public boolean supportPatientLevel() {
        return provider.supportPatientLevel();
    }
    public boolean supportStudyLevel() {
        return provider.supportStudyLevel();
    }
    public boolean supportSeriesLevel() {
        return provider.supportSeriesLevel();
    }
    public boolean supportInstanceLevel() {
        return provider.supportInstanceLevel();
    }
    public boolean supportPresentationState() {
        return provider.supportPresentationState();
    }
    public boolean supportKeySelectionObject() {
        return provider.supportKeySelectionObject();
    }
    public String getUrlForPatient(String patientId, String issuer) {
        return provider.getUrlForPatient(patientId, issuer);
    }
    public String getUrlForStudy(String studyIuid){
        return provider.getUrlForStudy(studyIuid);
    }
    public String getUrlForSeries(String seriesIuid){
        return provider.getUrlForSeries(seriesIuid);
    }
    public String getUrlForInstance(String sopIuid) {
        return provider.getUrlForInstance(sopIuid);
    }
    public String getUrlForPresentationState(String iuid) {
        return provider.getUrlForPresentationState(iuid);
    }
    public String getUrlForKeyObjectSelection(String iuid) {
        return provider.getUrlForKeyObjectSelection(iuid);
    }
    
    private class DummyProvider extends WebviewerLinkProviderSPI {

        private static final long serialVersionUID = 1L;
        
        public String getName() {
            return "DUMMY";
        }

        public String getUrlForInstance(String sopIuid) {
            return null;
        }
        public String getUrlForKeyObjectSelection(String iuid) {
            return null;
        }
        public String getUrlForPatient(String patientId, String issuer) {
            return null;
        }
        public String getUrlForPresentationState(String iuid) {
            return null;
        }
        public String getUrlForSeries(String seriesIuid) {
            return null;
        }
        public String getUrlForStudy(String studyIuid) {
            return null;
        }
        public boolean supportInstanceLevel() {
            return false;
        }
        public boolean supportKeySelectionObject() {
            return false;
        }
        public boolean supportPatientLevel() {
            return false;
        }
        public boolean supportPresentationState() {
            return false;
        }
        public boolean supportSeriesLevel() {
            return false;
        }
        public boolean supportStudyLevel() {
            return false;
        }
    }
}
