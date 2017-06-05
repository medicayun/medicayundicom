/* $Id: AuditLoginModule.java 1744 2005-05-24 14:20:47Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.archive.security;

import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;

/**
 * <description>
 * 
 * @see <related>
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger </a>
 * @version $Revision: 1744 $ $Date: 2005-05-24 22:20:47 +0800 (周二, 24 5月 2005) $
 * @since August 31, 2002
 * 
 */
public class AuditLoginModule implements LoginModule
{

    static final Logger log = Logger.getLogger(AuditLoginModule.class);

    private CallbackHandler cbh;

    private ObjectName auditLoggerName;

    private MBeanServer server;

    public void initialize(Subject subject, CallbackHandler cbh,
            Map sharedState, Map options) {
        this.cbh = cbh;
        try {
            auditLoggerName = new ObjectName((String) options
                    .get("auditLoggerName"));
        } catch (MalformedObjectNameException mone) {
            String prompt = "Illegal value of <module-option name=\"auditLoggerName\">"
                    + options.get("auditLoggerName");
            log.error(prompt);
            throw new IllegalArgumentException(prompt);

        }

        if (auditLoggerName == null) {
            log.error("Missing <module-option name=\"auditLoggerName\">");
            throw new IllegalArgumentException(
                    "Missing <module-option name=\"auditLoggerName\">");
        }
        server = (MBeanServer) MBeanServerFactory.findMBeanServer(null)
                .iterator().next();
    }

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

    public boolean logout() throws LoginException {
		logUserAuthenticated("Logout");
        return true;
    }

    private String getUserName() {
        try {
            NameCallback nc = new NameCallback("prompt");
            cbh.handle(new Callback[] { nc});
            return nc.getName();
        } catch (Exception e) {
            log.error("Failed to access UserName:", e);
        }
        return null;
    }

    private void logUserAuthenticated(String action) {
        try {
            server.invoke(auditLoggerName, "logUserAuthenticated",
                new Object[] { getUserName(), action },
                new String[] { String.class.getName(), String.class.getName()});
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }
    }	
}
