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
package org.dcm4chex.xds.mbean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.util.UIDUtils;
import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.audit.HttpUserInfo;
import org.dcm4chee.xds.common.audit.XDSExportMessage;
import org.dcm4chee.xds.common.audit.XDSImportMessage;
import org.dcm4chee.xds.common.delegate.XdsHttpCfgDelegate;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.store.DocumentStoreDelegate;
import org.dcm4chee.xds.common.store.XDSDocument;
import org.dcm4chee.xds.common.store.XDSDocumentWriterFactory;
import org.dcm4chex.xds.UUID;
import org.dcm4chex.xds.XDSDocumentMetadata;
import org.dcm4chex.xds.common.SoapBodyProvider;
import org.dcm4chex.xds.common.XDSResponseObject;
import org.dcm4chex.xds.query.SQLQueryObject;
import org.dcm4chex.xds.query.XDSQueryObject;
import org.dcm4chex.xds.query.XDSQueryObjectFatory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import com.sun.xml.messaging.saaj.util.JAXMStreamSource;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 5476 $ $Date: 2007-11-21 09:45:36 +0100 (Mi, 21 Nov 2007) $
 * @since Mar 08, 2006
 */
public class XDSService extends ServiceMBeanSupport {

    private static final String OBJECT_REF = "ObjectRef";
    private static final String EXTERNAL_IDENTIFIER = "ExternalIdentifier";
    private static final String EXTRINSIC_OBJECT = "ExtrinsicObject";
    private static final String REGISTRY_PACKAGE = "RegistryPackage";
    private static final String LEAF_REGISTRY_OBJECT_LIST = "LeafRegistryObjectList";
    private static final String SUBMIT_OBJECTS_REQUEST = "SubmitObjectsRequest";
    private static final String NONE = "NONE";

    private static Logger log = Logger.getLogger(XDSService.class.getName());

    private DocumentBuilderFactory dbFactory;

//  http attributes to document repository actor (synchron) 
    private String xdsRegistryURI;
    private String fetchNewPatIDURL;

    private DocumentStoreDelegate docStoreDelegate = new DocumentStoreDelegate();
    private XdsHttpCfgDelegate httpCfgDelegate = new XdsHttpCfgDelegate();

    private String retrieveURI;

    private List filteredSlots;

    private boolean useLongURI = false;
    private boolean logSOAPMessage = true;
    private boolean logReceivedSOAPMessage = false;
    private boolean indentSOAPLog = true;

    private String xdsQueryURI;
    private boolean forceSQLQuery = false;
    private boolean forceSourceAsRequestor;

    private String rimPrefix = null;
    public XDSService() {
        dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
    }
//  http
    /**
     * @return Returns the docRepositoryURI.
     */
    public String getXDSRegistryURI() {
        return this.xdsRegistryURI;
    }
    /**
     * @param docRepositoryURI The docRepositoryURI to set.
     */
    public void setXDSRegistryURI(String xdsRegistryURI) {
        this.xdsRegistryURI = xdsRegistryURI.trim();
    }

    /**
     * @return the xdsQueryURI
     */
    public String getXDSQueryURI() {
        return xdsQueryURI;
    }
    /**
     * @param xdsQueryURI the xdsQueryURI to set
     */
    public void setXDSQueryURI(String xdsQueryURI) {
        this.xdsQueryURI = xdsQueryURI.trim();
    }
    /**
     * @return the forceSQLQuery
     */
    public boolean isForceSQLQuery() {
        return forceSQLQuery;
    }
    /**
     * @param forceSQLQuery the forceSQLQuery to set
     */
    public void setForceSQLQuery(boolean forceSQLQuery) {
        this.forceSQLQuery = forceSQLQuery;
    }

    public ObjectName getXDSStoreService() {
        return docStoreDelegate.getDocumentStoreService();
    }

    public void setXDSStoreService(ObjectName name) {
        docStoreDelegate.setDocumentStoreService(name);
    }

