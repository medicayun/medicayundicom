/*$Id: RefSOPImpl.java 3580 2002-10-03 12:47:17Z gunterze $*/
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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.Tags;

import org.dcm4che.srom.RefSOP;

import org.apache.log4j.Logger;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class RefSOPImpl implements org.dcm4che.srom.RefSOP {
    static Logger log = Logger.getLogger(RefSOPImpl.class);
    // Constants -----------------------------------------------------
    private static UIDDictionary uidDict =
            DictionaryFactory.getInstance().getDefaultUIDDictionary( );   

    // Attributes ----------------------------------------------------
    private final String refSOPClassUID;
    private final String refSOPInstanceUID;

    // Constructors --------------------------------------------------
    public RefSOPImpl(String refSOPClassUID, String refSOPInstanceUID) {
        if (refSOPClassUID.length() == 0)
            throw new IllegalArgumentException(refSOPClassUID);
    
        if (refSOPInstanceUID.length() == 0)
            throw new IllegalArgumentException(refSOPInstanceUID);
    
        this.refSOPClassUID = refSOPClassUID;
        this.refSOPInstanceUID = refSOPInstanceUID;
    }

    public RefSOPImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.RefSOPClassUID),
            ds.getString(Tags.RefSOPInstanceUID));
    }
    
    private static boolean hasValue(String s) {
        return s != null && s.length() > 0;
    }
    
    public static RefSOP newRefSOP(Dataset ds) throws DcmValueException {
        if (ds == null) {
            return null;
        }
        String cuid;
        String iuid;
        if (!hasValue(cuid = ds.getString(Tags.RefSOPClassUID))) {
            log.warn("Missing Ref SOP Class UID - ignore reference");
            return null;
        }
        if (!hasValue(iuid = ds.getString(Tags.RefSOPInstanceUID))) {
            log.warn("Missing Ref SOP Instance UID - ignore reference");
            return null;
        }
        return new RefSOPImpl(cuid, iuid);
    }
    
    // Public --------------------------------------------------------
    public String toString() {
        return uidDict.lookup(refSOPClassUID) + "[" + refSOPInstanceUID + "]"; 
    }

    public void toDataset(Dataset ds) {
        ds.putUI(Tags.RefSOPClassUID, refSOPClassUID);
        ds.putUI(Tags.RefSOPInstanceUID, refSOPInstanceUID);
    }    
    
    public final String getRefSOPClassUID() {
        return refSOPClassUID;
    }
    
    public final String getRefSOPInstanceUID() {
        return refSOPInstanceUID;
    }
    
    public final int hashCode() {
        return refSOPInstanceUID.hashCode();
    }
    
    public final boolean equals(Object o) {
        if (o == this)
            return true;
        
        if (!(o instanceof RefSOP))
            return false;
    
        RefSOP refSOP = (RefSOP)o;
        return refSOPInstanceUID.equals(refSOP.getRefSOPInstanceUID());
    }
}
