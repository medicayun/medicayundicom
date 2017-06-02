/*$Id: Charsets.java 3863 2005-02-11 14:09:53Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG                             *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

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
        put("ISO_IR 13","EUC-JP");
        put("ISO_IR 166","TIS-620");
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
        put("ISO 2022 IR 13","EUC-JP");
        put("ISO 2022 IR 166","TIS-620");
    }    

    /** Private constructor */
    private Charsets() {
    }
}
