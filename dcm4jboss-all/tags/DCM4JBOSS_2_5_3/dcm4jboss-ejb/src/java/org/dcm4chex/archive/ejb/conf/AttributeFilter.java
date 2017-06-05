/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
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
 * @version $Revision: 1530 $ $Date: 2005-02-12 01:04:56 +0800 (周六, 12 2月 2005) $
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
