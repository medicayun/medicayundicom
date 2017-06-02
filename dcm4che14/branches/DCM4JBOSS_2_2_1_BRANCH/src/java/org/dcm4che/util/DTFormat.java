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

/**
 * <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3868 $ $Date: 2005-02-25 18:57:26 +0800 (周五, 25 2月 2005) $
 * @since September 21, 2002
 */
public class DTFormat extends SimpleDateFormat {
    
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    
    // Constructors --------------------------------------------------
    public DTFormat() {
        super("yyyyMMddHHmmss.SSS");
    }
    
    // Methods -------------------------------------------------------
    public Date parse(String source, ParsePosition pos) {        
        calendar.clear();
        try {
            String s = parseTZ(source);
            int p = 0;
            int l = s.length();
            calendar.set(Calendar.YEAR,
                Integer.parseInt(s.substring(p,p+4)));
            pos.setIndex(p+=4);
            if (l > p) {
                if (s.charAt(p) == '-') {
                    pos.setIndex(++p);
                }
                calendar.set(Calendar.MONTH,
                    Integer.parseInt(s.substring(p,p+2)) - 1);
                pos.setIndex(p+=2);
                if (l > p) {
                    if (s.charAt(p) == '-') {
                        pos.setIndex(++p);
                    }
                    calendar.set(Calendar.DAY_OF_MONTH,
                        Integer.parseInt(s.substring(p,p+2)));
                    pos.setIndex(p+=2);
                    if (l > p) {
                        calendar.set(Calendar.HOUR_OF_DAY,
                            Integer.parseInt(s.substring(p,p+2)));
                        pos.setIndex(p+=2);
                        if (l > p) {
                            if (s.charAt(p) == ':') {
                                pos.setIndex(++p);
                            }
                            calendar.set(Calendar.MINUTE,
                                Integer.parseInt(s.substring(p,p+2)));
                            pos.setIndex(p+=2);
                            if (l > p) {
                                if (s.charAt(p) == ':') {
                                    pos.setIndex(++p);
                                }
                                float f = Float.parseFloat(s.substring(p));
                                int i = (int) f;
                                calendar.set(Calendar.SECOND, i);
                                calendar.set(Calendar.MILLISECOND,
                                    (int) (1000 * (f - i)));
                            }
                        }
                    }
                }
            }            
            pos.setIndex(source.length());
            return calendar.getTime();
        } catch (Exception e) {
            return null;
        }
    }
    
    private String parseTZ(String source) {
        int zpos = source.length() - 5;
        if (zpos >= 0) {
            char ch = source.charAt(zpos);
            if (ch == '+' || ch == '-') {
                int off = Integer.parseInt(source.substring(zpos+1));                
                calendar.set(Calendar.ZONE_OFFSET, ch == '-' ? -off : off);
                calendar.set(Calendar.DST_OFFSET, 0);
                return source.substring(0, zpos);
            }
        }
        return source;
    }    
}
