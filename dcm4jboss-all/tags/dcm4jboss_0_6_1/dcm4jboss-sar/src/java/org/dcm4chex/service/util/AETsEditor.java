/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.service.util;

import java.beans.PropertyEditorSupport;
import java.util.StringTokenizer;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 894 $
 * @since 02.08.2003
 */
public class AETsEditor extends PropertyEditorSupport {
    private static String ANY = "__any__";

    public String getAsText() {
        String[] a = (String[]) getValue();
        if (a == null || a.length == 0) {
            return ANY;
        }
        StringBuffer sb = new StringBuffer(a[0]);
        for (int i = 1; i < a.length; i++) {
            sb.append(',').append(a[i]);
        }
        return sb.toString();
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (ANY.equalsIgnoreCase(text)) {
            setValue(null);
        } else {
            StringTokenizer stk = new StringTokenizer(text, ", \t\n\r");
            String[] a = new String[stk.countTokens()];
            for (int i = 0; i < a.length; i++) {
                a[i] = stk.nextToken();
            }
            setValue(a);
        }
    }
}
