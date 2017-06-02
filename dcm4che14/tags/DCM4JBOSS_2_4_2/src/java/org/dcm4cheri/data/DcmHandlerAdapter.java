/*$Id: DcmHandlerAdapter.java 3656 2003-06-09 17:17:51Z gunterze $*/
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

import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.dict.TagDictionary;

import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.dcm4cheri.util.StringUtils;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class DcmHandlerAdapter implements org.dcm4che.data.DcmHandler {

    private static final Attributes EMPTY_ATTR = new AttributesImpl();
    
    private final ContentHandler handler;
    private final TagDictionary dict;
    private boolean explicitVR;
    private ByteOrder byteOrder;
    private static final int UNDEF = 0;
    private static final int COMMAND = 1;
    private static final int FILEMETAINFO = 2;
    private static final int DATASET = 3;
    private static final int DCMFILE = 4;
    private int docType = UNDEF;
    private int vr;
    
    /** Creates a new instance of ContentHandlerAdapter */
    public DcmHandlerAdapter(ContentHandler handler, TagDictionary dict)
    {
        this.handler = handler;
        this.dict = dict;
    }

    public void startCommand() throws IOException {
        start(COMMAND, "command", EMPTY_ATTR);
    }
    
    public void endCommand() throws IOException {
        end(COMMAND, "command");
    }
    
    public void startFileMetaInfo(byte[] preamble) throws IOException {
        AttributesImpl attrs = new AttributesImpl();
        if (preamble != null)
            attrs.addAttribute("","preamble","preamble","",
                    StringUtils.promptBytes(preamble, 0, preamble.length));
        start(FILEMETAINFO, "filemetainfo", attrs);
    }
    
    public void endFileMetaInfo() throws IOException {
        end(FILEMETAINFO, "filemetainfo");
    }

    public void startDataset() throws IOException {
        start(DATASET, "dataset", EMPTY_ATTR);
    }
    
    public void endDataset() throws IOException {
        end(DATASET, "dataset");
    }

    public void startDcmFile() throws IOException {
        start(DCMFILE, "dicomfile", EMPTY_ATTR);
    }

    public void endDcmFile() throws IOException {
        end(DCMFILE, "dicomfile");
    }

    private void start(int docType, String xmltag, Attributes attrs)
            throws IOException {
        try {
            if (this.docType == UNDEF) {
                this.docType = docType;
                handler.startDocument();
            }
            handler.startElement("", xmltag, xmltag, attrs);
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in start " + xmltag)
                    .initCause(se);
        }
    }        
    
    private void end(int docType, String xmltag) throws IOException  {
        try {
            handler.endElement("",xmltag,xmltag);
            if (this.docType == docType) {
                handler.endDocument();
                docType = UNDEF;
            }
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in end " + xmltag)
                    .initCause(se);
        }
    }
        
    public void setDcmDecodeParam(DcmDecodeParam param) {
        this.byteOrder = param.byteOrder;
        this.explicitVR = param.explicitVR;
    }

    public void startElement(int tag, int vr, long pos) throws IOException {
        this.vr = vr;
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("","tag","tag","",Tags.toHexString(tag, 8));
            attrs.addAttribute("","vr","vr","",VRs.toString(vr));
            attrs.addAttribute("","pos","pos","", "" + pos);
            if (dict != null) {
                TagDictionary.Entry entry = dict.lookup(tag);
                if (entry != null)
                    attrs.addAttribute("","name","name","",entry.name);
            }
            handler.startElement("","elm","elm",attrs);
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in startElement")
                    .initCause(se);
        }
    }

    public void startSequence(int length) throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("","len","len","", "" + length);
            handler.startElement("","seq","seq",attrs);
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in startElement")
                    .initCause(se);
        }
    }
    
    public void fragment(int id, long pos, byte[] data, int start, int length)
            throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("","id","id","", "" + id);
            attrs.addAttribute("","pos","pos","", "" + pos);
            attrs.addAttribute("","len","len","", "" + length);
            attrs.addAttribute("","data","data","",
                    StringUtils.promptValue(vr,
                        ByteBuffer.wrap(data, start, length).order(byteOrder)));
            handler.startElement("","frag","frag",attrs);
            handler.endElement("","frag","frag");        
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in fragment")
                    .initCause(se);
        }
    }
    
    public int vm(byte[] data, int start, int length) throws IOException {
        if (length == 0)
            return 0;
        
        switch (vr) {
            case VRs.LT: case VRs.ST: case VRs.OB: case VRs.OW: case VRs.UN:
            case VRs.UT:
                return 1;
            case VRs.SS: case VRs.US:
                return length >> 1;
            case VRs.AT: case VRs.FL: case VRs.SL: case VRs.UL:                
                return length >> 2;
            case VRs.FD:
                return length >> 3;
        }
        int count = 1;
        for (int i = 0, j = start; i < length; ++i,++j)
            if (data[j] == '\\')
                ++count;
        return count;
    }
  
    public void value(byte[] data, int start, int length) throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("","vm","vm","","" + vm(data, start, length));
            attrs.addAttribute("","len","len","","" + length);
            attrs.addAttribute("","data","data","",
                    StringUtils.promptValue(vr, ByteBuffer.wrap(data, start,
                            length).order(byteOrder)));
            handler.startElement("","val","val",attrs);
            handler.endElement("","val","val");        
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in value")
                    .initCause(se);
        }
    }
    
    public void endElement() throws IOException {
        try {
            handler.endElement("","elm","elm");        
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in endElement")
                    .initCause(se);
        }
    }

    public void endSequence(int length) throws IOException {
        try {
            handler.endElement("","seq","seq");
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in endSequence")
                    .initCause(se);
        }
    }
    
    public void startItem(int id, long pos, int length) throws IOException {
        try {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("","id","id","", "" + id);
            attrs.addAttribute("","pos","pos","", "" + pos);
            attrs.addAttribute("","len","len","", "" + length);
            handler.startElement("","item","item",attrs);
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in startItem")
                    .initCause(se);
        }
    }
    
    public void endItem(int len) throws IOException {
        try {
            handler.endElement("","item","item");
        } catch (SAXException se) {
            throw (IOException)new IOException("Exception in endItem")
                    .initCause(se);
        }
    }    
}
