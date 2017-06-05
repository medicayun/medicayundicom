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
import java.util.Arrays;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
 * @since 28.12.2003
 */
public final class AttributeFilter {

    private int[] patientFilter;
    private int[] studyFilter;
    private int[] seriesFilter;
    private int[] instanceFilter;
    private int[] noCoercion;

    private static int[] parseInts(ArrayList list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = Integer.parseInt((String) list.get(i), 16);
        }
        Arrays.sort(array);
        return array;
    }
    
    private class MyHandler extends DefaultHandler {
        private String level;
        private ArrayList filter = new ArrayList();
        private ArrayList noCoerceList = new ArrayList();

        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
            if (qName.equals("attr")) {
            	String tag = attributes.getValue("tag");
            	String coerce = attributes.getValue("coerce");
            	filter.add(tag);
            	if ("false".equalsIgnoreCase(coerce))
            		noCoerceList.add(tag);
            } else if (qName.equals("filter")) {
                level = attributes.getValue("level");
            }
        }
        
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName.equals("filter")) {
	            if (level.equals("PATIENT")) {
	                patientFilter = AttributeFilter.parseInts(filter);
	            } else if (level.equals("STUDY")) {
	                studyFilter = AttributeFilter.parseInts(filter);
	            } else if (level.equals("SERIES")) {
	                seriesFilter = AttributeFilter.parseInts(filter);
	            } else if (level.equals("IMAGE")) {
	                instanceFilter = AttributeFilter.parseInts(filter);
	            }
	            filter.clear();
            }
        }
        
		public void endDocument() throws SAXException {
			noCoercion = AttributeFilter.parseInts(noCoerceList);
			noCoerceList.clear();
		}
    }

    public AttributeFilter(String uri) throws ConfigurationException {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(
                uri,
                new MyHandler());
        } catch (Exception e) {
            throw new ConfigurationException(
                "Failed to load attribute filter from " + uri,
                e);
        }
    }

    public final int[] getPatientFilter() {
        return patientFilter;
    }

    public final int[] getStudyFilter() {
        return studyFilter;
    }

    public final int[] getSeriesFilter() {
        return seriesFilter;
    }

    public final int[] getInstanceFilter() {
        return instanceFilter;
    }
    
    public boolean isCoercionForbidden(int tag) {
    	return Arrays.binarySearch(noCoercion, tag) >= 0;
    }
    
}
