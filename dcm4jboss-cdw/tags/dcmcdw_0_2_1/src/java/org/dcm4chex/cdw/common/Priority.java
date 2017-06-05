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
public class Priority {

    public static final String HIGH = "HIGH";

    public static final String MED = "MED";

    public static final String LOW = "LOW";

    public static boolean isValid(String s) {
        return s.equals(LOW) || s.equals(MED) || s.equals(HIGH);
    }
    
    private Priority() {};
}
