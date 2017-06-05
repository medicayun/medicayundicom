/* $Id: AbstractScpService.java 1132 2004-06-14 14:34:51Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.archive.dcm;

import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.sql.DataSource;

import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.jboss.system.ServiceMBeanSupport;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1132 $ $Date: 2004-06-14 22:34:51 +0800 (周一, 14 6月 2004) $
 * @since 31.08.2003
 */
public abstract class AbstractScpService extends ServiceMBeanSupport {

    private static final String ANY = "ANY";

    private static final Map dumpParam = new HashMap(5);
    static {
        dumpParam.put("maxlen", new Integer(128));
        dumpParam.put("vallen", new Integer(64));
        dumpParam.put("prefix", "\t");
    }

    protected static final String[] ONLY_DEFAULT_TS = { UIDs.ImplicitVRLittleEndian,};

    protected static final String[] NATIVE_LE_TS = { UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian,};

    protected final static AssociationFactory asf = AssociationFactory.getInstance();

    protected ObjectName dcmServerName;

    protected DcmHandler dcmHandler;

    protected String[] calledAETs = {};

    protected String[] callingAETs = { ANY };
    
    protected boolean acceptExplicitVRLE = true;
    
    protected AuditLogger auditLogger;

    protected String dsJndiName = "java:/DefaultDS";

    private DataSource datasource;

    public final AuditLogger getAuditLogger() {
        return auditLogger;
    }

    public final ObjectName getDcmServerName() {
        return dcmServerName;
    }

    public final void setDcmServerName(ObjectName dcmServerName) {
        this.dcmServerName = dcmServerName;
    }

    public final String getDataSourceJndiName() {
        return dsJndiName;
    }

    public final void setDataSourceJndiName(String jndiName) {
        this.dsJndiName = jndiName;
    }

    public DataSource getDataSource() throws ConfigurationException {
        if (datasource == null) {
            try {
                Context jndiCtx = new InitialContext();
                try {
                    datasource = (DataSource) jndiCtx.lookup(dsJndiName);
                } finally {
                    try {
                        jndiCtx.close();
                    } catch (NamingException ignore) {
                    }
                }
            } catch (NamingException ne) {
                throw new ConfigurationException(
                        "Failed to access Data Source: " + dsJndiName, ne);
            }
        }
        return datasource;
    }

    public final String[] getCalledAETs() {
        return calledAETs;
    }
    
    public final void setCalledAETs(String[] calledAETs) {
        if (getState() == STARTED)
            updatePolicy(null);
        this.calledAETs = calledAETs;
        updatePolicy();
    }

    protected void updatePolicy() {
        if (getState() == STARTED)
            updatePolicy(makeAcceptorPolicy());
    }

    public final String[] getCallingAETs() {
        return callingAETs;
    }

    public final void setCallingAETs(String[] callingAETs) {
        this.callingAETs = callingAETs;
        updatePolicy();
    }
    
    public final boolean isAcceptExplicitVRLE() {
        return acceptExplicitVRLE;
    }

    public final void setAcceptExplicitVRLE(boolean acceptExplicitVRLE) {
        this.acceptExplicitVRLE = acceptExplicitVRLE;
        updatePolicy();
    }
    
    protected void startService() throws Exception {
        auditLogger = (AuditLogger) server.invoke(dcmServerName,
                "getAuditLogger", null, null);
        dcmHandler = (DcmHandler) server.invoke(dcmServerName, "getDcmHandler",
                null, null);
        bindDcmServices(dcmHandler.getDcmServiceRegistry());
        updatePolicy(makeAcceptorPolicy());
    }

    protected void updatePolicy(AcceptorPolicy policy) {
        for (int i = 0; i < calledAETs.length; ++i) {
            dcmHandler.getAcceptorPolicy().putPolicyForCalledAET(calledAETs[i],
                    policy);
        }
    }

    protected void stopService() throws Exception {
        updatePolicy(null);
        unbindDcmServices(dcmHandler.getDcmServiceRegistry());
        dcmHandler = null;
    }

    protected abstract void bindDcmServices(DcmServiceRegistry services);

    protected abstract void unbindDcmServices(DcmServiceRegistry services);

    protected abstract void initPresContexts(AcceptorPolicy policy);
    
    protected AcceptorPolicy makeAcceptorPolicy() {
        AcceptorPolicy policy = asf.newAcceptorPolicy();
        policy.setCallingAETs(isAcceptAnyCallingAET() ? null : callingAETs);
        initPresContexts(policy);
        return policy;        
    }

    protected String[] getTransferSyntaxUIDs() {
        return acceptExplicitVRLE ? NATIVE_LE_TS : ONLY_DEFAULT_TS;
    }
    
    protected static void addPresContexts(AcceptorPolicy policy, String[] asuids, String[] tsuids) {
        for (int i = 0; i < asuids.length; i++)
	        policy.putPresContext(asuids[i], tsuids);
    }        
    
    private final boolean isAcceptAnyCallingAET() {
        for (int i = 0; i < callingAETs.length; i++)
	        if (ANY.equalsIgnoreCase(callingAETs[i])) return true;
	    return callingAETs.length == 0;
    }
    
    public void logDataset(String prompt, Dataset ds) {
        if (!log.isDebugEnabled()) { return; }
        try {
            StringWriter w = new StringWriter();
            w.write(prompt);
            ds.dumpDataset(w, dumpParam);
            log.debug(w.toString());
        } catch (Exception e) {
            log.warn("Failed to dump dataset", e);
        }
    }

    public ServerSocketFactory getServerSocketFactory(String[] cipherSuites) {
        try {
            return (ServerSocketFactory) server.invoke(dcmServerName,
                    "getServerSocketFactory", new Object[] { cipherSuites},
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
            return (SocketFactory) server.invoke(dcmServerName,
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


    public Semaphore getCodecSemaphore() throws Exception {
        return (Semaphore) server.invoke(dcmServerName,
                "getCodecSemaphore", null, null);
    }
}
