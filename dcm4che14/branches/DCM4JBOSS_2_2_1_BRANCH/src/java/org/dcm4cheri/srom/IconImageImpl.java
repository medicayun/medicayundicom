/*$Id: IconImageImpl.java 3536 2002-08-05 14:16:48Z gunterze $*/
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

package org.dcm4cheri.srom;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;

import org.dcm4che.srom.IconImage;

import java.util.Arrays;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class IconImageImpl implements IconImage {
    
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private final int rows;
    private final int columns;
    private final byte[] pixeldata;

    // Constructors --------------------------------------------------
    public IconImageImpl(int rows, int columns, byte[] pixeldata) {
        if (rows <= 0) {
            throw new IllegalArgumentException("Rows: " + rows);
        }
    
        if (columns <= 0) {
            throw new IllegalArgumentException("Columns: " + columns);
        }
        
        if (pixeldata != null && pixeldata.length != rows * columns) {
            throw new IllegalArgumentException("Length of Pixel Data ["
                + pixeldata.length + "] do not match Rows [" + rows
                + "] x Columns [" + columns + "]");
        }
    
        this.rows = rows;
        this.columns = columns;
        this.pixeldata = pixeldata != null
            ? pixeldata
            : new byte[rows * columns];
    }

    public IconImageImpl(Dataset ds) throws DcmValueException {
        this(ds.getInt(Tags.Rows, -1), ds.getInt(Tags.Columns, -1),
            ds.getByteBuffer(Tags.PixelData).array());
    }
    
    public static IconImage newIconImage(Dataset ds) throws DcmValueException {
        return ds != null ? new IconImageImpl(ds) : null;
    }
    
    // Public --------------------------------------------------------
    public String toString() {
        return "Icon[" + columns + "x" + rows + "]"; 
    }

    public void toDataset(Dataset ds) {
        ds.putUS(Tags.SamplesPerPixel, 1);
        ds.putCS(Tags.PhotometricInterpretation, "MONOCHROME2");
        ds.putUS(Tags.Rows, rows);
        ds.putUS(Tags.Columns, columns);
        ds.putUS(Tags.BitsAllocated, 8);
        ds.putUS(Tags.BitsStored, 8);
        ds.putUS(Tags.HighBit, 7);
        ds.putUS(Tags.PixelPresentation, 0);
        ds.putOB(Tags.PixelData, pixeldata);
    }    
    
    public final boolean equals(Object o) {
        if (o == this)
            return true;
        
        if (!(o instanceof IconImageImpl))
            return false;
    
        IconImageImpl icon = (IconImageImpl)o;
        return icon.rows == rows && icon.columns == columns
            && Arrays.equals(icon.pixeldata, pixeldata);
    }
    
    public final int getRows() {
        return rows;
    }
    
    public final int getColumns() {
        return columns;
    }
    
    public final byte[] getPixelData() {
        return pixeldata;
    }
    
}
