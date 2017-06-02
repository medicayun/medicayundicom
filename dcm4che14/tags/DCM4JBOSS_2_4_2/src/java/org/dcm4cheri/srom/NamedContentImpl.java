/*$Id: NamedContentImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
import java.util.Date;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0
 */
abstract class NamedContentImpl extends ContentImpl {
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    protected Code name;
    protected final Long obsDateTime;
    protected final Template template;
    
    protected static Code checkNotNull(Code name) {
        if (name == null)
            throw new NullPointerException();
        return name;
    }
    // Constructors --------------------------------------------------
    NamedContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name) {
        super(owner);
        this.name = name;
        this.template = template;
        this.obsDateTime = obsDateTime != null ? new Long(obsDateTime.getTime())
                                               : null;
    }

    // Methodes ------------------------------------------------------
    StringBuffer prompt() {
        return super.prompt().append(getValueType()).append(name).append('=');
    }

    public Code getName() {
        return name;
    }
    
    public void setName(Code newName) {
        this.name = newName;
    }

    public Date getObservationDateTime(boolean inherit) {
        if (obsDateTime != null)
            return new Date(obsDateTime.longValue());
        
        if (!inherit)
            return null;
        
        if (parent != null)
            return parent.getObservationDateTime(true);
            
        return owner.getContentDateTime();
    }
    
    public Template getTemplate() {
        return template;
    }
}
