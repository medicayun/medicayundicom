/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.util;

import java.io.Serializable;
import java.util.HashMap;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4chex.archive.exceptions.ConfigurationException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1158 $ $Date: 2004-08-29 05:54:50 +0800 (周日, 29 8月 2004) $
 * @since 23.08.2004
 *
 */
public class JMSDelegate {

    public static final String CONNECTION_FACTORY = "java:ConnectionFactory";

    /**
     * JBoss-vendor specific property for scheduling a JMS message. In
     * milliseconds since January 1, 1970.
     */
    public static final String PROPERTY_SCHEDULED_DELIVERY = "JMS_JBOSS_SCHEDULED_DELIVERY";

    public static int toJMSPriority(int dcmPriority) {
        if (dcmPriority == 1) return 5;
        if (dcmPriority == 2) return 3;
        return 4;
    }
    
    private static HashMap map = new HashMap();

    public synchronized static JMSDelegate getInstance(String qname) {
        JMSDelegate instance = (JMSDelegate) map.get(qname);
        if (instance == null)
                map.put(qname, instance = new JMSDelegate(qname));
        return instance;
    }

    private static QueueConnection conn;

    private final String qname;

    private final Queue queue;

    private int deliveryMode = DeliveryMode.PERSISTENT;

    public JMSDelegate(String qname) {
        this.qname = qname;
        InitialContext iniCtx = null;
        try {
            iniCtx = new InitialContext();
            if (conn == null) {
                Object tmp = iniCtx.lookup(CONNECTION_FACTORY);
                QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
                conn = qcf.createQueueConnection();
                conn.start();
            }
            queue = (Queue) iniCtx.lookup("queue/" + qname);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } catch (JMSException e) {
            throw new ConfigurationException(e);
        } finally {
            if (iniCtx != null) {
                try {
                    iniCtx.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public QueueSession setMessageListener(MessageListener listener)
            throws JMSException {
        QueueSession session = conn.createQueueSession(false,
                QueueSession.AUTO_ACKNOWLEDGE);
        QueueReceiver receiver = session.createReceiver(queue);
        receiver.setMessageListener(listener);
        return session;
    }

    public void queueMessage(Serializable obj, int priority,
            long scheduledTime) throws JMSException {
        QueueSession session = conn.createQueueSession(false,
                QueueSession.AUTO_ACKNOWLEDGE);
        try {
            ObjectMessage msg = session.createObjectMessage(obj);
            if (scheduledTime > 0L)
                    msg.setLongProperty(PROPERTY_SCHEDULED_DELIVERY,
                            scheduledTime);
            QueueSender sender = session.createSender(queue);
            sender.send(msg,
                    deliveryMode,
                    priority,
                    ObjectMessage.DEFAULT_TIME_TO_LIVE);
        } finally {
            session.close();
        }
    }

}