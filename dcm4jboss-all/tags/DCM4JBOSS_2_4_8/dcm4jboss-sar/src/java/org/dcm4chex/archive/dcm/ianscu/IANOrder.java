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
 * @version $Revision: 1793 $ $Date: 2005-06-22 15:47:41 +0800 (周三, 22 6月 2005) $
 * @since 28.08.2004
 *
 */
class IANOrder implements Serializable {

	private static final long serialVersionUID = 3258412833061679669L;

	private final String dest;

    private final Dataset ds;

    private int failureCount;

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

    public final String getDestination() {
        return dest;
    }

    public String toString() {
        return "IanOrder[dest=" + dest + ", suid="
                + ds.getString(Tags.StudyInstanceUID)
                + ", failureCount=" + failureCount
        		+ "]";
    }

}