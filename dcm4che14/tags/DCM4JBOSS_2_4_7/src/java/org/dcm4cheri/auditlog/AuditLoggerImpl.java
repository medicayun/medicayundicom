/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4cheri.auditlog;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.MediaDescription;
import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.auditlog.User;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.HostNameUtils;
import org.dcm4che.util.SyslogWriter;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      August 22, 2002
 * @version    $Revision: 3834 $ $Date: 2004-12-02 04:34:53 +0800 (周四, 02 12月 2004) $
 */
class AuditLoggerImpl implements AuditLogger {

    // Constants -----------------------------------------------------
    private final Logger log;

    // Variables -----------------------------------------------------
    private final SyslogWriter writer = new SyslogWriter();

    private boolean actorStartStop = true;

    private boolean instancesStored = true;

    private boolean beginStoringInstances = true;

    private boolean instancesSent = true;

    private boolean dicomQuery = true;

    private boolean securityAlert = true;

    private boolean userAuthenticated = true;

    private boolean actorConfig = true;

    private boolean logExport = true;

    private boolean logStudyDeleted = true;

    private boolean logPatientRecord = true;

    private boolean logProcedureRecord = true;
    
    private boolean strictIHEYr4 = false;

    // Constructors --------------------------------------------------
    AuditLoggerImpl(Logger log) {
        this.log = log;
    }

    // Methods -------------------------------------------------------
    public void setSyslogHost(String syslogHost) throws UnknownHostException {
        writer.setSyslogHost(syslogHost);
    }

    public String getSyslogHost() {
        return writer.getSyslogHost();
    }

    public void setSyslogPort(int syslogPort) {
        writer.setSyslogPort(syslogPort);
    }

    public int getSyslogPort() {
        return writer.getSyslogPort();
    }

    public String getFacility() {
        return writer.getFacilityAsString();
    }

    public void setFacility(String facility) {
        writer.setFacility(facility);
    }

    public boolean isLogActorStartStop() {
        return actorStartStop;
    }

    public void setLogActorStartStop(boolean actorStartStop) {
        this.actorStartStop = actorStartStop;
    }

    public boolean isLogInstancesStored() {
        return instancesStored;
    }

    public void setLogInstancesStored(boolean instancesStored) {
        this.instancesStored = instancesStored;
    }

    public boolean isLogBeginStoringInstances() {
        return beginStoringInstances;
    }

    public void setLogBeginStoringInstances(boolean beginStoringInstances) {
        this.beginStoringInstances = beginStoringInstances;
    }

    public boolean isLogInstancesSent() {
        return instancesSent;
    }

    public void setLogInstancesSent(boolean instancesSent) {
        this.instancesSent = instancesSent;
    }

    public boolean isLogDicomQuery() {
        return dicomQuery;
    }

    public void setLogDicomQuery(boolean dicomQuery) {
        this.dicomQuery = dicomQuery;
    }

    public boolean isLogSecurityAlert() {
        return securityAlert;
    }

    public void setLogSecurityAlert(boolean securityAlert) {
        this.securityAlert = securityAlert;
    }

    public boolean isLogUserAuthenticated() {
        return userAuthenticated;
    }

    public void setLogUserAuthenticated(boolean userAuthenticated) {
        this.userAuthenticated = userAuthenticated;
    }

    public boolean isLogActorConfig() {
        return actorConfig;
    }

    public void setLogActorConfig(boolean actorConfig) {
        this.actorConfig = actorConfig;
    }

    public boolean isLogExport() {
        return logExport;
    }

    public void setLogExport(boolean logExport) {
        this.logExport = logExport;
    }

    public final boolean isLogPatientRecord() {
        return logPatientRecord;
    }

    public final void setLogPatientRecord(boolean logPatientRecord) {
        this.logPatientRecord = logPatientRecord;
    }

    public final boolean isLogProcedureRecord() {
        return logProcedureRecord;
    }

    public final void setLogProcedureRecord(boolean logProcedureRecord) {
        this.logProcedureRecord = logProcedureRecord;
    }

    public final boolean isLogStudyDeleted() {
        return logStudyDeleted;
    }

    public final void setLogStudyDeleted(boolean logStudyDeleted) {
        this.logStudyDeleted = logStudyDeleted;
    }

    public final boolean isStrictIHEYr4() {
        return strictIHEYr4;
    }
    
    public final void setStrictIHEYr4(boolean strictIHEYr4) {
        this.strictIHEYr4 = strictIHEYr4;
    }
    
    public void logActorStartStop(String actorName, String action, User user) {
        if (!actorStartStop) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newActorStartStop(actorName,
                            action,
                            user,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logInstancesStored(RemoteNode rnode,
            InstancesAction instanceActionDescription) {
        if (!instancesStored) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newInstancesStored(rnode,
                            instanceActionDescription,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logBeginStoringInstances(RemoteNode rnode,
            InstancesAction instanceActionDescription) {
        if (!beginStoringInstances) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newBeginStoringInstances(rnode,
                            instanceActionDescription,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logInstancesSent(RemoteNode rnode,
            InstancesAction instanceActionDescription) {
        if (!instancesSent) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4.newInstancesSent(rnode,
                    instanceActionDescription,
                    HostNameUtils.getLocalHostName(),
                    millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logDicomQuery(Dataset keys, RemoteNode requestor, String cuid) {
        if (!dicomQuery) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4.newDicomQuery(keys,
                    requestor,
                    cuid,
                    HostNameUtils.getLocalHostName(),
                    millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logSecurityAlert(String alertType, User user, String description) {
        if (!securityAlert) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newSecurityAlert(alertType,
                            user,
                            description,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logUserAuthenticated(String localUserName, String action) {
        if (!userAuthenticated) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newUserAuthenticated(localUserName,
                            action,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logActorConfig(String description, User user, String configType) {
        if (!actorConfig) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newActorConfig(description,
                            user,
                            configType,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logExport(MediaDescription media, User user) {
        if (!logExport) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4.newExport(media,
                    user,
                    HostNameUtils.getLocalHostName(),
                    millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logPatientRecord(String action, Patient patient, User user,
            String desc) {
        if (!logPatientRecord) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4.newPatientRecord(action,
                    patient,
                    user,
                    strictIHEYr4 ? null : desc,
                    HostNameUtils.getLocalHostName(),
                    millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logProcedureRecord(String action, Patient patient,
            String placerOrderNumber, String fillerOrderNumber, String suid,
            String accessionNumber, User user, String desc) {
        if (!logProcedureRecord) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4
                    .newProcedureRecord(action,
                            placerOrderNumber,
                            fillerOrderNumber,
                            suid,
                            accessionNumber,
                            patient,
                            user,
                            strictIHEYr4 ? null : desc,
                            HostNameUtils.getLocalHostName(),
                            millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logStudyDeleted(InstancesAction action, String desc) {
        if (!logStudyDeleted) { return; }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO, IHEYr4.newStudyDeleted(action,
                    strictIHEYr4 ? null : desc,
                    HostNameUtils.getLocalHostName(),
                    millis).toString(), millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }
}

