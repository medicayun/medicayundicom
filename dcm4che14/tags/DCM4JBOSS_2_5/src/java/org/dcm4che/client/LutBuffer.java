/*
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4che.client;

import java.nio.ByteBuffer;

import org.dcm4che.dict.VRs;

/**
 * Wraps a <code>ByteBuffer</code> for a DICOM look-up table (LUT).
 * @author jforaci
 * @since Jul 29, 2003
 * @version $Revision: 3712 $ $Date: 2003-07-30 00:35:15 +0800 (周三, 30 7月 2003) $
 */
final class LutBuffer
{
    public static final int TYPE_BYTE = 0;
    public static final int TYPE_WORD = 1;

    private final ByteBuffer buff;
    private final int dataType;
    private final int lutSize, firstValueMapped, depth;

    /**
     * Creates a LutBuffer with the specified backing buffer. Note that
     * the value of the third descriptor (lut depth) determines whether the
     * size of the entries with be grabbed in bytes or words. The depth of the
     * LUT must be from 8 to 16 inclusive.
     * @param backend The backing <code>ByteBuffer</code> of the LUT.
     * @param descriptor Assumed to be <code>int</code>s with values that would
     *  be the sign-extended or unsigned value from a dataset, depending on the
     *  implied or explicit VR when reading the dataset.
     * @param vr Overrides the actual value (only for the second descriptor) to
     *  be interpreted as <code>vr</code>, which must be either <code>VRs.US</code>
     *  or <code>VRs.SS</code>.
     */
    public LutBuffer(ByteBuffer backend, int[] descriptor, int vr)
    {
        if (descriptor[2] <= 8)
            this.dataType = TYPE_BYTE;
        else if (descriptor[2] <= 16)
            this.dataType = TYPE_WORD;
        else
            throw new IllegalArgumentException("The LUT's depth must be within 8 and 16, inclusive");
        lutSize = (descriptor[0] == 0) ? (1 << 16) : descriptor[0] & 0xFFFF; //always US
        if (vr == VRs.US)
            firstValueMapped = descriptor[1] & 0xFFFF;
        else if (vr == VRs.SS)
            firstValueMapped = (descriptor[1] << 16) >> 16;
        else throw new IllegalArgumentException("VR may only be US or SS");
        depth = descriptor[2] & 0xFFFF; //always US
        this.buff = backend;
    }

    /**
     * Creates a LutBuffer with the specified backing buffer. The depth of the
     * LUT must be from 8 to 16 inclusive.
     * @param backend The backing <code>ByteBuffer</code> of the LUT.
     * @param lutSize The number of entries in the LUT
     * @param firstValueMapped The value mapped to the first entry of this LUT
     * @param depthInBits The depth of this LUT's entries
     */
    public LutBuffer(ByteBuffer backend, int lutSize, int firstValueMapped,
        int depthInBits)
    {
        if (depthInBits <= 8)
            this.dataType = TYPE_BYTE;
        else if (depthInBits <= 16)
            this.dataType = TYPE_WORD;
        else
            throw new IllegalArgumentException("The LUT's depth must be within 8 and 16, inclusive");
        this.lutSize = lutSize;
        this.firstValueMapped = firstValueMapped;
        this.depth = depthInBits;
        this.buff = backend;
    }

    /**
     * Creates a LutBuffer with the specified backing buffer and the data type
     * allocated for the LUT entries. The depth of the LUT must be from 8 to 16
     * inclusive.
     * @param backend The backing <code>ByteBuffer</code> of the LUT.
     * @param lutSize The number of entries in the LUT
     * @param firstValueMapped The value mapped to the first entry of this LUT
     * @param depthInBits The depth of this LUT's entries
     * @param dataType The actual data type allocated for each entry
     *  (<code>TYPE_BYTE</code> or <code>TYPE_WORD</code>)
     */
    public LutBuffer(ByteBuffer backend, int lutSize, int firstValueMapped,
        int depthInBits, int dataType)
    {
        if (dataType != TYPE_BYTE && dataType != TYPE_WORD)
            throw new IllegalArgumentException("Bad dataType");
        this.dataType = dataType;
        this.lutSize = lutSize;
        this.firstValueMapped = firstValueMapped;
        this.depth = depthInBits;
        this.buff = backend;
    }

    public int[] getDescriptor()
    {
        return new int[] {lutSize, firstValueMapped, depth};
    }

    public int getEntry(int index)
    {
        return (dataType == TYPE_BYTE)
            ? (int)(buff.get(index) & 0xFF)
            : (int)(buff.getShort(index * 2) & 0xFFFF);
    }

    public int getEntryFromInput(int value)
    {
        if (value <= firstValueMapped)
            return getEntry(0);
        else if (value - firstValueMapped >= lutSize)
            return getEntry(lutSize - 1);
        else
            return getEntry(value - firstValueMapped);
    }

    public int getDepth() {
        return depth;
    }

    public int getFirstValueMapped() {
        return firstValueMapped;
    }

    public int getLutSize() {
        return lutSize;
    }
}
