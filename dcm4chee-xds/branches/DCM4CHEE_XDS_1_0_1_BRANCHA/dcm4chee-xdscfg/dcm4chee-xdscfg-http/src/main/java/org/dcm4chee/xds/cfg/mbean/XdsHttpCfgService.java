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

package org.dcm4chee.xds.cfg.mbean;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.dcm4chee.xds.common.delegate.XdsHttpCfgDelegate;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A MBean service to manage configurations for XDS services.
 * <p>
 * This service can be used to save and load jmx attributes for a configurable set of mbeans.
 * <p>
 * 
 * @author franz.willer@agfa.com
 * @version $Revision$ $Date$
 * @since 05.09.2007
 */
public class XdsHttpCfgService extends ServiceMBeanSupport {

    private static final String HIDE_PASSWD = "####";
    private static final String CERT = "CERT";
    private static final String NONE = "NONE";
    private static final String EMPTY = "";

    public static final String HTTP_PROXY_HOST = "http.proxyHost";
    public static final String HTTP_PROXY_PORT = "http.proxyPort";
    public static final String HTTP_PROXY_USER = "http.proxy.username";
    public static final String HTTP_PROXY_PASSWD = "http.proxy.password";
    public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    public static final String HTTPS_PROXY_HOST = "https.proxyHost";
    public static final String HTTPS_PROXY_PORT = "https.proxyPort";
    public static final String HTTPS_PROXY_USER = "https.proxy.username";
    public static final String HTTPS_PROXY_PASSWD = "https.proxy.password";
    public static final String HTTPS_NON_PROXY_HOSTS = "https.nonProxyHosts";
    public static final String HTTPS_PROTOCOLS = "https.protocols";
    public static final String HTTPS_CIPHER_SUITES = "https.cipherSuites";

    public static final String TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
    public static final String TRUST_STORE = "javax.net.ssl.trustStore";
    public static final String KEY_STORE_TYPE = "javax.net.ssl.keyStoreType";
    public static final String KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword";
    public static final String KEY_STORE = "javax.net.ssl.keyStore";

    private static final String SOCKS_PROXY_HOST = "socksProxyHost";
    private static final String SOCKS_PROXY_PORT = "socksProxyPort";
    private static final String SOCKS_NON_PROXY_HOSTS = "socksNonProxyHosts";
    private static final String FTP_PROXY_HOST = "ftp.proxyHost";
    private static final String FTP_PROXY_PORT = "ftp.proxyPort";
    private static final String FTP_NON_PROXY_HOSTS = "ftp.nonProxyHosts";

    private static final String HTTP_AGENT = "http.agent";
    private static final String HTTP_AUTH_VALIDATE_SERVER = "http.auth.digest.validateServer";
    private static final String HTTP_AUTH_VALIDATE_PROXY = "http.auth.digest.validateProxy";
    private static final String HTTP_AUTH_VALIDATE_CNONCE_REPEAT = "http.auth.digest.cnonceRepeat";
    private static final String HTTP_AUTH_NTLM_DOMAIN = "http.auth.ntlm.domain";
    private static final String HTTP_KEEP_ALIVE = "http.keepAlive";
    private static final String HTTP_MAX_CONNECTIONS = "http.maxConnections";

    private static final String PREVER_IP_V4_STACK = "java.net.preferIPv4Stack";
    private static final String PREFER_IP_V6_ADDR = "java.net.preferIPv6Addresses";
    private static final String NW_ADDR_CACHE_TTL = "networkaddress.cache.ttl";
    private static final String NW_ADDR_CACHE_NEGATIVE_TTL = "networkaddress.cache.negative.ttl";

    private int proxyPort;
    private int secureProxyPort;
    private int socksProxyPort;

    final String[] authCredential = new String[2];

    private String keystoreURL = "resource:identity.p12";
    private String keystorePassword;
    private String keystoreType;
    private String trustStoreURL = "resource:cacerts.jks";
    private String trustStorePassword;
    private HostnameVerifier origHostnameVerifier = null;
    private String allowedUrlHost = null;

    private boolean sslConfigured = false;

    static Logger log = LoggerFactory.getLogger(XdsHttpCfgService.class);

