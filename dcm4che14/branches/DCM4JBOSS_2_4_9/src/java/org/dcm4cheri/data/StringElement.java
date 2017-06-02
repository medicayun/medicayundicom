/*  $Id: StringElement.java 3872 2005-03-04 12:18:32Z gunterze $
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
package org.dcm4cheri.data;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.dcm4che.data.*;
import org.dcm4che.dict.VRs;
import org.dcm4che.util.DAFormat;
import org.dcm4che.util.DTFormat;
import org.dcm4che.util.TMFormat;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author     <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since      May, 2002
 * @version    $Revision: 3872 $ $Date: 2005-03-04 20:18:32 +0800 (周五, 04 3月 2005) $
 */
abstract class StringElement extends ValueElement {
    private static long MS_PER_DAY = 24 * 3600000L;

    static Logger log = Logger.getLogger(StringElement.class);

    private interface Trim {
        public String trim(String s);
    }

    private final static Trim NO_TRIM = new Trim() {
        public String trim(String s) {
            return s;
        }
    };

    private final static Trim TRAIL_TRIM = new Trim() {
        public String trim(String s) {
            char ch;
            for (int r = s.length(); r > 0; --r) {
                if ((ch = s.charAt(r - 1)) != '\0' && ch != ' ') {
                    return s.substring(0, r);
                }
            }
            return "";
        }
    };

    private final static Trim PN_TRIM = new Trim() {
        public String trim(String s) {
            char ch;
            for (int r = s.length(); r > 0; --r) {
                if ((ch = s.charAt(r - 1)) != '^' && ch != ' ' && ch != '\0') {
                    return s.substring(0, r);
                }
            }
            return "";
        }
    };

    private final static Trim TOT_TRIM = new Trim() {
        public String trim(String s) {
            char ch;
            for (int r = s.length(); r > 0; --r) {
                if ((ch = s.charAt(r - 1)) != ' ' && ch != '\0') {
                    for (int l = 0; l < r; ++l) {
                        if (s.charAt(l) != ' ') {
                            return s.substring(l, r);
                        }
                    }
                }
            }
            return "";
        }
    };

    private interface Check {
        public String check(String s);
    }

    private final static Check NO_CHECK = new Check() {
        public String check(String s) {
            return s;
        }
    };

    private static class CheckImpl implements Check {
        protected final int maxLen;
        protected final boolean text;

        CheckImpl(int maxLen, boolean text) {
            this.maxLen = maxLen;
            this.text = text;
        }

        public String check(String s) {
            char[] a = s.toCharArray();
            if (a.length > maxLen) {
                log.warn("Value: " + s + " exeeds VR length limit: " + maxLen);
            }
            for (int i = 0; i < a.length; ++i) {
                if (!check(a[i])) {
                    log.warn("Illegal character '" + a[i] + "' in value: " + s);
                }
            }
            return s;
        }

        protected boolean check(char c) {
            return Character.isISOControl(c)
                ? (text && isDICOMControl(c))
                : (text || c != '\\');
        }
    }

    private static boolean isDICOMControl(char c) {
        switch (c) {
            case '\n' :
            case '\f' :
            case '\r' :
            case '\033' :
                return true;
        }
        return false;
    }

    private static ByteBuffer toByteBuffer(
        String value,
        Trim trim,
        Check check,
        Charset cs) {
        if (value == null || value.length() == 0) {
            return EMPTY_VALUE;
        }
        try {
            return (cs != null ? cs : Charsets.ASCII).newEncoder().encode(
                CharBuffer.wrap(check.check(trim.trim(value))));
        } catch (CharacterCodingException ex) {
            if (cs == null) {
                log.warn(
                    "Non ASCII chars in "
                        + value
                        + " - try to encode as ISO_8859_1");
                try {
                    return Charsets.ISO_8859_1.newEncoder().encode(
                        CharBuffer.wrap(check.check(trim.trim(value))));
                } catch (CharacterCodingException e) {}
            }
            throw new IllegalArgumentException(value);
        }
    }

