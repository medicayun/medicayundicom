/*                                                                           *
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG                             *
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
 */
package org.dcm4chex.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che.util.DcmURL;
import org.dcm4chex.archive.ejb.interfaces.AELocal;

import org.dcm4chex.archive.ejb.interfaces.AELocalHome;
import org.jboss.system.ServiceMBeanSupport;

/**
 * <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      July 24, 2002
 * @version    $Revision: 884 $ $Date: 2003-11-22 17:45:54 +0800 (周六, 22 11月 2003) $
 * 
 * @jmx.mbean
 *  extends="org.jboss.system.ServiceMBean"
 */
public class AEService
         extends ServiceMBeanSupport
         implements org.dcm4chex.service.AEServiceMBean
{

    private AELocalHome home = null;

    private AELocalHome getHome()
    {
        if (home != null) {
            return home;
        }
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            return home = (AELocalHome) jndiCtx.lookup(AELocalHome.JNDI_NAME);
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {}
            }
        }
    }


    /**
     * @jmx.managed-attribute
     */
    public String[] getAEs()
        throws FinderException
    {
        Collection c = getHome().findAll();
        String[] retval = new String[c.size()];
        Iterator it = c.iterator();
        for (int i = 0; i < retval.length; ++i) {
            retval[i] = ((AELocal) it.next()).asString();
        }
        return retval;
    }


    /**
     * @jmx.managed-operation
     */
    public void addAE(String urls)
        throws CreateException
    {
        StringTokenizer stk = new StringTokenizer(urls, " ,;\t\r\n");
        while (stk.hasMoreTokens()) {
            DcmURL url = new DcmURL(stk.nextToken());
            try {
                getHome().findByPrimaryKey(url.getCalledAET()).remove();
            } catch (FinderException ignore) {
            } catch (RemoveException ignore) {
            }
            getHome().create(
                    url.getCalledAET(),
                    url.getHost(),
                    url.getPort(),
                    toString(url.getCipherSuites()));
        }
    }


    /**
     * @jmx.managed-operation
     */
    public void removeAE(String titles)
        throws RemoveException
    {
        StringTokenizer stk = new StringTokenizer(titles, " ,;\t\r\n");
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken();
            try {
                DcmURL url = new DcmURL(tk);
                tk = url.getCalledAET();
            } catch (IllegalArgumentException ignore) {
            }
            getHome().remove(tk);
        }
    }


    private String toString(String[] a)
    {
        if (a == null || a.length == 0) {
            return null;
        }
        if (a.length == 1) {
            return a[0];
        }
        StringBuffer sb = new StringBuffer(a[0]);
        for (int i = 1; i < a.length; ++i) {
            sb.append(',').append(a[i]);
        }
        return sb.toString();
    }
}

