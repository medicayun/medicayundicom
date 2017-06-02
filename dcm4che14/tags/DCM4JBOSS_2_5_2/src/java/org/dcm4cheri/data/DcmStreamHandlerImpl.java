/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

/*$Id: DcmStreamHandlerImpl.java 3898 2005-07-11 14:38:36Z gunterze $*/

package org.dcm4cheri.data;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.stream.ImageOutputStream;

import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.VRs;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class DcmStreamHandlerImpl implements org.dcm4che.data.DcmHandler {
    
    private static final int ITEM_TAG = 0xFFFEE000;
    private static final int ITEM_DELIMITATION_ITEM_TAG = 0xFFFEE00D;
    private static final int SEQ_DELIMITATION_ITEM_TAG = 0xFFFEE0DD;

    private final byte[] b12 = new byte[12];
    private final ByteBuffer bb12 = 
            ByteBuffer.wrap(b12).order(ByteOrder.LITTLE_ENDIAN);
    private boolean explicitVR = false;
    private int tag = 0;
    private int vr = 0;
    
    private DataOutput out;

    /** Creates a new instance of DcmStreamHandlerImpl */
    public DcmStreamHandlerImpl(OutputStream out) {
        this.out  = out instanceof DataOutput ? (DataOutput)out
                                              : new DataOutputStream(out);
    }

    /** Creates a new instance of DcmStreamHandlerImpl */
    public DcmStreamHandlerImpl(ImageOutputStream out) {
        this.out  = out;
    }

    public void startCommand() {
        // noop
    }
    
    public void endCommand() {
        // noop
    }
    
    public void startDcmFile() {
        // noop
    }
    
    public void endDcmFile() {
        // noop
    }
    
    public void startFileMetaInfo(byte[] preamble) throws IOException {
        if (preamble != null) {
            out.write(preamble, 0, 128);
            out.write(FileMetaInfoImpl.DICM_PREFIX, 0, 4);
        }
    }
    
    public void endFileMetaInfo() {
        // noop
    }
    
    public void startDataset() {
        // noop
    }
    
    public void endDataset() {
        // noop
    }
    
    public void setDcmDecodeParam(DcmDecodeParam param) {
        bb12.order(param.byteOrder);
        this.explicitVR = param.explicitVR;
    }
/*    public final void setByteOrder(ByteOrder byteOrder) {
        bb12.order(byteOrder);
    }

    public final void setExplicitVR(boolean explicitVR) {
        this.explicitVR = explicitVR;
    }
*/
    public int writeHeader(int tag, int vr, int len) throws IOException {
        bb12.clear();
        bb12.putShort((short)(tag >>> 16));
        bb12.putShort((short)tag);
        if (!explicitVR || vr == VRs.NONE) {
            bb12.putInt(len);
            out.write(b12,0,8);
            return 8;
        }
        bb12.put((byte)(vr >>> 8));
        bb12.put((byte)vr);
        if (VRs.isLengthField16Bit(vr)) {
            bb12.putShort((short)len);
            out.write(b12,0,8);
            return 8;
        }
        bb12.put((byte)0);
        bb12.put((byte)0);
        bb12.putInt(len);
        out.write(b12,0,12);
        return 12;
    }
            
    public void startElement(int tag, int vr, long pos) throws IOException {
        this.tag = tag;
        this.vr = vr;
    }
    
    public void endElement() throws IOException {
    }

    public void startSequence(int len) throws IOException {
        writeHeader(tag, vr, len);
    }

    public void endSequence(int len) throws IOException {
        if (len == -1)
            writeHeader(SEQ_DELIMITATION_ITEM_TAG, VRs.NONE, 0);
    }
    
    public void startItem(int id, long pos, int len) throws IOException {
        writeHeader(ITEM_TAG, VRs.NONE, len);
    }
    
    public void endItem(int len) throws IOException {
        if (len == -1)
            writeHeader(ITEM_DELIMITATION_ITEM_TAG, VRs.NONE, 0);
    }

    public void value(byte[] data, int start, int length) throws IOException {
        writeHeader(tag, vr, (length+1)&(~1));
        out.write(data, start, length);
        if ((length & 1) != 0)
            out.write(VRs.getPadding(vr));
    }
    
    public void fragment(int id, long pos, byte[] data, int start, int length)
            throws IOException {
        writeHeader(ITEM_TAG, VRs.NONE, (length+1)&(~1));
        out.write(data, start, length);
        if ((length & 1) != 0)
            out.write(0);
    }        
}