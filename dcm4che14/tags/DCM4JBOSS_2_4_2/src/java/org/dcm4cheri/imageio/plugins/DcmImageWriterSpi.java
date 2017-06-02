/*
 * Copyright (C) 2002 Thomas Hacklaender, mailto:hacklaender@iftm.de
 *
 * IFTM Institut fuer Telematik in der Medizin GmbH, www.iftm.de
 *
 *  This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * http://www.gnu.org/copyleft/copyleft.html
 */
package org.dcm4cheri.imageio.plugins;

import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.spi.*;


/**
 *
 * @author   Thomas Hacklaender
 * @version  2002.6.16
 */
public class DcmImageWriterSpi extends ImageWriterSpi {
  
  static final private String      vendorName = "IFTM GmbH";
  static final private String      version = "1.0";
  static final private String[]    names = { "DICOM" };
  static final private String[]    suffixes = { "dcm" };
  static final private String[]    MIMETypes = { "Application/dicom" };
  static final private String      writerClassName = "org.dcm4cheri.imageio.plugins.DcmImageWriter";
  static final private Class[]     outputTypes = { ImageOutputStream.class };
  static final private String[]    readerSpiNames = null;
  static final private boolean     supportsStandardStreamMetadataFormat = false;
  static final private String      nativeStreamMetadataFormatName = org.dcm4che.imageio.plugins.DcmMetadata.nativeMetadataFormatName;
  static final private String      nativeStreamMetadataFormatClassName = "org.dcm4che.imageio.plugins.DcmMetadataFormat";
  static final private String[]    extraStreamMetadataFormatNames = null;  // ????
  static final private String[]    extraStreamMetadataFormatClassNames = null; // ????
  static final private boolean     supportsStandardImageMetadataFormat = false;
  static final private String      nativeImageMetadataFormatName = null;
  static final private String      nativeImageMetadataFormatClassName = null;
  static final private String[]    extraImageMetadataFormatNames = null;
  static final private String[]    extraImageMetadataFormatClassNames = null;
  
  
  /**
   * Constructs a blank ImageWriterSpi.
   */
  public DcmImageWriterSpi() {
    super(
      vendorName,
      version,
      names,
      suffixes,
      MIMETypes,
      writerClassName,
      outputTypes,
      readerSpiNames,
      supportsStandardStreamMetadataFormat,
      nativeStreamMetadataFormatName,
      nativeStreamMetadataFormatClassName,
      extraStreamMetadataFormatNames,
      extraStreamMetadataFormatClassNames,
      supportsStandardImageMetadataFormat,
      nativeImageMetadataFormatName,
      nativeImageMetadataFormatClassName,
      extraImageMetadataFormatNames,
      extraImageMetadataFormatClassNames 
    );
  }
  
  
  /**
   * Returns true if the ImageWriter implementation associated with this service 
   * provider is able to encode an image with the given layout. The layout (i.e., 
   * the image's SampleModel and ColorModel) is described by an ImageTypeSpecifier 
   * object.<br>
   * A return value of true is not an absolute guarantee of successful encoding; 
   * the encoding process may still produce errors due to factors such as I/O 
   * errors, inconsistent or malformed data structures, etc. The intent is that 
   * a reasonable inspection of the basic structure of the image be performed in 
   * order to determine if it is within the scope of the encoding format.<br>
   * @param type an ImageTypeSpecifier specifying the layout of the image to be 
   *             written.
   * @return allways true.
   * @throws IllegalArgumentException if type is null.
   */
  public boolean canEncodeImage(ImageTypeSpecifier type)
      throws IllegalArgumentException
  {
    return true;
  }
  
  
  /**
   * Returns an instance of the ImageWriter implementation associated with this 
   * service provider. The returned object will initially be in an initial state 
   * as if its reset method had been called.<br>
   * @param extension a plug-in specific extension object, which may be null. This
   *                  implementation does not support any extensions.
   * @return an ImageWriter instance.
   * @throws IOException if the attempt to instantiate the writer fails.
   * @throws IllegalArgumentException if the ImageWriter's constructor throws an 
   *         IllegalArgumentException to indicate that the extension object is 
   *         unsuitable.
   */
  public ImageWriter createWriterInstance(Object extension)
    throws IOException
  {
        return new DcmImageWriter(this);
  }
  
  
  /**
   * Returns the Locale associated with this writer.
   * @return the Locale.
   */
  public String getDescription(Locale locale)
  {
    return "DICOM image writer";
  }
  
}
