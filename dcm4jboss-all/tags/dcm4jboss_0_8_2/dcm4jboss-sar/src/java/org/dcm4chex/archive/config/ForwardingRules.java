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
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.dcm4che.net.Association;
import org.dcm4cheri.util.StringUtils;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1131 $ $Date: 2004-06-14 06:26:17 +0800 (周一, 14 6月 2004) $
 * @since 15.05.2004
 *
 */
public class ForwardingRules {

    static final String NONE = "NONE";

    static final String[] EMPTY = {};

    private final ArrayList list = new ArrayList();

    private static final class Entry {

        final Condition condition;

        final String[] forwardAETs;

        Entry(Condition condition, String[] forwardAETs) {
            this.condition = condition;
            this.forwardAETs = forwardAETs;
        }
    }

    public ForwardingRules(String s) {
        StringTokenizer stk = new StringTokenizer(s, "\r\n;");
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken().trim();
            if (tk.length() == 0) continue;
            try {
                int endCond = tk.indexOf(']') + 1;
                Condition cond = new Condition(tk.substring(0, endCond));
                String second = tk.substring(endCond);
                String[] aets = (second.length() == 0 || NONE
                        .equalsIgnoreCase(second)) ? EMPTY : StringUtils.split(
                        second, ',');
                list.add(new Entry(cond, aets));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(tk);
            }
        }
    }

    public String[] getForwardDestinationsFor(Association assoc) {
        Map param = Condition.toParam(assoc, null);
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            if (e.condition.isTrueFor(param)) return e.forwardAETs;
        }
        return EMPTY;
    }

    public String toString() {
        if (list.isEmpty()) return "";
        StringBuffer sb = new StringBuffer();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            e.condition.toStringBuffer(sb);
            if (e.forwardAETs.length == 0) {
                sb.append(NONE);
            } else {
                for (int i = 0; i < e.forwardAETs.length; ++i)
                    sb.append(e.forwardAETs[i]).append(',');
                sb.setLength(sb.length() - 1);
            }
            sb.append('\r').append('\n');
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }
}
