/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.codec;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.exceptions.ConfigurationException;

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;
import EDU.oswego.cs.dl.util.concurrent.Semaphore;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1607 $ $Date: 2005-03-16 00:40:06 +0800 (周三, 16 3月 2005) $
 * @since 14.03.2005
 *
 */

public abstract class CodecCmd {
	
	static final Logger log = Logger.getLogger(CodecCmd.class);
	
    static final String YBR_RCT = "YBR_RCT";

    static final String JPEG2000 = "jpeg2000";

    static final String JPEG = "jpeg";

    static final String JPEG_LOSSLESS = "JPEG-LOSSLESS";

    static final String JPEG_LS = "JPEG-LS";

    static final String JPEG_IMAGE_READER = "com.sun.imageio.plugins.jpeg.JPEGImageReader";

    static final String CLIB_JPEG_IMAGE_READER = "com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader";

    static final String J2K_IMAGE_READER = "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReader";

    static final String J2K_IMAGE_READER_CODEC_LIB = "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLib";

    static final String CLIB_JPEG_IMAGE_WRITER = "com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriter";

    static final String J2K_IMAGE_WRITER_CODEC_LIB = "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterCodecLib";

    static int maxConcurrentCodec = 1;
    
    static Semaphore codecSemaphore = new FIFOSemaphore(maxConcurrentCodec);

    public static void setMaxConcurrentCodec(int maxConcurrentCodec) {
        codecSemaphore = new FIFOSemaphore(maxConcurrentCodec);
        CodecCmd.maxConcurrentCodec = maxConcurrentCodec;
    }

    public static int getMaxConcurrentCodec() {
        return maxConcurrentCodec;
    }
    
	protected boolean useNative = true;

	protected final int samples;

	protected final int frames;

	protected final int rows;

	protected final int columns;

    protected final int planarConfiguration;

    protected final int bitsAllocated;

    protected final int bitsStored;

    protected final int pixelRepresentation;
    
    protected final int frameLength;    

    protected final int pixelDataLength;    
    
	protected CodecCmd(Dataset ds) {
        this.samples = ds.getInt(Tags.SamplesPerPixel, 1);
        this.frames = ds.getInt(Tags.NumberOfFrames, 1);
        this.rows = ds.getInt(Tags.Rows, 1);
        this.columns = ds.getInt(Tags.Columns, 1);		
        this.bitsAllocated = ds.getInt(Tags.BitsAllocated, 8);
        this.bitsStored = ds.getInt(Tags.BitsStored, bitsAllocated);
        this.pixelRepresentation = ds.getInt(Tags.PixelRepresentation, 0);
        this.planarConfiguration = ds.getInt(Tags.PlanarConfiguration, 0);
        this.frameLength = rows * columns * samples * bitsAllocated / 8;
        this.pixelDataLength = frameLength * frames;
	}

    static ImageReader getImageReader(String formatName, String className) {
        for (Iterator it = ImageIO.getImageReadersByFormatName(formatName); it
                .hasNext();) {
            ImageReader r = (ImageReader) it.next();
            if (className == null || className.equals(r.getClass().getName()))
                    return r;
        }

        throw new ConfigurationException("No Image Reader for format:"
                + formatName);
    }

    static ImageWriter getImageWriter(String formatName, String className) {
        for (Iterator it = ImageIO.getImageWritersByFormatName(formatName); it
                .hasNext();) {
            ImageWriter r = (ImageWriter) it.next();
            if (className == null || className.equals(r.getClass().getName()))
                    return r;
        }

        throw new ConfigurationException("No Image Writer for format:"
                + formatName);
    }
    
    public final int getPixelDataLength() {
    	return pixelDataLength;
    }
}
