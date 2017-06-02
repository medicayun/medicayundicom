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
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4chee.arr.listeners.tcp;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.SecurityAlertMessage;
import org.dcm4che2.audit.util.SSLUtils;
import org.dcm4chee.arr.listeners.common.AbstractSyslogListener;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 1, 2009
 */

public class TCPListener extends AbstractSyslogListener {

    private static final int INIT_MSG_LEN = 8192;
    private static final int MIN_MSG_LEN = 0;
    private static final int MAX_MSG_LEN = 65536;
    private enum StoreType { JKS, PKCS12 }
    
    private int soTimeout;
    private int backlog;
    private boolean tlsEnabled;
    private boolean tlsNeedClientAuth;
    private String[] tlsProtocols;
    private String[] tlsCiphers;
    private String keyStorePath;
    private File keyStoreFile;
    private char[] keyStorePass;
    private char[] keyPass;
    private StoreType keyStoreType;
    private String trustStorePath;
    private File trustStoreFile;
    private char[] trustStorePass;
    private StoreType trustStoreType;

    private KeyStore keyStore;
    private KeyStore trustStore;
    private SecureRandom random;
    private SSLContext sslContext;
    private ServerSocketFactory socketFactory;
    private ServerSocket socket;
    private Thread thread;
    private AtomicInteger numClients = new AtomicInteger();

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getNumClients() {
        return numClients.get();
    }

    public boolean isTLSEnabled() {
        return tlsEnabled;
    }

    public void setTLSEnabled(boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
        socketFactory = null;
    }

    public boolean isTLSNeedClientAuth() {
        return tlsNeedClientAuth;
    }

    public void setTLSNeedClientAuth(boolean tlsNeedClientAuth) {
        this.tlsNeedClientAuth = tlsNeedClientAuth;
        socketFactory = null;
    }

    public String getTLSProtocols() {
        return toString(tlsProtocols);
    }

    public void setTLSProtocols(String tlsProtocols) {
        this.tlsProtocols = split(tlsProtocols);
        socketFactory = null;
    }

    public String getTLSCiphers() {
        return toString(tlsCiphers);
    }

    public void setTLSCiphers(String tlsCiphers) {
        this.tlsCiphers = split(tlsCiphers);
        socketFactory = null;
    }

