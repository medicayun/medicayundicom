/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.ejb.session;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;

import org.dcm4chex.archive.ejb.jdbc.AddRoleToUserCmd;
import org.dcm4chex.archive.ejb.jdbc.AddUserCmd;
import org.dcm4chex.archive.ejb.jdbc.QueryPasswordForUserCmd;
import org.dcm4chex.archive.ejb.jdbc.QueryRolesForUserCmd;
import org.dcm4chex.archive.ejb.jdbc.QueryUsersCmd;
import org.dcm4chex.archive.ejb.jdbc.RemoveRoleFromUserCmd;
import org.dcm4chex.archive.ejb.jdbc.RemoveUserCmd;
import org.dcm4chex.archive.ejb.jdbc.UpdatePasswordForUserCmd;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1813 $ $Date: 2005-06-23 19:16:55 +0800 (周四, 23 6月 2005) $
 * @since Jun 22, 2005
 * 
 * @ejb.bean name="UserManager" 
 *           type="Stateless"
 *           view-type="remote" 
 * 	         jndi-name="ejb/UserManager"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb:resource-ref res-name="jdbc/DS"
 * 	                 res-type="javax.sql.DataSource"
 * 					 res-auth="Container"
 * @jboss:resource-ref res-ref-name="jdbc/DS" 
 *                     jndi-name="java:/DefaultDS"
 *
 */
public abstract class UserManagerBean
		implements SessionBean {
		
	private static final String DB_JNDI_NAME = "java:comp/env/jdbc/DS";

	/**
     * @ejb.interface-method
     */
	public Collection getUsers() {
		try {
			QueryUsersCmd cmd = new QueryUsersCmd(DB_JNDI_NAME);
			try {
				cmd.execute();
				ArrayList users = new ArrayList();
				while (cmd.next()) {
					users.add(cmd.getUser());
				}
				return users;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public void addUser(String user, String passwd) {		
		try {
			AddUserCmd cmd = new AddUserCmd(DB_JNDI_NAME);
			try {
				cmd.setUser(user);
				cmd.setPassword(passwd);
				cmd.execute();
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public void addUser(String user, String passwd, Collection roles) {		
		try {
			addUser(user,passwd);
			AddRoleToUserCmd cmd = new AddRoleToUserCmd(DB_JNDI_NAME);
			try {
				cmd.setUser(user);
				for (Iterator i = roles.iterator(); i.hasNext();) {
					cmd.setRole( (String) i.next());
					cmd.execute();
				}
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}
	
	/**
     * @ejb.interface-method
     */
	public boolean removeUser(String user) {		
		try {
			Collection roles = getRolesOfUser(user);
			for (Iterator i = roles.iterator(); i.hasNext();) {
				removeRoleFromUser(user, (String) i.next());
			}
			RemoveUserCmd cmd = new RemoveUserCmd(DB_JNDI_NAME);
			try {
				cmd.setUser(user);
				return cmd.execute() != 0;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public String getPasswordForUser(String user) {
		try {
			QueryPasswordForUserCmd cmd = new QueryPasswordForUserCmd(DB_JNDI_NAME);
			try {
				cmd.setUser(user);
				cmd.execute();
				return cmd.next() ? cmd.getPassword() : null;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public boolean setPasswordForUser(String user, String passwd) {
		try {
			UpdatePasswordForUserCmd cmd = new UpdatePasswordForUserCmd(DB_JNDI_NAME);
			try {
				cmd.setUser(user);
				cmd.setPassword(passwd);
				return cmd.execute() != 0;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public boolean changePasswordForUser(String user, String oldPasswd, String newPasswd) {
		if ( !getPasswordForUser(user).equals(oldPasswd) ) {
			return false;
		}
		return setPasswordForUser(user, newPasswd);
	}
	
	/**
     * @ejb.interface-method
     */
	public Collection getRolesOfUser(String user) {
		try {
			QueryRolesForUserCmd cmd = new QueryRolesForUserCmd(DB_JNDI_NAME);
			try {
				cmd.setUser(user);
				cmd.execute();
				ArrayList roles = new ArrayList();
				while (cmd.next()) {
					roles.add(cmd.getRole());
				}
				return roles;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}
	
	/**
     * @ejb.interface-method
     */
	public void addRoleToUser(String user, String role) {
		try {
			AddRoleToUserCmd cmd = new AddRoleToUserCmd(DB_JNDI_NAME);
			try {
				cmd.setUser(user);
				cmd.setRole(role);
				cmd.execute();
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}

	/**
     * @ejb.interface-method
     */
	public boolean removeRoleFromUser(String user, String role) {
		try {
			RemoveRoleFromUserCmd cmd = new RemoveRoleFromUserCmd(DB_JNDI_NAME);
			try {
				cmd.setUser(user);
				cmd.setRole(role);
				return cmd.execute() != 0;
			} finally {
				cmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}
	
	/**
     * @ejb.interface-method
     */
	public void updateUser(String user, Collection roles) {		
		try {
			Collection col = getRolesOfUser(user);
			AddRoleToUserCmd cmd = new AddRoleToUserCmd(DB_JNDI_NAME);
			String role;
			try {
				cmd.setUser(user);
				for (Iterator i = roles.iterator(); i.hasNext();) {
					role = (String) i.next();
					if ( !col.contains( role) ) {
						cmd.setRole(role);
						cmd.execute();
					} else {
						col.remove(role);
					}
				}
			} finally {
				cmd.close();
			}
			//remove old roles from user (the remaining items in col).
			RemoveRoleFromUserCmd delCmd = new RemoveRoleFromUserCmd(DB_JNDI_NAME);
			try {
				delCmd.setUser(user);
				for (Iterator i = col.iterator(); i.hasNext();) {
					delCmd.setRole( (String) i.next());
					delCmd.execute();
				}
			} finally {
				delCmd.close();
			}
		} catch (SQLException e) {
			throw new EJBException(e);
		}
	}
	
}
