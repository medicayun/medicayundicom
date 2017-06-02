/*$Id: DcmParser.java 3857 2005-01-30 22:10:25Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.data;

import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.VRMap;

import org.xml.sax.ContentHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface DcmParser {

    InputStream getInputStream();

    ImageInputStream getImageInputStream();

    long getStreamPosition();

    void setStreamPosition(long pos);

    void seek(long pos) throws IOException;

    void setDcmHandler(DcmHandler handler);

    void setSAXHandler(ContentHandler hc, TagDictionary dict);

    void setSAXHandler2(ContentHandler hc, TagDictionary dict,
            int[] excludeTags, int excludeValueLengthLimit, File basedir);

    void setVRMap(VRMap vrMap);

    void setDcmDecodeParam(DcmDecodeParam decodeParam);

    DcmDecodeParam getDcmDecodeParam();

    FileFormat detectFileFormat() throws IOException;

    int parseHeader() throws IOException;

    long parseFileMetaInfo(boolean preamble, DcmDecodeParam param)
            throws IOException;

    long parseFileMetaInfo() throws IOException;

    long parseCommand() throws IOException;

    long parseDataset(String tuid, int stopTag) throws IOException;

    long parseDataset(DcmDecodeParam param, int stopTag) throws IOException;

    long parseDcmFile(FileFormat format, int stopTag) throws IOException;

    long parseItemDataset() throws IOException;

    int getReadTag();

    int getReadVR();

    int getReadLength();

    boolean hasSeenEOF();
}