    /**
     * @return
     */
    public String getRetrieveURI() {
        return retrieveURI;
    }
    /**
     * @param retrieveURI The retrieveURI to set.
     */
    public void setRetrieveURI(String retrieveURI) {
        this.retrieveURI = retrieveURI.trim();
    }

    /**
     * @return Returns the useLongURI.
     */
    public boolean isUseLongURI() {
        return useLongURI;
    }
    /**
     * @param useLongURI The useLongURI to set.
     */
    public void setUseLongURI(boolean useLongURI) {
        this.useLongURI = useLongURI;
    }
    /**
     * @return Returns the logSOAPMessage.
     */
    public boolean isLogSOAPMessage() {
        return logSOAPMessage;
    }
    /**
     * @param logSOAPMessage The logSOAPMessage to set.
     */
    public void setLogSOAPMessage(boolean logSOAPMessage) {
        this.logSOAPMessage = logSOAPMessage;
    }

    public boolean isLogReceivedSOAPMessage() {
        return logReceivedSOAPMessage;
    }
    public void setLogReceivedSOAPMessage(boolean logReceivedSOAPMessage) {
        this.logReceivedSOAPMessage = logReceivedSOAPMessage;
    }
    public boolean isIndentSOAPLog() {
        return indentSOAPLog;
    }
    public void setIndentSOAPLog(boolean indentSOAPLog) {
        this.indentSOAPLog = indentSOAPLog;
    }
    
    public boolean isForceSourceAsRequestor() {
        return forceSourceAsRequestor;
    }
    public void setForceSourceAsRequestor(boolean forceSourceAsRequestor) {
        this.forceSourceAsRequestor = forceSourceAsRequestor;
    }
    
    /**
     * @return the filterSlots
     */
    public String getFilteredSlots() {
        return getListString(filteredSlots);
    }
    /**
     * @param filterSlots the filterSlots to set
     */
    public void setFilteredSlots(String filterSlots) {
        this.filteredSlots = setListString(filterSlots.trim());
    }

    public String getRimPrefix() {
        return rimPrefix == null ? NONE : rimPrefix;
    }
    public void setRimPrefix(String rimPrefix) {
        this.rimPrefix = NONE.equals(rimPrefix) ? null : rimPrefix.trim();
    }
    /**
     * @return Returns the fetchNewPatIDURL.
     */
    public String getFetchNewPatIDURL() {
        return fetchNewPatIDURL;
    }
    /**
     * @param fetchNewPatIDURL The fetchNewPatIDURL to set.
     */
    public void setFetchNewPatIDURL(String fetchNewPatIDURL) {
        this.fetchNewPatIDURL = fetchNewPatIDURL.trim();
    }

    private List setListString(String s) {
        List l = new ArrayList();
        if ( NONE.equals(s) ) return l;
        StringTokenizer st = new StringTokenizer( s, ";\n\r");
        while ( st.hasMoreTokens() ) {
            l.add(st.nextToken());
        }
        return l;
    }

