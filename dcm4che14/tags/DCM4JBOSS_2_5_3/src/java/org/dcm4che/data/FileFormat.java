/*$Id: FileFormat.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.data;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class FileFormat {

    public final boolean hasPreamble;
    
    public final boolean hasFileMetaInfo;
    
    public final DcmDecodeParam decodeParam;
    
    private FileFormat(boolean hasPreamble, boolean hasFileMetaInfo,
            DcmDecodeParam decodeParam) {
        if (hasPreamble && !hasFileMetaInfo) {
            throw new IllegalArgumentException("Preamble without FMI");
        }
        this.hasPreamble = hasPreamble;
        this.hasFileMetaInfo = hasFileMetaInfo;
        this.decodeParam = decodeParam;
    }
    
    public String toString() {
        return "FileFormat["
            + (hasFileMetaInfo ? 
                        (hasPreamble ? "Part 10,"
                                     : "FMI without preamble,")
                               : "Stream, ")
            + decodeParam.toString() + "]";
    }
    
    public static final FileFormat DICOM_FILE =
            new FileFormat(true, true, DcmDecodeParam.EVR_LE);

    public static final FileFormat DICOM_FILE_WO_PREAMBLE =
            new FileFormat(false, true, DcmDecodeParam.EVR_LE);

    public static final FileFormat EVR_LE_STREAM =
            new FileFormat(false, false, DcmDecodeParam.EVR_LE);

    public static final FileFormat EVR_BE_FILE =
            new FileFormat(true, true, DcmDecodeParam.EVR_BE);

    public static final FileFormat EVR_BE_FILE_WO_PREAMBLE =
            new FileFormat(false, true, DcmDecodeParam.EVR_BE);

    public static final FileFormat EVR_BE_STREAM =
            new FileFormat(false, false, DcmDecodeParam.EVR_BE);

    public static final FileFormat IVR_BE_FILE =
            new FileFormat(true, true, DcmDecodeParam.IVR_BE);

    public static final FileFormat IVR_BE_FILE_WO_PREAMBLE =
            new FileFormat(false, true, DcmDecodeParam.IVR_BE);

    public static final FileFormat IVR_BE_STREAM =
            new FileFormat(false, false, DcmDecodeParam.IVR_BE);

    public static final FileFormat IVR_LE_FILE =
            new FileFormat(true, true, DcmDecodeParam.IVR_LE);

    public static final FileFormat IVR_LE_FILE_WO_PREAMBLE =
            new FileFormat(false, true, DcmDecodeParam.IVR_LE);

    public static final FileFormat ACRNEMA_STREAM =
            new FileFormat(false, false, DcmDecodeParam.IVR_LE);
}
