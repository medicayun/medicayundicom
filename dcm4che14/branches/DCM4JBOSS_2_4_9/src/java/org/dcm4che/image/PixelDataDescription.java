package org.dcm4che.image;

import java.nio.ByteOrder;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;

public class PixelDataDescription
{
    protected final int cols;
    protected final int rows;
    protected final int nf;
    protected final int frameSize;
    protected final int size;
    protected final int samplesPerFrame;
    protected final int ba;
    protected final int bs;
    protected final int hb;
    protected final int spp;
    protected final boolean signed;
    protected final boolean byPlane;
    protected final String pmi;
    protected final ByteOrder byteOrder;
    protected final int pixelDataVr;

    private PixelDataDescription(int cols, int rows, int nf, int ba, int bs, int hb,
        int spp, boolean signed, boolean byPlane, String pmi, ByteOrder byteOrder,
        int pixelDataVr)
    {
        this.cols = cols;
        this.rows = rows;
        this.nf = nf;
        this.frameSize = cols * rows;
        this.samplesPerFrame = frameSize * spp;
        this.size = frameSize * nf;
        this.ba = ba;
        this.bs = bs;
        this.hb = hb;
        this.spp = spp;
        this.signed = signed;
        this.byPlane = byPlane;
        this.pmi = pmi;
        this.byteOrder = byteOrder;
        this.pixelDataVr = pixelDataVr;
    }

    public PixelDataDescription(PixelDataDescription desc, DcmDecodeParam dcmParam,
        int bitsAllocated, int bitsStored, boolean signed, boolean byPlane)
    {
        this.cols = desc.getCols();
        this.rows = desc.getRows();
        this.nf = desc.getNumberOfFrames();
        this.ba = bitsAllocated;
        this.bs = bitsStored;
        this.hb = bs - 1;
        this.spp = desc.getSamplesPerPixel();
        this.frameSize = cols * rows;
        this.samplesPerFrame = frameSize * spp;
        this.size = frameSize * nf;
        this.signed = signed;
        this.byPlane = byPlane;
        this.pmi = desc.getPmi();
        //set up proper values depending on target bit depth
        this.byteOrder = dcmParam.byteOrder;
        if (dcmParam.explicitVR) {
            if (bitsAllocated > 8)
                this.pixelDataVr = VRs.OW;
            else
                this.pixelDataVr = VRs.OB; //can also be OW!
        }
        else
            this.pixelDataVr = VRs.OW;
    }

    public PixelDataDescription(Dataset ds, ByteOrder byteOrder, int pixelDataVr)
    {
        if ((pmi = ds.getString(Tags.PhotometricInterpretation, "MONOCHROME2")) == null)
            throw new IllegalArgumentException("No photometric interpretation");
        if ((ba = ds.getInt(Tags.BitsAllocated, -1)) == -1)
            throw new IllegalArgumentException("No Bits Allocated");
        if ((bs = ds.getInt(Tags.BitsStored, -1)) == -1)
            throw new IllegalArgumentException("No Bits Stored");
        if ((hb = ds.getInt(Tags.HighBit, -1)) == -1)
            throw new IllegalArgumentException("No High Bit");
        if (bs <= hb)
            throw new IllegalArgumentException("Bits Stored <= High Bit");
        int tmp;
        if ((tmp = ds.getInt(Tags.PixelRepresentation, -1)) == -1)
            throw new IllegalArgumentException("No Pixel Representation");
        signed = tmp == 1;
        if ((spp = ds.getInt(Tags.SamplesPerPixel, 1)) == -1)
            throw new IllegalArgumentException("No Samples Per Pixel");
        tmp = ds.getInt(Tags.PlanarConfiguration, 0);
        byPlane = tmp == 1;
        if ((cols = ds.getInt(Tags.Columns, -1)) == -1)
            throw new IllegalArgumentException("No Columns");
        if ((rows = ds.getInt(Tags.Rows, -1)) == -1)
            throw new IllegalArgumentException("No Rows");
        nf = ds.getInt(Tags.NumberOfFrames, 1);
        frameSize = cols * rows;
        samplesPerFrame = frameSize * spp;
        size = frameSize * nf;
        if (ba > 32)
            throw new UnsupportedOperationException("Bits Allocated > 32 are not supported: " + ba);
        this.byteOrder = byteOrder;
        this.pixelDataVr = pixelDataVr;
    }

    public long calcPixelDataLength()
    {
        long pixelDataLen = (long)cols * rows * nf * spp * ba;
        if (pixelDataLen % 8 != 0)
            pixelDataLen = (pixelDataLen >>> 3) + 1;
        else
            pixelDataLen = pixelDataLen >>> 3;
        return ((pixelDataLen & 0x1) == 0) ? pixelDataLen : pixelDataLen + 1;
    }

    public int maxPossibleStoredValue()
    {
        return (signed) ? (1 << (bs - 1)) - 1 : (1 << bs) - 1;
    }

    public int minPossibleStoredValue()
    {
        return (signed) ? -maxPossibleStoredValue() - 1 : 0;
    }

    public int getBitsAllocated() {
        return ba;
    }

    public int getBitsStored() {
        return bs;
    }

    public boolean isByPlane() {
        return byPlane;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public int getCols() {
        return cols;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public int getHighBit() {
        return hb;
    }

    public int getNumberOfFrames() {
        return nf;
    }

    public int getPixelDataVr() {
        return pixelDataVr;
    }

    public String getPmi() {
        return pmi;
    }

    public int getRows() {
        return rows;
    }

    public int getSamplesPerFrame() {
        return samplesPerFrame;
    }

    public boolean isSigned() {
        return signed;
    }

    public int getSize() {
        return size;
    }

    public int getSamplesPerPixel() {
        return spp;
    }

    protected Object clone() throws CloneNotSupportedException {
        return new PixelDataDescription(cols, rows, nf, ba, bs, hb, spp, signed,
            byPlane, pmi, byteOrder, pixelDataVr);
    }
}
