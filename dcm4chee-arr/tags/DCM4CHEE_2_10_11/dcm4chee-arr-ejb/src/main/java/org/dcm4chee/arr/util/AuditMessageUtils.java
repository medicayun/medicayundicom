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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4chee.arr.util;

import java.io.UnsupportedEncodingException;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Jun 19, 2006
 *
 */
public class AuditMessageUtils {
    
    private static final int MAX_HEADER_LEN = 200;
    private static final int MAX_PROMPT_LEN = 1000;
    private static final int PROMPT_END = 16;

    private static final byte[] AUDIT_MESSAGE = { 
        (byte)'<', (byte)'A', (byte)'u', (byte)'d', (byte)'i', (byte)'t',
        (byte)'M', (byte)'e', (byte)'s', (byte)'s', (byte)'a', (byte)'g',
        (byte)'e' };

    private static final byte[] IHEYR4 = { 
        (byte)'<', (byte)'I', (byte)'H', (byte)'E', (byte)'Y', (byte)'r',
        (byte)'4',
    };
    
    private static final byte[] XML = {
        (byte)'<', (byte)'?', (byte)'x', (byte)'m', (byte)'l',        
    };
    
    private static boolean startsWith(byte[] val, byte[] msg, int off) {
        if (val.length + off > msg.length) {
            return false;
        }
        for (int i = 0; i < val.length; i++) {
            if (val[i] != msg[i + off]) {
                return false;
            }
        }
        return true;
    }
    
    public static int indexOfXML(byte[] data, int len) {
        for (int i = 0, off = -1, n = Math.min(len, MAX_HEADER_LEN);
                i < n; i++) {
            if (data[i] != '<') {
                continue;
            }
            if (off < 0 && startsWith(XML, data, i)) {
                off = i;
                i += XML.length;
                continue;
            }
            if (startsWith(AUDIT_MESSAGE, data, i)
                    || startsWith(IHEYR4, data, i)) {
                return off < 0 ? i : off;
            }
            if (off >= 0) {
                return -1;
            }
        }
        return -1;
    }

    public static boolean isIHEYr4(byte[] xml) {
        int i = 0;
        if (startsWith(XML, xml, i)) {
            i += XML.length;
            while (i < xml.length && xml[i] != '<') {
                ++i;
            }
        }
        return startsWith(IHEYR4, xml, i);
    }
    
    public static String promptMsg(byte[] xmldata) {
        return promptMsg(xmldata, xmldata.length);
    }
    
    public static String promptMsg(byte[] data, int len) {
        StringBuffer sb = new StringBuffer(MAX_PROMPT_LEN);
        sb.append("AuditMessage[").append(len).append("]:");
        int l = MAX_PROMPT_LEN - sb.length();
        if (l >= len) {
            l = len;
        } else {
            l -= PROMPT_END + 2;
        }
        sb.append(toAscii(data, 0, l));
        if (l < len) {
            sb.append("..").append(toAscii(data, len - PROMPT_END, PROMPT_END));
        }
        return sb.toString();
    }

    private static String toAscii(byte[] msg, int off, int len) {
        try {
            return new String(msg, off, len, "ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
}
