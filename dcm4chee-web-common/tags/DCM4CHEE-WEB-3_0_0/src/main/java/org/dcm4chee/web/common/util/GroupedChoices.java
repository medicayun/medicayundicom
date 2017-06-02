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
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.web.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.security.hive.authorization.Principal;
import org.apache.wicket.security.swarm.strategies.SwarmStrategy;
import org.dcm4che2.util.StringUtils;
import org.dcm4chee.web.common.delegate.BaseCfgDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since 26.05.2010
 */
public class GroupedChoices implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static Map<String,GroupedChoices> singletons = new HashMap<String,GroupedChoices>();
    
    private Map<String,List<String>> groups = new HashMap<String,List<String>>();
    private Map<String,Roles> groupRoles = new HashMap<String,Roles>();
    private Roles universalMatchRoles;
    private Roles availableChoicesRoles;
    
    private File configFile;
    private long lastModified;
    
    private static Logger log = LoggerFactory.getLogger(GroupedChoices.class);
    
    private GroupedChoices(String configFilename) {
        configFile = FileUtils.resolve(new File(BaseCfgDelegate.getInstance().getWebConfigPath(), configFilename));
        init();
    }
    
    public void init() {
        log.info("###Initialize GroupedChoices.configFile:{}",configFile);
        if (configFile.isFile() && configFile.lastModified() > lastModified) {
            log.debug("Initialize GroupedChoices with file {}",configFile);
            Properties p = new Properties();
            try {
                p.load(new FileInputStream(configFile));
                lastModified = configFile.lastModified();
                List<String> groupNames = Arrays.asList(StringUtils.split(p.getProperty("groupnames"), ','));
                String name, prop;
                Roles roles;
                for (int i=0, len=groupNames.size() ; i < len ; i++) {
                    name = groupNames.get(i);
                    prop = p.getProperty(name);
                    if (prop != null) {
                        groups.put(name, Arrays.asList(StringUtils.split(prop, ',')));
                        prop = p.getProperty(name+".roles");
                        if (prop != null) {
                            roles = new Roles();
                            roles.addAll(Arrays.asList(StringUtils.split(prop, ',')));
                            groupRoles.put(name, roles);
                        }
                    }
                }
                prop = p.getProperty("universalmatch.roles");
                if (prop != null) {
                    universalMatchRoles = new Roles();
                    universalMatchRoles.addAll(Arrays.asList(StringUtils.split(prop, ',')));
                }
                prop = p.getProperty("availablechoices.roles");
                if (prop != null) {
                    availableChoicesRoles = new Roles();
                    availableChoicesRoles.addAll(Arrays.asList(StringUtils.split(prop, ',')));
                }
                
            } catch (Exception x) {
                log.error("Can not read GroupedChoices properties file!:"+configFile, x);
            }
            log.info("GroupedChoices initialized!");
       } else {
           log.debug("GroupedChoices config files is up to date (or doesn't exist).");
       }
    }
    
    public static GroupedChoices get(String configFilename) {
        GroupedChoices singleton = singletons.get(configFilename);
        if (singleton == null) {
            singleton = new GroupedChoices(configFilename);
            singletons.put(configFilename, singleton);
        } else {
            singleton.init();
        }
        return singleton;
    }
    
    public List<String> getChoices(List<String> availableChoices) {
        Set<Principal> principals = ((SwarmStrategy) SwarmStrategy.get()).getSubject().getPrincipals();
        List<String> choices = new ArrayList<String>(availableChoices.size()+groups.size());
        if (principals != null && principals.size() > 0) {
            Roles roles = new Roles();
            Iterator<Principal> i = principals.iterator();
            while (i.hasNext())
                roles.add(i.next().getName());
            if (universalMatchRoles == null || roles.hasAnyRole(universalMatchRoles)) {
                choices.add("*");
            }
            Roles r;
            for (String grp : groups.keySet()) {
                r = this.groupRoles.get(grp);
                if (r == null || roles.hasAnyRole(r)) {
                    choices.add(grp);
                }
            }
            if (availableChoicesRoles == null || roles.hasAnyRole(availableChoicesRoles)) {
                choices.addAll(availableChoices);
            }
        }
        return choices;
    }
    
    public Map<String,List<String>> getAllGroups() {
        return groups;
    }
    
    public List<String> getGroupMembers(String groupName) {
        return groups.get(groupName);
    }
}
