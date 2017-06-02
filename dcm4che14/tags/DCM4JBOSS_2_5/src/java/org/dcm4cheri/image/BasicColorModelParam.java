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

package org.dcm4cheri.image;

import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelParam;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @version $Revision: 3686 $ $Date: 2003-07-15 23:37:03 +0800 (周二, 15 7月 2003) $
 */
abstract class BasicColorModelParam
    implements ColorModelParam
{
    protected final int dataType;
    protected final int size;
    protected final int bits;
    protected final int min;
    protected final int max;
    protected final int shiftmask;
    protected final int alloc;
    protected final int hBit;

    protected BasicColorModelParam(Dataset ds)
    {
        alloc = ds.getInt(Tags.BitsAllocated, 8);
        if (alloc <= 8)
            dataType = DataBuffer.TYPE_BYTE;
        else if (alloc <= 16)
            dataType = DataBuffer.TYPE_USHORT;
        else if (alloc <= 32) //dataType = DataBuffer.TYPE_INT
            throw new IllegalArgumentException(alloc + " Bits Allocated not supported for Java BufferedImages");
        else
            throw new IllegalArgumentException("Bits allocated " + alloc + " not supported");
        bits = ds.getInt(Tags.BitsStored, 8);
        hBit = ds.getInt(Tags.HighBit, bits - 1);
        size = 1 << bits;
        if(ds.getInt(Tags.PixelRepresentation, 0) == 0) {
            min = 0;
            max = size;
        }
        else {
            min = -(size >> 1);
            max = -min - 1;
        }
        shiftmask = 32 - bits;
    }

    protected BasicColorModelParam(BasicColorModelParam other)
    {
        alloc = other.alloc;
        hBit = other.hBit;
        dataType = other.dataType;
        size = other.size;
        bits = other.bits;
        min = other.min;
        max = other.max;
        shiftmask = other.shiftmask;
    }

    public final int toSampleValue(int pxValue)
    {
        return min != 0 ? (pxValue << shiftmask) >> shiftmask : (pxValue << shiftmask) >>> shiftmask;
    }

    public final int toPixelValueRaw(int sampleValue)
    {
        int bsMask = (1 << bits) - 1;
        int packedValue = (sampleValue & bsMask) << (hBit - bits) + 1;
        return packedValue;
    }

    public abstract ColorModel newColorModel();

    public abstract ColorModelParam update(float f, float f1, boolean flag);

    public abstract float getRescaleSlope();

    public abstract float getRescaleIntercept();

    public abstract float getWindowCenter(int i);

    public abstract float getWindowWidth(int i);

    public abstract int getNumberOfWindows();

    public abstract float toMeasureValue(int i);

    public abstract int toPixelValue(float f);

    public abstract boolean isInverse();

    public abstract boolean isCacheable();
}
