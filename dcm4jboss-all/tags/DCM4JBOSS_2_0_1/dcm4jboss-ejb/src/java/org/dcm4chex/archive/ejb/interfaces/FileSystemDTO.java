/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.interfaces;

import java.io.Serializable;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1237 $ $Date: 2004-11-30 06:28:36 +0800 (周二, 30 11月 2004) $
 * @since 13.09.2004
 *
 */
public class FileSystemDTO implements Serializable {

    private int pk;

    private String directoryPath;

    private String retrieveAET;

    public StringBuffer toString(StringBuffer sb) {
        sb.append("FileSystem[pk=").append(pk);
        sb.append(", dir=").append(directoryPath);
        sb.append(", aet=").append(retrieveAET);
        sb.append("]");
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

    public final void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public final String getRetrieveAET() {
        return retrieveAET;
    }

    public final void setRetrieveAET(String retrieveAET) {
        this.retrieveAET = retrieveAET;
    }
}