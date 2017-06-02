/*                                                                           *
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG                             *
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
package org.dcm4che.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.dcm4che.Implementation;

/**
 * <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since    June, 2002
 * @version    $Revision: 3753 $ $Date: 2004-03-12 06:11:40 +0800 (周五, 12 3月 2004) $
 */
public abstract class SSLContextAdapter
{
    // Constants -----------------------------------------------------
    /**  Description of the Field */
    public final static String SSL_RSA_WITH_NULL_SHA =
            "SSL_RSA_WITH_NULL_SHA";
    /**  Description of the Field */
    public final static String SSL_RSA_WITH_3DES_EDE_CBC_SHA =
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA";

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------
    /**
     *  Gets the instance attribute of the SSLContextAdapter class
     *
     * @return    The instance value
     */
    public static SSLContextAdapter getInstance()
    {
        return (SSLContextAdapter) Implementation.findFactory(
                "dcm4che.util.SSLContextAdapter");
    }

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------
    /**
     *  Gets the sSLContext attribute of the SSLContextAdapter object
     *
     * @return    The sSLContext value
     */
    public abstract SSLContext getSSLContext()
        throws GeneralSecurityException;


    /**
     *  Gets the supportedCipherSuites attribute of the SSLContextAdapter object
     *
     * @return    The supportedCipherSuites value
     */
    public abstract String[] getSupportedCipherSuites();


    /**
     *  Sets the enabledProtocols attribute of the SSLContextAdapter object
     *
     * @param  protocols  The new enabledProtocols value
     */
    public abstract void setEnabledProtocols(String[] protocols);

//    public abstract void setEnabledCipherSuites(String[] cipherSuites);

//    public abstract String[] getEnabledCipherSuites();

    /**
     *  Adds a feature to the HandshakeCompletedListener attribute of the SSLContextAdapter object
     *
     * @param  listener  The feature to be added to the HandshakeCompletedListener attribute
     */
    public abstract void addHandshakeCompletedListener(
            HandshakeCompletedListener listener);


    /**
     *  Adds a feature to the HandshakeFailedListener attribute of the SSLContextAdapter object
     *
     * @param  listener  The feature to be added to the HandshakeFailedListener attribute
     */
    public abstract void addHandshakeFailedListener(
            HandshakeFailedListener listener);


    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public abstract void removeHandshakeCompletedListener(
            HandshakeCompletedListener listener);


    /**
     *  Description of the Method
     *
     * @param  listener  Description of the Parameter
     */
    public abstract void removeHandshakeFailedListener(
            HandshakeFailedListener listener);


    /**
     *  Gets the enabledProtocols attribute of the SSLContextAdapter object
     *
     * @return    The enabledProtocols value
     */
    public abstract String[] getEnabledProtocols();


    /**
     *  Gets the supportedProtocols attribute of the SSLContextAdapter object
     *
     * @return    The supportedProtocols value
     */
    public abstract String[] getSupportedProtocols();


    /**
     *  Sets the needClientAuth attribute of the SSLContextAdapter object
     *
     * @param  needClientAuth  The new needClientAuth value
     */
    public abstract void setNeedClientAuth(boolean needClientAuth);


    /**
     *  Gets the needClientAuth attribute of the SSLContextAdapter object
     *
     * @return    The needClientAuth value
     */
    public abstract boolean isNeedClientAuth();

    //   public abstract void setStartHandshake(boolean startHandshake);

    /**
     *  Description of the Method
     *
     * @param  seed  Description of the Parameter
     */
    public abstract void seedRandom(long seed);


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
    public abstract KeyStore loadKeyStore(InputStream in, char[] password, String type)
        throws GeneralSecurityException, IOException;


    /**
     *  Description of the Method
     *
     * @param  file                          Description of the Parameter
     * @param  password                      Description of the Parameter
     * @return                               Description of the Return Value
     * @exception  GeneralSecurityException  Description of the Exception
     * @exception  IOException               Description of the Exception
     */
    public abstract KeyStore loadKeyStore(File file, char[] password)
        throws GeneralSecurityException, IOException;


    /**
     *  Description of the Method
     *
     * @param  url                           Description of the Parameter
     * @param  password                      Description of the Parameter
     * @return                               Description of the Return Value
     * @exception  GeneralSecurityException  Description of the Exception
     * @exception  IOException               Description of the Exception
     */
    public abstract KeyStore loadKeyStore(URL url, char[] password)
        throws GeneralSecurityException, IOException;

    public abstract KeyStore loadKeyStore(String url, char[] password)
    throws GeneralSecurityException, IOException;

    /**
     *  Sets the key attribute of the SSLContextAdapter object
     *
     * @param  key                           The new key value
     * @param  password                      The new key value
     * @exception  GeneralSecurityException  Description of the Exception
     */
    public abstract void setKey(KeyStore key, char[] password)
        throws GeneralSecurityException;


    /**
     *  Gets the keyManagers attribute of the SSLContextAdapter object
     *
     * @return    The keyManagers value
     */
    public abstract KeyManager[] getKeyManagers();


    /**
     *  Sets the trust attribute of the SSLContextAdapter object
     *
     * @param  cacerts                       The new trust value
     * @exception  GeneralSecurityException  Description of the Exception
     */
    public abstract void setTrust(KeyStore cacerts)
        throws GeneralSecurityException;


    /**
     *  Gets the trustManagers attribute of the SSLContextAdapter object
     *
     * @return    The trustManagers value
     */
    public abstract TrustManager[] getTrustManagers();


    /**
     *  Description of the Method
     *
     * @exception  GeneralSecurityException  Description of the Exception
     */
    public abstract void init()
        throws GeneralSecurityException;

//    public abstract SocketFactory getSocketFactory();

    /**
     *  Gets the socketFactory attribute of the SSLContextAdapter object
     *
     * @param  cipherSuites  Description of the Parameter
     * @return               The socketFactory value
     */
    public abstract SocketFactory getSocketFactory(String[] cipherSuites)
        throws GeneralSecurityException;

//    public abstract ServerSocketFactory getServerSocketFactory();

    /**
     *  Gets the serverSocketFactory attribute of the SSLContextAdapter object
     *
     * @param  cipherSuites  Description of the Parameter
     * @return               The serverSocketFactory value
     */
    public abstract ServerSocketFactory getServerSocketFactory(String[] cipherSuites)
        throws GeneralSecurityException;
}

