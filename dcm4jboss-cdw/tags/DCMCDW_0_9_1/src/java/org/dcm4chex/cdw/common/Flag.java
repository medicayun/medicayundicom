/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4189 $ $Date: 2004-09-26 04:36:31 +0800 (周日, 26 9月 2004) $
 * @since 25.06.2004
 *
 */
public class Flag {

    public static final String YES = "YES";

    public static final String NO = "NO";

    public static boolean isValid(String s) {
        return s == null || s.equals(YES) || s.equals(NO);
    }

    public static boolean isYES(String s) {
        return YES.equals(s);
    }

    public static boolean isNO(String s) {
        return NO.equals(s);
    }
    
    public static String valueOf(boolean b) {
        return b ? YES : NO;
    }
    
    private Flag() {};
}
