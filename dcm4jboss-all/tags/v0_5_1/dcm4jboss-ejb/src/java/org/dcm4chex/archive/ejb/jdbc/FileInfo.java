/* $Id: FileInfo.java 962 2004-02-11 15:04:04Z gunterze $
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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4cheri.util.DatasetUtils;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public class FileInfo
{
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
        int size)
    {
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

    public String toString()
    {
        return "FileInfo[iuid="
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

    public File toFile()
    {
        String fpath = basedir + '/' + fileID;
        return new File(fpath.replace('/', File.separatorChar));
    }

    public Dataset getPatientAttrs()
    {
        return DatasetUtils.fromByteArray(patAttrs, DcmDecodeParam.IVR_LE);
    }

    public Dataset getStudyAttrs()
    {
        return DatasetUtils.fromByteArray(studyAttrs, DcmDecodeParam.IVR_LE);
    }

    public Dataset getSeriesAttrs()
    {
        return DatasetUtils.fromByteArray(seriesAttrs, DcmDecodeParam.IVR_LE);
    }

    public Dataset getInstanceAttrs()
    {
        return DatasetUtils.fromByteArray(instAttrs, DcmDecodeParam.IVR_LE);
    }

}
