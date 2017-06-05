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
 * @version $Revision: 2095 $ $Date: 2005-11-24 00:05:26 +0800 (周四, 24 11月 2005) $
 * @since Nov 23, 2005
 */
public class FileStatus {

    private static final String[] ENUM = { 
    	"QUERY_HSM_FAILED",
    	"MD5_CHECK_FAILED",
    	"VERIFY_COMPRESS_FAILED", 
    	"COMPRESS_FAILED",
    	"DEFAULT", 
    	"TO_ARCHIVE",
    	"ARCHIVED"
    };

    public static final int QUERY_HSM_FAILED = -4;
    public static final int MD5_CHECK_FAILED = -3;
    public static final int VERIFY_COMPRESS_FAILED = -2;
    public static final int COMPRESS_FAILED = -1;
    public static final int DEFAULT = 0;    
    public static final int TO_ARCHIVE = 1;    
    public static final int ARCHIVED = 2;
    
    private static final int OFFSET = -QUERY_HSM_FAILED;

    public static final String toString(int value) {
        return ENUM[OFFSET + value];
    }

    public static final int toInt(String s) {        
        final int index = Arrays.asList(ENUM).indexOf(s);
        if (index == -1)
            throw new IllegalArgumentException(s);
        return index - OFFSET;
    }
}
