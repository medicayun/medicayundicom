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

/* $Id: RelationConstraints.java 3493 2002-07-14 16:03:36Z gunterze $ */

package org.dcm4che.srom;


/**
 * This interface will check the <i>Relationship</i> 
 * constraints between two <i>SR Content Item</i>s. 
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 0.9.9
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 */
public interface RelationConstraints {
    // Constants -----------------------------------------------------    
   
    // Public --------------------------------------------------------
    
    /**
     * Checks if a <i>Relation</i> is valid.
     * <i>SR Content Item</i>s have to have valid <i>Relationship Type</i>s
     * in order to have a valid SR Document.
     * <br>
     * e.g. the <code>source</code> of <i>Relationship</i> 
     * <code>CONTAINS</code> has to be of 
     * <i>Content Type</i> <code>CONTAINER</code>.
     *
     * @param source  the source <i>Content Item</i>.
     * @param relation  the <i>Relationship Type</i>. 
     * @param target  the target <i>Content Item</i>.
     *
     * @throws IllegalArgumentException  if <i>Relationship</i> is not valid.
     */
    public void check(Content source, Content.RelationType relation,
            Content target);

}//end interface RelationConstraints 
