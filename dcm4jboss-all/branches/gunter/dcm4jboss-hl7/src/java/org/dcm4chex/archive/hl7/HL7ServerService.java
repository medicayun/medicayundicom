/* $Id: HL7ServerService.java 1010 2004-02-24 17:24:09Z gunterze $
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

import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1010 $ $Date: 2004-02-25 01:24:09 +0800 (周三, 25 2月 2004) $
 * @since 24.02.2004
 * 
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */
public class HL7ServerService
    extends ServiceMBeanSupport
    implements org.dcm4chex.archive.hl7.HL7ServerServiceMBean {

    private HL7Server server = new HL7Server(log);

    protected void startService() throws Exception {
        server.start();
    }

    protected void stopService() throws Exception {
        server.stop();
    }

    /**
     * @jmx.managed-attribute
     */
    public int getPort() {
        return server.getPort();
    }

    /**
     * @jmx.managed-attribute
     */
    public void setPort(int port) {
        server.setPort(port);
    }

}
