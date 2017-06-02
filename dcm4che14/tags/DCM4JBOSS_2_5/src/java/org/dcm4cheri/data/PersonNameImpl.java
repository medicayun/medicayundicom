/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

/*$Id: PersonNameImpl.java 3694 2003-07-19 22:13:50Z gunterze $*/

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
