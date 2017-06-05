/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

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

import org.jboss.logging.Logger;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4157 $ $Date: 2004-07-18 22:39:13 +0800 (周日, 18 7月 2004) $
 * @since 23.06.2004
 *
 */
public class JMSDelegate {

    static final String CONNECTION_FACTORY = "java:ConnectionFactory";

    static int toJMSPriority(String dcmPriority) {
        if (dcmPriority.equals(Priority.LOW)) return 3;
        if (dcmPriority.equals(Priority.HIGH)) return 5;
        return 4;
    }

    private static HashMap map = new HashMap();

    public synchronized static JMSDelegate getInstance(String name) {
        JMSDelegate instance = (JMSDelegate) map.get(name);
        if (instance == null)
            map.put(name, instance = new JMSDelegate(name));
        return instance;
    }

    private static QueueConnection conn;

    private final String name;

    private final Queue queue;

    private final QueueReceiver receiver;

    private JMSDelegate(String name) {
        this.name = name;
        InitialContext iniCtx = null;
        try {
            iniCtx = new InitialContext();
            if (conn == null) {
	            Object tmp = iniCtx.lookup(CONNECTION_FACTORY);
	            QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
	            conn = qcf.createQueueConnection();
	            conn.start();
            }
            queue = (Queue) iniCtx.lookup("queue/" + name);
            QueueSession session = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            receiver = session.createReceiver(queue);
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

    public void setMessageListener(MessageListener listener) {
        try {
            receiver.setMessageListener(listener);
        } catch (JMSException e) {
            throw new ConfigurationException(e);
        }
    }


    public void queue(Logger log, MediaCreationRequest rq)
            throws JMSException {
        QueueSession session = null;
        QueueSender send = null;
        log.info("Queue " + rq + " for " + name);
        try {
            session = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            send = session.createSender(queue);
            ObjectMessage msg = session.createObjectMessage(rq);
            send.send(msg, DeliveryMode.PERSISTENT, toJMSPriority(rq
                    .getPriority()), 0);
        } catch (JMSException e) {
            log.error("Failed to queue " + rq + " for " + name, e);
            throw e;
        } finally {
            if (send != null) {
                try {
                    send.close();
                } catch (Exception ignore) {
                }
            }
            if (session != null) {
                try {
                    session.close();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
