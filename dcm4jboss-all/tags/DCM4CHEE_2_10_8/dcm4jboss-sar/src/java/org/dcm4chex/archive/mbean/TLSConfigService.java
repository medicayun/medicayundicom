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

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;

import org.dcm4che.util.HandshakeFailedEvent;
import org.dcm4che.util.HandshakeFailedListener;
import org.dcm4che.util.SSLContextAdapter;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 3011 $ $Date: 2006-12-11 07:46:59 +0800 (周一, 11 12月 2006) $
 * @since 13.12.2004
 */
public class TLSConfigService extends ServiceMBeanSupport
		implements HandshakeFailedListener, HandshakeCompletedListener {

    private SSLContextAdapter ssl = SSLContextAdapter.getInstance();

    private String keyStoreURL = "resource:identity.p12";

    private char[] keyStorePassword = { 'p', 'a', 's', 's', 'w', 'd'};

    private String trustStoreURL = "resource:cacerts.jks";

    private char[] trustStorePassword = { 'p', 'a', 's', 's', 'w', 'd'};

    private KeyStore keyStore;

    private KeyStore trustStore;

    private ObjectName auditLogName;

    public TLSConfigService() {
        ssl.addHandshakeFailedListener(this);
        ssl.addHandshakeCompletedListener(this);
    }
    
    public ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }

    public String getEnabledProtocols() {
        return StringUtils.toString(ssl.getEnabledProtocols(), ',');
    }

    public void setEnabledProtocols(String protocols) {
        ssl.setEnabledProtocols(StringUtils.split(protocols, ','));
    }

    public String getSupportedProtocols() {
        try {
            initTLSConf();
            ssl.getSSLContext(); // XXX force to initialize SSLContext
        } catch (Exception e) {
            return e.getMessage();
        }
        return StringUtils.toString(ssl.getSupportedProtocols(), ',');
    }
    
    public boolean isNeedClientAuth() {
        return ssl.isNeedClientAuth();
    }
    
    public void setNeedClientAuth(boolean needClientAuth) {
        ssl.setNeedClientAuth(needClientAuth);
    }

    public final void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword.toCharArray();
    }

    public final String getKeyStoreURL() {
        return keyStoreURL;
    }

    public final void setKeyStoreURL(String keyStoreURL) {
        this.keyStoreURL = keyStoreURL;
        keyStore = null;
    }

    public final void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword.toCharArray();
    }

    public final String getTrustStoreURL() {
        return trustStoreURL;
    }

    public final void setTrustStoreURL(String trustStoreURL) {
        this.trustStoreURL = trustStoreURL;
        trustStore = null;
    }
    
    public final HandshakeFailedListener handshakeFailedListener() {
        return this;
    }

    public final HandshakeCompletedListener handshakeCompletedListener() {
        return this;
    }

    public ServerSocketFactory serverSocketFactory(String[] cipherSuites) {
        if (cipherSuites == null || cipherSuites.length == 0) { return ServerSocketFactory
                .getDefault(); }
        try {
            initTLSConf();
            return ssl.getServerSocketFactory(cipherSuites);
        } catch (GeneralSecurityException e) {
            throw new ConfigurationException(e);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }

    public SocketFactory socketFactory(String[] cipherSuites) {
        if (cipherSuites == null || cipherSuites.length == 0) { return SocketFactory
                .getDefault(); }
        try {
            initTLSConf();
            return ssl.getSocketFactory(cipherSuites);
        } catch (GeneralSecurityException e) {
            throw new ConfigurationException(e);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }
    
    private void initTLSConf() throws GeneralSecurityException, IOException {
        if (keyStore == null) {
            keyStore = ssl.loadKeyStore(keyStoreURL, keyStorePassword);
            ssl.setKey(keyStore, keyStorePassword);
        }
        if (trustStore == null) {
            trustStore = ssl.loadKeyStore(trustStoreURL, trustStorePassword);
            ssl.setTrust(trustStore);
        }
    }

    protected void startService() throws Exception {
        // force reload of key/truststore
        keyStore = null;
        trustStore = null;
    }

    protected void stopService() throws Exception {
    }
    
    public void handshakeFailed(HandshakeFailedEvent event) {
       sendNotification(event);
       logSecurityAlert("NodeAuthentification", 
                    event.getSocket(), null,
                    event.getException().getMessage());
    }


    public void handshakeCompleted(HandshakeCompletedEvent event) {
        sendNotification(event);
    }

    private void sendNotification(Object event) {
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(event.getClass().getName(), this, 
                eventID);
        notif.setUserData(event);
        super.sendNotification(notif);
    }
    
    private void logSecurityAlert(String alertType, Socket socket, String aet,
            String description) {
        if (auditLogName == null) return;
        try {
            server.invoke(auditLogName, "logSecurityAlert",
                new Object[] { alertType, socket, aet, description },
                new String[] { String.class.getName(), Socket.class.getName(),
                    String.class.getName(), String.class.getName()});
        } catch (Exception e) {
            log.warn("Audit Log failed:", e);
        }
    }
}
