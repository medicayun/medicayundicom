/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

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
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
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
