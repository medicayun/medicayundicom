/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.ianscu;

import java.io.Serializable;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1158 $ $Date: 2004-08-29 05:54:50 +0800 (周日, 29 8月 2004) $
 * @since 28.08.2004
 *
 */
class IANOrder implements Serializable {

    static final long serialVersionUID = 3594630119462653994L;

    private final String dest;

    private final Dataset ds;

    private int failureCount;

    private int failureStatus;

    public IANOrder(String dest, Dataset ds) {
        if (dest == null) throw new NullPointerException();
        if (ds == null) throw new NullPointerException();
        this.dest = dest;
        this.ds = ds;
    }

    public final Dataset getDataset() {
        return ds;
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

    public final String getDestination() {
        return dest;
    }

    public String toString() {
        return "IanOrder[dest=" + dest + ", suid="
                + ds.getString(Tags.StudyInstanceUID) + ", failureStatus="
                + Integer.toHexString(failureStatus).toUpperCase()
                + "H, failureCount=" + failureCount + "]";
    }

}