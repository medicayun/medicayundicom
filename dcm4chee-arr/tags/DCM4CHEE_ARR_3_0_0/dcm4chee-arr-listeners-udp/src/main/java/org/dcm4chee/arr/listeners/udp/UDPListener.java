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

package org.dcm4chee.arr.listeners.udp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
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
 * @since Jan 24, 2007
 */
public class UDPListener extends ServiceMBeanSupport {
    
    private static final String QUEUE_FACTORY = "java:ConnectionFactory";
    private static final String QUEUE = "queue/ARRIncoming";

    private static final int MAX_PACKAGE_SIZE = 65507;
    
    private static final int MIN_MSG_LEN = 100;

    private static final int MSG_PROMPT_LEN = 200;
    
    private static final int MIN_XMLDECL_LEN = 20;
    
    private static final int DEFAULT_PORT = 4000;
    
    private QueueConnectionFactory connFactory;
    private QueueConnection conn;
    private Queue queue;
    private QueueSession session;
    private QueueSender sender;
    
    private InetAddress laddr;
    private int port = DEFAULT_PORT;
    private int maxMsgSize = MAX_PACKAGE_SIZE;
    private int rcvBuf = 0;
    private boolean enableDNSLookups = false;

    private DatagramSocket socket;
    private Thread thread;
    private long lastStartedAt;
    private long lastStoppedAt;
    
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

    public final int getMaxMessageSize() {
        return maxMsgSize;
    }

    public void setMaxMessageSize(int maxMessageSize)  {
        if (maxMessageSize < 512 || maxMessageSize > MAX_PACKAGE_SIZE) {
            throw new IllegalArgumentException("maxMessageSize: " 
                    + maxMessageSize + " not in range 512..65507");
        }
        this.maxMsgSize = maxMessageSize;
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
            conn = connFactory.createQueueConnection();
            session = conn.createQueueSession(false, 
                    QueueSession.AUTO_ACKNOWLEDGE);
            sender = session.createSender(queue);
        }
        startServer();
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

    private void startServer() throws SocketException {
        if (socket != null) {
            stopServer();
        }
        socket = new DatagramSocket(port, laddr);
        int prevRcvBuf = socket.getReceiveBufferSize();
        if (rcvBuf == 0) {
            rcvBuf = prevRcvBuf;
        } else if (rcvBuf != prevRcvBuf) {
            socket.setReceiveBufferSize(rcvBuf);
            rcvBuf = socket.getReceiveBufferSize();
        }
        thread = new Thread(new Runnable() {
            public void run() {
                lastStartedAt = System.currentTimeMillis();
                SocketAddress lsa = socket.getLocalSocketAddress();
                log.info("Started UDP Server listening on " + lsa);
                byte[] data = new byte[maxMsgSize];
                DatagramPacket p = new DatagramPacket(data, data.length);
                boolean restart = false;
                while (socket != null && !socket.isClosed()) {
                    try {
                        socket.receive(p);
                    } catch (IOException e) {
                        if (!socket.isClosed()) {
                            log.warn("UDP Server throws i/o exception - restart", e);
                            restart = true;
                        }
                        break;
                    }
                    onMessage(data, p.getLength(), p.getAddress());
                    p.setLength(data.length);
                }
                socket = null;
                thread = null;
                lastStoppedAt = System.currentTimeMillis();
                log.info("Stopped UDP Server listening on " + lsa);
                if (restart) {
                    try {
                        startServer();
                    } catch (SocketException e) {
                        log.error("Failed to restart UDP Server", e);
                    }
                }
            }
        });
        thread.start();
    }    

    private void stopServer() {
        if (socket != null) {
            socket.close();
            try { thread.join(); } catch (Exception ignore) {}
            socket = null;
        }
    }

    private void onMessage(byte[] data, int length, InetAddress from) {
        if (enableDNSLookups) {
            // initialize from.hostName, so it will show up in log messages
            from.getHostName();
        }
        if (log.isDebugEnabled()) {
             log.debug("Received message from " + from + " - "
                     + prompt(data));
        }
        int end = length;
        while (end > 0 && data[end-1] != '>') {
            --end;
        }
        int off = indexOfXML(data, end);
        if (off == -1) {
            log.warn("Ignore unexpected message from " + from + " - " 
                        + prompt(data));
            return;
        }
        try {           
            sendMessage(data, off, end - off, from);
        } catch (Throwable e) {
            log.error("Failed to schedule processing message received from " 
                    + from + " - " +  prompt(data), e);
        }        
    }

    private static int indexOfXML(byte[] data, int end) {
        int off = -1;
        for (int i = 0, n = end - MIN_MSG_LEN; i < n; ++i) {
            if (data[i] != '<') continue;           
            switch (data[i+1]) {
            case '?':
                if (data[i+2] == 'x' && data[i+3] == 'm' && data[i+4] == 'l') {
                    off = i;
                    i += MIN_XMLDECL_LEN;
                }
                continue;
            case 'A':
                return (data[i+2] == 'u' 
                     && data[i+3] == 'd'
                     && data[i+4] == 'i' 
                     && data[i+5] == 't'
                     && data[i+6] == 'M' 
                     && data[i+7] == 'e'
                     && data[i+8] == 's'
                     && data[i+9] == 's'
                    && data[i+10] == 'a' 
                    && data[i+11] == 'g'
                    && data[i+12] == 'e') ? (off != -1 ? off : i) : -1;
            case 'I':
                return (data[i+2] == 'H'
                     && data[i+3] == 'E'
                     && data[i+4] == 'Y'
                     && data[i+5] == 'r'
                     && data[i+6] == '4') ? (off != -1 ? off : i) : -1;
            default:
                if (off != -1) {
                    return -1;
                }
            }
        }
        return -1;
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

    private void sendMessage(byte[] data, int off, int length, InetAddress from)
            throws JMSException {
        BytesMessage msg = session.createBytesMessage();
        msg.setStringProperty("sourceHostAddress", from.getHostAddress());
        if (enableDNSLookups) {
            msg.setStringProperty("sourceHostName", from.getHostName());
        }
        msg.writeBytes(data, off, length);
        sender.send(msg);
    }

}
