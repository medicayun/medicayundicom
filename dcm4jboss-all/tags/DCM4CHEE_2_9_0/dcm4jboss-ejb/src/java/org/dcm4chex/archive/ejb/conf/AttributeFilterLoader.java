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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below. 
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
import java.util.HashMap;

import javax.xml.parsers.SAXParserFactory;

import org.dcm4che.dict.VRs;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Id: AttributeFilterLoader.java 2603 2006-07-10 17:06:43Z gunterze $
 * @since Jul 4, 2006
 */
class AttributeFilterLoader extends DefaultHandler {
    private final HashMap patient;
    private final HashMap study;
    private final HashMap series;
    private final HashMap instance;
    
    private final ArrayList tagList = new ArrayList();
    private final ArrayList vrList = new ArrayList();
    private final ArrayList noCoerceList = new ArrayList(); 
    private String cuid;
    private AttributeFilter filter;

    public AttributeFilterLoader(HashMap patient, HashMap study, HashMap series,
            HashMap instance) {
        this.patient = patient;
        this.study = study;
        this.series = series;
        this.instance = instance;
    }

    public static void loadFrom(HashMap patient, HashMap study, HashMap series,
            HashMap instance, String url) throws ConfigurationException {
        AttributeFilterLoader h = new AttributeFilterLoader(patient, study,
                series, instance);
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(url, h);
        } catch (Exception e) {
            throw new ConfigurationException(
                    "Failed to load attribute filter from " + url, e);
        }
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (qName.equals("attr")) {
            String tag = attributes.getValue("tag");
            if (tag != null) {
                tagList.add(tag);
                if ("false".equalsIgnoreCase(attributes.getValue("coerce")))
                    noCoerceList.add(tag);
            } else {
                String vr = attributes.getValue("vr");
                if (vr != null) {
                    vrList.add(vr);
                }
            }
            return;
        }
        HashMap map;
        if (qName.equals("instance")) {
            map = instance;
        } else if (qName.equals("series")) {
            map = series;
        } else if (qName.equals("study")) {
            map = study;
        } else if (qName.equals("patient")) {
            map = patient;
        } else {
            return;
        }
        cuid = attributes.getValue("cuid");
        String tsuid = attributes.getValue("tsuid");
        boolean exclude =
            "true".equalsIgnoreCase(attributes.getValue("exclude"));
        boolean exludePrivate =
            "true".equalsIgnoreCase(attributes.getValue("excludePrivate"));
        filter = new AttributeFilter(tsuid, exclude, exludePrivate);
        if (map.put(cuid, filter) != null) {
            throw new SAXException("more than one " + qName 
                    + " element with cuid=" + cuid);
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equals("attr")) {
            return;
        }
        boolean inst = qName.equals("instance");
        if (inst || qName.equals("series")
                || qName.equals("study")
                || qName.equals("patient")) {
            int[] tags = parseInts(tagList);
            int[] vrs = parseVRs(vrList);
            if (inst && filter.isExclude()) {
                AttributeFilter patFilter = AttributeFilter.getAttributeFilter(cuid, patient);
                if (patFilter == null) {
                    throw new SAXException("missing patient before instance element");                    
                }
                AttributeFilter studyFilter = AttributeFilter.getAttributeFilter(cuid, study);
                if (studyFilter == null) {
                    throw new SAXException("missing study before instance element");                    
                }
                AttributeFilter seriesFilter = AttributeFilter.getAttributeFilter(cuid, series);
                if (seriesFilter == null) {
                    throw new SAXException("missing series before instance element");                    
                }
                tags = merge(patFilter.getTags(), studyFilter.getTags(),
                        seriesFilter.getTags(), tags);
                vrs = merge(patFilter.getVRs(), studyFilter.getVRs(),
                        seriesFilter.getVRs(), vrs);
            }
            filter.setTags(tags);
            filter.setNoCoercion(parseInts(noCoerceList));
            filter.setVRs(vrs);
            tagList.clear();
            noCoerceList.clear();
            vrList.clear();
            cuid = null;
            filter = null;
        }
    }


    private int[] merge(int[] a, int[] b, int[] c, int[] d) {
        int[] dst = new int[a.length + b.length + c.length + d.length];
        System.arraycopy(a, 0, dst, 0, a.length);
        System.arraycopy(b, 0, dst, a.length, b.length);
        System.arraycopy(c, 0, dst, a.length + b.length, c.length);
        System.arraycopy(d, 0, dst, a.length + b.length + c.length, d.length);
        Arrays.sort(dst);
        return dst;
    }

    private static int[] parseInts(ArrayList list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = Integer.parseInt((String) list.get(i), 16);
        }
        Arrays.sort(array);
        return array;
    }

    private static int[] parseVRs(ArrayList list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = VRs.valueOf((String) list.get(i));
        }
        return array;
    }

    public void endDocument() throws SAXException {
        check(patient, "patient");
        check(study, "study");
        check(series, "series");
        check(instance, "instance");
    }

    private void check(HashMap map, String qname) throws SAXException {
        if (!map.containsKey(null)) {
            throw new SAXException("missing element " + qname);
        }

    }
}
