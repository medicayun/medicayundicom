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

import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.RemoteNode;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3558 $ $Date: 2002-09-01 21:44:11 +0800 (周日, 01 9月 2002) $
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
class RnodeWithInstanceActionDescription implements IHEYr4.Message {
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    private String name;
    private RemoteNode rnode;
    private InstancesAction action;
    private final String rnodeName;
    
    // Constructors --------------------------------------------------
    public RnodeWithInstanceActionDescription(String name, RemoteNode rnode,
            InstancesAction action, String rnodeName) {
        this.name = name;
        this.rnode = rnode;
        this.action = action;
        this.rnodeName = rnodeName;
    }
    
    // Methods -------------------------------------------------------
    public void writeTo(StringBuffer sb) {
        sb.append("<")
          .append(name)
          .append("><")
          .append(rnodeName)
          .append(">");
        rnode.writeTo(sb);
        sb.append("</")
          .append(rnodeName)
          .append("><InstanceActionDescription>");
        action.writeTo(sb);
        sb.append("</InstanceActionDescription></")
          .append(name)
          .append(">");
    }
    
}
