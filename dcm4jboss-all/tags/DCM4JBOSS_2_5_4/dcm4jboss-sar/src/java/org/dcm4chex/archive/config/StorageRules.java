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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.dcm4che.net.Association;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
 * @since 15.05.2004
 *
 */
public class StorageRules {

    private final ArrayList list = new ArrayList();

    private static final class Entry {

        final Condition condition;

        final String location;

        Entry(Condition condition, String location) {
            if (location.length() == 0) throw new IllegalArgumentException();
            this.condition = condition;
            this.location = location;
        }
    }

    public StorageRules(String s) {
        boolean containsDefault = false;
        StringTokenizer stk = new StringTokenizer(s, "\r\n;");
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken().trim();
            if (tk.length() == 0) continue;
            try {
                int endCond = tk.indexOf(']') + 1;
                Condition cond = new Condition(tk.substring(0, endCond));
                containsDefault = containsDefault || cond.isAlwaysTrue();
                list.add(new Entry(cond, tk.substring(endCond)));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(tk);
            }
        }
        if (!containsDefault)
            throw new IllegalArgumentException("Missing default storage location");
    }

    public String getStorageLocationFor(Association assoc) {
        Map param = new HashMap();
		param.put("calling", new String[]{assoc.getCallingAET()});
		param.put("called", new String[]{assoc.getCalledAET()});
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            if (e.condition.isTrueFor(param)) return e.location;
        }
        throw new IllegalStateException("Missing default storage location");
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            e.condition.toStringBuffer(sb);
            sb.append(e.location);
            sb.append('\r').append('\n');
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }
}
