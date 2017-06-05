/* $Id: HomeFactoryException.java 937 2004-01-26 21:26:25Z gunterze $
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
package org.dcm4chex.archive.util;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 937 $ $Date: 2004-01-27 05:26:25 +0800 (周二, 27 1月 2004) $
 * @since 16.12.2003
 */
public class HomeFactoryException extends Exception {

    /**
     * 
     */
    public HomeFactoryException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public HomeFactoryException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public HomeFactoryException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public HomeFactoryException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
