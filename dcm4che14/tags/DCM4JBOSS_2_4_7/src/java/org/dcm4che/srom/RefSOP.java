/*$Id: RefSOP.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

/**
 * The <code>RefSOP</code> interface represents a
 * <i>SOP Class/SOP Instance</i> pair.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
public interface RefSOP {
    
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
     * Returns the DICOM <i>Referenced SOP Class UID</i>.
     * <br>DICOM Tag: <code>(0008,1150)</code>
     * Uniquely identifies the referenced SOP Class.
     *
     * @return  the <i>Referenced SOP Class UID</i>.
     */
    public String getRefSOPClassUID();
    
    /**
     * Returns the DICOM <i>Referenced SOP Instance UID</i>.
     * <br>DICOM Tag: <code>(0008,1155)</code>
     * Uniquely identifies the referenced SOP Instance.
     *
     * @return  the <i>Referenced SOP Instance UID</i>.
     */
    public String getRefSOPInstanceUID();
    
    public void toDataset(Dataset ds);
}//end interface RefSOP
