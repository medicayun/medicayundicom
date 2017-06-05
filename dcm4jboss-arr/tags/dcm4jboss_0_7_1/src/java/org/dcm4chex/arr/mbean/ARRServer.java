/*
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
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
 */
package org.dcm4chex.arr.mbean;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che.server.ServerFactory;
import org.dcm4che.server.SyslogService;
import org.dcm4che.server.UDPServer;
import org.dcm4che.util.HostNameUtils;
import org.dcm4chex.arr.ejb.session.StoreAuditRecordLocal;
import org.dcm4chex.arr.ejb.session.StoreAuditRecordLocalHome;
import org.jboss.security.SecurityAssociation;
import org.jboss.system.ServiceMBeanSupport;

/**
 * <description>
 * 
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger </a>
 * @author <a href="mailto:joseph@tiani.com">joseph foraci </a>
 * @since February 13, 2003
 * @version $Revision: 4364 $
 * 
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class ARRServer extends ServiceMBeanSupport implements
        org.dcm4chex.arr.mbean.ARRServerMBean, SyslogService {

    // Constants -----------------------------------------------------
    private static final String START = "Start";

    private static final String STOP = "Stop";

    private static final String ACTOR_START_STOP = "<IHEYr4>"
            + "<ActorStartStop>" + "<ActorName>{0}</ActorName>"
            + "<ApplicationAction>{1}</ApplicationAction>"
            + "<User><LocalUser>{2}</LocalUser></User>" + "</ActorStartStop>"
            + "<Host>{3}</Host>"
            + "<TimeStamp>{4,date,yyyy-MM-dd'T'HH:mm:ss.SSS}</TimeStamp>"
            + "</IHEYr4>";

    // Attributes ----------------------------------------------------
    private ServerFactory sf = ServerFactory.getInstance();

    //create new UDP server with a syslog transport handler (which in turn
    // uses
    // this class to service the received syslog fields in the implemented
    // process() method)
    private UDPServer udpsrv = sf.newUDPServer(sf.newSyslogHandler(this));

    private StoreAuditRecordLocal store = null;

    private String actorName;

    // Methods -------------------------------------------------------
    public String getName() {
        return actorName;
    }

    protected ObjectName getObjectName(MBeanServer server, ObjectName name) {
        actorName = name.getKeyProperty("name");
        return name;
    }

    /**
     * Description of the Method
     * 
     * @param date
     *            Description of the Parameter
     * @param host
     *            Description of the Parameter
     * @param content
     *            Description of the Parameter
     */
    public void process(Date date, String host, String content) {
        getStoreAuditRecord().store(content);
    }

    /**
     * @jmx.managed-attribute
     */
    public int getPort() {
        return udpsrv.getPort();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setPort(int port) {
        udpsrv.setPort(port);
    }

    /**
     * @jmx.managed-attribute
     */
    public int getMaxClients() {
        return udpsrv.getMaxClients();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setMaxClients(int maxClients) {
        udpsrv.setMaxClients(maxClients);
    }

    /**
     * @jmx.managed-attribute
     */
    public int getNumClients() {
        return udpsrv.getNumClients();
    }

    // ServiceMBeanSupport overrides ---------------------------------
    public void startService() throws Exception {
        udpsrv.start();
        getStoreAuditRecord().store(buildActorStartStopAuditMessage(START));
    }

    public String getCurrentPrincipalName() {
        Principal p = SecurityAssociation.getPrincipal();
        return p != null ? p.getName() : System.getProperty("user.name");
    }

    private String buildActorStartStopAuditMessage(String action) {
        Object[] arguments = { actorName, action, getCurrentPrincipalName(),
                HostNameUtils.getLocalHostName(), new Date()};
        return MessageFormat.format(ACTOR_START_STOP, arguments);
    }

    public void stopService() throws Exception {
        udpsrv.stop();
        getStoreAuditRecord().store(buildActorStartStopAuditMessage(STOP));
    }

    private StoreAuditRecordLocal getStoreAuditRecord() {
        if (store != null) { return store; }
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            StoreAuditRecordLocalHome home = (StoreAuditRecordLocalHome) jndiCtx
                    .lookup(StoreAuditRecordLocalHome.JNDI_NAME);
            return (store = home.create());
        } catch (CreateException e) {
            throw new EJBException(e);
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
}
