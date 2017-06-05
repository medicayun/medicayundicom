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
 * Joe Foraci <jforaci@users.sourceforge.net>
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

package org.dcm4chex.arr.security;

/*
 * THIS lOGIN MODULE IS NOT SUITABLE FOR JBoss AS IT IS NOW SINCE JBoss
 * REQUIRES
 */

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;
import org.dcm4che.util.HostNameUtils;
import org.dcm4chex.arr.ejb.session.StoreAuditRecordLocal;
import org.dcm4chex.arr.ejb.session.StoreAuditRecordLocalHome;

//import org.jboss.security.auth.spi.UsersRolesLoginModule;
//import org.jboss.security.SecurityAssociation;

/**
 * <description>
 * 
 * @see <related>
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger </a>
 * @version $Revision: 4422 $ $Date: 2005-10-07 03:47:54 +0800 (周五, 07 10月 2005) $
 * @since August 31, 2002
 */
public class AuditingLoginModule implements LoginModule //extends
                                                        // AbstractLoginModule
{

    static final Logger log = Logger.getLogger(AuditingLoginModule.class);

    private CallbackHandler cbh = null;

    private StoreAuditRecordLocal store = null;

    public boolean login() throws LoginException {
        return true;
    }

    public boolean abort() throws LoginException {
        logUserAuthenticated("Failure");
        return true;
    }

    public boolean commit() throws LoginException {
        logUserAuthenticated("Login");
        return true;
    }

    public void initialize(Subject subject, CallbackHandler cbh,
            Map sharedState, Map options) {
        this.cbh = cbh;
    }

    public boolean logout() throws LoginException {
        logUserAuthenticated("Logout");
        return true;
    }

    private void logUserAuthenticated(String action) {
        try {
            String xmldata = "<IHEYr4><UserAuthenticated><LocalUsername>"
                    + getUserName()
                    + "</LocalUsername><Action>"
                    + action
                    + "</Action></UserAuthenticated><Host>"
                    + HostNameUtils.getLocalHostName()
                    + "</Host><TimeStamp>"
                    + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                            .format(new Date()) + "</TimeStamp></IHEYr4>";
            getStoreAuditRecord().store(xmldata);
        } catch (Exception e) {
            log.error("Failed to log User Authentication " + action, e);
        }
    }

    private String getUserName() throws IOException,
            UnsupportedCallbackException {
        NameCallback nc = new NameCallback("prompt");
        cbh.handle(new Callback[] { nc});
        return nc.getName();
    }

    private StoreAuditRecordLocal getStoreAuditRecord() throws Exception {
        if (store != null) { return store; }
        Context jndiCtx = new InitialContext();
        try {
            StoreAuditRecordLocalHome home = (StoreAuditRecordLocalHome) jndiCtx
                    .lookup(StoreAuditRecordLocalHome.JNDI_NAME);
            return (store = home.create());
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

}
