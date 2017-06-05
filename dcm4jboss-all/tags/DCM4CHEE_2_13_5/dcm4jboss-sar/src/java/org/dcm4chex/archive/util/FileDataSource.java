/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.util;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.FileImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DataSource;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.codec.DecompressCmd;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 5615 $
 * @since 18.09.2003
 */
public class FileDataSource implements DataSource {

    private static final Logger log = Logger.getLogger(FileDataSource.class);
    private static final Dataset EXTRACTED_FRAMES;

    static { //TODO final DCM code value not yet available
        EXTRACTED_FRAMES = DcmObjectFactory.getInstance().newDataset();
        EXTRACTED_FRAMES.putLO(Tags.CodeValue, "121350");
        EXTRACTED_FRAMES.putSH(Tags.CodingSchemeDesignator, "99DCM4CHEE");
        EXTRACTED_FRAMES.putLO(Tags.CodeMeaning, "EXTRACTED FRAMES");
    }
    private final File file;
    private final Dataset mergeAttrs;
    private final byte[] buffer;

    /** if true use Dataset.writeFile instead of writeDataset */
    private boolean writeFile = false;
    private boolean withoutPixeldata = false;
    private boolean excludePrivate = false;
    private int[] simpleFrameList;
    private int[] calculatedFrameList;
    private Dataset contributingEquipment;

    public FileDataSource(File file, Dataset mergeAttrs, byte[] buffer) {
        this.file = file;
        this.mergeAttrs = mergeAttrs;
        this.buffer = buffer;
    }

    /**
     * @return Returns the writeFile.
     */
    public final boolean isWriteFile() {
        return writeFile;
    }

    /**
     * Set the write method (file or net).
     * <p>
     * If true, this datasource use writeFile instead of writeDataset. Therefore
     * the FileMetaInfo will be only written if writeFile is set to true
     * explicitly!
     * 
     * @param writeFile
     *                The writeFile to set.
     */
    public final void setWriteFile(boolean writeFile) {
        this.writeFile = writeFile;
    }

    public final boolean isWithoutPixeldata() {
        return withoutPixeldata;
    }

    public final void setWithoutPixeldata(boolean withoutPixelData) {
        this.withoutPixeldata = withoutPixelData;
    }

    public final boolean isExcludePrivate() {
        return excludePrivate;
    }

    public final void setExcludePrivate(boolean excludePrivate) {
        this.excludePrivate = excludePrivate;
    }

    public final void setSimpleFrameList(int[] simpleFrameList) {
        if (calculatedFrameList != null) {
            if (calculatedFrameList != null) {
                throw new IllegalStateException();
            }
            if (simpleFrameList.length == 0) {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < simpleFrameList.length; i++) {
                if (simpleFrameList[i] <= 0) {
                    throw new IllegalArgumentException();
                }
                if (i != 0 && calculatedFrameList[i]
                           <= calculatedFrameList[i-1]) {
                    throw new IllegalArgumentException();
                }
            }
        }
        this.simpleFrameList = simpleFrameList;
    }

    public final void setCalculatedFrameList(int[] calculatedFrameList) {
        if (calculatedFrameList != null) {
            if (simpleFrameList != null) {
                throw new IllegalStateException();
            }
            if (calculatedFrameList.length == 0) {
                throw new IllegalArgumentException();
            }
            if (calculatedFrameList.length % 3 != 0) {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < calculatedFrameList.length; i++) {
                if (calculatedFrameList[i] <= 0) {
                    throw new IllegalArgumentException();
                }
                switch (i % 3) {
                case 0:
                    if (i != 0 && calculatedFrameList[i]
                               <= calculatedFrameList[i-2]) {
                        throw new IllegalArgumentException();
                    }
                    break;
                case 1:
                    if (i != 0 && calculatedFrameList[i]
                               < calculatedFrameList[i-1]) {
                        throw new IllegalArgumentException();
                    }
                    break;
                }
            }
        }
        this.calculatedFrameList = calculatedFrameList;
    }

    public final void setContributingEquipment(Dataset contributingEquipment) {
        this.contributingEquipment = contributingEquipment;
    }

    public final File getFile() {
        return file;
    }

    public final Dataset getMergeAttrs() {
        return mergeAttrs;
    }

