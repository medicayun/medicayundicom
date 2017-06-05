/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParserFactory;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.srom.Content;
import org.dcm4chex.cdw.common.ConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 4139 $ $Date: 2004-07-06 08:02:47 +0800 (周二, 06 7月 2004) $
 * @since 05.07.2004
 */
class DirRecordFactory {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private HashMap filterForType = new HashMap();

    private class MyHandler extends DefaultHandler {

        private static final String TYPE = "type";

        private static final String RECORD = "record";

        private static final String TAG = "tag";

        private static final String ATTR = "attr";

        private String type;

        private ArrayList attrs = new ArrayList();

        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            if (qName.equals(ATTR)) {
                attrs.add(attributes.getValue(TAG));
            } else if (qName.equals(RECORD)) {
                type = attributes.getValue(TYPE);
            }
        }

        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName.equals(RECORD)) {
                int[] filter = new int[attrs.size()];
                for (int i = 0; i < filter.length; i++)
                    filter[i] = Tags.valueOf((String) attrs.get(i));
                filterForType.put(type, filter);
                attrs.clear();
            }
        }

    }

    public DirRecordFactory(String uri) throws ConfigurationException {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(uri,
                    new MyHandler());
        } catch (Exception e) {
            throw new ConfigurationException(
                    "Failed to load record filter from " + uri, e);
        }
    }

    public Dataset makeRecord(String type, Dataset obj) {
        int[] filter = (int[]) filterForType.get(type);
        if (filter == null) throw new IllegalArgumentException("type:" + type);
        Dataset keys = dof.newDataset();
        keys.putAll(obj.subSet(filter));
        DcmElement srcSq = obj.get(Tags.ContentSeq);
        if (srcSq != null) {
            DcmElement dstSq = keys.putSQ(Tags.ContentSeq);
            for (int i = 0, n = srcSq.vm(); i < n; ++i) {
                Dataset item = srcSq.getItem(i);
                if (Content.RelationType.HAS_CONCEPT_MOD.equals(item
                        .getString(Tags.RelationshipType))) {
                    dstSq.addItem(item);
                }
            }
            if (dstSq.isEmpty()) keys.remove(Tags.ContentSeq);
        }
        return keys;
    }
}
