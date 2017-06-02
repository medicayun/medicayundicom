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
import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.User;

import java.util.Iterator;
import java.util.LinkedHashSet;

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
class InstancesActionImpl implements InstancesAction {
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    private String action;
    private String accessionNumber;
    private LinkedHashSet suids = new LinkedHashSet(3);
    private Patient patient;
    private User user;
    private LinkedHashSet cuids = new LinkedHashSet(7);
    private int numberOfInstances = 0;
    private String mppsUID;
    
    // Constructors --------------------------------------------------
    public InstancesActionImpl(String action, String suid, Patient patient) {
        this.action = action;
        addStudyInstanceUID(suid);
        this.patient = patient;
    }
    
    // Methods -------------------------------------------------------
    public final void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }
    
    public final void addStudyInstanceUID(String suid) {
        suids.add(suid);
    }

    public final String[] listStudyInstanceUIDs() {
        return (String[]) suids.toArray(new String[suids.size()]);
    }
    
    public final void addSOPClassUID(String cuid) {
        cuids.add(cuid);
    }
    
    public final void clearSOPClassUIDs() {
        cuids.clear();
    }
    
    public final String[] listSOPClassUIDs() {
        return (String[]) cuids.toArray(new String[cuids.size()]);
    }
    
    public final void setUser(User user) {
        this.user = user;
    }
    
    public final void setNumberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }
    
    public final void incNumberOfInstances(int inc) {
        this.numberOfInstances += inc;
    }
    
    public final void setMPPSInstanceUID(String mppsUID) {
        this.mppsUID = mppsUID;
    }
    
    public final String getMPPSInstanceUID() {
        return mppsUID;
    }

    public void writeTo(StringBuffer sb) {
        sb.append("<ObjectAction>")
          .append(action)
          .append("</ObjectAction>");
        if (accessionNumber != null) {
            sb.append("<AccessionNumber><![CDATA[")
              .append(accessionNumber)
              .append("]]></AccessionNumber>");
        }
        for (Iterator it = suids.iterator(); it.hasNext();) {
            sb.append("<SUID>")
              .append(it.next())
              .append("</SUID>");
        }
        patient.writeTo(sb);
        if (user != null) {
            user.writeTo(sb);
        }
        for (Iterator it = cuids.iterator(); it.hasNext();) {
            sb.append("<CUID>")
              .append(it.next())
              .append("</CUID>");
        }
            
        if (numberOfInstances > 0) {
            sb.append("<NumberOfInstances>")
              .append(numberOfInstances)
              .append("</NumberOfInstances>");
        }
        if (mppsUID != null) {
            sb.append("<MPPSUID>")
              .append(mppsUID)
              .append("</MPPSUID>");
        }
    }
}
