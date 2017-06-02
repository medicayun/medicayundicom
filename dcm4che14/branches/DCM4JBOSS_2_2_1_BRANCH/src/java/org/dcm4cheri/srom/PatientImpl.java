/*$Id: PatientImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
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

package org.dcm4cheri.srom;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.Patient;
import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
final class PatientImpl implements org.dcm4che.srom.Patient {
    // Constants -----------------------------------------------------        
    
    private final String patientID;
    private final String patientName;
    private final Sex patientSex;
    private final Long patientBirthDate;

    // Constructors --------------------------------------------------
    public PatientImpl(String patientID, String patientName, Sex patientSex,
            Date patientBirthDate) {
        if (patientID == null)
            throw new NullPointerException();
        
        if (patientName == null)
            throw new NullPointerException();

        this.patientID = patientID;
        this.patientName = patientName;
        this.patientSex = patientSex;
        this.patientBirthDate = patientBirthDate != null 
            ? new Long(patientBirthDate.getTime()) : null;
    }       

    public PatientImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.PatientID),
             ds.getString(Tags.PatientName),
             Patient.Sex.valueOf(ds.getString(Tags.PatientSex)),
             ds.getDate(Tags.PatientBirthDate));
    }
    
    // Public --------------------------------------------------------
    public final String getPatientID() {
        return patientID;
    }
    
    public final String getPatientName() {
        return patientName;
    }
    
    public final Sex getPatientSex() {
        return patientSex;
    }
    
    public final Date getPatientBirthDate() {
        return patientBirthDate != null
            ? new Date(patientBirthDate.longValue()) : null;
    }
    
    public int hashCode() {
        return patientID.hashCode() + patientName.hashCode();
    }
    
    public boolean equals(Object o) {
        if (o == this)
            return true;
        
        if (!(o instanceof Patient))
            return false;
    
        Patient p = (Patient)o;
        return patientID.equals(p.getPatientID())
                && patientName.equals(p.getPatientName());
    }
    
    public String toString() {
        return "Patient[" + patientName + ",ID=" + patientID + "]";
    }

    public void toDataset(Dataset ds) {
        ds.putLO(Tags.PatientID, patientID);
        ds.putPN(Tags.PatientName, patientName);
        ds.putCS(Tags.PatientSex, 
                patientSex != null ? patientSex.toString() : null);
        ds.putDA(Tags.PatientBirthDate, getPatientBirthDate());
    }
}
