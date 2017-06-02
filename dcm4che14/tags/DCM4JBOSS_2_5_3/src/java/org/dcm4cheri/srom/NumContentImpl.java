/*$Id: NumContentImpl.java 3758 2004-04-05 10:56:10Z umbertoc $*/
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
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.*;
import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
class NumContentImpl extends NamedContentImpl implements NumContent {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    private Float value;
    private Code unit;
    private Code qualifier;

    // Constructors --------------------------------------------------
    NumContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, Float value, Code unit, Code qualifier) {
        super(owner, obsDateTime, template, checkNotNull(name));
    	this.value = value;
    	this.unit = unit;
    	this.qualifier = qualifier;
    }
    
    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
    	return new NumContentImpl(newOwner,
                getObservationDateTime(inheritObsDateTime),
                template, name, value, unit, qualifier);
    }

    // Methodes --------------------------------------------------------
    public final void setName(Code newName) {
        this.name = checkNotNull(name);
    }
    
    public String toString() {
        return prompt().append(value).append(unit).toString();
    }

    public final ValueType getValueType() {
        return ValueType.NUM;
    }    
    
    public final Float getValue() {
        return value;
    }

    public final Code getUnit() {
        return unit;
    }

    public final Code getQualifier() {
        return qualifier;
    }

    public final void setValue(Float value) {
        this.value = value;
    }

    public final void setUnit(Code unit) {
        this.unit = unit;
    }

    public final void setQualifier(Code qualifier) {
        this.qualifier = qualifier;
    }

    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        DcmElement mvsq = ds.putSQ(Tags.MeasuredValueSeq);
        if (value != null && unit != null) {
            Dataset mv = mvsq.addNewItem();
	        mv.putDS(Tags.NumericValue, value.floatValue());
	        unit.toDataset(mv.putSQ(Tags.MeasurementUnitsCodeSeq).addNewItem());
        }
        if (qualifier != null) {
        	qualifier.toDataset(ds.putSQ(Tags.NumericValueQualifierCodeSeq).addNewItem());
        }
    }
}
