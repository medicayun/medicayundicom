/* $Id: AbstractScpService.java 1110 2004-05-06 20:58:01Z gunterze $
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

package org.dcm4chex.service;

import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.service.util.ConfigurationException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1110 $ $Date: 2004-05-07 04:58:01 +0800 (周五, 07 5月 2004) $
 * @since 31.08.2003
 */
abstract class AbstractScpService extends ServiceMBeanSupport {

    private final static Map dumpParam = new HashMap(5);
    static {
        dumpParam.put("maxlen", new Integer(128));
        dumpParam.put("vallen", new Integer(64));
        dumpParam.put("prefix", "\t");
    }

    final static AssociationFactory asf = AssociationFactory.getInstance();

    protected ObjectName dcmServerName;

    protected DcmHandler dcmHandler;

    protected String[] aet;

    protected AuditLogger auditLogger;

    protected ObjectName getObjectName(MBeanServer server, ObjectName name)
            throws MalformedObjectNameException {
        aet = StringUtils.split(name.getKeyProperty("aet"), '\\');
        return name;
    }

    public final String getAET() {
        return aet[0];
    }

    public final AuditLogger getAuditLogger() {
        return auditLogger;
    }

    protected void startService() throws Exception {
        auditLogger = (AuditLogger) server.getAttribute(dcmServerName,
                "AuditLogger");
        dcmHandler = (DcmHandler) server.getAttribute(dcmServerName,
                "DcmHandler");
        bindDcmServices(dcmHandler.getDcmServiceRegistry());
        updatePolicy(makeAcceptorPolicy());
    }

    protected void updatePolicy(AcceptorPolicy policy) {
        for (int i = 0; i < aet.length; ++i) {
            dcmHandler.getAcceptorPolicy()
                    .putPolicyForCalledAET(aet[i], policy);
        }
    }

    protected void stopService() throws Exception {
        updatePolicy(null);
        unbindDcmServices(dcmHandler.getDcmServiceRegistry());
        dcmHandler = null;
    }

    protected abstract void bindDcmServices(DcmServiceRegistry services);

    protected abstract void unbindDcmServices(DcmServiceRegistry services);

    protected abstract AcceptorPolicy makeAcceptorPolicy();

    protected static void putPresContext(AcceptorPolicy policy, String asuid,
            String ts) {
        if (ts == null || ts.length() == 0) { return; }
        StringTokenizer stk = new StringTokenizer(ts, ", -");
        if (stk.hasMoreTokens()) {
            String[] tsuids = new String[stk.countTokens()];
            for (int i = 0; i < tsuids.length; i++) {
                tsuids[i] = UIDs.forName(stk.nextToken());
            }
            policy.putPresContext(asuid, tsuids);
        }
    }

    void logDataset(String prompt, Dataset ds) {
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

}
