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
package org.dcm4chee.xds.common.utils;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBResult;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4chee.xds.common.infoset.ExternalIdentifierType;
import org.dcm4chee.xds.common.infoset.ExtrinsicObjectType;
import org.dcm4chee.xds.common.infoset.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds.common.infoset.RegistryObjectType;
import org.dcm4chee.xds.common.infoset.RegistryPackageType;
import org.dcm4chee.xds.common.infoset.SlotType1;
import org.dcm4chee.xds.common.infoset.SubmitObjectsRequest;
import org.dcm4chee.xds.common.infoset.ProvideAndRegisterDocumentSetRequestType.Document;
import org.w3c.dom.Node;

public class InfoSetUtil {

    private static JAXBContext jaxbContext;

    public static Map getSlotsFromRegistryObject(RegistryObjectType ro) throws JAXBException {
        List slots = ro.getSlot();
        Map slotByName = new HashMap(slots.size());
        if (slots != null) {
            for (Iterator iter = slots.iterator() ; iter.hasNext() ;) {
                SlotType1 slot = (SlotType1)iter.next();
                String slotName = slot.getName();
                slotByName.put(slotName, slot);
            }
        }
        return slotByName;
    }

    public static String getExternalIdentifierValue(String urn, RegistryObjectType ro) {
        List<ExternalIdentifierType> l = ro.getExternalIdentifier();
        ExternalIdentifierType ei;
        for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
            ei = (ExternalIdentifierType) iter.next();
            if ( ei.getIdentificationScheme().equals(urn)) {
                return ei.getValue();
            }
        }
        return null;
    }
    public static String setExternalIdentifierValue(String urn, String value, RegistryObjectType ro) {
        List<ExternalIdentifierType> l = ro.getExternalIdentifier();
        ExternalIdentifierType ei;
        for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
            ei = (ExternalIdentifierType) iter.next();
            if ( ei.getIdentificationScheme().equals(urn)) {
                String oldValue = ei.getValue();
                ei.setValue(value);
                return oldValue;
            }
        }
        ei = new ExternalIdentifierType();
        ei.setIdentificationScheme(urn);
        ei.setValue(value);
        l.add(ei);
        return null;
    }

    public static RegistryPackageType getRegistryPackage(SubmitObjectsRequest so) {
        List list = so.getRegistryObjectList().getIdentifiable();
        Object o;
        for ( Iterator iter = list.iterator(); iter.hasNext() ; ) {
            o = ((JAXBElement) iter.next()).getValue();
            if ( o instanceof RegistryPackageType) {
                return (RegistryPackageType) o;
            }
        }
        return null;
    }

    public static Map getExtrinsicObjects(SubmitObjectsRequest so) {
        Map map = new HashMap();
        List list = so.getRegistryObjectList().getIdentifiable();
        Object o;
        ExtrinsicObjectType extrObj;
        for ( Iterator iter = list.iterator(); iter.hasNext() ; ) {
            o = ((JAXBElement) iter.next()).getValue();
            if ( o instanceof ExtrinsicObjectType) {
                extrObj = (ExtrinsicObjectType) o;
                map.put(extrObj.getId(), extrObj);

            }
        }
        return map;
    }

    public static Map getDocuments(ProvideAndRegisterDocumentSetRequestType req) {
        List docs = req.getDocument();
        Map map = new HashMap(docs.size());
        Document doc;
        for ( Iterator iter = docs.iterator() ; iter.hasNext() ;) {
            doc = (Document) iter.next();
            map.put(doc.getId(), doc);
        }
        return map;
    }

    public static Source getSourceForObject(Object o) throws JAXBException {
        return new StreamSource(new ByteArrayInputStream(marshallObject(o, false).getBytes()));
    }

    public static String marshallObject(Object o, boolean indent) throws JAXBException {
        StringWriter sw = new StringWriter();
        Marshaller m = getJAXBContext().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf(indent));
        m.marshal(o, sw);
        String s = sw.toString();
        return s;
    }
    
    public static Node getNodeForObject(Object o) throws JAXBException {
        Marshaller m = getJAXBContext().createMarshaller();
        DOMResult res = new DOMResult(); 
        m.marshal(o, res);
        return res.getNode();
    }

    public static void writeObject(Object o, OutputStream os, boolean indent) throws JAXBException {
        Marshaller m = getJAXBContext().createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf(indent));
        m.marshal(o, os);
    }

    public static JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance("org.dcm4chee.xds.common.infoset");
        }
        return jaxbContext;
    }

}
