/*$Id: IconImage.java 3537 2002-08-05 15:22:20Z gunterze $*/
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
 * The <code>IconImage</code> interface represents a
 * <i>Icon Image</i>. Optional property of {@link ImageContent}. Use
 * {@link SRDocumentFactory#newIconImage} to <code>IconImage</code>
 * generate instances,
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.17.3 SR Document Content Module"
 *
 * @see "DICOM Part 3: Information Object Definitions,
 * Annex C.18.4 Image Reference Macro"
 */
public interface IconImage {
    // Constants -----------------------------------------------------
    
    // Public --------------------------------------------------------
    int getRows();

    int getColumns();

    byte[] getPixelData();

    void toDataset(Dataset ds);
}
