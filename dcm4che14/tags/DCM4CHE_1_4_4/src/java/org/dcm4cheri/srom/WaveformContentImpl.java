/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

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
