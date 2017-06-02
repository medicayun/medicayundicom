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

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3835 $ $Date: 2004-12-03 22:12:29 +0800 (周五, 03 12月 2004) $
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
class StudyDeleted implements IHEYr4.Message {
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    private InstancesAction instancesAction;
    
    private final String desc;
    
    // Constructors --------------------------------------------------
    public StudyDeleted(InstancesAction instancesAction, String desc) {
        this.instancesAction = instancesAction;
        this.desc = desc;
    }
    
    // Methods -------------------------------------------------------
    public void writeTo(StringBuffer sb) {
        sb.append("<StudyDeleted>");
        instancesAction.writeTo(sb);
        if (desc != null)
            sb.append("<Description><![CDATA[").append(desc)
            	.append("]]></Description>");
        sb.append("</StudyDeleted>");
    }
    
}
