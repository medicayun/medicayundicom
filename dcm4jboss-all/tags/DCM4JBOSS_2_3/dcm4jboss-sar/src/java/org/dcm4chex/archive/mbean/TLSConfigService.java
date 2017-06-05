/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.management.ObjectName;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.dcm4che.util.HandshakeFailedEvent;
import org.dcm4che.util.HandshakeFailedListener;
import org.dcm4che.util.SSLContextAdapter;
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1292 $ $Date: 2004-12-14 00:56:26 +0800 (周二, 14 12月 2004) $
 * @since 13.12.2004
 */
public class TLSConfigService extends ServiceMBeanSupport
		implements HandshakeFailedListener {

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
    }
    
    public ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
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
    
    public final HandshakeFailedListener getHandshakeFailedListener() {
        return this;
    }

    public ServerSocketFactory getServerSocketFactory(String[] cipherSuites) {
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

    public SocketFactory getSocketFactory(String[] cipherSuites) {
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
    
    //  HandshakeFailedListener Implementation-------------------------------
    public void handshakeFailed(HandshakeFailedEvent event) {
        logSecurityAlert("NodeAuthentification", 
                    event.getSocket(), null,
                    event.getException().getMessage());
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
