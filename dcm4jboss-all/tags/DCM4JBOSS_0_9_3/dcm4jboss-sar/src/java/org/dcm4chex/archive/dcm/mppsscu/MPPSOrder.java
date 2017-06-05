/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.mppsscu;

import java.io.Serializable;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;


/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1204 $ $Date: 2004-11-16 08:09:30 +0800 (周二, 16 11月 2004) $
 * @since 15.11.2004
 *
 */
public class MPPSOrder implements Serializable {

    public static final String QUEUE = "MPPSScu";

    private final Dataset ds;

    private final String dest;

    private int failureCount;

    private int failureStatus;

    public MPPSOrder(Dataset ds, String dest) {
        if (dest == null) throw new NullPointerException();
        if (ds == null) throw new NullPointerException();
        this.ds = ds;
        this.dest = dest;
    }

    public final Dataset getDataset() {
        return ds;
    }

    public final String getDestination() {
        return dest;
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

    public String toString() {
        return (ds.contains(Tags.ScheduledStepAttributesSeq)
                ? "MPPSOrder[N-CREATE, iuid="
                : "MPPSOrder[N-SET, iuid=")
                + ds.getString(Tags.SOPInstanceUID)
                + ", dest="
                + dest
                + ", failureStatus="
                + Integer.toHexString(failureStatus).toUpperCase()
                + "H, failureCount=" + failureCount + "]";
    }
}
