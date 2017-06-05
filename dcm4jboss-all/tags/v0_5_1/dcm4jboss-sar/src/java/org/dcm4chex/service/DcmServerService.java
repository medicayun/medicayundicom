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
import java.io.File;
import java.security.GeneralSecurityException;

import javax.management.ObjectName;
import javax.net.ServerSocketFactory;

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
import org.dcm4chex.service.util.ConfigFileEditor;
import org.dcm4chex.service.util.ConfigurationException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @jmx.mbean
 *  extends="org.jboss.system.ServiceMBean"
 * 
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 873 $
 * @since 02.08.2003
 */
public class DcmServerService
    extends ServiceMBeanSupport
    implements HandshakeFailedListener, org.dcm4chex.service.DcmServerServiceMBean {

    private ObjectName deviceConfigName;               
    private ServerFactory sf = ServerFactory.getInstance();
    private AssociationFactory af = AssociationFactory.getInstance();
    private AcceptorPolicy policy = af.newAcceptorPolicy();
    private DcmServiceRegistry services = af.newDcmServiceRegistry();
    private DcmHandler handler = sf.newDcmHandler(policy, services);
    private Server dcmsrv = sf.newServer(handler);
    private SSLContextAdapter ssl = SSLContextAdapter.getInstance();
    private DcmProtocol protocol = DcmProtocol.DICOM;
    private File keyFile;
    private String keyPasswd;
    private File lastKeyFile;
    private long lastKeyFileModified;
    private File cacertsFile;
    private String cacertsPasswd;
    private File lastCacertsFile;
    private long lastCacertsFileModified;

    /**
     * @jmx.managed-attribute
     */
    public ObjectName getDeviceConfigName() {
        return deviceConfigName;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setDeviceConfigName(ObjectName deviceConfigName) {
        this.deviceConfigName = deviceConfigName;
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
    public String getKeyFile() {
        PropertyEditor pe = new ConfigFileEditor();
        pe.setValue(keyFile);
        return pe.getAsText();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setKeyFile(String keyFile) {
        PropertyEditor pe = new ConfigFileEditor();
        pe.setAsText(keyFile);
        this.keyFile = (File) pe.getValue();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setKeyPasswd(String keyPasswd) {
        this.keyPasswd = keyPasswd;
    }

    /**
     * @jmx.managed-attribute
     */
    public String getCacertsFile() {
        PropertyEditor pe = new ConfigFileEditor();
        pe.setValue(cacertsFile);
        return pe.getAsText();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setCacertsFile(String cacertsFile) {
        PropertyEditor pe = new ConfigFileEditor();
        pe.setAsText(cacertsFile);
        this.cacertsFile = (File) pe.getValue();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setCacertsPasswd(String cacertsPasswd) {
        this.cacertsPasswd = cacertsPasswd;
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

    private ServerSocketFactory getServerSocketFactory()
        throws ConfigurationException {
        if (!protocol.isTLS()) {
            return ServerSocketFactory.getDefault();
        }
        reloadKey();
        reloadCacerts();
        try {
            return ssl.getServerSocketFactory(protocol.getCipherSuites());
        } catch (GeneralSecurityException e) {
            throw new ConfigurationException(e);
        }
    }

    private void reloadKey() throws ConfigurationException {
        if (keyFile.equals(lastKeyFile)
            && keyFile.lastModified() == lastKeyFileModified) {
            return;
        }
        char[] passwd = keyPasswd.toCharArray();
        try {
            ssl.setKey(ssl.loadKeyStore(keyFile, passwd), passwd);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
        lastKeyFile = keyFile;
        lastKeyFileModified = keyFile.lastModified();
    }

    private void reloadCacerts() throws ConfigurationException {
        if (cacertsFile.equals(lastCacertsFile)
            && cacertsFile.lastModified() == lastCacertsFileModified) {
            return;
        }
        try {
            ssl.setTrust(
                ssl.loadKeyStore(cacertsFile, cacertsPasswd.toCharArray()));
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
        lastCacertsFile = cacertsFile;
        lastCacertsFileModified = cacertsFile.lastModified();
    }

    protected void startService() throws Exception {
        dcmsrv.addHandshakeFailedListener(this);
        dcmsrv.setServerSocketFactory(getServerSocketFactory());
        dcmsrv.start();
    }

    protected void stopService() throws Exception {
        dcmsrv.stop();
        dcmsrv.removeHandshakeFailedListener(this);
    }

    //  HandshakeFailedListener Implementation-------------------------------
    public void handshakeFailed(HandshakeFailedEvent event) {}

}
