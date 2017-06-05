/* $Id: FileInfo.java 1026 2004-03-03 18:22:28Z gunterze $
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4cheri.util.StringUtils;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
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
    public final String sopIUID;
    public final String sopCUID;
    public final String retrieveAETs;
    public final String basedir;
    public final String fileID;
    public final String tsUID;
    public final String md5;
    public final int size;

    public FileInfo(
        int pk,
        byte[] patAttrs,
        byte[] studyAttrs,
        byte[] seriesAttrs,
        byte[] instAttrs,
        String sopIUID,
        String sopCUID,
        String retrieveAETs,
        String basedir,
        String fileID,
        String tsUID,
        String md5,
        int size) {
        this.pk = pk;
        this.patAttrs = patAttrs;
        this.studyAttrs = studyAttrs;
        this.seriesAttrs = seriesAttrs;
        this.instAttrs = instAttrs;
        this.sopIUID = sopIUID;
        this.sopCUID = sopCUID;
        this.retrieveAETs = retrieveAETs;
        this.basedir = basedir;
        this.fileID = fileID;
        this.tsUID = tsUID;
        this.md5 = md5;
        this.size = size;
    }

    public String toString() {
        return "FileInfo[pk="
            + pk
            + "iuid="
            + sopIUID
            + ", cuid="
            + sopCUID
            + ", retrieveAETs="
            + retrieveAETs
            + ", basedir="
            + basedir
            + ", fileid="
            + fileID
            + ", tsuid="
            + tsUID;
    }

    public File toFile() {
        String uri = "file:" + basedir + '/' + fileID;
        try {
            return new File(new URI(uri));
        } catch (URISyntaxException e) {
            throw new RuntimeException(uri, e);
        }
    }

    public Dataset getPatientAttrs() {
        return DatasetUtils.fromByteArray(patAttrs, DcmDecodeParam.IVR_LE);
    }

    public Dataset getStudyAttrs() {
        return DatasetUtils.fromByteArray(studyAttrs, DcmDecodeParam.IVR_LE);
    }

    public Dataset getSeriesAttrs() {
        return DatasetUtils.fromByteArray(seriesAttrs, DcmDecodeParam.IVR_LE);
    }

    public Dataset getInstanceAttrs() {
        return DatasetUtils.fromByteArray(instAttrs, DcmDecodeParam.IVR_LE);
    }

    public Set getRetrieveAETSet() {
        return new HashSet(
            Arrays.asList(StringUtils.split(retrieveAETs, '\\')));
    }

    public byte[] getFileMd5()
    {
        char[] md5Hex = md5.toCharArray();
        byte[] retval = new byte[16];
        for (int i = 0; i < retval.length; i++)
        {
            retval[i] =
                (byte) ((Character.digit(md5Hex[i << 1], 16) << 4)
                    + Character.digit(md5Hex[(i << 1) + 1], 16));
        }
        return retval;
    }
}
