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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert Group.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4chee.arr.seam.ejb;

import java.io.UnsupportedEncodingException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Mar 8, 2007
 */
public class Base64Decoder {

    public static String decodeToUTF8(String s) {
        try {
            return new String(decode(s), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static byte[] decode(String s) {
        int sLen = s.length();
        int groups = sLen/4;
        if (4*groups != sLen)
            throw new IllegalArgumentException(
                "String length must be a multiple of four.");
        int missing = 0;
        int fullGroups = groups;
        if (sLen != 0) {
            if (s.charAt(sLen-1) == '=') {
                missing++;
                fullGroups--;
            }
            if (s.charAt(sLen-2) == '=')
                missing++;
        }
        byte[] out = new byte[3*groups - missing];

        int readPos = 0, writePos = 0;
        for (int i=0; i<fullGroups; i++) {
            int b1 = BYTES[s.charAt(readPos++)];
            int b12 = BYTES[s.charAt(readPos++)];
            int b23 = BYTES[s.charAt(readPos++)];
            int b3 = BYTES[s.charAt(readPos++)];
            out[writePos++] = (byte) ((b1 << 2) | (b12 >> 4));
            out[writePos++] = (byte) ((b12 << 4) | (b23 >> 2));
            out[writePos++] = (byte) ((b23 << 6) | b3);
        }

        if (missing != 0) {
            int b1 = BYTES[s.charAt(readPos++)];
            int b12 = BYTES[s.charAt(readPos++)];
            out[writePos++] = (byte) ((b1 << 2) | (b12 >> 4));

            if (missing == 1) {
                int b23 = BYTES[s.charAt(readPos++)];
                out[writePos++] = (byte) ((b12 << 4) | (b23 >> 2));
            }
        }
        return out;
    }

    private static final byte BYTES[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
        55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
        35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

}
