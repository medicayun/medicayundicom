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
 * @version $Revision: 4132 $ $Date: 2004-06-29 07:06:09 +0800 (周二, 29 6月 2004) $
 * @since 23.06.2004
 *
 */
public class ExecutionStatusInfo {

    public static final String NORMAL = "NORMAL";
    
    public static final String CHECK_MCD_OP = "CHECK_MCD_OP";
    
    public static final String CHECK_MCD_SRV = "CHECK_MCD_SRV";
    
    public static final String DIR_PROC_ERR = "DIR_PROC_ERR";
    
    public static final String DUPL_REF_INST = "DUPL_REF_INST";
    
    public static final String INST_AP_CONFLICT = "INST_AP_CONFLICT";
    
    public static final String INST_OVERSIZED = "INST_OVERSIZED";
    
    public static final String INSUFFIC_MEMORY = "INSUFFIC_MEMORY";
    
    public static final String MCD_BUSY = "MCD_BUSY";
    
    public static final String MCD_FAILURE = "MCD_FAILURE";
    
    public static final String NO_INSTANCE = "NO_INSTANCE";
    
    public static final String NOT_SUPPORTED = "NOT_SUPPORTED";
    
    public static final String OUT_OF_SUPPLIES = "OUT_OF_SUPPLIES";
    
    public static final String PROC_FAILURE = "PROC_FAILURE";
    
    public static final String QUEUED = "QUEUED";
    
    public static final String SET_OVERSIZED = "SET_OVERSIZED";
    
    public static final String UNKNOWN = "UNKNOWN";

    // defined by TIANI 
    public static final String QUEUED_BUILD = "QUEUED_BUILD";

    public static final String BUILDING = "BUILDING";

    public static final String QUEUED_MKISOFS = "QUEUED_MKISOFS";

    public static final String MKISOFS = "MKISOFS";
    
    private ExecutionStatusInfo() {}

}
