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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package org.dcm4cheri.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ServerSocketFactory;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;
import org.dcm4che.server.Server;
import org.dcm4che.util.HandshakeFailedEvent;
import org.dcm4che.util.HandshakeFailedListener;
import org.dcm4cheri.util.LF_ThreadPool;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3922 $ $Date: 2005-10-06 00:26:16 +0800 (周四, 06 10月 2005) $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20021105 Gunter Zeilinger:</b>
 * <ul>
 * <li> add property port and serverSocketFactory
 * <li> deprecate use of {@link #start(int)} 
 *                   and {@link #start(int,ServerSocketFactory)}
 * </ul>
 */
class ServerImpl implements LF_ThreadPool.Handler, Server {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    static final Logger log = Logger.getLogger(ServerImpl.class);
    private static int instCount = 0;
    private final String name = "TCPServer-" + ++instCount;
    
    private final Handler handler;
    private LF_ThreadPool threadPool = new LF_ThreadPool(this, name);
    private ServerSocket ss;
    private int port = 104;
    private List hcl = null;
    private List hfl = null;
    private ServerSocketFactory ssf = ServerSocketFactory.getDefault();
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public ServerImpl(Handler handler) {
        if (handler == null)
            throw new NullPointerException();
        
        if (handler == null)
            throw new NullPointerException();
        
        this.handler = handler;
    }
    
    // Public --------------------------------------------------------
    
    // Server implementation -----------------------------------------
    public void addHandshakeCompletedListener(
            HandshakeCompletedListener listener) {
        hcl = addToList(hcl, listener);
    }
    
    public void addHandshakeFailedListener(
            HandshakeFailedListener listener) {
        hfl = addToList(hfl, listener);
    }
    
    public void removeHandshakeCompletedListener(
            HandshakeCompletedListener listener) {
        hcl = removeFromList(hcl, listener);
    }
    
    public void removeHandshakeFailedListener(
            HandshakeFailedListener listener) {
        hfl = removeFromList(hfl, listener);
    }
    // Listeners
    
    // Add an element to a list, creating a new list if the
    // existing list is null, and return the list.
    static List addToList(List l, Object elt) {
        if (l == null) {
            l = new ArrayList();
        }
        l.add(elt);
        return l;
    }
    
    
    // Remove an element from a list, discarding the list if the
    // resulting list is empty, and return the list or null.
    static List removeFromList(List l, Object elt) {
        if (l == null) {
            return l;
        }
        l.remove(elt);
        if (l.size() == 0) {
            l = null;
        }
        return l;
    }
    
    public void setMaxClients(int max) {
        threadPool.setMaxRunning(max);
    }
    
    public int getMaxClients() {
        return threadPool.getMaxRunning();
    }
    
    public int getNumClients() {
        return threadPool.running()-1;
    }
    
    public void setMaxIdleThreads(int max) {
        threadPool.setMaxWaiting(max);
    }

    public int getMaxIdleThreads() {
        return threadPool.getMaxWaiting();
    }

    public int getNumIdleThreads() {
        return threadPool.waiting();
    }
    
    public void start(int port) throws IOException {
        start(port, ServerSocketFactory.getDefault());
    }
    
    public void start(int port, ServerSocketFactory ssf) throws IOException {
        setPort(port);
        setServerSocketFactory(ssf);
        start();
    }
    
    public void start() throws IOException {
        checkNotRunning();
        if (log.isInfoEnabled())
            log.info("Start Server listening at port " + port);
        ss = ssf.createServerSocket(port);
        new Thread(new Runnable() {
            public void run() { threadPool.join(); }
        }, name).start();
    }
    
    public void stop() {
        if (ss == null)
            return;
        
        InetAddress ia = ss.getInetAddress();
        int port = ss.getLocalPort();
        if (log.isInfoEnabled())
            log.info("Stop Server listening at port " + port);
        try {
            ss.close();
        } catch (IOException ignore) {}
        
        // try to connect to server port to ensure to leave blocking accept
        /*try {
            new Socket(ia, port).close();
        } catch (IOException ignore) {}*/
        ss = null;
        threadPool.shutdown();
    	LF_ThreadPool tp = new LF_ThreadPool(this, name);
    	tp.setMaxRunning( threadPool.getMaxRunning());
    	tp.setMaxWaiting( threadPool.getMaxWaiting());
    	threadPool = tp;
    }
    
    // LF_ThreadPool.Handler implementation --------------------------
    public void run(LF_ThreadPool pool) {
        if (ss == null)
            return;
        
        Socket s = null;
        try {
            s = ss.accept();
            if (log.isInfoEnabled()) {
                log.info("handle - " + s);
            }
            if (s instanceof SSLSocket) {
                init((SSLSocket) s);
            }
            
            pool.promoteNewLeader();
            handler.handle(s);
            if (!handler.isSockedClosedByHandler() && s != null) {
                try { s.close(); } catch (IOException ignore) {}
            }
        } catch (Exception e) {
            log.error(e);
            if (s != null) {
                try { s.close(); } catch (IOException ignore) {};
            }
        }
        if (log.isInfoEnabled()) {
            log.info("finished - " + s);
        }
    }
    
    // Y overrides ---------------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    private void checkNotRunning() {
        if (ss != null) {
            throw new IllegalStateException("Already Running - " + threadPool);
        }
    }
    
    private void init(SSLSocket s) throws IOException {
        if (hcl != null) {
            for (int i = 0, n = hcl.size(); i < n; ++i) {
                s.addHandshakeCompletedListener(
                    (HandshakeCompletedListener) hcl.get(i));
            }
        }
        try {
            s.startHandshake();
            if (log.isInfoEnabled()) {
                SSLSession se = s.getSession();
                try {
                    X509Certificate cert = (X509Certificate)
                        se.getPeerCertificates()[0];
                    cert.checkValidity();
                    log.info(s.getInetAddress().toString() + 
                        ": accept " + se.getCipherSuite() + " with "
                        + cert.getSubjectDN()+" valid from "+cert.getNotBefore()+" to "+cert.getNotAfter());
                } catch (SSLPeerUnverifiedException e) {
                    log.error("SSL peer not verified:",e);
                } catch ( CertificateException ce ) {
                	throw new IOException(ce.getMessage());
                }
            }
        } catch (IOException e) {
            if (hfl != null) {
                HandshakeFailedEvent event = new HandshakeFailedEvent(s,e);
                for (int i = 0, n = hfl.size(); i < n; ++i) {
                    ((HandshakeFailedListener) hfl.get(i)).handshakeFailed(event);
                }
                throw e;
            }
        }
    }
        
    /** Getter for property port.
     * @return Value of property port.
     *
     */
    public int getPort() {
        return port;
    }
    
    /** Setter for property port.
     * @param port New value of property port.
     *
     */
    public void setPort(int port) {
        if (port <= 0) {
            throw new IllegalArgumentException("port: " + port);
        }
        this.port = port;
    }
    
    /** Getter for property serverSocketFactory.
     * @return Value of property serverSocketFactory.
     *
     */
    public ServerSocketFactory getServerSocketFactory() {
        return ssf;
    }
    
    /** Setter for property serverSocketFactory.
     * @param serverSocketFactory New value of property serverSocketFactory.
     *
     */
    public void setServerSocketFactory(ServerSocketFactory ssf) {
        if (ssf == null) {
            throw new NullPointerException();
        }
        this.ssf = ssf;
    }
    
}
