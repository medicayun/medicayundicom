/*$Id: ImageContentImpl.java 3536 2002-08-05 14:16:48Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
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

package org.dcm4cheri.srom;

import org.dcm4che.srom.*;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class ImageContentImpl extends CompositeContentImpl implements ImageContent {
    // Constants -----------------------------------------------------
    private static final int[] NULL_FRAMENUMBER = {};
    
    // Attributes ----------------------------------------------------
    protected int[] frameNumbers;
    protected RefSOP refPresentationSOP;
    protected IconImage iconImage;

    // Constructors --------------------------------------------------
    ImageContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, RefSOP refSOP, int[] frameNumbers,
            RefSOP refPresentationSOP, IconImage iconImage) {
        super(owner, obsDateTime, template, name, refSOP);
        setFrameNumbers(frameNumbers);
        this.refPresentationSOP = refPresentationSOP;
        this.iconImage = iconImage;
    }
    
    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
        return new ImageContentImpl(newOwner,
                getObservationDateTime(inheritObsDateTime), template,
                name, refSOP, frameNumbers, refPresentationSOP, iconImage);
    }

    // Methodes --------------------------------------------------------
    public String toString() {
        StringBuffer sb = prompt().append(refSOP);
        for (int i = 0; i < frameNumbers.length; ++i)
            sb.append(",[").append(frameNumbers[0]).append("]");

        if (refPresentationSOP != null)
            sb.append(",").append(refPresentationSOP);

        return sb.append(')').toString();
    }

    public final ValueType getValueType() {
        return ValueType.IMAGE;
    }    
    
    public final int[] getFrameNumbers() {
        return (int[])frameNumbers.clone();
    }
    
    public final void setFrameNumbers(int[] frameNumbers) {
        this.frameNumbers = frameNumbers != null
                ? (int[])frameNumbers.clone() : NULL_FRAMENUMBER;
    }
    
    public final RefSOP getRefPresentationSOP() {
        return refPresentationSOP;
    }

    public final void setRefPresentationSOP(RefSOP refPresentationSOP) {
        this.refPresentationSOP = refPresentationSOP;
    }
    
    public final IconImage getIconImage() {
        return iconImage;
    }
    
    public final void setIconImage(IconImage iconImage) {
        this.iconImage = iconImage;
    }
    
    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        if (frameNumbers.length == 0 && refPresentationSOP == null) {
            return;
        }

        Dataset sop = ds.get(Tags.RefSOPSeq).getItem();
        if (frameNumbers.length != 0) {
            sop.putIS(Tags.RefFrameNumber, frameNumbers);
        }

        if (refPresentationSOP != null) {
            refPresentationSOP.toDataset(
                    sop.putSQ(Tags.RefSOPSeq).addNewItem());
        }

        if (iconImage != null) {
            iconImage.toDataset(
                    sop.putSQ(Tags.IconImageSeq).addNewItem());
        }
    }
    
}
