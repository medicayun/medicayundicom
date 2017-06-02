/*$Id: DcmObjectHandlerImpl.java 3656 2003-06-09 17:17:51Z gunterze $*/
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

package org.dcm4cheri.data;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObject;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.VRs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.LinkedList;
/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class DcmObjectHandlerImpl implements org.dcm4che.data.DcmHandler {

    private final DcmObject result;
    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    private DcmObject curDcmObject;
    private int tag;
    private int vr;
    private long pos;
    private final LinkedList seqStack = new LinkedList();
    
    /** Creates a new instance of DcmHandlerImpl */
    public DcmObjectHandlerImpl(DcmObject result) {
        if (result == null)
            throw new NullPointerException();
        
        this.result = result;
    }

    public void startCommand() {
        curDcmObject = (Command)result;
        seqStack.clear();
    }
    
    public void endCommand() {
        curDcmObject = null;
    }
    
    public void startDcmFile() {
        // noop
    }
    
    public void endDcmFile() {
        // noop
    }
    
    public void startFileMetaInfo(byte[] preamble) {
        if (result instanceof Dataset) {
            curDcmObject = ((Dataset)result).getFileMetaInfo();
            if (curDcmObject == null)
                ((Dataset)result).setFileMetaInfo((FileMetaInfo)
                        (curDcmObject =  new FileMetaInfoImpl()));
        } else
            curDcmObject = (FileMetaInfo)result;
        seqStack.clear();
        if (preamble != null) {
            if (preamble.length == 128) {
                System.arraycopy(preamble, 0,
                    ((FileMetaInfo)curDcmObject).getPreamble(), 0, 128);
            } else {
                // log.warn
            }
        }
    }
    
    public void endFileMetaInfo() {
        if (result instanceof Dataset) {
            curDcmObject = result;
        } else
            curDcmObject = null;
    }
    
    public void startDataset() {
        curDcmObject = (Dataset)result;
        seqStack.clear();
    }
    
    public void endDataset() {
        curDcmObject = null;
    }
    
    public void setDcmDecodeParam(DcmDecodeParam param) {
        this.byteOrder = param.byteOrder;
    }
            
    public void startElement(int tag, int vr, long pos)
            throws IOException {
        this.tag = tag;
        this.vr = vr;
        this.pos = pos;
    }
    
    public void endElement() throws IOException {
    }

    public void startSequence(int length) throws IOException {
        seqStack.add(vr == VRs.SQ ? curDcmObject.putSQ(tag)
                                      : curDcmObject.putXXsq(tag, vr));
    }

    public void endSequence(int length) throws IOException {
        seqStack.removeLast();
    }
    
    public void value(byte[] data, int start, int length) throws IOException {
        curDcmObject.putXX(tag, vr,
                ByteBuffer.wrap(data, start, length).order(byteOrder))
                .setStreamPosition(pos);
    }
    
    public void fragment(int id, long pos, byte[] data, int start, int length)
            throws IOException {
        ((DcmElement)seqStack.getLast()).addDataFragment(
                ByteBuffer.wrap(data, start, length).order(byteOrder));
    }
    
    public void startItem(int id, long pos, int length) throws IOException {
        curDcmObject = ((DcmElement)seqStack.getLast()).addNewItem()
                .setItemOffset(pos);
    }
    
    public void endItem(int len) throws IOException {
        curDcmObject = ((Dataset)curDcmObject).getParent();
    }    
}
