/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.config;

import java.util.StringTokenizer;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1158 $ $Date: 2004-08-29 05:54:50 +0800 (周日, 29 8月 2004) $
 * @since 17.12.2003
 */
public final class RetryIntervalls {

    private static final long MS_PER_MIN = 60000L;

    private static final long MS_PER_HOUR = 60 * MS_PER_MIN;

    private static final long MS_PER_DAY = 24 * MS_PER_HOUR;

    private final int[] counts;

    private final long[] intervalls;

    public RetryIntervalls() {
        counts = new int[0];
        intervalls = new long[0];
    }

    public RetryIntervalls(String text) {
        try {
            StringTokenizer stk = new StringTokenizer(text, ", \t\n\r");
            counts = new int[stk.countTokens()];
            intervalls = new long[counts.length];
            for (int i = 0; i < counts.length; i++) {
                init(i, stk.nextToken());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(text);
        }
    }

    private void init(int i, String item) {
        final int x = item.indexOf('x');
        intervalls[i] = parseMilliseconds(x != -1 ? item.substring(x + 1)
                : item);
        counts[i] = x != -1 ? Math.max(1, Integer
                .parseInt(item.substring(0, x))) : 1;
    }

    public String toString() {
        if (counts.length == 0) { return ""; }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < counts.length; i++) {
            sb.append(counts[i]);
            sb.append('x');
            if (intervalls[i] % MS_PER_DAY == 0)
                sb.append(intervalls[i] / MS_PER_DAY).append("d,");
            else if (intervalls[i] % MS_PER_HOUR == 0)
                sb.append(intervalls[i] / MS_PER_HOUR).append("h,");
            else if (intervalls[i] % MS_PER_MIN == 0)
                sb.append(intervalls[i] / MS_PER_MIN).append("m,");
            else
                sb.append(intervalls[i] / 1000).append("s,");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private long parseMilliseconds(String text) {
        int len = text.length();
        long k = 1L;
        switch (text.charAt(len - 1)) {
        case 'd':
            k *= 24;
        case 'h':
            k *= 60;
        case 'm':
            k *= 60;
        case 's':
            k *= 1000;
            --len;
        }
        return k * Long.parseLong(text.substring(0, len));
    }

    public long getIntervall(int failureCount) {
        int countDown = failureCount;
        for (int i = 0; i < counts.length; ++i) {
            if ((countDown -= counts[i]) <= 0) { return intervalls[i]; }
        }
        return -1L;
    }

}