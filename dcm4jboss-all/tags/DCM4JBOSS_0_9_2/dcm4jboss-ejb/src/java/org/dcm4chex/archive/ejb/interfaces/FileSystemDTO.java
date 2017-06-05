/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.interfaces;

import java.io.File;
import java.io.Serializable;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1178 $ $Date: 2004-10-08 22:56:13 +0800 (周五, 08 10月 2004) $
 * @since 13.09.2004
 *
 */
public class FileSystemDTO implements Serializable {

    private int pk;

    private String directoryPath;

    private String retrieveAETs;

    private long diskUsage;

    private long highWaterMark;


    public StringBuffer toString(StringBuffer sb) {
        sb.append("FileSystem[pk=").append(pk);
        sb.append(", dir=").append(directoryPath);
        sb.append(", aets=").append(retrieveAETs);
        sb.append(", diskUsage=").append(diskUsage / 1000000f);
        sb.append("MB, highwater=").append(highWaterMark / 1000000f);
        sb.append("MB]");
        return sb;
    }

    public String toString() {
        return toString(new StringBuffer()).toString();
    }

    public final int getPk() {
        return pk;
    }

    public final void setPk(int pk) {
        this.pk = pk;
    }

    public final String getDirectoryPath() {
        return directoryPath;
    }

    public final File getDirectory() {
        return new File(directoryPath.replace('/',File.separatorChar));
    }
    
    public final void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public final long getHighWaterMark() {
        return highWaterMark;
    }

    public final void setHighWaterMark(long highWaterMark) {
        this.highWaterMark = highWaterMark;
    }

    public final String getRetrieveAETs() {
        return retrieveAETs;
    }

    public final void setRetrieveAETs(String retrieveAETs) {
        this.retrieveAETs = retrieveAETs;
    }

    public final long getDiskUsage() {
        return diskUsage;
    }

    public final void setDiskUsage(long diskUsage) {
        this.diskUsage = diskUsage;
    }

    public final long getAvailable() {
        return highWaterMark > 0 ? highWaterMark - diskUsage : Long.MAX_VALUE;
    }
}