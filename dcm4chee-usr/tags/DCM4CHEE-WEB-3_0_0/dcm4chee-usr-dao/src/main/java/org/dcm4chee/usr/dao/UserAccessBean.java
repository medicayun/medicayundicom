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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.usr.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.PostActivate;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import net.sf.json.JSONObject;

import org.dcm4chee.usr.entity.User;
import org.dcm4chee.usr.entity.UserRoleAssignment;
import org.dcm4chee.usr.model.Group;
import org.dcm4chee.usr.model.Role;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.system.server.ServerConfigLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 19.08.2009
 */
@Stateless
@LocalBinding(jndiBinding=UserAccess.JNDI_NAME)
public class UserAccessBean implements UserAccess {

    private static Logger log = LoggerFactory.getLogger(UserAccessBean.class);
    
    @PersistenceContext(unitName="dcm4chee-usr")
    private EntityManager em;

    private File rolesMappingFile;
    private File groupsFile;
    private String userRoleName;
    private String adminRoleName;
    private boolean ensureUserAndAdminRole;
    
    @SuppressWarnings("unused")
    @PostConstruct
    @PostActivate
    private void config() {
        if (this.rolesMappingFile == null) {
            userRoleName = System.getProperty("dcm4chee-usr.cfg.userrole", "+WebUser");
            adminRoleName = System.getProperty("dcm4chee-usr.cfg.adminrole", "+WebAdmin");
            rolesMappingFile = new File(System.getProperty("dcm4chee-web3.cfg.path", "conf/dcm4chee-web3") + "roles.json");
            if (!rolesMappingFile.isAbsolute())
                rolesMappingFile = new File(ServerConfigLocator.locate().getServerHomeDir(), rolesMappingFile.getPath());
            if (log.isDebugEnabled()) {
                log.debug("UserAccess configuration:\nuserRoleName:"+userRoleName);
                log.debug("adminRoleName:"+adminRoleName);
                log.debug("ensureUserAndAdminRole:"+ensureUserAndAdminRole);
                log.debug("mappingFile:"+rolesMappingFile);
            }
            if (!rolesMappingFile.exists()) {
                try {
                    if (rolesMappingFile.getParentFile().mkdirs())
                        log.info("M-WRITE dir:" +rolesMappingFile.getParent());
                    rolesMappingFile.createNewFile();
                } catch (IOException e) {
                    log.error("RolesMapping file doesn't exist and can't be created!", e);
                }
            }
        }
        if (this.groupsFile == null) {
            groupsFile = new File(System.getProperty("dcm4chee-web3.cfg.path", "conf/dcm4chee-web3") + "groups.json");
            if (!groupsFile.isAbsolute())
                groupsFile = new File(ServerConfigLocator.locate().getServerHomeDir(), groupsFile.getPath());
            if (log.isDebugEnabled()) {
                log.debug("groupsFile:"+groupsFile);
            }
            if (!groupsFile.exists()) {
                try {
                    if (groupsFile.getParentFile().mkdirs())
                        log.info("M-WRITE dir:" + groupsFile.getParent());
                    groupsFile.createNewFile();
                } catch (IOException e) {
                    log.error("Groups file doesn't exist and can't be created!", e);
                }
            }
        }
        List<Role> roles = null;
        if (userRoleName.charAt(0)=='+') {
            userRoleName = userRoleName.substring(1);
            roles = getAllRoles();
            Role userRole = new Role(userRoleName);
            if (!roles.contains(userRole)) {
                addRole(userRole);
                roles.add(userRole);
            }
        }
        if (adminRoleName.charAt(0)=='+') {
            adminRoleName = adminRoleName.substring(1);
            Role adminRole = new Role(adminRoleName);
            if ( roles == null) 
                roles = getAllRoles();
            if (!roles.contains(adminRole)) {
                addRole(adminRole);
            }
        }
    }

    public String getUserRoleName() {
        return userRoleName;
    }

    public String getAdminRoleName() {
        return adminRoleName;
    }

    // TODO: change this to generic version using JPA 2.0 implementation
    @SuppressWarnings("unchecked")
    public List<User> getAllUsers() {
        return this.em.createQuery("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles ORDER BY u.userID")
        .getResultList();
    }

    public User getUser(String userId) {
        return this.em.find(User.class, userId);
    }

    public void createUser(User user) {
        this.em.persist(user);
    }

    public void updateUser(String userId, String password) {
        User managedUser = this.em.find(User.class, userId);
        managedUser.setPassword(password);
    }
    
