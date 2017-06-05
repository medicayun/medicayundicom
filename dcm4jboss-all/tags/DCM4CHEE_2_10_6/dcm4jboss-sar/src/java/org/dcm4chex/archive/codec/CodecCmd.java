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

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;
import EDU.oswego.cs.dl.util.concurrent.Semaphore;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2474 $ $Date: 2006-05-15 17:59:33 +0800 (周一, 15 5月 2006) $
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

    static int maxConcurrentCodec = 1;
    
    static Semaphore codecSemaphore = new FIFOSemaphore(maxConcurrentCodec);

    public static void setMaxConcurrentCodec(int maxConcurrentCodec) {
        codecSemaphore = new FIFOSemaphore(maxConcurrentCodec);
        CodecCmd.maxConcurrentCodec = maxConcurrentCodec;
    }

    public static int getMaxConcurrentCodec() {
        return maxConcurrentCodec;
    }
    
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

    public final int getPixelDataLength() {
    	return pixelDataLength;
    }

    protected BufferedImage createBufferedImage() {
        int pixelStride;
        int[] bandOffset;
        int dataType;
        int colorSpace;
        if (samples == 3) {
            pixelStride = 3;
            bandOffset = new int[] { 0, 1, 2 };
            dataType = DataBuffer.TYPE_BYTE;
            colorSpace = ColorSpace.CS_sRGB;
        } else {
            pixelStride = 1;
            bandOffset = new int[] { 0 };
            dataType = bitsAllocated == 8 ? DataBuffer.TYPE_BYTE 
                    : DataBuffer.TYPE_USHORT;
            colorSpace = ColorSpace.CS_GRAY;
        }
        SampleModel sm = new PixelInterleavedSampleModel(dataType, columns,
                rows, pixelStride, columns * pixelStride, bandOffset);
        ColorModel cm = new ComponentColorModel(
                ColorSpace.getInstance(colorSpace), sm.getSampleSize(),
                false, false, Transparency.OPAQUE, dataType);
        WritableRaster r = Raster.createWritableRaster(sm, new Point(0, 0));
        return new BufferedImage(cm, r, false, new Hashtable());
    }
}
