/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.interfaces;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1276 $ $Date: 2004-12-08 01:55:57 +0800 (周三, 08 12月 2004) $
 * @since 07.12.2004
 */
public class MD5 {
    public static byte[] toBytes(String s) {
        char[] md5Hex = s.toCharArray();
        byte[] md5 = new byte[16];
        for (int i = 0; i < md5.length; i++)
        {
            md5[i] =
                (byte) ((Character.digit(md5Hex[i << 1], 16) << 4)
                    + Character.digit(md5Hex[(i << 1) + 1], 16));
        }
        return md5;        
    }

    public static String toString(byte[] md5) {
        if (md5.length != 16)
        {
            throw new IllegalArgumentException("md5.length=" + md5.length);
        }
        char[] md5Hex = new char[32];
        for (int i = 0; i < md5.length; i++)
        {
            md5Hex[i << 1] = Character.forDigit((md5[i] >> 4) & 0xf, 16);
            md5Hex[(i << 1) + 1] = Character.forDigit(md5[i] & 0xf, 16);
        }
        return new String(md5Hex);
    }
}
