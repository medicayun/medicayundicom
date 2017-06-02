/*$Id: ContainerContent.java 3536 2002-08-05 14:16:48Z gunterze $*/
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
 * The <code>ContainerContent</code> interface represents a
 * <i>DICOM SR Container Content</i> of value type <code>CONTAINER</code>.
 * <br>
 * A <i>Container Content</i> is used as Document Title or 
 * document section heading. Concept Name conveys 
 * the Document Title (if the <code>CONTAINER</code> is the 
 * Document Root Content Item) or the category of observation.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 */
public interface ContainerContent extends Content {

    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------

    /**
     * Spefifies whether <i>Content Items</i> are logically linked.
     * <br>DICOM Tag: <code>(0040,A050)</code>
     * <br>Tag Name: <code>Continuity of Content</code>
     * <br>
     * This flag specifies whether or not its contained
     * Content Items are logically linked in a continuous textual flow, 
     * or are separate items.
     *
     * @return  <code>true</code> if <code>Continuity of Content</code>
     * has value <code>SEPARATE</code> or <code>false</code> if
     * it has value <code>CONTINUOUS</code>
     *
     * @see "DICOM Part 3: Information Object Definitions,
     * Annex C.17.3.2 Continuity of Content"
     */
    public boolean isSeparate();

    public void setSeparate(boolean separate);
    
    /**
     * Convenient method to insert a new <i>Composite Content Item</i> into
     * this container and add the SOP instance reference into the <i>Current
     * Requested Procedure Evidence Sequence</i>. Particularly useful for
     * construction of Key Object Selection Documents.
     *
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param refSOP  the SOP instance reference.
     */
    public void insertCompositeContent(Code name, SOPInstanceRef refSOP);

    /**
     * Convenient method to insert a new <i>Image Content Item</i> into
     * this container and add the SOP instance reference of the image and the 
     * presentation state SOP instance reference into the <i>Current
     * Requested Procedure Evidence Sequence</i>. Particularly useful for
     * construction of Key Object Selection Documents.
     *
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param refSOP  the SOP instance reference of the referenced image.
     * @param frameNumbers  the references frame numbers of a multiframe image.
     * @param refPresentationSOP  the SOP instance reference of the
     *                            referenced presentation state.
     * @param iconImage <i>Icon Image</i> or <code>null</code>.
     */
    public void insertImageContent(Code name, SOPInstanceRef refSOP,
        int[] frameNumbers, SOPInstanceRef refPresentationSOP,
        IconImage iconImage);

    /**
     * Convenient method to insert a new <i>Waveform Conten Item</i> into
     * this container and add the SOP instance reference into the <i>Current
     * Requested Procedure Evidence Sequence</i>. Particularly useful for
     * construction of Key Object Selection Documents.
     *
     * @param name <i>Concept Name Code</i> or <code>null</code>.
     * @param refSOP  the SOP instance reference of the referenced Wavoform.
     * @param channelNumbers  the referenced channel numbers.
     */
    public void insertWaveformContent(Code name, SOPInstanceRef refSOP,
                                         int[] channelNumbers);
}
