/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.codec;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4cheri.image.ItemParser;

import com.sun.media.imageio.stream.SegmentedImageInputStream;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1612 $ $Date: 2005-03-16 21:41:50 +0800 (周三, 16 3月 2005) $
 * @since 22.05.2004
 *
 */
public class DecompressCmd extends CodecCmd {

    private final String tsuid;

    private final DcmParser parser;

    private final ItemParser itemParser;
    
    private final ImageInputStream iis;

    public static byte[] decompressFile(File inFile, File outFile, String tsuid,
    		int pxdataVR)
		throws Exception {
        log.info("M-READ file:" + inFile);
        FileImageInputStream fiis = new FileImageInputStream(inFile);
        try {
            DcmParser parser = DcmParserFactory.getInstance().newDcmParser(fiis);
            DcmObjectFactory dof = DcmObjectFactory.getInstance();
            Dataset ds = dof.newDataset();
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
            log.info("M-WRITE file:" + outFile);
            BufferedOutputStream os = new BufferedOutputStream(
                    new FileOutputStream(outFile));
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestOutputStream dos = new DigestOutputStream(os, md);
            try {
    			DcmEncodeParam encParam = DcmEncodeParam.valueOf(tsuid);
                DecompressCmd cmd = new DecompressCmd(ds, parser);
                int len = cmd.getPixelDataLength();
        		FileMetaInfo fmi = dof.newFileMetaInfo(ds, tsuid);
        		ds.setFileMetaInfo(fmi);
                ds.writeFile(dos, encParam);
                ds.writeHeader(dos, encParam, Tags.PixelData, pxdataVR, (len+1)&~1);
                try {
	                cmd.decompress(encParam.byteOrder, dos);
				} catch (IOException e) {
				    throw e;
				} catch (Throwable e) {
				    throw new RuntimeException("Decompression failed:", e);
				}
				if ((len&1)!=0)
					dos.write(0);
				parser.parseDataset(parser.getDcmDecodeParam(), -1);
				ds.subSet(Tags.PixelData, -1).writeDataset(dos, encParam);
            } finally {
            	dos.close();
            }
            return md.digest();
        } finally {
            try {
                fiis.close();
            } catch (IOException ignore) {
            }
        }
    }
    
    public DecompressCmd(Dataset ds, DcmParser parser) throws IOException {
    	super(ds);
        this.parser = parser;
        this.iis = parser.getImageInputStream();
        this.itemParser = new ItemParser(parser);
        tsuid = ds.getFileMetaInfo().getTransferSyntaxUID();
        if (samples == 3) ds.putCS(Tags.PhotometricInterpretation, "RGB");
    }

    protected ImageReader getReaderForTransferSyntax(String ts) {
        if (ts.equals(UIDs.JPEG2000Lossless) || ts.equals(UIDs.JPEG2000Lossy))
                return getImageReader(JPEG2000,
                        useNative ? J2K_IMAGE_READER_CODEC_LIB
                                : J2K_IMAGE_READER);
        if (ts.equals(UIDs.JPEGBaseline))
                return getImageReader(JPEG, useNative ? CLIB_JPEG_IMAGE_READER
                        : JPEG_IMAGE_READER);
        // only supported by native CLibJPEGImageReader
        if (ts.equals(UIDs.JPEGExtended) || ts.equals(UIDs.JPEGLossless)
                || ts.equals(UIDs.JPEGLossless14)
                || ts.equals(UIDs.JPEGLSLossless)
                || ts.equals(UIDs.JPEGLSLossy))
                return getImageReader(JPEG, CLIB_JPEG_IMAGE_READER);
        throw new UnsupportedOperationException(
                "No Image Reader available for Transfer Syntax:" + ts);
    }
    
    public void decompress(ByteOrder byteOrder, OutputStream out)
            throws Exception {
        long t1;
        ImageReader reader = null;
        BufferedImage bi = null;
        boolean codecSemaphoreAquired = false;
        try {
            log.debug("acquire codec semaphore");
            codecSemaphore.acquire();
            codecSemaphoreAquired = true;
            log.info("start decompression of image: " + rows + "x" + columns
                    + "x" + frames);
            t1 = System.currentTimeMillis();
            SegmentedImageInputStream siis = new SegmentedImageInputStream(iis, itemParser);
            reader = getReaderForTransferSyntax(tsuid);
            for (int i = 0; i < frames; ++i) {
                log.debug("start decompression of frame #" + (i + 1));
                reader.setInput(siis);
                ImageReadParam param = reader.getDefaultReadParam();
                if (bi != null) param.setDestination(bi);
                bi = reader.read(0, param);
                reader.reset();
                itemParser.seekNextFrame(siis);
                write(bi.getRaster(), out, byteOrder);
            }
            itemParser.seekFooter();
        } finally {
            if (reader != null) reader.dispose();
            if (codecSemaphoreAquired) {
                log.debug("release codec semaphore");
                codecSemaphore.release();
            }
        }
        long t2 = System.currentTimeMillis();
        log.info("finished decompression in " + (t2 - t1) + "ms.");
    }

    private void write(WritableRaster raster, OutputStream out,
            ByteOrder byteOrder) throws IOException {
        DataBuffer buffer = raster.getDataBuffer();
        final int stride = ((ComponentSampleModel) raster.getSampleModel())
                .getScanlineStride();
        final int h = raster.getHeight();
        final int w = raster.getWidth();
        final int b = raster.getNumBands();
        final int wb = w * b;
        switch (buffer.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            for (int i = 0; i < h; ++i)
                out.write(((DataBufferByte) buffer).getData(), i * stride, wb);
            break;
        case DataBuffer.TYPE_USHORT:
            if (byteOrder == ByteOrder.LITTLE_ENDIAN)
                for (int i = 0; i < h; ++i)
                    writeShortLE(((DataBufferUShort) buffer).getData(), i
                            * stride, wb, out);
            else
                for (int i = 0; i < h; ++i)
                    writeShortBE(((DataBufferUShort) buffer).getData(), i
                            * stride, wb, out);
            break;
        case DataBuffer.TYPE_SHORT:
            if (byteOrder == ByteOrder.LITTLE_ENDIAN)
                for (int i = 0; i < h; ++i)
                    writeShortLE(((DataBufferShort) buffer).getData(), i
                            * stride, wb, out);
            else
                for (int i = 0; i < h; ++i)
                    writeShortBE(((DataBufferShort) buffer).getData(), i
                            * stride, wb, out);
            break;
        default:
            throw new RuntimeException(buffer.getClass().getName()
                    + " not supported");
        }
    }

    private void writeShortLE(short[] data, int off, int len, OutputStream out)
            throws IOException {
        for (int i = off, end = off + len; i < end; i++) {
            final short px = data[i];
            out.write(px & 0xff);
            out.write((px >>> 8) & 0xff);
        }
    }

    private void writeShortBE(short[] data, int off, int len, OutputStream out)
            throws IOException {
        for (int i = off, end = off + len; i < end; i++) {
            final short px = data[i];
            out.write((px >>> 8) & 0xff);
            out.write(px & 0xff);
        }
    }

}
