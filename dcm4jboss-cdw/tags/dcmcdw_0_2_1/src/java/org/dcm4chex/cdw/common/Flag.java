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
 * @version $Revision: 4128 $ $Date: 2004-06-28 08:25:30 +0800 (周一, 28 6月 2004) $
 * @since 25.06.2004
 *
 */
public class Flag {

    public static final String YES = "YES";

    public static final String NO = "NO";

    public static boolean isValid(String s) {
        return s == null || s.equals(YES) || s.equals(NO);
    }

    public static boolean isYes(String s) {
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
