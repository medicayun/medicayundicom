/* $Id: ForwardAETs.java 925 2004-01-07 21:22:45Z gunterze $
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 925 $ $Date: 2004-01-08 05:22:45 +0800 (周四, 08 1月 2004) $
 * @since 07.01.2004
 */
final class ForwardAETs {

    private String[] defaultAETs = {};
    private final ArrayList list = new ArrayList();
    private static final class Entry {
        final HashSet callingAETs;
        final String[] forwardAETs;
        Entry(String[] callingAETs, String[] forwardAETs) {
            this.callingAETs = new HashSet(Arrays.asList(callingAETs));
            this.forwardAETs = forwardAETs;
        }
    }

    public String[] get(String callingAET) {
        HashSet result = new HashSet();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            if (e.callingAETs.contains(callingAET)) {
                return e.forwardAETs;                                
            }
        }
        return defaultAETs;
    }

    public void setDefault(String[] forwardAETs) {
        this.defaultAETs = forwardAETs;
    }

    public void add(String[] callingAETs, String[] forwardAETs) {
        list.add(new Entry(callingAETs, forwardAETs));
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            appendTo(e.callingAETs, sb);
            sb.append(':');
            appendTo(Arrays.asList(e.forwardAETs), sb);
            sb.append(',');
        }
        appendTo(Arrays.asList(defaultAETs), sb);
        return sb.toString();
    }
    
    private static void appendTo(Collection aets, StringBuffer sb) {
        if (aets.isEmpty()) {
            return;
        }
        for (Iterator it = aets.iterator(); it.hasNext();) {
            sb.append((String) it.next()).append('\\');
        }
        sb.setLength(sb.length()-1);
    }

}
