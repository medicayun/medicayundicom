/* $Id: AuditLoggerService.java 1048 2004-03-14 22:43:43Z gunterze $
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
import java.security.Principal;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.AuditLoggerFactory;
import org.jboss.security.SecurityAssociation;
import org.jboss.system.ServiceMBeanSupport;
/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger </a>
 * @since August 21, 2002
 * @version $Revision: 1048 $ $Date: 2004-03-15 06:43:43 +0800 (周一, 15 3月 2004) $
 * 
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class AuditLoggerService extends ServiceMBeanSupport
		implements
			org.dcm4chex.service.AuditLoggerServiceMBean {
	private final static AuditLoggerFactory alf = AuditLoggerFactory.getInstance();
	private final AuditLogger logger = alf.newAuditLogger(Logger
			.getLogger(getClass().getName()));
	private String actorName;

	/**
	 * @jmx.managed-attribute
	 */
	public AuditLogger getAuditLogger() {
		return logger;
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setSyslogHost(String newSyslogHost) throws Exception {
		logActorConfig(actorName, "SyslogHost", getSyslogHost(), newSyslogHost,
				AuditLogger.SECURITY);
		logger.setSyslogHost(newSyslogHost);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public String getSyslogHost() {
		return logger.getSyslogHost();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setSyslogPort(int newSyslogPort) {
		logActorConfig(actorName, "SyslogPort", new Integer(getSyslogPort()),
				new Integer(newSyslogPort), AuditLogger.SECURITY);
		logger.setSyslogPort(newSyslogPort);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public int getSyslogPort() {
		return logger.getSyslogPort();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public String getFacility() {
		return logger.getFacility();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setFacility(String newFacility) {
		String oldFacility = getFacility();
		logger.setFacility(newFacility);
		logActorConfig(actorName, "Facility", oldFacility, newFacility,
				AuditLogger.SECURITY);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public boolean isLogActorConfig() {
		return logger.isLogActorConfig();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public boolean isLogActorStartStop() {
		return logger.isLogActorStartStop();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public boolean isLogBeginStoringInstances() {
		return logger.isLogBeginStoringInstances();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public boolean isLogDicomQuery() {
		return logger.isLogDicomQuery();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public boolean isLogInstancesSent() {
		return logger.isLogInstancesSent();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public boolean isLogInstancesStored() {
		return logger.isLogInstancesStored();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public boolean isLogSecurityAlert() {
		return logger.isLogSecurityAlert();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public boolean isLogUserAuthenticated() {
		return logger.isLogUserAuthenticated();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public boolean isLogExport() {
		return logger.isLogExport();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setLogActorConfig(boolean enable) {
		logActorConfig(actorName, "LogActorConfig", new Boolean(
				isLogActorConfig()), new Boolean(enable), AuditLogger.SECURITY);
		logger.setLogActorConfig(enable);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setLogActorStartStop(boolean enable) {
		logActorConfig(actorName, "LogActorStartStop", new Boolean(
				isLogActorStartStop()), new Boolean(enable),
				AuditLogger.SECURITY);
		logger.setLogActorStartStop(enable);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setLogBeginStoringInstances(boolean enable) {
		logActorConfig(actorName, "LogBeginStoringInstances", new Boolean(
				isLogBeginStoringInstances()), new Boolean(enable),
				AuditLogger.SECURITY);
		logger.setLogBeginStoringInstances(enable);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setLogDicomQuery(boolean enable) {
		logActorConfig(actorName, "LogDicomQuery", new Boolean(
				isLogDicomQuery()), new Boolean(enable), AuditLogger.SECURITY);
		logger.setLogDicomQuery(enable);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setLogInstancesSent(boolean enable) {
		logActorConfig(actorName, "LogInstancesSent", new Boolean(
				isLogInstancesSent()), new Boolean(enable),
				AuditLogger.SECURITY);
		logger.setLogInstancesSent(enable);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setLogInstancesStored(boolean enable) {
		logActorConfig(actorName, "LogInstancesStored", new Boolean(
				isLogInstancesStored()), new Boolean(enable),
				AuditLogger.SECURITY);
		logger.setLogInstancesStored(enable);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setLogSecurityAlert(boolean enable) {
		logActorConfig(actorName, "LogSecurityAlert", new Boolean(
				isLogSecurityAlert()), new Boolean(enable),
				AuditLogger.SECURITY);
		logger.setLogSecurityAlert(enable);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setLogUserAuthenticated(boolean enable) {
		logActorConfig(actorName, "LogUserAuthenticated", new Boolean(
				isLogUserAuthenticated()), new Boolean(enable),
				AuditLogger.SECURITY);
		logger.setLogUserAuthenticated(enable);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setLogExport(boolean enable) {
		logActorConfig(actorName, "LogExport", new Boolean(isLogExport()),
				new Boolean(enable), AuditLogger.SECURITY);
		logger.setLogExport(enable);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public String getCurrentPrincipalName() {
		Principal p = SecurityAssociation.getPrincipal();
		return p != null ? p.getName() : System.getProperty("user.name");
	}
	/**
	 * @jmx.managed-operation
	 */
	public void logActorConfig(String actorName, String propName,
			Object oldVal, Object newVal, String type) {
		if (!newVal.equals(oldVal)) {
			logActorConfig(actorName + ": " + propName + " changed from "
					+ oldVal + " to " + newVal, type);
		}
	}
	/**
	 * @jmx.managed-operation
	 */
	public void logActorConfig(String desc, String type) {
		if (getState() == STARTED) {
			logger.logActorConfig(desc, alf
					.newLocalUser(getCurrentPrincipalName()), type);
		}
	}

	private void logActorStartStop(String action) {
	    logger.logActorStartStop(actorName, action, alf
					.newLocalUser(getCurrentPrincipalName()));
	}
	
	protected ObjectName getObjectName(MBeanServer server, ObjectName name) {
		actorName = name.getKeyProperty("actorName");
		return name;
	}
	
    protected void startService() throws Exception {
        logActorStartStop(AuditLogger.START);
        super.startService();
    }

    protected void stopService() throws Exception {
        super.stopService();
        logActorStartStop(AuditLogger.STOP);
    }
}
