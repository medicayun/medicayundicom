/**
 * ***************************************************************************
 * *
 * Copyright (c) 2001 by IFTM Institut fuer Telematik in der Medizin GmbH   *
 * <thomas.hacklaender@iftm.de>                                             *
 * *
 * This file is a supplement to the dcm4che project at                      *
 * http://sourceforge.net/projects/dcm4che/                                 *
 * *
 * This library is free software; you can redistribute it and/or modify it  *
 * under the terms of the GNU Lesser General Public License as published    *
 * by the Free Software Foundation; either version 2 of the License, or     *
 * (at your option) any later version.                                      *
 * *
 * This library is distributed in the hope that it will be useful, but      *
 * WITHOUT ANY WARRANTY; without even the implied warranty of               *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 * Lesser General Public License for more details.                          *
 * *
 * You should have received a copy of the GNU Lesser General Public         *
 * License along with this library; if not, write to the Free Software      *
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 * http://www.gnu.org/copyleft/copyleft.html                                *
 * *
 * **************************************************************************
 */

package de.iftm.dcm4che.imageio.plugins;

import javax.imageio.*;
import javax.imageio.metadata.*;

/**
 * This class describes the structure of metadata documents of type <code>
 * de.iftm.NativeDcmImageMetadata_0.9</code>. The DTD of the metadata is as
 * follows:<br><br>
 * <code>
 * &lt;?xml version="1.0" encoding="UTF-8" standalone="no" ?&gt;<br>
 * <br>
 * &lt;!DOCTYPE de.iftm.NativeDcmImageMetadata_0.9 [<br>
 * <br>
 * &nbsp; &lt;!ELEMENT "de.iftm.NativeDcmImageMetadata_0.9" (Dimension?, Window?)&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;!-- Dimension element only included if --&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;!-- 1. column and row information are available in
 * base data --&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;!--&nbsp;&nbsp;&nbsp; Window element only included
 * if --&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;!-- 1. center and window information are available
 * in base data --&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;!-- 2. photometric interpretation is MONOCHROME1 or
 * MONOCHROME1 --&gt;<br>
 * <br>
 * &nbsp; &lt;!-- Dimension information --&gt;<br>
 * <br>
 * &nbsp; &lt;!ELEMENT "Dimension" EMPTY&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;!ATTLIST "Dimension" "columns" #CDATA #REQUIRED&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Columns of base image (before correction
 * of aspect ratio)--&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Data type: Integer --&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;!ATTLIST "Dimension" "rows" #CDATA #REQUIRED&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Rows of base image (before correction
 * of aspect ratio)--&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Data type: Integer --&gt;<br>
 * <br>
 * &nbsp; &lt;!-- Window information --&gt;<br>
 * <br>
 * &nbsp; &lt;!ELEMENT "Window" EMPTY&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;!ATTLIST "Window" "center" #CDATA #REQUIRED&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Current window center --&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Data type: Integer --&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;!ATTLIST "Window" "width" #CDATA #REQUIRED&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Current window width --&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Data type: Integer --&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;!ATTLIST "Window" "slope" #CDATA #REQUIRED&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Rescale slope for CT-images. 1.0 for
 * other images --&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Data type: Float --&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;!ATTLIST "Window" "intercept" #CDATA #REQUIRED&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Rescale intercept for CT-images. 0.0
 * for other images --&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Data type: Float --&gt;<br>
 * <br>
 * ]&gt;<br>
 * </code>
 * <br><br>
 * Bugs:<br>
 * No bugs known.<br>
 * <br>
 * Limitations:<br>
 * <br><br>
 * To Do:<br>
 * <br><br>
 * @author Thomas Hacklaender
 * @version 0.9, 2002.2.11
 * @since dcm4che Version 0.9.8
 */
public class SimpleDcmMetadataFormat extends IIOMetadataFormatImpl {

  /**
   * Create a single instance of this class (singleton pattern)
   */
  private static SimpleDcmMetadataFormat
        defaultInstance = new SimpleDcmMetadataFormat();

  
  /**
   * Make the constructor private to enforce the singleton pattern
   */
  private SimpleDcmMetadataFormat() {
    super("de.iftm.SimpleDcmMetadata_0.9", CHILD_POLICY_REPEAT);
    
    // Noch nicht implementiert
    throw new UnsupportedOperationException();
  }

  
  /**
   * Returns true if the element (and the subtree below it) is allowed to appear 
   * in a metadata document for an image of the given type, defined by an 
   * ImageTypeSpecifier. For example, a metadata document format might contain 
   * an element that describes the primary colors of the image, which would not 
   * be allowed when writing a grayscale image.
   * @param elementName the name of the element being queried.
   * @param imageType an ImageTypeSpecifier indicating the type of the image 
   *        that will be associated with the metadata.
   * @return true if the node is meaningful for images of the given type.
   */
  public boolean canNodeAppear (String elementName, ImageTypeSpecifier imageType) {
    
    // Noch nicht implementiert
    throw new UnsupportedOperationException();
  }
  
  
  /**
   * Return the singleton instance.
   */
  public static SimpleDcmMetadataFormat getDefaultInstance() {
    return defaultInstance;
  }
  
}
