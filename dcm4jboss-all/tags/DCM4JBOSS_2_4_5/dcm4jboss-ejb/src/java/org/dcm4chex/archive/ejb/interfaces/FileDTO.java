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
 * @version $Revision: 1734 $ $Date: 2005-05-11 21:06:25 +0800 (周三, 11 5月 2005) $
 * @since 20.02.2004
 */
public final class FileDTO implements Serializable {

    private static final long serialVersionUID = 3617574916212536371L;
    
    private int pk;

    private String aet;

    private String basedir;

    private String path;

    private String tsuid;

    private int size;

    private byte[] md5;
    
    private int status;

	public static final int COMPRESS_FAILED = -1;

	public static final int VERIFY_COMPRESS_FAILED = -2;

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
     * @return Returns the aet.
     */
    public final String getRetrieveAET() {
        return aet;
    }

    /**
     * @param aets The aets to set.
     */
    public final void setRetrieveAET(String aet) {
        this.aet = aet;
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

    public final String getMd5String() {
    	return MD5.toString(md5);
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
    
    public final int getFileStatus() {
        return status;
    }

    public final void setFileStatus(int status) {
        this.status = status;
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

}