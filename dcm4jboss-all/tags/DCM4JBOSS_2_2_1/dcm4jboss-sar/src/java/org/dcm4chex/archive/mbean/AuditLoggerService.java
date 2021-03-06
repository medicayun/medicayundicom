/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.net.Socket;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.auditlog.User;
import org.dcm4che.data.Dataset;
import org.dcm4cheri.util.StringUtils;
import org.jboss.security.SecurityAssociation;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger </a>
 * @since August 21, 2002
 * @version $Revision: 1292 $ $Date: 2004-12-14 00:56:26 +0800 (周二, 14 12月 2004) $
 */
public class AuditLoggerService extends ServiceMBeanSupport  {

    private final static AuditLoggerFactory alf = AuditLoggerFactory
            .getInstance();

    private final AuditLogger logger = alf.newAuditLogger(Logger
            .getLogger(getClass().getName()));

    private String actorName;
    
    private ArrayList supressLogForAETs = new ArrayList();

    public final String getActorName() {
        return actorName;
    }

    public final void setActorName(String actorName) {
        this.actorName = actorName.trim();
    }

    public String getSupressLogForAETs() {
        if (supressLogForAETs.isEmpty()) return "NONE";
        StringBuffer sb = new StringBuffer((String) supressLogForAETs.get(0));
        for (int i = 1, n = supressLogForAETs.size(); i < n; i++) {
            sb.append('\\').append((String) supressLogForAETs.get(i));
        }
        return sb.toString();
    }

    public final void setSupressLogForAETs(String aets) {
        String trimed = aets.trim();
        supressLogForAETs.clear();
        if (trimed.length() > 0 && !trimed.equalsIgnoreCase("NONE")) {
            supressLogForAETs.addAll(Arrays.asList(StringUtils.split(trimed, '\\')));
        }
    }

    public AuditLogger getAuditLogger() {
        return logger;
    }

    public void setSyslogHost(String newSyslogHost) throws Exception {
        String trimed = newSyslogHost.trim();
        logActorConfig(actorName,
                "SyslogHost",
                getSyslogHost(),
                trimed,
                AuditLogger.SECURITY);
        logger.setSyslogHost(trimed);
    }

    public boolean isLogPatientRecord() {
        return logger.isLogPatientRecord();
    }

    public boolean isLogProcedureRecord() {
        return logger.isLogProcedureRecord();
    }

    public boolean isLogStudyDeleted() {
        return logger.isLogStudyDeleted();
    }

    public void setLogPatientRecord(boolean enable) {
        logActorConfig(actorName,
                "LogPatientRecord",
                new Boolean(isLogPatientRecord()),
                new Boolean(enable),
                AuditLogger.SECURITY);
        logger.setLogPatientRecord(enable);
    }

    public void setLogProcedureRecord(boolean enable) {
        logActorConfig(actorName,
                "LogProcedureRecord",
                new Boolean(isLogProcedureRecord()),
                new Boolean(enable),
                AuditLogger.SECURITY);
        logger.setLogProcedureRecord(enable);
    }

    public void setLogStudyDeleted(boolean enable) {
        logActorConfig(actorName, "LogStudyDeleted", new Boolean(
                isLogStudyDeleted()), new Boolean(enable), AuditLogger.SECURITY);
        logger.setLogStudyDeleted(enable);
    }

    public String getSyslogHost() {
        return logger.getSyslogHost();
    }

    public void setSyslogPort(int newSyslogPort) {
        logActorConfig(actorName,
                "SyslogPort",
                new Integer(getSyslogPort()),
                new Integer(newSyslogPort),
                AuditLogger.SECURITY);
        logger.setSyslogPort(newSyslogPort);
    }

    public int getSyslogPort() {
        return logger.getSyslogPort();
    }

    public String getFacility() {
        return logger.getFacility();
    }

    public void setFacility(String newFacility) {
        String trimed = newFacility.trim();
        String oldFacility = getFacility();
        logger.setFacility(trimed);
        logActorConfig(actorName,
                "Facility",
                oldFacility,
                trimed,
                AuditLogger.SECURITY);
    }

    public boolean isStrictIHEYr4() {
        return logger.isStrictIHEYr4();
    }
    
    public void setStrictIHEYr4(boolean enable) {
        logActorConfig(actorName, "StrictIHEYr4", new Boolean(
                isStrictIHEYr4()), new Boolean(enable), AuditLogger.SECURITY);
        logger.setStrictIHEYr4(enable);
    }
        
    public boolean isLogActorConfig() {
        return logger.isLogActorConfig();
    }

    public boolean isLogActorStartStop() {
        return logger.isLogActorStartStop();
    }

    public boolean isLogBeginStoringInstances() {
        return logger.isLogBeginStoringInstances();
    }

    public boolean isLogDicomQuery() {
        return logger.isLogDicomQuery();
    }

    public boolean isLogInstancesSent() {
        return logger.isLogInstancesSent();
    }

    public boolean isLogInstancesStored() {
        return logger.isLogInstancesStored();
    }

    public boolean isLogSecurityAlert() {
        return logger.isLogSecurityAlert();
    }

    public boolean isLogUserAuthenticated() {
        return logger.isLogUserAuthenticated();
    }

    public boolean isLogExport() {
        return logger.isLogExport();
    }

    public void setLogActorConfig(boolean enable) {
        logActorConfig(actorName, "LogActorConfig", new Boolean(
                isLogActorConfig()), new Boolean(enable), AuditLogger.SECURITY);
        logger.setLogActorConfig(enable);
    }

