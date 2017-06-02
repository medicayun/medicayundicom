/*$Id: DcmParseException.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG                             *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.data;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 */
public class DcmParseException extends java.io.IOException {

    /**
     * Creates a new instance of <code>DcmValueException</code> without detail
     * message.
     */
    public DcmParseException() {
    }

    /**
     * Constructs an instance of <code>DcmValueException</code> with the
     * specified detail message.
     * @param msg the detail message.
     */
    public DcmParseException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new throwable with the specified detail message and
     * cause.
     *
     * @param msg the detail message.
     * @param  cause the cause.
     */
    public DcmParseException(String msg, Throwable cause) {
        super(msg);
        super.initCause(cause);
    }
}


