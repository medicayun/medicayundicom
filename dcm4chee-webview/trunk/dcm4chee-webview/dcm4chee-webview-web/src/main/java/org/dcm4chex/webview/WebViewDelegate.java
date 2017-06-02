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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.webview;

import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.dcm4che2.data.DicomObject;
import org.jboss.mx.util.MBeanServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author franz.willer@agfa.com
 * @version $Revision$ $Date$
 * @since 04.10.2006
 *
 */
public class WebViewDelegate {

    private static final String DEFAULT_WEBVIEW_SERVICE = "dcm4chee.archive:service=WebViewService";

    private static MBeanServer server;

    private ObjectName webviewName;
    
    private Boolean ignorePR, selectPR;
    
    static Logger log = LoggerFactory.getLogger(WebViewDelegate.class);
    
    private void init() throws MalformedObjectNameException, NullPointerException {
        WebViewDelegate.server = MBeanServerLocator.locate();
        if ( webviewName == null )
            webviewName = new ObjectName(DEFAULT_WEBVIEW_SERVICE);
    }
    
    /**
     * Set ObjectName of WebView Service.
     * <p>
     * If name is null use default WebView Service (dcm4chee.archive:service=WebViewService)
     * 
     * @param name New Objectname of WebView Service
     * 
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     */
    public void setWebviewServiceName(String name) throws MalformedObjectNameException, NullPointerException {
        webviewName = new ObjectName(name != null ? name : DEFAULT_WEBVIEW_SERVICE);
    }

    /**
     * @return Returns the ignorePR.
     */
    public Boolean getIgnorePR() {
        return ignorePR;
    }
    /**
     * @param ignorePR The ignorePR to set.
     */
    public void setIgnorePR(String ignorePR) {
        if ( ignorePR != null) {
            this.ignorePR = new Boolean(ignorePR);
        }
    }
    /**
     * @return Returns the selectPR.
     */
    public Boolean getSelectPR() {
        return selectPR;
    }
    /**
     * @param selectPR The selectPR to set.
     */
    public void setSelectPR(String selectPR) {
        if ( selectPR != null ) {
            this.selectPR = new Boolean(selectPR);
        }
    }
    
    /**
     * Get properties to launch webviewer according given http parameter map.
     * <p>
     * <dl>
     * <dt>Supported Query parameter:</dt>
     * <dd>accNr: Accession Number.</dd>
     * <dd>studyUID: Study Instance UID.</dd>
     * <dd>seriesUID: Series Instance UID.</dd>
     * <dd>prUID: SOP Instance UID of a Presentation State.</dd>
     * <dd>instanceUID: SOP Instance UID of a DICOM object.</dd>
     * </dl>
     * <dl>
     * <dt>Supported config parameter:</dt>
     * <dd>ignorePR: overwrites ignorePR attribute of WebView Service.</dd>
     * <dd>selectPR: overwrites selectPR attribute of WebView service.</dd>
     * </dl>
     * <p>
     * The return value contains a special property 'launchMode' which indicates the meaning
     * of the other properties.
     * <dl>
     * <dt>Property 'launchMode':</dt>
     * <dd>  applet: Properties contains applet parameter. </dd>
     * <dd>  pr_select: Properties contains a presentation state list (key=IUID/value=Description). </dd>
     * <dd>  study_select: Properties contains list of studies(key=StudyIUID/value=Description). </dd>
     * <dd>  empty: Properties contains only CODE and ARCHIVE. </dd>
     * <dd>  error: Properties contains only MESSAGE and SEVERITY. </dd>
     * </dl>
     * 
     * @param paraMap
     * @return
     */
    public Properties getLaunchProperties(Map paraMap) {
        setIgnorePR(getValue(paraMap,"ignorePR"));
        setSelectPR(getValue(paraMap,"selectPR"));

        String accNr = getValue(paraMap, "accNr");
        if ( accNr != null ) {
            return getLaunchPropertiesForAccNr( accNr );
        }
        String patId = getValue(paraMap, "patId");
        if ( patId != null ) {
            return getLaunchPropertiesForPatId( patId );
        }
        String url = getValue(paraMap, "manifestURL");
        if ( url != null ) {
            return getLaunchPropertiesForManifest( url );
        }
        String studyUID = getValue(paraMap,"studyUID");
        String seriesUID = getValue(paraMap,"seriesUID");
        String prUID = getValue(paraMap, "prUID");
        if ( prUID != null ) {
            return getLaunchPropertiesForPresentationState(studyUID, seriesUID, prUID);
        } 
        String iuid = getValue(paraMap, "instanceUID");
        if ( seriesUID != null || studyUID != null || iuid != null ) {
            return getLaunchProperties(studyUID, seriesUID, iuid);
        } else {
            return getErrorProperties("Missing query parameter!", "WARNING", new IllegalArgumentException("Missing iuid, seriesUID or studyUID as query parameter"));
        }
    }
        
