/* $Id: AttributeFilter.java 913 2003-12-28 22:46:51Z gunterze $
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

import javax.xml.parsers.SAXParserFactory;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 913 $ $Date: 2003-12-29 06:46:51 +0800 (周一, 29 12月 2003) $
 * @since 28.12.2003
 */
public final class AttributeFilter {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private Dataset patientFilter = dof.newDataset();
    private Dataset studyFilter = dof.newDataset();
    private Dataset seriesFilter = dof.newDataset();
    private Dataset instanceFilter = dof.newDataset();

    private class MyHandler extends DefaultHandler {
        private Dataset filter = null;

        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
            if (qName.equals("attr")) {
                filter.putXX(Tags.valueOf(attributes.getValue("tag")));
            } else if (qName.equals("patient")) {
                filter = patientFilter;
            } else if (qName.equals("study")) {
                filter = studyFilter;
            } else if (qName.equals("series")) {
                filter = seriesFilter;
            } else if (qName.equals("instance")) {
                filter = instanceFilter;
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
    public final Dataset getPatientFilter() {
        return patientFilter;
    }

    /**
     * @return
     */
    public final Dataset getStudyFilter() {
        return studyFilter;
    }

    /**
     * @return
     */
    public final Dataset getSeriesFilter() {
        return seriesFilter;
    }

    /**
     * @return
     */
    public final Dataset getInstanceFilter() {
        return instanceFilter;
    }

}
