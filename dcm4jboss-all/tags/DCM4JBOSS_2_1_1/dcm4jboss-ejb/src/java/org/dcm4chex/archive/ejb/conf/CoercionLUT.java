/* $Id: CoercionLUT.java 922 2004-01-05 17:08:29Z gunterze $
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

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 922 $ $Date: 2004-01-06 01:08:29 +0800 (周二, 06 1月 2004) $
 * @since 05.01.2004
 */
public class CoercionLUT {
	Hashtable params = new Hashtable();
    
	public String lookup(String name) {
	    return (String) params.get(name);
	}
}
