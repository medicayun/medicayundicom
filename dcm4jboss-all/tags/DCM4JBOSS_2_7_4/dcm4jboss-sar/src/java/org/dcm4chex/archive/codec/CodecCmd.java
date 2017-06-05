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
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
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
    
    protected final int bitsUsed;
    
	protected CodecCmd(Dataset ds) {
        this.samples = ds.getInt(Tags.SamplesPerPixel, 1);
        this.frames = ds.getInt(Tags.NumberOfFrames, 1);
        this.rows = ds.getInt(Tags.Rows, 1);
        this.columns = ds.getInt(Tags.Columns, 1);		
        this.bitsAllocated = ds.getInt(Tags.BitsAllocated, 8);
        this.bitsStored = ds.getInt(Tags.BitsStored, bitsAllocated);
        this.bitsUsed = isOverlayInPixelData(ds) ? bitsAllocated : bitsStored;
        this.pixelRepresentation = ds.getInt(Tags.PixelRepresentation, 0);
        this.planarConfiguration = ds.getInt(Tags.PlanarConfiguration, 0);
        this.frameLength = rows * columns * samples * bitsAllocated / 8;
        this.pixelDataLength = frameLength * frames;
	}

    private boolean isOverlayInPixelData(Dataset ds) {
        for (int i = 0; i < 16; ++i) {
            if (ds.getInt(Tags.OverlayBitPosition + 2*i, 0) != 0) {
                return true;
            }
        }
        return false;
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
