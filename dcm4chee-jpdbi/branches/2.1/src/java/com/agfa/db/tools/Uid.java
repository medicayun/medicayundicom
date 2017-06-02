// $Id: Uid.java 14424 2010-04-10 17:58:33Z kianusch $

package com.agfa.db.tools;

import java.util.Calendar;

class Uid {
    public static String Generate(String base, long count) {
        Calendar cal = Calendar.getInstance();
        long _cal = cal.get(Calendar.YEAR)  * 10000000000000L;
        _cal += ((cal.get(Calendar.MONTH)+1) * 100000000000L);
        _cal += (cal.get(Calendar.DATE)      * 1000000000L);
        
        _cal += (cal.get(Calendar.HOUR_OF_DAY)      * 10000000L);
        
        _cal += (cal.get(Calendar.MINUTE)    * 100000L);
        _cal += (cal.get(Calendar.SECOND)    * 1000L);
        
        _cal += cal.get(Calendar.MILLISECOND);

        String __cal = String.valueOf(_cal);
        
        // System.out.println(__cal);
        // String year = __cal.substring(0, 4);
        // String month = __cal.substring(4, 6);
        // String day = __cal.substring(6, 8);
        // String hour = __cal.substring(8, 10);
        // String minute = __cal.substring(10, 12);
        // String second = __cal.substring(12, 14);

        base = base.replace("%Y", __cal.substring(0, 4));
        base = base.replace("%m", __cal.substring(4, 6));
        base = base.replace("%d", __cal.substring(6, 8));
        base = base.replace("%H", __cal.substring(8, 10));
        base = base.replace("%M", __cal.substring(10, 12));
        base = base.replace("%S", __cal.substring(12, 14));
        base = base.replace("%n", __cal.substring(14));
        
        base = base.replace("%N", String.valueOf(System.nanoTime()));
        base = base.replace("%s", String.valueOf((long) System.currentTimeMillis()/1000));

        base = base.replace("%Z", String.valueOf(count));
        
        return base;
    }
}