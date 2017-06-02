/*$Id: Code.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

package org.dcm4che.srom;

import org.dcm4che.data.Dataset;

/**
 * The <code>Code</code> interface represents a
 * <i>coded entry</i>.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 0.9.9
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * 8.8 STANDARD ATTRIBUTE SETS FOR CODE SEQUENCE ATTRIBUTES"
 */
public interface Code {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the DICOM <i>Code Value</i>.
     * <br>DICOM Tag: <code>(0008,0100)</code>
     * The code value.
     *
     * @return  the <i>Code Value</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions, 8.1 CODE VALUE"
     */
    public String getCodeValue();

    /**
     * Returns the DICOM <i>Coding Scheme Designator</i>.
     * <br>DICOM Tag: <code>(0008,0102)</code>
     * The coding scheme designator.
     *
     * @return  the <i>Coding Scheme Designator</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions, 
     * 8.2 CODING SCHEME DESIGNATOR"
     */
    public String getCodingSchemeDesignator();
    
    /**
     * Returns the DICOM <i>Coding Scheme Version</i>.
     * <br>DICOM Tag: <code>(0008,0103)</code>
     * The coding scheme version.
     *
     * @return  the <i>Coding Scheme Version</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions, 
     * 8.2 CODING SCHEME VERSION"
     */
    public String getCodingSchemeVersion();

    /**
     * Returns the DICOM <i>Code Meaning</i>.
     * <br>DICOM Tag: <code>(0008,0104)</code>
     * The code meaning.
     *
     * @return  the <i>Code Meaning</i>.
     *
     * @see "DICOM Part 3: Information Object Definitions, 
     * 8.3 CODE MEANING"
     */
    public String getCodeMeaning();
    
    /**
     * Compares two Code objects for equality.
     * Only code value and coding scheme designator will be use to
     * find out of this code and the one specified as parameter 
     * <code>obj</code> are equal.
     *
     * @param obj  the Code object to be compared for equality with this Code
     *             object.
     *
     * @return <code>true</code> if equal <code>false</code> otherwise.
     */
    public boolean equals(Object obj);
    
    public void toDataset(Dataset ds);   
}//end interface Code
