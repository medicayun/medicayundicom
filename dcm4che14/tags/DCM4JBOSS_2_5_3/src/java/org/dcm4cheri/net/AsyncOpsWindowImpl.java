/*$Id: AsyncOpsWindowImpl.java 3493 2002-07-14 16:03:36Z gunterze $*/
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
final class AsyncOpsWindowImpl implements AsyncOpsWindow {

    private final int maxOpsInvoked;
    private final int maxOpsPerformed;
    
    static final AsyncOpsWindow DEFAULT = new AsyncOpsWindowImpl(1,1);
    
    /** Creates a new instance of AsyncOpsWindowImpl */
    AsyncOpsWindowImpl(int maxOpsInvoked, int maxOpsPerformed) {
        this.maxOpsInvoked = maxOpsInvoked;
        this.maxOpsPerformed = maxOpsPerformed;
    }
    
    AsyncOpsWindowImpl(DataInputStream din, int len)
            throws IOException, PDUException {
        if (len != 4) {
            throw new PDUException(
                    "Illegal length of AsyncOpsWindow sub-item: " + len,
                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                               AAbort.INVALID_PDU_PARAMETER_VALUE));
        }
        this.maxOpsInvoked = din.readUnsignedShort();
        this.maxOpsPerformed = din.readUnsignedShort();
    }
    
    public final int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }
    
    public final int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }
    
    void writeTo(DataOutputStream dout) throws IOException {
        dout.write(0x53);
        dout.write(0);
        dout.writeShort(4);
        dout.writeShort(maxOpsInvoked);
        dout.writeShort(maxOpsPerformed);
    }
    
    public String toString() {
       return "AsyncOpsWindow[maxOpsInvoked=" + maxOpsInvoked
                          + ",maxOpsPerformed=" + maxOpsPerformed
                          + "]";
    }

}