    protected void startService() {
        XdsHttpCfgDelegate.setXdsRepositoryServiceName(this.getServiceName());
    }

    public String getProxyHost() {
        return System.getProperty(HTTP_PROXY_HOST, NONE);
    }
    public void setProxyHost(String proxyHost) {
        if ( NONE.equals(proxyHost) ) {
            System.getProperties().remove(HTTP_PROXY_HOST);
            System.getProperties().remove(HTTP_PROXY_PORT);
        } else {
            System.setProperty(HTTP_PROXY_HOST, proxyHost.trim());
            System.setProperty(HTTP_PROXY_PORT, String.valueOf(proxyPort));
        }
    }

    public int getProxyPort() {
        return proxyPort;
    }
    public void setProxyPort(int proxyPort) {
        if ( this.proxyPort != proxyPort ) {
            this.proxyPort = proxyPort;
            if (System.getProperty(HTTP_PROXY_HOST) != null)
                System.setProperty(HTTP_PROXY_PORT, String.valueOf(proxyPort));
        }
    }
    public String getProxyUser() {
        return System.getProperty(HTTP_PROXY_USER, NONE);
    }
    public void setProxyUser(String user) {
        setOrRemoveAttribute(HTTP_PROXY_USER, user.trim());
    }
    public String getProxyPasswd() {
        return System.getProperty(HTTP_PROXY_PASSWD);
    }
    public void setProxyPasswd(String passwd) {
        setOrRemoveAttribute(HTTP_PROXY_PASSWD, passwd.trim());
    }
    public String getNonProxyHosts() {
        return System.getProperty(HTTP_NON_PROXY_HOSTS, NONE);
    }
    public void setNonProxyHosts(String hosts) {
        setOrRemoveAttribute(HTTP_NON_PROXY_HOSTS, hosts.trim());
    }

    public String getSecureProxyHost() {
        return System.getProperty(HTTPS_PROXY_HOST, NONE);
    }
    public void setSecureProxyHost(String proxyHost) {
        if ( NONE.equals(proxyHost) ) {
            System.getProperties().remove(HTTPS_PROXY_HOST);
            System.getProperties().remove(HTTPS_PROXY_PORT);
        } else {
            System.setProperty(HTTPS_PROXY_HOST, proxyHost.trim());
            System.setProperty(HTTPS_PROXY_PORT, String.valueOf(secureProxyPort));
        }
    }
    public int getSecureProxyPort() {
        return secureProxyPort;
    }
    public void setSecureProxyPort(int proxyPort) {
        if ( this.secureProxyPort != proxyPort ) {
            this.secureProxyPort = proxyPort;
            if (System.getProperty(HTTPS_PROXY_HOST) != null)
                System.setProperty(HTTPS_PROXY_PORT, String.valueOf(proxyPort));
        }
    }
    public String getSecureProxyUser() {
        return System.getProperty(HTTPS_PROXY_USER, NONE);
    }
    public void setSecureProxyUser(String user) {
        setOrRemoveAttribute(HTTPS_PROXY_USER, user.trim());
    }
    public String getSecureProxyPasswd() {
        return System.getProperty(HTTPS_PROXY_PASSWD);
    }
    public void setSecureProxyPasswd(String passwd) {
        setOrRemoveAttribute(HTTPS_PROXY_PASSWD, passwd.trim());
    }
    public String getSecureNonProxyHosts() {
        return System.getProperty(HTTPS_NON_PROXY_HOSTS, NONE);
    }
    public void setSecureNonProxyHosts(String hosts) {
        setOrRemoveAttribute(HTTPS_NON_PROXY_HOSTS, hosts.trim());
    }

    public String getHttpsProtocols() {
        return System.getProperty(HTTPS_PROTOCOLS, NONE);
    }
    public void setHttpsProtocols(String p) {
        setOrRemoveAttribute(HTTPS_PROTOCOLS, p.trim());
    }

    public String getHttpsCipherSuites() {
        return System.getProperty(HTTPS_CIPHER_SUITES, NONE);
    }
    public void setHttpsCipherSuites(String ciphers) {
        setOrRemoveAttribute(HTTPS_CIPHER_SUITES, ciphers.trim());
    }

