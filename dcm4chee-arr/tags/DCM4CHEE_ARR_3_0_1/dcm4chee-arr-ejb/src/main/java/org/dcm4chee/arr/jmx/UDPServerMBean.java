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
 * The Initial Developer of the Original Code is Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4chee.arr.jmx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

import javax.annotation.Resource;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import org.dcm4chee.arr.util.AuditMessageUtils;
import org.jboss.annotation.ejb.Depends;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since May 24, 2006
 *
 */
@Service (objectName="dcm4chee.arr:service=UDPServer")
@Depends ("jboss.mq.destination:service=Queue,name=ARRReceiver")
@Management(UDPServer.class)
public class UDPServerMBean implements UDPServer {

    private static final int MAX_PACKAGE_SIZE = 65507;
    private static final int DEFAULT_PORT = 4000;

    private static Logger log = LoggerFactory.getLogger(UDPServerMBean.class);
    
    @Resource (mappedName="java:ConnectionFactory")
    private QueueConnectionFactory cf;
    private QueueConnection conn;

    @Resource (mappedName="queue/ARRReceiver")
    private Queue queue;
    
    private InetAddress laddr;
    private int port = DEFAULT_PORT;
    private int maxPacketSize = MAX_PACKAGE_SIZE;
    private int rcvBuf = 0;
    private boolean lookupSourceHostName = false;

    private DatagramSocket socket;
    private Thread thread;
    private long lastStartedAt;
    private long lastStoppedAt;
    
    public final boolean isRunning() {
        return socket != null;
    }

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

    public final int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize)  {
        if (maxPacketSize < 512 || maxPacketSize > MAX_PACKAGE_SIZE) {
            throw new IllegalArgumentException("maxPacketSize: " 
                    + maxPacketSize + " not in range 512..65507");
        }
        this.maxPacketSize = maxPacketSize;
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

    public boolean isLookupSourceHostName() {
        return lookupSourceHostName;
    }

    public void setLookupSourceHostName(boolean enable) {
        this.lookupSourceHostName = enable;
    }

    public synchronized void start() throws Exception {
        startConnection();
        startServer();
    }

    private synchronized void startConnection() throws JMSException {
        if (conn == null) {
            QueueConnection tmp = cf.createQueueConnection();
            tmp.start();
            conn = tmp;
        }
    }

    private synchronized void startServer() throws SocketException {
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
                log.info("Started UDP Server listening on {}", lsa);
                byte[] data = new byte[maxPacketSize];
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
                log.info("Stopped UDP Server listening on {}", lsa);
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

    public synchronized void stop() {
        stopServer();
        stopConnection();
    }

    private synchronized void stopConnection() {
        if (conn != null) {
            try { conn.close(); } catch (Exception ignore) {}
            conn = null;
        }
    }

    private synchronized void stopServer() {
        if (socket != null) {
            socket.close();
            try { thread.join(); } catch (Exception ignore) {}
            socket = null;
        }
    }

    private void onMessage(byte[] data, int length, InetAddress from) {
        if (log.isDebugEnabled()) {
            log.debug("Received {} from {}",
                    AuditMessageUtils.promptMsg(data, length), from);
        }
        int off = AuditMessageUtils.indexOfXML(data, length);
        if (off == -1) {
            if (log.isInfoEnabled()) {
                log.info("Ignore unexpected {} from {}",
                        AuditMessageUtils.promptMsg(data, length), from);
            }
            return;
        }
        try {           
            sendMessage(data, off, trimTail(data, length) - off, from);
        } catch (Throwable e) {
            log.error("Failed to schedule processing of " +
                    AuditMessageUtils.promptMsg(data, length)
                    + " received from " + from, e);
        }        
    }

    private int trimTail(byte[] data, int length) {
        int index = length - 1;
        while (data[index] == 0) --index;
        return index + 1;
    }

    private void sendMessage(byte[] data, int off, int length, InetAddress from)
            throws JMSException {
        QueueSession session = conn.createQueueSession(false, 
                QueueSession.AUTO_ACKNOWLEDGE);
        try {
            BytesMessage msg = session.createBytesMessage();
            msg.setStringProperty("sourceHostAddress", from.getHostAddress());
            if (lookupSourceHostName) {
                msg.setStringProperty("sourceHostName", from.getHostName());
            }
            msg.writeBytes(data, off, length);
            QueueSender sender = session.createSender(queue);
            sender.send(msg);
        } finally {
            try { session.close(); } catch (Exception e) {}
        }
    }

}
