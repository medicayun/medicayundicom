/*
 * $Id: AttributeCoercions.java 1559 2005-02-25 15:20:31Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 * 
 * This file is part of dcm4che.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.conf;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.SAXParserFactory;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.PrivateTags;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1559 $ $Date: 2005-02-25 23:20:31 +0800 (周五, 25 2月 2005) $
 * @since 28.12.2003
 */
public class AttributeCoercions {

    private ArrayList list = new ArrayList();
    private static class Entry {
        final CoercionCondition condition;
        final AttributeCoercion coercion;
        Entry(CoercionCondition condition, AttributeCoercion coercion) {
            this.condition = condition;
            this.coercion = coercion;
        }
    }

    private class MyHandler extends DefaultHandler {
        private CoercionCondition cc = new CoercionCondition();
        private AttributeCoercion ac;
		private CoercionLUT lut;
        private Hashtable table = new Hashtable() ;
        private int tag;
        private String name;
        private StringBuffer sb = new StringBuffer();
        private boolean takeCharacters = false;
        private Hashtable luts = new Hashtable();

        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
            if (qName.equals("attr")) {
                tag = Tags.valueOf(attributes.getValue("tag"));
                takeCharacters = true;
			} else if (qName.equals("param")) {
				name = attributes.getValue("name");
				takeCharacters = true;
            } else if (qName.equals("if")) {
                String clazz = attributes.getValue("class");
                cc =
                    (clazz == null)
                        ? new CoercionCondition()
                        : (CoercionCondition) newInstance(clazz);
				table = cc.params;
            } else if (qName.equals("then")) {
                String clazz = attributes.getValue("class");
                ac =
                    (clazz == null)
                        ? new AttributeCoercion()
                        : (AttributeCoercion) newInstance(clazz);
				table = ac.params;
            } else if (qName.equals("lut")) {
				String clazz = attributes.getValue("class");
				lut =
					(clazz == null)
						? new CoercionLUT()
						: (CoercionLUT) newInstance(clazz);
				table = lut.params;
                luts.put(attributes.getValue("name"), lut);
            }
        }

        public void characters(char[] ch, int start, int length)
            throws SAXException {
            if (takeCharacters) {
                sb.append(ch, start, length);
            }
        }

        public void endElement(String uri, String localName, String qName)
            throws SAXException {
            if (qName.equals("attr")) {
                String val = sb.toString();
                if (ac == null) {
                    cc.condition.putXX(tag, StringUtils.split(val, '\\'));
                } else {
                    ac.add(tag, val, luts);
                }
            } else if (qName.equals("param")) {
                table.put(name, sb.toString());
            } else if (qName.equals("then")) {
                list.add(new Entry(cc, ac));
                ac = null;
            }
            sb.setLength(0);
            takeCharacters = false;
        }

    }

    private static Object newInstance(String clazz) throws SAXException {
        try {
            return Class.forName(clazz).newInstance();
        } catch (Exception e) {
            throw new SAXException(clazz, e);
        }
    }

    public AttributeCoercions(String uri) throws ConfigurationException {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(
                uri,
                new MyHandler());
        } catch (Exception e) {
            throw new ConfigurationException(
                "Failed to load attribute coercions from " + uri,
                e);
        }
    }

    public void coerce(Dataset ds, Dataset coercedElements) {
    	ds.setPrivateCreatorID(PrivateTags.CreatorID);
        final String callingAET = ds.getString(PrivateTags.CallingAET);
		final String calledAET = ds.getString(PrivateTags.CalledAET);
        for (int i = 0, n = list.size(); i < n; ++i) {
            Entry entry = (Entry) list.get(i);
			if (entry.condition.match(callingAET, calledAET, ds)) {
                entry.coercion.coerce(ds, coercedElements);
            }
        }
    }

}
