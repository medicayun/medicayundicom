/* $Id: AETFilter.java 3764 2004-04-10 09:34:53Z gunterze $
 * Copyright (c) 2004 by TIANI MEDGRAPH AG
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
package org.dcm4che.net;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 3764 $ $Date: 2004-04-10 17:34:53 +0800 (周六, 10 4月 2004) $
 * @since 09.04.2004
 *
 */
public interface AETFilter {
    boolean accept(String aet); 
}
