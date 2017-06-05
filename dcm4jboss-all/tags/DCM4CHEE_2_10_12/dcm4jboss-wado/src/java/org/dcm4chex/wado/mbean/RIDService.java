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

package org.dcm4chex.wado.mbean;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.xml.transform.TransformerConfigurationException;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;
//import org.dcm4chex.archive.notif.Export;
import org.dcm4chex.archive.notif.RIDExport;
import org.dcm4chex.wado.common.RIDRequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;
import org.jboss.system.ServiceMBeanSupport;
import org.xml.sax.SAXException;

/**
 * @author franz.willer
 *
 * The MBean to manage the IHE RID (Retrieve Information for Display service.
 * <p>
 * This class use RIDSupport for the Retrieve Information for Display methods and WADOCache for caching documents.
 */
public class RIDService extends AbstractCacheService  {

	private RIDSupport support = new RIDSupport( this );
	private float waveformCorrection = 1f;
	
	public RIDService() {
		cache = WADOCacheImpl.getRIDCache();
	}

	public String getDirectoryTree() {
		return ((WADOCacheImpl) cache).getDirectoryTree();
	}

	public void setDirectoryTree(String primes) {
		((WADOCacheImpl) cache).setDirectoryTree(primes);
	}

	public float getWaveformCorrection() {
		return waveformCorrection;
	}
	public void setWaveformCorrection(float waveformCorrection) {
		this.waveformCorrection = waveformCorrection;
	}
	
	public String getWadoURL() {
		return support.getWadoURL();
	}
	public void setWadoURL( String wadoURL ) {
		support.setWadoURL( wadoURL );
	}
	
	public String getRIDSummaryXsl() {
		String s = support.getRIDSummaryXsl();
		if ( s == null ) s = "";
		return s;
	}
	
	public void setRIDSummaryXsl( String xslFile ) {
		if ( xslFile != null && xslFile.trim().length() < 1 ) xslFile = null;
		support.setRIDSummaryXsl( xslFile );
	}
	
	/**
	 * @return Returns the useXSLInstruction.
	 */
	public boolean isUseXSLInstruction() {
		return support.isUseXSLInstruction();
	}
	/**
	 * @param useXSLInstruction The useXSLInstruction to set.
	 */
	public void setUseXSLInstruction(boolean useXSLInstruction) {
		support.setUseXSLInstruction( useXSLInstruction );
	}
	
	/**
	 * @return Returns the useOrigFile.
	 */
	public boolean isUseOrigFile() {
		return support.isUseOrigFile();
	}
	/**
	 * @param useOrigFile The useOrigFile to set.
	 */
	public void setUseOrigFile(boolean useOrigFile) {
		support.setUseOrigFile(useOrigFile);
	}

	/**
	 * Returns a String with all defined SOP Class UIDs that are used to find ECG documents.
	 * <p>
	 * The uids are seperated with '|'.
	 * 
	 * @return SOP Class UIDs to find ECG related files.
	 */
	public String getECGSopCuids() {
		Map uids = support.getECGSopCuids();
		if ( uids == null || uids.isEmpty() ) return "";
		StringBuffer sb = new StringBuffer( uids.size() << 5);//StringBuffer initial size: nrOfUIDs x 32
		Iterator iter = uids.keySet().iterator();
		while ( iter.hasNext() ) {
			sb.append(iter.next()).append(System.getProperty("line.separator", "\n"));
		}
		return sb.toString();
	}
	
	/**
	 * Set a list of SOP Class UIDs that are used to find ECG documents.
	 * <p>
	 * The UIDs are seperated with '|'.
	 * 
	 * @param sopCuids String with SOP class UIDs seperated with '|'
	 */
	public void setECGSopCuids( String sopCuids ) {
		
        StringTokenizer st = new StringTokenizer(sopCuids, "\r\n;");
        String uid,name;
        Map map = new TreeMap();
        int i = 0;
        while ( st.hasMoreTokens() ) {
        	uid = st.nextToken().trim();
    		name = uid;
        	if ( isDigit(uid.charAt(0) ) ) {
        		if ( ! UIDs.isValid(uid) ) 
        			throw new IllegalArgumentException("UID "+uid+" isn't a valid UID!");
        	} else {
        		uid = UIDs.forName( name );
        	}
        	map.put(name,uid);
        }
		support.setECGSopCuids( map );
	}

