/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.dcm.qrscp;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.stream.FileImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DataSource;
import org.dcm4chex.archive.codec.DecompressCmd;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.util.FileUtils;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1607 $
 * @since 18.09.2003
 */
class FileDataSource implements DataSource {

    private final QueryRetrieveScpService service;
    private final FileInfo fileInfo;
    private final byte[] buffer;

    // buffer == null => send no Pixeldata
    public FileDataSource(
        QueryRetrieveScpService service,
        FileInfo fileInfo,
        byte[] buffer) {
        this.service = service;
        this.fileInfo = fileInfo;
        this.buffer = buffer;
    }

    public void writeTo(OutputStream out, String tsUID) throws IOException {
        DcmEncodeParam enc = DcmEncodeParam.valueOf(tsUID);
        File file = FileUtils.toFile(fileInfo.basedir, fileInfo.fileID);
        service.getLog().info("M-READ file:" + file);
        FileImageInputStream fiis = new FileImageInputStream(file);
        try {
            DcmParser parser = QueryRetrieveScpService.paf.newDcmParser(fiis);
            Dataset ds = QueryRetrieveScpService.dof.newDataset();
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
            updateAttrs(ds, fileInfo.patAttrs);
            updateAttrs(ds, fileInfo.studyAttrs);
            updateAttrs(ds, fileInfo.seriesAttrs);
            updateAttrs(ds, fileInfo.instAttrs);
            if (parser.getReadTag() != Tags.PixelData || buffer == null) {
                service.logDataset("Dataset:\n", ds);
                ds.writeDataset(out, enc);
                return;                
            }
            int len = parser.getReadLength();
            if (len == -1 && !enc.encapsulated) {
                DecompressCmd cmd = new DecompressCmd(ds, parser);
                len = cmd.getPixelDataLength();
                service.logDataset("Dataset:\n", ds);
                ds.writeDataset(out, enc);
                ds.writeHeader(out, enc, Tags.PixelData, VRs.OW, (len+1)&~1);
                try {
	                cmd.decompress(enc.byteOrder, out);
				} catch (IOException e) {
				    throw e;
				} catch (Throwable e) {
				    throw new RuntimeException("Decompression failed:", e);
				}
				if ((len&1)!=0)
                    out.write(0);
            } else {
                service.logDataset("Dataset:\n", ds);
                ds.writeDataset(out, enc);
                ds.writeHeader(out, enc, Tags.PixelData, VRs.OB, len);
                if (len == -1) {
		            parser.parseHeader();
		            int itemlen;
	                while (parser.getReadTag() == Tags.Item) {
	                    itemlen = parser.getReadLength();
	                    ds.writeHeader(out, enc, Tags.Item, VRs.NONE, itemlen);
	                    copy(fiis, out, itemlen, buffer);
	                    parser.parseHeader();
	                }
	                ds.writeHeader(out, enc, Tags.SeqDelimitationItem,
	                        VRs.NONE, 0);
	            } else {
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

    private void copy(FileImageInputStream fiis, OutputStream out, int totLen,
            byte[] buffer) throws IOException {
        for (int len, toRead = totLen; toRead > 0; toRead -= len) {
            len = fiis.read(buffer, 0, Math.min(toRead, buffer.length));
            if (len == -1) { throw new EOFException(); }
            out.write(buffer, 0, len);
        }
    }

    private void updateAttrs(Dataset ds, byte[] attrs) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(attrs);
        DcmParser parser = QueryRetrieveScpService.paf.newDcmParser(bis);
        parser.setDcmHandler(ds.getDcmHandler());
        parser.parseDataset(DcmDecodeParam.EVR_LE, -1);
        bis.close();
    }
}
