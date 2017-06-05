/* $Id: AttributeFilter.java 1066 2004-03-22 23:26:15Z gunterze $
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
package org.dcm4chex.archive.ejb.conf;

import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.SAXParserFactory;

import org.dcm4che.dict.Tags;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1066 $ $Date: 2004-03-23 07:26:15 +0800 (周二, 23 3月 2004) $
 * @since 28.12.2003
 */
public final class AttributeFilter {

    private int[] patientFilter;
    private int[] studyFilter;
    private int[] seriesFilter;
    private int[] instanceFilter;

    private class MyHandler extends DefaultHandler {
        private ArrayList list = new ArrayList();

        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
            if (qName.equals("attr")) {
                list.add(attributes.getValue("tag"));
            }
        }

        private int[] tags() {
            int[] array = new int[list.size()];
            for (int i = 0; i < array.length; i++) {
	            array[i] = Tags.valueOf((String) list.get(i));
            }
            Arrays.sort(array);
            list.clear();
            return array;
        }

        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName.equals("patient")) {
                patientFilter = tags();
            } else if (qName.equals("study")) {
                studyFilter = tags();
            } else if (qName.equals("series")) {
                seriesFilter = tags();
            } else if (qName.equals("instance")) {
                instanceFilter = tags();
            }
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

    /**
     * @return
     */
    public final int[] getPatientFilter() {
        return patientFilter;
    }

    /**
     * @return
     */
    public final int[] getStudyFilter() {
        return studyFilter;
    }

    /**
     * @return
     */
    public final int[] getSeriesFilter() {
        return seriesFilter;
    }

    /**
     * @return
     */
    public final int[] getInstanceFilter() {
        return instanceFilter;
    }

}