    public void writeTo(OutputStream out, String tsUID) throws IOException {

        log.info("M-READ file:" + file);
        FileImageInputStream fiis = new FileImageInputStream(file);
        try {
            DcmParser parser = DcmParserFactory.getInstance()
                    .newDcmParser(fiis);
            Dataset ds = DcmObjectFactory.getInstance().newDataset();
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDcmFile(null, Tags.PixelData);
            if (!parser.hasSeenEOF() && parser.getReadTag() != Tags.PixelData) {
                parser.unreadHeader();
                parser.parseDataset(parser.getDcmDecodeParam(), -1);
            }
            ds.putAll(mergeAttrs);
            int framesInFile = ds.getInt(Tags.NumberOfFrames, 1);
            if (simpleFrameList != null) {
                if (simpleFrameList[simpleFrameList.length - 1] > framesInFile) {
                    throw new RequestedFrameNumbersOutOfRangeException();
                }
            } else if (calculatedFrameList != null) {
                if (calculatedFrameList[0] > framesInFile) {
                    throw new RequestedFrameNumbersOutOfRangeException();
                }
                simpleFrameList = calculateFrameList(framesInFile);
            }
            if (framesInFile == 1) {
                simpleFrameList = null;
            }
            if (simpleFrameList != null) {
                addSourceImageSeq(ds);
                addContributingEquipmentSeq(ds);
                adjustNumberOfFrames(ds);
                replaceIUIDs(ds);
            }
            String tsOrig = DecompressCmd.getTransferSyntax(ds);
            if (writeFile) {
                if (tsUID != null) {
                    if (tsUID.equals(UIDs.ExplicitVRLittleEndian)
                            || !tsUID.equals(tsOrig)) { // can only decompress
                                                        // here!
                        tsUID = UIDs.ExplicitVRLittleEndian;
                        ds.setFileMetaInfo(DcmObjectFactory.getInstance()
                                .newFileMetaInfo(ds, tsUID));
                    }
                } else {
                    tsUID = tsOrig;
                }
            }
            DcmEncodeParam enc = DcmEncodeParam.valueOf(tsUID);
            if (withoutPixeldata || parser.getReadTag() != Tags.PixelData) {
                log.debug("Dataset:\n");
                log.debug(ds);
                write(ds, out, enc);
                return;
            }
            int len = parser.getReadLength();
            if (len == -1 && !enc.encapsulated) {
                DecompressCmd cmd = new DecompressCmd(ds, tsOrig, parser);
                cmd.setSimpleFrameList(simpleFrameList);
                len = cmd.getPixelDataLength();
                log.debug("Dataset:\n");
                log.debug(ds);
                write(ds, out, enc);
                ds.writeHeader(out, enc, Tags.PixelData, VRs.OW, 
                        (len + 1) & ~1);
                try {
                    cmd.decompress(enc.byteOrder, out);
                } catch (IOException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException("Decompression failed:", e);
                }
                if ((len & 1) != 0)
                    out.write(0);
            } else {
                log.debug("Dataset:\n");
                log.debug(ds);
                write(ds, out, enc);
                if (len == -1) {
                    ds.writeHeader(out, enc, Tags.PixelData, VRs.OB, len);
                    parser.parseHeader();
                    int itemlen;
                    if (simpleFrameList != null) {
                        itemlen = parser.getReadLength();
                        ds.writeHeader(out, enc, Tags.Item, VRs.NONE, 0);
                        fiis.skipBytes(itemlen);
                        parser.parseHeader();
                        for (int srcFrame = 1, destFrame = 0;
                                parser.getReadTag() == Tags.Item; srcFrame++) {
                            itemlen = parser.getReadLength();
                            if (destFrame < simpleFrameList.length
                                    && srcFrame == simpleFrameList[destFrame]) {
                                ds.writeHeader(out, enc, Tags.Item, VRs.NONE, itemlen);
                                copy(fiis, out, itemlen, buffer);
                                destFrame++;
                            } else {
                                fiis.skipBytes(itemlen);
                            }
                            parser.parseHeader();
                        }
                    } else {
                        while (parser.getReadTag() == Tags.Item) {
                            itemlen = parser.getReadLength();
                            ds.writeHeader(out, enc, Tags.Item, VRs.NONE, itemlen);
                            copy(fiis, out, itemlen, buffer);
                            parser.parseHeader();
                        }
                    }
                    ds.writeHeader(out, enc, Tags.SeqDelimitationItem,
                            VRs.NONE, 0);
                } else if (simpleFrameList != null) {
                    int frameLength = len / framesInFile;
                    int newPixelDataLength =
                        frameLength * simpleFrameList.length;
                    ds.writeHeader(out, enc, Tags.PixelData, VRs.OW,
                            (newPixelDataLength+1)&~1);
                    long pixelDataPos = fiis.getStreamPosition();
                    for (int i = 0; i < simpleFrameList.length; i++) {
                        fiis.seek(pixelDataPos
                                + frameLength * (simpleFrameList[i]-1));
                        copy(fiis, out, frameLength, buffer);
                    }
                    if ((newPixelDataLength & 1) != 0)
                        out.write(0);
                    fiis.seek(pixelDataPos + len);
                } else {
                    ds.writeHeader(out, enc, Tags.PixelData, VRs.OW, len);
                    copy(fiis, out, len, buffer);
                }
            }
            parser.parseDataset(parser.getDcmDecodeParam(), -1);
            ds.subSet(Tags.PixelData, -1).writeDataset(out, enc);
        } finally {
            try {
                fiis.close();
            } catch (IOException ignore) {
            }
        }
    }
    