    private String getValue( Map paraMap, String key ) {
        String[] value = (String[]) paraMap.get(key);
        return value == null ? null : value[0];
    }
    
    public Properties getLaunchPropertiesForAccNr(String accNr) {
        try {
            if ( server == null ) init();
            return (Properties) server.invoke(webviewName,
                    "getLaunchPropertiesForAccNr",
                    new Object[] { accNr, ignorePR, selectPR },
                    new String[] { String.class.getName(), Boolean.class.getName(), Boolean.class.getName() });
        } catch (Exception e) {
            return getErrorProperties("Failed to get LaunchProperties for Accession Number! Exception:", "ERROR", e);
        }
    }
    public Properties getLaunchPropertiesForPatId(String patId) {
        try {
            if ( server == null ) init();
            return (Properties) server.invoke(webviewName,
                    "getLaunchPropertiesForPatId",
                    new Object[] { patId, ignorePR, selectPR },
                    new String[] { String.class.getName(), Boolean.class.getName(), Boolean.class.getName() });
        } catch (Exception e) {
            return getErrorProperties("Failed to get LaunchProperties for Patient ID! Exception:", "ERROR", e);
        }
    }
    public Properties getLaunchPropertiesForManifest(String url) {
        try {
            if ( server == null ) init();
            return (Properties) server.invoke(webviewName,
                    "getLaunchPropertiesForManifest",
                    new Object[] { url },
                    new String[] { String.class.getName() });
        } catch (Exception e) {
            log.error("Failed to get LaunchProperties for Manifest!", e);
            return getErrorProperties("Failed to get LaunchProperties for manifest (URL:"+url+"! Exception:", "ERROR", e);
        }
    }
    public Properties getLaunchProperties(String studyUID, String seriesUID, String iuid) {
        try {
            if ( server == null ) init();
            return (Properties) server.invoke(webviewName,
                    "getLaunchProperties",
                    new Object[] { studyUID, seriesUID, iuid, ignorePR, selectPR },
                    new String[] { String.class.getName(), String.class.getName(), String.class.getName(), 
                                   Boolean.class.getName(), Boolean.class.getName() });
        } catch (Exception e) {
            return getErrorProperties("Failed to get LaunchProperties for UIDs! Exception:", "ERROR", e);
        }
    }
    
    public Properties getLaunchPropertiesForQuery(DicomObject keys) {
        try {
            if ( server == null ) init();
            return (Properties) server.invoke(webviewName,
                    "getLaunchPropertiesForQuery",
                    new Object[] { keys, ignorePR, selectPR },
                    new String[] { DicomObject.class.getName(), Boolean.class.getName(), Boolean.class.getName() });
        } catch (Exception e) {
            return getErrorProperties("Failed to get LaunchProperties for Query Dataset! Exception:", "ERROR", e);
        }
    }
    
    public Properties getLaunchPropertiesForPresentationState( String studyUID, String seriesUID, String instanceUID) {
        try {
            if ( server == null ) init();
            return (Properties) server.invoke(webviewName,
                    "getLaunchPropertiesForPresentationState",
                    new Object[] { studyUID, seriesUID, instanceUID },
                    new String[] { String.class.getName(),String.class.getName(),String.class.getName() });
        } catch (Exception e) {
            return getErrorProperties("Failed to get LaunchProperties for PresentationState! Exception:", "ERROR", e);
        }
    }
     
   private Properties getErrorProperties(String msg, String severity, Exception e) {
       log.error("msg", e);
       Properties p = new Properties();
       p.setProperty("launchMode", "error");
       p.setProperty("MESSAGE", msg != null ? msg+e.getMessage() : 
           e.getMessage() != null ? e.getMessage() : "Unknown Error!");
       p.setProperty("SEVERITY", severity != null ? severity : "ERROR");
       return p;
   }
}