    private static ByteBuffer toByteBuffer(ByteBuffer[] bbs, int totLen) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[totLen]);
        bb.put(bbs[0]);
        for (int i = 1; i < bbs.length; ++i) {
            bb.put(DELIM);
            bb.put(bbs[i]);
        }
        bb.rewind();
        return bb;
    }

    private static ByteBuffer toByteBuffer(
        String[] values,
        Trim trim,
        Check check,
        Charset cs) {
        if (values.length == 0) {
            return EMPTY_VALUE;
        }

        if (values.length == 1) {
            return toByteBuffer(values[0], trim, check, cs);
        }

        ByteBuffer[] bbs = new ByteBuffer[values.length];
        int totLen = -1;
        for (int i = 0; i < values.length; ++i) {
            bbs[i] = toByteBuffer(values[i], trim, check, cs);
            totLen += bbs[i].limit() + 1;
        }
        return toByteBuffer(bbs, totLen);
    }

    private final Trim trim;

    StringElement(int tag, ByteBuffer data, Trim trim) {
        super(tag, data);
        this.trim = trim;
    }

    /**
     *  Gets the string attribute of the StringElement object
     *
     * @param  index                  Description of the Parameter
     * @param  cs                     Description of the Parameter
     * @return                        The string value
     * @exception  DcmValueException  Description of the Exception
     */
    public String getString(int index, Charset cs)
        throws DcmValueException {
        ByteBuffer bb = getByteBuffer(index);
        if (bb == null) {
            return null;
        }
        final Charset charset = cs != null ? cs : Charsets.ASCII;
        final int start = bb.position();
        String s;
        try {
            s = charset.newDecoder().decode(bb).toString();
        } catch (Exception ex) {
            s = new String(bb.array(), start, bb.limit() - start);
            log.warn(
                "Failed to decode value["
                    + index
                    + "] of "
                    + this
                    + " using "
                    + charset
                    + "; return "
                    + s);
        }
        return trim.trim(s);
    }

    /**
     *  Gets the strings attribute of the StringElement object
     *
     * @param  cs                     Description of the Parameter
     * @return                        The strings value
     * @exception  DcmValueException  Description of the Exception
     */
    public String[] getStrings(Charset cs)
        throws DcmValueException {
        String[] a = new String[vm()];
        for (int i = 0; i < a.length; ++i) {
            a[i] = getString(i, cs);
        }
        return a;
    }

    /**
     *  Gets the byteBuffer attribute of the StringElement object
     *
     * @param  index  Description of the Parameter
     * @return        The byteBuffer value
     */
    public ByteBuffer getByteBuffer(int index) {
        if (index >= vm()) {
            return null;
        }
        return data.duplicate().order(data.order());
    }

    private static boolean isUniversalMatch(String p) {
        if (p == null) {
            return true;
        }
        for (int i = 0, n = p.length(); i < n; ++i) {
            if (p.charAt(i) != '*') {
                return false;
            }
        }
        return true;
    }

    /**
     *  Description of the Method
     *
     * @param  key           Description of the Parameter
     * @param  ignorePNCase  Description of the Parameter
     * @param  keyCS         Description of the Parameter
     * @param  dsCS          Description of the Parameter
     * @return               Description of the Return Value
     */
    protected boolean matchValue(
        DcmElement key,
        boolean ignorePNCase,
        boolean ignoreEmpty,
        Charset keyCS,
        Charset dsCS) {
        for (int i = 0, m = key.vm(); i < m; ++i) {
            String pattern;
            try {
                pattern = key.getString(i, keyCS);
            } catch (DcmValueException e) {
                throw new IllegalArgumentException("key: " + key);
            }
            if (isUniversalMatch(pattern)) {
                return true;
            }
            String s;
            for (int j = 0, n = vm(); j < n; ++j) {
                try {
                    s = getString(j, dsCS);
                } catch (DcmValueException e) {
                    // Illegal Value match always (like null value)
                    return true;
                }
                if (ignorePNCase
                    && vr() == VRs.PN
                        ? match(pattern.toUpperCase(), s.toUpperCase())
                        : match(pattern, s)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean match(String pattern, String input) {
        if (pattern.indexOf('*') == -1 && pattern.indexOf('?') == -1) {
            return pattern.equals(input);
        }
        return Pattern.matches(toRegEx(pattern), input);
    }

    private static String toRegEx(String pattern) {
        char[] a = pattern.toCharArray();
        StringBuffer sb = new StringBuffer(a.length + 10);
        boolean inQuote = false;
        for (int i = 0; i < a.length; ++i) {
            if (a[i] == '*' || a[i] == '?') {
                if (inQuote) {
                    sb.append('\\').append('E');
                    inQuote = false;
                }
                sb.append('.');
                if (a[i] == '*') {
                    sb.append('*');
                }
            } else {
                if (!inQuote) {
                    sb.append('\\').append('Q');
                    inQuote = true;
                }
                sb.append(a[i]);
            }
        }
        if (inQuote) {
            sb.append('\\').append('E');
        }
        return sb.toString();
    }

    // LT -------------------------------------------------------------
    private final static class LT extends StringElement {
        LT(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x4C54;
        }
    }

    private final static Check LT_CHECK = new CheckImpl(10240, true);

    static DcmElement createLT(int tag, ByteBuffer data) {
        return new LT(tag, data);
    }

    static DcmElement createLT(int tag) {
        return new LT(tag, EMPTY_VALUE);
    }

    static DcmElement createLT(int tag, String value, Charset cs) {
        return new LT(tag, toByteBuffer(value, TRAIL_TRIM, LT_CHECK, cs));
    }

    static DcmElement createLT(int tag, String[] values, Charset cs) {
        return new LT(tag, toByteBuffer(values, TRAIL_TRIM, LT_CHECK, cs));
    }

    // ST -------------------------------------------------------------
    private final static class ST extends StringElement {
        ST(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x5354;
        }
    }

    private final static Check ST_CHECK = new CheckImpl(1024, true);

    static DcmElement createST(int tag, ByteBuffer data) {
        return new ST(tag, data);
    }

    static DcmElement createST(int tag) {
        return new ST(tag, EMPTY_VALUE);
    }

    static DcmElement createST(int tag, String value, Charset cs) {
        return new ST(tag, toByteBuffer(value, TRAIL_TRIM, ST_CHECK, cs));
    }

    static DcmElement createST(int tag, String[] values, Charset cs) {
        return new ST(tag, toByteBuffer(values, TRAIL_TRIM, ST_CHECK, cs));
    }

    // UT -------------------------------------------------------------
    private final static class UT extends StringElement {
        UT(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x5554;
        }
    }

    final static Check UT_CHECK = new CheckImpl(Integer.MAX_VALUE, true);

    static DcmElement createUT(int tag, ByteBuffer data) {
        return new UT(tag, data);
    }

    static DcmElement createUT(int tag) {
        return new UT(tag, EMPTY_VALUE);
    }

    static DcmElement createUT(int tag, String value, Charset cs) {
        return new UT(tag, toByteBuffer(value, TRAIL_TRIM, UT_CHECK, cs));
    }

    static DcmElement createUT(int tag, String[] values, Charset cs) {
        return new UT(tag, toByteBuffer(values, TRAIL_TRIM, UT_CHECK, cs));
    }

    // MultiStringElements ---------------------------------------------------
    private final static byte DELIM = 0x5c;

    private abstract static class MultiStringElement extends StringElement {
        private int[] delimPos = null;

        MultiStringElement(int tag, ByteBuffer data, Trim trim) {
            super(tag, data, trim);
        }

        public final int vm() {
            if (delimPos != null) {
                return delimPos.length - 1;
            }
            byte[] a = data.array();
            if (a.length == 0) {
                return 0;
            }
            int vm = 1;
            for (int i = 0; i < a.length; ++i) {
                if (a[i] == DELIM) {
                    ++vm;
                }
            }
            delimPos = new int[vm + 1];
            delimPos[0] = -1;
            delimPos[vm] = a.length;
            for (int i = 0, j = 0; i < a.length; ++i) {
                if (a[i] == DELIM) {
                    delimPos[++j] = i;
                }
            }
            return vm;
        }

        public ByteBuffer getByteBuffer(int index) {
            if (index >= vm()) {
                return null;
            }
            ByteBuffer bb = data.duplicate().order(data.order());
            if (vm() != 1) {
                bb.position(delimPos[index] + 1);
                bb.limit(delimPos[index + 1]);
            }
            return bb;
        }
    }

    // LO ------------------------------------------------------------------
    private final static class LO extends MultiStringElement {
        LO(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x4C4F;
        }
    }

    private final static Check LO_CHECK = new CheckImpl(64, false);

    static DcmElement createLO(int tag, ByteBuffer data) {
        return new LO(tag, data);
    }

    static DcmElement createLO(int tag) {
        return new LO(tag, EMPTY_VALUE);
    }

    static DcmElement createLO(int tag, String value, Charset cs) {
        return new LO(tag, toByteBuffer(value, TOT_TRIM, LO_CHECK, cs));
    }

    static DcmElement createLO(int tag, String[] values, Charset cs) {
        return new LO(tag, toByteBuffer(values, TOT_TRIM, LO_CHECK, cs));
    }

    // PN ------------------------------------------------------------------
    private final static class PN extends MultiStringElement {
        PN(int tag, ByteBuffer data) {
            super(tag, data, PN_TRIM);
        }

        public final int vr() {
            return 0x504E;
        }

        public final PersonName getPersonName(int index, Charset cs)
            throws DcmValueException {
            return new PersonNameImpl(getString(index, cs));
        }
    }

    static DcmElement createPN(int tag, ByteBuffer data) {
        return new PN(tag, data);
    }

    static DcmElement createPN(int tag) {
        return new PN(tag, EMPTY_VALUE);
    }

    static DcmElement createPN(int tag, PersonName value, Charset cs) {
        return new PN(
            tag,
            toByteBuffer(value.toString(), NO_TRIM, NO_CHECK, cs));
    }

    static DcmElement createPN(int tag, PersonName[] values, Charset cs) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = values[i].toString();
        }
        return new PN(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, cs));
    }

    // SH ------------------------------------------------------------------
    private final static class SH extends MultiStringElement {
        SH(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x5348;
        }
    }

    private final static Check SH_CHECK = new CheckImpl(16, false);

    static DcmElement createSH(int tag, ByteBuffer data) {
        return new SH(tag, data);
    }

    static DcmElement createSH(int tag) {
        return new SH(tag, EMPTY_VALUE);
    }

    static DcmElement createSH(int tag, String value, Charset cs) {
        return new SH(tag, toByteBuffer(value, TOT_TRIM, SH_CHECK, cs));
    }

    static DcmElement createSH(int tag, String[] values, Charset cs) {
        return new SH(tag, toByteBuffer(values, TOT_TRIM, SH_CHECK, cs));
    }

    // AsciiMultiStringElements ----------------------------------------------
    private abstract static class AsciiMultiStringElement
        extends MultiStringElement {
        AsciiMultiStringElement(int tag, ByteBuffer data, Trim trim) {
            super(tag, data, trim);
        }

        public final String getString(int index, Charset cs)
            throws DcmValueException {
            return super.getString(index, null);
        }
    }

    // AE ------------------------------------------------------------------
    private final static class AE extends AsciiMultiStringElement {
        AE(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x4145;
        }
    }

    private final static Check AE_CHECK = new CheckImpl(16, false);

    static DcmElement createAE(int tag, ByteBuffer data) {
        return new AE(tag, data);
    }

    static DcmElement createAE(int tag) {
        return new AE(tag, EMPTY_VALUE);
    }

    static DcmElement createAE(int tag, String value) {
        return new AE(tag, toByteBuffer(value, TOT_TRIM, AE_CHECK, null));
    }

    static DcmElement createAE(int tag, String[] values) {
        return new AE(tag, toByteBuffer(values, TOT_TRIM, AE_CHECK, null));
    }

    // AS ------------------------------------------------------------------
    private final static class AS extends AsciiMultiStringElement {
        AS(int tag, ByteBuffer data) {
            super(tag, data, NO_TRIM);
        }

        public final int vr() {
            return 0x4153;
        }
    }

    private final static Check AS_CHECK = new Check() {
        public String check(String s) {
            if (s.length() == 4
                && Character.isDigit(s.charAt(0))
                && Character.isDigit(s.charAt(1))
                && Character.isDigit(s.charAt(2))) {
                switch (s.charAt(3)) {
                    case 'D' :
                    case 'W' :
                    case 'M' :
                    case 'Y' :
                        return s;
                }
            }
            log.warn("Illegal Age String: " + s);
            return s;
        }
    };

    static DcmElement createAS(int tag, ByteBuffer data) {
        return new AS(tag, data);
    }

    static DcmElement createAS(int tag) {
        return new AS(tag, EMPTY_VALUE);
    }

    static DcmElement createAS(int tag, String value) {
        return new AS(tag, toByteBuffer(value, NO_TRIM, AS_CHECK, null));
    }

    static DcmElement createAS(int tag, String[] values) {
        return new AS(tag, toByteBuffer(values, NO_TRIM, AS_CHECK, null));
    }

    // CS ------------------------------------------------------------------
    private final static class CS extends AsciiMultiStringElement {
        CS(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x4353;
        }
    }

    final static Check CS_CHECK = new CheckImpl(16, false) {
        protected boolean check(char c) {
            return (
                (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == ' '
                    || c == '_');
        }
    };

    static DcmElement createCS(int tag, ByteBuffer data) {
        return new CS(tag, data);
    }

    static DcmElement createCS(int tag) {
        return new CS(tag, EMPTY_VALUE);
    }

    static DcmElement createCS(int tag, String v) {
        return new CS(tag, toByteBuffer(v, TOT_TRIM, CS_CHECK, null));
    }

    static DcmElement createCS(int tag, String[] a) {
        return new CS(tag, toByteBuffer(a, TOT_TRIM, CS_CHECK, null));
    }

    // DS ------------------------------------------------------------------
    final static Check DS_CHECK = new Check() {
        public String check(String s) {
            try {
                Float.parseFloat(s);
                if (s.length() > 16) {
                    log.warn("DS Value: " + s + " exeeds DS length limit: 16");
                }
            } catch (NumberFormatException e) {
                log.warn("Illegal DS Value: " + s);
            }
            return s;
        }
    };

    private final static class DS extends AsciiMultiStringElement {
        DS(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x4453;
        }

        public final float getFloat(int index) throws DcmValueException {
            return Float.parseFloat(super.getString(index, null));
        }

        public final float[] getFloats() throws DcmValueException {
            float[] retval = new float[vm()];
            for (int i = 0; i < retval.length; ++i) {
                retval[i] = getFloat(i);
            }
            return retval;
        }
    }

    static DcmElement createDS(int tag, ByteBuffer data) {
        return new DS(tag, data);
    }

    static DcmElement createDS(int tag) {
        return new DS(tag, EMPTY_VALUE);
    }

    static DcmElement createDS(int tag, float value) {
        return new DS(
            tag,
            toByteBuffer(String.valueOf(value), NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDS(int tag, float[] values) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = String.valueOf(values[i]);
        }
        return new DS(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDS(int tag, String v) {
        return new DS(tag, toByteBuffer(v, TOT_TRIM, DS_CHECK, null));
    }

    static DcmElement createDS(int tag, String[] a) {
        return new DS(tag, toByteBuffer(a, TOT_TRIM, DS_CHECK, null));
    }

    // IS ------------------------------------------------------------------
    final static Check IS_CHECK = new Check() {
        public String check(String s) {
            try {
                Integer.parseInt(s);
                if (s.length() > 12) {
                    log.warn("IS Value: " + s + " exeeds IS length limit: 12");
                }
            } catch (NumberFormatException e) {
                log.warn("Illegal IS Value: " + s);
            }
            return s;
        }
    };

    private final static class IS extends AsciiMultiStringElement {
        IS(int tag, ByteBuffer data) {
            super(tag, data, TOT_TRIM);
        }

        public final int vr() {
            return 0x4953;
        }

        public final int getInt(int index) throws DcmValueException {
            String s = super.getString(index, null);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                throw new DcmValueException(s, ex);
            }
        }

        public final int[] getInts() throws DcmValueException {
            int[] retval = new int[vm()];
            for (int i = 0; i < retval.length; ++i) {
                retval[i] = getInt(i);
            }
            return retval;
        }
    }

    static DcmElement createIS(int tag, ByteBuffer data) {
        return new IS(tag, data);
    }

    static DcmElement createIS(int tag) {
        return new IS(tag, EMPTY_VALUE);
    }

    static DcmElement createIS(int tag, int value) {
        return new IS(
            tag,
            toByteBuffer(String.valueOf(value), NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createIS(int tag, int[] values) {
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = String.valueOf(values[i]);
        }
        return new IS(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createIS(int tag, String v) {
        return new IS(tag, toByteBuffer(v, TOT_TRIM, IS_CHECK, null));
    }

    static DcmElement createIS(int tag, String[] a) {
        return new IS(tag, toByteBuffer(a, TOT_TRIM, IS_CHECK, null));
    }

    // UI ------------------------------------------------------------------
    private final static class UI extends AsciiMultiStringElement {
        UI(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x5549;
        }
    }

    private final static int UID_DIGIT1 = 0;
    private final static int UID_DIGIT = 1;
    private final static int UID_DOT = 2;
    private final static int UID_ERROR = -1;

    private static int nextState(int state, char c) {
        switch (state) {
            case UID_DIGIT1 :
                if (c > '0' && c <= '9') {
                    return UID_DIGIT;
                }
                if (c == '0') {
                    return UID_DOT;
                }
                return UID_ERROR;
            case UID_DIGIT :
                if (c >= '0' && c <= '9') {
                    return UID_DIGIT;
                }
                // fall through
            case UID_DOT :
                if (c == '.') {
                    return UID_DIGIT1;
                }
        }
        return UID_ERROR;
    }

    private final static Check UI_CHECK = new CheckImpl(64, false) {
        public String check(String s) {
            char[] a = s.toCharArray();
            if (a.length > maxLen) {
                log.warn("Value: " + s + " exeeds VR length limit: " + maxLen);
            }
            int state = UID_DIGIT1;
            for (int i = 0; i < a.length; ++i) {
                if ((state = nextState(state, a[i])) == UID_ERROR) {
                    log.warn("Illegal UID value: " + s);
                    return s;
                }
            }
            if (state == UID_DIGIT1) {
                log.warn("Illegal UID value: " + s);
            }
            return s;
        }
    };

    static DcmElement createUI(int tag, ByteBuffer data) {
        return new UI(tag, data);
    }

    static DcmElement createUI(int tag) {
        return new UI(tag, EMPTY_VALUE);
    }

    static DcmElement createUI(int tag, String value) {
        return new UI(tag, toByteBuffer(value, NO_TRIM, UI_CHECK, null));
    }

    static DcmElement createUI(int tag, String[] values) {
        return new UI(tag, toByteBuffer(values, NO_TRIM, UI_CHECK, null));
    }

    // DA ----------------------------------------------
    private final static byte HYPHEN = 0x2d;

    private abstract static class DateString extends AsciiMultiStringElement {
        DateString(int tag, ByteBuffer data, Trim trim) {
            super(tag, data, trim);
        }

        public final boolean isDataRange() {
            for (int i = 0, n = data.limit(); i < n; ++i) {
                if (data.get(i) == HYPHEN) {
                    return true;
                }
            }
            return false;
        }

        public final Date getDate(int index) throws DcmValueException {
            return toDate(getFormat(), super.getString(index, null));
        }

        public final Date[] getDateRange(int index) throws DcmValueException {
            return toDateRange(getFormat(), super.getString(index, null), getTimeResolution());
        }

        public final Date[] getDates() throws DcmValueException {
            DateFormat f = getFormat();
            Date[] a = new Date[vm()];
            for (int i = 0; i < a.length; ++i) {
                a[i] = toDate(f, super.getString(i, null));
            }
            return a;
        }

        protected boolean matchValue(
            DcmElement key,
            boolean ignorePNCase,
            boolean ignoreEmpty,
            Charset keyCS,
            Charset dsCS) {
            for (int i = 0, n = key.vm(); i < n; ++i) {
                Date[] range;
                try {
                    range = key.getDateRange(i);
                } catch (DcmValueException e) {
                    throw new IllegalArgumentException("key: " + key);
                }
                long from =
                    range[0] != null ? range[0].getTime() : Long.MIN_VALUE;
                long to =
                    range[1] != null ? range[1].getTime() : Long.MAX_VALUE;
                try {
                    Date[] values = getDates();
                    for (int j = 0; j < values.length; ++j) {
                        if (values[i] == null) {
                            return true;
                        }
                        final long time = values[i].getTime();
                        if (time >= from && time <= to) {
                            return true;
                        }
                    }
                } catch (DcmValueException e) {
                    return true;
                }
            }
            return false;
        }

        protected abstract DateFormat getFormat();
        
        protected long getTimeResolution() { return 1L; }

    }

    private static Date toDate(DateFormat f, String s) {
        try {
            return s != null ? f.parse(s) : null;
        } catch (ParseException e) {
            throw new IllegalArgumentException(s);
        }
    }

    private static Date[] toDateRange(DateFormat f, String s, long resolution) {
        if (s == null) {
            return null;
        }
        Date[] range = new Date[2];
        int delim = s.indexOf('-');
        try {
            if (delim == -1) {
                range[0] = f.parse(s);
                range[1] = new Date(range[0].getTime() + resolution - 1);
            } else {
                if (delim > 0) {
                    range[0] = f.parse(s.substring(0, delim));
                }
                if (delim + 1 < s.length()) {
                    range[1] = new Date(
                    		f.parse(s.substring(delim + 1)).getTime()
                    		+ resolution - 1);
                }
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(s);
        }
        return range;
    }

    private final static class DA extends DateString {
        DA(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x4441;
        }

        protected DateFormat getFormat() {
            return new DAFormat();
        }
        
        protected long getTimeResolution() { 
        	return MS_PER_DAY;
        }

    }

    static DcmElement createDA(int tag, ByteBuffer data) {
        return new DA(tag, data);
    }

    static DcmElement createDA(int tag) {
        return new DA(tag, EMPTY_VALUE);
    }

    private static String toString(DateFormat f, Date d) {
        return d == null ? null : f.format(d);
    }

    private static String toString(DateFormat f, Date from, Date to) {
        StringBuffer sb = new StringBuffer(64);
        if (from != null) {
            sb.append(f.format(from));
        }
        sb.append('-');
        if (to != null) {
            sb.append(f.format(to));
        }
        return sb.toString();
    }

    static DcmElement createDA(int tag, Date value) {
        return new DA(
            tag,
            toByteBuffer(
                toString(new DAFormat(), value),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createDA(int tag, Date[] values) {
        DAFormat f = new DAFormat();
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = toString(f, values[i]);
        }
        return new DA(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDA(int tag, Date from, Date to) {
        return new DA(
            tag,
            toByteBuffer(
                toString(new DAFormat(), from, to),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createDA(int tag, String value) {
        DAFormat f = new DAFormat();
        if (value.indexOf('-') != -1) {
            toDateRange(f, value, MS_PER_DAY);
        } else {
            toDate(f, value);
        }
        return new DA(tag, toByteBuffer(value, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDA(int tag, String[] values) {
        DAFormat f = new DAFormat();
        for (int i = 0; i < values.length; ++i) {
            toDate(f, values[i]);
        }
        return new DA(tag, toByteBuffer(values, NO_TRIM, NO_CHECK, null));
    }

    private final static class DT extends DateString {
        DT(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x4454;
        }

        protected DateFormat getFormat() {
            return new DTFormat();
        }
    }

    static DcmElement createDT(int tag, ByteBuffer data) {
        return new DT(tag, data);
    }

    static DcmElement createDT(int tag) {
        return new DT(tag, EMPTY_VALUE);
    }

    static DcmElement createDT(int tag, Date value) {
        return new DT(
            tag,
            toByteBuffer(
                toString(new DTFormat(), value),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createDT(int tag, Date[] values) {
        DTFormat f = new DTFormat();
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = toString(f, values[i]);
        }
        return new DT(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDT(int tag, Date from, Date to) {
        return new DT(
            tag,
            toByteBuffer(
                toString(new DTFormat(), from, to),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createDT(int tag, String value) {
        DTFormat f = new DTFormat();
        if (value.indexOf('-') != -1) {
            toDateRange(f, value, 0);
        } else {
            toDate(f, value);
        }
        return new DT(tag, toByteBuffer(value, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createDT(int tag, String[] values) {
        DTFormat f = new DTFormat();
        for (int i = 0; i < values.length; ++i) {
            toDate(f, values[i]);
        }
        return new DT(tag, toByteBuffer(values, NO_TRIM, NO_CHECK, null));
    }

    private final static class TM extends DateString {
        TM(int tag, ByteBuffer data) {
            super(tag, data, TRAIL_TRIM);
        }

        public final int vr() {
            return 0x544D;
        }

        protected DateFormat getFormat() {
            return new TMFormat();
        }
    }

    static DcmElement createTM(int tag, ByteBuffer data) {
        return new TM(tag, data);
    }

    static DcmElement createTM(int tag) {
        return new TM(tag, EMPTY_VALUE);
    }

    static DcmElement createTM(int tag, Date value) {
        return new TM(
            tag,
            toByteBuffer(
                toString(new TMFormat(), value),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createTM(int tag, Date[] values) {
        TMFormat f = new TMFormat();
        String[] tmp = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            tmp[i] = toString(f, values[i]);
        }
        return new TM(tag, toByteBuffer(tmp, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createTM(int tag, Date from, Date to) {
        return new TM(
            tag,
            toByteBuffer(
                toString(new TMFormat(), from, to),
                NO_TRIM,
                NO_CHECK,
                null));
    }

    static DcmElement createTM(int tag, String value) {
        TMFormat f = new TMFormat();
        if (value.indexOf('-') != -1) {
            toDateRange(f, value, 0);
        } else {
            toDate(f, value);
        }
        return new TM(tag, toByteBuffer(value, NO_TRIM, NO_CHECK, null));
    }

    static DcmElement createTM(int tag, String[] values) {
        TMFormat f = new TMFormat();
        for (int i = 0; i < values.length; ++i) {
            toDate(f, values[i]);
        }
        return new TM(tag, toByteBuffer(values, NO_TRIM, NO_CHECK, null));
    }

}
