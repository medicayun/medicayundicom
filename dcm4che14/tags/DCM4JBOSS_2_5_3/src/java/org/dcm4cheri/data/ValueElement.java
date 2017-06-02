/*  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
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
package org.dcm4cheri.data;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.WeakHashMap;

import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author     <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since      May, 2002
 * @version    $Revision: 3845 $ $Date: 2004-12-19 03:17:35 +0800 (周日, 19 12月 2004) $
 */
abstract class ValueElement extends DcmElementImpl {
    
    private static final WeakHashMap whm = new WeakHashMap();
    
    protected final ByteBuffer data;

    ValueElement(int tag, ByteBuffer data) {
        super(tag);
        this.data = data;
    }

    public int hashCode() {
        if (data == null || data.limit() == 0)
            return tag;
//        data.rewind();
        return tag ^ data.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ValueElement))
            return false;
        ValueElement ve = (ValueElement) o;
        if (tag != ve.tag)
            return false;
        if (data == null || data.limit() == 0)
            return (ve.data == null || ve.data.limit() == 0);
        if (ve.data == null || ve.data.limit() == 0)
            return false;
//        data.rewind();
//        ve.data.rewind();
        return data.equals(ve.data);
    }
    
    public DcmElement share() {
        WeakReference wr = (WeakReference) whm.get(this);
        if (wr != null) {
            DcmElement e = (DcmElement) wr.get();
            if (e != null)
                return e;
        }
        streamPos = -1L;
        whm.put(this, new WeakReference(this));
        return this;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public final int length() {
        return ((data.limit() + 1) & (~1));
    }

    /**
     *  Gets the byteBuffer attribute of the ValueElement object
     *
     * @return    The byteBuffer value
     */
    public final ByteBuffer getByteBuffer() {
        return data.duplicate().order(data.order());
    }

    /**
     *  Gets the byteBuffer attribute of the ValueElement object
     *
     * @param  byteOrder  Description of the Parameter
     * @return            The byteBuffer value
     */
    public final ByteBuffer getByteBuffer(ByteOrder byteOrder) {
        if (data.order() != byteOrder) {
            swapOrder();
        }
        return getByteBuffer();
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public int vm() {
        return data.limit() == 0 ? 0 : 1;
    }

    /**
     *  Gets the string attribute of the ValueElement object
     *
     * @param  index                  Description of the Parameter
     * @param  cs                     Description of the Parameter
     * @return                        The string value
     * @exception  DcmValueException  Description of the Exception
     */
    public String getString(int index, Charset cs) throws DcmValueException {
        if (index >= vm()) {
            return null;
        }
        return String.valueOf(getInt(index));
    }

    /**
     *  Gets the strings attribute of the ValueElement object
     *
     * @param  cs                     Description of the Parameter
     * @return                        The strings value
     * @exception  DcmValueException  Description of the Exception
     */
    public String[] getStrings(Charset cs) throws DcmValueException {
        String[] a = new String[vm()];
        for (int i = 0; i < a.length; ++i) {
            a[i] = String.valueOf(getInt(i));
        }
        return a;
    }

    /**  Description of the Method */
    protected void swapOrder() {
        data.order(swap(data.order()));
    }

    // SS, US -------------------------------------------------------------
    private static ByteBuffer setShort(int v) {
        return ByteBuffer.wrap(new byte[2]).order(
                ByteOrder.LITTLE_ENDIAN).putShort(0, (short)v);
    }

    private static ByteBuffer setShorts(int[] a) {
        if (a.length == 0) {
            return EMPTY_VALUE;
        }

        if (a.length == 1) {
            return setShort(a[0]);
        }

        ByteBuffer bb =
            ByteBuffer.wrap(new byte[a.length << 1]).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            bb.putShort((short) a[i]);
        }
        bb.rewind();
        return bb;
    }

    private final static class SS extends ValueElement {
        SS(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x5353;
        }

        public final int vm() {
            return data.limit() >> 1;
        }

        public final int getInt(int index) {
            if (index >= vm()) {
                return 0;
            }
            return data.getShort(index << 1);
        }

        public final int[] getInts() {
            int[] a = new int[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getInt(i);
            }
            return a;
        }

        protected void swapOrder() {
            swapWords(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            Charset keyCS,
            Charset dsCS) {
            int v;
            try {
                v = key.getInt();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = vm(); i < n; ++i) {
                if (getInt(i) == v) {
                    return true;
                }
            }
            return false;
        }

    }

    static DcmElement createSS(int tag, ByteBuffer data) {
        if ((data.limit() & 1) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " SS #"
                    + data.limit());
            return new SS(tag, EMPTY_VALUE);
        }
        return new SS(tag, data);
    }

    static DcmElement createSS(int tag) {
        return new SS(tag, EMPTY_VALUE);
    }

    static DcmElement createSS(int tag, int v) {
        return new SS(tag, setShort(v));
    }

    static DcmElement createSS(int tag, int[] a) {
        return new SS(tag, setShorts(a));
    }

    private final static class US extends ValueElement {
        US(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x5553;
        }

        public final int vm() {
            return data.limit() >> 1;
        }

        public final int getInt(int index) {
            if (index >= vm()) {
                return 0;
            }
            return data.getShort(index << 1) & 0xffff;
        }

        public final int[] getInts() {
            int[] a = new int[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getInt(i);
            }
            return a;
        }

        protected void swapOrder() {
            swapWords(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            Charset keyCS,
            Charset dsCS) {
            int v;
            try {
                v = key.getInt();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = vm(); i < n; ++i) {
                if (getInt(i) == v) {
                    return true;
                }
            }
            return false;
        }
    }

    static DcmElement createUS(int tag, ByteBuffer data) {
        if ((data.limit() & 1) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " US #"
                    + data.limit());
            return new US(tag, EMPTY_VALUE);
        }
        return new US(tag, data);
    }

    static DcmElement createUS(int tag) {
        return new US(tag, EMPTY_VALUE);
    }

    static DcmElement createUS(int tag, int s) {
        return new US(tag, setShort(s));
    }

    static DcmElement createUS(int tag, int[] s) {
        return new US(tag, setShorts(s));
    }

    // SL, UL -------------------------------------------------------------
    private static ByteBuffer setInt(int v) {
        return ByteBuffer.wrap(new byte[4]).order(
            ByteOrder.LITTLE_ENDIAN).putInt(0, v);
    }

    private static ByteBuffer setInts(int[] a) {
        if (a.length == 0) {
            return EMPTY_VALUE;
        }

        if (a.length == 1) {
            return setInt(a[0]);
        }

        ByteBuffer bb =
            ByteBuffer.wrap(new byte[a.length << 2]).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            bb.putInt(a[i]);
        }
        bb.rewind();
        return bb;
    }

    private abstract static class Int extends ValueElement {
        Int(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int getInt(int index) {
            if (index >= vm()) {
                return 0;
            }
            return data.getInt(index << 2);
        }

        public final int[] getInts() {
            int[] a = new int[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getInt(i);
            }
            return a;
        }

        public final int vm() {
            return data.limit() >> 2;
        }

        protected void swapOrder() {
            swapInts(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            Charset keyCS,
            Charset dsCS) {
            int v;
            try {
                v = key.getInt();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = vm(); i < n; ++i) {
                if (getInt(i) == v) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class SL extends Int {
        SL(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x534C;
        }
    }

    static DcmElement createSL(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " SL #"
                    + data.limit());
            return new SL(tag, EMPTY_VALUE);
        }
        return new SL(tag, data);
    }

    static DcmElement createSL(int tag) {
        return new SL(tag, EMPTY_VALUE);
    }

    static DcmElement createSL(int tag, int v) {
        return new SL(tag, setInt(v));
    }

    static DcmElement createSL(int tag, int[] a) {
        return new SL(tag, setInts(a));
    }

    static class UL extends Int {
        UL(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x554C;
        }

        public String getString(int index, Charset cs) {
            if (index >= super.vm()) {
                return null;
            }
            return String.valueOf(getInt(index) & 0xFFFFFFFFL);
        }

        public String[] getStrings(Charset cs) throws DcmValueException {
            String[] a = new String[super.vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = String.valueOf(getInt(i) & 0xFFFFFFFFL);
            }
            return a;
        }
    }

    static DcmElement createUL(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " UL #"
                    + data.limit());
            return new UL(tag, EMPTY_VALUE);
        }
        return new UL(tag, data);
    }

    static DcmElement createUL(int tag) {
        return new UL(tag, EMPTY_VALUE);
    }

    static DcmElement createUL(int tag, int v) {
        return new UL(tag, setInt(v));
    }

    static DcmElement createUL(int tag, int[] a) {
        return new UL(tag, setInts(a));
    }

    // AT -------------------------------------------------------------
    private static ByteBuffer setTag(int v) {
        return ByteBuffer
            .wrap(new byte[4])
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(0, (short) (v >> 8))
            .putShort(2, (short) v);
    }

    private static ByteBuffer setTags(int[] a) {
        if (a.length == 0) {
            return EMPTY_VALUE;
        }

        if (a.length == 1) {
            return setTag(a[0]);
        }

        ByteBuffer bb =
            ByteBuffer.wrap(new byte[a.length << 2]).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            bb.putShort((short) (a[i] >> 16)).putShort((short) a[i]);
        }
        bb.rewind();
        return bb;
    }

    private final static class AT extends ValueElement {
        AT(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x4154;
        }

        public final int vm() {
            return data.limit() >> 2;
        }

        public final int getTag(int index) {
            if (index >= vm()) {
                return 0;
            }
            final int pos = index << 2;
            return (
                (data.getShort(pos) << 16) | (data.getShort(pos + 2) & 0xffff));
        }

        public final int[] getTags() {
            int[] a = new int[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getTag(i);
            }
            return a;
        }

        public String getString(int index, Charset cs)
            throws DcmValueException {
            if (index >= vm()) {
                return null;
            }
            return Tags.toHexString(getTag(index), 8);
        }

        public String[] getStrings(Charset cs) throws DcmValueException {
            String[] a = new String[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getString(i, null);
            }
            return a;
        }

        protected void swapOrder() {
            swapWords(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            Charset keyCS,
            Charset dsCS) {
            int v;
            try {
                v = key.getTag();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = vm(); i < n; ++i) {
                if (getTag(i) == v) {
                    return true;
                }
            }
            return false;
        }
    }

    static DcmElement createAT(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " AT #"
                    + data.limit());
            return new AT(tag, EMPTY_VALUE);
        }
        return new AT(tag, data);
    }

    static DcmElement createAT(int tag) {
        return new AT(tag, EMPTY_VALUE);
    }

    static DcmElement createAT(int tag, int v) {
        return new AT(tag, setTag(v));
    }

    static DcmElement createAT(int tag, int[] a) {
        return new AT(tag, setTags(a));
    }

    // FL -------------------------------------------------------------
    private static ByteBuffer setFloat(float v) {
        return ByteBuffer.wrap(new byte[4]).order(
            ByteOrder.LITTLE_ENDIAN).putFloat(0, v);
    }

    private static ByteBuffer setFloats(float[] a) {
        if (a.length == 0) {
            return EMPTY_VALUE;
        }

        if (a.length == 1) {
            return setFloat(a[0]);
        }

        ByteBuffer bb =
            ByteBuffer.wrap(new byte[a.length << 2]).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            bb.putFloat(a[i]);
        }
        bb.rewind();
        return bb;
    }

    private final static class FL extends ValueElement {
        FL(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vm() {
            return data.limit() >> 2;
        }

        public final int vr() {
            return 0x464C;
        }

        public final float getFloat(int index) {
            if (index >= vm()) {
                return 0.f;
            }
            return data.getFloat(index << 2);
        }

        public final float[] getFloats() {
            float[] a = new float[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getFloat(i);
            }
            return a;
        }

        public String getString(int index, Charset cs) {
            if (index >= vm()) {
                return null;
            }
            return String.valueOf(getFloat(index));
        }

        public String[] getStrings(Charset cs) throws DcmValueException {
            String[] a = new String[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = String.valueOf(getFloat(i));
            }
            return a;
        }

        protected void swapOrder() {
            swapInts(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            Charset keyCS,
            Charset dsCS) {
            float v;
            try {
                v = key.getFloat();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = vm(); i < n; ++i) {
                if (getFloat(i) == v) {
                    return true;
                }
            }
            return false;
        }
    }

    static DcmElement createFL(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " FL #"
                    + data.limit());
            return new FL(tag, EMPTY_VALUE);
        }

        return new FL(tag, data);
    }

    static DcmElement createFL(int tag) {
        return new FL(tag, EMPTY_VALUE);
    }

    static DcmElement createFL(int tag, float v) {
        return new FL(tag, setFloat(v));
    }

    static DcmElement createFL(int tag, float[] a) {
        return new FL(tag, setFloats(a));
    }

    // FD -------------------------------------------------------------
    private static ByteBuffer setDouble(double v) {
        return ByteBuffer.wrap(new byte[8]).order(
            ByteOrder.LITTLE_ENDIAN).putDouble(0, v);
    }

    private static ByteBuffer setDoubles(double[] a) {
        if (a.length == 0) {
            return EMPTY_VALUE;
        }

        if (a.length == 1) {
            return setDouble(a[0]);
        }

        ByteBuffer bb =
            ByteBuffer.wrap(new byte[a.length << 3]).order(
                ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            bb.putDouble(a[i]);
        }
        bb.rewind();
        return bb;
    }

    private final static class FD extends ValueElement {
        FD(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vm() {
            return data.limit() >>> 3;
        }

        public final int vr() {
            return 0x4644;
        }

        public final double getDouble(int index) {
            if (index >= vm()) {
                return 0.;
            }
            return data.getDouble(index << 3);
        }

        public final double[] getDoubles() {
            double[] a = new double[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getDouble(i);
            }
            return a;
        }

        public String getString(int index, Charset cs) {
            if (index >= vm()) {
                return null;
            }
            return String.valueOf(getDouble(index));
        }

        public String[] getStrings(Charset cs) throws DcmValueException {
            String[] a = new String[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = String.valueOf(getDouble(i));
            }
            return a;
        }

        protected void swapOrder() {
            swapLongs(data);
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
			boolean ignoreEmpty,
            Charset keyCS,
            Charset dsCS) {
            double v;
            try {
                v = key.getDouble();
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            for (int i = 0, n = vm(); i < n; ++i) {
                if (getDouble(i) == v) {
                    return true;
                }
            }
            return false;
        }
    }

    static DcmElement createFD(int tag, ByteBuffer data) {
        if ((data.limit() & 7) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " FD #"
                    + data.limit());
            return new FD(tag, EMPTY_VALUE);
        }
        return new FD(tag, data);
    }

    static DcmElement createFD(int tag) {
        return new FD(tag, EMPTY_VALUE);
    }

    static DcmElement createFD(int tag, double v) {
        return new FD(tag, setDouble(v));
    }

    static DcmElement createFD(int tag, double[] a) {
        return new FD(tag, setDoubles(a));
    }

    // OF, OW, OB, UN -------------------------------------------------------------

    private final static class OF extends ValueElement {
        OF(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x4F46;
        }

        public final float getFloat(int index) {
            final int pos = index << 2;
            if (pos >= data.limit()) {
                return 0.f;
            }
            return data.getFloat(pos);
        }

        public final float[] getFloats() {
            float[] a = new float[data.limit() >> 2];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getFloat(i);
            }
            return a;
        }

        public String getString(int index, Charset cs) {
            return getBoundedString(Integer.MAX_VALUE, index, cs);
        }

        public String getBoundedString(int maxLen, int index, Charset cs) {
            if (index >= vm()) {
                return null;
            }
            return StringUtils.promptOF(data, maxLen);
        }

        public String[] getStrings(Charset cs) throws DcmValueException {
            return getBoundedStrings(Integer.MAX_VALUE, cs);
        }

        public String[] getBoundedStrings(int maxLen, Charset cs) {
            String[] a = new String[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getBoundedString(maxLen, i, null);
            }
            return a;
        }

        protected void swapOrder() {
            swapInts(data);
        }
    }

    static DcmElement createOF(int tag) {
        return new OF(tag, EMPTY_VALUE);
    }

    static DcmElement createOF(int tag, float[] v) {
        ByteBuffer buf = ByteBuffer.allocate(v.length << 2);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < v.length; ++i) {
            buf.putFloat(v[i]);
        }
        buf.rewind();
        return new OF(tag, buf);
    }

    static DcmElement createOF(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " OW #"
                    + data.limit());
            return new OF(tag, EMPTY_VALUE);
        }
        return new OF(tag, data);
    }

    private final static class OW extends ValueElement {
        OW(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x4F57;
        }

        public final int getInt(int index) {
            final int pos = index << 1;
            if (pos >= data.limit()) {
                return 0;
            }
            return data.getShort(pos) & 0xffff;
        }

        public final int[] getInts() {
            int[] a = new int[data.limit() >> 1];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getInt(i);
            }
            return a;
        }

        public String getString(int index, Charset cs) {
            return getBoundedString(Integer.MAX_VALUE, index, cs);
        }

        public String getBoundedString(int maxLen, int index, Charset cs) {
            if (index >= vm()) {
                return null;
            }
            return StringUtils.promptOW(data, maxLen);
        }

        public String[] getStrings(Charset cs) {
            return getBoundedStrings(Integer.MAX_VALUE, cs);
        }

        public String[] getBoundedStrings(int maxLen, Charset cs) {
            String[] a = new String[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getBoundedString(maxLen, i, null);
            }
            return a;
        }

        protected void swapOrder() {
            swapWords(data);
        }
    }

    static DcmElement createOW(int tag) {
        return new OW(tag, EMPTY_VALUE);
    }

    static DcmElement createOW(int tag, short[] v) {
        ByteBuffer buf = ByteBuffer.allocate(v.length << 1);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < v.length; ++i) {
            buf.putShort(v[i]);
        }
        buf.rewind();
        return new OW(tag, buf);
    }

    static DcmElement createOW(int tag, ByteBuffer data) {
        if ((data.limit() & 1) != 0) {
            log.warn(
                "Ignore illegal value of "
                    + Tags.toString(tag)
                    + " OW #"
                    + data.limit());
            return new OW(tag, EMPTY_VALUE);
        }
        return new OW(tag, data);
    }

    private final static class OB extends ValueElement {
        OB(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x4F42;
        }

        public String getString(int index, Charset cs) {
            return getBoundedString(Integer.MAX_VALUE, index, cs);
        }

        public String getBoundedString(int maxLen, int index, Charset cs) {
            if (index >= vm()) {
                return null;
            }
            return StringUtils.promptOB(data, maxLen);
        }

        public String[] getStrings(Charset cs) {
            return getBoundedStrings(Integer.MAX_VALUE, cs);
        }

        public String[] getBoundedStrings(int maxLen, Charset cs) {
            String[] a = new String[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getBoundedString(maxLen, i, null);
            }
            return a;
        }
    }

    static DcmElement createOB(int tag) {
        return new OB(tag, EMPTY_VALUE);
    }

    static DcmElement createOB(int tag, ByteBuffer v) {
        return new OB(tag, v);
    }

    static DcmElement createOB(int tag, byte[] v) {
        return new OB(tag, ByteBuffer.wrap(v).order(ByteOrder.LITTLE_ENDIAN));
    }

    private final static class UN extends ValueElement {
        UN(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x554E;
        }

        public String getString(int index, Charset cs) {
            return getBoundedString(Integer.MAX_VALUE, index, cs);
        }

        public String getBoundedString(int maxLen, int index, Charset cs) {
            if (index >= vm()) {
                return null;
            }
            return StringUtils.promptOB(data, maxLen);
        }

        public String[] getStrings(Charset cs) throws DcmValueException {
            return getBoundedStrings(Integer.MAX_VALUE, cs);
        }

        public String[] getBoundedStrings(int maxLen, Charset cs) {
            String[] a = new String[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = getBoundedString(maxLen, i, null);
            }
            return a;
        }
    }

    static DcmElement createUN(int tag) {
        return new UN(tag, EMPTY_VALUE);
    }

    static DcmElement createUN(int tag, ByteBuffer v) {
        return new UN(tag, v);
    }

    static DcmElement createUN(int tag, byte[] v) {
        return new UN(tag, ByteBuffer.wrap(v).order(ByteOrder.LITTLE_ENDIAN));
    }
}
