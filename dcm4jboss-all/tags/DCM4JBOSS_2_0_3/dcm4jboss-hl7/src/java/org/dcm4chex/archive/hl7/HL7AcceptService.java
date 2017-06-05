/* $Id: HL7AcceptService.java 1034 2004-03-09 22:54:38Z gunterze $
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
package org.dcm4chex.archive.hl7;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.dcm4chex.archive.ejb.interfaces.PatientUpdateHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;
import ca.uhn.hl7v2.app.AcceptApplication;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.DefaultApplication;
/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1034 $ $Date: 2004-03-10 06:54:38 +0800 (周三, 10 3月 2004) $
 * @since 08.03.2004
 * 
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class HL7AcceptService extends ServiceMBeanSupport
		implements
			org.dcm4chex.archive.hl7.HL7AcceptServiceMBean {
	static Application ACCEPT_HANDLER = new AcceptApplication();
	static Application REJECT_HANDLER = new DefaultApplication();
	protected ObjectName hl7ServerName;
	protected String messageType;
	protected String triggerEvent;
	/**
	 * @jmx.managed-attribute
	 */
	public String getEjbProviderURL() {
		return EJBHomeFactory.getEjbProviderURL();
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setEjbProviderURL(String ejbProviderURL) {
		EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
	}
	/**
	 * @jmx.managed-attribute
	 */
	public ObjectName getHL7ServerName() {
		return hl7ServerName;
	}
	/**
	 * @jmx.managed-attribute
	 */
	public void setHL7ServerName(ObjectName hl7ServerName) {
		this.hl7ServerName = hl7ServerName;
	}
	protected Application getApplication() {
		return ACCEPT_HANDLER;
	}
	protected ObjectName getObjectName(MBeanServer server, ObjectName name)
			throws MalformedObjectNameException {
		messageType = name.getKeyProperty("type");
		triggerEvent = name.getKeyProperty("event");
		return name;
	}
	private void registerApplication(Application handler) throws Exception {
		server.invoke(hl7ServerName, "registerApplication", new Object[]{
				messageType, triggerEvent, handler}, new String[]{
				String.class.getName(), String.class.getName(),
				Application.class.getName()});
	}
	protected void startService() throws Exception {
		registerApplication(getApplication());
	}
	protected void stopService() throws Exception {
		registerApplication(REJECT_HANDLER);
	}
	protected PatientUpdateHome getPatientUpdateHome()
			throws HomeFactoryException {
		return (PatientUpdateHome) EJBHomeFactory.getFactory().lookup(
				PatientUpdateHome.class, PatientUpdateHome.JNDI_NAME);
	}
}