/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 4185 $ $Date: 2004-09-23 09:30:26 +0800 (周四, 23 9月 2004) $
 * @since 28.06.2004
 *
 */
public class MediaCreationException extends Exception {

    private final String statusInfo;
    private final List failedSOPInstances;

    public MediaCreationException(String statusInfo) {
        super();
        this.statusInfo = statusInfo;
        this.failedSOPInstances = null;
    }

    public MediaCreationException(String statusInfo, String message) {
        super(message);
        this.statusInfo = statusInfo;
        this.failedSOPInstances = null;
    }

    public MediaCreationException(String statusInfo, List failedSOPInstances) {
        super();
        this.statusInfo = statusInfo;
        this.failedSOPInstances = Collections.unmodifiableList(new ArrayList(failedSOPInstances));
    }

    public MediaCreationException(String statusInfo, String message, List failedSOPInstances) {
        super(message);
        this.statusInfo = statusInfo;
        this.failedSOPInstances = Collections.unmodifiableList(new ArrayList(failedSOPInstances));
    }

    public MediaCreationException(String statusInfo, Throwable cause) {
        super(cause);
        this.statusInfo = statusInfo;
        this.failedSOPInstances = null;
    }

    public MediaCreationException(String statusInfo, String message,
            Throwable cause) {
        super(message, cause);
        this.statusInfo = statusInfo;
        this.failedSOPInstances = null;
    }

    public final String getStatusInfo() {
        return statusInfo;
    }

    public final List getFailedSOPInstances() {
        return failedSOPInstances;
    }
}