	public boolean isEncapsulatedPDFSupport() {
		return support.isEncapsulatedPDFSupport();
	}
	public void setEncapsulatedPDFSupport(boolean encapsulatedPDFSupport) {
		support.setEncapsulatedPDFSupport(encapsulatedPDFSupport);
	}
	
	public String getRadiologyConceptNames() {
		return support.getRadiologyConceptNames();
	}
	public void setRadiologyConceptNames( String conceptNames ) {
		support.setRadiologyConceptNames( conceptNames );
	}

	public String getCardiologyConceptNames() {
		return support.getCardiologyConceptNames();
	}
	public void setCardiologyConceptNames( String conceptNames ) {
		support.setCardiologyConceptNames( conceptNames );
	}

	public String getRadiologyPDFConceptCodeNames() {
		return support.getRadiologyPDFConceptCodeNames();
	}
	
	public void setRadiologyPDFConceptCodeNames( String conceptNames ) {
		support.setRadiologyPDFConceptNameCodes( conceptNames );
	}
	public String getCardiologyPDFConceptCodeNames() {
		return support.getCardiologyPDFConceptCodeNames();
	}
	
	public void setCardiologyPDFConceptCodeNames( String conceptNames ) {
		support.setCardiologyPDFConceptNameCodes( conceptNames );
	}
	public String getECGPDFConceptCodeNames() {
		return support.getECGPDFConceptCodeNames();
	}
	
	public void setECGPDFConceptCodeNames( String conceptNames ) {
		support.setECGPDFConceptNameCodes( conceptNames );
	}
	
    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
	
	/**
	 * Getter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @return Name of the MBean
	 */
	public ObjectName getFileSystemMgtName() {
		return support.getFileSystemMgtName();
	}
	
	/**
	 * Setter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @param Name of the MBean
	 */
	public void setFileSystemMgtName( ObjectName name ) {
			support.setFileSystemMgtName( name );
	}

	/**
	 * Set the name of the AuditLogger MBean.
	 * <p>
	 * This bean is used to create Audit Logs.
	 * 
	 * @param name The Audit Logger Name to set.
	 */
	public void setAuditLoggerName( ObjectName name ) {
		support.setAuditLoggerName( name );
	}

	/**
	 * Get the name of the AuditLogger MBean.
	 * <p>
	 * This bean is used to create Audit Logs.
	 * 
	 * @return Returns the name of the Audit Logger MBean.
	 */
	public ObjectName getAuditLoggerName() {
		return support.getAuditLoggerName();
	}
	
	/**
	 * Get the requested Summary information object as Stream packed in a WADOResponseObject.
	 * <p>
	 *  
	 * @param reqVO The request parameters packed in an value object.
	 * 
	 * @return The value object containing the retrieved object or an error.
	 * @throws SQLException
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerConfigurationException
	 */
	public WADOResponseObject getRIDSummary( RIDRequestObject reqVO ) throws SQLException, TransformerConfigurationException, IOException, SAXException {
		if ( log.isDebugEnabled() ) log.debug( "getRIDSummary:"+reqVO );
		return support.getRIDSummary( reqVO );
	}
	
	/**
	 * Get the requested Document object as Stream packed in a WADOResponseObject.
	 * <p>
	 *  
	 * @param reqVO The request parameters packed in an value object.
	 * 
	 * @return The value object containing the retrieved object or an error.
	 */
	public WADOResponseObject getRIDDocument( RIDRequestObject reqVO ) {
		if ( log.isDebugEnabled() ) log.debug("getRIDDocument:"+reqVO );
		return support.getRIDDocument( reqVO );
	}
	
	public File getDocumentFile(String objectUID, String contentType) {
		return support.getDocumentFile(objectUID,contentType);
	}
	
	
    protected void sendExportNotification(RIDExport export) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(RIDExport.class.getName(), this, eventID);
        notif.setUserData(export);
        super.sendNotification(notif);
    }
    
}
