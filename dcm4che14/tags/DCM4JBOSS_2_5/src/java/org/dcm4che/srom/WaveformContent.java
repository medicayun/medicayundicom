/*$Id: WaveformContent.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

package org.dcm4che.srom;

/**
 * The <code>WaveformContent</code> interface represents a
 * <i>DICOM SR Waveform Content</i> of value type <code>WAVEFORM</code>.
 * <br>
 *
 * A <i>DICOM SR Waveform Content</i> specifies the Attributes that 
 * convey a reference to a DICOM waveform.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.18.5 Waveform Reference Macro"
 */
public interface WaveformContent extends CompositeContent {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------

    /**
     * Returns the list of channels in Waveform.
     * <br>DICOM Tag: <code>(0040,A0B0)</code>
     * <br>Tag Name: <code>Referenced Waveform Channels</code>
     * <br>
     * List of channels in Waveform to which the reference applies. 
     * Required if the <i>Referenced SOP Instance</i> is a Waveform that 
     * contains multiple Channels and not all Channels in the Waveform 
     * are referenced.
     * 
     * @return  List of channels in Waveform to which the reference applies.
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.5.1.1 Referenced Waveform Channels"
     */
    public int[] getChannelNumbers();

    public void setChannelNumbers(int[] channelNumbers);
    
}//end interface WaveformContent