    private String getListString(List l) {
        if ( l == null || l.isEmpty() ) return NONE;
        StringBuffer sb = new StringBuffer();
        for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
            sb.append(iter.next()).append( System.getProperty("line.separator", "\n"));
        }
        return sb.toString();
    }

    public XDSResponseObject exportDocument( SOAPMessage message ) {
        List<XDSDocument> storedDocuments = new ArrayList<XDSDocument>();
        if ( logReceivedSOAPMessage ) {
            log.info("Received SOAP message:");
            this.dumpSOAPMessage(message);
        }
        String submissionUID = null;
        XDSDocumentMetadata metadata = null;
        try {
            XDSDocument storedDoc;
            Map attachments = getAttachments(message);
            Node leafRegistryObjectList;
            SOAPBody body = message.getSOAPBody();
            log.debug("SOAPBody:"+body );
            leafRegistryObjectList = getLeafRegistryObjectList( getSubmitObjectsRequest(body) );
            List extrinsicObjects = getExtrinsicObjects(leafRegistryObjectList);
            if(extrinsicObjects.size() < 1 ) {
                if ( attachments.isEmpty() ) {
                    log.debug("No ExtrinsicObject found! But we have nothing to store (no Attachment) -> forward message to registry!");
                    return new SOAPMessageResponse( sendSOAP(message, getXDSRegistryURI()));
                }
                log.error("No XDSDocumentEntry metadata (ExtrinsicObject) found.");
                if ( !logReceivedSOAPMessage ) {
                    log.info("------------------------------------------------------------------------------------- ####");
                    log.info("DEBUGGING received SOAP message with missing ExtrinsicObject?:");
                    dumpSOAPMessage(message);
                    log.info("------------------------------------------------------------------------------------- ####");
                }
                return new XDSRegistryResponse( false, "XDSMissingDocumentMetadata", "XDSMissingDocumentMetadata",null);
            }
            if ( attachments.isEmpty() ) {
                log.error("No Attachments found -> Missing document!");
                return new XDSRegistryResponse( false, "XDSMissingDocument", "XDSMissingDocument",null);
            }
            Element registryPackage = this.getRegistryPackage(leafRegistryObjectList);
            if ( registryPackage == null ) {
                log.error("No RegistryPackage found!");
                return new XDSRegistryResponse( false, "XDSRepositoryError", "RegistryPackage missing",null);
            }
            submissionUID = getValueOfExternalIdentifier(registryPackage, UUID.XDSSubmissionSet_uniqueId);
            log.info("SubmissionSet.uniqueID:"+submissionUID);
            Element el;
            AttachmentPart part;
            String mime, contentType;
            boolean reassignDocumentUID = "true".equals(getSystemProperty("reassignDocumentUID") );
            String testPatient = getSystemProperty("testPatient");
            for(Iterator iter = extrinsicObjects.iterator() ; iter.hasNext() ; ) {
                el = (Element)iter.next();
                metadata = new XDSDocumentMetadata(el);
                part = (AttachmentPart)attachments.get(metadata.getContentID());
                mime = metadata.getMimeType();
                contentType = part.getContentType();
                if ( mime == null || contentType == null || !metadata.getMimeType().equalsIgnoreCase( part.getContentType() ) ) {
                    log.error("Mimetype mismatch detected! metadata:"+mime+ "attachment:"+contentType);
                    return new XDSRegistryResponse( false, "XDSRepositoryError", "Mimetype mismatch detected! metadata:"+mime+" attachment:"+contentType,null);
                }
                if (reassignDocumentUID) {
                    String newUID = UIDUtils.createUID();
                    log.warn("Reassign documenUID "+metadata.getUniqueID()+" to "+newUID );
                    metadata.setUniqueID( newUID );
                }
                filterMetadata(metadata, testPatient);
                storedDoc = saveDocumentEntry(metadata, part);
                metadata.setURI(getDocumentURI(metadata.getUniqueID(), metadata.getMimeType()), rimPrefix);
                if ( storedDoc != null) {
                    storedDocuments.add(storedDoc);
                }
                if ( testPatient!= null) {
                    log.warn("Change patientID in metadata (XDSSubmissionSet: schemeOID:"+UUID.XDSSubmissionSet_patientId+") to testPatient! new patientID:"+testPatient);
                    this.updateExternalIdentifier(el,UUID.XDSSubmissionSet_patientId,testPatient);
                }
            }
            log.info(storedDocuments.size()+" Documents saved!");
            this.logImport(submissionUID, metadata.getPatientID(), true);
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage msg = messageFactory.createMessage();
            msg.getSOAPPart().setContent(message.getSOAPPart().getContent());
            SOAPMessage response;
            try {
                response = sendSOAP(msg, getXDSRegistryURI());
            } catch ( Exception x) {
                return new XDSRegistryResponse( false, XDSConstants.XDS_ERR_REG_NOT_AVAIL, "Document Registry not available: "+xdsRegistryURI,x);
            }
            boolean success = checkResponse( response, "RegistryResponse" );
            logExport(submissionUID, metadata.getPatientID(), success);
            if ( ! success ) {
                deleteDocuments(storedDocuments);
                log.error("Export document(s) failed! see prior messages for reason. SubmissionSet uid:"+submissionUID);
                return new SOAPMessageResponse(response);
            }
            log.info("Register document finished.");
            commitDocuments(storedDocuments);
            return new SOAPMessageResponse(response);
        } catch ( Throwable x ) {
            log.error("Export document(s) failed! SubmissionSet uid:"+submissionUID,x);
            logExport(submissionUID != null ? submissionUID : "SubmissionSet UID missing", 
                    metadata != null ? metadata.getPatientID() : "unknown", 
                    false);
            deleteDocuments(storedDocuments);
            return new XDSRegistryResponse( false, "XDSRepositoryError", "Export document(s) failed! SubmissionSet uid:"+submissionUID+" Reason:"+x.getMessage(),x);
        }

    }
    private void deleteDocuments(List storedDocuments) {
        docStoreDelegate.rollbackDocuments(storedDocuments);
    }
    private void commitDocuments(List storedDocuments) {
        docStoreDelegate.commitDocuments(storedDocuments);
    }

    private String getSystemProperty(String name) {
        return System.getProperty(getClass().getName()+"."+name);
    }

    private Node getSubmitObjectsRequest(SOAPBody body) {
        return getChildNode( body, XDSDocumentMetadata.NS_URN_REGISTRY_2_1, SUBMIT_OBJECTS_REQUEST);
    }
    private Node getLeafRegistryObjectList(Node submObjReq) {
        return getChildNode(submObjReq, XDSDocumentMetadata.NS_URN_RIM_2_1, LEAF_REGISTRY_OBJECT_LIST);
    }

    private List getExtrinsicObjects(Node leafRegistryObjectList) {
        return getChildNodes(leafRegistryObjectList, EXTRINSIC_OBJECT);
    }
    private Element getRegistryPackage(Node leafRegistryObjectList) {
        List l = getChildNodes(leafRegistryObjectList, REGISTRY_PACKAGE);
        if ( l.size() < 1 )
            return null;
        if ( l.size() > 1 )
            log.warn("This request contains more than one RegistryPackage! Only the first will be used!");
        return (Element) l.get(0);
    }

    private List getExternalIdentifiers(Node extrinsicObject) {
        return getChildNodes(extrinsicObject, EXTERNAL_IDENTIFIER);
    }

    private Node getChildNode(Node node, String namespaceURI, String localName) {
        if ( node == null ) return null;
        NodeList nl =  node.getChildNodes();
        Node child;
        for ( int i = 0, len = nl.getLength() ; i < len ; i++ ) {
            child = nl.item(i);
            if ( child.getNodeType() == Node.ELEMENT_NODE ) {
                if ( (namespaceURI == null || namespaceURI.equals( child.getNamespaceURI() ) ) &&
                        localName.equals( child.getLocalName() ) ) {
                    return child;
                }
            }    
        }
        return null;
    }

    private List getChildNodes(Node node, String localName) {
        ArrayList l = new ArrayList();
        if ( node == null ) return l;
        NodeList nl =  node.getChildNodes();
        for ( int i = 0 ; i < nl.getLength() ; i++ ) {
            if ( (localName == null || localName.equals(nl.item(i).getLocalName()) ) ) {
                l.add(nl.item(i));
            }
        }
        return l;
    }
    private String getValueOfExternalIdentifier(Element node, String scheme) { 
        List l = getChildNodes(node, EXTERNAL_IDENTIFIER);
        NamedNodeMap attributes;
        for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
            attributes = ((Node) iter.next() ).getAttributes();
            if ( attributes.getNamedItem("identificationScheme").getNodeValue().equals(scheme) ) {
                return attributes.getNamedItem("value").getNodeValue();
            }
        }
        return null;
    }

    public List xdsQuery(XDSQueryObject query) throws SOAPException {
        SOAPMessage response = sendSOAP(getSOAPMessage(query), this.xdsQueryURI);
        if ( !checkResponse( response, query.getResponseTag() ) ) return null;
        return getRegistryObjects(response, query.getResponseTag() );
    }

    private SOAPMessage getSOAPMessage(SoapBodyProvider docProvider) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage msg = messageFactory.createMessage();
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        SOAPBody soapBody = envelope.getBody();
        soapBody.addDocument(docProvider.getDocument());
        return msg;
    }

    public List sqlQuery(String sql) throws SOAPException {
        log.info("make SQL Query:"+sql);
        SQLQueryObject qry = new SQLQueryObject(sql,SQLQueryObject.RETURN_TYPE_LEAF,true);
        return xdsQuery(new SQLQueryObject(sql));
    }

    public List findDocuments(String patId) throws SOAPException {
        if ( patId == null || patId.trim().length() < 1 ) patId = this.getSystemProperty("testPatient");
        XDSQueryObject query = XDSQueryObjectFatory.getInstance( forceSQLQuery ).newFindDocumentQuery(patId, null);
        log.info("findDocument Stored Query:"+query);
        return xdsQuery(query);
    }

    public List getDocuments(String uuids) throws SOAPException {
        StringTokenizer st = new StringTokenizer(uuids,"|");
        String[] sa = new String[st.countTokens()];
        for ( int i = 0 ; i < sa.length ; i++) {
            sa[i] = st.nextToken();
        }
        return getDocuments(sa);
    }
    public List getDocuments(String[] uuids) throws SOAPException {
        XDSQueryObject query = XDSQueryObjectFatory.getInstance( forceSQLQuery ).newGetDocumentQuery(uuids);
        log.info("getDocument Stored Query:"+query);
        return xdsQuery(query);
    }

    /**
     * @param metadata
     */
    private void filterMetadata(XDSDocumentMetadata metadata, String testPatient) {
        if ( ! filteredSlots.isEmpty() ) {
            for ( Iterator iter = filteredSlots.iterator() ; iter.hasNext() ; ) {
                metadata.removeSlot((String) iter.next());
            }
        }
        if ( testPatient!= null) {
            log.warn("Change patientID in metadata (XDSDocumentEntry: schemeOID:"+UUID.XDSDocumentEntry_patientId+") to testPatient! new patientID:"+testPatient);
            this.updateExternalIdentifier((Element)metadata.getMetadata(),UUID.XDSDocumentEntry_patientId,testPatient);
        }

    }

    private void updateExternalIdentifier(Element extrinsicObject, String scheme, String value) { 
        //NodeList nl = element.getElementsByTagNameNS(NS_URN_RIM_2_1,EXTERNAL_IDENTIFIER);
        List l = getChildNodes(extrinsicObject, EXTERNAL_IDENTIFIER);
        NamedNodeMap attributes;
        for ( Iterator iter = l.iterator() ; iter.hasNext() ; ) {
            attributes = ((Node) iter.next() ).getAttributes();
            if ( attributes.getNamedItem("identificationScheme").getNodeValue().equals(scheme) ) {
                attributes.getNamedItem("value").setNodeValue(value);
            }
        }
    }
    /**
     * @param uuid
     * @param contentType
     * @return
     */
    private String[] getDocumentURI(String uuid, String contentType) {
        if ( useLongURI ) {
            String uidPart = "?requestType=DOCUMENT&documentUID="+uuid;
            String pctPart = "&preferredContentType="+contentType;
            int len = retrieveURI.length()+uidPart.length()+pctPart.length(); 
            if ( len < 129 ) { 
                return new String[]{retrieveURI+uidPart+pctPart};
            } else if ( uidPart.length()+pctPart.length() < 127) {//Slot max len = 128, we need 2 for <idx>|
                return new String[]{"1|"+retrieveURI, "2|"+uidPart+pctPart};
            } else {
                return new String[]{"1|"+retrieveURI, "2|"+uidPart, "3|"+pctPart};
            }
        } else {
            String s = retrieveURI+"?RT=DOCUMENT&UID="+uuid+"&PCT="+getShortContentType(contentType);
            return new String[]{s};
        }
    }

    /**
     * @param contentType
     * @return
     */
    private String getShortContentType(String contentType) {
        if ( "application/pdf".equals(contentType) ) return "pdf";
        if ( "text/xml".equals(contentType) ) return "xml";
        if ( "application/dicom".equals(contentType) ) return "dcm";
        return contentType;
    }
    private Map getAttachments(SOAPMessage message) {
        Map map = new HashMap();
        AttachmentPart part;
        String id;
        for ( Iterator iter = message.getAttachments(); iter.hasNext() ; ) {
            part = (AttachmentPart)iter.next();
            id = part.getContentId();
            if ( id.charAt(0) == '<') id = id.substring(1,id.length()-1); //remove < and >
            map.put( id, part);
        }
        return map;
    }

    private SOAPMessage getRegistryMessage( SOAPMessage request, Document metadata ) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        SOAPBody soapBody = envelope.getBody();
        soapBody.addDocument(metadata);
        return message;
    }

    /**
     * @param metadata
     * @throws IOException
     * @throws SOAPException 
     * @throws XDSException 
     */
    private XDSDocument saveDocumentEntry(XDSDocumentMetadata metadata, AttachmentPart part) throws IOException, SOAPException, XDSException {
        log.info("Store attachment:"+metadata);
        XDSDocument storedDoc;
        String id = metadata.getContentID();
        String docUid = metadata.getUniqueID();
        XDSDocument xdsDoc = new XDSDocument(docUid, part.getContentType(), 
                XDSDocumentWriterFactory.getInstance().getDocumentWriter(part));
        storedDoc = docStoreDelegate.storeDocument(xdsDoc, new DOMSource (metadata.getMetadata()));
        metadata.setHash(storedDoc.getHash(), rimPrefix);
        metadata.setSize(storedDoc.getSize(), rimPrefix);
        log.info("Attachment ("+docUid+") stored in file "+storedDoc+" (size:"+storedDoc.getSize()+" hash:"+storedDoc.getHash());
        return storedDoc;
    }

    public SOAPMessage sendSOAP( SOAPMessage message, String url ) throws SOAPException {
        SOAPConnection conn = null;
        try {
            log.debug("Send request to "+url);
            httpCfgDelegate.configTLS(url);
            SOAPConnectionFactory connFactory = SOAPConnectionFactory.newInstance();

            conn = connFactory.createConnection();
            log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            log.info("send registry request to "+url);
            log.info("-------------------------------- request  ----------------------------------");
            dumpSOAPMessage(message);
            SOAPMessage response = conn.call(message, url);
            log.info("-------------------------------- response ----------------------------------");
            dumpSOAPMessage(response);
            return response;
        } catch ( RuntimeException x ) {
            log.error("Cant send SOAP message! RuntimeException occured:", x);
            throw x;
        } catch ( SOAPException x ) {
            log.error("Cant send SOAP message! Reason:", x);
            throw x;
        } finally {
            if ( conn != null ) try {
                conn.close();
            } catch (SOAPException ignore) {}
        }

    }
    public boolean soapQueryTest( String xmlFileName ) throws SOAPException {
        return soapTest(xmlFileName, this.xdsQueryURI);
    }
    public boolean soapRegistryTest( String xmlFileName ) throws SOAPException {
        return soapTest(xmlFileName, this.xdsRegistryURI);
    }
    public boolean soapTest( String xmlFileName, String soapURL ) throws SOAPException {
        log.info("\n\nPerform SOAP Test with SOAP Body from file:"+xmlFileName);
        File xmlFile = new File( xmlFileName );
        if ( !xmlFile.exists() ) {
            log.warn("XML File for SOAP Body does not exist:"+xmlFile);
            return false;
        }
        Document sb = readXMLFile(xmlFile);
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        SOAPBody soapBody = envelope.getBody();
        soapBody.addDocument(sb);
        SOAPMessage response = sendSOAP(message, soapURL);
        return checkResponse(response, "RegistryResponse");
    }

    public String checkXDSDocumentMetadata(String xmlFileName) {
        log.info("\n\nCheck XDSDocumentMetadata for SOAP message:"+xmlFileName);
        File xmlFile = new File( xmlFileName );
        if ( !xmlFile.exists() ) {
            log.warn("XML File for SOAP Body does not exist:"+xmlFile);
            return "File not found!:"+xmlFileName;
        }
        Document d = readXMLFile(xmlFile);
        List extrinsicObjects = getExtrinsicObjects( getLeafRegistryObjectList( 
                getChildNode( getChildNode( getChildNode( d, null, "Envelope"), null, "Body"), 
                        XDSDocumentMetadata.NS_URN_REGISTRY_2_1, SUBMIT_OBJECTS_REQUEST) ) );
        XDSDocumentMetadata metadata;
        Element el;
        StringBuffer sb = new StringBuffer();
        for(Iterator iter = extrinsicObjects.iterator() ; iter.hasNext() ; ) {
            el = (Element)iter.next();
            metadata = new XDSDocumentMetadata(el);
            sb.append("SubmissionSet.uniqueid :").append(metadata.getUniqueID());
            sb.append("\ncontentID              :").append(metadata.getContentID());
            sb.append("\npat_id                 :").append(metadata.getPatientID());
            sb.append("\nmime                   :").append(metadata.getMimeType());
            sb.append("\n------------------------------------------------------------------------------------\n");
        }
        return sb.toString();
    }

    private Document readXMLFile(File xmlFile){
        Document document = null;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            dbFactory.setNamespaceAware(true);
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            document = builder.parse(xmlFile);
        } catch (Exception x) {
            log.error("Cant read xml file:"+xmlFile, x);
        }
        return document;
    }


    private boolean checkResponse(SOAPMessage response, String responseTag) throws SOAPException {
        log.info("checkResponse:"+response+" \nresponseTag:"+responseTag);
        try {
            SOAPBody body = response.getSOAPBody();
            log.debug("SOAPBody:"+body );
            NodeList nl = body.getChildNodes();
            if ( nl.getLength() != 0  ) {
                for ( int i = 0, len = nl.getLength() ; i < len ; i++ ) {
                    Node n = nl.item(i);
                    if ( n.getNodeType() == Node.ELEMENT_NODE &&
                            "RegistryResponse".equals(n.getLocalName() ) ) {
                        String status = n.getAttributes().getNamedItem("status").getNodeValue();
                        log.info("XDSI: SOAP response status."+status);
                        if ( "Failure".equals(status) ) {
                            StringBuffer sb = new StringBuffer();
                            NodeList errList = n.getChildNodes().item(0).getChildNodes();
                            Node errNode;
                            for ( int j = 0, lenj = errList.getLength() ; j < lenj ; j++ ) {
                                sb.setLength(0); 
                                sb.append("Error (").append(j).append("):");
                                if ( (errNode = errList.item(j)) != null && errNode.getFirstChild() != null ) {
                                    sb.append( errNode.getFirstChild().getNodeValue());
                                }
                                log.info(sb.toString());
                            }
                            return false;
                        } else {
                            return true;
                        }
                    }
                }
            } else {
                log.warn("XDSI: Empty SOAP response!");
            }
        } catch ( Exception x ) {
            log.error("Cant check response!", x);
        }
        return false;
    }

    /**
     * @param message
     * @return
     * @throws IOException
     * @throws SOAPException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SOAPException
     */
    private void dumpSOAPMessage(SOAPMessage message) {
        if ( ! logSOAPMessage ) return;
        try {
            Source s = message.getSOAPPart().getContent();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("SOAP message:".getBytes());
            Transformer t = TransformerFactory.newInstance().newTransformer();
            if (indentSOAPLog)
                t.setOutputProperty("indent", "yes");
            t.transform(s, new StreamResult(out));
            log.info(out.toString());
        } catch (Exception e) {
            log.warn("Failed to log SOAP message", e);
        }
        /*_*/        
    }

    public String fetchNewPatientIDfromNIST() throws IOException {
        URL url = new URL(fetchNewPatIDURL);
        httpCfgDelegate.configTLS(url.getProtocol());
        log.info("fetchNewPatIDURL:"+fetchNewPatIDURL);
        InputStream is = url.openStream();
        byte[] buffer = new byte[4096];
        int len;
        StringBuffer sb = new StringBuffer();
        while ( (len = is.read(buffer)) > 0 ) {
            sb.append(new String(buffer,0,len));
        }
        log.info("response of fetching new patientID:"+sb);
        String response = sb.toString().trim();
        int pos = response.indexOf("ISO");
        response = response.substring(0,pos+3);
        pos = response.lastIndexOf(">");
        if ( pos == -1 ) pos = response.lastIndexOf("="); //in Attribute?
        response = response.substring(pos+1);
        return response;
    }
    public static String resolvePath(String fn) {
        File f = new File(fn);
        if (f.isAbsolute()) return f.getAbsolutePath();
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath()).getAbsolutePath();
    }

    public List getRegistryObjects(SOAPMessage response, String responseTag) {
        ArrayList l = new ArrayList();
        try {
            SOAPBody body = response.getSOAPBody();
            log.debug("SOAPBody:"+body );
            List extrinsicObjects = this.getExtrinsicObjects( 
                    getLeafRegistryObjectList( getSubmitObjectsRequest(body) ) );
            if ( extrinsicObjects.size() != 0  ) {
                Node node = (Node) extrinsicObjects.get(0);
                List objRefs = this.getChildNodes(node, OBJECT_REF);
                log.debug("ObjectRefs:"+objRefs);
                for ( Iterator iter = objRefs.iterator() ; iter.hasNext() ; ) {
                    String id = ((Node) iter.next() ).getAttributes().getNamedItem("id").getNodeValue();
                    l.add( id );
                }
            }
        } catch ( Exception x ) {
            log.error("Cant get RegistryObjects from response!", x);
            return null;
        }
        log.info("return RegistryObjects:"+l);
        return l;
    }
    /*_*/

    private void logExport(String submissionUID, String patId, boolean success) {
        try {
            HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
            String user = userInfo.getUserId();
            XDSExportMessage msg = XDSExportMessage.createDocumentRepositoryExportMessage(submissionUID, patId);
            msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
                AuditEvent.OutcomeIndicator.MINOR_FAILURE);
            msg.setSource(AuditMessage.getProcessID(), 
                    AuditMessage.aetsToAltUserID(AuditMessage.getLocalAETitles()),
                    AuditMessage.getProcessName(),
                    AuditMessage.getLocalHostName(),
                    forceSourceAsRequestor || user == null);
            if (user != null) {
                msg.setHumanRequestor(user, null, null, true);
            }
            String host = "unknown";
            try {
                host = new URL(xdsRegistryURI).getHost();
            } catch (MalformedURLException ignore) {
            }
            msg.setDestination(xdsRegistryURI, null, null, host, false );
            msg.validate();
            Logger.getLogger("auditlog").info(msg);
        } catch ( Throwable t ) {
            log.warn("Audit Log (Export) failed! Ignored!",t);
        }
}
    
    private void logImport(String submissionUID, String patId, boolean success) {
        try {
            HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
            String user = userInfo.getUserId();
            XDSImportMessage msg = XDSImportMessage.createDocumentRepositoryImportMessage(submissionUID, patId);
            msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
                AuditEvent.OutcomeIndicator.MAJOR_FAILURE);
            msg.setSource(AuditMessage.getProcessID(), 
                    AuditMessage.aetsToAltUserID(AuditMessage.getLocalAETitles()),
                    AuditMessage.getProcessName(),
                    AuditMessage.getLocalHostName(),
                    forceSourceAsRequestor || user == null);
            if (user != null) {
                msg.setHumanRequestor(user, null, null, true);
            }
    
            String requestURI = userInfo.getRequestURL();
            String host = "unknown";
            try {
                host = new URL(requestURI).getHost();
            } catch (MalformedURLException ignore) {
            }
            msg.setDestination(requestURI, AuditMessage.getProcessID(), null, host, false );
            msg.validate();
            Logger.getLogger("auditlog").info(msg);
        } catch ( Throwable t ) {
            log.warn("Audit Log (Import) failed! Ignored!",t);
        }
    }

}
