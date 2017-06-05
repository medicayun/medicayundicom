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

package org.dcm4chex.archive.web.maverick.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.UserManager;
import org.dcm4chex.archive.ejb.interfaces.UserManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.BasicFormModel;


/**
 * @author franz.willer
 *
 * The Model for Media Creation Managment WEB interface.
 */
public class UserAdminModel extends BasicFormModel {

	private List userList = null;
	private DCMUser editUser = null;

	private static final String USERMODEL_ATTR_NAME = "UserAdminModel";

	private static Logger log = Logger.getLogger(UserAdminModel.class.getName());

	
	/**
	 * Creates the model.
	 * <p>
	 * Perform an initial media search with the default filter. <br>
	 * (search for all media with status COLLECTING)
	 * <p>
	 * performs an initial availability check for MCM_SCP service.
	 */
	private UserAdminModel(HttpServletRequest request) {
		super(request);
	}
	
	/**
	 * Get the model for an http request.
	 * <p>
	 * Look in the session for an associated model via <code>MCMMODEL_ATTR_NAME</code><br>
	 * If there is no model stored in session (first request) a new model is created and stored in session.
	 * 
	 * @param request A http request.
	 * 
	 * @return The model for given request.
	 */
	public static final UserAdminModel getModel( HttpServletRequest request ) {
		UserAdminModel model = (UserAdminModel) request.getSession().getAttribute(USERMODEL_ATTR_NAME);
		if (model == null) {
				model = new UserAdminModel(request);
				request.getSession().setAttribute(USERMODEL_ATTR_NAME, model);
				model.setErrorCode( NO_ERROR ); //reset error code
		}
		return model;
	}

	public String getModelName() { return "UserAdmin"; }
	
	/**
	 * Get list of users.
	 * 
	 * @return List of DCMUser objects.
	 */
	public List getUserList() {
		if ( userList == null ) {
			try {
				queryUsers();
			} catch (Exception e) {
				log.error("Cant query list of users!",e);
			}
		}
		return userList;
	}
	
	/**
	 * create a new user.
	 * <p>
	 * returns the new created user or null if user cant be created.
	 * 
	 * @param userID The (unique) userID.
	 * @param passwd The password for the new user.
	 * @param roles The roles assigned to this user.
	 * 
	 * @return User object if user is created or null.
	 */
	public DCMUser createUser( DCMUser user, String passwd ) {
		if ( getUserList().contains( user ) ) {
			log.warn("Cant create user! UserID "+user.getUserID()+" already exists!");
			this.setPopupMsg("Cant create user! UserID "+user.getUserID()+" already exists!");
			editUser = user;
			return null;
		} else {
			String userID = user.getUserID();
			try {
				UserManager manager = lookupUserManager();
				manager.addUser(user.getUserID(),passwd, user.roles());
			} catch (Exception e) {
				log.error("Cant create new user "+userID+" with roles "+user.roles(), e);
				this.setPopupMsg("Cant create user "+userID+"! Exception:"+e.getMessage());
				return null;
			}
			log.info("User "+user+" created! roles:"+user.roles());
		}
		return user;
	}

	/**
	 * Update an existing user.
	 * 
	 * @param oldUserID UserID of the user to change.
	 * @param userID new userID.
	 * @param roles The roles that should be assigned to the user.
	 * 
	 * @return the new user object of the changed user or null.
	 */
	public DCMUser updateUser( int userHash, DCMUser user ) {
		DCMUser qUser = DCMUser.getQueryUser(userHash );
		int idx = getUserList().indexOf(qUser);
		if ( idx == -1 ) {
			log.error("User doesnt exist! UserID "+user.getUserID());
			return null;
		} else {
			String userID = ((DCMUser)getUserList().get(idx)).getUserID();
			Collection roles = user.roles();
			String role;
			try {
				UserManager manager = lookupUserManager();
				manager.updateUser(userID, roles);
			} catch (Exception e) {
				log.error("Cant update user "+userID+" with roles "+roles, e);
				this.setPopupMsg( "Cant update user "+userID+"! Exception:"+e.getMessage());
				return null;
			}
			log.info("User "+userID+" updated:"+user);
		}
		return user;
	}
	
	public boolean changePassword(String user, String oldPasswd, String newPasswd){
		try {
			if ( lookupUserManager().changePasswordForUser(user, oldPasswd, newPasswd) ) {
				log.info("Password changed of user "+user );
				return true;
			}
		} catch (Exception e) {
			log.error("Cant change password for user "+user);
		}
		this.setPopupMsg( "Cant change password for user "+user);
		return false;
	}
	
	/**
	 * Returns the user object for given userID or null if user doesnt exist.
	 * 
	 * @param userID
	 * @return user object or null.
	 */
	public DCMUser getUser( int userHash ) {
		return (DCMUser) getUserList().get( getUserList().indexOf( DCMUser.getQueryUser(userHash) ) );
	}

	/**
	 * Returns the 'edit' user.
	 * <p>
	 * This is the current user for update or create. This user can be used to prefill the 
	 * fields for userID and roles.
	 * 
	 * @return
	 */
	public DCMUser getEditUser() {
		return editUser;
	}
	
	public void selectEditUser( String userHash ) {
		editUser = userHash == null ? null : getUser( Integer.parseInt(userHash) );
	}
	
	public void setEditUser( DCMUser user ) {
		editUser = user;
	}
	
	/**
	 * Deletes given user.
	 * 
	 * @param userID The userID to delete
	 * 
	 * @return true if user is deleted, false otherwise.
	 */
	public boolean deleteUser( int userHash ) {
		String userID = getUser( userHash ).getUserID();
		try {
			this.lookupUserManager().removeUser( userID );
		} catch (Exception e) {
			log.error("Cant delete user "+userID, e );
			this.setPopupMsg( "Cant delete user "+userID+"! Reason:"+e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Get list of users from application server. 
	 * @throws Exception
	 */
	public void queryUsers() {
		userList = new ArrayList();
		try {
			UserManager manager = lookupUserManager();
			Iterator iterUsers = manager.getUsers().iterator();
			String user; 
			StringBuffer sbRoles;
			while ( iterUsers.hasNext() ) {
				user = (String) iterUsers.next();
				userList.add( new DCMUser( user, manager.getRolesOfUser( user ) ) );
			}
		} catch ( Exception x ) {
			log.error("Cant query user list!", x);
			this.setPopupMsg( "Cant query user list! Exception:"+x.getMessage());
		}
	}
	
	/**
	 * @param rolesOfUser
	 * @return
	 */
	private String getRolesString(Collection col) {
		if ( col == null ) return null;
		StringBuffer sb = new StringBuffer();
		Iterator iter = col.iterator();
		if ( iter.hasNext()) 
			sb.append( iter.next() );
		while ( iter.hasNext() ) {
			sb.append(",").append(iter.next());
		}
		return sb.toString();
	}

	/**
	 * Returns the UserManager bean.
	 * 
	 * @return The UserManager bean.
	 * @throws Exception
	 */
	protected UserManager lookupUserManager() throws Exception
	{
		UserManagerHome home =
			(UserManagerHome) EJBHomeFactory.getFactory().lookup(
					UserManagerHome.class,
					UserManagerHome.JNDI_NAME);
		return home.create();
	}			
	
}
