/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.movescu;

import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.mbean.TLSConfigDelegate;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1629 $ $Date: 2005-03-18 18:06:31 +0800 (周五, 18 3月 2005) $
 * @since 17.12.2003
 */
public class MoveScuService extends ServiceMBeanSupport implements
        MessageListener {

    private static final String DEF_CALLING_AET = "MOVE_SCU";

    private static final String DEF_CALLED_AET = "QR_SCP";

    private static final Map dumpParam = new HashMap(5);
    static {
        dumpParam.put("maxlen", new Integer(128));
        dumpParam.put("vallen", new Integer(64));
        dumpParam.put("prefix", "\t");
    }

    private TLSConfigDelegate tlsConfig = new TLSConfigDelegate(this);

    private String callingAET = DEF_CALLING_AET;

    private String calledAET = DEF_CALLED_AET;

    private int acTimeout;

    private int dimseTimeout;

    private int soCloseDelay;

    private RetryIntervalls retryIntervalls = new RetryIntervalls();

    private PooledExecutor pool = new PooledExecutor();

    public MoveScuService() {
        pool.waitWhenBlocked();
    }

    public final ObjectName getTLSConfigName() {
        return tlsConfig.getTLSConfigName();
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        tlsConfig.setTLSConfigName(tlsConfigName);
    }

    public final String getCalledAET() {
        return calledAET;
    }

    public final void setCalledAET(String retrieveAET) {
        this.calledAET = retrieveAET;
    }

    public final int getAcTimeout() {
        return acTimeout;
    }

    public final void setAcTimeout(int acTimeout) {
        this.acTimeout = acTimeout;
    }

    public final int getDimseTimeout() {
        return dimseTimeout;
    }

    public final void setDimseTimeout(int dimseTimeout) {
        this.dimseTimeout = dimseTimeout;
    }

    public final int getSoCloseDelay() {
        return soCloseDelay;
    }

    public final void setSoCloseDelay(int soCloseDelay) {
        this.soCloseDelay = soCloseDelay;
    }
    
    public final int getConcurrency() {
        return pool.getMaximumPoolSize();
    }

    public final void setConcurrency(int concurrency) {
        pool.setMaximumPoolSize(concurrency);
    }

    public String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public void setRetryIntervalls(String text) {
        retryIntervalls = new RetryIntervalls(text);
    }

    public String getCallingAET() {
        return callingAET;
    }

    public void setCallingAET(String aet) {
        this.callingAET = aet;
    }

    protected void startService() throws Exception {
        JMSDelegate.startListening(MoveOrder.QUEUE, this);
    }

    protected void stopService() throws Exception {
        JMSDelegate.stopListening(MoveOrder.QUEUE);
    }

    void queueFailedMoveOrder(MoveOrder order) {
        final long delay = retryIntervalls
                .getIntervall(order.getFailureCount());
        if (delay == -1L) {
            log.error("Give up to process move order: " + order);
        } else {
            log.warn("Failed to process " + order + ". Scheduling retry.");
            try {
                JMSDelegate.queue(MoveOrder.QUEUE, order,
                        0,
                        System.currentTimeMillis() + delay);
            } catch (JMSException e) {
                log.error("Failed to reschedule order: " + order);
            }
        }
    }

    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        MoveOrder order = null;
        try {
            pool.execute(new MoveCmd(this, order = (MoveOrder) om.getObject()));
        } catch (JMSException e) {
            log.error("jms error during processing message: " + message, e);
        } catch (InterruptedException e) {
            log.error("Failed to process " + order, e);
        }
    }

    void logDataset(String prompt, Dataset ds) {
        if (!log.isDebugEnabled()) { return; }
        log.debug(prompt);
        log.debug( ds );
    }

    Socket createSocket(AEData aeData) throws IOException {
        return tlsConfig.createSocket(aeData);
    }
}