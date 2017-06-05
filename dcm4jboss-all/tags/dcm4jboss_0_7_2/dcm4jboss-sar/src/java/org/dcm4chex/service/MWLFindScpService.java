/* $Id: MWLFindScpService.java 1111 2004-05-07 09:16:12Z gunterze $
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

import java.beans.PropertyEditor;

import javax.management.ObjectName;
import javax.sql.DataSource;

import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4chex.service.util.AETsEditor;
import org.dcm4chex.service.util.ConfigurationException;

/**
 * @jmx.mbean
 *  extends="org.jboss.system.ServiceMBean"
 * 
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1111 $ $Date: 2004-05-07 17:16:12 +0800 (周五, 07 5月 2004) $
 * @since 31.08.2003
 */
public class MWLFindScpService
    extends AbstractScpService
    implements org.dcm4chex.service.MWLFindScpServiceMBean
{

    private DataSourceFactory dsf = new DataSourceFactory(log);
    private MWLFindScp mwlFindScp = new MWLFindScp(this);
    private String[] callingAETs;
    private String tsUIDs;

    /**
      * @jmx.managed-attribute
      */
    public ObjectName getDcmServerName()
    {
        return dcmServerName;
    }

    /**
     * @jmx.managed-attribute
     */
    public void setDcmServerName(ObjectName dcmServerName)
    {
        this.dcmServerName = dcmServerName;
    }

    DataSource getDS() throws ConfigurationException
    {
        return dsf.getDataSource();
    }

    /**
     * @jmx.managed-attribute
     */
    public String getDataSource()
    {
        return dsf.getJNDIName();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setDataSource(String jndiName)
    {
        dsf.setJNDIName(jndiName);
    }

    /**
     * @jmx.managed-attribute
     */
    public String getCallingAETs()
    {
        PropertyEditor pe = new AETsEditor();
        pe.setValue(callingAETs);
        return pe.getAsText();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setCallingAETs(String newCallingAETs)
    {
        PropertyEditor pe = new AETsEditor();
        pe.setAsText(newCallingAETs);
        callingAETs = (String[]) pe.getValue();
        if (getState() == STARTED)
        {
            updatePolicy(makeAcceptorPolicy());
        }
    }

    /**
     * @jmx.managed-attribute
     */
    public final String getTransferSyntaxes()
    {
        return tsUIDs;
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setTransferSyntaxes(String tsUIDs)
    {
        this.tsUIDs = tsUIDs;
        if (getState() == STARTED)
        {
            updatePolicy(makeAcceptorPolicy());
        }
    }
    
    protected void bindDcmServices(DcmServiceRegistry services)
    {
        services.bind(
            UIDs.ModalityWorklistInformationModelFIND,
            mwlFindScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services)
    {
        services.unbind(UIDs.ModalityWorklistInformationModelFIND);
    }

    protected AcceptorPolicy makeAcceptorPolicy()
    {
        AcceptorPolicy policy = asf.newAcceptorPolicy();
        policy.setCallingAETs(callingAETs);
        putPresContext(
            policy,
            UIDs.ModalityWorklistInformationModelFIND,
            tsUIDs);
        return policy;
    }
}