    private static String toString(String[] ss) {
        if (ss.length == 0)
            return "";

        if (ss.length == 1)
            return ss[0];

        int iMax = ss.length - 1;
        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(String.valueOf(ss[i]));
            if (i == iMax)
                return b.toString();
            b.append(", ");
        }
     }

    private static String[] split(String s) {
        StringTokenizer stk = new StringTokenizer(s, " ,");
        int count = stk.countTokens();
        if (count == 0)
            throw new IllegalArgumentException(s);
        String[] ss = new String[count];
        for (int i = 0; i < ss.length; i++)
            ss[i] = stk.nextToken();
        return ss;
    }

    public final String getKeyStoreFile() {
        return keyStorePath;
    }

    public void setKeyStoreFile(String path) {
        String trim = path.trim();
        File file = toFile(trim);
        if (!file.isFile())
            throw new IllegalArgumentException("No such file: " + path);
        this.keyStorePath = trim;
        this.keyStoreFile = file;
        keyStore = null;
        sslContext = null;
        socketFactory = null;
    }

    private static File toFile(String path) {
        if (path.startsWith("${")) {
            int end = path.indexOf('}', 3);
            if (end >= 0) {
                String replacement = System.getProperty(path.substring(2, end));
                if (replacement != null)
                    path = replacement + path.substring(end+1);
            }
        }
        return new File(path);
    }

    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass.trim().toCharArray();
    }

    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass.trim().toCharArray();
    }

    public String getKeyStoreType() {
        return keyStoreType.toString();
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = StoreType.valueOf(
                keyStoreType.trim().toUpperCase());
    }

    public String getTrustStoreFile() {
        return trustStorePath.toString();
    }

    public void setTrustStoreFile(String path) {
        String trim = path.trim();
        File file = toFile(trim);
        if (!file.isFile())
            throw new IllegalArgumentException("No such file: " + path);
        this.trustStorePath = trim;
        this.trustStoreFile = file;
        trustStore = null;
        sslContext = null;
        socketFactory = null;
    }

    public final void setTrustStorePass(String trustStorePass) {
        this.trustStorePass = trustStorePass.trim().toCharArray();
    }

    public String getTrustStoreType() {
        return trustStoreType.toString();
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = StoreType.valueOf(
                trustStoreType.trim().toUpperCase());
    }

    public void restart() throws Exception {
        startServer();
    }

    protected void startServer() throws Exception {
        if (socket != null) {
            stopServer();
        }
        initSocketFactory();
        socket = socketFactory.createServerSocket();
        int prevRcvBuf = socket.getReceiveBufferSize();
        if (rcvBuf == 0) {
            rcvBuf = prevRcvBuf;
        } else if (rcvBuf != prevRcvBuf) {
            socket.setReceiveBufferSize(rcvBuf);
            rcvBuf = socket.getReceiveBufferSize();
        }
        socket.bind(new InetSocketAddress(laddr, port), backlog);
        if (socket instanceof SSLServerSocket) {
            SSLServerSocket sslSocket = (SSLServerSocket) socket;
            sslSocket.setNeedClientAuth(tlsNeedClientAuth);
            sslSocket.setEnabledProtocols(tlsProtocols);
            sslSocket.setEnabledCipherSuites(tlsCiphers);
        }
        thread = new Thread(new Runnable() {
            public void run() {
                lastStartedAt = System.currentTimeMillis();
                SocketAddress lsa = socket.getLocalSocketAddress();
                log.info("Started TCP Server listening on " + lsa);
                boolean restart = false;
                while (socket != null && !socket.isClosed()) {
                    try {
                        new HandlerThread(socket.accept()).start();
                    } catch (IOException e) {
                        if (!socket.isClosed()) {
                            log.warn("TCP Server throws i/o exception - restart", e);
                            restart = true;
                        }
                        break;
                    }
                }
                socket = null;
                thread = null;
                lastStoppedAt = System.currentTimeMillis();
                log.info("Stopped TCP Server listening on " + lsa);
                if (restart) {
                    try {
                        startServer();
                    } catch (Exception e) {
                        log.error("Failed to restart TCP Server", e);
                    }
                }
            }
        });
        thread.start();
    }

    private void initSocketFactory() throws Exception {
        if (socketFactory != null)
            return;
        if (tlsEnabled) {
            initSSLContext();
            socketFactory = sslContext.getServerSocketFactory();
        } else {
            socketFactory = ServerSocketFactory.getDefault();
        }
    }

    private void initSSLContext() throws Exception {
        if (sslContext != null)
            return;
        if (keyStore == null)
            keyStore = SSLUtils.loadKeyStore(
                    keyStoreFile, keyStorePass, keyStoreType.toString());
        if (trustStore == null)
            trustStore = SSLUtils.loadKeyStore(
                    trustStoreFile, trustStorePass, trustStoreType.toString());
        if (random == null)
            random = SecureRandom.getInstance("SHA1PRNG");
        sslContext = SSLUtils.getSSLContext(
                keyStore, keyPass, trustStore, random);
    }

    protected void stopServer() {
        if (socket != null) {
            try { socket.close(); } catch (Exception ignore) {}
            try { thread.join(); } catch (Exception ignore) {}
            socket = null;
        }
    }

    private class HandlerThread extends Thread {

        private Socket s;
        private byte[] data = new byte[INIT_MSG_LEN];

        public HandlerThread(Socket s) {
            this.s = s;
        }

        public void run() {
            InputStream in = null;
            try {
                numClients.incrementAndGet();
                log.info("Accept connection from " + s);
                InetAddress addr = s.getInetAddress();
                if (s instanceof SSLSocket) {
                    SSLSocket ssl = (SSLSocket) s;
                    try {
                        ssl.startHandshake();
                    } catch (IOException e) {
                        log.warn("SSL Handshake with " + s + " failed:", e);
                        sendAuditSecurityAlertMessage(addr, e);
                        return;
                    }
                }
                in = s.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(in);
                for (;;) {
                    s.setSoTimeout(getSoTimeout());
                    int len = readMessageLength(bis, s);
                    if (len == -1)
                        break;
                    if (len > data.length)
                        data = new byte[len];
                    readMessage(bis, len);
                    onMessage(data, 0, len, addr);
                }
            } catch (IOException e) {
                log.error("Failed to receive message from " + s, e);
            } finally {
                if (in != null)
                    try { in.close(); } catch (IOException ignore) {}
                try { s.close(); } catch (IOException ignore) {}
                numClients.decrementAndGet();
                log.info("Close connection from " + s);
            }
        }

        private void sendAuditSecurityAlertMessage(InetAddress addr,
                IOException e) {
            try {
                SecurityAlertMessage msg = new SecurityAlertMessage(
                        SecurityAlertMessage.NODE_AUTHENTICATION);
                msg.setOutcomeIndicator(
                        AuditEvent.OutcomeIndicator.MINOR_FAILURE);
                msg.addReportingProcess(AuditMessage.getProcessID(),
                        AuditMessage.getLocalAETitles(),
                        AuditMessage.getProcessName(),
                        AuditMessage.getLocalHostName());
                msg.addPerformingNode(AuditMessage.hostNameOf(addr));
                msg.addAlertSubjectWithNodeID(
                        AuditMessage.getLocalNodeID(), e.getMessage());
                msg.validate();
                byte[] data = msg.toString().getBytes("UTF-8");
                sendMessage(data, 0, data.length, 
                        AuditMessage.getLocalHost());
            } catch (IOException e2) {
                log.warn("Failed to send Audit Security Alert message", e);
            }
        }

        private int readMessageLength(InputStream in, Socket s) throws IOException {
            int ch;
            try {
                ch = in.read();
            } catch (SocketTimeoutException e) {
                log.info("Timeout expired for connection to " + s);
                return -1;
            }
            if (ch < 0)
                return -1;

            int d, len = 0;
            do {
                d = ch - '0';
                if (d < 0 || d > 9) {
                    log.warn("Illegal character code: " + ch
                            + " in message length");
                    return -1;
                }
                len = (len << 3) + (len << 1) + d; // 10 * len + d
                ch = in.read();
                if (ch < 0) {
                    throw new EOFException();
                }
            } while (ch != ' ');
            if (len < MIN_MSG_LEN || len > MAX_MSG_LEN) {
                log.warn("Illegal message length: " + len);
                return -1;
            }
            return len;
       }

        private void readMessage(InputStream in, int len) throws IOException {
            int n = 0;
            while (n < len) {
                int count = in.read(data, n, len - n);
                if (count < 0)
                    throw new EOFException();
                n += count;
            }
        }
    }
}
