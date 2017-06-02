/*                                                                           *
 *  Copyright (c) 2002, 2003 by TIANI MEDGRAPH AG                            *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package org.dcm4cheri.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;
import org.dcm4che.util.HandshakeFailedEvent;
import org.dcm4che.util.HandshakeFailedListener;

import org.dcm4che.util.SSLContextAdapter;

/**
 * <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created    Juni 2002
 * @version    $Revision: 3885 $ $Date: 2005-04-07 16:35:22 +0800 (周四, 07 4月 2005) $
 */
public class SSLContextAdapterImpl extends SSLContextAdapter
{
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    final static Logger log = Logger.getLogger(SSLContextAdapterImpl.class);

    private String[] protocols = {
            "TLSv1",
        "SSLv3",
    //    "SSLv2Hello"
            };
    /*
    private String[] cipherSuites = {
        "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
        "SSL_RSA_WITH_NULL_SHA"
    };
  */
    private final SSLContext ctx;
    private final KeyManagerFactory kmf;
    private final TrustManagerFactory tmf;
    private SecureRandom random = null;
    private KeyManager[] kms = null;
    private TrustManager[] tms = null;
    private boolean dirty = true;
    private boolean needClientAuth = true;
    private SSLServerSocket unboundSSLServerSocket = null;
    private List hcl = null;
    private List hfl = null;

    // Static --------------------------------------------------------
    /**
     *  The main program for the SSLContextAdapterImpl class
     *
     * @param  args           The command line arguments
     * @exception  Exception  Description of the Exception
     */
    public static void main(String[] args)
        throws Exception
    {
        SSLContextAdapter inst = new SSLContextAdapterImpl();
        System.out.println("SupportedCipherSuites"
                 + Arrays.asList(inst.getSupportedCipherSuites()));
        System.out.println("SupportedProtocols"
                 + Arrays.asList(inst.getSupportedProtocols()));
    }

    // Constructors --------------------------------------------------
    /**Constructor for the SSLContextAdapterImpl object */
    public SSLContextAdapterImpl()
    {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        try {
            ctx = SSLContext.getInstance("TLS");
            kmf = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
        } catch (GeneralSecurityException e) {
            throw new ConfigurationError("could not instantiate SSLContext", e);
        }
    }

    // Public --------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString()
    {
        return ctx.toString();
    }

    // SSLContextAdapter implementation ------------------------------
    /**
     *  Adds a feature to the HandshakeCompletedListener attribute of the SSLContextAdapterImpl object
     *
     * @param  listener  The feature to be added to the HandshakeCompletedListener attribute
     */
    public void addHandshakeCompletedListener(
            HandshakeCompletedListener listener)
    {
        hcl = addToList(hcl, listener);
    }


    /**
     *  Adds a feature to the HandshakeFailedListener attribute of the SSLContextAdapterImpl object
     *
     * @param  listener  The feature to be added to the HandshakeFailedListener attribute
     */
    public void addHandshakeFailedListener(
            HandshakeFailedListener listener)
    {
        hfl = addToList(hfl, listener);
    }


    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public void removeHandshakeCompletedListener(
            HandshakeCompletedListener listener)
    {
        hcl = removeFromList(hcl, listener);
    }


    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public void removeHandshakeFailedListener(
            HandshakeFailedListener listener)
    {
        hfl = removeFromList(hfl, listener);
    }
    // Listeners

    // Add an element to a list, creating a new list if the
    // existing list is null, and return the list.
    static List addToList(List l, Object elt)
    {
        if (l == null) {
            l = new ArrayList();
        }
        l.add(elt);
        return l;
    }


    // Remove an element from a list, discarding the list if the
    // resulting list is empty, and return the list or null.
    static List removeFromList(List l, Object elt)
    {
        if (l == null) {
            return l;
        }
        l.remove(elt);
        if (l.size() == 0) {
            l = null;
        }
        return l;
    }


    /**
     *  Gets the sSLContext attribute of the SSLContextAdapterImpl object
     *
     * @return                               The sSLContext value
     * @exception  GeneralSecurityException  Description of the Exception
     */
    public SSLContext getSSLContext()
        throws GeneralSecurityException
    {
        init();
        return ctx;
    }


