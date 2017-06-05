/* $Id: DuplicateStorageException.java 1052 2004-03-18 16:06:16Z gunterze $
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
package org.dcm4chex.archive.ejb.interfaces;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1052 $ $Date: 2004-03-19 00:06:16 +0800 (周五, 19 3月 2004) $
 * @since 18.03.2004
 *
 */
public class DuplicateStorageException extends Exception {

    public DuplicateStorageException() {
        super();
    }

    public DuplicateStorageException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }
}
