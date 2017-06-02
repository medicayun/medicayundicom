/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

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
