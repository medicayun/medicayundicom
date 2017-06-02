/*$Id: ContainerContentImpl.java 3536 2002-08-05 14:16:48Z gunterze $*/
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

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.*;
import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class ContainerContentImpl extends NamedContentImpl
            implements org.dcm4che.srom.ContainerContent {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private boolean separate;

    // Constructors --------------------------------------------------
    ContainerContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, boolean separate) {
        super(owner, obsDateTime, template, name);
        this.separate = separate;
    }
    
    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
        return new ContainerContentImpl(newOwner,
                getObservationDateTime(inheritObsDateTime),
                template, name, separate);
    }

    // Methodes --------------------------------------------------------
    public String toString() {
        return prompt().append(separate ? "separate" : "continuous").toString();
    }

    public final ValueType getValueType() {
        return ValueType.CONTAINER;
    }
    
    public final boolean isSeparate() {
        return separate;
    }
    public final void setSeparate(boolean separate) {
        this.separate = separate;
    }

    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        ds.putCS(Tags.ContinuityOfContent,
                separate ? "SEPARATE" : "CONTINUOUS");
    }


    public void insertCompositeContent(Code name, SOPInstanceRef refSOP) {
        appendChild(Content.RelationType.CONTAINS,
                    owner.createCompositeContent(null, null, name, refSOP));
        owner.addCurrentEvidence(refSOP);
    }

    public void insertImageContent(Code name, SOPInstanceRef refSOP,
            int[] frameNumbers,  SOPInstanceRef refPresentationSOP,
            IconImage iconImage) {
        appendChild(Content.RelationType.CONTAINS,
                    owner.createImageContent(null, null, name, refSOP,
                            frameNumbers, refPresentationSOP, iconImage));
        owner.addCurrentEvidence(refSOP);        
        if (refPresentationSOP != null) {
            owner.addCurrentEvidence(refPresentationSOP);
        }
    }

    public void insertWaveformContent(Code name, SOPInstanceRef refSOP,
                                         int[] channelNumbers) {
        appendChild(Content.RelationType.CONTAINS,
                    owner.createWaveformContent(null, null, name, refSOP, 
                                          channelNumbers));
        owner.addCurrentEvidence(refSOP);        
    }
}
