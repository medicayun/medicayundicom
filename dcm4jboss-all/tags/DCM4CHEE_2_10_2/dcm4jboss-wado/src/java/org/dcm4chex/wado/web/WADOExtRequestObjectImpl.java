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

package org.dcm4chex.wado.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4che.dict.UIDs;
import org.dcm4chex.wado.common.WADOExtRequestObject;
import org.dcm4chex.wado.mbean.ExtendedWADOService;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WADOExtRequestObjectImpl extends WADORequestObjectImpl implements WADOExtRequestObject {

	private static Logger log = Logger.getLogger( ExtendedWADOService.class.getName() );
	
	private String reqType;
	private String serviceType;
	private String level;
	
	/**
	 * Creates a WADORequestObjectImpl instance configured with http request.
	 * 
	 * @param request The http request.
	 */
	public WADOExtRequestObjectImpl( String reqType, HttpServletRequest request ) {
		super( request );
		this.reqType = reqType;
		serviceType = request.getParameter("serviceType");
		level = request.getParameter("level");
	}
	
	
	/** 
	 * Checks this request object and returns an error code.
	 * <p>
	 * <DL>
	 * <DT>Following checks:</DT>
	 * <DD>  requestType must be "WADOext"</DD>
	 * <DD>  studyUID must be set</DD>
	 * <DD>  if transferSyntax is set: check if only one of explicitVRLittleEndian or deflatedExplicitVRLittleEndian is set</DD>
	 * </DL>
	 * 
	 * @return OK if it is a valid WADO request or an error code.
	 */
	public int checkRequest() {
		
		if ( getRequestType() == null || !reqType.equalsIgnoreCase(getRequestType()) ||
				serviceType == null ) {
			setErrorMsg("Extended WADO: URL not valid! Parameters must be set: requestType="+reqType+" and serviceType"); 
 			return INVALID_WADO_URL;
		}
		List l = getContentTypes();
		if ( l != null && l.size() > 0) {
			if ( l.size() > 1 ) {
				setErrorMsg("Extended WADO: Invalid contentType parameter! Only one content type (application/dicom) is allowed!");
				return INVALID_CONTENT_TYPE;
			}
			if ( ! "application/dicom".equals(l.get(0)) ) {
				setErrorMsg("Extended WADO: Invalid contentType parameter! Only application/dicom is allowed!");
				return INVALID_CONTENT_TYPE;
			}
		}
		String ts = getTransferSyntax();
		if ( ts != null && !UIDs.ExplicitVRLittleEndian.equals(ts) && !UIDs.DeflatedExplicitVRLittleEndian.equals(ts)) {
			setErrorMsg("Extended WADO: Invalid transferSyntax parameter! Only 1.2.840.10008.1.2.1(explicit VR little endian) or 1.2.840.10008.1.2.1.99(deflated explicit VR little endian) are allowed!");
			return INVALID_TRANSFER_SYNTAX;
		}
		
		return EXTENDED_WADO_URL;
	}

	/**
	 * @return Returns the serviceType.
	 */
	public String getServiceType() {
		return serviceType;
	}
	/**
	 * @param serviceType The serviceType to set.
	 */
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	/**
	 * @return Returns the level.
	 */
	public String getLevel() {
		return level;
	}
	/**
	 * @param level The level to set.
	 */
	public void setLevel(String level) {
		this.level = level;
	}
	
	public String toString()
	{
		String str = null;
		String uid = null;
		if ( (uid = getObjectUID()) != null ) {
			str = "objectUID: " + uid;
		} else if ( (uid = getSeriesUID()) != null ) {
			str = "seriesUID: " + uid;
		} else if ( (uid = getStudyUID()) != null ) {
			str = "studyUID: " + uid;
		} else
			str = "UNKNOWN UID";
		
		return str + ", level: " + level;
	}
}
