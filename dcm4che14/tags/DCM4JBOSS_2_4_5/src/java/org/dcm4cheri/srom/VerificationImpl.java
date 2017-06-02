/*$Id: VerificationImpl.java 3554 2002-08-22 23:15:38Z gunterze $*/
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
import org.dcm4che.srom.Verification;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;

import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class VerificationImpl implements Verification {
    // Constants -----------------------------------------------------
    static final Verification[] EMPTY_ARRAY = {};

    // Attributes ----------------------------------------------------
    private final long time;
    private final String observerName;
    private final String observerOrg;
    private final Code observerCode;

    // Constructors --------------------------------------------------
    public VerificationImpl(Date time, String observerName, String observerOrg,
        Code observerCode)
    {
        this.time = time.getTime();
        if ((this.observerName = observerName).length() == 0)
            throw new IllegalArgumentException();
        this.observerOrg = observerOrg;
        this.observerCode = observerCode;
    }

    public VerificationImpl(Dataset ds) throws DcmValueException {
        this(ds.getDate(Tags.VerificationDateTime),
                ds.getString(Tags.VerifyingObserverName),
                ds.getString(Tags.VerifyingOrganization),
                CodeImpl.newCode(ds.getItem(
                        Tags.VerifyingObserverIdentificationCodeSeq)));
    }
    
    // Methodes ------------------------------------------------------
    public String getVerifyingObserverName() { return observerName; }
    public Code getVerifyingObserverCode() { return observerCode; }
    public String getVerifyingOrganization() { return observerOrg; }
    public Date getVerificationDateTime() { return new Date(time); }
    
    //compares code value,coding scheme designator only    
    public String toString() {
        return "Verification[" + getVerificationDateTime() 
                         + "/" + observerName + "]";
    }
        
    public int compareTo(java.lang.Object obj) {
        VerificationImpl v = (VerificationImpl)obj;
        return (int)(v.time - time);
    }
        
    public void toDataset(Dataset ds) {
        ds.putDT(Tags.VerificationDateTime, new Date(time));
        ds.putPN(Tags.VerifyingObserverName, observerName);
        ds.putLO(Tags.VerifyingOrganization, observerOrg);
        if (observerCode != null) {
            observerCode.toDataset(
                ds.putSQ(Tags.VerifyingObserverIdentificationCodeSeq)
                        .addNewItem());
        }
    }
    
}
