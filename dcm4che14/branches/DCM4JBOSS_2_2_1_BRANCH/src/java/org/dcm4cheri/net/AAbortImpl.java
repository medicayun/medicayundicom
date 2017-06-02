/*$Id: AAbortImpl.java 3538 2002-08-06 17:45:17Z gunterze $*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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

package org.dcm4cheri.net;

import org.dcm4che.net.*;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class AAbortImpl implements AAbort {
    
    private final byte[] buf;
    
    static AAbortImpl parse(UnparsedPDUImpl raw) throws PDUException {
        if (raw.length() != 4) {
            throw new PDUException("Illegal A-ABORT " + raw,
                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                   AAbort.INVALID_PDU_PARAMETER_VALUE));
        }
        return new AAbortImpl(raw.buffer());
    }

    private AAbortImpl(byte[] buf) {
        this.buf = buf;
    }
    
    AAbortImpl(int source, int reason) {
        this.buf = new byte[]{ 7, 0, 0, 0, 0, 4, 0, 0,
                (byte)source,
                (byte)reason
        };
    }

    public final int source() {
        return buf[8] & 0xff;
    }
    
    public final int reason() {
        return buf[9] & 0xff;
    }
    
    public void writeTo(OutputStream out) throws IOException {
        out.write(buf);
//        out.flush();
    }

    public String toString(boolean verbose) {
       return toString();
    }

    public String toString() {
        return toStringBuffer(new StringBuffer()).toString();
    }
    
    final StringBuffer toStringBuffer(StringBuffer sb) {
        return sb.append("A-ABORT\n\tsource=").append(sourceAsString())
                .append("\n\treason=").append(reasonAsString());
    }    
    
    private String sourceAsString() {
        switch (source()) {
            case SERVICE_USER:
                return "0 - service-user";
            case SERVICE_PROVIDER:
                return "2 - service-provider";
            default:
                return String.valueOf(source());
        }
    }
    
    private String reasonAsString() {
        switch (reason()) {
            case REASON_NOT_SPECIFIED:
                return "0 - reason-not-specified";
            case UNRECOGNIZED_PDU:
                return "1 - unrecognized-PDU";
            case UNEXPECTED_PDU:
                return "2 - unexpected-PDU";
            case UNRECOGNIZED_PDU_PARAMETER:
                return "4 - unrecognized-PDU parameter";
            case UNEXPECTED_PDU_PARAMETER:
                return "5 - unexpected-PDU parameter";
            case INVALID_PDU_PARAMETER_VALUE:
                return "6 - invalid-PDU-parameter value";
            default:
                return String.valueOf(reason());
        }
    }        
}
