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
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 4185 $ $Date: 2004-09-23 09:30:26 +0800 (周四, 23 9月 2004) $
 * @since 28.06.2004
 */
public class FailureReason {
    
    public static final int ProcessingFailure = 0x0110;
     
    public static final int NoSuchObjectInstance = 0x0112; 
    
    public static final int RefSOPClassNotSupported = 0x0122; 
    
    public static final int ClassInstanceConflict = 0x0119;
    
    public static final int MediaApplicationProfilesConflict = 0x0201;
    
    public static final int MediaApplicationProfileInstanceConflict = 0x0202; 
    
    public static final int MediaApplicationProfileCompressionConflict = 0x0203;
    
    public static final int MediaApplicationProfileNotSupported = 0x0204;
    
    public static final int InstanceSizeExceeded = 0x0205;

    public static final int MissingAttribute = 0x0120;
    
    public static final int MissingAttributeValue = 0x0121;
}
