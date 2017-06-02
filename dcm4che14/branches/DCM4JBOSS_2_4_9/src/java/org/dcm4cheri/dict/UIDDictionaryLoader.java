/*$Id: UIDDictionaryLoader.java 3656 2003-06-09 17:17:51Z gunterze $*/
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

package org.dcm4cheri.dict;

import org.dcm4che.dict.UIDDictionary;

import java.io.File;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class UIDDictionaryLoader extends org.xml.sax.helpers.DefaultHandler {

    private final UIDDictionaryImpl dict;
    private final SAXParser parser;
    
    /** Creates a new instance of UIDDictionaryLoader */
    public UIDDictionaryLoader(UIDDictionaryImpl dict) {
        this.dict = dict;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception ex) {
            throw new ConfigurationError("Could not create SAX Parser", ex);
        }
    }    

    public void startElement (String uri, String localName, String qName,
            Attributes attr) throws SAXException {
        if ("uid".equals(qName)) {
            dict.add(new UIDDictionary.Entry(
                    attr.getValue("value"), attr.getValue("name")));
        }
    }
    
    public void parse(InputSource xmlSource) throws SAXException, IOException {
        parser.parse(xmlSource, this);
    }
    
    public void parse(File xmlFile) throws SAXException, IOException {
        parser.parse(xmlFile, this);
    }
    
    static class ConfigurationError extends Error {
        ConfigurationError(String msg, Exception x) {
            super(msg,x);
        }
    }
}
