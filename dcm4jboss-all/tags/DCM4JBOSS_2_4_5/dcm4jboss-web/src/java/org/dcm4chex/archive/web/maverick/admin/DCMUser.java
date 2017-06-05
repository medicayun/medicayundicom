/*
 * Created on 21.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.admin;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DCMUser {

	public static final String JBOSSADMIN = "JBossAdmin";
	public static final String WEBADMIN = "WebAdmin";
	public static final String WEBUSER = "WebUser";
	public static final String ARRUSER = "arr-user";
	
	private String userID;
	private Collection roles = new ArrayList();
	
	public DCMUser( String userID, Collection roles ) {
		if ( userID == null ) {
			throw new IllegalArgumentException("Cant create DCMUser! UserID must not be null!");
		}
		this.userID = userID;
		if ( roles != null )
			this.roles = roles;
	}
	
	public void setUserID( String id ) {
		userID = id;
	}
	
	public void setRole(String role, boolean enable) {
		if ( enable )
			addRole(role);
		else
			removeRole(role);
	}
	public void addRole( String role ) {
		if ( ! roles.contains(role ) ) {
			roles.add(role);
			if ( role.equals(WEBADMIN) && !roles.contains(WEBUSER) ) {//ensures that WebAdmin is also WebUser (to get the web pages)
				roles.add(WEBUSER);
			}
		}
	}

	public void removeRole( String role ) {
		roles.remove(role);
	}
	
	public boolean isInRole( String role ) {
		return roles.contains( role );
	}
	
	public Collection roles() { //Dont use getter to avoid marshalling from maverick.
		return roles;
	}
	
	/**
	 * @return Returns the roles.
	 */
	public boolean isWebUser() {
		return roles.contains(WEBUSER);
	}
	/**
	 * @return Returns the roles.
	 */
	public boolean isWebAdmin() {
		return roles.contains(WEBADMIN);
	}
	/**
	 * @return Returns the roles.
	 */
	public boolean isJBossAdmin() {
		return roles.contains(JBOSSADMIN);
	}
	/**
	 * @return Returns the roles.
	 */
	public boolean isArrUser() {
		return roles.contains(ARRUSER);
	}
	/**
	 * @return Returns the userID.
	 */
	public String getUserID() {
		return userID;
	}
	
	/**
	 * Returns simple description of this object.
	 */
	public String toString() {
		return "UserID:"+userID+" roles:"+roles;	
	}
	
	/**
	 * Returns true if parameter is a DCMUser object with same userID as this object.
	 * <p>
	 * This method returns true even roles are equal or not!
	 * 
	 * @param user The object to check equality.
	 * 
	 * @return true if userID is equal.
	 */
	public boolean equals( Object user ) {
		if ( user != null || (user instanceof DCMUser) ) {
			return userID.equals( ((DCMUser)user).getUserID() );
		} 
		return false;
	}
	
	/**
	 * Returns hashCode of userID String object.
	 * 
	 * @return Hashcode of this object.
	 */
	public int hashCode() {
		return userID.hashCode();
	}
}
