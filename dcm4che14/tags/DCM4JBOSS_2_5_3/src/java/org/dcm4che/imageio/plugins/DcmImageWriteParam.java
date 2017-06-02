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
package org.dcm4che.imageio.plugins;

import java.util.Locale;

import javax.imageio.ImageWriteParam;

import org.dcm4che.data.DcmEncodeParam;


/**
 * A class describing how a DICOM stream shold be encoded.
 *
 * @author   Thomas Hacklaender
 * @version  2002.06.16
 */
public abstract class DcmImageWriteParam extends ImageWriteParam {

  
  /**
   * Constructs an empty ImageWriteParam. It is up to the subclass to set up the 
   * instance variables properly.
   */
  public DcmImageWriteParam() {
    super();
  }

  
  /**
   * Constructs an ImageWriteParam set to use a given Locale.
   * @param locale a Locale to be used to localize compression type names and 
   *               quality descriptions, or null.
   */
  public DcmImageWriteParam(Locale locale) {
    super(locale);
  }
  
  
  /**
   * Set the property writeFMI.
   * @param writeFMI true, if the file-meta-information block (Part 10) should
   *                 be written.
   */
  public abstract void setWriteFMI(boolean writeFMI);
  
  
  /**
   * Returns the property writeFMI.
   * @return true, if the file-meta-information block (Part 10) should be written.
   *         The default value is true.
   */
  public abstract boolean isWriteFMI();
  
  public abstract void setDcmEncodeParameters(DcmEncodeParam dcmEncodeParams);
  public abstract DcmEncodeParam getDcmEncodeParameters();
  
  //public abstract void setPixelRepresentation(boolean signed);
  //public abstract boolean getPixelRepresentation();
  
  /**
   * Set the property monochrome2.
   * @param monochrome2 true, if the Photometric Interpretation should be 
   *                    MONOCHROME2. Otherwise it will be set to MONOCHROME1.
   */
  public abstract void setMONOCHROME2(boolean monochrome2);
  
  
  /**
   * Returns the property monochrome2.
   * @return true, if the Photometric Interpretation should be MONOCHROME2.
   *         The default value is true.
   */
  public abstract boolean isMONOCHROME2();
  
  
  /**
   * Set the property bitsStored.
   * The number of bits stored. Should be smaller than BitsAllocated (depending
   * on the Raster of the BufferedImage to write).
   * @param bitsStored the number of bits to store. A value of -1 denotes, that
   *                   the DcmImageWriter should choose the best value.
   */
  //public abstract void setBitsStored(int bitsStored);
  
  
  /**
   * Returns the property bitsStored.
   * @return the number of bits to store. A value of -1 denotes, that the
   *         DcmImageWriter should choose the best value.
   */
  //public abstract int getBitsStored();
  
  
  /**
   * Set the property highBit.
   * The highest bit position in the pixel data. Should be smaller than BitsStored. 
   * A value of -1 denotes, that the DcmImageWriter should choose the best value.
   * @param highBit the highest bit position in the pixel data. A value of -1 
   *                denotes, that the DcmImageWriter should choose the best value.
   */
  //public abstract void setHighBit(int highBit);
  
  
  /**
   * Returns the property highBit.
   * @return the highest bit position in the pixel data. A value of -1 denotes,
   *         that the DcmImageWriter should choose the best value.
   */
  //public abstract int getHighBit();
  
  
  /**
   * Set the property writeAlwaysRGB.
   * @param writeAlwaysRGB true, if all images should be written with Photometric 
   *                       Interpretation RGB.
   */
  public abstract void setWriteAlwaysRGB(boolean writeAlwaysRGB);
  
  
  /**
   * Returns the property writeAlwaysRGB.
   * @return true, if all images should be written with the Photometric 
   *         Interpretation RGB. The default value is false.
   */
  public abstract boolean isWriteAlwaysRGB();
  
  
  /**
   * Set the property writeIndexedAsRGB.
   * @param writeIndexedAsRGB true, if BufferedImages of type TYPE_BYTE_INDEXED  
   *                          should be written with the Photometric Interpretation RGB.
   */
  public abstract void setWriteIndexedAsRGB(boolean writeIndexedAsRGB);
  
  
  /**
   * Returns the property writeIndexedAsRGB.
   * @return true, if BufferedImages of type TYPE_BYTE_INDEXED  
   *         should be written with the Photometric Interpretation RGB.
   *         The default value is true.
   */
  public abstract boolean isWriteIndexedAsRGB();
  
}
