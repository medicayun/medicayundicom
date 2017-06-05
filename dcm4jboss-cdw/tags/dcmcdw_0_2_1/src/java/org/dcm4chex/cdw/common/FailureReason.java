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
 * @version $Revision: 4131 $ $Date: 2004-06-29 00:56:50 +0800 (周二, 29 6月 2004) $
 * @since 28.06.2004
 */
public class FailureReason {
    
    private final int ProcessingFailure = 0x0110;
     
    private final int NoSuchObjectInstance = 0x0112; 
    
    private final int RefSOPClassNotSupported = 0x0122; 
    
    private final int ClassInstanceConflict = 0x0119;
    
    private final int MediaApplicationProfilesConflict = 0x0201;
    
    private final int MediaApplicationProfileInstanceConflict = 0x0202; 
    
    private final int MediaApplicationProfileCompressionConflict = 0x0203;
    
    private final int MediaApplicationProfileNotSupported = 0x0204;
    
    private final int InstanceSizeExceeded = 0x0205;
    
    private final int MissingAttribute = 0x0120;
    
    private final int MissingAttributeValue = 0x0121;
}