    public String getAuthUser() {
        return authCredential[0] == null ? NONE : authCredential[0];
    }

    public void setAuthUser(String authUser) {
        authCredential[0] = NONE.equals(authUser) ? null : authUser;
        if ( authCredential[0] != null ) {
            Authenticator.setDefault(new ProxyAuth());
        }
    }

    public void setAuthPasswd(String passwd) {
        authCredential[1] = NONE.equals(passwd) ? null : passwd;
    }

    public String getSocksProxyHost() {
        return System.getProperty(SOCKS_PROXY_HOST, NONE);
    }
    public void setSocksProxyHost(String proxyHost) {
        if ( NONE.equals(proxyHost) ) {
            System.getProperties().remove(SOCKS_PROXY_HOST);
            System.getProperties().remove(SOCKS_PROXY_PORT);
        } else {
            System.setProperty(SOCKS_PROXY_HOST, proxyHost.trim());
            System.setProperty(SOCKS_PROXY_PORT, String.valueOf(socksProxyPort));
        }
    }
    public int getSocksProxyPort() {
        return socksProxyPort;
    }
    public void setSocksProxyPort(int port) {
        if ( this.socksProxyPort != port ) {
            this.socksProxyPort = port;
            if (System.getProperty(SOCKS_PROXY_HOST) != null)
                System.setProperty(SOCKS_PROXY_PORT, String.valueOf(port));
        }
    }
    public String getSocksNonProxyHosts() {
        return System.getProperty(SOCKS_NON_PROXY_HOSTS, NONE);
    }
    public void setSocksNonProxyHosts(String hosts) {
        setOrRemoveAttribute(SOCKS_NON_PROXY_HOSTS, hosts.trim());
    }

    public void setKeyStorePassword(String keyStorePassword) {
        if ( NONE.equals(keyStorePassword)) keyStorePassword = null;
        this.keystorePassword = keyStorePassword.trim();
    }
    
    public String getKeyStoreType() {
    	if (keystoreType == null) {
    		return NONE;
    	}
    	return keystoreType;
    }
    
    public void setKeyStoreType(String keystoreType) {
    	if (NONE.compareToIgnoreCase(keystoreType) == 0) {
    		this.keystoreType = null;
    	} else {
    		this.keystoreType = keystoreType;
    	}
    }
    
    public String getKeyStoreURL() {
        return keystoreURL;
    }
    public void setKeyStoreURL(String keyStoreURL) {
        this.keystoreURL = keyStoreURL.trim();
    }
    public String getTrustStoreURL() {
        return trustStoreURL == null ? NONE : trustStoreURL;
    }
    public void setTrustStoreURL(String trustStoreURL) {
        if ( NONE.equals(trustStoreURL ) ) {
            this.trustStoreURL = null;
        } else {
            this.trustStoreURL = trustStoreURL.trim();
        }
    }
    public void setTrustStorePassword(String trustStorePassword) {
        if ( NONE.equals(trustStorePassword)) trustStorePassword = null;
        this.trustStorePassword = trustStorePassword;
    }
    public String getAllowedUrlHost() {
        return allowedUrlHost == null ? CERT : allowedUrlHost;
    }
    public void setAllowedUrlHost(String allowedUrlHost) {
        this.allowedUrlHost = CERT.equals(allowedUrlHost) ? null : allowedUrlHost.trim();
    }

