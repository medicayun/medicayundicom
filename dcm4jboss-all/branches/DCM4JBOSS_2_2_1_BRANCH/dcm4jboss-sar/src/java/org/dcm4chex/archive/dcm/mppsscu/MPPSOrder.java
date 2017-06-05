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
 * @version $Revision: 1418 $ $Date: 2005-01-14 00:01:57 +0800 (周五, 14 1月 2005) $
 * @since 15.11.2004
 *
 */
public class MPPSOrder implements Serializable {

    private static final long serialVersionUID = 3256725091219157049L;

//    public static final String QUEUE = "MPPSScu";

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
