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

import java.net.DatagramPacket;
import org.dcm4che.server.SyslogService;
import org.dcm4che.server.UDPServer;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:{email}">{full name}</a>.
 * @author  <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 3656 $ $Date: 2003-06-10 01:17:51 +0800 (周二, 10 6月 2003) $
 *   
 */
public class SyslogHandlerImpl implements UDPServer.Handler
{
    private final SyslogService service;
    
    public SyslogHandlerImpl(SyslogService service) {
        this.service = service;
    }
    
    public final void handle(DatagramPacket datagram)
    {
        byte[] buff = datagram.getData();
        try {
            SyslogMsg msg = new SyslogMsg(buff,datagram.getLength());
            service.process(msg.getTimestamp(), msg.getHost(), msg.getContent());
        }
        catch (SyslogMsg.InvalidSyslogMsgException e) {
            e.printStackTrace();
        }
    }

}
