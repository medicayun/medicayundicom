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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.management.ObjectName;

import org.dcm4che.dict.UIDs;
import org.dcm4chex.wado.common.WADORequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;

/**
 * @author franz.willer 
 *
 * The MBean to manage the WADO service.
 * <p>
 * This class use WADOSupport for the WADO methods and WADOCache for caching jpg images.
 */
public class WADOService extends AbstractCacheService {

	private WADOSupport support = new WADOSupport( this.server );

	
	public WADOService() {
		cache = WADOCacheImpl.getWADOCache();
	}
	
	public String getDirectoryTree() {
		return ((WADOCacheImpl) cache).getDirectoryTree();
	}

	public void setDirectoryTree(String primes) {
		((WADOCacheImpl) cache).setDirectoryTree(primes);
	}

	public void computeDirectoryStructure(long cacheSize, long fileSize, int maxSubDirPerDir ) {
		((WADOCacheImpl) cache).computeDirectoryStructure(cacheSize, fileSize, maxSubDirPerDir);
	}
	
	/**
	 * @return Returns the clientRedirect.
	 */
	public boolean isClientRedirect() {
		return cache.isClientRedirect();
	}
	/**
	 * @param clientRedirect The clientRedirect to set.
	 */
	public void setClientRedirect(boolean clientRedirect) {
		cache.setClientRedirect( clientRedirect );
	}
	/**
	 * @return Returns the redirectCaching.
	 */
	public boolean isRedirectCaching() {
		return cache.isRedirectCaching();
	}
	/**
	 * @param redirectCaching The redirectCaching to set.
	 */
	public void setRedirectCaching(boolean redirectCaching) {
		cache.setRedirectCaching( redirectCaching );
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
	 * @return Returns the useTransferSyntaxOfFileAsDefault.
	 */
	public boolean isUseTransferSyntaxOfFileAsDefault() {
		return support.isUseTransferSyntaxOfFileAsDefault();
	}
	/**
	 * Set default transfer syntax option.
	 * <p>
	 * If true use the TS from file.<br>
	 * If false use Explicit VR littlle Endian (as defined in part 18)
	 *  
	 * @param b If true use TS from file.
	 */
	public void setUseTransferSyntaxOfFileAsDefault(boolean b) {
		support.setUseTransferSyntaxOfFileAsDefault(b);
	}
	
	/**
	 * Set URL to XSLT stylesheet that should be used to transform DICOM SR to HTML document.
	 * 
	 * @return
	 */
	public String getHtmlXslURL() {
		return support.getHtmlXslURL();
	}
	
	public void setHtmlXslURL(String htmlXslURL) {
		support.setHtmlXslURL(htmlXslURL);
	}

	/**
	 * Set URL to XSLT stylesheet that should be used to transform DICOM SR to XHTML document.
	 * 
	 * @return
	 */
	public String getXHtmlXslURL() {
		return support.getXHtmlXslURL();
	}
	
	public void setXHtmlXslURL(String htmlXslURL) {
		support.setXHtmlXslURL(htmlXslURL);
	}
	
	/**
	 * Set URL to XSLT stylesheet that should be used to transform DICOM SR to xml document.
	 * 
	 * @return
	 */
	public String getXmlXslURL() {
		return support.getXmlXslURL();
	}
	
	public void setXmlXslURL(String xslURL) {
		support.setXmlXslURL(xslURL);
	}
	
	public void clearTemplateCache() {
		support.clearTemplateCache();
	}
	
	/**
	 * Returns a String with all defined SOP Class UIDs that are used to find text (SR) documents.
	 * <p>
	 * The uids are separated with line separator.
	 * 
	 * @return SOP Class UIDs to find ECG related files.
	 */
	public String getTextSopCuids() {
		Map uids = support.getTextSopCuids();
		if ( uids == null || uids.isEmpty() ) return "";
		StringBuffer sb = new StringBuffer( uids.size() << 5);//StringBuffer initial size: nrOfUIDs x 32
		Iterator iter = uids.keySet().iterator();
		while ( iter.hasNext() ) {
			sb.append(iter.next()).append(System.getProperty("line.separator", "\n"));
		}
		return sb.toString();
	}
	
	/**
	 * Set a list of SOP Class UIDs that are used to find text (SR) documents.
	 * <p>
	 * The UIDs are separated with line separator.
	 * 
	 * @param sopCuids String with SOP class UIDs seperated with '|'
	 */
	public void setTextSopCuids( String sopCuids ) {
		
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
		support.setTextSopCuids( map );
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
	
	public String getDisabledAuditLogHosts() {
		Set s = support.getDisabledAuditLogHosts();
		if ( s == null ) return "ALL";
		if ( s.isEmpty() ) return "NONE";
		StringBuffer sb = new StringBuffer(s.size()<<4);
		for ( Iterator it = s.iterator() ; it.hasNext() ;){
			sb.append(it.next()).append(System.getProperty("line.separator", "\n"));
		}
		return sb.toString();
	}
	public void setDisabledAuditLogHosts(String disabledAuditLogHosts) {
		if ( "ALL".equals(disabledAuditLogHosts) ) {
			support.setDisabledAuditLogHosts(null);
		} else {
			Set disabledHosts = new HashSet();
			if ( !"NONE".equals(disabledAuditLogHosts)) {
				StringTokenizer st = new StringTokenizer(disabledAuditLogHosts, "\r\n;");
				while ( st.hasMoreTokens() ) {
					disabledHosts.add(st.nextElement());
				}
			}
			support.setDisabledAuditLogHosts(disabledHosts);
		}
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
	 * @return Returns the extendedWADOAllowed.
	 */
	public boolean isExtendedWADOAllowed() {
		return support.isExtendedWADOAllowed();
	}
	/**
	 * @param extendedWADOAllowed The extendedWADOAllowed to set.
	 */
	public void setExtendedWADOAllowed(boolean extendedWADOAllowed) {
		support.setExtendedWADOAllowed(extendedWADOAllowed);
	}
	
	/**
	 * @return Returns the extendedWADORequestType.
	 */
	public String getExtendedWADORequestType() {
		return support.getExtendedWADORequestType();
	}
	/**
	 * @param extendedWADORequestType The extendedWADORequestType to set.
	 */
	public void setExtendedWADORequestType(String extendedWADORequestType) {
		support.setExtendedWADORequestType( extendedWADORequestType );
	}
	
	/**
	 * Get the requested DICOM object as File packed in a WADOResponseObject.
	 * <p>
	 *  
	 * @param reqVO The request parameters packed in an value object.
	 * 
	 * @return The value object containing the retrieved object or an error.
	 */
	public WADOResponseObject getWADOObject( WADORequestObject reqVO ) {
		return support.getWADOObject( reqVO );
	}

}
