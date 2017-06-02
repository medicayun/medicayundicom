/*                                                                           *
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
 */
package org.dcm4cheri.auditlog;
import org.dcm4che.auditlog.Destination;

import org.dcm4che.auditlog.RemoteNode;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      March 16, 2003
 * @version    $Revision: 3616 $ $Date: 2003-03-17 07:38:03 +0800 (周一, 17 3月 2003) $
 */
class RemotePrinterImpl implements Destination
{

    private RemoteNode rnode;


    /**
     *Constructor for the RemotePrinterImpl object
     *
     * @param  rnode  Description of the Parameter
     */
    public RemotePrinterImpl(RemoteNode rnode)
    {
        this.rnode = rnode;
    }


    /**
     *  Description of the Method
     *
     * @param  sb  Description of the Parameter
     */
    public void writeTo(StringBuffer sb)
    {
        sb.append("<Destination><Node>");
        rnode.writeTo(sb);
        sb.append("</Node><Destination>");
    }

}

