/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.Association;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1131 $ $Date: 2004-06-14 06:26:17 +0800 (周一, 14 6月 2004) $
 * @since 13.06.2004
 *
 */
public class Condition {

    public static final String CALLING = "calling";

    public static final String CALLED = "called";

    public static final String CUID = "cuid";

    public static final String PMI = "pmi";

    private static final int EXPECT_KEY = 0;

    private static final int EXPECT_EQUAL = 1;

    private static final int EXPECT_VALUE = 2;

    private static final int EXPECT_DELIM = 3;

    private static final String DELIM = "=|,]";

    private static boolean isDelim(String s) {
        return s.length() == 1 && DELIM.indexOf(s.charAt(0)) != -1;
    }

    public static Map toParam(Association assoc, Dataset ds) {
        Map retval = new HashMap();
        putInto(retval, CALLING, assoc.getCallingAET());
        putInto(retval, CALLED, assoc.getCalledAET());
        if (ds != null) {
	        putInto(retval, CUID, ds.getString(Tags.SOPClassUID));
	        putInto(retval, PMI, ds.getString(Tags.PhotometricInterpretation));
        }
        return retval;
    }

    private static void putInto(Map retval, String key, String val) {
        if (val != null && val.length() != 0) retval.put(key, val);
    }

    private HashMap map = new HashMap();

    public Condition(String spec) {
        if (!spec.startsWith("[") || !spec.endsWith("]") || spec.length() == 2)
                return;
        StringTokenizer stk = new StringTokenizer(spec.substring(1), DELIM,
                true);
        String tk;
        String key = null;
        int state = EXPECT_KEY;
        for (;;) {
            try {
                tk = stk.nextToken();
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException(spec);
            }
            switch (state) {
            case EXPECT_KEY:
                if (isDelim(tk)) throw new IllegalArgumentException(spec);
                key = tk;
                state = EXPECT_EQUAL;
                break;
            case EXPECT_EQUAL:
                if (!"=".equals(tk)) throw new IllegalArgumentException(spec);
                state = EXPECT_VALUE;
                break;
            case EXPECT_VALUE:
                if (isDelim(tk)) throw new IllegalArgumentException(spec);
                put(key, tk);
                state = EXPECT_DELIM;
                break;
            case EXPECT_DELIM:
                if ("|".equals(tk))
                    state = EXPECT_VALUE;
                else if (",".equals(tk))
                    state = EXPECT_KEY;
                else if ("]".equals(tk)) return;
            }
        }
    }

    private void put(String key, String val) {
        HashSet set = (HashSet) map.get(key);
        if (set == null) {
            set = new HashSet();
            map.put(key, set);
        }
        set.add(val);
    }

    public boolean isAlwaysTrue() {
        return map.isEmpty();
    }

    public boolean isTrueFor(Map param) {
        Iterator it = param.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            HashSet set = (HashSet) map.get(key);
            if (set != null && !set.contains(val)) return false;
            HashSet antiset = (HashSet) map.get(key + '!');
            if (antiset != null && antiset.contains(val)) return false;
        }
        return true;
    }

    public String toString() {
        return toStringBuffer(new StringBuffer()).toString();
    }

    public StringBuffer toStringBuffer(StringBuffer sb) {
        if (map.isEmpty()) return sb;
        sb.append('[');
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            sb.append(entry.getKey()).append('=');
            HashSet set = (HashSet) entry.getValue();
            Iterator values = set.iterator();
            while (values.hasNext())
                sb.append(values.next()).append('|');
            sb.setCharAt(sb.length() - 1, ',');
        }
        sb.setCharAt(sb.length() - 1, ']');
        return sb;
    }
}
