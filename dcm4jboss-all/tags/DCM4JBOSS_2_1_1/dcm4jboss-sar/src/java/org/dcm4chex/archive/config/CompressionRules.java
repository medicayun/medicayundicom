/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.Association;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1131 $ $Date: 2004-06-14 06:26:17 +0800 (周一, 14 6月 2004) $
 * @since 11.06.2004
 *
 */
public class CompressionRules {

    static final int NONE = 0;

    static final int J2LL = 1;

    static final int JPLS = 2;

    static final int J2KR = 3;

    static final String[] CODES = { "NONE", "JPEGLossless", "JPEGLSLossless",
            "JPEG2000Lossless"};

    static final String[] TSUIDS = { null, UIDs.JPEGLossless,
            UIDs.JPEGLSLossless, UIDs.JPEG2000Lossless,};

    private final ArrayList list = new ArrayList();

    private static final class Entry {

        final Condition condition;

        final int compression;

        Entry(Condition condition, int compression) {
            this.condition = condition;
            this.compression = compression;
        }
    }

    public CompressionRules(String s) {
        StringTokenizer stk = new StringTokenizer(s, "\r\n;");
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken().trim();
            if (tk.length() == 0) continue;
            try {
                int endCond = tk.indexOf(']') + 1;
                Condition cond = new Condition(tk.substring(0, endCond));
                int compression = Math.max(0, Arrays.asList(CODES).indexOf(
                        tk.substring(endCond)));
                list.add(new Entry(cond, compression));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(tk);
            }
        }
    }

    public String getTransferSyntaxFor(Association assoc, Dataset ds) {
        Map param = Condition.toParam(assoc, ds);
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            if (e.condition.isTrueFor(param)) return TSUIDS[e.compression];
        }
        return null;
    }

    public String toString() {
        if (list.isEmpty()) return "";
        StringBuffer sb = new StringBuffer();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            e.condition.toStringBuffer(sb);
            sb.append(CODES[e.compression]);
            sb.append('\r').append('\n');
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }
}
