/*$Id: PaletteColorParam.java 3860 2005-02-08 17:35:08Z gunterze $*/
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

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelParam;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class PaletteColorParam extends BasicColorModelParam {
    
    private final byte[] r,g,b;

    /** Creates a new instance of PaletteColorParam */
    public PaletteColorParam(Dataset ds) {
        super(ds);
        if (super.min < 0) {
            throw new UnsupportedOperationException(
                    "Signed PALETTE COLOR not supported!");
        }
        this.r = generate(size, ds, Tags.RedPaletteColorLUTDescriptor,
                Tags.RedPaletteColorLUTData,
                Tags.SegmentedRedPaletteColorLUTData);
        this.g = generate(size, ds, Tags.GreenPaletteColorLUTDescriptor,
                Tags.GreenPaletteColorLUTData,
                Tags.SegmentedGreenPaletteColorLUTData);
        this.b = generate(size, ds, Tags.BluePaletteColorLUTDescriptor,
                Tags.BluePaletteColorLUTData,
                Tags.SegmentedBluePaletteColorLUTData);
    }

    public ColorModel newColorModel() {
        return new IndexColorModel(bits, size, r, g, b);
    }
    
    private static void throwLengthMismatch(int lutLen, int descLen) {
        throw new IllegalArgumentException("LUT Data length: " + lutLen
                +  " mismatch entry value: " + descLen + " in LUT Descriptor");
    }
        
    private static byte[] generate(int size, Dataset ds, int descTag,
                int dataTag, int segmTag) {
        int[] desc = ds.getInts(descTag);
        if (desc == null) {
            throw new IllegalArgumentException("Missing LUT Descriptor!");
        }
        if (desc.length != 3) {
            throw new IllegalArgumentException("Illegal LUT Descriptor: " + desc);
        }
        int len = desc[0] == 0 ? 0x10000 : desc[0];
        int off = desc[1];
        if (len < 0)
            throw new IllegalArgumentException("Illegal LUT Descriptor: len=" + len);
        if (off < 0)
            throw new IllegalArgumentException("off: " + off);
        ByteBuffer data = ds.getByteBuffer(dataTag);
        ByteBuffer segm = ds.getByteBuffer(segmTag);

        if (data == null && segm == null)
            throw new IllegalArgumentException("Missing LUT Data!");

//        if (data != null && segm != null)
//            throw new IllegalArgumentException("Native & Segmented LUT Data!");

        byte[] out = new byte[size];
        switch (desc[2]) {
            case 16:
                if (data != null) {
                    if (data.limit() != len * 2) {
                        throwLengthMismatch(data.limit(), len);
                    }
                    data.rewind();
                    for (int i = off; data.hasRemaining(); ++i) {
                        out[i] = (byte)(data.getShort() >> 8);
                    }
                } else {
                    inflate(segm, out, off, len);
                }
                break;
            case 8:
                if (data != null) {
                    if (data.limit() != len) {
                        throwLengthMismatch(data.limit(), len);
                    }
                    data.rewind();
                    short tmp;
                    for (int i = off; data.hasRemaining(); ) {
                        tmp = data.getShort();
                        out[i++] = (byte)(tmp & 0xff);
                        out[i++] = (byte)((tmp >> 8) & 0xff);
                    }
                    break;
                }
            default:
                throw new IllegalArgumentException (
                    "Illegal LUT Descriptor: bits=" + desc[2]);
        }
        Arrays.fill(out, 0, off, out[off]);
        Arrays.fill(out, off + len, size, out[off + len - 1]);
        return out;
    }

    private static void inflate(ByteBuffer segm, byte[] out, int off, int len) {
        int x0 = off;
        int y0 = 0;
        int y1,dy;
        segm.rewind();
        while (segm.hasRemaining()) {
            int op = segm.getShort();
            int n = segm.getShort() & 0xffff;
            switch (op) {
                case 0:
                    for (int j = 0; j < n; ++j) {
                        out[x0++] = (byte)((y0 = segm.getShort() & 0xffff) >> 8);
                    }
                    break;
                case 1:
                    y1 = segm.getShort() & 0xffff;
                    dy = y1 - y0;
                    for (int j = 0; j < n;) {
                        out[x0++] = (byte)((y0 + dy * ++j / n)>>8);
                    }
                    y0 = y1;
                    break;
                case 2:
                    int pos = (segm.getShort() & 0xffff)
                            | (segm.getShort() << 16);
                    segm.mark();
                    segm.position(pos);
                    for (int j = 0; j < n; ++j) {
                        int op2 = segm.getShort();
                        int n2 = segm.getShort() & 0xffff;
                        switch (op2) {
                            case 0:
                                for (int j2 = 0; j2 < n2; ++j2) {
                                    out[x0++] = (byte)((y0 = segm.getShort()
                                            & 0xffff) >> 8);
                                }
                                break;
                            case 1:
                                y1 = segm.getShort() & 0xffff;
                                dy = y1 - y0;
                                for (int j2 = 0; j2 < n2;) {
                                    out[x0++] = (byte)((y0 + dy*++j2 / n2)>>8);
                                }
                                y0 = y1;
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "illegal op code:" + op2
                                        + ", index:" + (segm.position()-4));
                        }
                    }
                    segm.reset();
                    break;
                default:
                    throw new IllegalArgumentException("illegal op code:" + op
                            + ", index:" + (segm.position()-4));
            }
        }
        if (x0 - off != len) {
            throwLengthMismatch(x0 - off, len);
        }
    }
    
    public ColorModelParam update(float center, float width, boolean inverse) {
        throw new UnsupportedOperationException();
    }
    
    public float getRescaleSlope() {
        throw new UnsupportedOperationException();
    }
    
    public float getRescaleIntercept() {
        throw new UnsupportedOperationException();
    }

    public float getWindowCenter(int index) {
        throw new UnsupportedOperationException();
    }
    
    public float getWindowWidth(int index) {
        throw new UnsupportedOperationException();
    }
    
    public int getNumberOfWindows() {
        throw new UnsupportedOperationException();
    }
    
    public boolean isMonochrome() {
        return false;
    }
    
    public boolean isCacheable() {
        return false;
    }
    
    public boolean isInverse() {
        return false;
    }
    
    public float toMeasureValue(int pxValue) {
        throw new UnsupportedOperationException();
    }
    
    public int toPixelValue(float measureValue) {
        throw new UnsupportedOperationException();
    }
}
