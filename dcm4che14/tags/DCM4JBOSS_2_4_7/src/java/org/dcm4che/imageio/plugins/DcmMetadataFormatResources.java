/*$Id: DcmMetadataFormatResources.java 3499 2002-07-16 15:48:04Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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

package org.dcm4che.imageio.plugins;

import java.util.ListResourceBundle;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmMetadataFormatResources extends ListResourceBundle {

    static final Object[][] contents = {
        // Node name, followed by description
        { "filemetainfo", "DICOM File Meta Information" },
        { "dataset", "Data Set with Data Elements, except the Pixel Data" },
        { "elm", "Data Element, contains single Value or Sequence" },
        { "val", "Value of Data Element" },
        { "seq", "Sequence of Items or Data Fragments" },
        { "item", "Sequence Item, contains nested Data Elements" },
        { "frag", "Data Fragment" },

        // Node name + "/" + AttributeName, followed by description
        { "filemetainfo/preamble", "128 byte File Preamble" },
        { "elm/pos", "offset of first byte of this attribute in stream" },
        { "elm/tag", "data element tag as 4 byte hex number" },
        { "elm/vr", "value representation" },
        { "elm/name", "attribute name" },
        { "val/vm", "number of entries in the value data attribute" },
        { "val/len", "value length" },
        { "val/data", "value data" },
        { "seq/len", "sequence length or -1 for undefined length" },
        { "item/pos", "offset of first byte of this item in stream" },
        { "item/id", "item index, starting with 1" },
        { "item/len", "item length or -1 for undefined length" },
        { "frag/pos", "offset of first byte of this fragment in stream" },
        { "frag/id", "data fragment index, starting with 1" },
        { "frag/len", "data fragment length" },
        { "frag/data", "fragment data" },
    };

    public DcmMetadataFormatResources() {}

    public Object[][] getContents() {
        return contents;
    }
}
