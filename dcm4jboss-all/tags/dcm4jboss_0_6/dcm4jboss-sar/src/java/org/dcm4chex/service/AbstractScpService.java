/* $Id: AbstractScpService.java 1025 2004-03-02 23:55:32Z gunterze $
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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1025 $ $Date: 2004-03-03 07:55:32 +0800 (周三, 03 3月 2004) $
 * @since 31.08.2003
 */
abstract class AbstractScpService extends ServiceMBeanSupport
{
    private final static Map dumpParam = new HashMap(5);
    static {
        dumpParam.put("maxlen", new Integer(128));
        dumpParam.put("vallen", new Integer(64));
        dumpParam.put("prefix", "\t");
    }
    final static AssociationFactory asf =
        AssociationFactory.getInstance();

    protected ObjectName dcmServerName;
    protected DcmHandler dcmHandler;
    protected String aet;

    protected ObjectName getObjectName(MBeanServer server, ObjectName name)
        throws MalformedObjectNameException
    {
        aet = name.getKeyProperty("aet");
        return name;
    }
    
    public final String getAET() {
        return aet;
    }

    protected void startService() throws Exception
    {
        dcmHandler =
            (DcmHandler) server.getAttribute(dcmServerName, "DcmHandler");
        bindDcmServices(dcmHandler.getDcmServiceRegistry());
        updatePolicy();
    }

    protected void updatePolicy()
    {
        dcmHandler.getAcceptorPolicy().putPolicyForCalledAET(
            aet,
            getAcceptorPolicy());
    }

    protected void stopService() throws Exception
    {
        dcmHandler.getAcceptorPolicy().putPolicyForCalledAET(aet, null);
        unbindDcmServices(dcmHandler.getDcmServiceRegistry());
        dcmHandler = null;
    }

    protected abstract void bindDcmServices(DcmServiceRegistry services);

    protected abstract void unbindDcmServices(DcmServiceRegistry services);

    protected abstract AcceptorPolicy getAcceptorPolicy();

    protected static void putPresContext(
        AcceptorPolicy policy,
        String asuid,
        String ts)
    {
        if (ts == null || ts.length() == 0) {
            return;
        }
        StringTokenizer stk = new StringTokenizer(ts, ", -");
        if (stk.hasMoreTokens())
        {
            String[] tsuids = new String[stk.countTokens()];
            for (int i = 0; i < tsuids.length; i++)
            {
                tsuids[i] = UIDs.forName(stk.nextToken());
            }
            policy.putPresContext(asuid, tsuids);
        }
    }

    void logDataset(String prompt, Dataset ds)
    {
        if (!log.isDebugEnabled()) {
            return;
        }
        try {
            StringWriter w = new StringWriter();
            w.write(prompt);
            ds.dumpDataset(w, dumpParam);
            log.debug(w.toString());
        } catch (Exception e) {
            log.warn("Failed to dump dataset", e);
        }
    }
}
