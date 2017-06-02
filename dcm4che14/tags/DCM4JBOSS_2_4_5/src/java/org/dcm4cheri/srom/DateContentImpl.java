/*$Id: DateContentImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
class DateContentImpl extends NamedContentImpl implements DateContent {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private long time;

    // Constructors --------------------------------------------------
    DateContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, long time) {
        super(owner, obsDateTime, template, checkNotNull(name));
        this.time = time;
    }
    
    DateContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, Date date) {
        this(owner, obsDateTime, template, name, date.getTime());
    }

    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
        return new DateContentImpl(newOwner,
                getObservationDateTime(inheritObsDateTime),
                template, name, time);
    }

    // Methodes --------------------------------------------------------
    public final void setName(Code newName) {
        this.name = checkNotNull(newName);
    }
    
    public String toString() {
        return prompt().append('(').append(getDate()).append(')').toString();
    }

    public final ValueType getValueType() {
        return ValueType.DATE;
    }
    
    public final Date getDate() {
        return new java.sql.Date(time);
    }        

    public final void setDate(Date date) {
        this.time = date.getTime();
    }        

    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        ds.putDA(Tags.Date, getDate());
    }
}
