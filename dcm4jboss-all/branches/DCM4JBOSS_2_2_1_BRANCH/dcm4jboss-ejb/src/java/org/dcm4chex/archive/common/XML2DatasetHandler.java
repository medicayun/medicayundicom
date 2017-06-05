/* $Id: XML2DatasetHandler.java 1489 2005-02-04 17:46:16Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.common;

import java.util.LinkedList;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1489 $ $Date: 2005-02-05 01:46:16 +0800 (周六, 05 2月 2005) $
 * @since 11.02.2004
 */
class XML2DatasetHandler extends DefaultHandler {
    private final LinkedList dsStack = new LinkedList();
    private final LinkedList sqStack = new LinkedList();  
    private final StringBuffer value = new StringBuffer();
    private int attrTag;
    
    /**
     * @param ds
     */
    public XML2DatasetHandler(Dataset ds) {
        dsStack.addLast(ds);
    }
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {
        value.append(ch, start, length);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
        throws SAXException {
        if ("attr".equals(qName)) {
            getCurDataset().putXX(attrTag, value.toString());
        } else if ("seq".equals(qName)) {
            sqStack.removeLast();
        } else if ("item".equals(qName)) {
            dsStack.removeLast();
        }
    }
    
    private Dataset getCurDataset() {
        return (Dataset) dsStack.getLast();
    }

    private DcmElement getCurSeq() {
        return (DcmElement) sqStack.getLast();
    }
    
    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(
        String uri,
        String localName,
        String qName,
        Attributes attr)
        throws SAXException {
        value.setLength(0);
        if ("attr".equals(qName)) {
            attrTag = Tags.valueOf(attr.getValue("tag"));
        } else if ("seq".equals(qName)) {
            sqStack.addLast(getCurDataset().putSQ(Tags.valueOf(attr.getValue("tag"))));
        } else if ("item".equals(qName)) {
            dsStack.addLast(getCurSeq().addNewItem());
        }
    }

}
