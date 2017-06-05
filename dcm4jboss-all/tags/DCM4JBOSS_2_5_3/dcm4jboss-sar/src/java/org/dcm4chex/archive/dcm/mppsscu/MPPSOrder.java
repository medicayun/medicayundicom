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
 * @version $Revision: 1793 $ $Date: 2005-06-22 15:47:41 +0800 (周三, 22 6月 2005) $
 * @since 15.11.2004
 *
 */
public class MPPSOrder implements Serializable {

	private static final long serialVersionUID = 4049078245976520754L;

	private final boolean create;

    private final Dataset ds;

    private final String dest;

    private int failureCount;

    public MPPSOrder(Dataset ds, String dest) {
        if (dest == null) throw new NullPointerException();
        if (ds == null) throw new NullPointerException();
        this.create = ds.contains(Tags.ScheduledStepAttributesSeq);
        this.ds = ds;
        this.dest = dest;
    }

	public final boolean isCreate() {
		return create;
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

    public String toString() {
        return (create
                ? "MPPSOrder[N-CREATE, iuid="
                : "MPPSOrder[N-SET, iuid=")
                + ds.getString(Tags.SOPInstanceUID)
                + ", dest=" + dest
                + ", failureCount=" + failureCount + "]";
    }
}
