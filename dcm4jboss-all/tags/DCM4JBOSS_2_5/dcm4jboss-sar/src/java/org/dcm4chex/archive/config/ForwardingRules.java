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
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.dcm4cheri.util.StringUtils;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1789 $ $Date: 2005-06-20 16:27:54 +0800 (周一, 20 6月 2005) $
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
        if (start < 0 || end > 24 || start >= end)
            throw new IllegalArgumentException();
    }

    public static String toAET(String s) {
        int delim = s.lastIndexOf('!');
        return delim == -1 ? s : s.substring(0,delim);
    }

    public static long toScheduledTime(String s) {
        int delim = s.lastIndexOf('!');
        if (delim != -1) {
	        Calendar cal = Calendar.getInstance();
	        long now = cal.getTimeInMillis();
	        cal.set(Calendar.MILLISECOND, 0);
	        cal.set(Calendar.SECOND, 0);
	        cal.set(Calendar.MINUTE, 0);
	        cal.set(Calendar.HOUR_OF_DAY, 0);
	        long date = cal.getTimeInMillis();
	        long time = now - date;
	        int hypen = s.lastIndexOf('-');
	        long start = Integer.parseInt(s.substring(delim+1, hypen)) * MS_PER_HOUR;
	        long end = Integer.parseInt(s.substring(hypen+1)) * MS_PER_HOUR;
	        if (time > start && time < end)
	            return date + end;
        }
        return 0L;
    }
    
    public String[] getForwardDestinationsFor(Map param) {
    	ArrayList l = new ArrayList(); 
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            if (e.condition.isTrueFor(param) && e.forwardAETs.length > 0 ) l.addAll( Arrays.asList( e.forwardAETs ) );
        }
        if ( l.isEmpty() )
        	return EMPTY;
        else
        	return (String[]) l.toArray( new String[l.size()]);
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
