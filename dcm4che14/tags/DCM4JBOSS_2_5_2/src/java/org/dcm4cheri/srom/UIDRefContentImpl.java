/*$Id: UIDRefContentImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
class UIDRefContentImpl extends NamedContentImpl implements UIDRefContent {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private String uid;

    // Constructors --------------------------------------------------
    UIDRefContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, String uid) {
        super(owner, obsDateTime, template, checkNotNull(name));
        setUID(uid);
    }
    
    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
        return new UIDRefContentImpl(newOwner,
                getObservationDateTime(inheritObsDateTime),
                template, name, uid);
    }

    // Methodes --------------------------------------------------------
    public final void setName(Code newName) {
        this.name = checkNotNull(newName);
    }
    
    public String toString() {
        return prompt().append('\"').append(uid).append('\"').toString();
    }

    public final ValueType getValueType() {
        return ValueType.UIDREF;
    }
    
    public final String getUID() {
        return uid;
    }
    
    public final void setUID(String uid) {
        if (uid.length() == 0)
            throw new IllegalArgumentException();
        this.uid = uid;
    }

    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        ds.putUI(Tags.UID, uid);
    }
}
