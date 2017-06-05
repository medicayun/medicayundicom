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

package org.dcm4chex.archive.web.maverick;

import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.archive.web.maverick.admin.DCMUser;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2274 $ $Date: 2006-02-14 18:41:42 +0800 (周二, 14 2月 2006) $
 * @since 13.02.2006
 */
public abstract class BasicFormModel {

    public static final String NO_ERROR ="OK";

    private final boolean admin;
    private boolean mcmUser;
    private boolean datacareUser;
    
    /** Error code for rendering message. */
    private String errorCode = NO_ERROR;
    
    /** Popup message */
    private String popupMsg = null;
    
	protected BasicFormModel( HttpServletRequest request ) {
    	admin = request.isUserInRole(DCMUser.WEBADMIN);
        datacareUser = request.isUserInRole(DCMUser.DATACARE_USER) || admin;
        mcmUser = request.isUserInRole(DCMUser.MCMUSER);
    }
	
	public String getModelName() { return "BASIC"; }

	/**
	 * @return Returns the admin.
	 */
	public boolean isAdmin() {
		return admin;
	}
	
    
	/**
	 * @return Returns the datacareUser.
	 */
	public boolean isDatacareUser() {
		return datacareUser;
	}
	/**
	 * @return Returns the mcmUser.
	 */
	public boolean isMcmUser() {
		return mcmUser;
	}
    public final String getErrorCode() {
    	return errorCode;
    }
    
    
    public final void setErrorCode( String err ) {
    	errorCode = err;
    }

	/**
	 * @return Returns the popupMsg.
	 */
	public String getPopupMsg() {
		return popupMsg;
	}
	/**
	 * @param popupMsg The popupMsg to set.
	 */
	public void setPopupMsg(String popupMsg) {
		this.popupMsg = popupMsg;
	}

}