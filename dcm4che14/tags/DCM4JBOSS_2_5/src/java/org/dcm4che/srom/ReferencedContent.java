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

/* $Id: ReferencedContent.java 3493 2002-07-14 16:03:36Z gunterze $ */

package org.dcm4che.srom;


/**
 * The <code>ReferencedContent</code> interface represents a
 * <i>Content Item</i> reference.
 * <br>
 * 
 * Shall not be present if <i>Relationship Type</i> <code>(0040,A010)</code> is 
 * <code>CONTAINS</code>.
 * 
 * @author  gunter.zeilinger@tiani.com
 * @version 0.9.9
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 */
public interface ReferencedContent extends Content {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the reference <i>Content Item</i>.
     * 
     * @return  the reference <i>Content Item</i>.
     */
    public Content getRefContent();

}//end interface ReferencedContent
