/* $Id: QueryRetrieveScpService.java 964 2004-02-11 20:15:27Z gunterze $
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
 * @version $Revision: 964 $ $Date: 2004-02-12 04:15:27 +0800 (周四, 12 2月 2004) $
 * @since 31.08.2003
 */
public class QueryRetrieveScpService
    extends AbstractScpService
    implements org.dcm4chex.service.QueryRetrieveScpServiceMBean
{

    private DataSourceFactory dsf = new DataSourceFactory(log);
    private FindScp findScp = new FindScp(this);
    private MoveScp moveScp = new MoveScp(this);
    private String[] callingAETs;
    private String patientRootFind;
    private String studyRootFind;
    private String patientStudyOnlyFind;
    private String patientRootMove;
    private String studyRootMove;
    private String patientStudyOnlyMove;

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
            updatePolicy();
        }
    }

    /**
     * @jmx.managed-attribute
     */
    public final String getPatientRootFind()
    {
        return patientRootFind;
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setPatientRootFind(String patientRootFind)
    {
        this.patientRootFind = patientRootFind;
        if (getState() == STARTED)
        {
            updatePolicy();
        }
    }

    /**
     * @jmx.managed-attribute
     */
    public final String getPatientRootMove()
    {
        return patientRootMove;
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setPatientRootMove(String patientRootMove)
    {
        this.patientRootMove = patientRootMove;
        if (getState() == STARTED)
        {
            updatePolicy();
        }
    }

    /**
     * @jmx.managed-attribute
     */
    public final String getPatientStudyOnlyFind()
    {
        return patientStudyOnlyFind;
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setPatientStudyOnlyFind(String patientStudyOnlyFind)
    {
        this.patientStudyOnlyFind = patientStudyOnlyFind;
        if (getState() == STARTED)
        {
            updatePolicy();
        }
    }

    /**
     * @jmx.managed-attribute
     */
    public final String getPatientStudyOnlyMove()
    {
        return patientStudyOnlyMove;
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setPatientStudyOnlyMove(String patientStudyOnlyMove)
    {
        this.patientStudyOnlyMove = patientStudyOnlyMove;
        if (getState() == STARTED)
        {
            updatePolicy();
        }
    }

    /**
     * @jmx.managed-attribute
     */
    public final String getStudyRootFind()
    {
        return studyRootFind;
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setStudyRootFind(String studyRootFind)
    {
        this.studyRootFind = studyRootFind;
        if (getState() == STARTED)
        {
            updatePolicy();
        }
    }

    /**
     * @jmx.managed-attribute
     */
    public final String getStudyRootMove()
    {
        return studyRootMove;
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setStudyRootMove(String studyRootMove)
    {
        this.studyRootMove = studyRootMove;
        if (getState() == STARTED)
        {
            updatePolicy();
        }
    }

    /**
     * @jmx.managed-attribute
     */
    public final boolean isSendPendingMoveRSP() {
        return moveScp.isSendPendingMoveRSP();
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setSendPendingMoveRSP(boolean sendPendingMoveRSP) {
        moveScp.setSendPendingMoveRSP(sendPendingMoveRSP);
    }

    /**
     * @jmx.managed-attribute
     */
    public final int getAcTimeout() {
        return moveScp.getAcTimeout();
    }

    /**
     * @jmx.managed-attribute
     */
    public final void setAcTimeout(int acTimeout) {
        moveScp.setAcTimeout(acTimeout);
    }

    protected void bindDcmServices(DcmServiceRegistry services)
    {
        services.bind(
            UIDs.PatientRootQueryRetrieveInformationModelFIND,
            findScp);
        services.bind(UIDs.StudyRootQueryRetrieveInformationModelFIND, findScp);
        services.bind(
            UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND,
            findScp);

        services.bind(
            UIDs.PatientRootQueryRetrieveInformationModelMOVE,
            moveScp);
        services.bind(UIDs.StudyRootQueryRetrieveInformationModelMOVE, moveScp);
        services.bind(
            UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE,
            moveScp);
    }

    protected void unbindDcmServices(DcmServiceRegistry services)
    {
        services.unbind(UIDs.PatientRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.StudyRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND);

        services.unbind(UIDs.PatientRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.StudyRootQueryRetrieveInformationModelFIND);
        services.unbind(UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND);
    }

    protected AcceptorPolicy getAcceptorPolicy()
    {
        AcceptorPolicy policy = asf.newAcceptorPolicy();
        policy.setCallingAETs(callingAETs);
        putPresContext(
            policy,
            UIDs.PatientRootQueryRetrieveInformationModelFIND,
            patientRootFind);
        putPresContext(
            policy,
            UIDs.StudyRootQueryRetrieveInformationModelFIND,
            studyRootFind);
        putPresContext(
            policy,
            UIDs.PatientStudyOnlyQueryRetrieveInformationModelFIND,
            patientStudyOnlyFind);
        putPresContext(
            policy,
            UIDs.PatientRootQueryRetrieveInformationModelMOVE,
            patientRootMove);
        putPresContext(
            policy,
            UIDs.StudyRootQueryRetrieveInformationModelMOVE,
            studyRootMove);
        putPresContext(
            policy,
            UIDs.PatientStudyOnlyQueryRetrieveInformationModelMOVE,
            patientStudyOnlyMove);
        return policy;
    }
}
