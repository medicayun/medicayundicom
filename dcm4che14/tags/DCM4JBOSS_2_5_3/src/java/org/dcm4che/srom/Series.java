/*$Id: Series.java 3656 2003-06-09 17:17:51Z gunterze $*/
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

import org.dcm4che.data.Dataset;


/**
 * The <code>Series</code> interface represents the 
 * <i>DICOM SR Document Series Module</i>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.1 SR Document Series Module"
 */
public interface Series {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the DICOM modality name (<code>SR</code>).
     * <br>DICOM Tag: <code>(0008,0060)</code>.
     *
     * @return  <code>SR</code> as DICOM modality name for DICOM SR.
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.7.3.1.1.1 Modality"
     */
    public String getModality();
    
    /**
     * Returns the DICOM series instance UID.
     * <br>DICOM Tag: <code>(0020,000E)</code>.
     *
     * @return  The DICOM series instance UID.
     */
    public String getSeriesInstanceUID();
    
    /**
     * Returns a number that identifies the Series.
     * <br>DICOM Tag: <code>(0020,0011)</code>.
     *
     * @return  A number that identifies the Series.
     */
    public int getSeriesNumber();
    
    /**
     * Returns the single item of the 
     * <i>Referenced Study Component Sequence</i>.
     * <br>
     * <i>Referenced Study Component Sequence</i> 
     * (Tag: <code>(0008,1111)</code>)
     * is a sequence that permits only a single item.
     * Uniquely identifies the <i>Performed Procedure Step SOP Instance</i>
     * for which the Series is created. <br>
     *
     * This sequence containes two entries for the single item:
     * <ul>
     *   <li><i>Referenced SOP Class UID</i> <code>(0008,1150)</code></li>
     *   <li><i>Referenced SOP Instance UID</i> <code>(0008,1155)</code></li>
     * </ul>
     *
     * @return  Single item of the <i>Referenced Study Component Sequence</i>.
     */
    public RefSOP getRefStudyComponent();

    public void toDataset(Dataset ds);
}//end interface Series
