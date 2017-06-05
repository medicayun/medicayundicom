/* $Id: ForwardAETsEditor.java 925 2004-01-07 21:22:45Z gunterze $
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
package org.dcm4chex.service;

import java.beans.PropertyEditorSupport;
import java.util.StringTokenizer;

import org.dcm4cheri.util.StringUtils;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 925 $ $Date: 2004-01-08 05:22:45 +0800 (周四, 08 1月 2004) $
 * @since 07.01.2004
 */
class ForwardAETsEditor extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        ForwardAETs theValue = new ForwardAETs();
        StringTokenizer stk = new StringTokenizer(text, " ,\t\n\r\f", false);
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken();
            int colon = tk.indexOf(':');
            if (colon <= 0) {
                theValue.setDefault(StringUtils.split(tk, '\\'));
            } else {
                theValue.add(StringUtils.split(tk.substring(0, colon), '\\'), StringUtils.split(tk.substring(colon+1), '\\'));
            }
        }
        super.setValue(theValue);
    }

}
