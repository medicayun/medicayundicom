/* $Id: FileDTO.java 1006 2004-02-22 20:09:10Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.interfaces;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1006 $ $Date: 2004-02-23 04:09:10 +0800 (周一, 23 2月 2004) $
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

    public File toFile() {
        String uri = "file:" + basedir + '/' + path;
        try {
            return new File(new URI(uri));
        } catch (URISyntaxException e) {
            throw new RuntimeException(uri, e);            
        }
    }
}
