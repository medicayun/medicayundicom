/* $Id: HL7ServerService.java 1047 2004-03-14 22:41:26Z gunterze $
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
package org.dcm4chex.archive.hl7;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.management.ObjectName;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.util.MLLP_Protocol;
import org.dcm4che.util.SSLContextAdapter;
import org.jboss.system.ServiceMBeanSupport;

import ca.uhn.hl7v2.app.Application;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1047 $ $Date: 2004-03-15 06:41:26 +0800 (周一, 15 3月 2004) $
 * @since 24.02.2004
 * 
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class HL7ServerService extends ServiceMBeanSupport implements
        org.dcm4chex.archive.hl7.HL7ServerServiceMBean {

    private final static AuditLoggerFactory alf = AuditLoggerFactory
            .getInstance();

    private HL7Server hl7srv = new HL7Server(this);

    private SSLContextAdapter ssl = SSLContextAdapter.getInstance();

    private MLLP_Protocol protocol = MLLP_Protocol.MLLP;

    private String keyStoreURL = "resource:identity.p12";

    private char[] keyStorePassword = { 'p', 'a', 's', 's', 'w', 'd'};

    private String trustStoreURL = "resource:cacerts.jks";

    private char[] trustStorePassword = { 'p', 'a', 's', 's', 'w', 'd'};

    private ObjectName auditLogName;

    private AuditLogger auditLogger;

    /**
     * @jmx.managed-attribute
     */
    public ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }

    /**
     * @jmx.managed-attribute
     */
    public int getPort() {
        return hl7srv.getPort();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setPort(int port) {
        hl7srv.setPort(port);
    }

    /**
     * @jmx.managed-attribute
     */
    public String getProtocolName() {
        return protocol.toString();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setProtocolName(String protocolName) {
        this.protocol = MLLP_Protocol.valueOf(protocolName);
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword.toCharArray();
    }

    /**
     * @return Returns the keyStoreURL.
     */
    public final String getKeyStoreURL() {
        return keyStoreURL;
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setKeyStoreURL(String keyStoreURL) {
        this.keyStoreURL = keyStoreURL;
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword.toCharArray();
    }

    /**
     * @jmx.managed-attribute
     */
    public final String getTrustStoreURL() {
        return trustStoreURL;
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setTrustStoreURL(String trustStoreURL) {
        this.trustStoreURL = trustStoreURL;
    }

    /**
     * @jmx.managed-operation
     */
    public void registerApplication(String messageType, String triggerEvent,
            Application handler) {
        hl7srv.registerApplication(messageType, triggerEvent, handler);
    }

    protected void startService() throws Exception {
        if (auditLogName != null) {
            auditLogger = (AuditLogger) server.getAttribute(auditLogName,
                    "AuditLogger");
        }
        hl7srv.setServerSocketFactory(getServerSocketFactory(protocol
                .getCipherSuites()));
        hl7srv.start();
    }

    private ServerSocketFactory getServerSocketFactory(String[] cipherSuites)
            throws GeneralSecurityException, IOException {
        if (cipherSuites == null || cipherSuites.length == 0) { return ServerSocketFactory
                .getDefault(); }
        ssl.setKey(ssl.loadKeyStore(keyStoreURL, keyStorePassword),
                keyStorePassword);
        ssl.setTrust(ssl.loadKeyStore(trustStoreURL, trustStorePassword));
        return ssl.getServerSocketFactory(protocol.getCipherSuites());
    }

    protected void stopService() throws Exception {
        hl7srv.stop();
    }

    /**
     * @param newSocket
     */
    public void handshake(SSLSocket s) {
        try {
            s.startHandshake();
            if (log.isInfoEnabled()) {
                SSLSession se = s.getSession();
                try {
                    X509Certificate cert = (X509Certificate) se
                            .getPeerCertificates()[0];
                    log.info(s.getInetAddress().toString() + ": accept "
                            + se.getCipherSuite() + " with "
                            + cert.getSubjectDN());
                } catch (SSLPeerUnverifiedException e) {
                    log.error("SSL peer not verified:", e);
                }
            }
        } catch (IOException e) {
            logHandshakeFailed(s, e);
        }

    }

    private void logHandshakeFailed(SSLSocket s, IOException e) {
        if (auditLogger != null) {
            auditLogger.logSecurityAlert("NodeAuthentification", alf
                    .newRemoteUser(alf.newRemoteNode(s, null)), e.getMessage());
        }
    }
}
