/*
 * Created on 21.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.admin;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.UserManager;
import org.dcm4chex.archive.ejb.interfaces.UserManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;


/**
 * @author franz.willer
 *
 * The Model for Media Creation Managment WEB interface.
 */
public class UserAdminModel {

	
	/** Errorcode: no error */
    public static final String NO_ERROR ="OK";

    private String errorCode = null;
	private boolean admin = false;
	private List userList = null;
	private DCMUser editUser = null;

	private static final String USERMODEL_ATTR_NAME = "UserAdminModel";

	private static Logger log = Logger.getLogger(UserAdminModel.class.getName());

	private String popupMsg;
	
	/**
	 * Creates the model.
	 * <p>
	 * Perform an initial media search with the default filter. <br>
	 * (search for all media with status COLLECTING)
	 * <p>
	 * performs an initial availability check for MCM_SCP service.
	 */
	private UserAdminModel(boolean admin) {
		this.admin = admin;
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
				model = new UserAdminModel(request.isUserInRole("WebAdmin"));
				request.getSession().setAttribute(USERMODEL_ATTR_NAME, model);
				model.setErrorCode( NO_ERROR ); //reset error code
		}
		return model;
	}

	/**
	 * @return Returns true if the user have WebAdmin role.
	 */
	public boolean isAdmin() {
		return admin;
	}

	/**
	 * Set the error code of this model.
	 * 
	 * @param errorCode The error code
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode  = errorCode;
		
	}
	
	/**
	 * Get current error code of this model.
	 * 
	 * @return error code.
	 */
	public String getErrorCode() {
		return errorCode;
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
		if ( userList.contains( user ) ) {
			log.warn("Cant create user! UserID "+user.getUserID()+" already exists!");
			this.popupMsg = "Cant create user! UserID "+user.getUserID()+" already exists!";
			editUser = user;
			return null;
		} else {
			String userID = user.getUserID();
			try {
				UserManager manager = lookupUserManager();
				manager.addUser(user.getUserID(),passwd, user.roles());
			} catch (Exception e) {
				log.error("Cant create new user "+userID+" with roles "+user.roles(), e);
				popupMsg = "Cant create user "+userID+"! Exception:"+e.getMessage();
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
	public DCMUser updateUser( String oldUserID, DCMUser user ) {
		if ( !userList.contains( user ) ) {
			log.error("User doesnt exist! UserID "+oldUserID);
			return null;
		} else {
			String userID = user.getUserID();
			Collection roles = user.roles();
			String role;
			try {
				UserManager manager = lookupUserManager();
				manager.updateUser(oldUserID, roles);
			} catch (Exception e) {
				log.error("Cant update user "+oldUserID+" with roles "+roles, e);
				popupMsg = "Cant create user "+oldUserID+"! Exception:"+e.getMessage();
				return null;
			}
			log.info("User "+oldUserID+" updated:"+user);
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
		popupMsg = "Cant change password for user "+user;
		return false;
	}
	
	/**
	 * Returns the user object for given userID or null if user doesnt exist.
	 * 
	 * @param userID
	 * @return user object or null.
	 */
	public DCMUser getUser( String userID ) {
		if ( userID == null ) return null;
		return (DCMUser) userList.get( userList.indexOf( new DCMUser(userID, null) ) );
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
	
	public void selectEditUser( String userID ) {
		editUser = getUser( userID );
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
	public boolean deleteUser( String userID ) {
		try {
			this.lookupUserManager().removeUser( userID );
		} catch (Exception e) {
			log.error("Cant delete user "+userID, e );
			this.popupMsg = "Cant delete user "+userID+"! Reason:"+e.getMessage();
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
			popupMsg = "Cant query user list! Exception:"+x.getMessage();
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
