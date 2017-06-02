/*$Id: TextContentImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
class TextContentImpl extends NamedContentImpl
        implements org.dcm4che.srom.TextContent {
    // Constants -----------------------------------------------------
    private static final int TEXT_PROMPT_LEN = 20;
    
    // Attributes ----------------------------------------------------
    private String text;

    // Constructors --------------------------------------------------
    TextContentImpl(KeyObject owner, Date obsDateTime, Template template,
            Code name, String text) {
        super(owner, obsDateTime, template, checkNotNull(name));
        setText(text);
    }
    
    Content clone(KeyObject newOwner,  boolean inheritObsDateTime) {
        return new TextContentImpl(newOwner,
                getObservationDateTime(inheritObsDateTime),
                template, name, text);
    }

    // Methodes --------------------------------------------------------
    public final void setName(Code newName) {
        this.name = checkNotNull(newName);
    }
    
    public String toString() {
        StringBuffer sb = prompt().append('\"');
        if (text.length() <= TEXT_PROMPT_LEN)
            sb.append(text);
        else
            sb.append(text.substring(0,TEXT_PROMPT_LEN)).append("..");
        return sb.append('\"').toString();
    }

    public final ValueType getValueType() {
        return ValueType.TEXT;
    }
    
    public final String getText() {
        return text;
    }

    public final void setText(String text) {
        if (text.length() == 0)
            throw new IllegalArgumentException();
        this.text = text;
    }

    public void toDataset(Dataset ds) {
        super.toDataset(ds);
        ds.putUT(Tags.TextValue, text);
    }
}
