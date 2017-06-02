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

package org.dcm4chex.webview.mbean;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chex.webview.CustomLaunchProperties;
import org.dcm4chex.webview.InstanceComparator;
import org.dcm4chex.webview.InstanceContainer;

/**
 * @author franz.willer@agfa.com
 * @version $Revision$ $Date$
 * @since 04.10.2006
 */
public class LaunchProperties {

    
    public static final String PROP_LAUNCH_MODE = "launchMode";
    public static final String PROP_CODE = "CODE";
    public static final String PROP_ARCHIVE = "ARCHIVE";
    public static final String PROP_MESSAGE = "MESSAGE";
    public static final String PROP_SEVERITY = "SEVERITY";
    public static final String SEVERITY_WARNING = "WARNING";
    public static final String SEVERITY_ERROR = "ERROR";
    public static final String SEVERITY_FATAL = "FATAL";
    
    private String appletClass;
    private String appletArchive;
    private Map result2appletParameterMap = new HashMap();
    private Map appletParameterMap = new HashMap();
    private Map imageCUIDs = null;
    private Map psCUIDs = null;//Presentation state SOPClass UIDs
    private String[] paraSeriesInstances = new String[]{null,"","",";",";",""};
    private String[] paraPresentationStates = new String[]{null,"","","",";",""};
    private CustomLaunchProperties customProps = null;

    private InstanceComparator instanceComparator = null;
    
    public LaunchProperties() {
    }
    
    /**
     * @return Returns the appletArchive.
     */
    public String getAppletArchive() {
        return appletArchive;
    }
    /**
     * @param appletArchive The appletArchive to set.
     */
    public void setAppletArchive(String appletArchive) {
        this.appletArchive = appletArchive;
    }
    /**
     * @return Returns the appletClass.
     */
    public String getAppletClass() {
        return appletClass;
    }
    /**
     * @param appletClass The appletClass to set.
     */
    public void setAppletClass(String appletClass) {
        this.appletClass = appletClass;
    }
    /**
     * @return Returns the appletParameterMap.
     */
    public Map getAppletParameterMap() {
        return appletParameterMap;
    }
    /**
     * @param appletParameterMap The appletParameterMap to set.
     */
    public void setAppletParameterMap(Map appletParameterMap) {
        this.appletParameterMap = appletParameterMap;
    }
    /**
     * @return Returns the customProps.
     */
    public CustomLaunchProperties getCustomProps() {
        return customProps;
    }
    /**
     * @param customProps The customProps to set.
     */
    public void setCustomProps(CustomLaunchProperties customProps) {
        this.customProps = customProps;
    }
    /**
     * @return Returns the imageCUIDs.
     */
    public Map getImageCUIDs() {
        return imageCUIDs;
    }
    /**
     * @param imageCUIDs The imageCUIDs to set.
     */
    public void setPresentationStateCUIDs(Map cuids) {
        this.psCUIDs = cuids;
    }
    
    /**
     * @return Returns the imageCUIDs.
     */
    public Map getPresentationStateCUIDs() {
        return psCUIDs;
    }
    /**
     * @param imageCUIDs The imageCUIDs to set.
     */
    public void setImageCUIDs(Map imageCUIDs) {
        this.imageCUIDs = imageCUIDs;
    }
     
    /**
     * @return Returns the paraPresentationStates.
     */
    public String getParaPresentationStates() {
        return toListConfigString( paraPresentationStates );
    }
    /**
     * @param paraPresentationStates The paraPresentationStates to set.
     */
    public void setParaPresentationStates(String cfg) {
        setListConfigString( paraPresentationStates, cfg );
    }
    /**
     * @return Returns the paraSeriesInstances.
     */
    public String getParaSeriesInstances() {
        return toListConfigString( paraSeriesInstances );
    }
    /**
     * @param paraSeriesInstances The paraSeriesInstances to set.
     */
    public void setParaSeriesInstances(String cfg) {
        setListConfigString( paraSeriesInstances, cfg );
    }
    
