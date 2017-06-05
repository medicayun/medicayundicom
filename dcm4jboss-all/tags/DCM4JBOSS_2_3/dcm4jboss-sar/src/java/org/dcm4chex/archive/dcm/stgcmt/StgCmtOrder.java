/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.stgcmt;

import java.io.Serializable;

import org.dcm4che.data.Dataset;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1418 $ $Date: 2005-01-14 00:01:57 +0800 (周五, 14 1月 2005) $
 * @since Jan 5, 2005
 */
class StgCmtOrder implements Serializable {

    private static final long serialVersionUID = 3256442495255525432L;

//    public static final String QUEUE = "StgCmtScuScp";

    private final String callingAET;

    private final String calledAET;

    private final Dataset actionInfo;

    private final boolean scpRole;

    private int failureCount;

    private int failureStatus;

    public StgCmtOrder(String callingAET, String calledAET, Dataset actionInfo,
            boolean scpRole) {
        this.callingAET = callingAET;
        this.calledAET = calledAET;
        this.actionInfo = actionInfo;
        this.scpRole = scpRole;
    }

    public final int getFailureCount() {
        return failureCount;
    }

    public final void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public final int getFailureStatus() {
        return failureStatus;
    }

    public final void setFailureStatus(int failureStatus) {
        this.failureStatus = failureStatus;
    }

    public final Dataset getActionInfo() {
        return actionInfo;
    }
    
    public final String getCalledAET() {
        return calledAET;
    }
    
    public final String getCallingAET() {
        return callingAET;
    }
    
    public final boolean isScpRole() {
        return scpRole;
    }
    
    public String toString() {
        return "StgCmtOrder[calling=" + callingAET + ", called=" + calledAET
        	+ ", role=" + (scpRole ? "SCP, failures=" : "SCU, failures=")
        	+ failureCount + ", status=" + failureStatus + "]";
    }
}
