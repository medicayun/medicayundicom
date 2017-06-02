/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

/* $Id: SOPInstanceRef.java 3493 2002-07-14 16:03:36Z gunterze $ */

package org.dcm4che.srom;


/**
 * The <code>SOPInstanceRef</code> interface represents a
 * <i>SOP Instance Reference</i>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 0.9.9
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.2.1 SOP Instance Reference Macro"
 */
public interface SOPInstanceRef extends RefSOP {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
   
    /**
     * Returns the DICOM <i>Study Instance UID</i>.
     * <br>DICOM Tag: <code>(0020,000D)</code>
     * <br>Tag Name: <code>Study Instance UID</code>
     * <br>
     * Unique identifier for the Study.
     *
     * @return  the <i>Study Instance UID</i>.
     */
    public String getStudyInstanceUID();
    
    /**
     * Returns the DICOM <i>Series Instance UID</i>.
     * <br>DICOM Tag: <code>(0020,000E)</code>
     * <br>Tag Name: <code>Series Instance UID</code>
     * <br>
     * Unique identifier of a Series that is part of this Study 
     * and contains the referenced Composite Object(s).
     *
     * @return  the <i>Series Instance UID</i>.
     */
    public String getSeriesInstanceUID();
    
}//end interface SOPInstanceRef
