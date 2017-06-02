/*$Id: Study.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

import java.util.Date;


/**
 * The <code>Study</code> interface represents some of the fields of the
 * <i>DICOM General Study Module</i>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.7.2.1 General Study Module"
 */
public interface Study {
        
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the study instance UID.
     * <br>DICOM Tag: <code>(0020,000D)</code>
     *
     * @return the study instance UID.
     */
    public String getStudyInstanceUID();
    
    /**
     * Returns the DICOM <i>Study ID</i>.
     * <br>DICOM Tag: <code>(0020,0010)</code>
     *
     * @return the study ID.
     */
    public String getStudyID();
    
    /**
     * Returns the study date and time.
     * <br>DICOM Tags: <br>
     * <ul>
     *   <li> <i>Study Date</i> <code>(0008,0020)</code> </li>
     *   <li> <i>Study Time</i> <code>(0008,0030)</code> </li>
     * </ul>
     *
     * @return  the study date and time.
     */
    public Date getStudyDateTime();
    
    /**
     * Returns the DICOM <i>Referring Physician's Name</i>.
     * <br>DICOM Tag: <code>(0008,0090)</code>
     *
     * @return Referring Physician's Name.
     */
    public String getReferringPhysicianName();
    
    /**
     * Returns the DICOM <i>Accession Number</i>.
     * <br>DICOM Tag: <code>(0008,0050)</code>
     *
     * @return  Accession Number.
     */
    public String getAccessionNumber();
    
    /**
     * Returns the DICOM <i>Study Description</i>.
     * <br>DICOM Tag: <code>(0008,1030)</code>
     *
     * @return  Study Description.
     */
    public String getStudyDescription();
    
    /**
     * Returns the entries of the DICOM <i>Procedure Code Sequence</i>.
     * <br>DICOM Tag: <code>(0008,1032)</code>
     *
     * @see Code
     * @return Procedure Code Sequence.
     */
    public Code[] getProcedureCodes();
    
    public void toDataset(Dataset ds);
}//end interface Study