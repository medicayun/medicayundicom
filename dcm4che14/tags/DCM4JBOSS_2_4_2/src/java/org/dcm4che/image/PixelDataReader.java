/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.image;

import java.io.IOException;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since July 2003
 * @version $Revision: 3695 $ $Date: 2003-07-23 02:50:35 +0800 (周三, 23 7月 2003) $
 * @see "DICOM Part 5: Data Structures and Encoding, Section 8. 'Encoding of Pixel,
 *      Overlay and Waveform Data', Annex D"
 */
public interface PixelDataReader
{
    /**
     * Retrieves the <code>PixelDataDescription</code>.
     * @return PixelDataDescription
     */
    public PixelDataDescription getPixelDataDescription();

    /**
     * Marks the read position of the backing <code>ImageInputStream</code>
     */
    public void mark();

    /**
     * Resets the read position of the backing <code>ImageInputStream</code> to
     * the matching mark (where mark() was last called on the stream). If no
     * mark was set using @see org.dcm4che.image.PixelData#mark(), nothing
     * happens.
     * @throws IOException If the matching mark is in a portion of the backing
     *      <code>ImageInputStream</code> that has already been discarded.
     */
    public void reset()
        throws IOException;

    /**
     * Reset the read position of the backing <code>ImageInputStream</code> and
     * state of this instance to the initial read position
     * @throws IOException On I/O error -- a seek to beginning of stream was not
     *      was not possible
     */
    public void resetStream()
        throws IOException;

    /**
     * If a sample can't be read, the state of the contained <code>ImageInputStream</code> is
     * not disturbed (beyond any effect of trying to read and getting an IOException)
     * @return The next sample value from the underlying <code>ImageInputStream</code>
     * @throws IOException If underlying <code>ImageInputStream</code> has an I/O problem
     */
    public int readSample()
        throws IOException;

    /**
     * Skip the specified number of samples from the current position in the stream.
     * @param n Number of samples to skip. The behaviour of this method is undefined
     *          for negative values of <code>n</code> or if an IOException occurs.
     * @throws IOException On I/O error
     */
    public void skipSamples(int n)
        throws IOException;
    /**
     * Skip the specified number of samples from the current position in the stream.
     * @param n Number of samples to skip. The behavior of this class is undefined
     *          for negative values of <code>n</code> or if an IOException occurs.
     * @throws IOException On I/O error
     */
    public void skipSamples(long n)
        throws IOException;

    public void skipToNextFrame()
        throws IOException;

    public void readFully(int[] samples, int offset, int len)
        throws IOException;

    public int[] readFully(int len)
        throws IOException;

    public int[] getPixel(int i, int j, int k)
        throws IOException;

    public int getSample(int i, int j, int k, int band)
        throws IOException;

    public int[][][] getPixelDataArray();

    public int[][] getPixelDataArray(int frame);

    public void readPixelData()
        throws IOException;

    public void readPixelData(boolean grabOverlayData)
        throws IOException;
}