    public void setLogActorStartStop(boolean enable) {
        logActorConfig(actorName,
                "LogActorStartStop",
                new Boolean(isLogActorStartStop()),
                new Boolean(enable),
                AuditLogger.SECURITY);
        logger.setLogActorStartStop(enable);
    }

    public void setLogBeginStoringInstances(boolean enable) {
        logActorConfig(actorName,
                "LogBeginStoringInstances",
                new Boolean(isLogBeginStoringInstances()),
                new Boolean(enable),
                AuditLogger.SECURITY);
        logger.setLogBeginStoringInstances(enable);
    }

    public void setLogDicomQuery(boolean enable) {
        logActorConfig(actorName, "LogDicomQuery", new Boolean(
                isLogDicomQuery()), new Boolean(enable), AuditLogger.SECURITY);
        logger.setLogDicomQuery(enable);
    }

    public void setLogInstancesSent(boolean enable) {
        logActorConfig(actorName,
                "LogInstancesSent",
                new Boolean(isLogInstancesSent()),
                new Boolean(enable),
                AuditLogger.SECURITY);
        logger.setLogInstancesSent(enable);
    }

    public void setLogInstancesStored(boolean enable) {
        logActorConfig(actorName,
                "LogInstancesStored",
                new Boolean(isLogInstancesStored()),
                new Boolean(enable),
                AuditLogger.SECURITY);
        logger.setLogInstancesStored(enable);
    }

    public void setLogSecurityAlert(boolean enable) {
        logActorConfig(actorName,
                "LogSecurityAlert",
                new Boolean(isLogSecurityAlert()),
                new Boolean(enable),
                AuditLogger.SECURITY);
        logger.setLogSecurityAlert(enable);
    }

    public void setLogUserAuthenticated(boolean enable) {
        logActorConfig(actorName,
                "LogUserAuthenticated",
                new Boolean(isLogUserAuthenticated()),
                new Boolean(enable),
                AuditLogger.SECURITY);
        logger.setLogUserAuthenticated(enable);
    }

    public void setLogExport(boolean enable) {
        logActorConfig(actorName,
                "LogExport",
                new Boolean(isLogExport()),
                new Boolean(enable),
                AuditLogger.SECURITY);
        logger.setLogExport(enable);
    }

    public void logActorConfig(String actorName, String propName,
            Object oldVal, Object newVal, String type) {
        if (!newVal.equals(oldVal)) {
            logActorConfig(actorName + ": " + propName + " changed from "
                    + oldVal + " to " + newVal, type);
        }
    }

    public void logActorConfig(String desc, String type) {
        if (getState() == STARTED) {
            logger.logActorConfig(desc, getCurrentUser(), type);
        }
    }

    private User getCurrentUser() {
        Principal p = SecurityAssociation.getPrincipal();
        return alf.newLocalUser(p != null ? p.getName() : System
                .getProperty("user.name"));
    }

    public void logStudyDeleted(String patid, String patname, String suid,
            Integer numInsts, String desc) {
        if (getState() == STARTED) {
            Patient patient = alf.newPatient(patid, patname);
            InstancesAction action = alf.newInstancesAction("Delete",
                    suid,
                    patient);
            if (numInsts != null)
                    action.setNumberOfInstances(numInsts.intValue());
            action.setUser(getCurrentUser());
            logger.logStudyDeleted(action, desc);
        }
    }

    public void logPatientRecord(String action, String patid, String patname,
            String desc) {
        if (getState() == STARTED) {
            Patient patient = alf.newPatient(patid, patname);
            logger.logPatientRecord(action, patient, getCurrentUser(), desc);
        }
    }

    public void logProcedureRecord(String action, String patid, String patname,
            String placerOrderNo, String fillerOrderNo, String suid,
            String accNo, String desc) {
        if (getState() == STARTED) {
            Patient patient = alf.newPatient(patid, patname);
            logger.logProcedureRecord(action,
                    patient,
                    placerOrderNo,
                    fillerOrderNo,
                    suid,
                    accNo,
                    getCurrentUser(),
                    desc);
        }
    }
    
    public void logInstancesStored(RemoteNode node, InstancesAction action) {
        if (getState() == STARTED 
                && !supressLogForAETs.contains(node.getAET())) {
            logger.logInstancesStored(node, action);
        }        
    }
    
    public void logInstancesSent(RemoteNode node, InstancesAction action) {
        if (getState() == STARTED 
                && !supressLogForAETs.contains(node.getAET())) {
            logger.logInstancesSent(node, action);
        }        
    }

    public void logDicomQuery(Dataset keys, RemoteNode node, String cuid) {        
        if (getState() == STARTED 
                && !supressLogForAETs.contains(node.getAET())) {
            logger.logDicomQuery(keys, node, cuid);
        }        
    }
    
    public void logSecurityAlert(String alertType, Socket socket, String aet,
            String description) {
        if (getState() == STARTED && !supressLogForAETs.contains(aet)) {
            logger.logSecurityAlert(alertType, alf.newRemoteUser(alf
                    .newRemoteNode(socket, aet)), description);
        }
    }
    
    private void logActorStartStop(String action) {
        logger.logActorStartStop(actorName, action, getCurrentUser());
    }

    protected void startService() throws Exception {
        logActorStartStop(AuditLogger.START);
    }

    protected void stopService() throws Exception {
        logActorStartStop(AuditLogger.STOP);
    }
}