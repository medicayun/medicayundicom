/*$Id: CodeContent.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
 * The <code>CodeContent</code> interface represents a
 * <i>DICOM SR Code Content</i> of value type <code>CODE</code>.
 * <br>
 * A <i>Code Content</i> item represents a coded value in the 
 * DICOM SR document. 
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.18.2 Code Macro"
 */
public interface CodeContent extends Content {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the single item of <i>DICOM SR Concept Code Sequence</i>.
     * <br>DICOM Tag: <code>(0040,A168)</code>
     * <br>Tag Name: <code>Concept Code Sequence</code>
     * <br>
     * This is the value of the <i>Code Content Item</i>. 
     * Only a single Item is permitted in this sequence.
     *
     * @return  Code value item of <code>Concept Code Sequence</code>.
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.18.2 Code Macro"
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * 8.8 STANDARD ATTRIBUTE SETS FOR CODE SEQUENCE ATTRIBUTES"
     */
    public Code getCode();
    
    public void setCode(Code code);
    
}//end interface CodeContent 
