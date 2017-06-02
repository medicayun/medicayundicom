/*$Id: WaveformContentImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

import org.dcm4che.srom.*;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class WaveformContentImpl extends CompositeContentImpl
        implements WaveformContent {
    // Constants -----------------------------------------------------
    private static final int[] NULL_CHANNELNUMBER = {};
    
    // Attributes ----------------------------------------------------
    protected int[] channelNumbers;

    // Constructors --------------------------------------------------
    WaveformContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, RefSOP refSOP, int[] channelNumbers) {
        super(owner, obsDateTime, template, name, refSOP);
        setChannelNumbers(channelNumbers);
    }
    
    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
        return new WaveformContentImpl(newOwner,
                getObservationDateTime(inheritObsDateTime),
                template, name, refSOP, channelNumbers);
    }
    
    // Methodes --------------------------------------------------------
    public String toString() {
        StringBuffer sb = prompt().append(refSOP);
        for (int i = 0; i < channelNumbers.length; ++i)
            sb.append(",[").append(channelNumbers[0]).append("]");

        return sb.append(')').toString();
    }

    public final ValueType getValueType() {
        return ValueType.WAVEFORM;
    }
    
    public final int[] getChannelNumbers() {
        return (int[])channelNumbers.clone();
    }
    
    public final void setChannelNumbers(int[] channelNumbers) {
        if (channelNumbers != null) {
            if ((channelNumbers.length & 1) != 0) {
                throw new IllegalArgumentException("L="+channelNumbers.length);
            }
            this.channelNumbers = (int[])channelNumbers.clone();
        } else {
            this.channelNumbers = NULL_CHANNELNUMBER;
        }
    }


    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        if (channelNumbers.length != 0) {
            ds.get(Tags.RefSOPSeq).getItem()
                    .putUS(Tags.RefWaveformChannels, channelNumbers);
        }
   }
}
