/*  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4che.util;

import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3868 $ $Date: 2005-02-25 18:57:26 +0800 (周五, 25 2月 2005) $
 * @since September 21, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class TMFormat extends SimpleDateFormat {
    
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    
    // Constructors --------------------------------------------------
    public TMFormat() {
        super("HHmmss.SSS");
    }
    
    // Methods -------------------------------------------------------
    public Date parse(String s, ParsePosition pos) {        
        calendar.clear();
        try {
            int p = 0;
            int l = s.length();
            calendar.set(Calendar.HOUR_OF_DAY,
                Integer.parseInt(s.substring(p,p+2)));
            pos.setIndex(p += 2);
            if (p < l) {
                if (s.charAt(p) == ':') {
                    pos.setIndex(++p);
                }
                calendar.set(Calendar.MINUTE,
                    Integer.parseInt(s.substring(p,p+2)));
                pos.setIndex(p += 2);
                if (p < l) {
                    if (s.charAt(p) == ':') {
                        pos.setIndex(++p);
                    }
                    float f = Float.parseFloat(s.substring(p));
                    int i = (int) f;
                    calendar.set(Calendar.SECOND, i);
                    calendar.set(Calendar.MILLISECOND,
                        (int) (1000 * (f - i)));
                }
                pos.setIndex(l);
            }            
            return calendar.getTime();
        } catch (Exception e) {
            return null;
        }
    }
}
