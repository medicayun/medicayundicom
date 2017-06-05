/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.movescu;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1170 $ $Date: 2004-10-04 05:56:22 +0800 (周一, 04 10月 2004) $
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

    private String callingAET = DEF_CALLING_AET;

    private String calledAET = DEF_CALLED_AET;

    private String dsJndiName = "java:/DefaultDS";

    private DataSource datasource;

    private QueueSession jmsSession;

    private RetryIntervalls retryIntervalls = new RetryIntervalls();

    private PooledExecutor pool = new PooledExecutor();

    public MoveScuService() {
        pool.waitWhenBlocked();
    }

    public final String getCalledAET() {
        return calledAET;
    }

    public final void setCalledAET(String retrieveAET) {
        this.calledAET = retrieveAET;
    }

    public final String getDataSourceJndiName() {
        return dsJndiName;
    }

    public final void setDataSourceJndiName(String jndiName) {
        this.dsJndiName = jndiName;
    }

    public final int getConcurrency() {
        return pool.getMaximumPoolSize();
    }

    public final void setConcurrency(int concurrency) {
        pool.setMaximumPoolSize(concurrency);
    }

    public DataSource getDataSource() throws ConfigurationException {
        if (datasource == null) {
            try {
                Context jndiCtx = new InitialContext();
                try {
                    datasource = (DataSource) jndiCtx.lookup(dsJndiName);
                } finally {
                    try {
                        jndiCtx.close();
                    } catch (NamingException ignore) {
                    }
                }
            } catch (NamingException ne) {
                throw new ConfigurationException(
                        "Failed to access Data Source: " + dsJndiName, ne);
            }
        }
        return datasource;
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
        if (this.jmsSession != null) {
            log.warn("Closing existing JMS Session for receiving messages");
            this.jmsSession.close();
            this.jmsSession = null;
        }
        this.jmsSession = JMSDelegate.getInstance(MoveOrder.QUEUE)
                .setMessageListener(this);
    }

    protected void stopService() throws Exception {
        if (jmsSession != null) {
            jmsSession.close();
            jmsSession = null;
        }
    }

    void queueFailedMoveOrder(MoveOrder order) {
        final long delay = retryIntervalls
                .getIntervall(order.getFailureCount());
        if (delay == -1L) {
            log.error("Give up to process move order: " + order);
        } else {
            log.warn("Failed to process " + order + ". Scheduling retry.");
            try {
                JMSDelegate.getInstance(MoveOrder.QUEUE).queueMessage(order,
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
        try {
            StringWriter w = new StringWriter();
            w.write(prompt);
            ds.dumpDataset(w, dumpParam);
            log.debug(w.toString());
        } catch (Exception e) {
            log.warn("Failed to dump dataset", e);
        }
    }
}