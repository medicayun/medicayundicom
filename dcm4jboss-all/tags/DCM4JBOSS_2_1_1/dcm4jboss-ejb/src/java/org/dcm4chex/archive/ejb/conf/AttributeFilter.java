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

import org.dcm4che.dict.Tags;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1451 $ $Date: 2005-01-31 06:58:29 +0800 (周一, 31 1月 2005) $
 * @since 28.12.2003
 */
public final class AttributeFilter {

    private int[] patientFilter;
    private int[] studyFilter;
    private int[] seriesFilter;
    private int[] instanceFilter;
    private boolean excludePatientFilter;
    private boolean excludeStudyFilter;
    private boolean excludeSeriesFilter;
    private boolean excludeInstanceFilter;

    private class MyHandler extends DefaultHandler {
        private String level;
        private boolean exclude;
        private ArrayList list = new ArrayList();

        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
            if (qName.equals("attr")) {
                list.add(attributes.getValue("tag"));
            } else if (qName.equals("filter")) {
                level = attributes.getValue("level");
                exclude = "true".equalsIgnoreCase(attributes.getValue("exclude"));
            }
        }

        private int[] tags() {
            int[] array = new int[list.size()];
            for (int i = 0; i < array.length; i++) {
	            array[i] = Integer.parseInt((String) list.get(i), 16);
            }
            Arrays.sort(array);
            list.clear();
            return array;
        }

        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName.equals("filter")) {
	            if (level.equals("PATIENT")) {
	                patientFilter = tags();
	                excludePatientFilter = exclude;
	            } else if (level.equals("STUDY")) {
	                studyFilter = tags();
	                excludeStudyFilter = exclude;
	            } else if (level.equals("SERIES")) {
	                seriesFilter = tags();
	                excludeSeriesFilter = exclude;
	            } else if (level.equals("IMAGE")) {
	                instanceFilter = tags();
	                excludeInstanceFilter = exclude;
	            }
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

    public final boolean isExcludePatientFilter() {
        return excludePatientFilter;
    }

    public final boolean isExcludeStudyFilter() {
        return excludeStudyFilter;
    }

    public final boolean isExcludeSeriesFilter() {
        return excludeSeriesFilter;
    }

    public final boolean isExcludeInstanceFilter() {
        return excludeInstanceFilter;
    }
}
