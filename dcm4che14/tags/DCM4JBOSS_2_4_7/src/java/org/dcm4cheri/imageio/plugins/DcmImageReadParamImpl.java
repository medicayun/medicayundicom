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

package org.dcm4cheri.imageio.plugins;

import org.dcm4che.imageio.plugins.DcmImageReadParam;

/**
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3860 $
 * @since November 21, 2002
 *
 */
public class DcmImageReadParamImpl extends DcmImageReadParam {

    private byte[] pvalToDDL = null;

    private boolean maskPixelData = true;
    private boolean autoWindowing = true;

    public byte[] getPValToDDL() {
        return pvalToDDL;
    }

    public void setPValToDDL(byte[] pvalToDDL) {
        if (pvalToDDL != null) {
            checkLen(pvalToDDL.length);
        }
        this.pvalToDDL = pvalToDDL;
    }

    public final boolean isMaskPixelData() {
        return maskPixelData;
    }

    public final void setMaskPixelData(boolean mask) {
        this.maskPixelData = mask;
    }

	public final boolean isAutoWindowing() {
		return autoWindowing;
	}

	public final void setAutoWindowing(boolean autoWindowing) {
		this.autoWindowing = autoWindowing;
	}
    
    private final static void checkLen(int len) {
        for (int n = 0x100; n <= 0x10000; n <<= 1) {
            if (n == len)
                return;
        }
        throw new IllegalArgumentException("pvalToDDL length: " + len);
    }
}
