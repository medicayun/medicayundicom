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

package org.dcm4cheri.auditlog;

import org.dcm4che.auditlog.RemoteNode;

import java.net.InetAddress;
import java.net.Socket;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3840 $ $Date: 2004-12-10 23:24:36 +0800 (周五, 10 12月 2004) $
 * @since August 27, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class RemoteNodeImpl implements RemoteNode {
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    private String ip;
    private String hname;
    private String aet;
    
    // Constructors --------------------------------------------------
    public RemoteNodeImpl(String ip, String hname, String aet) {
        this.ip = ip;
        this.hname = hname;
        this.aet = aet;
    }

    public RemoteNodeImpl(Socket s, String aet) {
        InetAddress addr = s.getInetAddress();
        this.ip = addr.getHostAddress();
        this.hname = toHname(addr.getHostName());
        this.aet = aet;
    }
        
    public final String getAET() {
        return aet;
    }
    
    public final String getHostName() {
        return hname;
    }
    
    public final String getIP() {
        return ip;
    }

    private final String toHname(String name) {
        if (Character.isDigit(name.charAt(0))) {
            return null;
        }
        int pos = name.indexOf('.');
        return pos == -1 ? name : name.substring(0,pos);
    }
    
    // Methods -------------------------------------------------------
    public void writeTo(StringBuffer sb) {
        sb.append("<IP>").append(ip).append("</IP>");
        if (hname != null) {
            sb.append("<Hname>").append(hname).append("</Hname>");
        }
        if (aet != null) {
            sb.append("<AET><![CDATA[").append(aet).append("]]></AET>");
        }
    }
}
