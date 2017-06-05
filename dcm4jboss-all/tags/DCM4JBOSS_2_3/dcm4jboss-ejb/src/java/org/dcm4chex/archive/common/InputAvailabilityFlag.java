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
 * @version $Revision: 1646 $ $Date: 2005-03-29 07:37:20 +0800 (周二, 29 3月 2005) $
 * @since 06.10.2004
 *
 */
public class InputAvailabilityFlag {

    private static final String[] ENUM = { "PARTIAL", "COMPLETE" };

    public static final int PARTIAL = 0;    
    public static final int COMPLETED = 1;    

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