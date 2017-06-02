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

package org.dcm4cheri.data;

import org.dcm4che.data.PersonName;

import java.util.Arrays;
import java.util.StringTokenizer;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class PersonNameImpl implements org.dcm4che.data.PersonName {

    private final String[] components = new String[5];
    private PersonNameImpl ideographic;
    private PersonNameImpl phonetic;  
  
    public PersonNameImpl(String s) {
        int grLen = s.indexOf('=');
        if ((grLen == -1 ? s.length() : grLen) > 64) {
            throw new IllegalArgumentException(s);
        }
        StringTokenizer stk = new StringTokenizer(s, "=^", true);
        int field = FAMILY;
        String tk;
        WHILE:
        while (stk.hasMoreTokens()) {
            tk = stk.nextToken();
            switch (tk.charAt(0)) {
                case '^':
                    if (++field > PersonName.SUFFIX)
                        throw new IllegalArgumentException(s);
                    break;
                case '=':
                    break WHILE;
                default:
                    components[field] = tk;
                    break;
            }
        }
        if (!stk.hasMoreTokens())
            return;

        tk = stk.nextToken("=");
        if (tk.charAt(0) != '=' ) {
            ideographic = new PersonNameImpl(tk);
            if (stk.hasMoreTokens())
                tk = stk.nextToken("=");
        }
        if (!stk.hasMoreTokens())
            return;

        tk = stk.nextToken();
        if (tk.charAt(0) == '=' || stk.hasMoreTokens())
            throw new IllegalArgumentException(s);

        phonetic = new PersonNameImpl(tk);
    }

    public String get(int field) {
	return components[field];
    }
    
    public void set(int field, String value) {
	components[field] = value;
    }
    
    public PersonName getIdeographic() {
	return ideographic;
    }
    
    public PersonName getPhonetic() {
	return phonetic;
    }

    public void setIdeographic(PersonName ideographic) {
	this.ideographic = (PersonNameImpl)ideographic;
    }
    
    public void setPhonetic(PersonName phonetic) {
	this.phonetic = (PersonNameImpl)phonetic;
    }
    
    private StringBuffer appendComponents(StringBuffer sb) {
        int lastField = FAMILY;
        for (int field = FAMILY; field <= SUFFIX; ++field) {
            if (components[field] != null) {
                sb.append(components[field]);
                lastField = field;
            }
            sb.append('^');
        }
        final int l = sb.length();
        sb.delete(l + lastField - SUFFIX - 1, l);
        return sb;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        appendComponents(sb);
        if (ideographic != null || phonetic != null) {
            sb.append('=');
            if (ideographic != null)
                ideographic.appendComponents(sb);
            if (phonetic != null)
                phonetic.appendComponents(sb.append('='));
        }
        return sb.toString();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof PersonNameImpl)) {
            return false;
        }
        
        PersonNameImpl other = (PersonNameImpl)o;
        return Arrays.equals(components, other.components);
    }
}
