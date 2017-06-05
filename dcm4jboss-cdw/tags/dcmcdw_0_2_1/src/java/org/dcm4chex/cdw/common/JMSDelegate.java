/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

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
 * @version $Revision: 4132 $ $Date: 2004-06-29 07:06:09 +0800 (周二, 29 6月 2004) $
 * @since 23.06.2004
 *
 */
public class JMSDelegate {

    static final String CONNECTION_FACTORY = "java:ConnectionFactory";

    static final String COMPOSER_QUEUE = "queue/MediaComposer";

    static final String MKISOFS_QUEUE = "queue/MakeIsoImage";

    static final String WRITER_QUEUE = "queue/MediaWriter";

    static int toJMSPriority(String dcmPriority) {
        if (dcmPriority.equals(Priority.LOW)) return 3;
        if (dcmPriority.equals(Priority.HIGH)) return 5;
        return 4;
    }

    private static JMSDelegate instance;

    public synchronized static JMSDelegate getInstance() {
        if (instance == null) instance = new JMSDelegate();
        return instance;
    }

    private QueueConnection conn;

    private Queue composerQueue;

    private Queue mkisofsQueue;

    private Queue writerQueue;

    private QueueReceiver composerReceiver;

    private QueueReceiver mkisofsReceiver;

    private QueueReceiver writerReceiver;

    private JMSDelegate() {
        InitialContext iniCtx = null;
        try {
            iniCtx = new InitialContext();
            Object tmp = iniCtx.lookup(CONNECTION_FACTORY);
            QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
            conn = qcf.createQueueConnection();
            composerQueue = (Queue) iniCtx.lookup(COMPOSER_QUEUE);
            mkisofsQueue = (Queue) iniCtx.lookup(MKISOFS_QUEUE);
            writerQueue = (Queue) iniCtx.lookup(WRITER_QUEUE);
            QueueSession composerSession = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            QueueSession mkisofsSession = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            QueueSession writerSession = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            composerReceiver = composerSession.createReceiver(composerQueue);
            mkisofsReceiver = mkisofsSession.createReceiver(mkisofsQueue);
            writerReceiver = writerSession.createReceiver(writerQueue);
            conn.start();
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

    public void setMediaWriterListener(MessageListener listener) {
        try {
            writerReceiver.setMessageListener(listener);
        } catch (JMSException e) {
            throw new ConfigurationException(e);
        }
    }

    public void setMediaComposerListener(MessageListener listener) {
        try {
            composerReceiver.setMessageListener(listener);
        } catch (JMSException e) {
            throw new ConfigurationException(e);
        }
    }

    public void setMakeIsoImageListener(MessageListener listener) {
        try {
            mkisofsReceiver.setMessageListener(listener);
        } catch (JMSException e) {
            throw new ConfigurationException(e);
        }
    }

    public void queueForMediaComposer(Logger log, MediaCreationRequest rq)
            throws JMSException {
        queueFor(log, rq, composerQueue, " to Media Composer");
    }

    public void queueForMediaWriter(Logger log, MediaCreationRequest rq)
            throws JMSException {
        queueFor(log, rq, writerQueue, " to Media Writer");
    }

    public void queueForMakeIsoImage(Logger log, MediaCreationRequest rq)
            throws JMSException {
        queueFor(log, rq, mkisofsQueue, " to Make ISO Image");
    }

    private void queueFor(Logger log, MediaCreationRequest rq, Queue queue, String toDest)
            throws JMSException {
        QueueSession session = null;
        QueueSender send = null;
        log.info("Forwarding " + rq + toDest);
        try {
            session = conn.createQueueSession(false,
                    QueueSession.AUTO_ACKNOWLEDGE);
            send = session.createSender(queue);
            ObjectMessage msg = session.createObjectMessage(rq);
            send.send(msg, DeliveryMode.PERSISTENT, toJMSPriority(rq
                    .getPriority()), 0);
        } catch (JMSException e) {
            log.error("Failed to forward " + rq + toDest, e);
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
