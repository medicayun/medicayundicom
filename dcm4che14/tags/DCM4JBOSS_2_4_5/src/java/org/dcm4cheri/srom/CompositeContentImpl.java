/*$Id: CompositeContentImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
class CompositeContentImpl extends NamedContentImpl
        implements org.dcm4che.srom.CompositeContent {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    protected RefSOP refSOP;

    // Constructors --------------------------------------------------
    CompositeContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, RefSOP refSOP) {
        super(owner, obsDateTime, template, name);
        setRefSOP(refSOP);
    }
    
    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
        return new CompositeContentImpl(newOwner,
                getObservationDateTime(inheritObsDateTime),
                template, name, refSOP);
    }
    
    public final void setRefSOP(RefSOP refSOP) {
        if (refSOP == null)
            throw new NullPointerException();
        this.refSOP = refSOP;
    }


    // Methodes --------------------------------------------------------
    public String toString() {
        return prompt().append(refSOP).toString();
    }

    public ValueType getValueType() {
        return ValueType.COMPOSITE;
    }
    
    public final RefSOP getRefSOP() {
        return refSOP;
    }

    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        refSOP.toDataset(ds.putSQ(Tags.RefSOPSeq).addNewItem());
    }
}
