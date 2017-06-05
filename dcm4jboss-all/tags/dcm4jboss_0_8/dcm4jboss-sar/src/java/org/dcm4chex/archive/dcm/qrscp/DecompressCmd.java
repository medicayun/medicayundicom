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
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmParser;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.jboss.logging.Logger;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;

import com.sun.media.imageio.stream.SegmentedImageInputStream;
import com.sun.media.imageio.stream.StreamSegment;
import com.sun.media.imageio.stream.StreamSegmentMapper;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1131 $ $Date: 2004-06-14 06:26:17 +0800 (周一, 14 6月 2004) $
 * @since 22.05.2004
 *
 */
class DecompressCmd implements StreamSegmentMapper {

    private static final String JPEG2000 = "jpeg2000";

    private static final String JPEG = "jpeg";

    private static final String JPEG_IMAGE_READER = "com.sun.imageio.plugins.jpeg.JPEGImageReader";

    private static final String CLIB_JPEG_IMAGE_READER = "com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader";

    private static final String J2K_IMAGE_READER = "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReader";

    private static final String J2K_IMAGE_READER_CODEC_LIB = "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLib";

    private static final class Item {

        int frameOffset;

        long streamPos;

        int length;

        final int nextFrameOffset() {
            return frameOffset + length;
        }

        final long nextStreamPos() {
            return streamPos + length;
        }
    }

    private final ArrayList items = new ArrayList();

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

    private final ImageInputStream iis;

    private final byte[] frameOffsets;

    private boolean useNative = false;

    public DecompressCmd(QueryRetrieveScpService service, Dataset ds,
            DcmParser parser) throws IOException {
        this.service = service;
        this.log = service.getLog();
        this.debug = log.isDebugEnabled();
        this.parser = parser;
        tsuid = ds.getFileMetaInfo().getTransferSyntaxUID();
        samples = ds.getInt(Tags.SamplesPerPixel, 1);
        frames = ds.getInt(Tags.NumberOfFrames, 1);
        rows = ds.getInt(Tags.Rows, 1);
        columns = ds.getInt(Tags.Columns, 1);
        bitsalloc = ds.getInt(Tags.BitsAllocated, 8);
        iis = parser.getImageInputStream();
        parser.parseHeader();
        frameOffsets = new byte[parser.getReadLength()];
        iis.read(frameOffsets);
        if (samples == 3) ds.putCS(Tags.PhotometricInterpretation, "RGB");
    }

    private boolean nextItem() {
        Item item = new Item();
        try {
            parser.parseHeader();
            item.streamPos = iis.getStreamPosition();
        } catch (IOException e) {
            return false;
        }
        if (parser.getReadTag() != Tags.Item) { return false; }

        if (!items.isEmpty()) item.frameOffset = lastItem().nextFrameOffset();
        item.length = parser.getReadLength();
        items.add(item);
        if (debug)
                log.debug("read " + Tags.toString(parser.getReadTag())
                        + ", pos=" + item.streamPos + ", len=" + item.length);
        return true;
    }

    private Item lastItem() {
        return (Item) items.get(items.size() - 1);
    }

    public int getPixelDataLength() {
        return rows * columns * samples * bitsalloc * frames / 8;
    }

    public void decompress(ByteOrder byteOrder, OutputStream out) throws Exception {
        long t1;
        ImageReader reader = null;
        BufferedImage bi = null;
        Semaphore codecSemaphore = service.getCodecSemaphore();
        boolean codecSemaphoreAquired = false;
        try {
            log.debug("acquire codec semaphore");
            codecSemaphore.acquire();
            codecSemaphoreAquired = true;
            log.info("start decompression of image: " + rows + "x" + columns + "x"
                    + frames);            
            t1 = System.currentTimeMillis();
            reader = getReaderForTransferSyntax(tsuid);
            for (int i = 0; i < frames; ++i) {
                if (debug)
                        log.debug("start decompression of frame #" + (i + 1));
                items.clear();
                nextItem();
                reader.setInput(new SegmentedImageInputStream(iis, this));
                ImageReadParam param = reader.getDefaultReadParam();
                if (bi != null) param.setDestination(bi);
                bi = reader.read(0, param);
                write(bi.getRaster().getDataBuffer(), out, byteOrder);
                iis.seek((lastItem()).nextStreamPos());
            }
        } finally {
            if (reader != null)
                reader.dispose();
            if (codecSemaphoreAquired) {
                log.debug("release codec semaphore");
                codecSemaphore.release();
            }
        }
        // skip end of sequence tag;
        parser.parseHeader();
        long t2 = System.currentTimeMillis();
        log.info("finished decompression in " + (t2 - t1) + "ms.");
    }

    private void write(DataBuffer buffer, OutputStream out, ByteOrder byteOrder) throws IOException {
        switch (buffer.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            out.write(((DataBufferByte) buffer).getData());
            break;
        case DataBuffer.TYPE_USHORT:
            if (byteOrder == ByteOrder.LITTLE_ENDIAN)
                writeShortLE(((DataBufferUShort) buffer).getData(), out);
            else
                writeShortBE(((DataBufferUShort) buffer).getData(), out);
            break;
        case DataBuffer.TYPE_SHORT:
            if (byteOrder == ByteOrder.LITTLE_ENDIAN)
                writeShortLE(((DataBufferShort) buffer).getData(), out);
            else
                writeShortBE(((DataBufferShort) buffer).getData(), out);
            break;
        default:
            throw new RuntimeException(buffer.getClass().getName()
                    + " not supported");
        }
    }

    private void writeShortLE(short[] data, OutputStream out)
            throws IOException {
        for (int i = 0; i < data.length; i++) {
            final short px = data[i];
            out.write(px & 0xff);
            out.write((px >>> 8) & 0xff);
        }
    }

    private void writeShortBE(short[] data, OutputStream out)
    throws IOException {
		for (int i = 0; i < data.length; i++) {
		    final short px = data[i];
		    out.write((px >>> 8) & 0xff);
		    out.write(px & 0xff);
		}
	}

    private void writeBytes(DataBufferByte buffer, OutputStream out)
            throws IOException {
        out.write(buffer.getData());
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

    public StreamSegment getStreamSegment(long pos, int len) {
        StreamSegment retval = new StreamSegment();
        getStreamSegment(pos, len, retval);
        return retval;
    }

    public void getStreamSegment(long pos, int len, StreamSegment seg) {
        if (debug)
                log.debug("getStreamSegment(pos=" + pos + ", len=" + len + ")");
        Item item;
        while ((item = lastItem()).nextFrameOffset() <= pos) {
            if (!nextItem()) {
                seg.setStartPos(item.nextStreamPos());
                seg.setSegmentLength(-1);
                if (debug)
                        log.debug("getStreamSegment->StreamSegment[pos="
                                + seg.getStartPos() + ", len="
                                + seg.getSegmentLength());
                return;
            }
        }
        for (int i = items.size() - 2; item.frameOffset > pos; --i)
            item = (Item) items.get(i);
        seg.setStartPos(item.streamPos + pos - item.frameOffset);
        seg.setSegmentLength(Math.min(
                (int) (item.frameOffset + item.length - pos), len));
        if (debug)
                log
                        .debug("getStreamSegment ->StreamSegment[pos="
                                + seg.getStartPos() + ", len="
                                + seg.getSegmentLength());
    }
}
