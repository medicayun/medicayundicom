/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm;

import javax.management.ObjectName;

import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.util.DcmProtocol;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1744 $
 * @since 02.08.2003
 */
public class DcmServerService extends ServiceMBeanSupport {

    
    private ServerFactory sf = ServerFactory.getInstance();

    private AssociationFactory af = AssociationFactory.getInstance();

    private AcceptorPolicy policy = af.newAcceptorPolicy();

    private DcmServiceRegistry services = af.newDcmServiceRegistry();

    private DcmHandler handler = sf.newDcmHandler(policy, services);

    private Server dcmsrv = sf.newServer(handler);
    
    private DcmProtocol protocol = DcmProtocol.DICOM;

    private ObjectName auditLogName;

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    public ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
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

    public DcmHandler dcmHandler() {
        return handler;
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

    public int getMaxIdleThreads() {
        return dcmsrv.getMaxIdleThreads();
    }
    
    public int getNumIdleThreads() {
        return dcmsrv.getNumIdleThreads();
    }
    
    public void setMaxIdleThreads(int max) {
        dcmsrv.setMaxIdleThreads(max);
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

    
    protected void startService() throws Exception {
        dcmsrv.addHandshakeFailedListener(tlsConfig.handshakeFailedListener());
        dcmsrv.setServerSocketFactory(tlsConfig.serverSocketFactory(protocol
                .getCipherSuites()));
        dcmsrv.start();
    }

    protected void stopService() throws Exception {
        dcmsrv.stop();
    }
}