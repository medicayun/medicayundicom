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
 * @version $Revision: 1163 $ $Date: 2004-09-20 06:23:30 +0800 (周一, 20 9月 2004) $
 * @since 20.02.2004
 */
public final class FileDTO implements Serializable {

    private int pk;

    private String aets;

    private String basedir;

    private String path;

    private String tsuid;

    private int size;

    private byte[] md5;

    /**
     * @return Returns the pk.
     */
    public final int getPk() {
        return pk;
    }

    /**
     * @param pk The pk to set.
     */
    public final void setPk(int pk) {
        this.pk = pk;
    }

    /**
     * @return Returns the aets.
     */
    public final String getRetrieveAETs() {
        return aets;
    }

    /**
     * @param aets The aets to set.
     */
    public final void setRetrieveAETs(String aets) {
        this.aets = aets;
    }

    /**
     * @return Returns the baseDir.
     */
    public final String getDirectoryPath() {
        return basedir;
    }

    /**
     * @param baseDir The baseDir to set.
     */
    public final void setDirectoryPath(String baseDir) {
        this.basedir = baseDir;
    }

    /**
     * @return Returns the md5.
     */
    public final byte[] getFileMd5() {
        return md5;
    }

    /**
     * @param md5 The md5 to set.
     */
    public final void setFileMd5(byte[] md5) {
        this.md5 = md5;
    }

    /**
     * @return Returns the path.
     */
    public final String getFilePath() {
        return path;
    }

    /**
     * @param path The path to set.
     */
    public final void setFilePath(String path) {
        this.path = path;
    }

    /**
     * @return Returns the size.
     */
    public final int getFileSize() {
        return size;
    }

    /**
     * @param size The size to set.
     */
    public final void setFileSize(int size) {
        this.size = size;
    }

    /**
     * @return Returns the tsuid.
     */
    public final String getFileTsuid() {
        return tsuid;
    }

    /**
     * @param tsuid The tsuid to set.
     */
    public final void setFileTsuid(String tsuid) {
        this.tsuid = tsuid;
    }
    
    public final File getFile() {
        return new File(basedir.replace('/', File.separatorChar),
                path.replace('/', File.separatorChar));
    }

}