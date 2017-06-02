/*                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG                             *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package org.dcm4cheri.auditlog;

import java.net.Socket;

import org.apache.log4j.Logger;
import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.Destination;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.MediaDescription;
import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.auditlog.User;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      August 27, 2002
 * @version    $Revision: 3733 $ $Date: 2003-11-07 18:54:18 +0800 (周五, 07 11月 2003) $
 */

public class AuditLoggerFactoryImpl extends AuditLoggerFactory
{

    // Public --------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public AuditLogger newAuditLogger()
    {
        return new AuditLoggerImpl(Logger.getLogger(AuditLogger.class));
    }


    /**
     *  Description of the Method
     *
     * @param  log  Description of the Parameter
     * @return      Description of the Return Value
     */
    public AuditLogger newAuditLogger(Logger log)
    {
        return new AuditLoggerImpl(log);
    }


    /**
     *  Description of the Method
     *
     * @param  id    Description of the Parameter
     * @param  name  Description of the Parameter
     * @return       Description of the Return Value
     */
    public Patient newPatient(String id, String name)
    {
        return new PatientImpl(id, name);
    }


    /**
     *  Description of the Method
     *
     * @param  ip     Description of the Parameter
     * @param  hname  Description of the Parameter
     * @param  aet    Description of the Parameter
     * @return        Description of the Return Value
     */
    public RemoteNode newRemoteNode(String ip, String hname, String aet)
    {
        return new RemoteNodeImpl(ip, hname, aet);
    }


    /**
     *  Description of the Method
     *
     * @param  socket  Description of the Parameter
     * @param  aet     Description of the Parameter
     * @return         Description of the Return Value
     */
    public RemoteNode newRemoteNode(Socket socket, String aet)
    {
        return new RemoteNodeImpl(socket, aet);
    }


    /**
     *  Description of the Method
     *
     * @param  name  Description of the Parameter
     * @return       Description of the Return Value
     */
    public User newLocalUser(String name)
    {
        return new LocalUserImpl(name);
    }


    /**
     *  Description of the Method
     *
     * @param  rnode  Description of the Parameter
     * @return        Description of the Return Value
     */
    public User newRemoteUser(RemoteNode rnode)
    {
        return new RemoteUserImpl(rnode);
    }


    /**
     *  Description of the Method
     *
     * @param  name  Description of the Parameter
     * @return       Description of the Return Value
     */
    public Destination newLocalPrinter(String name)
    {
        return new LocalPrinterImpl(name);
    }


    /**
     *  Description of the Method
     *
     * @param  rnode  Description of the Parameter
     * @return        Description of the Return Value
     */
    public Destination newRemotePrinter(RemoteNode rnode)
    {
        return new RemotePrinterImpl(rnode);
    }


    /**
     *  Description of the Method
     *
     * @param  action   Description of the Parameter
     * @param  suid     Description of the Parameter
     * @param  patient  Description of the Parameter
     * @return          Description of the Return Value
     */
    public InstancesAction newInstancesAction(String action, String suid,
            Patient patient)
    {
        return new InstancesActionImpl(action, suid, patient);
    }


    /**
     *  Description of the Method
     *
     * @param  patient  Description of the Parameter
     * @return          Description of the Return Value
     */
    public MediaDescription newMediaDescription(Patient patient)
    {
        return new MediaDescriptionImpl(patient);
    }
}
