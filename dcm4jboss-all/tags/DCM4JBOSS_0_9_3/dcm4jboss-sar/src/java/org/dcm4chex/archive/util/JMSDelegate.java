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
 * @version $Revision: 1200 $ $Date: 2004-11-15 21:44:13 +0800 (周一, 15 11月 2004) $
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

    public static void startListening(String name, MessageListener listener) {
        if (map.containsKey(name))
            throw new IllegalStateException("Already listening on queue " + name);
        map.put(name, new JMSDelegate(name, listener));
    }

    public static void stopListening(String name) {
        JMSDelegate jms = (JMSDelegate) map.remove(name);
        if (jms == null)
            throw new IllegalStateException("No listener on queue " + name);
        jms.close();
    }
    
    public static void queue(String name, Serializable obj, int prior,
            long scheduledTime) throws JMSException {
        JMSDelegate jms = (JMSDelegate) map.get(name);
        if (jms == null)
            throw new IllegalStateException("No listener on queue " + name);
        jms.queueMessage(obj, prior, scheduledTime);
    }
    
    public static int toJMSPriority(int dcmPriority) {
        if (dcmPriority == 1) return 5;
        if (dcmPriority == 2) return 3;
        return 4;
    }
    
    private static HashMap map = new HashMap();

    private final QueueConnection conn;

    private final Queue queue;

    private int deliveryMode = DeliveryMode.PERSISTENT;

    private JMSDelegate(String name, MessageListener listener) {
        InitialContext iniCtx = null;
        QueueConnectionFactory qcf = null;
        try {
            iniCtx = new InitialContext();
	        qcf = (QueueConnectionFactory) iniCtx.lookup(CONNECTION_FACTORY);
            queue = (Queue) iniCtx.lookup("queue/" + name);
            conn = qcf.createQueueConnection();
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
        try {
            QueueSession session = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            QueueReceiver receiver = session.createReceiver(queue);
            receiver.setMessageListener(listener);
            conn.start();
        } catch (JMSException e) {
            close();
        }
    }
    
    private void close() {
        try {
            conn.close();
        } catch (Exception ignore) {
        }
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