    public int configTLS(String url) {
        int rsp = XdsHttpCfgDelegate.CFG_RSP_OK;
        log.debug("configTLS called for URL:"+url);
        if ( !url.toLowerCase().startsWith("https") ) {
            log.debug("NO TLS connection required! Do not configure SSL!");
            rsp = XdsHttpCfgDelegate.CFG_RSP_IGNORED;
        } else if ( sslConfigured ) {
            log.debug("SSL is already configured! You have to restart JBoss to apply changes!");
            rsp = XdsHttpCfgDelegate.CFG_RSP_ALREADY;
        } else if ( trustStoreURL == null ) {
            log.warn("NO Trust Store URL configured!");
            rsp = XdsHttpCfgDelegate.CFG_RSP_ERROR;
        } else {
            String keyStorePath = resolvePath(keystoreURL);
            String trustStorePath = resolvePath(trustStoreURL);
            System.setProperty(KEY_STORE, keyStorePath);
            if ( keystorePassword != null ) 
                System.setProperty(KEY_STORE_PASSWORD, keystorePassword);
            if ( keystoreType != null )
                System.setProperty(KEY_STORE_TYPE, keystoreType);
            System.setProperty(TRUST_STORE, trustStorePath);
            if ( trustStorePassword != null )
                System.setProperty(TRUST_STORE_PASSWORD, trustStorePassword);
            if ( origHostnameVerifier == null) {
                origHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
                HostnameVerifier hv = new HostnameVerifier() {
                    public boolean verify(String urlHostName, SSLSession session) {
                        if ( !origHostnameVerifier.verify ( urlHostName, session)) {
                            if ( isAllowedUrlHost(urlHostName)) {
                                log.warn("Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
                            } else {
                                return false;
                            }
                        }
                        return true;
                    }

                    private boolean isAllowedUrlHost(String urlHostName) {
                        if (allowedUrlHost == null) return false;
                        if ( allowedUrlHost.equals("*")) return true;
                        return allowedUrlHost.equals(urlHostName);
                    }

                };

                HttpsURLConnection.setDefaultHostnameVerifier(hv);
            }
            sslConfigured = true;
        }
        if ( log.isDebugEnabled()) {
            log.debug("Current http configuration:\n"+listProperties());
        }
        return rsp;
    }

    public static String resolvePath(String fn) {
        File f = new File(fn);
        if (f.isAbsolute()) return f.getAbsolutePath();
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath()).getAbsolutePath();
    }

    private void setOrRemoveAttribute(String name, String value) {
        if ( NONE.equals(value) ) {
            System.getProperties().remove(name);
        } else {
            System.setProperty(name, value);
        }
    }

