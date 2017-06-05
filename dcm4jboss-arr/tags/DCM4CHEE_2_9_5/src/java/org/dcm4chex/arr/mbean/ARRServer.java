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

package org.dcm4chex.arr.mbean;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
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
 * @version $Revision: 4453 $
 */
public class ARRServer extends ServiceMBeanSupport implements
        SyslogService {

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

    private StoreAuditRecordLocalHome storeHome = null;

    private String actorName = "ARR";

    public final String getActorName() {
        return actorName;
    }

    public final void setActorName(String actorName) {
        this.actorName = actorName;
    }
    
	public final int getPort() {
        return udpsrv.getPort();
    }

	public final void setPort(int port) {
        udpsrv.setPort(port);
    }

    public String toString() {
        return udpsrv.toString();
    }

    public void process(Date date, String host, String content) {
        store(content);
    }

	private void store(String content) {
		StoreAuditRecordLocal bean;
		try {
			bean = getStoreAuditRecordHome().create();
		} catch (CreateException e) {
			throw new EJBException(e);
		}
		try {
			bean.store(content);
		} finally {
			try {
				bean.remove();
			} catch (Exception ignore) {
			}
		}
		
	}

    // ServiceMBeanSupport overrides ---------------------------------
    public void startService() throws Exception {
    	storeHome = null;
        udpsrv.start();
        store(buildActorStartStopAuditMessage(START));
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
        store(buildActorStartStopAuditMessage(STOP));
    }

    private StoreAuditRecordLocalHome getStoreAuditRecordHome() {
        if (storeHome != null) { return storeHome; }
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            StoreAuditRecordLocalHome home = (StoreAuditRecordLocalHome) jndiCtx
                    .lookup(StoreAuditRecordLocalHome.JNDI_NAME);
            return home;
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

    public String getLocalAddress() {
        return udpsrv.getLocalAddress();
    }

    public void setLocalAddress(String laddrStr) {
        udpsrv.setLocalAddress(laddrStr);
    }
    
    public int getMaxPacketSize() {
        return udpsrv.getMaxPacketSize();
    }
    
    public void setMaxPacketSize(int maxPacketSize) {
        udpsrv.setMaxPacketSize(maxPacketSize);
    }

    public int getReceiveBufferSize() {
        return udpsrv.getReceiveBufferSize();
    }
    
    public void setReceiveBufferSize(int receiveBufferSize) {
        udpsrv.setReceiveBufferSize(receiveBufferSize);
    }

    public boolean isRunning() {
        return udpsrv.isRunning();
    }
    
    public Date getLastStartedAt() {
        return udpsrv.getLastStartedAt();
    }
    
    public Date getLastStoppedAt() {
        return udpsrv.getLastStoppedAt();
    }
}

