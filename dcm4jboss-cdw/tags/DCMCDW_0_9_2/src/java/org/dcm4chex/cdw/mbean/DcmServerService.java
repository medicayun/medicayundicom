/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.dcm4che.net.AETFilter;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.jboss.system.ServiceMBeanSupport;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4161 $ $Date: 2004-07-19 18:32:39 +0800 (周一, 19 7月 2004) $
 * @since 21.06.2004
 *
 */
public class DcmServerService extends ServiceMBeanSupport {

    private static final String ANY = "ANY";
    
    private Properties aetMap = new Properties();
    
    private ServerFactory sf = ServerFactory.getInstance();

    private AssociationFactory af = AssociationFactory.getInstance();

    private AcceptorPolicy policy = af.newAcceptorPolicy();

    private DcmServiceRegistry services = af.newDcmServiceRegistry();

    private DcmHandler handler = sf.newDcmHandler(policy, services);

    private Server dcmsrv = sf.newServer(handler);
    
    private final AETFilter aetFilter = new AETFilter() {

        public boolean accept(String aet) {
            return aetMap.containsKey(aet);
        }
        
    };

    private static String[] nullToAny(String[] aets) {
        return aets == null || aets.length == 0 ? new String[]{ ANY } : aets;
    }
    
    public String lookupMediaWriterName(String aet) {
        return aetMap.getProperty(aet);
    }
    
    public void setCallingAETs(String[] callingAETs) {
        policy.setCallingAETs(anyToNull(callingAETs));
    }

    public String[] getCallingAETs() {
        return nullToAny(policy.getCallingAETs());
    }
    
    private String[] anyToNull(String[] aets) {
        return Arrays.asList(aets).indexOf(ANY) == -1 ? aets : null;
    }

    public void setAETofMediaWriter(String s) {
        Properties p = new Properties();
        try {
            p.load(new ByteArrayInputStream(s.replace(',','\n').getBytes()));
        } catch (IOException e) {
            throw new IllegalArgumentException(s);
        }
        aetMap = p;
    }

    public String getAETofMediaWriter() {
        StringBuffer sb = new StringBuffer();
        Iterator it = aetMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append(',');
        }
        return sb.toString();
    }
    
    protected void startService() throws Exception {
        policy.setCalledAETFilter(aetFilter);    
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

    public void setPort(int port) throws Exception {
        if (getPort() == port) return;
        dcmsrv.setPort(port);
        if (getState() == STARTED) {
            stop();
            Thread.sleep(5000L);
            start();
        }
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
