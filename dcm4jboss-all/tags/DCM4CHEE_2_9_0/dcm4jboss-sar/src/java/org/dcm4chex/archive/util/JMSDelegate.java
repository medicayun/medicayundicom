/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

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
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
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

    public static void startListening(String name, MessageListener listener, 
				int receiverCount)
    		throws JMSException {
        if (map.containsKey(name))
            throw new IllegalStateException("Already listening on queue " + name);
        map.put(name, new JMSDelegate(name, listener, receiverCount));
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

    private JMSDelegate(String name, MessageListener listener, int receiverCount)
    		throws JMSException {
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
			for (int i = 0; i < receiverCount; ++i) {
	            QueueSession session = conn.createQueueSession(false,
	                    QueueSession.AUTO_ACKNOWLEDGE);
	            QueueReceiver receiver = session.createReceiver(queue);
	            receiver.setMessageListener(listener);
			}
            conn.start();
        } catch (JMSException e) {
            close();
            throw e;
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