/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

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
 * @version $Revision: 4364 $ $Date: 2004-03-25 01:20:44 +0800 (周四, 25 3月 2004) $
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
