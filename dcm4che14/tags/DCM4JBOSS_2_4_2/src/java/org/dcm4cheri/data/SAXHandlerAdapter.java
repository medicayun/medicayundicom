/*$Id: SAXHandlerAdapter.java 3656 2003-06-09 17:17:51Z gunterze $*/
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
import org.dcm4che.data.DcmHandler;
import org.dcm4che.dict.VRs;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.Charset;

import org.dcm4cheri.util.StringUtils;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
class SAXHandlerAdapter extends org.xml.sax.helpers.DefaultHandler {

    private final DcmHandler handler;
    
    private int vr;
    
    /** Creates a new instance of DatasetXMLAdapter */
    public SAXHandlerAdapter(DcmHandler handler) {
        this.handler = handler;
    }
    
    public void startDocument() throws SAXException
    {
	handler.setDcmDecodeParam(DcmDecodeParam.EVR_LE);
    }
    
    public void startElement (String uri, String localName,
			      String qName, Attributes attr)
	throws SAXException
    {
        try {
            if ("elm".equals(qName)) {
                element(attr.getValue("tag"), 
                        attr.getValue("vr"),
                        attr.getValue("pos"));
            } else if ("val".equals(qName)) {
                value(attr.getValue("len"),
                      attr.getValue("data"));
            } else if ("seq".equals(qName)) {
                handler.startSequence(-1);
            } else if ("item".equals(qName)) {
                item(attr.getValue("id"),
                        attr.getValue("pos"));
            } else if ("frag".equals(qName)) {
                fragment(attr.getValue("id"),
                        attr.getValue("pos"),
                        attr.getValue("len"),
                        attr.getValue("data"));
            } else if ("filemetainfo".equals(qName)) {
                handler.startFileMetaInfo(
                        preamble(attr.getValue("preamble")));
            } else if ("dataset".equals(qName)) {
                handler.startDataset();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SAXException(qName, ex);
        }
    }
    
    private byte[] preamble(String data) {
        return data == null ? null : StringUtils.parseBytes(data);
    }
    
    public void endElement (String uri, String localName, String qName)
	throws SAXException
    {
        try {
            if ("elm".equals(qName))
                handler.endElement();
            else if ("seq".equals(qName))
                handler.endSequence(-1);
            else if ("item".equals(qName))
                handler.endItem(-1);
            else if ("filemetainfo".equals(qName)) {
                handler.endFileMetaInfo();
            } else if ("dataset".equals(qName)) {
                handler.endDataset();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SAXException(qName, ex);
        }
    }

    static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");         
    private void element(String tag, String vrStr, String pos)
             throws IOException {
        handler.startElement(
                        Integer.parseInt(tag,16),
                        vr = VRs.valueOf(vrStr),
                        pos != null ? Integer.parseInt(pos) : -1);
    }
    
    private void value(String len, String val)
            throws IOException {
        int l = Integer.parseInt(len);
        byte[] b = StringUtils.parseValue(vr, val, ISO_8859_1);
        handler.value(b, 0, b.length);
    }

    private void sequence(String tag) throws IOException {
        handler.startSequence(-1);
    }
    
    private void item(String id, String pos) throws IOException {
        handler.startItem(Integer.parseInt(id),
                pos != null ? Integer.parseInt(pos) : -1, -1);
    }
    
    private void fragment(String id, String pos, String len, String val)
            throws IOException {
        int l = Integer.parseInt(len);
        byte[] b = StringUtils.parseValue(vr, val, null);
        handler.fragment(Integer.parseInt(id),
                pos != null ? Integer.parseInt(pos) : -1, b, 0, l);
    }
}
