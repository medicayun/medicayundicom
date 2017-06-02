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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.dcm4chee.xds.infoset.v30.ClassificationType;
import org.dcm4chee.xds.infoset.v30.ExternalIdentifierType;
import org.dcm4chee.xds.infoset.v30.ExtrinsicObjectType;
import org.dcm4chee.xds.infoset.v30.ObjectFactory;
import org.dcm4chee.xds.infoset.v30.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds.infoset.v30.RegistryError;
import org.dcm4chee.xds.infoset.v30.RegistryErrorList;
import org.dcm4chee.xds.infoset.v30.RegistryObjectType;
import org.dcm4chee.xds.infoset.v30.RegistryPackageType;
import org.dcm4chee.xds.infoset.v30.RegistryResponseType;
import org.dcm4chee.xds.infoset.v30.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds.infoset.v30.SlotType1;
import org.dcm4chee.xds.infoset.v30.SubmitObjectsRequest;
import org.dcm4chee.xds.infoset.v30.ProvideAndRegisterDocumentSetRequestType.Document;
import org.dcm4chee.xds.infoset.v30.RetrieveDocumentSetResponseType.DocumentResponse;
import org.w3c.dom.Node;

public class InfoSetUtil {

    private static JAXBContext jaxbContext;
    private static ObjectFactory objFac = new ObjectFactory();

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

    public static RegistryPackageType getRegistryPackage(SubmitObjectsRequest so, String classificationUUID) {
        List list = so.getRegistryObjectList().getIdentifiable();
        String id = null;
        Object o;
        if ( classificationUUID != null ) {
            ClassificationType ct;
            for ( Iterator iter = list.iterator(); iter.hasNext() ; ) {
                o = ((JAXBElement) iter.next()).getValue();
                if ( o instanceof ClassificationType) {
                    ct = (ClassificationType) o;
                    if ( classificationUUID.equals( ct.getClassificationNode())) {
                        id = ct.getClassifiedObject();
                        break;
                    }
                }
            }
        }
        RegistryPackageType rp;
        for ( Iterator iter = list.iterator(); iter.hasNext() ; ) {
            o = ((JAXBElement) iter.next()).getValue();
            if ( o instanceof RegistryPackageType) {
                rp = (RegistryPackageType) o;
                if ( id == null || id.equals( rp.getId())) {
                    return rp;
                }
            }
        }
        return null;
    }

    public static Map<String,ExtrinsicObjectType> getExtrinsicObjects(SubmitObjectsRequest so) {
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

    public static Map<String,Document> getDocuments(ProvideAndRegisterDocumentSetRequestType req) {
        List docs = req.getDocument();
        Map<String,Document> map = new HashMap<String,Document>(docs.size());
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

    public static void writeObject(ProvideAndRegisterDocumentSetRequestType req, OutputStream os, boolean indent) throws JAXBException {
        Marshaller m = getJAXBContext().createMarshaller();
        List saveList = new ArrayList(); 
        saveList.addAll( (req).getDocument() );
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf(indent));
        m.marshal(req, os);
        req.getDocument().addAll(saveList);
    }

    public static JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance("org.dcm4chee.xds.infoset.v30");
        }
        return jaxbContext;
    }

    public static String getLogMessage(ProvideAndRegisterDocumentSetRequestType req) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("ProvideAndRegisterDocumentSetRequest:\n");
            List<Document> docList = new ArrayList<Document>();
            docList.addAll(req.getDocument());
            req.getDocument().clear();
            sb.append(InfoSetUtil.marshallObject( objFac.createProvideAndRegisterDocumentSetRequest(req), true));
            sb.append("Documents:").append(docList.size()).append(" DocumentElements in request. (Hidden in above xml representation!)");
            Document doc;
            for ( Iterator<Document> iter = docList.iterator() ; iter.hasNext() ; ) {
                doc = iter.next();
                sb.append("Document:").append(doc.getId()).append(" contentType:").append(doc.getValue().getContentType() )
                        .append(" size:").append(doc.getValue().getInputStream().available());
            }
            req.getDocument().addAll(docList);
            return sb.toString();
        } catch (Exception ignore) {
            return "Failed to log ProvideAndRegisterDocumentSetRequest! Ignored";
        }
    }
    public static String getLogMessage(RetrieveDocumentSetResponseType rsp) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("RetrieveDocumentSetResponse:");
            RegistryResponseType rRsp = rsp.getRegistryResponse();
            sb.append("\nRequestId:").append(rRsp.getRequestId());
            sb.append("\n   Status:").append(rRsp.getStatus());
            appendErrorListLog(rRsp.getRegistryErrorList(), sb);
            sb.append("\n   Status:").append(rRsp.getRegistryErrorList());
            List<DocumentResponse> docRspList = rsp.getDocumentResponse();
            int count = 1;
            for ( DocumentResponse docRspItem : docRspList) {
                sb.append("\nDocument #").append(count++);
                sb.append("\n    DocumentUniqueId:").append(docRspItem.getDocumentUniqueId());
                sb.append("\n            MimeType:").append(docRspItem.getMimeType());
                sb.append("\n  RepositoryUniqueId:").append(docRspItem.getRepositoryUniqueId());
                sb.append("\n     HomeCommunityId:").append(docRspItem.getHomeCommunityId());
                sb.append("\n       Document size:").append(docRspItem.getDocument().getInputStream().available() );
                sb.append("\n------------------------------------------------------");
            }
            sb.append("\n======================================================");
            return sb.toString();
        } catch (Exception ignore) {
            return "Failed to log RetrieveDocumentSetResponse! Ignored";
        }
    }

    
    private static void appendErrorListLog(RegistryErrorList errList,
            StringBuffer sb) {
        if ( errList != null ) {
            List<RegistryError> l = errList.getRegistryError();
            sb.append("\nErrorlist:");
            for ( RegistryError err : l ) {
                sb.append("\n  ErrorCode:").append(err.getErrorCode())
                    .append(" (").append(err.getCodeContext()).append(')');
                sb.append("\n  Msg:").append(err.getValue());
                sb.append("\n  Severity:").append(err.getSeverity());
                sb.append("\n  Location:").append(err.getLocation());
                sb.append("\n------------------------------------------------------");
            }
        }
    }

}
