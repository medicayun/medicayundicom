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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.Association;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1764 $ $Date: 2005-06-02 07:40:50 +0800 (周四, 02 6月 2005) $
 * @since 11.06.2004
 *
 */
public class CompressionRules {

    static final int NONE = 0;

    static final int J2LL = 1;

    static final int JLSL = 2;

    static final int J2KR = 3;

    static final String[] CODES = { "NONE", "JPLL", "JLSL", "J2KR"};

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
        Map param = new HashMap();
        param.put("calling", new String[]{assoc.getCallingAET()});
		param.put("called", new String[]{assoc.getCalledAET()});
        if (ds != null) {
			putIntoIfNotNull(param, "cuid", ds, Tags.SOPClassUID);
			putIntoIfNotNull(param, "pmi", ds, Tags.PhotometricInterpretation);
			putIntoIfNotNull(param, "imgtype", ds, Tags.ImageType);
        }
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            if (e.condition.isTrueFor(param)) return TSUIDS[e.compression];
        }
        return null;
    }

    private void putIntoIfNotNull(Map param, String key, Dataset ds, int tag) {
		String[] val = ds.getStrings(tag);
		if (val != null && val.length != 0) {
			param.put(key, val);
		}
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