    public void deleteUser(String userId) {
        this.em.createQuery("DELETE FROM UserRoleAssignment ura WHERE ura.userID = :userID")
        .setParameter("userID", userId)
        .executeUpdate();
        this.em.createQuery("DELETE FROM User u WHERE u.userID = :userID")
        .setParameter("userID", userId)
        .executeUpdate();
    }

    public Boolean userExists(String username) {
        try {
            this.em.createQuery("SELECT DISTINCT u FROM User u WHERE u.userID = :userID")
            .setParameter("userID", username)
            .getSingleResult();
            return true;
        } catch (NoResultException nre) {
            return false;
        }
    }

    public Boolean hasPassword(String username, String password) {
        try {
            this.em.createQuery("SELECT DISTINCT u FROM User u WHERE u.userID = :userID AND u.password = :password")
            .setParameter("userID", username)
            .setParameter("password", password)
            .getSingleResult();
            return true;
        } catch (NoResultException nre) {
            return false;
        }
    }

    public void assignRole(UserRoleAssignment ura) {
        this.em.persist(ura);
    }

    public void unassignRole(UserRoleAssignment ura) {
        this.em.createQuery("DELETE FROM UserRoleAssignment ura WHERE ura.userID = :userID AND ura.role = :rolename")
        .setParameter("userID", ura.getUserID())
        .setParameter("rolename", ura.getRole())
        .executeUpdate();
    }

    public List<Role> getAllRoles() {
        BufferedReader reader = null;
        try {
            List<Role> roleList = new ArrayList<Role>();
            String line;
            reader = new BufferedReader(new FileReader(rolesMappingFile));
            while ((line = reader.readLine()) != null)
                roleList.add((Role) JSONObject.toBean(JSONObject.fromObject(line), Role.class));

            final List<Group> allGroups = getAllGroups();
            final List<String> groupUuidList = new ArrayList<String>(allGroups.size());
            for (Group group : allGroups)
                groupUuidList.add(group.getUuid());
            Collections.sort(roleList, new Comparator<Role>() {

                @Override
                public int compare(Role role1, Role role2) {
                    return (role1.getGroupUuid().equals(role2.getGroupUuid())) ? 
                            role1.getRolename().compareToIgnoreCase(role2.getRolename()) : 
                                groupUuidList.indexOf(role1.getGroupUuid()) - groupUuidList.indexOf(role2.getGroupUuid());
                }
            });
            return roleList;
        } catch (Exception e) {
            log.error("Can't get roles from roles mapping file!", e);
            return null;
        } finally {
            close(reader, "mapping file reader");
        }
    }

