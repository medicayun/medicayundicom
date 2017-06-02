/*$Id: StudyImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.*;
import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class StudyImpl implements org.dcm4che.srom.Study {
    
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private final String studyInstanceUID;
    private final String studyID;
    private final Long studyDateTime;
    private final String referringPhysicianName;
    private final String accessionNumber;
    private final String studyDescription;
    private final Code[] procedureCodes;

    // Constructors --------------------------------------------------
    public StudyImpl(String studyInstanceUID, String studyID,
            Date studyDateTime, String referringPhysicianName,
            String accessionNumber, String studyDescription,
            Code[] procedureCodes) {
        if (studyInstanceUID.length() == 0)
            throw new IllegalArgumentException(studyInstanceUID);
    
        this.studyInstanceUID = studyInstanceUID;
        this.studyID = studyID;
        this.studyDateTime = studyDateTime != null 
            ? new Long(studyDateTime.getTime()) : null;
        this.referringPhysicianName = referringPhysicianName;
        this.accessionNumber = accessionNumber;
        this.studyDescription = studyDescription;
        this.procedureCodes = procedureCodes != null
                ? (Code[])procedureCodes.clone()
                : CodeImpl.EMPTY_ARRAY;
    }
    
    public StudyImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.StudyInstanceUID),
            ds.getString(Tags.StudyID),
            ds.getDateTime(Tags.StudyDate, Tags.StudyTime),
            ds.getString(Tags.ReferringPhysicianName),
            ds.getString(Tags.AccessionNumber),
            ds.getString(Tags.StudyDescription), 
            CodeImpl.newCodes(ds.get(Tags.ProcedureCodeSeq)));
    }
    
    // Public --------------------------------------------------------
    public final String getStudyInstanceUID() {
        return studyInstanceUID;
    }
    
    public final String getStudyID() {
        return studyID;
    }
    
    public final Date getStudyDateTime() {
        return studyDateTime != null
            ? new Date(studyDateTime.longValue()) : null;
    }
    
    public final String getReferringPhysicianName() {
        return referringPhysicianName;
    }
    
    public final String getAccessionNumber() {
        return accessionNumber;
    }
    
    public final String getStudyDescription() {
        return studyDescription;
    }
    
    public final Code[] getProcedureCodes() {
        return (Code[])procedureCodes.clone();
    }

    public int hashCode() {
        return studyInstanceUID.hashCode();
    }
    
    public boolean equals(Object o) {
        if (o == this)
            return true;
        
        if (!(o instanceof Study))
            return false;
    
        Study sty = (Study)o;
        return studyInstanceUID.equals(sty.getStudyInstanceUID());
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Study[").append(studyInstanceUID);
        sb.append(",ID=").append(studyID);
        sb.append(",A#=").append(accessionNumber);
        for (int i = 0; i < procedureCodes.length; ++i)
            sb.append(",PC=").append(procedureCodes[i]);
        sb.append("]");
        return sb.toString();
    }

    public void toDataset(Dataset ds) {
        ds.putUI(Tags.StudyInstanceUID, studyInstanceUID);
        ds.putSH(Tags.StudyID, studyID);
        Date date = getStudyDateTime();
        ds.putDA(Tags.StudyDate, date);
        ds.putTM(Tags.StudyTime, date);
        ds.putPN(Tags.ReferringPhysicianName, referringPhysicianName);
        ds.putLO(Tags.AccessionNumber, accessionNumber);
                
        if (studyDescription != null)
            ds.putLO(Tags.StudyDescription, studyDescription);
        
        if (procedureCodes.length != 0) {
            DcmElement sq = ds.putSQ(Tags.ProcedureCodeSeq);
            for (int i = 0; i < procedureCodes.length; ++i) {
                procedureCodes[i].toDataset(sq.addNewItem());
            }
        }
    }
}
