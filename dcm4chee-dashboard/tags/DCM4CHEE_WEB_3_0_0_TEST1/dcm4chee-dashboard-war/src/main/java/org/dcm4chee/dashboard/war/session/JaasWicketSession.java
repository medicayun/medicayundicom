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

package org.dcm4chee.dashboard.war.session;

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
import org.dcm4chee.dashboard.war.WicketApplication;

public class JaasWicketSession extends AuthenticatedWebSession {

    private static final long serialVersionUID = 1L;

    private String username;
    private Roles roles;

    public JaasWicketSession(Request request) {
        super(request);
    }

    public static JaasWicketSession get() {
        return (JaasWicketSession) AuthenticatedWebSession.get();
    }

    public boolean authenticate(String username, String password) {
        WicketApplication application = WicketApplication.get();
        try {
            LoginContext ctx = new LoginContext(
                    application.getSecurityDomainName(),
                    new LoginCallbackHandler(username, password));
            ctx.login();
            this.username = username;
            this.roles = application.getRoles(ctx.getSubject());
            return true;
        } catch (LoginException e) {
            return false;
        }
    }

    public String getUsername() {
        return username;
    }

    public Roles getRoles() {
        return roles;
    }

    private static class LoginCallbackHandler implements CallbackHandler {

        private String username;
        private String password;

        public LoginCallbackHandler(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks)
                throws UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                Callback callback = callbacks[i];
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(username);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callbacks[i],
                            "Callback type not supported");
                }
            }
        }

    }
}
