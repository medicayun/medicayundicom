/*$Id: RoleSelectionImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
final class RoleSelectionImpl implements RoleSelection {

    private final String asuid;
    private final boolean scu;
    private final boolean scp;
    
    /** Creates a new instance of RoleSelectionImpl */
    RoleSelectionImpl(String asuid, boolean scu, boolean scp) {
        this.asuid = asuid;
        this.scu = scu;
        this.scp = scp;
    }
    
    RoleSelectionImpl(DataInputStream din, int len)
            throws IOException, PDUException {
        int uidLen = din.readUnsignedShort();
        if (uidLen + 4 != len) {
            throw new PDUException( "SCP/SCU role selection sub-item length: "
                    + len + " mismatch UID-length:" + uidLen,
                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                               AAbort.INVALID_PDU_PARAMETER_VALUE));
        } 
        this.asuid = AAssociateRQACImpl.readASCII(din, uidLen);
        this.scu = din.readBoolean();
        this.scp = din.readBoolean();
    }

    public final String getSOPClassUID() {
        return asuid;
    }    

    public final boolean scu() {
        return scu;
    }

    public final boolean scp() {
        return scp;
    }
    
    final int length() {
        return 4 + asuid.length();
    }
    
    void writeTo(DataOutputStream dout) throws IOException {
        dout.write(0x54);
        dout.write(0);
        dout.writeShort(length());
        dout.writeShort(asuid.length());
        dout.writeBytes(asuid);
        dout.writeBoolean(scu);
        dout.writeBoolean(scp);         
    }
    
}
