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

package org.dcm4cheri.data;

import java.util.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class Charsets {
    static final Charset ASCII = Charset.forName("US-ASCII");         
    static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");         
    static Charset lookup(String[] specCharset) {
        if (specCharset == null || specCharset.length == 0)
            return ASCII;
        
        if (specCharset.length > 1)
            throw new UnsupportedCharsetException(
                Arrays.asList(specCharset).toString());
        
        Charset retval = (Charset)CHARSETS.get(specCharset[0]);
        if (retval == null)
            throw new UnsupportedCharsetException(specCharset[0]);

        return retval;
    }

    private static HashMap CHARSETS = new HashMap();
    private static void put(String dcmCharset, String charsetName) {
        try {
            CHARSETS.put(dcmCharset,Charset.forName(charsetName));
        } catch (Exception ignore) {
        }
    }
        
    static {
        CHARSETS.put("",ASCII);
        CHARSETS.put("ISO_IR 100",ISO_8859_1);
        put("ISO_IR 101","ISO-8859-2");
        put("ISO_IR 109","ISO-8859-3");
        put("ISO_IR 110","ISO-8859-4");
        put("ISO_IR 144","ISO-8859-5");
        put("ISO_IR 127","ISO-8859-6");
        put("ISO_IR 126","ISO-8859-7");
        put("ISO_IR 138","ISO-8859-8");
        put("ISO_IR 148","ISO-8859-9");
        put("ISO_IR 13","JIS_X0201");
        put("ISO_IR 166","TIS-620");
		put("ISO_IR 192", "UTF-8");
		put("GB18030", "GB18030");
        CHARSETS.put("ISO 2022 IR 6",ASCII);
        CHARSETS.put("ISO 2022 IR 100",ISO_8859_1);
        put("ISO 2022 IR 101","ISO-8859-2");
        put("ISO 2022 IR 109","ISO-8859-3");
        put("ISO 2022 IR 110","ISO-8859-4");
        put("ISO 2022 IR 144","ISO-8859-5");
        put("ISO 2022 IR 127","ISO-8859-6");
        put("ISO 2022 IR 126","ISO-8859-7");
        put("ISO 2022 IR 138","ISO-8859-8");
        put("ISO 2022 IR 148","ISO-8859-9");
        put("ISO 2022 IR 13","JIS_X0201");
        put("ISO 2022 IR 166","TIS-620");
    }    

    /** Private constructor */
    private Charsets() {
    }
}
