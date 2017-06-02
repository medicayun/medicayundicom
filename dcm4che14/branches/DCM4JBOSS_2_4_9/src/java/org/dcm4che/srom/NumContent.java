/*$Id: NumContent.java 3758 2004-04-05 10:56:10Z umbertoc $*/
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
 * The <code>NumContent</code> interface represents a
 * <i>DICOM SR Numeric Content</i> of value type <code>NUM</code>.
 * <br>
 * 
 * Numeric value fully qualified by coded representation of the 
 * measurement name and unit of measurement.
 * 
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.18.1 Numeric Measurement Macro"
 */
public interface NumContent extends Content {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the numeric value.
     * <br>DICOM Tag: <code>(0040,A30A)</code>
     * <br>Tag Name: <code>Numeric Value</code>
     * <br>
     * Numeric measurement value.
     * 
     * @return  the the numeric value.
     */
    public Float getValue();
    
    /**
     * Returns the measurement unit.
     * <br>DICOM Tag: <code>(0040,08EA)</code>
     * <br>Tag Name: <code>Measurement Units Code Sequence</code>
     * <br>
     * The single item of <i>Measurement Units Code Sequence</i>
     *
     * @return the measurement unit.
     */
    public Code getUnit();

    public Code getQualifier();
    
    public void setValue(Float value);

    public void setUnit(Code code);

    public void setQualifier(Code code);
    
}//end interface NumContent
