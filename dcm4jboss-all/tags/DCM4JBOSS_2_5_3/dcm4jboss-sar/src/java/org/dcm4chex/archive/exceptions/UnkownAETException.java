/* $Id: UnkownAETException.java 1765 2005-06-01 23:43:50Z gunterze $
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
package org.dcm4chex.archive.exceptions;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1765 $ $Date: 2005-06-02 07:43:50 +0800 (周四, 02 6月 2005) $
 * @since 20.04.2004
 */
public class UnkownAETException extends Exception {    

	private static final long serialVersionUID = 3257007665707431219L;

	public UnkownAETException() {
        super();
    }

    public UnkownAETException(String message) {
        super(message);
    }

}
