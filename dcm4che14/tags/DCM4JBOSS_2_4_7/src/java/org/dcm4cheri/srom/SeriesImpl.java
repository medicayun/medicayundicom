/*$Id: SeriesImpl.java 3573 2002-09-22 22:09:37Z gunterze $*/
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

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class SeriesImpl implements org.dcm4che.srom.Series {
    
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private final String modality;
    private final String seriesInstanceUID;
    private final int seriesNumber;
    private final RefSOP refStudyComponent;

    // Constructors --------------------------------------------------
    public SeriesImpl(String modality, String seriesInstanceUID,
            int seriesNumber, RefSOP refStudyComponent) {
        if (modality.length() == 0)
            throw new IllegalArgumentException(modality);
        if (seriesInstanceUID.length() == 0)
            throw new IllegalArgumentException(seriesInstanceUID);
    
        this.modality = modality;
        this.seriesInstanceUID = seriesInstanceUID;
        this.seriesNumber = seriesNumber;
        this.refStudyComponent = refStudyComponent;
    }
    
    public SeriesImpl(Dataset ds) throws DcmValueException {
        this(ds.getString(Tags.Modality),
            ds.getString(Tags.SeriesInstanceUID),
            ds.getInt(Tags.SeriesNumber, -1),
            RefSOPImpl.newRefSOP(
                ds.getItem(Tags.RefPPSSeq)));
    }

    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    public String getModality() {
        return modality;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }
    
    public int getSeriesNumber() {
        return seriesNumber;
    }
    
    public RefSOP getRefStudyComponent() {
        return refStudyComponent;
    }
    
    public int hashCode() {
        return seriesInstanceUID.hashCode();
    }
    
    public boolean equals(Object o) {
        if (o == this)
            return true;
        
        if (!(o instanceof Series))
            return false;
    
        Series ser = (Series)o;
        return seriesInstanceUID.equals(ser.getSeriesInstanceUID());
    }
    
    public String toString() {
        return "Series[" + seriesInstanceUID
            + ",#" + seriesNumber
            + ",PPS=" + refStudyComponent
            + "]";
    }

    public void toDataset(Dataset ds) {
        ds.putCS(Tags.Modality, modality);
        ds.putUI(Tags.SeriesInstanceUID, seriesInstanceUID);
        ds.putIS(Tags.SeriesNumber, seriesNumber);
        DcmElement sq = ds.putSQ(Tags.RefPPSSeq);
        if (refStudyComponent != null) {
            refStudyComponent.toDataset(sq.addNewItem());
        }
    }
}