    private void adjustNumberOfFrames(Dataset ds) {
        ds.putIS(Tags.NumberOfFrames, simpleFrameList.length);
        DcmElement src = ds.remove(Tags.PerFrameFunctionalGroupsSeq);
        if (src != null) {
            DcmElement dest = ds.putSQ(Tags.PerFrameFunctionalGroupsSeq);
            for (int i = 0; i < simpleFrameList.length; i++) {
                dest.addItem(src.getItem(simpleFrameList[i]-1));
            }
        }
    }

    private void addContributingEquipmentSeq(Dataset ds) {
        if (contributingEquipment != null) {
            getOrPutSQ(ds, Tags.ContributingEquipmentSeq)
                    .addItem(contributingEquipment);
        }
    }

    private void addSourceImageSeq(Dataset ds) {
        DcmElement seq = getOrPutSQ(ds, Tags.SourceImageSeq);
        Dataset item = seq.addNewItem();
        item.putUI(Tags.RefSOPClassUID, ds.getString(Tags.SOPClassUID));
        item.putUI(Tags.RefSOPInstanceUID, ds.getString(Tags.SOPInstanceUID));
        item.putIS(Tags.RefFrameNumber, simpleFrameList);
        item.putSQ(Tags.PurposeOfReferenceCodeSeq).addItem(EXTRACTED_FRAMES);
    }

    private DcmElement getOrPutSQ(Dataset ds, int tag) {
        DcmElement seq = ds.putSQ(Tags.SourceImageSeq);
        return seq != null ? seq : ds.putSQ(Tags.SourceImageSeq);
    }

    private void replaceIUIDs(Dataset ds) {
        UIDGenerator uidgen = UIDGenerator.getInstance();
        ds.putUI(Tags.SOPInstanceUID, uidgen.createUID());
        ds.putUI(Tags.SeriesInstanceUID, uidgen.createUID());
    }
    
    private int[] calculateFrameList(int frames) {
        int[] src = new int[frames];
        int length = 0;
        addFrame:
        for (int i = 0; i < calculatedFrameList.length;) {
            for (int f = calculatedFrameList[i++],
                    last = calculatedFrameList[i++],
                    step = calculatedFrameList[i++];
                    f <= last; f += step) {
                if (f > frames) {
                    break addFrame;
                }
                src[length++] = f;
            }
        }
        int[] dest = new int[length];
        System.arraycopy(src, 0, dest, 0, length);
        return dest;
    }

    private void write(Dataset ds, OutputStream out, DcmEncodeParam enc)
            throws IOException {
        if (writeFile) {
            if (excludePrivate) {
                Dataset dsOut = ds.excludePrivate();
                dsOut.setFileMetaInfo(ds.getFileMetaInfo());
                dsOut.writeFile(out, enc);
            } else {
                ds.writeFile(out, enc);
            }
        } else {
            if (excludePrivate)
                ds.excludePrivate().writeDataset(out, enc);
            else
                ds.writeDataset(out, enc);
        }
        return;

    }

    private void copy(FileImageInputStream fiis, OutputStream out, int totLen,
            byte[] buffer) throws IOException {
        for (int len, toRead = totLen; toRead > 0; toRead -= len) {
            len = fiis.read(buffer, 0, Math.min(toRead, buffer.length));
            if (len == -1) {
                throw new EOFException();
            }
            out.write(buffer, 0, len);
        }
    }
}
