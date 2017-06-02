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
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3646 $ $Date: 2003-05-10 06:12:03 +0800 (周六, 10 5月 2003) $
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
public class DAFormat extends SimpleDateFormat {
    
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    
    // Constructors --------------------------------------------------
    public DAFormat() {
        super("yyyyMMdd");
    }
    
    // Methods -------------------------------------------------------
    public Date parse(String s, ParsePosition pos) {        
        calendar.clear();
        try {
            int l = s.length();
            int delim = l == 8 ? 0 : l == 10 ? 1 : -1;
            if (delim < 0) {
                return null;
            }
            calendar.set(Calendar.YEAR,
                Integer.parseInt(s.substring(0,4)));
            pos.setIndex(4);
            calendar.set(Calendar.MONTH,
                Integer.parseInt(s.substring(4 + delim, 6 + delim)) - 1);
            pos.setIndex(6 + delim + delim);
            calendar.set(Calendar.DAY_OF_MONTH,
                Integer.parseInt(s.substring(6 + delim + delim)));
            pos.setIndex(l);
            return calendar.getTime();
        } catch (Exception e) {
            return null;
        }
    }
}
