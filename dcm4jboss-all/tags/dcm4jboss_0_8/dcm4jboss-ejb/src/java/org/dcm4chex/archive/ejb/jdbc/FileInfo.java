/* $Id: FileInfo.java 1113 2004-05-10 15:31:56Z gunterze $
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
package org.dcm4chex.archive.ejb.jdbc;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger </a>
 *  
 */
public class FileInfo {

    public static final Comparator ASC_ORDER = new Comparator() {

        public int compare(Object o1, Object o2) {
            return ((FileInfo) o1).pk - ((FileInfo) o2).pk;
        }
    };

    public static final Comparator DESC_ORDER = new Comparator() {

        public int compare(Object o1, Object o2) {
            return ((FileInfo) o2).pk - ((FileInfo) o1).pk;
        }
    };

    public final int pk;

    public final byte[] patAttrs;

    public final byte[] studyAttrs;

    public final byte[] seriesAttrs;

    public final byte[] instAttrs;

    public final String patID;

    public final String patName;

    public final String studyIUID;

    public final String sopIUID;

    public final String sopCUID;

    public final String instRetrieveAETs;
    
    public final String fileRetrieveAETs;

    public final String basedir;

    public final String fileID;

    public final String tsUID;

    public final String md5;

    public final int size;

    public FileInfo(int pk, String patID, String patName, byte[] patAttrs,
            String studyIUID, byte[] studyAttrs, byte[] seriesAttrs,
            byte[] instAttrs, String sopIUID, String sopCUID,
            String instRetrieveAETs, String fileRetrieveAETs, String basedir,
            String fileID, String tsUID, String md5, int size) {
        this.pk = pk;
        this.patID = patID;
        this.patName = patName;
        this.patAttrs = patAttrs;
        this.studyIUID = studyIUID;
        this.studyAttrs = studyAttrs;
        this.seriesAttrs = seriesAttrs;
        this.instAttrs = instAttrs;
        this.sopIUID = sopIUID;
        this.sopCUID = sopCUID;
        this.instRetrieveAETs = instRetrieveAETs;
        this.fileRetrieveAETs = fileRetrieveAETs;
        this.basedir = basedir;
        this.fileID = fileID;
        this.tsUID = tsUID;
        this.md5 = md5;
        this.size = size;
    }

    public String toString() {
        return "FileInfo[pk=" + pk + "iuid=" + sopIUID + ", cuid=" + sopCUID
                + ", instRetrieveAETs=" + instRetrieveAETs
                + ", fileRetrieveAETs=" + fileRetrieveAETs + ", basedir="
                + basedir + ", fileid=" + fileID + ", tsuid=" + tsUID;
    }

    public File toFile() {
        String uri = (basedir.charAt(0) == '/' ? "file:" : "file:/") + basedir + '/' + fileID;
        try {
            return new File(new URI(uri));
        } catch (URISyntaxException e) {
            throw new RuntimeException(uri, e);
        }
    }

    public byte[] getFileMd5() {
        char[] md5Hex = md5.toCharArray();
        byte[] retval = new byte[16];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = (byte) ((Character.digit(md5Hex[i << 1], 16) << 4) + Character
                    .digit(md5Hex[(i << 1) + 1], 16));
        }
        return retval;
    }
}