    public String listProperties() {
        StringBuffer sb = new StringBuffer();
        sb.append("HTTP Proxy:");
        sb.append("\n  ").append(HTTP_PROXY_HOST).append('=').append(System.getProperty(HTTP_PROXY_HOST, EMPTY));
        sb.append("\n  ").append(HTTP_PROXY_PORT).append('=').append(System.getProperty(HTTP_PROXY_PORT, EMPTY));
        sb.append("\n  ").append(HTTP_PROXY_USER).append('=').append(System.getProperty(HTTP_PROXY_USER, EMPTY));
        sb.append("\n  ").append(HTTP_PROXY_PASSWD).append('=').append(System.getProperty(HTTP_PROXY_PASSWD) == null ? EMPTY : HIDE_PASSWD);
        sb.append("\n  ").append(HTTP_NON_PROXY_HOSTS).append('=').append(System.getProperty(HTTP_NON_PROXY_HOSTS, EMPTY));
        sb.append("\nHTTPS Proxy:");
        sb.append("\n  ").append(HTTPS_PROXY_HOST).append('=').append(System.getProperty(HTTPS_PROXY_HOST, EMPTY));
        sb.append("\n  ").append(HTTPS_PROXY_PORT).append('=').append(System.getProperty(HTTPS_PROXY_PORT, EMPTY));
        sb.append("\n  ").append(HTTPS_PROXY_USER).append('=').append(System.getProperty(HTTPS_PROXY_USER, EMPTY));
        sb.append("\n  ").append(HTTPS_PROXY_PASSWD).append('=').append(System.getProperty(HTTPS_PROXY_PASSWD) == null ? EMPTY : HIDE_PASSWD);
        sb.append("\n  ").append(HTTPS_NON_PROXY_HOSTS).append('=').append(System.getProperty(HTTPS_NON_PROXY_HOSTS, EMPTY));
        sb.append("\n  ").append(HTTPS_PROTOCOLS).append('=').append(System.getProperty(HTTPS_PROTOCOLS, EMPTY));
        sb.append("\n  ").append(HTTPS_CIPHER_SUITES).append('=').append(System.getProperty(HTTPS_CIPHER_SUITES, EMPTY));
        sb.append("\nSocks Proxy:");
        sb.append("\n  ").append(SOCKS_PROXY_HOST).append('=').append(System.getProperty(SOCKS_PROXY_HOST, EMPTY));
        sb.append("\n  ").append(SOCKS_PROXY_PORT).append('=').append(System.getProperty(SOCKS_PROXY_PORT, EMPTY));
        sb.append("\n  ").append(SOCKS_NON_PROXY_HOSTS).append('=').append(System.getProperty(SOCKS_NON_PROXY_HOSTS, EMPTY));
        sb.append("\nSSL Configuration:");
        sb.append("\n  ").append(TRUST_STORE).append('=').append(System.getProperty(TRUST_STORE, EMPTY));
        sb.append("\n  ").append(TRUST_STORE_PASSWORD).append('=').append(System.getProperty(TRUST_STORE_PASSWORD) == null ? EMPTY : HIDE_PASSWD);
        sb.append("\n  ").append(KEY_STORE).append('=').append(System.getProperty(KEY_STORE, EMPTY));
        sb.append("\n  ").append(KEY_STORE_TYPE).append('=').append(System.getProperty(KEY_STORE_TYPE, EMPTY));
        sb.append("\n  ").append(KEY_STORE_PASSWORD).append('=').append(System.getProperty(KEY_STORE_PASSWORD) == null ? EMPTY : HIDE_PASSWD);
        sb.append("\n======================================================================");
        sb.append("\nOther:");
        sb.append("\n  ").append(FTP_PROXY_HOST).append('=').append(System.getProperty(FTP_PROXY_HOST, EMPTY));
        sb.append("\n  ").append(FTP_PROXY_PORT).append('=').append(System.getProperty(FTP_PROXY_PORT, EMPTY));
        sb.append("\n  ").append(FTP_NON_PROXY_HOSTS).append('=').append(System.getProperty(FTP_NON_PROXY_HOSTS, EMPTY));

        sb.append("\n  ").append(HTTP_AGENT).append('=').append(System.getProperty(HTTP_AGENT, EMPTY));
        sb.append("\n  ").append(HTTP_AUTH_VALIDATE_SERVER).append('=').append(System.getProperty(HTTP_AUTH_VALIDATE_SERVER, EMPTY));
        sb.append("\n  ").append(HTTP_AUTH_VALIDATE_PROXY).append('=').append(System.getProperty(HTTP_AUTH_VALIDATE_PROXY, EMPTY));
        sb.append("\n  ").append(HTTP_AUTH_VALIDATE_CNONCE_REPEAT).append('=').append(System.getProperty(HTTP_AUTH_VALIDATE_CNONCE_REPEAT, EMPTY));
        sb.append("\n  ").append(HTTP_AUTH_NTLM_DOMAIN).append('=').append(System.getProperty(HTTP_AUTH_NTLM_DOMAIN, EMPTY));
        sb.append("\n  ").append(HTTP_KEEP_ALIVE).append('=').append(System.getProperty(HTTP_KEEP_ALIVE, EMPTY));
        sb.append("\n  ").append(HTTP_MAX_CONNECTIONS).append('=').append(System.getProperty(HTTP_MAX_CONNECTIONS, EMPTY));

        sb.append("\n  ").append(PREVER_IP_V4_STACK).append('=').append(System.getProperty(PREVER_IP_V4_STACK, EMPTY));
        sb.append("\n  ").append(PREFER_IP_V6_ADDR).append('=').append(System.getProperty(PREFER_IP_V6_ADDR, EMPTY));
        sb.append("\n  ").append(NW_ADDR_CACHE_TTL).append('=').append(System.getProperty(NW_ADDR_CACHE_TTL, EMPTY));
        sb.append("\n  ").append(NW_ADDR_CACHE_NEGATIVE_TTL).append('=').append(System.getProperty(NW_ADDR_CACHE_NEGATIVE_TTL, EMPTY));
        return sb.toString();
    }

    private class ProxyAuth extends Authenticator {
        public ProxyAuth() {
            log.info("################ create ProxyAuth!");
        }
        protected PasswordAuthentication getPasswordAuthentication() {
            log.info("################ getPasswordAuthentication! user:"+authCredential[0]+" passwd:"+authCredential[1]);
            return(new PasswordAuthentication(authCredential[0], authCredential[1].toCharArray()));
        }
    }    
}
