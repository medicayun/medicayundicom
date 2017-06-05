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
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.dcm4che.net.Association;
import org.dcm4cheri.util.StringUtils;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 2199 $ $Date: 2006-01-09 18:10:30 +0800 (周一, 09 1月 2006) $
 * @since 15.05.2004
 *
 */
public class ForwardingRules {

    
private static final long MS_PER_HOUR = 3600000L;
    static final String NONE = "NONE";

    static final String[] EMPTY = {};

    private final ArrayList list = new ArrayList();

    public static final class Entry {

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
                for (int i = 0; i < aets.length; i++) {
                    checkAET(aets[i]);
                }
                list.add(new Entry(cond, aets));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(tk);
            }
        }
    }

    private static void checkAET(String s) {
        int delim = s.lastIndexOf('!');
        if (delim == -1) return;
        int hypen = s.lastIndexOf('-');
        int start = Integer.parseInt(s.substring(delim+1, hypen));
        int end = Integer.parseInt(s.substring(hypen+1));
        if (start < 0 || end > 24)
            throw new IllegalArgumentException();
    }

    public static String toAET(String s) {
        int delim = s.lastIndexOf('!');
        return delim == -1 ? s : s.substring(0,delim);
    }

    public static long toScheduledTime(String s) {
        int delim = s.lastIndexOf('!');
        if (delim == -1)
            return 0L;
        int hypen = s.lastIndexOf('-');
        int notAfterHour = Integer.parseInt(s.substring(delim+1, hypen));
        int notBeforeHour = Integer.parseInt(s.substring(hypen+1));
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        boolean sameDay = notAfterHour <= notBeforeHour;
        boolean delay = sameDay 
            ? hour >= notAfterHour && hour < notBeforeHour
            : hour >= notAfterHour || hour < notBeforeHour;
        if (!delay)
            return 0L;
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, notBeforeHour);
        if (!sameDay && hour >= notAfterHour)
            cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTimeInMillis();
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
