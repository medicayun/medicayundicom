/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.io.File;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2004-11-30 06:28:36 +0800 (周二, 30 11月 2004) $
 * @since 29.11.2004
 */

public final class FileSystemInfo {
    private static final int MEGA = 1000000;
    private final String path;
    private final File directory;
    private final long available;
    private final String retrieveAET;
    public FileSystemInfo(String path, File directory, long available, String retrieveAET) {
        this.path = path;
        this.directory = directory;
        this.available = available;
        this.retrieveAET = retrieveAET;
    }
    
    public final String getPath() {
        return path;
    }
    
    public final File getDirectory() {
        return directory;
    }
    
    public final long getAvailable() {
        return available;
    }
    
    public final String getRetrieveAET() {
        return retrieveAET;
    }
    
    public String toString() {
        return "FileSystem[path=" + path
        	+ ", available=" + available / MEGA
        	+ "MB]";
    }
}
