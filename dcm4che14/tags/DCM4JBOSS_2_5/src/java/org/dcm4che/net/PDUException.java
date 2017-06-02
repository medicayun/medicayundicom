/*$Id: PDUException.java 3493 2002-07-14 16:03:36Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                             *
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

package org.dcm4che.net;

import java.io.IOException;

/**
 *
 * @author  <a href="mailto:gunter.zeilinger@tiani.com">gunter zeilinger</a>
 * @version 1.0.0
 */
public class PDUException extends IOException {

    private final AAbort abort;
    
    /**
     * Constructs an instance of <code>PDUException</code> with the
     * specified detail message and corresponding A-Abort PDU.
     * @param msg the detail message.
     * @param abort corresponding A-Abort PDU.
     */
    public PDUException(String msg, AAbort abort) {
        super(msg);
        this.abort = abort;
    }

    /**
     * Constructs a new throwable with the specified detail message and
     * cause and corresponding A-Abort PDU.
     *
     * @param msg the detail message.
     * @param  cause the cause.
     * @param abort corresponding A-Abort PDU.
     */
    public PDUException(String msg, Throwable cause, AAbort abort) {
        super(msg);
        super.initCause(cause);
        this.abort = abort;
    }
    
    /**
     * Returns corresponding A-Abort PDU.
     * @return corresponding A-Abort PDU.
     */
    public final AAbort getAAbort() {
        return abort;
    }
}