    public void addRole(Role role) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(rolesMappingFile, true));
            JSONObject jsonObject = JSONObject.fromObject(role);
            writer.write(jsonObject.toString());
            writer.newLine();
        } catch (IOException e) {
            log.error("Can't add role to roles mapping file!", e);
        } finally {
            close(writer, "mapping file reader");
        }
    }

    public void updateRole(Role role) {
        Role oldRole = null;
        List<Role> roles = getAllRoles();
        for(Role current : roles)
            if (role.getUuid().equals(current.getUuid()))
                oldRole = current;
        if (oldRole != null) {
            if (!oldRole.getRolename().equals(role.getRolename())) {
                this.em.createQuery("UPDATE UserRoleAssignment ura SET ura.role = :newRolename WHERE ura.role = :oldRolename")
                .setParameter("oldRolename", oldRole.getRolename())
                .setParameter("newRolename", role.getRolename())
                .executeUpdate();
            }
            roles.set(roles.indexOf(oldRole), role);
            saveRoles(roles);
        } else {
            log.warn("Update Role "+role+" failed! Removed from roles mapping file!");
        }
    }

    public void removeRole(Role role) {
        this.em.createQuery("DELETE FROM StudyPermission sp WHERE sp.role = :rolename")
        .setParameter("rolename", role.getRolename())
        .executeUpdate();
        this.em.createQuery("DELETE FROM UserRoleAssignment ura WHERE ura.role = :rolename")
        .setParameter("rolename", role.getRolename())
        .executeUpdate();
        List<Role> roles = getAllRoles();
        if (roles.remove(role)) {
            saveRoles(roles);
        } else {
            log.warn("Role "+role+" already removed from roles mapping file!");
        }
    }
    
    private void saveRoles(List<Role> roles) {
        BufferedWriter writer = null;
        try {
            File tmpFile = File.createTempFile(rolesMappingFile.getName(), null, rolesMappingFile.getParentFile());
            writer = new BufferedWriter(new FileWriter(tmpFile, true));
            JSONObject jsonObject;
            for (int i=0,len=roles.size() ; i < len ; i++) {
                jsonObject = JSONObject.fromObject(roles.get(i));
                writer.write(jsonObject.toString());
                writer.newLine();
            }
            if (close(writer, "Temporary mapping file"))
                writer = null;
            rolesMappingFile.delete();
            tmpFile.renameTo(rolesMappingFile);
        } catch (IOException e) {
            log.error("Can't save roles in roles mapping file!", e);
        } finally {
            close(writer, "Temporary mapping file (in finally block)");
        }
    }

    public Boolean roleExists(String rolename) {
        return getAllRoles().contains(new Role(rolename));
    }
    
    public List<Group> getAllGroups() {
        BufferedReader reader = null;
        try {
            List<Group> groupList = new ArrayList<Group>();
            String line;
            reader = new BufferedReader(new FileReader(groupsFile));
            while ((line = reader.readLine()) != null)
                groupList.add((Group) JSONObject.toBean(JSONObject.fromObject(line), Group.class));
            Collections.sort(groupList);
            
            int webPos = -1;
            int dicomPos = -1;

            for (int i = 0; i < groupList.size(); i++) {
                if (groupList.get(i).getGroupname().equalsIgnoreCase("Web")) 
                    webPos = i;
                if (groupList.get(i).getGroupname().equalsIgnoreCase("Dicom")) 
                    dicomPos = i;
            }
            
            if (dicomPos > 0) {
                Group dicomGroup = groupList.get(dicomPos);
                groupList.remove(dicomPos);
                groupList.add(0, dicomGroup);
            } else if (dicomPos == -1) {
                Group group = new Group();
                group.setGroupname("Dicom");
                addGroup(group);
                groupList.add(0, group);
            }
            if (webPos > 0) {
                Group webGroup = groupList.get(webPos);
                groupList.remove(webPos);
                groupList.add(0, webGroup);
            } else if (webPos == -1) {
                Group group = new Group();
                group.setGroupname("Web");
                addGroup(group);
                groupList.add(0, group);
            }
            
            return groupList;
        } catch (Exception e) {
            log.error("Can't get groups from groups file!", e);
            return null;
        } finally {
            close(reader, "groups file reader");
        }
    }

    public void addGroup(Group group) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(groupsFile, true));
            JSONObject jsonObject = JSONObject.fromObject(group);
            writer.write(jsonObject.toString());
            writer.newLine();
        } catch (IOException e) {
            log.error("Can't add group to groups file!", e);
        } finally {
            close(writer, "groups file reader");
        }
    }

    public void updateGroup(Group group) {
        Group oldGroup = null;
        List<Group> groups = getAllGroups();
        for(Group current : groups)
            if (group.getUuid().equals(current.getUuid()))
                oldGroup = current;
        if (oldGroup != null) {
            groups.set(groups.indexOf(oldGroup), group);
            saveGroups(groups);
        } else {
            log.warn("Update Group "+group+" failed! Removed from groups file!");
        }
    }

    public void removeGroup(Group group) {
        List<Role> roles = getAllRoles();
        for (Role role : roles)
            if (role.getGroupUuid().equals(group.getUuid())) 
                role.setGroupUuid(null);
        List<Group> groups = getAllGroups();
        if (groups.remove(group)) {
            saveGroups(groups);
        } else {
            log.warn("Group "+group+" already removed from groups file!");
        }
    }
    
    private void saveGroups(List<Group> groups) {
        BufferedWriter writer = null;
        try {
            File tmpFile = File.createTempFile(groupsFile.getName(), null, groupsFile.getParentFile());
            writer = new BufferedWriter(new FileWriter(tmpFile, true));
            JSONObject jsonObject;
            for (int i=0,len=groups.size() ; i < len ; i++) {
                jsonObject = JSONObject.fromObject(groups.get(i));
                writer.write(jsonObject.toString());
                writer.newLine();
            }
            if (close(writer, "Temporary groups file"))
                writer = null;
            groupsFile.delete();
            tmpFile.renameTo(groupsFile);
        } catch (IOException e) {
            log.error("Can't save groups in groups file!", e);
        } finally {
            close(writer, "Temporary types file (in finally block)");
        }
    }

    private boolean close(Closeable toClose, String desc) {
        log.debug("Closing ",desc);
        if (toClose != null) {
            try {
                toClose.close();
                return true;
            } catch (IOException ignore) {
                log.warn("Error closing : "+desc, ignore);
            }
        }
        return false;
    }
}
