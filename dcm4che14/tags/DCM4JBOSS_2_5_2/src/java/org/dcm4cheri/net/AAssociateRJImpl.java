/*$Id: AAssociateRJImpl.java 3789 2004-07-18 14:41:55Z gunterze $*/
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
final class AAssociateRJImpl implements AAssociateRJ {

    private final byte[] buf;
    
    static AAssociateRJImpl parse(UnparsedPDUImpl raw) throws PDUException {
        if (raw.length() != 4) {
            throw new PDUException("Illegal A-ASSOCIATE-RJ " + raw,
                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                   AAbort.INVALID_PDU_PARAMETER_VALUE));
        }
        return new AAssociateRJImpl(raw.buffer());
    }

    private AAssociateRJImpl(byte[] buf) {
        this.buf = buf;
    }
    
    AAssociateRJImpl(int result, int source, int reason) {
        this.buf = new byte[]{ 3, 0, 0, 0, 0, 4, 0,
                (byte)result,
                (byte)source,
                (byte)reason
        };
    }

    public final int result() {
        return buf[7] & 0xff;
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
        return sb.append("A-ASSOCIATE-RJ\n\tresult=").append(resultAsString())
                .append("\n\tsource=").append(sourceAsString())
                .append("\n\treason=").append(reasonAsString());
    }    
    
    private String resultAsString() {
        switch (result()) {
            case REJECTED_PERMANENT:
                return "1 - rejected-permanent";
            case REJECTED_TRANSIENT:
                return "2 - rejected-transient";
            default:
                return String.valueOf(result());
        }
    }

    private String sourceAsString() {
        switch (source()) {
            case SERVICE_USER:
                return "1 - service-user";
            case SERVICE_PROVIDER_ACSE:
                return "2 - service-provider (ACSE)";
            case SERVICE_PROVIDER_PRES:
                return "3 - service-provider (Presentation)";
            default:
                return String.valueOf(source());
        }
    }
    
    private String reasonAsString() {
        switch (source()) {
            case SERVICE_USER:
                switch (reason()) {
                    case NO_REASON_GIVEN:
                        return "1 - no-reason-given";
                    case APPLICATION_CONTEXT_NAME_NOT_SUPPORTED:
                        return "2 - application-context-name-not-supported";
                    case CALLING_AE_TITLE_NOT_RECOGNIZED:
                        return "3 - calling-AE-title-not-recognized";
                    case CALLED_AE_TITLE_NOT_RECOGNIZED:
                        return "7 - called-AE-title-not-recognized";
                }
            case SERVICE_PROVIDER_ACSE:
                switch (reason()) {
                    case NO_REASON_GIVEN:
                        return "1 - no-reason-given";
                    case PROTOCOL_VERSION_NOT_SUPPORTED:
                        return "2 - protocol-version-not-supported";
                }
            case SERVICE_PROVIDER_PRES:
                switch (reason()) {
                    case TEMPORARY_CONGESTION:
                        return "1 - temporary-congestion";
                    case LOCAL_LIMIT_EXCEEDED:
                        return "2 - local-limit-exceeded";
                }
        }
        return String.valueOf(reason());
    }        
}
