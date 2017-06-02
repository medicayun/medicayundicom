/*$Id: TextContent.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
 * The <code>TextContent</code> interface represents a
 * <i>DICOM SR Text Content</i> of value type <code>TEXT</code>.
 * <br>
 * 
 * Free text, narrative description of unlimited length.
 * 
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 */
public interface TextContent extends Content {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the value of the <i>Text Value</i> field.
     * <br>DICOM Tag: <code>(0040,A160)</code>
     * <br>Tag Name: <code>Text Value</code>
     * <br>
     * Text data which is unformatted and whose manner of display is 
     * implementation dependent. The text value may contain spaces, 
     * as well as multiple lines separated by either <code>LF</code>, 
     * <code>CR</code>, <code>CR LF</code> or <code>LF CR</code>, 
     * but otherwise no format control characters 
     * (such as horizontal or vertical tab and form feed) shall be 
     * present, even if permitted by the Value Representation of UT. 
     * The text shall be interpreted as specified by 
     * <i>Specific Character Set</i> <code>(0008,0005)</code> if present 
     * in the SOP Common Module. 
     * <br>
     * <br>
     * <b>Note:</b><br>
     * <pre>
     *   The text may contain single or 
     *   multi-byte characters and use code 
     *   extension techniques as described 
     *   in PS 3.5 if permitted by the values 
     *   of <i>Specific Character Set</i> <code>(0008,0005)</code>. 
     * </pre>
     *
     * @return  the value of the <i>Text Value</i> field.
     */
    public String getText();

    public void setText(String text);
    
}//end interface TextContent 
