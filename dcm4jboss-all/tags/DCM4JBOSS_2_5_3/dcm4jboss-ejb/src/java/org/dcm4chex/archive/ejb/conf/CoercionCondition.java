/* $Id: CoercionCondition.java 915 2003-12-29 16:16:31Z gunterze $
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
package org.dcm4chex.archive.ejb.conf;

import java.util.Hashtable;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 915 $ $Date: 2003-12-30 00:16:31 +0800 (周二, 30 12月 2003) $
 * @since 28.12.2003
 */
public class CoercionCondition {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    Dataset condition = dof.newDataset();
    Hashtable params = new Hashtable();

    public boolean match(String callingAET, String calledAET, Dataset ds) {
        return matchParam("calling-aet", callingAET)
            && matchParam("called-aet", calledAET)
            && ds.match(condition, false, false);
    }
    
    protected boolean matchParam(String name, String value) {
        String s = (String) params.get(name);
        return s == null || s.length() == 0 || s.equals(value);
    }
}
