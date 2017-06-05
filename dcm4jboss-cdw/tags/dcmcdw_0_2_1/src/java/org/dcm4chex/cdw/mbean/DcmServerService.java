/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.util.Arrays;

import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.jboss.system.ServiceMBeanSupport;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4127 $ $Date: 2004-06-24 21:34:08 +0800 (周四, 24 6月 2004) $
 * @since 21.06.2004
 *
 */
public class DcmServerService extends ServiceMBeanSupport {

    private static final String ANY = "ANY";
    
    private ServerFactory sf = ServerFactory.getInstance();

    private AssociationFactory af = AssociationFactory.getInstance();

    private AcceptorPolicy policy = af.newAcceptorPolicy();

    private DcmServiceRegistry services = af.newDcmServiceRegistry();

    private DcmHandler handler = sf.newDcmHandler(policy, services);

    private Server dcmsrv = sf.newServer(handler);

    private String[] nullToAny(String[] aets) {
        return aets == null || aets.length == 0 ? new String[]{ ANY } : aets;
    }

    public String[] getCalledAETs() {
        return nullToAny(policy.getCalledAETs());
    }

    public String[] getCallingAETs() {
        return nullToAny(policy.getCallingAETs());
    }

    private String[] anyToNull(String[] aets) {
        return Arrays.asList(aets).indexOf(ANY) == -1 ? aets : null;
    }

    public void setCalledAETs(String[] aets) {
        policy.setCalledAETs(anyToNull(aets));
    }

    public void setCallingAETs(String[] aets) {
        policy.setCallingAETs(anyToNull(aets));
    }
    
    protected void startService() throws Exception {
        dcmsrv.start();
    }

    protected void stopService() throws Exception {
        dcmsrv.stop();
    }

    public int getMaxClients() {
        return dcmsrv.getMaxClients();
    }

    public int getNumClients() {
        return dcmsrv.getNumClients();
    }

    public int getPort() {
        return dcmsrv.getPort();
    }

    public void setMaxClients(int max) {
        dcmsrv.setMaxClients(max);
    }

    public void setPort(int port) {
        dcmsrv.setPort(port);
    }

    public int getDimseTimeout() {
        return handler.getDimseTimeout();
    }

    public int getRqTimeout() {
        return handler.getRqTimeout();
    }

    public int getSoCloseDelay() {
        return handler.getSoCloseDelay();
    }

    public boolean isPackPDVs() {
        return handler.isPackPDVs();
    }

    public void setDimseTimeout(int dimseTimeout) {
        handler.setDimseTimeout(dimseTimeout);
    }

    public void setPackPDVs(boolean packPDVs) {
        handler.setPackPDVs(packPDVs);
    }

    public void setRqTimeout(int timeout) {
        handler.setRqTimeout(timeout);
    }

    public void setSoCloseDelay(int soCloseDelay) {
        handler.setSoCloseDelay(soCloseDelay);
    }

    public final DcmHandler getDcmHandler() {
        return handler;
    }

    public int getMaxPDULength() {
        return policy.getMaxPDULength();
    }

    public void setMaxPDULength(int maxLength) {
        policy.setMaxPDULength(maxLength);
    }
}
