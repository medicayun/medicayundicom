/* $Id: NoPresContextException.java 1090 2004-04-22 12:45:01Z gunterze $
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

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1090 $ $Date: 2004-04-22 20:45:01 +0800 (周四, 22 4月 2004) $
 * @since 20.04.2004
 */
public class NoPresContextException extends Exception {

    /**
     * 
     */
    public NoPresContextException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public NoPresContextException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public NoPresContextException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public NoPresContextException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
