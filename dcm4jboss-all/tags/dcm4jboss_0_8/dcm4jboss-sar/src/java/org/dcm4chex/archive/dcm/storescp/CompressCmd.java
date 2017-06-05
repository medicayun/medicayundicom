/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.storescp;

import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4chex.archive.exceptions.ConfigurationException;
import org.jboss.logging.Logger;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;

import com.sun.media.imageio.plugins.jpeg2000.J2KImageWriteParam;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1134 $ $Date: 2004-06-15 19:16:25 +0800 (周二, 15 6月 2004) $
 * @since 11.06.2004
 *
 */
abstract class CompressCmd {

    private static final String YBR_RCT = "YBR_RCT";

    private static final String JPEG = "jpeg";

    private static final String JPEG2000 = "jpeg2000";

    private static final String JPEG_LOSSLESS = "JPEG-LOSSLESS";

    private static final String JPEG_LS = "JPEG-LS";

    private static final String CLIB_JPEG_IMAGE_WRITER = "com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriter";

    private static final String J2K_IMAGE_WRITER_CODEC_LIB = "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterCodecLib";

    private static final byte[] ITEM_TAG = { (byte) 0xfe, (byte) 0xff,
            (byte) 0x00, (byte) 0xe0};

    private static final int[] GRAY_BAND_OFFSETS = { 0};

    private static final int[] RGB_BAND_OFFSETS = { 0, 1, 2};

    private static ImageWriter getWriter(String formatName, String className) {
        for (Iterator it = ImageIO.getImageWritersByFormatName(formatName); it
                .hasNext();) {
            ImageWriter r = (ImageWriter) it.next();
            if (className == null || className.equals(r.getClass().getName()))
                    return r;
        }

        throw new ConfigurationException("No Image Writer for format:"
                + formatName);
    }

    private static class Jpeg2000 extends CompressCmd {

        public Jpeg2000(StoreScpService service, Dataset ds) {
            super(service, ds, true);
        }

        public void coerceDataset(Dataset ds) {
            if (samples == 3) {
                ds.putUS(Tags.PlanarConfiguration, 0);
                ds.putCS(Tags.PhotometricInterpretation, YBR_RCT);
            }
        }

        protected ImageWriter getWriter() {
            return CompressCmd.getWriter(JPEG2000, J2K_IMAGE_WRITER_CODEC_LIB);
        }

        protected void initWriteParam(ImageWriteParam param) {
            J2KImageWriteParam j2KwParam = (J2KImageWriteParam) param;
            j2KwParam.setWriteCodeStreamOnly(true);
        }
    };

    private static class JpegLossless extends CompressCmd {

        public JpegLossless(StoreScpService service, Dataset ds) {
            super(service, ds, false);
        }

        public void coerceDataset(Dataset ds) {
            if (samples == 3) {
                ds.putUS(Tags.PlanarConfiguration, 0);
            }
        }

        protected ImageWriter getWriter() {
            return CompressCmd.getWriter(JPEG, CLIB_JPEG_IMAGE_WRITER);
        }

        protected void initWriteParam(ImageWriteParam param) {
            param.setCompressionType(JPEG_LOSSLESS);
        }
    };

    private static class JpegLS extends CompressCmd {

        public JpegLS(StoreScpService service, Dataset ds) {
            super(service, ds, false);
        }

        public void coerceDataset(Dataset ds) {
            if (samples == 3) {
                ds.putUS(Tags.PlanarConfiguration, 0);
            }

        }

        protected ImageWriter getWriter() {
            return CompressCmd.getWriter(JPEG, CLIB_JPEG_IMAGE_WRITER);
        }

        protected void initWriteParam(ImageWriteParam param) {
            param.setCompressionType(JPEG_LS);
        }
    };

    public static CompressCmd createCompressCmd(StoreScpService service,
            Dataset ds) {
        String tsuid = ds.getFileMetaInfo().getTransferSyntaxUID();
        if (UIDs.JPEG2000Lossless.equals(tsuid))
                return new Jpeg2000(service, ds);
        if (UIDs.JPEGLSLossless.equals(tsuid)) return new JpegLS(service, ds);
        if (UIDs.JPEGLossless.equals(tsuid)
                || UIDs.JPEGLossless14.equals(tsuid))
                return new JpegLossless(service, ds);
        throw new IllegalArgumentException("tsuid:" + tsuid);
    }

    private final StoreScpService service;

    private final Logger log;

    private final boolean debug;

    private final DcmEncodeParam encParam;

    protected final int samples;

    protected final int frames;

    protected final int rows;

    protected final int columns;

    protected final int planarConfiguration;

    protected final int bitsAllocated;

    protected final int bitsStored;

    protected final int pixelRepresentation;

    protected final int dataType;

    protected final int frameLength;

    protected CompressCmd(StoreScpService service, Dataset ds,
            boolean supportSigned) {
        this.service = service;
        this.log = service.getLog();
        this.debug = log.isDebugEnabled();
        this.encParam = DcmEncodeParam.valueOf(ds.getFileMetaInfo()
                .getTransferSyntaxUID());
        this.samples = ds.getInt(Tags.SamplesPerPixel, 1);
        this.frames = ds.getInt(Tags.NumberOfFrames, 1);
        this.rows = ds.getInt(Tags.Rows, 1);
        this.columns = ds.getInt(Tags.Columns, 1);
        this.pixelRepresentation = ds.getInt(Tags.PixelRepresentation, 0);
        this.planarConfiguration = ds.getInt(Tags.PlanarConfiguration, 0);

        switch (bitsAllocated = ds.getInt(Tags.BitsAllocated, 8)) {
        case 8:
            this.dataType = DataBuffer.TYPE_BYTE;
            this.frameLength = rows * columns * samples;
            break;
        case 16:
            this.dataType = pixelRepresentation == 0 || !supportSigned ? DataBuffer.TYPE_USHORT
                    : DataBuffer.TYPE_SHORT;
            this.frameLength = rows * columns * samples * 2;
            break;
        default:
            throw new IllegalArgumentException("bits allocated:"
                    + bitsAllocated);
        }
        this.bitsStored = ds.getInt(Tags.BitsStored, bitsAllocated);
    }

