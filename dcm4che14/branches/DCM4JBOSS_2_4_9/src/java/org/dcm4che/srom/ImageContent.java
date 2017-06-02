/*$Id: ImageContent.java 3536 2002-08-05 14:16:48Z gunterze $*/
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
 * The <code>ImageContent</code> interface represents a
 * <i>DICOM SR Image Content</i> of value type <code>IMAGE</code>.
 * <br>
 *
 * A <i>DICOM SR Image Content</i> specifies the Attributes that convey 
 * a reference to a DICOM image.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.18.4 Image Reference Macro"
 */
public interface ImageContent extends CompositeContent {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the frame numbers of a refereced DICOM image.
     * <br>DICOM Tag: <code>(0008,1160)</code>
     * <br>Tag Name: <code>Referenced Frame Number</code>
     * <br>
     * Identifies the frame numbers within the Referenced SOP Instance 
     * to which the reference applies. The first frame shall be denoted 
     * as frame number 1. 
     * 
     * <br><b>Note:</b><br> 
     * <pre>
     *   This Attribute may be multi-valued. 
     *   Required if the Referenced SOP Instance 
     *   is a multi-frame image and the reference 
     *   does not apply to all frames.
     * </pre>
     *
     * @return  Frame numbers of a refereced DICOM image.
     */
    public int[] getFrameNumbers();

    public void setFrameNumbers(int[] frameNumbers);
    
    /**
     * Returns the <b>single item</b> of <i>DICOM Referenced SOP Sequence</i>
     * for reference to a 
     * <i>Softcopy Presentation State SOP Class/SOP Instance</i> pair.
     * <br>DICOM Tag: <code>(0008,1199)</code>
     * <br>Tag Name: <code>Referenced SOP Sequence</code>
     * <br>
     * This is the reference to the presentation state of a DICOM image.
     * Only a single Item is permitted in this sequence so only one
     * {@link RefSOP} object will be returned.
     *
     * <br>
     * <br><b>NOTE:</b>  from [Clunie2000]
     * <pre>
     *   There may not be more than one item (since that would imply 
     *   more than one value),<sup>58</sup> and there may not be zero items 
     *   (since that would imply no value).
     *   
     *   <sup>58.</sup><small>
     *      This is not strictly true. As of the time of writing,
     *      there is what is probably an error in Supplement 23 
     *      that has not yet been corrected with a CP. Though the
     *      text describing the COMPOSITE value type implies a 
     *      single reference,the macro allows for one or more 
     *      items in the sequence. This also affects the IMAGE 
     *      and WAVEFORM value types which include the COMPOSITE 
     *      macro.
     *     </small>
     * </pre>
     *
     * @return  Single item of <code>Referenced SOP Sequence</code>.
     *
     * @see "[Clunie2000] - Clunie, David. <i>DICOM Structured Reporting</i>. 
     * PixelMed Publishing, Bangor, Pennsylvania, 2000. ISBN: 0970136900."
     */
    public RefSOP getRefPresentationSOP();

    public void setRefPresentationSOP(RefSOP refPresentationSOP);
    
    public IconImage getIconImage();

    public void setIconImage(IconImage iconImage);
    
}//end interface ImageContent