    /**
     *  Sets the key attribute of the SSLContextAdapterImpl object
     *
     * @param  key                           The new key value
     * @param  password                      The new key value
     * @exception  GeneralSecurityException  Description of the Exception
     */
    public void setKey(KeyStore key, char[] password)
        throws GeneralSecurityException
    {
        kmf.init(key, password);
        kms = kmf.getKeyManagers();
        dirty = true;
    }


    /**
     *  Sets the trust attribute of the SSLContextAdapterImpl object
     *
     * @param  cacerts                       The new trust value
     * @exception  GeneralSecurityException  Description of the Exception
     */
    public void setTrust(KeyStore cacerts)
        throws GeneralSecurityException
    {
        tmf.init(cacerts);
        tms = tmf.getTrustManagers();
        dirty = true;
    }


    /**
     *  Gets the keyManagers attribute of the SSLContextAdapterImpl object
     *
     * @return    The keyManagers value
     */
    public KeyManager[] getKeyManagers()
    {
        return kms;
    }


    /**
     *  Gets the trustManagers attribute of the SSLContextAdapterImpl object
     *
     * @return    The trustManagers value
     */
    public TrustManager[] getTrustManagers()
    {
        return tms;
    }


    /**
     *  Description of the Method
     *
     * @param  seed  Description of the Parameter
     */
    public void seedRandom(long seed)
    {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(seed);
            dirty = true;
        } catch (GeneralSecurityException e) {
            throw new ConfigurationError(
                    "could not instantiate SecureRandom", e);
        }
    }


    /**
     *  Sets the needClientAuth attribute of the SSLContextAdapterImpl object
     *
     * @param  needClientAuth  The new needClientAuth value
     */
    public void setNeedClientAuth(boolean needClientAuth)
    {
        this.needClientAuth = needClientAuth;
    }


    /**
     *  Gets the needClientAuth attribute of the SSLContextAdapterImpl object
     *
     * @return    The needClientAuth value
     */
    public boolean isNeedClientAuth()
    {
        return needClientAuth;
    }


    /**
     *  Sets the enabledProtocols attribute of the SSLContextAdapterImpl object
     *
     * @param  protocols  The new enabledProtocols value
     */
    public void setEnabledProtocols(String[] protocols)
    {
        this.protocols = (String[]) protocols.clone();
    }


    /**
     *  Gets the enabledProtocols attribute of the SSLContextAdapterImpl object
     *
     * @return    The enabledProtocols value
     */
    public String[] getEnabledProtocols()
    {
        return (String[]) protocols.clone();
    }


    /*
    public void setEnabledCipherSuites(String[] cipherSuites) {
        this.cipherSuites = (String[])cipherSuites.clone();
    }
    public String[] getEnabledCipherSuites() {
        return (String[])cipherSuites.clone();
    }
  */
    private SSLServerSocket getUnboundSSLServerSocket()
    {
        if (unboundSSLServerSocket != null) {
            return unboundSSLServerSocket;
        }
        try {
            ServerSocketFactory factory = ctx.getServerSocketFactory();
            unboundSSLServerSocket = (SSLServerSocket) factory.createServerSocket();
        } catch (IOException e) {
            throw new ConfigurationError(
                    "could not create unbounded ServerSocket", e);
        }
        return unboundSSLServerSocket;
    }


    /**
     *  Gets the supportedCipherSuites attribute of the SSLContextAdapterImpl object
     *
     * @return    The supportedCipherSuites value
     */
    public String[] getSupportedCipherSuites()
    {
        return getUnboundSSLServerSocket().getSupportedCipherSuites();
    }


    /**
     *  Gets the supportedProtocols attribute of the SSLContextAdapterImpl object
     *
     * @return    The supportedProtocols value
     */
    public String[] getSupportedProtocols()
    {
        return getUnboundSSLServerSocket().getSupportedProtocols();
    }


    /*
    public ServerSocketFactory getServerSocketFactory() {
        return new SSLServerSocketFactoryAdapter(cipherSuites);
    }
*/
    /**
     *  Gets the serverSocketFactory attribute of the SSLContextAdapterImpl object
     *
     * @param  cipherSuites                  Description of the Parameter
     * @return                               The serverSocketFactory value
     * @exception  GeneralSecurityException  Description of the Exception
     */
    public ServerSocketFactory getServerSocketFactory(String[] cipherSuites)
        throws GeneralSecurityException
    {
        return new SSLServerSocketFactoryAdapter(cipherSuites);
    }


    /*
    public SocketFactory getSocketFactory() {
        return new SSLSocketFactoryAdapter(cipherSuites);
    }
*/
    /**
     *  Gets the socketFactory attribute of the SSLContextAdapterImpl object
     *
     * @param  cipherSuites                  Description of the Parameter
     * @return                               The socketFactory value
     * @exception  GeneralSecurityException  Description of the Exception
     */
    public SocketFactory getSocketFactory(String[] cipherSuites)
        throws GeneralSecurityException
    {
        return new SSLSocketFactoryAdapter(cipherSuites);
    }


    /**
     *  Description of the Method
     *
     * @exception  GeneralSecurityException  Description of the Exception
     */
    public void init()
        throws GeneralSecurityException
    {
        if (dirty) {
            ctx.init(kms, tms, random);
            dirty = false;
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------
    private String toKeyStoreType(String fname)
    {
        return fname.endsWith(".p12")
                 || fname.endsWith(".P12")
                 ? "PKCS12" : "JKS";
    }


    /**
     *  Description of the Method
     *
     * @param  url                           Description of the Parameter
     * @param  password                      Description of the Parameter
     * @return                               Description of the Return Value
     * @exception  GeneralSecurityException  Description of the Exception
     * @exception  IOException               Description of the Exception
     */
    public KeyStore loadKeyStore(URL url, char[] password)
        throws GeneralSecurityException, IOException
    {
        InputStream in = url.openStream();
        try {
            return loadKeyStore(in, password, toKeyStoreType(url.getPath()));
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {}
        }
    }

    public KeyStore loadKeyStore(String url, char[] password)
    throws GeneralSecurityException, IOException
	{
    	try {
    		return loadKeyStore(new URL(url), password);
    	} catch (MalformedURLException e) {
    		return loadKeyStore(new File(url), password);
    	}
    }
    
    /**
     *  Description of the Method
     *
     * @param  file                          Description of the Parameter
     * @param  password                      Description of the Parameter
     * @return                               Description of the Return Value
     * @exception  GeneralSecurityException  Description of the Exception
     * @exception  IOException               Description of the Exception
     */
    public KeyStore loadKeyStore(File file, char[] password)
        throws GeneralSecurityException, IOException
    {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            return loadKeyStore(in, password, toKeyStoreType(file.getName()));
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {}
        }
    }


    /**
     *  Description of the Method
     *
     * @param  in                            Description of the Parameter
     * @param  password                      Description of the Parameter
     * @param  type                          Description of the Parameter
     * @return                               Description of the Return Value
     * @exception  GeneralSecurityException  Description of the Exception
     * @exception  IOException               Description of the Exception
     */
    public KeyStore loadKeyStore(InputStream in, char[] password, String type)
        throws GeneralSecurityException, IOException
    {
        KeyStore key = KeyStore.getInstance(type);
        key.load(in, password);
        return key;
    }

    // Inner classes -------------------------------------------------
    /**
     *  Description of the Class
     *
     * @author     gunter
     * @created    March 5, 2003
     */
    private class SSLServerSocketFactoryAdapter extends ServerSocketFactory
    {

        final SSLServerSocketFactory ssf;
        final String[] cipherSuites;


        SSLServerSocketFactoryAdapter(String[] cipherSuites)
            throws GeneralSecurityException
        {
            ssf = getSSLContext().getServerSocketFactory();
            this.cipherSuites = cipherSuites != null
                     ? (String[]) cipherSuites.clone() : null;
        }


        /**
         *  Description of the Method
         *
         * @param  port             Description of the Parameter
         * @param  backlog          Description of the Parameter
         * @return                  Description of the Return Value
         * @exception  IOException  Description of the Exception
         */
        public ServerSocket createServerSocket(int port, int backlog)
            throws IOException
        {
            return init((SSLServerSocket) ssf.createServerSocket(port, backlog));
        }


        /**
         *  Description of the Method
         *
         * @param  port             Description of the Parameter
         * @param  backlog          Description of the Parameter
         * @param  ia               Description of the Parameter
         * @return                  Description of the Return Value
         * @exception  IOException  Description of the Exception
         */
        public ServerSocket createServerSocket(int port, int backlog,
                InetAddress ia)
            throws IOException
        {
            return init(
                    (SSLServerSocket) ssf.createServerSocket(port, backlog, ia));
        }


        /**
         *  Description of the Method
         *
         * @param  port             Description of the Parameter
         * @return                  Description of the Return Value
         * @exception  IOException  Description of the Exception
         */
        public ServerSocket createServerSocket(int port)
            throws IOException
        {
            return init((SSLServerSocket) ssf.createServerSocket(port));
        }


        private ServerSocket init(SSLServerSocket ss)
        {
            ss.setNeedClientAuth(isNeedClientAuth());
            ss.setEnabledProtocols(getEnabledProtocols());
            if (cipherSuites != null) {
                ss.setEnabledCipherSuites(cipherSuites);
            }
            return ss;
        }
    }


    /**
     *  Description of the Class
     *
     * @author     gunter
     * @created    March 5, 2003
     */
    private class SSLSocketFactoryAdapter extends SocketFactory
    {

        final SSLSocketFactory sf;
        final String[] cipherSuites;


        SSLSocketFactoryAdapter(String[] cipherSuites)
            throws GeneralSecurityException
        {
            sf = getSSLContext().getSocketFactory();
            this.cipherSuites = cipherSuites != null
                     ? (String[]) cipherSuites.clone() : null;
        }


        /**
         *  Description of the Method
         *
         * @param  ia               Description of the Parameter
         * @param  port             Description of the Parameter
         * @return                  Description of the Return Value
         * @exception  IOException  Description of the Exception
         */
        public Socket createSocket(InetAddress ia, int port)
            throws IOException
        {
            return init((SSLSocket) sf.createSocket(ia, port));
        }


        /**
         *  Description of the Method
         *
         * @param  ia               Description of the Parameter
         * @param  port             Description of the Parameter
         * @param  clientIA         Description of the Parameter
         * @param  clientPort       Description of the Parameter
         * @return                  Description of the Return Value
         * @exception  IOException  Description of the Exception
         */
        public Socket createSocket(InetAddress ia, int port,
                InetAddress clientIA, int clientPort)
            throws IOException
        {
            return init(
                    (SSLSocket) sf.createSocket(ia, port, clientIA, clientPort));
        }


        /**
         *  Description of the Method
         *
         * @param  host             Description of the Parameter
         * @param  port             Description of the Parameter
         * @param  clientIA         Description of the Parameter
         * @param  clientPort       Description of the Parameter
         * @return                  Description of the Return Value
         * @exception  IOException  Description of the Exception
         */
        public Socket createSocket(String host, int port,
                InetAddress clientIA, int clientPort)
            throws IOException
        {
            return init(
                    (SSLSocket) sf.createSocket(host, port, clientIA, clientPort));
        }


        /**
         *  Description of the Method
         *
         * @param  host             Description of the Parameter
         * @param  port             Description of the Parameter
         * @return                  Description of the Return Value
         * @exception  IOException  Description of the Exception
         */
        public Socket createSocket(String host, int port)
            throws IOException
        {
            return init((SSLSocket) sf.createSocket(host, port));
        }


        private Socket init(SSLSocket s)
            throws IOException
        {
            if (cipherSuites != null) {
                s.setEnabledCipherSuites(cipherSuites);
            }
            s.setEnabledProtocols(getEnabledProtocols());
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
                        log.error("SSL peer not verified:", e);
                    } catch ( CertificateException ce ) {
                    	throw new IOException(ce.getMessage());
                    }
                }
            } catch (IOException e) {
                if (hfl != null) {
                    HandshakeFailedEvent event = new HandshakeFailedEvent(s, e);
                    for (int i = 0, n = hfl.size(); i < n; ++i) {
                        ((HandshakeFailedListener) hfl.get(i)).handshakeFailed(event);
                    }
                }
                throw e;
            }
            return s;
        }
    }


    /**
     *  Description of the Class
     *
     * @author     gunter
     * @created    March 5, 2003
     */
    static class ConfigurationError extends Error
    {
        ConfigurationError(String msg, Exception x)
        {
            super(msg, x);
        }
    }
}

