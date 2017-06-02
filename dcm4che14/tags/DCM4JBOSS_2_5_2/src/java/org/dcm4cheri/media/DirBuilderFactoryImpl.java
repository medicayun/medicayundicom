/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

/*$Id: DirBuilderFactoryImpl.java 3783 2004-06-30 17:50:18Z gunterze $*/

package org.dcm4cheri.media;

import java.io.File;
import java.io.IOException;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.UIDs;
import org.dcm4che.media.DirBuilder;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirBuilderPref;
import org.dcm4che.media.DirReader;
import org.dcm4che.media.DirWriter;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class DirBuilderFactoryImpl extends DirBuilderFactory {

    /** Creates a new instance of DcmParserFactoryImpl */
    public DirBuilderFactoryImpl() {
    }

    public DirReader newDirReader(File file) throws IOException {
        return new DirReaderImpl(file,
                new FileImageInputStream(file)).initReader();
    }    

    public DirReader newDirReader(ImageInputStream in) throws IOException {
        return new DirReaderImpl(null, in).initReader();
    }    

    public DirWriter newDirWriter(File file, DcmEncodeParam encParam)
            throws IOException {
        return new DirWriterImpl(file,
                new FileImageOutputStream(file), encParam).initWriter();
    }
    
    public DirWriter newDirWriter(File file,  FileMetaInfo fmi,
            String filesetID, File descriptorFile, String specCharset,
            DcmEncodeParam encParam) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        return new DirWriterImpl(file,
                new FileImageOutputStream(file), encParam).initWriter(
                        fmi, filesetID, descriptorFile, specCharset);
    }

    public DirWriter newDirWriter(File file, String uid, String filesetID,
            File descriptorFile, String specCharset, DcmEncodeParam encParam)
            throws IOException {
        return newDirWriter(file,
                DcmObjectFactory.getInstance().newFileMetaInfo(
                        UIDs.MediaStorageDirectoryStorage, uid,
                        UIDs.ExplicitVRLittleEndian),
                filesetID, descriptorFile, specCharset, encParam);
    }

    public DirBuilder newDirBuilder(DirWriter writer, DirBuilderPref pref) {
        return new DirBuilderImpl(writer, pref);
    }
    
    public DirBuilderPref newDirBuilderPref() {
        return new DirBuilderPrefImpl();
    }

}
