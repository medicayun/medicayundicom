/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

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
 * @version $Revision: 3922 $ $Date: 2005-10-06 00:26:16 +0800 (周四, 06 10月 2005) $
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
