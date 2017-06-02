/*                                                                           *
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG                             *
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
package org.dcm4che.auditlog;

import java.net.Socket;

import org.apache.log4j.Logger;
import org.dcm4che.Implementation;

/**
 *  Description of the Class
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      August 27, 2002
 * @version    $Revision: 3733 $ $Date: 2003-11-07 18:54:18 +0800 (周五, 07 11月 2003) $
 */
public abstract class AuditLoggerFactory
{
    // Constants -----------------------------------------------------

    // Constructor ---------------------------------------------------
    /**
     *  Gets the instance attribute of the AuditLoggerFactory class
     *
     * @return    The instance value
     */
    public static AuditLoggerFactory getInstance()
    {
        return (AuditLoggerFactory) Implementation.findFactory(
                "dcm4che.auditlog.AuditLoggerFactory");
    }

    // Public --------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public abstract AuditLogger newAuditLogger();


    /**
     *  Description of the Method
     *
     * @param  log  Description of the Parameter
     * @return      Description of the Return Value
     */
    public abstract AuditLogger newAuditLogger(Logger log);


    /**
     *  Description of the Method
     *
     * @param  id    Description of the Parameter
     * @param  name  Description of the Parameter
     * @return       Description of the Return Value
     */
    public abstract Patient newPatient(String id, String name);


    /**
     *  Description of the Method
     *
     * @param  ip     Description of the Parameter
     * @param  hname  Description of the Parameter
     * @param  aet    Description of the Parameter
     * @return        Description of the Return Value
     */
    public abstract RemoteNode newRemoteNode(
            String ip, String hname, String aet);


    /**
     *  Description of the Method
     *
     * @param  socket  Description of the Parameter
     * @param  aet     Description of the Parameter
     * @return         Description of the Return Value
     */
    public abstract RemoteNode newRemoteNode(Socket socket, String aet);


    /**
     *  Description of the Method
     *
     * @param  name  Description of the Parameter
     * @return       Description of the Return Value
     */
    public abstract User newLocalUser(String name);


    /**
     *  Description of the Method
     *
     * @param  rnode  Description of the Parameter
     * @return        Description of the Return Value
     */
    public abstract User newRemoteUser(RemoteNode rnode);


    /**
     *  Description of the Method
     *
     * @param  name  Description of the Parameter
     * @return       Description of the Return Value
     */
    public abstract Destination newLocalPrinter(String name);


    /**
     *  Description of the Method
     *
     * @param  rnode  Description of the Parameter
     * @return        Description of the Return Value
     */
    public abstract Destination newRemotePrinter(RemoteNode rnode);


    /**
     *  Description of the Method
     *
     * @param  action   Description of the Parameter
     * @param  suid     Description of the Parameter
     * @param  patient  Description of the Parameter
     * @return          Description of the Return Value
     */
    public abstract InstancesAction newInstancesAction(String action,
            String suid, Patient patient);


    /**
     *  Description of the Method
     *
     * @param  patient  Description of the Parameter
     * @return          Description of the Return Value
     */
    public abstract MediaDescription newMediaDescription(Patient patient);
}
