package org.dcm4che.image;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.stream.ImageOutputStream;

public interface PixelDataWriter
{
    /**
     * Retrieves the <code>PixelDataDescription</code>.
     * @return PixelDataDescription
     */
    public PixelDataDescription getPixelDataDescription();

    /**
     * Writes the Pixel Data to a <code>ByteBuffer</code>. Please note that the
     *  implementation should not try to evaluate the actual sample values, to
     * see if they are in the proper range. They are assumed to be correct.
     * @param writeOverlayData Whether to write overlay data in the initialized
     *  sample array. This is ignored if this class was initialized specifying
     *  that there is no overlay data in the sample array.
     * @return A <code>ByteBuffer</code> with the written Pixel Data stream.
     */
    public ByteBuffer writePixelDataToByteBuffer();

    /**
     * Writes the Pixel Data to an <code>ImageOutputStream</code>. Please note
     *  that the implementation should not try to evaluate the actual sample
     *  values, to see if they are in the proper range. They are assumed to be
     *  correct.
     * @throws IOException On I/O error
     */
    public void writePixelData()
        throws IOException;

    /**
     * Writes the Pixel Data to an <code>ImageOutputStream</code>. Please note
     *  that the implementation should not try to evaluate the actual sample
     *  values, to see if they are in the proper range. They are assumed to be
     *  correct.
     * @param out An alternative <code>ImageOoutputStream</code> to write to.
     * @throws IOException On I/O error
     */
    public void writePixelData(ImageOutputStream out)
        throws IOException;
}
