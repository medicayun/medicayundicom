/*$Id: ExtNegotiationImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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

import org.dcm4che.net.ExtNegotiation;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class ExtNegotiationImpl implements ExtNegotiation {

    private final String asuid;
    private final byte[] info;

    /** Creates a new instance of ExtNegotiationImpl */
    ExtNegotiationImpl(String asuid, byte[] info) {
        this.asuid = asuid;
        this.info = (byte[])info.clone();
    }

    ExtNegotiationImpl(DataInputStream din, int len) throws IOException {
        int uidLen = din.readUnsignedShort();
        this.asuid = AAssociateRQACImpl.readASCII(din, uidLen);
        this.info = new byte[len - uidLen - 2];
        din.readFully(info);
    }    

    public final String getSOPClassUID() {
        return asuid;
    }    

    public final byte[] info() {
        return (byte[])info.clone();
    }    

    final int length() {
        return 2 + asuid.length() + info.length;
    }

    void writeTo(DataOutputStream dout) throws IOException {
        dout.write(0x56);
        dout.write(0);
        dout.writeShort(length());
        dout.writeShort(asuid.length());
        dout.writeBytes(asuid);
        dout.write(info);
    }
    
}
