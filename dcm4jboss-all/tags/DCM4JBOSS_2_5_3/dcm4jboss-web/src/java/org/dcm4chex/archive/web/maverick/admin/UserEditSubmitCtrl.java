/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.admin;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.web.maverick.Dcm4JbossFormController;

/**
 * @author umberto.cappellini@tiani.com
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1860 $ $Date: 2005-07-15 18:58:22 +0800 (周五, 15 7月 2005) $
 */
public class UserEditSubmitCtrl extends Dcm4JbossFormController
{

	private String userHash = null;
	private String passwd = null;
	private String passwd1 = null;
	private DCMUser user = new DCMUser("",null);
	private String newPar = null;
	private String cancelPar = null;
	
	private static Logger log = Logger.getLogger(UserEditSubmitCtrl.class.getName());
	
	/**
	 * @param oldUserID The oldUserID to set.
	 */
	public void setUserHash(String hash) {
		this.userHash = hash;
	}
	/**
	 * @param cancelPar The cancelPar to set.
	 */
	public void setCancel(String cancelPar) {
		this.cancelPar = cancelPar;
	}
	/**
	 * @param newPar The newPar to set.
	 */
	public void setNew(String newPar) {
		this.newPar = newPar;
	}
	/**
	 * @param passwd The passwd to set.
	 */
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	/**
	 * @param passwd The passwd to set.
	 */
	public void setPasswd1(String passwd1) {
		this.passwd1 = passwd1;
	}
	/**
	 * @param role true means that this role is assigned to this user.
	 */
	public void setWebUser(boolean role) {
		user.setRole( DCMUser.WEBUSER, role);
	}
	/**
	 * @param role true means that this role is assigned to this user.
	 */
	public void setWebAdmin(boolean role) {
		user.setRole( DCMUser.WEBADMIN, role);
	}
	/**
	 * @param role true means that this role is assigned to this user.
	 */
	public void setJBossAdmin(boolean role) {
		user.setRole( DCMUser.JBOSSADMIN, role);
	}
	/**
	 * @param role true means that this role is assigned to this user.
	 */
	public void setArrUser(boolean role) {
		user.setRole( DCMUser.ARRUSER, role);
	}
	/**
	 * @param role true means that this role is assigned to this user.
	 */
	public void setMcmUser(boolean role) {
		user.setRole( DCMUser.MCMUSER, role);
	}
	/**
	 * @param userID The userID to set.
	 */
	public void setUserID(String userID) {
		log.info("setUserID:"+userID);
		user.setUserID( userID );
	}
	
	protected String perform() throws Exception
	{
		UserAdminModel model = UserAdminModel.getModel(this.getCtx().getRequest());
		model.setErrorCode("OK");
		model.setPopupMsg(null);
		if ( !model.isAdmin()) {
			log.warn("Illegal access to UserEditSubmitCtrl! User "+this.getCtx().getRequest().getUserPrincipal()+"is not in role WebAdmin!");
			return "error";
		}
		if ( cancelPar == null ) {
			if ( userHash != null ) {
				model.updateUser( Integer.parseInt(userHash), user );
			} else {
				String userID = user.getUserID();
				if ( userID == null || userID.trim().length() < 3 ) {
					model.setEditUser(user);
					model.setPopupMsg("UserID is too short! You have to type a user ID with at least 3 characters!");
					return "passwd_mismatch";
				}
				if ( passwd.equals(passwd1)) {
					if ( passwd.trim().length() > 2 ) {
						if ( model.createUser(user, passwd ) == null ) {
							return "passwd_mismatch";
						}
					} else {
						model.setEditUser(user);
						model.setPopupMsg("Password is too short! You have to type a password with at least 3 characters!");
						return "passwd_mismatch";
						
					}
				} else {
					model.setEditUser(user);
					model.setPopupMsg("Password mismatch! You have to type the same password in both password field!");
					return "passwd_mismatch";
				}
			}
		}
		return "success";
	}

}
