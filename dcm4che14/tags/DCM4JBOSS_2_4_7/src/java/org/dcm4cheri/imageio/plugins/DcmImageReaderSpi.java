/*$Id: DcmImageReaderSpi.java 3656 2003-06-09 17:17:51Z gunterze $*/
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

package org.dcm4cheri.imageio.plugins;

import org.dcm4che.data.DcmParser;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DcmImageReaderSpi extends ImageReaderSpi {

    static final String vendorName = "TIANI MEDGRAPH AG";

    static final String version = "1.0";

    static final String[] names = { "DICOM" };

    static final String[] suffixes = { "dcm" };

    static final String[] MIMETypes = { "Application/dicom" };

    static final String readerClassName =
            "org.dcm4cheri.imageio.plugins.DcmImageReader";

    private static final String[] writerSpiNames = null;

    static final boolean supportsStandardStreamMetadataFormat = false;
    static final String nativeStreamMetadataFormatName = 
            org.dcm4che.imageio.plugins.DcmMetadata.nativeMetadataFormatName;
    static final String nativeStreamMetadataFormatClassName = 
            "org.dcm4che.imageio.plugins.DcmMetadataFormat";
    
    static final DcmImageReaderConf conf = DcmImageReaderConf.getInstance();
    static final boolean supportsStandardImageMetadataFormat = false;
    static final String nativeImageMetadataFormatName = null;
    static final String nativeImageMetadataFormatClassName = null;
    static final String[] extraImageMetadataFormatNames = null;
    static final String[] extraImageMetadataFormatClassNames = null;

    public DcmImageReaderSpi() {
            super(vendorName, version,
                  names, suffixes, MIMETypes,
                  readerClassName,
                  STANDARD_INPUT_TYPE, // Accept ImageInputStreams
                  writerSpiNames,
                  supportsStandardStreamMetadataFormat,
                  nativeStreamMetadataFormatName,
                  nativeStreamMetadataFormatClassName,
                  conf.getExtraStreamMetadataFormatNames(),
                  conf.getExtraStreamMetadataFormatClassNames(),
                  supportsStandardImageMetadataFormat,
                  nativeImageMetadataFormatName,
                  nativeImageMetadataFormatClassName,
                  extraImageMetadataFormatNames,
                  extraImageMetadataFormatClassNames);
    }

    public String getDescription(Locale locale) {
        return "DICOM image reader";
    }

    public boolean canDecodeInput(Object input) throws IOException {
        if (!(input instanceof ImageInputStream)) {
            return false;
        }
        
        DcmParser parser =
                DcmImageReader.pfact.newDcmParser((ImageInputStream)input);
        return parser.detectFileFormat() != null;
    }

    public ImageReader createReaderInstance(Object extension) {
        return new DcmImageReader(this);
    }

}
