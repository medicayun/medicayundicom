/*$Id: DimseWriterImpl.java 3593 2003-02-09 17:23:10Z gunterze $*/
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
import org.dcm4che.data.Command;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DimseWriterImpl {

    private final FsmImpl fsm;
    private PDataTFImpl pDataTF = null;
    private int pcid;
    private boolean cmd;
    private boolean packPDVs = false;
    
//    PDataTFOutputStream pDataTFout = null;

    /** Creates a new instance of PDataTFWriteAdapter */
    public DimseWriterImpl(FsmImpl fsm) {
        this.fsm = fsm;
    }
    
    public synchronized void write(Dimse dimse) throws IOException {
        pcid = dimse.pcid();
        String tsUID = fsm.getAcceptedTransferSyntaxUID(pcid);
        if (tsUID == null) {
            throw new IllegalStateException();
        }
        ((DimseImpl)dimse).setTransferSyntaxUID(tsUID);
        fsm.fireWrite(dimse);
        if (pDataTF == null) {
            pDataTF = new PDataTFImpl(fsm.getWriteMaxLength());
        }
        pDataTF.openPDV(pcid, cmd = true);
        OutputStream out = new PDataTFOutputStream();
        Command c = dimse.getCommand();
        try {
            c.write(out);
        } finally {
            out.close();
        }
        if (c.hasDataset()) {
            if (!packPDVs) {
                flushPDataTF();
            }
            pDataTF.openPDV(pcid, cmd = false);
            out = new PDataTFOutputStream();
            try {
                dimse.writeTo(out, tsUID);
            } finally {
                out.close();
            }                
        }
        flushPDataTF();
    }
    
    public void flushPDataTF() throws IOException {
        boolean open = pDataTF.isOpenPDV();
        if (open) {
            pDataTF.closePDV(false);
        }
        if (!pDataTF.isEmpty()) {
            fsm.write(pDataTF);
        }
        pDataTF.clear();
//        pDataTF = new PDataTFImpl(fsm.getMaxLength()); 
        if (open) {
            pDataTF.openPDV(pcid, cmd);
        }
    }
        
    private void closeStream() throws IOException {
        pDataTF.closePDV(true);
        if (!cmd) {
            flushPDataTF();
        }
    }

    /** Getter for property packPDVs.
     * @return Value of property packPDVs.
     */
    public boolean isPackPDVs() {
        return packPDVs;
    }
    
    /** Setter for property packPDVs.
     * @param packPDVs New value of property packPDVs.
     */
    public void setPackPDVs(boolean packPDVs) {
        this.packPDVs = packPDVs;
    }
    
    private class PDataTFOutputStream extends OutputStream {
        public final void write(int b) throws IOException {
            if (pDataTF.free() == 0) {
                flushPDataTF();
            }
            pDataTF.write(b);
        }
        public final void write(byte b[], int off, int len)
                throws IOException {
            if (len == 0) {
                return;
            }
            int n = 0;
            for (;;) {
                int c = Math.min(pDataTF.free(), len - n);
                pDataTF.write(b, off + n, c);
                n += c;
                if (n == len) {
                    return;
                }
                flushPDataTF();
            }
        }
        public void close() throws IOException {
            closeStream();
        }
    }
}
