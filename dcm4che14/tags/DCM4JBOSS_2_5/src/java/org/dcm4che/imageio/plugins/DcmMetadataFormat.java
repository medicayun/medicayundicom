/*$Id: DcmMetadataFormat.java 3499 2002-07-16 15:48:04Z gunterze $*/
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

import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmMetadataFormat extends IIOMetadataFormatImpl {

    private static IIOMetadataFormat instance = null;
    private static final String[] VRs = {
        "AE", "AS", "AT", "CS", "DA", "DS", "DT", "FL", "FD", "IS", "LO", "LT",
        "OB", "OW", "PN", "SH", "SL", "SQ", "SS", "ST", "TM", "UI", "UL", "UN",
        "US", "UT"
    };
    private static final List VR_LIST = Arrays.asList(VRs);

    private DcmMetadataFormat() {
        super(DcmMetadata.nativeMetadataFormatName,
              CHILD_POLICY_SOME);

        addElement("filemetainfo", DcmMetadata.nativeMetadataFormatName,
                CHILD_POLICY_REPEAT);
        addAttribute("filemetainfo", "preamble", DATATYPE_STRING, false, null); 

        addElement("dataset", DcmMetadata.nativeMetadataFormatName,
                CHILD_POLICY_REPEAT);

        addElement("elm", "dataset", CHILD_POLICY_CHOICE);
        addAttribute("elm", "pos", DATATYPE_INTEGER, true, null);
        addAttribute("elm", "tag", DATATYPE_STRING, true, null); 
        addAttribute("elm", "vr", DATATYPE_STRING, true, null, VR_LIST);
        addAttribute("elm", "name", DATATYPE_STRING, false, null);

        addChildElement("elm", "filemetainfo");
        
        addElement("val", "elm", CHILD_POLICY_EMPTY);
        addAttribute("val", "vm", DATATYPE_INTEGER, true, null); 
        addAttribute("val", "len", DATATYPE_INTEGER, true, null); 
        addAttribute("val", "data", DATATYPE_STRING, true, null); 

        addElement("seq", "elm", CHILD_POLICY_SEQUENCE);
        addAttribute("seq", "len", DATATYPE_INTEGER, true, null); 
        
        addElement("item", "seq", CHILD_POLICY_REPEAT);
        addAttribute("item", "pos", DATATYPE_INTEGER, true, null);
        addAttribute("item", "id", DATATYPE_INTEGER, true, null); 
        addAttribute("item", "len", DATATYPE_INTEGER, true, null); 

        addChildElement("elm", "item");
        
        addElement("frag", "seq", CHILD_POLICY_EMPTY);
        addAttribute("frag", "pos", DATATYPE_INTEGER, true, null);
        addAttribute("frag", "id", DATATYPE_INTEGER, true, null); 
        addAttribute("frag", "len", DATATYPE_INTEGER, true, null); 
        addAttribute("frag", "data", DATATYPE_STRING, true, null); 
    }

    public boolean canNodeAppear(String elementName,
                                 ImageTypeSpecifier imageType) {
        return true;
    }

    public static synchronized IIOMetadataFormat getInstance() {
        if (instance == null) {
            instance = new DcmMetadataFormat();
        }
        return instance;
    }
}
