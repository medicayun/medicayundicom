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
package org.dcm4chex.archive.dcm;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

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
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.jboss.system.ServiceMBeanSupport;

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;
import EDU.oswego.cs.dl.util.concurrent.Semaphore;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1131 $
 * @since 02.08.2003
 */
public class DcmServerService extends ServiceMBeanSupport implements
        HandshakeFailedListener {

    
    private static final String GET_AUDIT_LOGGER = "getAuditLogger";

    private static final AuditLoggerFactory alf = AuditLoggerFactory
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

    private int maxConcurrentCodec = 1;
    
    private Semaphore codecSemaphore = new FIFOSemaphore(maxConcurrentCodec);
    
    public DcmServerService() {
        dcmsrv.addHandshakeFailedListener(this);
        ssl.addHandshakeFailedListener(this);
    }

    public ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }

    public AuditLogger getAuditLogger() {
        return auditLogger;
    }

    public int getPort() {
        return dcmsrv.getPort();
    }

    public void setPort(int port) {
        dcmsrv.setPort(port);
    }

    public String getProtocolName() {
        return protocol.toString();
    }

    public void setProtocolName(String protocolName) {
        this.protocol = DcmProtocol.valueOf(protocolName);
    }

    public DcmHandler getDcmHandler() {
        return handler;
    }

    public SSLContextAdapter getSSLContextAdapter() {
        return ssl;
    }
    
    public int getRqTimeout() {
        return handler.getRqTimeout();
    }

    public void setRqTimeout(int newRqTimeout) {
        handler.setRqTimeout(newRqTimeout);
    }

    public int getDimseTimeout() {
        return handler.getDimseTimeout();
    }

    public void setDimseTimeout(int newDimseTimeout) {
        handler.setDimseTimeout(newDimseTimeout);
    }

    public int getSoCloseDelay() {
        return handler.getSoCloseDelay();
    }

    public void setSoCloseDelay(int newSoCloseDelay) {
        handler.setSoCloseDelay(newSoCloseDelay);
    }

    public boolean isPackPDVs() {
        return handler.isPackPDVs();
    }

    public void setPackPDVs(boolean newPackPDVs) {
        handler.setPackPDVs(newPackPDVs);
    }

    public int getMaxClients() {
        return dcmsrv.getMaxClients();
    }

    public void setMaxClients(int newMaxClients) {
        dcmsrv.setMaxClients(newMaxClients);
    }

    public int getNumClients() {
        return dcmsrv.getNumClients();
    }

    public String[] getCallingAETs() {
        return policy.getCallingAETs();
    }

    public void setCallingAETs(String[] newCallingAETs) {
        policy.setCallingAETs(newCallingAETs);
    }

    public String[] getCalledAETs() {
        return policy.getCalledAETs();
    }

    public void setCalledAETs(String[] newCalledAETs) {
        policy.setCalledAETs(newCalledAETs);
    }

    public int getMaxPDULength() {
        return policy.getMaxPDULength();
    }

    public void setMaxPDULength(int newMaxPDULength) {
        policy.setMaxPDULength(newMaxPDULength);
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

    public final int getMaxConcurrentCodec() {
        return maxConcurrentCodec;
    }
    
    public final void setMaxConcurrentCodec(int maxConcurrentCodec) {
        codecSemaphore = new FIFOSemaphore(maxConcurrentCodec);
        this.maxConcurrentCodec = maxConcurrentCodec;
    }
    
    public Semaphore getCodecSemaphore() {
        return codecSemaphore;
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
        if (auditLogName != null) {
            auditLogger = (AuditLogger) server.invoke(auditLogName,
                    GET_AUDIT_LOGGER, null, null);
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