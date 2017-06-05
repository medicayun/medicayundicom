/* $Id: EJBHomeFactory.java 937 2004-01-26 21:26:25Z gunterze $
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
package org.dcm4chex.archive.util;

import java.util.Hashtable;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 937 $ $Date: 2004-01-27 05:26:25 +0800 (周二, 27 1月 2004) $
 * @since 16.12.2003
 */
public class EJBHomeFactory {
    private static EJBHomeFactory factory;
    private static String ejbProviderURL;

    private Hashtable homes = new Hashtable();
    private Context ctx;

    public static String getEjbProviderURL() {
        return ejbProviderURL;
    }

    public static void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.ejbProviderURL = ejbProviderURL;
    }

    public static EJBHomeFactory getFactory() throws HomeFactoryException {
        if (EJBHomeFactory.factory == null) {
            try {
                EJBHomeFactory.factory = new EJBHomeFactory();
            } catch (NamingException e) {
                throw new HomeFactoryException(e);
            }
        }
        return EJBHomeFactory.factory;
    }

    private EJBHomeFactory() throws NamingException {
        Hashtable env = new Hashtable();
        env.put(
            "java.naming.factory.initial",
            "org.jnp.interfaces.NamingContextFactory");
        env.put(
            "java.naming.factory.url.pkgs",
            "org.jboss.naming:org.jnp.interfaces");
        if (ejbProviderURL != null && ejbProviderURL.length() > 0) {
            env.put("java.naming.provider.url", ejbProviderURL);
        }
        ctx = new InitialContext(env);
    }

    public EJBHome lookup(Class homeClass, String jndiName)
        throws HomeFactoryException {
        EJBHome home = (EJBHome) homes.get(homeClass);
        if (home == null) {
            try {
                home =
                    (EJBHome) PortableRemoteObject.narrow(
                        ctx.lookup(jndiName),
                        homeClass);
            } catch (ClassCastException e) {
                throw new HomeFactoryException(e);
            } catch (NamingException e) {
                throw new HomeFactoryException(e);
            }
            homes.put(homeClass, home);
        }
        return home;
    }
}