    public abstract void coerceDataset(Dataset ds);

    protected abstract ImageWriter getWriter();

    protected abstract void initWriteParam(ImageWriteParam param);

    public int compress(ByteOrder byteOrder, InputStream in, OutputStream out)
            throws Exception {
        long t1;
        ImageWriter w = null;
        Semaphore codecSemaphore = service.getCodecSemaphore();
        boolean codecSemaphoreAquired = false;
        long end = 0;
        try {
            log.debug("acquire codec semaphore");
            codecSemaphore.acquire();
            codecSemaphoreAquired = true;
            log.info("start compression of image: " + rows + "x" + columns
                    + "x" + frames);
            t1 = System.currentTimeMillis();
            ImageOutputStream ios = new MemoryCacheImageOutputStream(out);
            ios.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            w = getWriter();
            ImageWriteParam wParam = w.getDefaultWriteParam();
            initWriteParam(wParam);
            WritableRaster raster = Raster.createWritableRaster(
                    getSampleModel(), null);
            DataBuffer db = raster.getDataBuffer();
            BufferedImage bi = new BufferedImage(getColorModel(), raster,
                    false, null);
            ios.write(ITEM_TAG);
            ios.writeInt(0);
            for (int i = 0; i < frames; ++i) {
                if (debug) log.debug("start compression of frame #" + (i + 1));
                ios.write(ITEM_TAG);
                long mark = ios.getStreamPosition();
                ios.writeInt(0);
                switch (dataType) {
                case DataBuffer.TYPE_BYTE:
                    read(in, ((DataBufferByte) db).getBankData());
                    break;
                case DataBuffer.TYPE_SHORT:
                    read(byteOrder, in, ((DataBufferShort) db).getBankData());
                    break;
                case DataBuffer.TYPE_USHORT:
                    read(byteOrder, in, ((DataBufferUShort) db).getBankData());
                    break;
                default:
                    throw new RuntimeException("dataType:" + db.getDataType());
                }
                w.setOutput(ios);
                w.write(null, new IIOImage(bi, null, null), wParam);
                end = ios.getStreamPosition();
                if ((end & 1) != 0) {
                    ios.write(0);
                    ++end;
                }
                ios.seek(mark);
                ios.writeInt((int) (end - mark - 4));
                ios.seek(end);
                ios.flush();
            }
        } finally {
            if (w != null) w.dispose();
            if (codecSemaphoreAquired) {
                log.debug("release codec semaphore");
                codecSemaphore.release();
            }
        }
        long t2 = System.currentTimeMillis();
        int pixelDataLength = frameLength * frames;
        log.info("finished compression " + ((float) pixelDataLength / end)
                + " : 1 in " + (t2 - t1) + "ms.");
        return pixelDataLength;
    }

    private void read(ByteOrder byteOrder, InputStream in, short[][] data)
            throws IOException {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            readLE(in, data);
        else
            readBE(in, data);
    }

    private void readLE(InputStream in, short[][] data) throws IOException {
        int lo, hi;
        for (int i = 0; i < data.length; i++) {
            short[] bank = data[i];
            for (int j = 0; j < bank.length; j++) {
                lo = in.read();
                hi = in.read();
                if ((lo | hi) < 0) throw new EOFException();
                bank[j] = (short) ((lo & 0xff) + (hi << 8));
            }
        }
    }

    private void readBE(InputStream in, short[][] data) throws IOException {
        int lo, hi;
        for (int i = 0; i < data.length; i++) {
            short[] bank = data[i];
            for (int j = 0; j < bank.length; j++) {
                hi = in.read();
                lo = in.read();
                if ((lo | hi) < 0) throw new EOFException();
                bank[j] = (short) ((lo & 0xff) + (hi << 8));
            }
        }
    }

    private void read(InputStream in, byte[][] data) throws IOException {
        for (int i = 0; i < data.length; i++) {
            byte[] bank = data[i];
            for (int toread = bank.length; toread > 0;)
                toread -= in.read(bank, bank.length - toread, toread);
        }
    }

    private SampleModel getSampleModel() {
        if (planarConfiguration == 0) {
            return new PixelInterleavedSampleModel(dataType, columns, rows,
                    samples, columns * samples,
                    samples == 1 ? GRAY_BAND_OFFSETS : RGB_BAND_OFFSETS);
        } else {
            return new BandedSampleModel(dataType, columns, rows, samples);
        }
    }

    private ColorModel getColorModel() {
        if (samples == 3) {
            return new ComponentColorModel(ColorSpace
                    .getInstance(ColorSpace.CS_sRGB), new int[] { bitsStored,
                    bitsStored, bitsStored}, false, false, ColorModel.OPAQUE,
                    dataType);
        } else {
            return new ComponentColorModel(ColorSpace
                    .getInstance(ColorSpace.CS_GRAY), new int[] { bitsStored},
                    false, false, ColorModel.OPAQUE, dataType);
        }
    }
}
