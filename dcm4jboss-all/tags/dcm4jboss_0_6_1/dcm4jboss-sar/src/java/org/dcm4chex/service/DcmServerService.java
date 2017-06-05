/*
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
package org.dcm4chex.service;

import java.beans.PropertyEditor;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.DcmProtocol;
import org.dcm4che.util.HandshakeFailedEvent;
import org.dcm4che.util.HandshakeFailedListener;
import org.dcm4che.util.SSLContextAdapter;
import org.dcm4chex.service.util.AETsEditor;
import org.dcm4chex.service.util.ConfigurationException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 * 
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1049 $
 * @since 02.08.2003
 */
public class DcmServerService extends ServiceMBeanSupport implements
        HandshakeFailedListener, org.dcm4chex.service.DcmServerServiceMBean {

    private final static AuditLoggerFactory alf = AuditLoggerFactory
            .getInstance();

    private ServerFactory sf = ServerFactory.getInstance();

    private AssociationFactory af = AssociationFactory.getInstance();

    private AcceptorPolicy policy = af.newAcceptorPolicy();

    private DcmServiceRegistry services = af.newDcmServiceRegistry();

    private DcmHandler handler = sf.newDcmHandler(policy, services);

    private Server dcmsrv = sf.newServer(handler);

    private SSLContextAdapter ssl = SSLContextAdapter.getInstance();

    private DcmProtocol protocol = DcmProtocol.DICOM;

    private String keyStoreURL = "resource:identity.p12";

    private char[] keyStorePassword = { 'p', 'a', 's', 's', 'w', 'd'};

    private String trustStoreURL = "resource:cacerts.jks";

    private char[] trustStorePassword = { 'p', 'a', 's', 's', 'w', 'd'};

    private KeyStore keyStore;

    private KeyStore trustStore;

    private ObjectName auditLogName;

    private AuditLogger auditLogger;

    private String actorName;

    public DcmServerService() {
        dcmsrv.addHandshakeFailedListener(this);
        ssl.addHandshakeFailedListener(this);
    }

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
    public AuditLogger getAuditLogger() {
        return auditLogger;
    }

    /**
     * @jmx.managed-attribute
     */
    public int getPort() {
        return dcmsrv.getPort();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setPort(int port) {
        dcmsrv.setPort(port);
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
        this.protocol = DcmProtocol.valueOf(protocolName);
    }

    /**
     * @jmx.managed-attribute
     */
    public DcmHandler getDcmHandler() {
        return handler;
    }

    /**
     * @jmx.managed-attribute
     */
    public SSLContextAdapter getSSLContextAdapter() {
        return ssl;
    }

    /**
     * @jmx.managed-attribute
     */
    public int getRqTimeout() {
        return handler.getRqTimeout();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setRqTimeout(int newRqTimeout) {
        handler.setRqTimeout(newRqTimeout);
    }

    /**
     * @jmx.managed-attribute
     */
    public int getDimseTimeout() {
        return handler.getDimseTimeout();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setDimseTimeout(int newDimseTimeout) {
        handler.setDimseTimeout(newDimseTimeout);
    }

    /**
     * @jmx.managed-attribute
     */
    public int getSoCloseDelay() {
        return handler.getSoCloseDelay();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setSoCloseDelay(int newSoCloseDelay) {
        handler.setSoCloseDelay(newSoCloseDelay);
    }

    /**
     * @jmx.managed-attribute
     */
    public boolean isPackPDVs() {
        return handler.isPackPDVs();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setPackPDVs(boolean newPackPDVs) {
        handler.setPackPDVs(newPackPDVs);
    }

    /**
     * @jmx.managed-attribute
     */
    public int getMaxClients() {
        return dcmsrv.getMaxClients();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setMaxClients(int newMaxClients) {
        dcmsrv.setMaxClients(newMaxClients);
    }

    /**
     * @jmx.managed-attribute
     */
    public int getNumClients() {
        return dcmsrv.getNumClients();
    }

    /**
     * @jmx.managed-attribute
     */
    public String getCallingAETs() {
        PropertyEditor pe = new AETsEditor();
        pe.setValue(policy.getCallingAETs());
        return pe.getAsText();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setCallingAETs(String newCallingAETs) {
        PropertyEditor pe = new AETsEditor();
        pe.setAsText(newCallingAETs);
        policy.setCallingAETs((String[]) pe.getValue());
    }

    /**
     * @jmx.managed-attribute
     */
    public String getCalledAETs() {
        PropertyEditor pe = new AETsEditor();
        pe.setValue(policy.getCalledAETs());
        return pe.getAsText();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setCalledAETs(String newCalledAETs) {
        PropertyEditor pe = new AETsEditor();
        pe.setAsText(newCalledAETs);
        policy.setCalledAETs((String[]) pe.getValue());
    }

    /**
     * @jmx.managed-attribute
     */
    public int getMaxPDULength() {
        return policy.getMaxPDULength();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setMaxPDULength(int newMaxPDULength) {
        policy.setMaxPDULength(newMaxPDULength);
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
        keyStore = null;
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
        trustStore = null;
    }

    /**
     * @jmx.managed-operation
     */
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

    /**
     * @jmx.managed-operation
     */
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

    protected ObjectName getObjectName(MBeanServer server, ObjectName name) {
        actorName = name.getKeyProperty("name");
        return name;
    }

    protected void startService() throws Exception {
        // force reload of key/truststore
        keyStore = null;
        trustStore = null;
        if (auditLogName != null) {
            auditLogger = (AuditLogger) server.getAttribute(auditLogName,
                    "AuditLogger");
        }
        dcmsrv.setServerSocketFactory(getServerSocketFactory(protocol
                .getCipherSuites()));
        dcmsrv.start();
    }

    protected void stopService() throws Exception {
        dcmsrv.stop();
    }

    //  HandshakeFailedListener Implementation-------------------------------
    public void handshakeFailed(HandshakeFailedEvent event) {
        if (auditLogger != null) {
            auditLogger.logSecurityAlert("NodeAuthentification", alf
                    .newRemoteUser(alf.newRemoteNode(event.getSocket(), null)),
                    event.getException().getMessage());
        }
    }
}