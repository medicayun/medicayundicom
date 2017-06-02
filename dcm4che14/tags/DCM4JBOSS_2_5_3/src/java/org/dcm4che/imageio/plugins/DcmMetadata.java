/*$Id: DcmMetadata.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.TagDictionary;

import org.w3c.dom.Node;

/**
 * ImageIO Meta Data for DICOM image.
 * Change history:<br>
 * 2002.06.13, Thomas Hacklaender: Method setDataset(Dataset ds) added.<br>
 * 2002.06.16, Thomas Hacklaender: Methods reset(), setFromTree(String formatName, 
 * Node root) and mergeTree(String formatName, Node root) added. Method 
 * isReadOnly() modified.<br>
 * <br>
 * @author  gunter.zeilinger@tiani.com
 * @author Thomas Hacklaender
 * @version 2002.06.13
 */
public abstract class DcmMetadata extends javax.imageio.metadata.IIOMetadata {

    public static final String nativeMetadataFormatName = 
            "dcm4che_imageio_dicom_1.0";

    protected DcmMetadata(boolean standardMetadataFormatSupported,
            String[] extraMetadataFormatNames,
            String[] extraMetadataFormatClassNames) {
        super(standardMetadataFormatSupported, 
              nativeMetadataFormatName,
              "org.dcm4che.imageio.plugins.DcmMetadataFormat",
              extraMetadataFormatNames,
              extraMetadataFormatClassNames);
    }

    public abstract Dataset getDataset();
    
    /**
     * Sets a new Dataset as a base for the metadata.
     * @param ds the new Dataset.
     */
    public abstract void setDataset(Dataset ds);

    public abstract void setDictionary(TagDictionary dict);
    
    /**
     * Sets the internal state of this IIOMetadata object from a tree of XML 
     * DOM Nodes whose syntax is defined by the given metadata format. The 
     * previous state is discarded. If the tree's structure or contents are 
     * invalid, an IIOInvalidTreeException will be thrown.
     * @param formatName the desired metadata format.
     * @param root an XML DOM Node object forming the root of a tree.
     * @throws IllegalStateException if this object is read-only.
     * @throws IllegalArgumentException if formatName is null or is not one of 
     *         the names returned by getMetadataFormatNames.
     * @throws IIOInvalidTreeException if the tree cannot be parsed successfully 
     *         using the rules of the given format.
     */
    public abstract void setFromTree(String formatName, Node root);
    
    /**
     * Alters the internal state of this IIOMetadata object from a tree of 
     * XML DOM Nodes whose syntax is defined by the given metadata format. The 
     * previous state is altered only as necessary to accomodate the nodes that 
     * are present in the given tree. If the tree structure or contents are 
     * invalid, an IIOInvalidTreeException will be thrown.
     * @param formatName the desired metadata format.
     * @param root an XML DOM Node object forming the root of a tree.
     * @throws IllegalStateException if this object is read-only.
     * @throws IllegalArgumentException if formatName is null or is not one of 
     *         the names returned by getMetadataFormatNames.
     * @throws IIOInvalidTreeException if the tree cannot be parsed successfully 
     *         using the rules of the given format.
     */
    public abstract void mergeTree(String formatName, Node root);
    
    /**
     * Resets all the data stored in this object to default values to the state 
     * this object was in immediately after construction.
     * @since 1.0.0
     */
    public abstract void reset();
    
}
