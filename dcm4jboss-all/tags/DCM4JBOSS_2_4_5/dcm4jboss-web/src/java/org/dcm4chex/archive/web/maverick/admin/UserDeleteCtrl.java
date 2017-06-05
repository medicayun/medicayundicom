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
 * @version $Revision: 1812 $ $Date: 2005-06-23 19:13:28 +0800 (周四, 23 6月 2005) $
 */
public class UserDeleteCtrl extends Errable
{
	private String userID;

	/**
	 * @return Returns the userID.
	 */
	public String getUserID() {
		return userID;
	}
	/**
	 * @param userID The userID to set.
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}
	protected String perform() throws Exception
	{
		UserAdminModel model = UserAdminModel.getModel( getCtx().getRequest() );
		if ( !model.isAdmin() ) return "error";
		model.deleteUser( userID );//Any failure will be marked in popUpMessage!
		return "success";
	}		
}
