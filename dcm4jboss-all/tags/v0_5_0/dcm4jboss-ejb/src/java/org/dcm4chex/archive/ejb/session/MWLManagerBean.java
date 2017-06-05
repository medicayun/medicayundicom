/* $Id: MWLManagerBean.java 960 2004-02-10 17:55:22Z gunterze $
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
package org.dcm4chex.archive.ejb.session;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.ejb.interfaces.MWLItemLocal;
import org.dcm4chex.archive.ejb.interfaces.MWLItemLocalHome;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 960 $ $Date: 2004-02-11 01:55:22 +0800 (周三, 11 2月 2004) $
 * @since 10.12.2003
 * 
 * @ejb.bean
 *  name="MWLManager"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/MWLManager"
 * 
 * @ejb.transaction-type 
 *  type="Container"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.ejb-ref
 *  ejb-name="MWLItem" 
 *  view-type="local"
 *  ref-name="ejb/MWLItem" 
 * 
 */
public abstract class MWLManagerBean implements SessionBean {
    private static Logger log = Logger.getLogger(MWLManagerBean.class);
    private MWLItemLocalHome mwlItemHome;

    public void setSessionContext(SessionContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            mwlItemHome =
                (MWLItemLocalHome) jndiCtx.lookup(
                    "java:comp/env/ejb/MWLItem");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetSessionContext() {
        mwlItemHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public Dataset removeWorklistItem(String id) {
        try {
            MWLItemLocal mwlItem = mwlItemHome.findBySpsId(id);
            Dataset ds = mwlItem.getAttributes();
            mwlItem.remove();
            return ds;
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public String addWorklistItem(Dataset ds) {
        try {
            MWLItemLocal mwlItem = mwlItemHome.create(ds);
            return mwlItem.getSpsId();
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

}
