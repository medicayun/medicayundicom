/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.qrscp;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmParser;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4cheri.image.ItemParser;
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.jboss.logging.Logger;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;

import com.sun.media.imageio.stream.SegmentedImageInputStream;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1365 $ $Date: 2005-01-03 06:49:12 +0800 (周一, 03 1月 2005) $
 * @since 22.05.2004
 *
 */
class DecompressCmd {

    private static final String JPEG2000 = "jpeg2000";

    private static final String JPEG = "jpeg";

    private static final String JPEG_IMAGE_READER = "com.sun.imageio.plugins.jpeg.JPEGImageReader";

    private static final String CLIB_JPEG_IMAGE_READER = "com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader";

    private static final String J2K_IMAGE_READER = "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReader";

    private static final String J2K_IMAGE_READER_CODEC_LIB = "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLib";

    private final QueryRetrieveScpService service;

    private final Logger log;

    private final boolean debug;

    private final int samples;

    private final int frames;

    private final int rows;

    private final int columns;

    private final int bitsalloc;

    private final String tsuid;

    private final DcmParser parser;

    private final ItemParser itemParser;
    
    private final ImageInputStream iis;

    private boolean useNative = true;

    public DecompressCmd(QueryRetrieveScpService service, Dataset ds,
            DcmParser parser) throws IOException {
        this.service = service;
        this.log = service.getLog();
        this.debug = log.isDebugEnabled();
        this.parser = parser;
        this.iis = parser.getImageInputStream();
        this.itemParser = new ItemParser(parser);
        tsuid = ds.getFileMetaInfo().getTransferSyntaxUID();
        samples = ds.getInt(Tags.SamplesPerPixel, 1);
        frames = ds.getInt(Tags.NumberOfFrames, 1);
        rows = ds.getInt(Tags.Rows, 1);
        columns = ds.getInt(Tags.Columns, 1);
        bitsalloc = ds.getInt(Tags.BitsAllocated, 8);
        if (samples == 3) ds.putCS(Tags.PhotometricInterpretation, "RGB");
    }

    public int getPixelDataLength() {
        return rows * columns * samples * bitsalloc * frames / 8;
    }

    public void decompress(ByteOrder byteOrder, OutputStream out)
            throws Exception {
        long t1;
        ImageReader reader = null;
        BufferedImage bi = null;
        Semaphore codecSemaphore = service.getCodecSemaphore();
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
                if (debug)
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

    private ImageReader getReaderForTransferSyntax(String ts) {
        if (ts.equals(UIDs.JPEG2000Lossless) || ts.equals(UIDs.JPEG2000Lossy))
                return getReader(JPEG2000,
                        useNative ? J2K_IMAGE_READER_CODEC_LIB
                                : J2K_IMAGE_READER);
        if (ts.equals(UIDs.JPEGBaseline))
                return getReader(JPEG, useNative ? CLIB_JPEG_IMAGE_READER
                        : JPEG_IMAGE_READER);
        // only supported by native CLibJPEGImageReader
        if (ts.equals(UIDs.JPEGExtended) || ts.equals(UIDs.JPEGLossless)
                || ts.equals(UIDs.JPEGLossless14)
                || ts.equals(UIDs.JPEGLSLossless)
                || ts.equals(UIDs.JPEGLSLossy))
                return getReader(JPEG, CLIB_JPEG_IMAGE_READER);
        throw new UnsupportedOperationException(
                "No Image Reader available for Transfer Syntax:" + ts);
    }

    private static ImageReader getReader(String formatName, String className) {
        for (Iterator it = ImageIO.getImageReadersByFormatName(formatName); it
                .hasNext();) {
            ImageReader r = (ImageReader) it.next();
            if (className == null || className.equals(r.getClass().getName()))
                    return r;
        }

        throw new ConfigurationException("No Image Reader for format:"
                + formatName);
    }
}
