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

package org.dcm4chex.archive.mbean;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

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
 * @version $Revision: 2495 $ $Date: 2006-05-26 20:33:15 +0800 (周五, 26 5月 2006) $
 * @since 13.12.2004
 */
public final class TLSConfigDelegate {

    private final ServiceMBeanSupport service;
    
    private int soSndBuf;

    private int soRcvBuf;

    private boolean tcpNoDelay = true;

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

    public final int getReceiveBufferSize() {
        return soRcvBuf;
    }
    
    public final void setReceiveBufferSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size: " + size);
        }
        this.soRcvBuf = size;
    }

    public final int getSendBufferSize() {
        return soSndBuf;        
    }
    
    public final void setSendBufferSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size: " + size);
        }
        this.soSndBuf = size;
    }
        
    public final boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public final void setTcpNoDelay(boolean on) {
        this.tcpNoDelay = on;
    }
                
    public HandshakeFailedListener handshakeFailedListener() {
        try {
	        return (HandshakeFailedListener) service.getServer().invoke(
	                tlsConfigName, "handshakeFailedListener", null, null);
	    } catch (InstanceNotFoundException e) {
	        throw new ConfigurationException(e);
	    } catch (MBeanException e) {
	        throw new ConfigurationException(e);
	    } catch (ReflectionException e) {
	        throw new ConfigurationException(e);
	    }
    }

    public ServerSocketFactory serverSocketFactory(String[] cipherSuites) {
        try {
            return (ServerSocketFactory) service.getServer().invoke(
                    tlsConfigName, "serverSocketFactory",
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

    public SocketFactory socketFactory(String[] cipherSuites) {
        try {
            return (SocketFactory) service.getServer().invoke(tlsConfigName,
                    "socketFactory", new Object[] { cipherSuites},
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
        Socket s;
        if (cipherSuites == null || cipherSuites.length == 0) {
            s = new Socket(aeData.getHostName(), aeData.getPort());
        } else {
            s = socketFactory(cipherSuites).createSocket(
                    aeData.getHostName(), aeData.getPort());
        }
        initSendBufferSize(s);
        initReceiveBufferSize(s);
        if (s.getTcpNoDelay() != tcpNoDelay) {
            s.setTcpNoDelay(tcpNoDelay );
        }
        return s;
    }

    private void initSendBufferSize(Socket s) throws SocketException {
        int tmp = s.getSendBufferSize();
        if (soSndBuf == 0) {
            soSndBuf = tmp;
        }
        if (soSndBuf != tmp) {
            s.setSendBufferSize(soSndBuf);
            soSndBuf = s.getSendBufferSize();
        }
    }
    
    private void initReceiveBufferSize(Socket s) throws SocketException {
        int tmp = s.getReceiveBufferSize();
        if (soRcvBuf == 0) {
            soRcvBuf = tmp;
        }
        if (soRcvBuf != tmp) {
            s.setReceiveBufferSize(soRcvBuf);
            soRcvBuf = s.getReceiveBufferSize();
        }
    }
    
    
}
