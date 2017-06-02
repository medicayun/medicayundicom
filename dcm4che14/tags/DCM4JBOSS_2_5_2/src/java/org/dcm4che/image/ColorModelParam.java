/*$Id: ColorModelParam.java 3860 2005-02-08 17:35:08Z gunterze $*/
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

import java.awt.image.ColorModel;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public interface ColorModelParam {
    
    public ColorModel newColorModel();
    
    public ColorModelParam update(float center, float width, boolean inverse);
    
    public float getRescaleSlope();

    public float getRescaleIntercept();

    public float getWindowCenter(int index);

    public float getWindowWidth(int index);

    public int getNumberOfWindows();
    
    public float toMeasureValue(int pxValue);
    
    public int toPixelValue(float measureValue);
    
    public int toSampleValue(int pxValue);
    public int toPixelValueRaw(int sampleValue);

    public boolean isInverse();
    
    public boolean isCacheable();

	public boolean isMonochrome();
}

