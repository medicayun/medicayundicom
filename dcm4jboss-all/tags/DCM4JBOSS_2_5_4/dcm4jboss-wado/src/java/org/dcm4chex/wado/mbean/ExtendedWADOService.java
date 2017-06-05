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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.dcm4chex.wado.common.WADOExtRequestObject;
import org.dcm4chex.wado.common.WADORequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.mbean.cache.WADOCacheImpl;

/**
 * @author franz.willer 
 *
 * The MBean to manage the extended WADO service.
 * <p>
 * This class use ExtendedWADOSupport for the WADOext methods.
 */
public class ExtendedWADOService extends AbstractCacheService {

	private ExtendedWADOSupport support = new ExtendedWADOSupport( this.server );

	public ExtendedWADOService() {
		cache = WADOCacheImpl.getWADOExtCache();
	}
	
	/**
	 * Getter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @return Name of the MBean
	 */
	public String getFileSystemMgtName() {
		return support.getFileSystemMgtName().toString();
	}
	
	/**
	 * Setter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @param Name of the MBean
	 */
	public void setFileSystemMgtName( String name ) {
		try {
			ObjectName on = new ObjectName( name );
			support.setFileSystemMgtName( on );
		} catch (MalformedObjectNameException e) {
		}
	}
	
	/**
	 * Getter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @return Name of the MBean
	 */
	public String getStudyInfoServiceName() {
		return support.getStudyInfoServiceName().toString();
	}
	
	/**
	 * Setter for the name of the FileSystemMgt MBean.
	 * <p>
	 * This bean is used to locate the DICOM file.
	 *  
	 * @param Name of the MBean
	 */
	public void setStudyInfoServiceName( String name ) {
		try {
			ObjectName on = new ObjectName( name );
			support.setStudyInfoServiceName( on );
		} catch (MalformedObjectNameException e) {
		}
	}
	
	/**
	 * @return Returns the cacheEnabled.
	 */
	public boolean isCacheEnabled() {
		return support.isCacheEnabled();
	}
	/**
	 * @param cacheEnabled The cacheEnabled to set.
	 */
	public void setCacheEnabled(boolean cacheEnabled) {
		support.setCacheEnabled(cacheEnabled);
	}
	
	public int getNumberOfCacheFolders() {
		return support.getNumberOfCacheFolders();
	}
	/**
	 * @param numberOfCacheFolders The numberOfCacheFolders to set.
	 */
	public void setNumberOfCacheFolders(int numberOfCacheFolders) {
		support.setNumberOfCacheFolders(numberOfCacheFolders);
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
		return support.getWADOObject( (WADOExtRequestObject)reqVO );
	}

}
