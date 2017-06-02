/*$Id: TagDictionaryLoader.java 3656 2003-06-09 17:17:51Z gunterze $*/
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

import org.dcm4che.dict.TagDictionary;

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
final class TagDictionaryLoader extends org.xml.sax.helpers.DefaultHandler {

    private final TagDictionaryImpl dict;
    private final SAXParser parser;
    
    /** Creates a new instance of TagDictionaryLoader */
    public TagDictionaryLoader(TagDictionaryImpl dict) {
        this.dict = dict;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception ex) {
            throw new ConfigurationError("Could not create SAX Parser", ex);
        }
    }    

    public void startElement (String uri, String localName, String qName,
            Attributes attr) throws SAXException {
        if ("element".equals(qName)) {
            String str = attr.getValue("tag");
            if (str == null)
                throw new SAXException("Missing tag attribute");
            char[] tag = str.toCharArray();
            if (tag.length != 11
                    || tag[0] != '(' || tag[5] != ',' || tag[10] != ')')
                throw new SAXException("Illegal tag value: " + str);
            try {
                dict.add(new TagDictionary.Entry(toTag(tag),
                        str.indexOf('x') == -1 ? -1 : toMask(tag),
                        attr.getValue("vr"), attr.getValue("vm"),
                        attr.getValue("name")));
            } catch (NumberFormatException nfe) {
                throw new SAXException("Illegal tag value: " + str, nfe);
            }
        }
    }
    
    private int toTag(char[] s) {
        StringBuffer sb = new StringBuffer(8);
        sb.append(toTag(s[1])).append(toTag(s[2])).append(toTag(s[3]))
          .append(toTag(s[4])).append(toTag(s[6])).append(toTag(s[7]))
          .append(toTag(s[8])).append(toTag(s[9]));
        return (int)Long.parseLong(sb.toString(),16);
    }
    
    private char toTag(char ch) {
        return ch == 'x' ? '0' : ch;
    }
    
    private int toMask(char[] s) {
        StringBuffer sb = new StringBuffer(8);
        sb.append(toMask(s[1])).append(toMask(s[2])).append(toMask(s[3]))
          .append(toMask(s[4])).append(toMask(s[6])).append(toMask(s[7]))
          .append(toMask(s[8])).append(toMask(s[9]));
        return (int)Long.parseLong(sb.toString(),16);
    }
    
    private char toMask(char ch) {
        return ch == 'x' ? '0' : 'f';
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
