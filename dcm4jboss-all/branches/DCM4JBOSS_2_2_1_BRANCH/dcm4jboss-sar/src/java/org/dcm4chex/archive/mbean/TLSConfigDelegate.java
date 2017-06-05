/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.io.IOException;
import java.net.Socket;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.dcm4che.util.HandshakeFailedListener;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1292 $ $Date: 2004-12-14 00:56:26 +0800 (周二, 14 12月 2004) $
 * @since 13.12.2004
 */
public final class TLSConfigDelegate {

    private final ServiceMBeanSupport service;
    
    private ObjectName tlsConfigName;

    public TLSConfigDelegate(final ServiceMBeanSupport service) {
        this.service = service;
    }
    
    public final ObjectName getTLSConfigName() {
        return tlsConfigName;
    }

    public final void setTLSConfigName(ObjectName tlsConfigName) {
        this.tlsConfigName = tlsConfigName;
    }
    
    public HandshakeFailedListener getHandshakeFailedListener() {
        try {
	        return (HandshakeFailedListener) service.getServer().invoke(
	                tlsConfigName, "getHandshakeFailedListener", null, null);
	    } catch (InstanceNotFoundException e) {
	        throw new ConfigurationException(e);
	    } catch (MBeanException e) {
	        throw new ConfigurationException(e);
	    } catch (ReflectionException e) {
	        throw new ConfigurationException(e);
	    }
    }

    public ServerSocketFactory getServerSocketFactory(String[] cipherSuites) {
        try {
            return (ServerSocketFactory) service.getServer().invoke(
                    tlsConfigName, "getServerSocketFactory",
                    new Object[] { cipherSuites},
                    new String[] { String[].class.getName(),});
        } catch (InstanceNotFoundException e) {
            throw new ConfigurationException(e);
        } catch (MBeanException e) {
            throw new ConfigurationException(e);
        } catch (ReflectionException e) {
            throw new ConfigurationException(e);
        }
    }

    public SocketFactory getSocketFactory(String[] cipherSuites) {
        try {
            return (SocketFactory) service.getServer().invoke(tlsConfigName,
                    "getSocketFactory", new Object[] { cipherSuites},
                    new String[] { String[].class.getName(),});
        } catch (InstanceNotFoundException e) {
            throw new ConfigurationException(e);
        } catch (MBeanException e) {
            throw new ConfigurationException(e);
        } catch (ReflectionException e) {
            throw new ConfigurationException(e);
        }
    }

    public Socket createSocket(AEData aeData) throws IOException {
        String[] cipherSuites = aeData.getCipherSuites();
        if (cipherSuites == null || cipherSuites.length == 0) {
            return new Socket(aeData.getHostName(), aeData.getPort());
        } else {
            return getSocketFactory(cipherSuites).createSocket(
                    aeData.getHostName(), aeData.getPort());
        }
    }
    
}
