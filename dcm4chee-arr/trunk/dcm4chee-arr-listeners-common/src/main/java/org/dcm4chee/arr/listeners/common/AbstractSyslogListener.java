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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.arr.listeners.common;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.jms.BytesMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;

import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 31, 2009
 */
public abstract class AbstractSyslogListener extends ServiceMBeanSupport {
    
    private static final String QUEUE_FACTORY = "java:ConnectionFactory";
    private static final String QUEUE = "queue/ARRIncoming";

    private static final int MAX_MSG_PREFIX = 200;

    private static final int MSG_PROMPT_LEN = 200;
    
    private static final int JMS_RETRY_COUNT = 6;
    
    private static final int JMS_RETRY_INTERVAL = 3000; // milliseconds
    
    private QueueConnectionFactory connFactory;
    private QueueConnection conn;
    private Queue queue;
    private QueueSession session;
    private QueueSender sender;
    
    protected InetAddress laddr;
    protected int port;
    protected int rcvBuf;
    protected boolean enableDNSLookups;

    protected long lastStartedAt;
    protected long lastStoppedAt;
    
    public Date getLastStoppedAt() {
        return toDate(lastStoppedAt);
    }

    public Date getLastStartedAt() {
        return toDate(lastStartedAt);
    }

    private static Date toDate(long ms) {
        return ms > 0 ? new Date(ms) : null;
    }
    
    public final String getLocalAddress() {
        return laddr == null ? "0.0.0.0" : laddr.getHostAddress();
    }
    
    public void setLocalAddress(String laddrStr) {
        try {
            laddr = InetAddress.getByName(laddrStr);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown Host: " + laddrStr);
        }
    }
    
    public final int getPort() {
        return port;        
    }

    public void setPort(int port) {
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port: " + port);
        }
        this.port = port;        
    }

    public final int getReceiveBufferSize() {
        return rcvBuf;
    }

    public void setReceiveBufferSize(int rcvBuf)  {
        if (rcvBuf < 0) {
            throw new IllegalArgumentException("rcvBuf: " + rcvBuf);
        }
        this.rcvBuf = rcvBuf;
    }

    public boolean isEnableDNSLookups() {
        return enableDNSLookups;
    }

    public void setEnableDNSLookups(boolean enable) {
        this.enableDNSLookups = enable;
    }

    protected void createService() throws Exception {
        InitialContext jndiCtx = new InitialContext();
        connFactory = (QueueConnectionFactory) jndiCtx.lookup(QUEUE_FACTORY);
        queue = (Queue) jndiCtx.lookup(QUEUE);
    }
    
    protected synchronized void startService() throws Exception {
        if (conn == null) {
            initJMS();
        }
        startServer();
    }

    protected synchronized void initJMS() throws Exception {
        conn = connFactory.createQueueConnection();
        session = conn.createQueueSession(false, 
                QueueSession.AUTO_ACKNOWLEDGE);
        sender = session.createSender(queue);
    }
    
    protected synchronized void stopService() {
        stopServer();
        if (conn != null) {
            try { conn.close(); } catch (Exception ignore) {}
            conn = null;
            session = null;
            sender = null;
        }
    }

    protected abstract void startServer() throws Exception;

    protected abstract void stopServer();

    protected void onMessage(byte[] data, int off, int length, InetAddress from) {
        if (enableDNSLookups) {
            // initialize from.hostName, so it will show up in log messages
            from.getHostName();
        }
        if (log.isDebugEnabled()) {
             log.debug("Received message from " + from + " - "
                     + prompt(data));
        }
        off = indexOfXML(data, off, off + Math.min(MAX_MSG_PREFIX, length) - 1);
        if (off == -1) {
            log.warn("Ignore unexpected message from " + from + " - " 
                        + prompt(data));
            return;
        }
        
        sendMessage(data, off, length - off, from);
    }

    private static int indexOfXML(byte[] buf, int offset, int maxIndex) {
        for(int index = offset, xmlDeclIndex = -1; index <= maxIndex; index++) {
            if (buf[index] != '<')
                continue;
            if (isAuditMessage(buf, index) || isIHEYr4(buf, index))
                return xmlDeclIndex == -1 ? index : xmlDeclIndex;
            else if (xmlDeclIndex == -1 && isXMLDecl(buf, index))
                xmlDeclIndex = index;
        }
        return -1;
    }

    private static boolean isXMLDecl(byte[] buf, int index) {
        return index + 4 < buf.length
            && buf[index+1] == '?'
            && buf[index+2] == 'x'
            && buf[index+3] == 'm'
            && buf[index+4] == 'l';
    }

    private static boolean isAuditMessage(byte[] buf, int index) {
        return index + 12 < buf.length
            && buf[index+1] == 'A'
            && buf[index+2] == 'u'
            && buf[index+3] == 'd'
            && buf[index+4] == 'i'
            && buf[index+5] == 't'
            && buf[index+6] == 'M'
            && buf[index+7] == 'e'
            && buf[index+8] == 's'
            && buf[index+9] == 's'
            && buf[index+10] == 'a'
            && buf[index+11] == 'g'
            && buf[index+12] == 'e';
    }

    private static boolean isIHEYr4(byte[] buf, int index) {
        return index + 6 < buf.length
            && buf[index+1] == 'I'
            && buf[index+2] == 'H'
            && buf[index+3] == 'E'
            && buf[index+4] == 'Y'
            && buf[index+5] == 'r'
            && buf[index+6] == '4';
    }

    private static String prompt(byte[] data) {
        try {
            return data.length > MSG_PROMPT_LEN
                    ? (new String(data, 0, MSG_PROMPT_LEN, "UTF-8") + "...") 
                    : new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected void sendMessage(byte[] data, int off, int length, InetAddress from) {
        for(int i = 0; i < JMS_RETRY_COUNT; i++) {
            try {     
                BytesMessage msg = session.createBytesMessage();
                msg.setStringProperty("sourceHostAddress", from.getHostAddress());
                if (enableDNSLookups) {
                    msg.setStringProperty("sourceHostName", from.getHostName());
                }
                msg.writeBytes(data, off, length);
                sender.send(msg);
                return;
            } catch (javax.jms.IllegalStateException e) {
                // typically caused by "session.createBytesMessage"
                handleJMSDisconnection();
                continue;
            } catch (Exception e) {
                if( e.getCause() instanceof IllegalStateException ) {
                    // typically caused by "sender.send"
                    handleJMSDisconnection();
                    continue;
                }
                log.error("Failed to schedule processing message received from " 
                        + from + " - " +  prompt(data), e);
                return;
            }            
        }
    }

    private void handleJMSDisconnection() {
        try {
            initJMS();
            log.info("Reconnected to JMS");
        } catch(Exception ex) {
            try { Thread.sleep(JMS_RETRY_INTERVAL);} catch (InterruptedException e1) {}
        }
    }
}
