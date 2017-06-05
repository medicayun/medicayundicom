/* $Id: MoveOrderQueueBean.java 1149 2004-08-06 07:50:10Z gunterze $
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

import java.sql.Timestamp;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.interfaces.MoveOrderLocal;
import org.dcm4chex.archive.ejb.interfaces.MoveOrderLocalHome;
import org.dcm4chex.archive.ejb.interfaces.MoveOrderValue;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1149 $ $Date: 2004-08-06 15:50:10 +0800 (周五, 06 8月 2004) $
 * @since 10.12.2003
 * 
 * @ejb.bean
 *  name="MoveOrderQueue"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/MoveOrderQueue"
 * 
 * @ejb.transaction-type 
 *  type="Container"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.ejb-ref
 *  ejb-name="MoveOrder" 
 *  view-type="local"
 *  ref-name="ejb/MoveOrder" 
 * 
 */
public abstract class MoveOrderQueueBean implements SessionBean {

    private static Logger log = Logger.getLogger(MoveOrderQueueBean.class);

    private MoveOrderLocalHome moveOrderHome;

    public void setSessionContext(SessionContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            moveOrderHome = (MoveOrderLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/MoveOrder");
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
        moveOrderHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public MoveOrderValue dequeue() {
        try {
            Collection c = moveOrderHome.findBefore(new Timestamp(System
                    .currentTimeMillis()));
            if (c.isEmpty()) { return null; }
            MoveOrderLocal order = (MoveOrderLocal) c.iterator().next();
            MoveOrderValue value = order.getMoveOrderValue();
            order.remove();
            return value;
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void queue(MoveOrderValue order) {
        try {
            moveOrderHome.create(order);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

}
