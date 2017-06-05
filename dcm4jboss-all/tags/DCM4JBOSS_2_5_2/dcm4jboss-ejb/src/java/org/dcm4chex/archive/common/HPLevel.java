/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.common;

import java.util.Arrays;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1909 $ $Date: 2005-08-18 01:24:52 +0800 (周四, 18 8月 2005) $
 * @since Aug 3, 2005
 */
public class HPLevel {
    private static final String[] ENUM = { "MANUFACTURER", "SITE", 
        "USER_GROUP", "SINGLE_USER" };

    public static final int MANUFACTURER = 0;    
    public static final int SITE = 1;    
    public static final int USER_GROUP = 2;    
    public static final int SINGLE_USER = 3;
	
    public static final String toString(int value) {
        return ENUM[value];
    }

    public static final int toInt(String s) {        
        final int index = Arrays.asList(ENUM).indexOf(s);
        if (index == -1)
            throw new IllegalArgumentException(s);
        return index;
    }

}
