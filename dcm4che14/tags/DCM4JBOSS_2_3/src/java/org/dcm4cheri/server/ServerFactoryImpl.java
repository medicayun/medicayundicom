/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.server;

import org.dcm4che.server.Server;
import org.dcm4che.server.UDPServer;
import org.dcm4che.server.SyslogService;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.server.HL7Handler;
import org.dcm4che.server.ServerFactory;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3592 $ $Date: 2003-02-07 22:19:55 +0800 (周五, 07 2月 2003) $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go 
 *            beyond the cvs commit message
 * </ul>
 */
public class ServerFactoryImpl extends ServerFactory
{
   
   public Server newServer(Server.Handler handler)
   {
      return new ServerImpl(handler);
   }
   
   public DcmHandler newDcmHandler(AcceptorPolicy policy,
         DcmServiceRegistry services)
   {
      return new DcmHandlerImpl(policy, services);
   }
   
   public HL7Handler newHL7Handler() {
      return new HL7HandlerImpl();
   }
   
   public UDPServer.Handler newSyslogHandler(SyslogService service) {
       return new SyslogHandlerImpl(service);
   }
   
   public UDPServer newUDPServer(UDPServer.Handler handler) {
       return new UDPServerImpl(handler);
   }
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
      
   // ServerFactory overrides ---------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
