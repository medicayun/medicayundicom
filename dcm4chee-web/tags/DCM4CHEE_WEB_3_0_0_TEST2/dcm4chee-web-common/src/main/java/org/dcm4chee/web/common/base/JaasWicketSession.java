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

package org.dcm4chee.web.common.base;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.wicket.Request;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAAS based Wicket Session to use JBoss web security.
 */
public class JaasWicketSession extends AuthenticatedWebSession {
    
    private static final long serialVersionUID = 1L;
    
    private String securityDomainName;
    private String rolesGroupName;
    private boolean useRoleMapping;
    private String userRoleName;
    private String adminRoleName;
    private boolean onlyUSERandADMIN;

    private Subject subject;
    private Roles roles = new Roles();
    private String username;

    private final static Logger log = LoggerFactory.getLogger(JaasWicketSession.class);

    public JaasWicketSession(Request req) {
        super(req);
    }
    
    private void init() {
        if (securityDomainName == null) {
            BaseWicketApplication app = (BaseWicketApplication) getApplication();
            this.securityDomainName = app.getInitParameter("securityDomainName");
            if ( securityDomainName == null) securityDomainName = "dcm4chee";
            this.rolesGroupName = app.getInitParameter("rolesGroupName");
            if ( rolesGroupName == null) rolesGroupName = "Roles";
            this.userRoleName = app.getInitParameter("userRoleName");
            this.adminRoleName = app.getInitParameter("adminRoleName");
            this.onlyUSERandADMIN = "true".equals(app.getInitParameter("onlyUSERandADMIN"));
            this.useRoleMapping = userRoleName != null || adminRoleName != null;
        }
    }

    public boolean authenticate(String username, String password) {
        init();
        if (log.isDebugEnabled()) 
            log.debug("authenticate(" + username + ", " + password + ")");
        boolean authenticated = false;
        LoginCallbackHandler handler = new LoginCallbackHandler(username, password);
        try {
            LoginContext ctx = new LoginContext(securityDomainName, handler);
            ctx.login();
            authenticated = true;
            this.username = username;
            subject = ctx.getSubject();
            String name;
            for (Principal p : subject.getPrincipals()) {
                if (log.isDebugEnabled()) log.debug("Principal for " + username + ": " + p.toString());
                // Group is a subclass of Principal, and the members are the names of the roles
                if ((p instanceof Group) && (rolesGroupName.equalsIgnoreCase(p.getName()))) {
                    Group g = (Group) p;
                    Enumeration<? extends Principal> members = g.members();
                    while (members.hasMoreElements()) {
                        Principal member = members.nextElement();
                        name = member.getName();
                        if (useRoleMapping) {
                            if (name.equals(userRoleName))
                                roles.add(Roles.USER);
                            if (name.equals(adminRoleName))
                                roles.add(Roles.ADMIN);
                            if (onlyUSERandADMIN) {
                                if ( roles.size() < 2)
                                    continue;
                                else 
                                    break;
                            }
                        }
                        roles.add(name);
                    }
                }
            }
        } catch (LoginException e) {
            authenticated = false;
        }
        return authenticated;
    }

    public Roles getRoles() {
        return roles;
    }

    public String getUsername() {
        return username;
    }

    private class LoginCallbackHandler implements CallbackHandler {

        private String user;
        private String passwd;

        public LoginCallbackHandler(String user, String passwd) {
            this.user = user;
            this.passwd = passwd;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback cb : callbacks) {
                if (cb instanceof NameCallback) {
                    ((NameCallback) cb).setName(user);
                } else if (cb instanceof PasswordCallback) {
                    ((PasswordCallback)cb).setPassword(passwd.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(cb, "Callback not supported! (only Name and Password Callback are supported)");
                }
            }
        }

    }
}
