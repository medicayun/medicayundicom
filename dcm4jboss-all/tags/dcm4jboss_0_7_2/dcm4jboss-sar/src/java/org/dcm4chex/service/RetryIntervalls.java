/* $Id: RetryIntervalls.java 938 2004-01-26 22:47:15Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.service;

import java.util.Date;
import java.util.StringTokenizer;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 938 $ $Date: 2004-01-27 06:47:15 +0800 (周二, 27 1月 2004) $
 * @since 17.12.2003
 */
final class RetryIntervalls {

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
        intervalls[i] =
            parseMilliseconds(x != -1 ? item.substring(x + 1) : item);
        counts[i] =
            x != -1 ? Math.max(1, Integer.parseInt(item.substring(0, x))) : 1;
    }

    public String toString() {
        if (counts.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < counts.length; i++) {
            sb.append(counts[i]);
            sb.append('x');
            sb.append(intervalls[i] / 1000);
            sb.append("s,");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private long parseMilliseconds(String text) {
        int len = text.length();
        long k = 1L;
        switch (text.charAt(len - 1)) {
            case 'd' :
                k *= 24;
            case 'h' :
                k *= 60;
            case 'm' :
                k *= 60;
            case 's' :
                k *= 1000;
                --len;
        }
        return k * Long.parseLong(text.substring(0, len));
    }

    public Date scheduleNextRetry(int failureCount) {
        int countDown = failureCount;
        for (int i = 0; i < counts.length; ++i) {
            if ((countDown -= counts[i]) <= 0) {
                return new Date(System.currentTimeMillis() + intervalls[i]);
            }
        }
        return null;
    }

}
