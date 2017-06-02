/*$Id: RequestImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com> *
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

import org.dcm4che.srom.Code;
import org.dcm4che.srom.Request;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class RequestImpl implements Request {
    // Constants -----------------------------------------------------
    static final Request[] EMPTY_ARRAY = {};

    // Attributes ----------------------------------------------------
    private final String studyInstanceUID;
    private final String accessionNumber;
    private final String fillerOrderNumber;
    private final String placerOrderNumber;
    private final String procedureID;
    private final String procedureDescription;
    private final Code procedureCode;

    // Constructors --------------------------------------------------
    public RequestImpl(String studyInstanceUID, String accessionNumber,
        String fillerOrderNumber, String placerOrderNumber,
        String procedureID, String procedureDescription,
        Code procedureCode)
    {
        if ((this.studyInstanceUID = studyInstanceUID).length() == 0)
            throw new IllegalArgumentException();
       this.accessionNumber = accessionNumber;
       this.fillerOrderNumber = fillerOrderNumber;
       this.placerOrderNumber = placerOrderNumber;
       this.procedureID = procedureID;
       this.procedureDescription = procedureDescription;
       this.procedureCode = procedureCode;
    }

    public RequestImpl(Dataset ds) throws DcmValueException
    {
        this(ds.getString(Tags.StudyInstanceUID),
                ds.getString(Tags.AccessionNumber),
                ds.getString(Tags.FillerOrderNumber),
                ds.getString(Tags.PlacerOrderNumber),
                ds.getString(Tags.RequestedProcedureID),
                ds.getString(Tags.RequestedProcedureDescription),
                CodeImpl.newCode(
                        ds.getItem(Tags.RequestedProcedureCodeSeq)));
    }
    // Methodes ------------------------------------------------------
    
    //compares code value,coding scheme designator only
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof RequestImpl))
            return false;
        RequestImpl o = (RequestImpl)obj;
        return studyInstanceUID.equals(o.studyInstanceUID);
    }        

    public int hashCode() { return studyInstanceUID.hashCode(); }
    
    public String toString() {
        return "Request[uid=" + studyInstanceUID
             + ",accession=" + accessionNumber
             + ",fillerOrd=" + fillerOrderNumber
             + ",placerOrd=" + placerOrderNumber
             + ",procedure(" + procedureID
             + "," + procedureDescription
             + "," + procedureCode
             + ")]";
    }
    
    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }
    
    public String getFillerOrderNumber() {
        return fillerOrderNumber;
    }
    
    public String getPlacerOrderNumber() {
        return placerOrderNumber;
    }
    
    public String getAccessionNumber() {
        return accessionNumber;
    }
    
    public String getProcedureID() {
        return procedureID;
    }
    
    public String getProcedureDescription() {
        return procedureDescription;
    }
    
    public Code getProcedureCode() {
        return procedureCode;
    }    

    public void toDataset(Dataset ds) {
        ds.putUI(Tags.StudyInstanceUID, studyInstanceUID);
        ds.putLO(Tags.AccessionNumber, accessionNumber);
        ds.putLO(Tags.FillerOrderNumber, fillerOrderNumber);
        ds.putLO(Tags.PlacerOrderNumber, placerOrderNumber);
        ds.putSH(Tags.RequestedProcedureID, procedureID);
        ds.putLO(Tags.RequestedProcedureDescription, procedureDescription);
        if (procedureCode != null) {
            procedureCode.toDataset(
                ds.putSQ(Tags.RequestedProcedureCodeSeq).addNewItem());
        }
    }
}
