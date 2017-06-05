/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.admin;

import org.dcm4chex.archive.web.maverick.Errable;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 1859 $ $Date: 2005-07-15 18:55:37 +0800 (周五, 15 7月 2005) $
 */
public class UserChgPwdSubmitCtrl extends Errable
{
	private String cancelPar = null;
	private String userID;
	private String oldPasswd;
	private String passwd;
	private String passwd1;

	/**
	 * @param userID The userID to set.
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}
	/**
	 * @param oldPasswd The oldPasswd to set.
	 */
	public void setOldPasswd(String oldPasswd) {
		this.oldPasswd = oldPasswd;
	}
	/**
	 * @param passwd The passwd to set.
	 */
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	/**
	 * @param passwd1 The passwd1 to set.
	 */
	public void setPasswd1(String passwd1) {
		this.passwd1 = passwd1;
	}
	/**
	 * @param cancelPar The cancelPar to set.
	 */
	public void setCancel(String cancelPar) {
		this.cancelPar = cancelPar;
	}
	
	protected String perform() throws Exception
	{
		UserAdminModel model = UserAdminModel.getModel( getCtx().getRequest() );
		model.setPopupMsg(null);
		if ( cancelPar == null ) {
			if ( passwd.equals(passwd1)) {
				if ( passwd.trim().length() > 2 ) {
					if ( ! model.changePassword( userID, oldPasswd, passwd ) ) {
						model.setPopupMsg("Password not changed! Retry with correctly typed old and new password.");
						return "chgpwd_error";
					}
				} else {
					model.setPopupMsg("Password is too short! You have to type a password with at least 3 characters!");
					return "chgpwd_error";
				}
			} else {
				model.setPopupMsg("Password mismatch! You have to type the same password in both new password field!");
				return "chgpwd_error";
			}
		}
		
		return "success";
	}		
}
