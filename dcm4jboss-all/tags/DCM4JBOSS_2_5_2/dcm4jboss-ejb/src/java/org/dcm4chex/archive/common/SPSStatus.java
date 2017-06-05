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
 * @version $Revision: 1687 $ $Date: 2005-04-13 23:54:56 +0800 (周三, 13 4月 2005) $
 * @since 06.10.2004
 *
 */
public class SPSStatus {

    private static final String[] ENUM = { "SCHEDULED", "ARRIVED",
    	"IN PROGRESS", "COMPLETED", "DISCONTINUED" };

    public static final int SCHEDULED = 0;    
    public static final int ARRIVED = 1;
    public static final int IN_PROGRESS = 2;    
    public static final int COMPLETED = 3;    
    public static final int DISCONTINUED = 4;

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