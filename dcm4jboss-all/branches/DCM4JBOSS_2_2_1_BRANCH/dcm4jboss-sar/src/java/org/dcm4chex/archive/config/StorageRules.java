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

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1131 $ $Date: 2004-06-14 06:26:17 +0800 (周一, 14 6月 2004) $
 * @since 15.05.2004
 *
 */
public class StorageRules {

    private final ArrayList list = new ArrayList();

    private static final class Entry {

        final Condition condition;

        final String location;

        Entry(Condition condition, String location) {
            if (location.length() == 0) throw new IllegalArgumentException();
            this.condition = condition;
            this.location = location;
        }
    }

    public StorageRules(String s) {
        boolean containsDefault = false;
        StringTokenizer stk = new StringTokenizer(s, "\r\n;");
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken().trim();
            if (tk.length() == 0) continue;
            try {
                int endCond = tk.indexOf(']') + 1;
                Condition cond = new Condition(tk.substring(0, endCond));
                containsDefault = containsDefault || cond.isAlwaysTrue();
                list.add(new Entry(cond, tk.substring(endCond)));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(tk);
            }
        }
        if (!containsDefault)
            throw new IllegalArgumentException("Missing default storage location");
    }

    public String getStorageLocationFor(Association assoc) {
        Map param = Condition.toParam(assoc, null);
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            if (e.condition.isTrueFor(param)) return e.location;
        }
        throw new IllegalStateException("Missing default storage location");
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            e.condition.toStringBuffer(sb);
            sb.append(e.location);
            sb.append('\r').append('\n');
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }
}
