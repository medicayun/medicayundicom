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
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.mbean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.ObjectNotFoundException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.SecurityAlertMessage;
import org.dcm4chex.archive.ejb.interfaces.AEDTO;
import org.dcm4chex.archive.ejb.interfaces.AEManager;
import org.dcm4chex.archive.ejb.interfaces.AEManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * <description>
 * 
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since July 24, 2002
 * @version $Revision: 3262 $ $Date: 2007-04-10 19:34:35 +0800 (周二, 10 4月 2007) $
 */
public class AEService extends ServiceMBeanSupport {

    private AuditLoggerDelegate auditLogger = new AuditLoggerDelegate(this);

    private ObjectName echoServiceName;

    private boolean dontSaveIP = true;

    private int[] portNumbers;

    /**
     * @return Returns the echoServiceName.
     */
    public ObjectName getEchoServiceName() {
        return echoServiceName;
    }

    /**
     * @param echoServiceName
     *            The echoServiceName to set.
     */
    public void setEchoServiceName(ObjectName echoServiceName) {
        this.echoServiceName = echoServiceName;
    }
    
    public ObjectName getAuditLoggerName() {
        return auditLogger.getAuditLoggerName();
    }

    public void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogger.setAuditLoggerName(auditLogName);
    }

    /**
     * @return Returns the autoConfig.
     */
    public boolean isDontSaveIP() {
        return dontSaveIP;
    }

    /**
     * @param dontSaveIP
     *            The dontSaveIP to set.
     */
    public void setDontSaveIP(boolean dontSaveIP) {
        this.dontSaveIP = dontSaveIP;
    }

    /**
     * @return Returns the portNumbers.
     */
    public String getPortNumbers() {
        if (portNumbers == null || portNumbers.length < 1)
            return "NONE";
        int len = portNumbers.length;
        String first = String.valueOf(portNumbers[0]);
        if (len == 1)
            return first;
        StringBuffer sb = new StringBuffer(first);
        for (int i = 1; i < len; i++)
            sb.append(",").append(portNumbers[i]);
        return sb.toString();
    }

    /**
     * @param portNumbers
     *            The portNumbers to set.
     */
    public void setPortNumbers(String ports) {
        if (ports == null || "NONE".equalsIgnoreCase(ports)) {
            portNumbers = null;
        } else {
            StringTokenizer st = new StringTokenizer(ports, ",");
            portNumbers = new int[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) {
                portNumbers[i] = Integer.parseInt(st.nextToken());
            }
        }
    }

    public String getAEs() throws Exception {
        Collection c = aeMgr().findAll();
        StringBuffer sb = new StringBuffer();
        AEDTO ae;
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            ae = (AEDTO) iter.next();
            sb.append(ae.toString()).append(" cipher:").append(
                    ae.getCipherSuitesAsString()).append("\r\n");
        }
        return sb.toString();
    }

    public List listAEs() throws Exception {
        return aeMgr().findAll();
    }

    public AEDTO getAE(String title) throws Exception {
        return aeMgr().findByAET(title);
    }

    public boolean updateAETitle(String prevAET, String newAET)
    throws Exception {
        if (prevAET.equals(newAET)) {
            return false;
        }
        AEManager aeManager = aeMgr();
        AEDTO aeData = aeManager.findByAET(prevAET);
        if (aeData == null) {
            return false;
        }
        aeData.setTitle(newAET);
        aeManager.updateAE(aeData);
        return true;
    }
    
    public AEDTO getAE(String title, String host) throws RemoteException,
            Exception {
        return getAE(title, host == null ? null : InetAddress.getByName(host));
    }

    public AEDTO getAE(String aet, InetAddress addr) throws Exception {
        AEManager aetMgr = aeMgr();
        try {
            return aetMgr.findByAET(aet);
        } catch (ObjectNotFoundException e) {
            return autoConfigAE(aet, addr, aetMgr);
        }
    }

    private AEDTO autoConfigAE(String aet, InetAddress addr, AEManager aetMgr) 
            throws Exception {
        if (portNumbers == null || addr == null) {
            return null;
        }
        String aeHost = addr.getHostName();
        for (int i = 0; i < portNumbers.length; i++) {
            AEDTO ae = new AEDTO(-1, aet, aeHost, portNumbers[i], null);
            if (echo(ae)) {
                if (dontSaveIP) {
                    if (!aeHost.equals(addr.getHostAddress()))
                        aetMgr.newAE(ae);
                } else {
                    aetMgr.newAE(ae);
                }
                logActorConfig("Add new auto-configured AE " + ae, 
                        SecurityAlertMessage.NETWORK_CONFIGURATION);
                return ae;
            }
        }
        return null;
    }

    /**
     * Adds (replace) a new AE Title.
     * 
     * @param aet
     *            Application Entity Title
     * @param host
     *            Hostname or IP addr.
     * @param port
     *            port number
     * @param cipher
     *            String with cypher(s) to create a secure connection (seperated
     *            with ',') or null
     * @param checkHost
     *            Enable/disable checking if the host can be resolved.
     * 
     * @throws Exception
     * @throws RemoteException
     */
    public void updateAE(long pk, String title, String host, int port,
            String cipher, boolean checkHost) throws Exception {
        if (checkHost) {
            try {
                host = InetAddress.getByName(host).getCanonicalHostName();
            } catch (UnknownHostException x) {
                throw new IllegalArgumentException(
                        "Host "
                                + host
                                + " cant be resolved! Disable hostname check to add new AE anyway!");
            }
        }

        AEManager aeManager = aeMgr();
        if (pk == -1) {
            AEDTO aeNew = new AEDTO(-1, title, host, port, cipher);
            aeManager.newAE(aeNew);
            logActorConfig("Add AE " + aeNew + " cipher:"
                    + aeNew.getCipherSuitesAsString(), SecurityAlertMessage.NETWORK_CONFIGURATION);
        } else {
            AEDTO aeOld = aeManager.findByPrimaryKey(pk);
            if (!aeOld.getTitle().equals(title)) {
                AEDTO aeOldByTitle = aeManager.findByAET(title);
                if (aeOldByTitle != null) {
                    throw new IllegalArgumentException("AE Title " + title
                            + " already exists!:" + aeOldByTitle);
                }
            }
            AEDTO aeNew = new AEDTO(pk, title, host, port, cipher);
            aeManager.updateAE(aeNew);
            logActorConfig("Modify AE " + aeOld + " -> " + aeNew, SecurityAlertMessage.NETWORK_CONFIGURATION);
        }
    }

    public void addAE(String title, String host, int port, String cipher,
            boolean checkHost) throws Exception {
        updateAE(-1, title, host, port, cipher, checkHost);
    }

    public void removeAE(String titles) throws Exception {
        StringTokenizer st = new StringTokenizer(titles, " ,;\t\r\n");
        AEDTO ae;
        AEManager aeManager = aeMgr();
        while (st.hasMoreTokens()) {
            ae = aeManager.findByAET(st.nextToken());
            aeManager.removeAE(ae.getPk());
            logActorConfig("Remove AE " + ae, SecurityAlertMessage.NETWORK_CONFIGURATION);
        }
    }
   
    private void logActorConfig(String desc, AuditEvent.TypeCode eventTypeCode) {
        log.info(desc);
        try {
            if (auditLogger.isAuditLogIHEYr4()) {
                server.invoke(auditLogger.getAuditLoggerName(), 
                        "logActorConfig",
                        new Object[] { desc, "NetWorking" },
                        new String[] { String.class.getName(), String.class.getName(), });
            } else {
                HttpUserInfo userInfo = new HttpUserInfo();
                SecurityAlertMessage msg = new SecurityAlertMessage(eventTypeCode);
                msg.addReportingProcess(AuditMessage.getProcessID(),
                        AuditMessage.getLocalAETitles(),
                        AuditMessage.getProcessName(),
                        AuditMessage.getLocalHostName());
                msg.addPerformingPerson(userInfo.getUserId(), null, null, userInfo.getHostName());
                msg.addAlertSubjectWithNodeID(AuditMessage.getLocalNodeID(), desc);
                msg.validate();
                Logger.getLogger("auditlog").info(msg);
            }
        } catch (Exception e) {
            log.warn("Failed to log ActorConfig:", e);
        }
    }

    private boolean echo(AEDTO ae) {
        try {
            Boolean result = (Boolean) server.invoke(this.echoServiceName,
                    "checkEcho", new Object[] { ae },
                    new String[] { AEDTO.class.getName() });
            return result.booleanValue();
        } catch (Exception e) {
            log.warn("Failed to use echo service:", e);
            return false;
        }

    }

    protected AEManager aeMgr() throws Exception {
        AEManagerHome home = (AEManagerHome) EJBHomeFactory.getFactory()
                .lookup(AEManagerHome.class, AEManagerHome.JNDI_NAME);
        return home.create();
    }
}