    public InstanceComparator getInstanceComparatorClass() {
		return instanceComparator;
	}
	public void setInstanceComparatorClass(InstanceComparator instanceComparator) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.instanceComparator = instanceComparator;
	}

    
    private String toListConfigString(String[] sa) {
        if ( sa == null || sa[0] == null) return "NONE";
        return sa[0]+"|"+sa[1]+"|"+sa[2]+"|"+sa[3]+"|"+sa[4]+"|"+sa[5];
    }
    
    private void setListConfigString(String[] sa, String cfg) {
        if ( cfg == null || cfg.equalsIgnoreCase("NONE")) {
            sa[0] = null;
        } else {
            int pos=0, pos1=0, i=0;
    	    while ( (pos1 = cfg.indexOf('|',pos)) > 0 ) {
    	        sa[i++]=cfg.substring(pos,pos1);
    	        pos=++pos1;
    	    }
            sa[i]=cfg.substring(pos);
        }
    }
    /**
     * @return Returns the result2appletParameterMap.
     */
    public Map getResult2appletParameterMap() {
        return result2appletParameterMap;
    }
    /**
     * @param result2appletParameterMap The result2appletParameterMap to set.
     */
    public void setResult2appletParameterMap(Map result2appletParameterMap) {
        this.result2appletParameterMap = result2appletParameterMap;
    }
    /**
     * @return Returns the customProps.
     */
    public CustomLaunchProperties getCustomLaunchPropertiesClass() {
        return customProps;
    }
    /**
     * @param customProps The customProps to set.
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void setCustomLaunchPropertiesClass(CustomLaunchProperties customProps) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.customProps = customProps;
    }

    /**
     * Return either APPLET or Selection parameter.
     * <p />
     * This method returns a list of presentation state selection when:<br/>
     * The query result (in map) contains more than 1 presentation states and selectPR is enabled.
     * <p/>
     * The 'meaning' of the returned properties is appointed by the property 'launchMode'!
     * <dl>
     * <dt>Property 'launchMode':</dt>
     * <dd>  applet: Properties contains applet parameter. </dd>
     * <dd>  pr_select: Properties contains presentation state list (key=IUID/value=Description). </dd>
     * <dd>  study_select: Properties contains list of studies(key=StudyIUID/value=Description). </dd>
     * <dd>  empty: Properties contains only CODE and ARCHIVE. </dd>
     * <dd>  error: Properties contains only MESSAGE and SEVERITY. </dd>
     * </dl>
     * @param map       Contains query Results (each map value is a list of DicomObject.
     * @param ignorePR  If true: Ignore DicomObjects with SOPClassUID listed in psCUIDs.
     * @param selectPR  Enable/disable PresentationState Selection when result contains >1 PRs.
     * 
     * @return either APPLET or Selection parameter.
     */
    public Properties getProperties( InstanceContainer results, boolean ignorePR, boolean selectPR ) {
        Properties p = new Properties();
        p.setProperty(PROP_CODE, appletClass);
        p.setProperty(PROP_ARCHIVE, appletArchive);
        p.putAll(appletParameterMap);
        if ( results == null || results.isEmpty() ) {
            p.setProperty(PROP_LAUNCH_MODE, "empty");
            return p;
        } else if ( results.countStudies() > 1 ) { //more than one study found!
            return getStudySelectProperties(results);
        }
        Map mapSeries = results.getSeriesMap(); //get map with all series
        p.setProperty(PROP_LAUNCH_MODE, "applet");
        addParameterFromResult(p, (DicomObject)((Collection) mapSeries.values().iterator().next()).iterator().next() );
        
        StringBuffer sbSeries = new StringBuffer();
        StringBuffer sbPR = null;
        if ( !ignorePR && paraPresentationStates[0] != null ) {
            sbPR = new StringBuffer(paraPresentationStates[1]); //prefix
            appendDescription( sbPR, paraPresentationStates, null, 0);
        }
        int i = 1;
        List seriess;
        DicomObject dcm;
        String cuid;
        boolean imgSeries;
        boolean noSelect = true; //set to false if PR selection is necessary (selectPR=true and more than one PR)
        Properties prProps = new Properties();
        for (  Iterator iter = mapSeries.values().iterator(); iter.hasNext() ; ){
            seriess = (List) iter.next();
            if (instanceComparator != null)
            	Collections.sort(seriess, instanceComparator);
            sbSeries.setLength(0);
            Iterator iterInstances = seriess.iterator();
            dcm = (DicomObject) iterInstances.next();
            if (paraSeriesInstances[0] != null) {
                appendDescription( sbSeries, paraSeriesInstances, dcm, i);
            }
            imgSeries = false;
            while (  dcm != null ) {
                cuid = dcm.getString(Tag.SOPClassUID);
                if ( noSelect && (imageCUIDs == null || imageCUIDs.values().contains(cuid)) ) {
                    sbSeries.append(dcm.getString(Tag.SOPInstanceUID)).append(paraSeriesInstances[4]);//list seperator
                    imgSeries = true;
                } else if ( !ignorePR && psCUIDs.values().contains(cuid)) {
                    if ( selectPR ) {
                        String descr = dcm.getString(Tag.ContentDescription)+"("+
                        dcm.getDate(Tag.PresentationCreationDate,Tag.PresentationCreationTime)+") - "+
                        dcm.getString(Tag.ContentLabel)+" - "+dcm.getString(Tag.ContentCreatorName);
                        prProps.setProperty( dcm.getString(Tag.SOPInstanceUID), descr );
                        noSelect = prProps.size() < 2;
                    }
                    if ( noSelect ) {
                        sbPR.append(dcm.getString(Tag.SOPInstanceUID)).append(paraPresentationStates[4]);//list seperator
                    }
                }
                dcm = iterInstances.hasNext() ? (DicomObject) iterInstances.next() : null;
            }
            if ( imgSeries ) {
                sbSeries.append(paraSeriesInstances[5]);//postfix
                p.setProperty(paraSeriesInstances[0]+i++, sbSeries.toString());
            }
        }
        if ( noSelect ) {
            if ( sbPR != null ) { 
                sbPR.append(paraPresentationStates[5]);
                p.setProperty(paraPresentationStates[0], sbPR.toString());
            }
            if ( customProps != null ) {
                customProps.addCustomProperties(p,mapSeries);
            }
            return p;
        } else {
            prProps.setProperty(PROP_LAUNCH_MODE, "pr_select");
            return prProps;
        }
    }
    
    private Properties getStudySelectProperties(InstanceContainer results) {
        Properties p = new Properties();
        p.setProperty("launchMode", "study_select");
        DicomObject obj;
        String desc;
        Map map;
        for ( Iterator iter = results.iterateStudies() ; iter.hasNext() ; ) {
            map = ((Map) iter.next());
            if ( ! map.isEmpty()) {
                obj = (DicomObject) ((Collection) map.values().iterator().next() ).iterator().next();
                desc = obj.getString(Tag.PatientName)+"("+obj.getString(Tag.PatientBirthDate)+") Descr:"+
                    obj.getString(Tag.StudyDescription)+" UID:"+obj.getString(Tag.StudyInstanceUID);
                p.setProperty( obj.getString(Tag.StudyInstanceUID), desc);
            }
        }
        return p;
    }

    private void appendDescription(StringBuffer sb, String[] sa, DicomObject dcm, int idx) {
        sb.append(sa[1]);//descr. part
        String s = sa[2];//descr. value
        if ( s.length() > 0 ) {
            if ( s.charAt(0) == '#') {
                if ( s.length() > 1 ) {
                    char ch = s.charAt(1);
                    if ( ch != '-' && ( ch < '0' || ch > '9') ) {
                        sb.append((char)(ch+idx-1));
                    } else {
                        sb.append(idx+Integer.parseInt(s.substring(1)));
                    }
                } else {
                    sb.append(idx);
                }
            } else {
                if ( dcm != null )
                    sb.append( dcm.getString(Tag.toTagPath(s))); //value from DICOM attribute
            }
        }
        sb.append( sa[3] );//delimiter description part - uid list
    }

    /**
     * @param object
     */
    private void addParameterFromResult(Properties p, DicomObject dcm) {
        Map.Entry entry;
        String value;
        for ( Iterator iter = result2appletParameterMap.entrySet().iterator() ; iter.hasNext() ; ) {
            entry = (Map.Entry) iter.next();
            value = dcm.getString(Tag.toTag((String)entry.getValue()));
            p.setProperty((String) entry.getKey(), value == null ?  "":value);
        }
    }
    
    
    

}
