/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4che.auditlog;

import java.net.UnknownHostException;

import org.dcm4che.data.Dataset;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      September 1, 2002
 * @version    $Revision: 3834 $ $Date: 2004-12-02 04:34:53 +0800 (周四, 02 12月 2004) $
 */
public interface AuditLogger {

    public final static String START = "Start";

    public final static String STOP = "Stop";

    public final static String SECURITY = "Security";

    public final static String NETWORKING = "Networking";

    public void setSyslogHost(String syslogHost) throws UnknownHostException;

    public String getSyslogHost();

    public void setSyslogPort(int syslogPort);

    public int getSyslogPort();

    public String getFacility();

    public void setFacility(String facility);

    public boolean isLogActorStartStop();

    public void setLogActorStartStop(boolean enable);

    public boolean isLogInstancesStored();

    public void setLogInstancesStored(boolean enable);

    public boolean isLogBeginStoringInstances();

    public void setLogBeginStoringInstances(boolean enable);

    public boolean isLogInstancesSent();

    public void setLogInstancesSent(boolean enable);

    public boolean isLogDicomQuery();

    public void setLogDicomQuery(boolean enable);

    public boolean isLogSecurityAlert();

    public void setLogSecurityAlert(boolean enable);

    public boolean isLogUserAuthenticated();

    public void setLogUserAuthenticated(boolean enable);

    public boolean isLogActorConfig();

    public void setLogActorConfig(boolean enable);

    public boolean isLogExport();

    public void setLogExport(boolean enable);

    public boolean isLogPatientRecord();

    public void setLogPatientRecord(boolean enable);

    public boolean isLogProcedureRecord();

    public void setLogProcedureRecord(boolean enable);

    public boolean isLogStudyDeleted();

    public void setLogStudyDeleted(boolean enable);

    public void logActorStartStop(String actorName, String action, User user);

    public void logInstancesStored(RemoteNode rNode, InstancesAction action);

    public void logBeginStoringInstances(RemoteNode rNode,
            InstancesAction action);

    public void logInstancesSent(RemoteNode rNode, InstancesAction action);

    public void logDicomQuery(Dataset keys, RemoteNode requestor, String cuid);

    public void logSecurityAlert(String alertType, User user,
            String description);

    public void logUserAuthenticated(String localUserName, String action);

    public void logActorConfig(String description, User user, String configType);

    public void logExport(MediaDescription media, User user);

    public void logPatientRecord(String action, Patient patient, User user, 
            String description);

    public void logProcedureRecord(String action, Patient patient,
            String placerOrderNumber, String fillerOrderNumber, String suid,
            String accessionNumber, User user, String description);

    public void logStudyDeleted(InstancesAction action, String description);

    public boolean isStrictIHEYr4();
    
    public void setStrictIHEYr4(boolean strictIHEYr4);
}