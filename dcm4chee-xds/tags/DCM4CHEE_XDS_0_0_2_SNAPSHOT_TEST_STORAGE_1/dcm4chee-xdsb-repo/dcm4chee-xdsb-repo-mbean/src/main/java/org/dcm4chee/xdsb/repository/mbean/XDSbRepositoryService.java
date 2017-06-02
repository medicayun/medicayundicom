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
package org.dcm4chee.xdsb.repository.mbean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.management.ObjectName;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4chee.xds.common.UUID;
import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.XDSPerformanceLogger;
import org.dcm4chee.xds.common.audit.HttpUserInfo;
import org.dcm4chee.xds.common.audit.XDSExportMessage;
import org.dcm4chee.xds.common.audit.XDSImportMessage;
import org.dcm4chee.xds.common.audit.XDSRetrieveMessage;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.infoset.ExtrinsicObjectType;
import org.dcm4chee.xds.common.infoset.ObjectFactory;
import org.dcm4chee.xds.common.infoset.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds.common.infoset.RegistryError;
import org.dcm4chee.xds.common.infoset.RegistryErrorList;
import org.dcm4chee.xds.common.infoset.RegistryObjectType;
import org.dcm4chee.xds.common.infoset.RegistryPackageType;
import org.dcm4chee.xds.common.infoset.RegistryResponseType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetRequestType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds.common.infoset.SlotType1;
import org.dcm4chee.xds.common.infoset.SubmitObjectsRequest;
import org.dcm4chee.xds.common.infoset.ValueListType;
import org.dcm4chee.xds.common.infoset.ProvideAndRegisterDocumentSetRequestType.Document;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetResponseType.DocumentResponse;
import org.dcm4chee.xds.common.store.BasicXDSDocument;
import org.dcm4chee.xds.common.store.DocumentStoreDelegate;
import org.dcm4chee.xds.common.store.StoredDocument;
import org.dcm4chee.xds.common.store.XDSDocument;
import org.dcm4chee.xds.common.store.XDSbDocument;
import org.dcm4chee.xds.common.store.XDSDocumentWriter;
import org.dcm4chee.xds.common.store.XDSDocumentWriterFactory;
import org.dcm4chee.xds.common.utils.InfoSetUtil;
import org.dcm4chee.xdsb.repository.ws.client.DocumentRegistryPortType;
import org.dcm4chee.xdsb.repository.ws.client.DocumentRegistryService;
import org.dcm4chee.xdsb.repository.ws.client.RegistryClientHeaderHandler;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.SOAPElementImpl;
import org.jboss.ws.core.soap.SOAPElementWriter;
import org.w3c.dom.Node;


/**
 * @author franz.willer@gmail.com
 * @version $Revision: 5476 $ $Date: 2007-11-21 09:45:36 +0100 (Mi, 21 Nov 2007) $
 * @since Mar 08, 2006
 */
public class XDSbRepositoryService extends ServiceMBeanSupport {


    private static final String CERT = "CERT";
    private static final String NONE = "NONE";
    private String xdsRegistryURI;
    private String proxyHost;
    private int proxyPort;

    private String keystoreURL = "resource:identity.p12";
    private String keystorePassword;
    private String trustStoreURL = "resource:cacerts.jks";
    private String trustStorePassword;
    private HostnameVerifier origHostnameVerifier = null;
    private String allowedUrlHost = null;

    private String repositoryUniqueId;
    private String retrieveURI;
    private boolean logReceivedMessage;
    private boolean logRegisterMessage;
    private boolean logResponseMessage;
    private boolean indentXmlLog;
    private boolean saveRequestAsFile;
    private boolean mockRegistryResponse = true;
    private String mockError;


    private DocumentStoreDelegate docStoreDelegate = new DocumentStoreDelegate();
    private ObjectFactory objFac = new ObjectFactory();
    private XDSDocumentWriterFactory wrFac = XDSDocumentWriterFactory.getInstance();

    public String getRepositoryUniqueId() {
        return repositoryUniqueId;
    }
    public void setRepositoryUniqueId(String repositoryUniqueId) {
        this.repositoryUniqueId = repositoryUniqueId;
    }

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
        this.xdsRegistryURI = xdsRegistryURI;
    }

    /**
     * @return Returns the proxyHost.
     */
    public String getProxyHost() {
        return proxyHost == null ? NONE : proxyHost;
    }
    /**
     * @param proxyHost The proxyHost to set.
     */
    public void setProxyHost(String proxyHost) {
        if ( NONE.equals(proxyHost) ) 
            this.proxyHost = null;
        else
            this.proxyHost = proxyHost;
    }
    /**
     * @return Returns the proxyPort.
     */
    public int getProxyPort() {
        return proxyPort;
    }
    /**
     * @param proxyPort The proxyPort to set.
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * @param keyStorePassword The keyStorePassword to set.
     */
    public void setKeyStorePassword(String keyStorePassword) {
        if ( NONE.equals(keyStorePassword)) keyStorePassword = null;
        this.keystorePassword = keyStorePassword;
    }
    /**
     * @return Returns the keyStoreURL.
     */
    public String getKeyStoreURL() {
        return keystoreURL;
    }
    /**
     * @param keyStoreURL The keyStoreURL to set.
     */
    public void setKeyStoreURL(String keyStoreURL) {
        this.keystoreURL = keyStoreURL;
    }
    /**
     * @return Returns the trustStore.
     */
    public String getTrustStoreURL() {
        return trustStoreURL == null ? NONE : trustStoreURL;
    }
    /**
     * @param trustStore The trustStore to set.
     */
    public void setTrustStoreURL(String trustStoreURL) {
        if ( NONE.equals(trustStoreURL ) ) {
            this.trustStoreURL = null;
        } else {
            this.trustStoreURL = trustStoreURL;
        }
    }
    /**
     * @param trustStorePassword The trustStorePassword to set.
     */
    public void setTrustStorePassword(String trustStorePassword) {
        if ( NONE.equals(trustStorePassword)) trustStorePassword = null;
        this.trustStorePassword = trustStorePassword;
    }
    /**
     * @return Returns the allowedUrlHost.
     */
    public String getAllowedUrlHost() {
        return allowedUrlHost == null ? CERT : allowedUrlHost;
    }
    /**
     * @param allowedUrlHost The allowedUrlHost to set.
     */
    public void setAllowedUrlHost(String allowedUrlHost) {
        this.allowedUrlHost = CERT.equals(allowedUrlHost) ? null : allowedUrlHost;
    }

    /**
     * @return
     */
    public String getRetrieveURI() {
        return retrieveURI == null ? NONE : retrieveURI;
    }
    /**
     * @param retrieveURI The retrieveURI to set.
     */
    public void setRetrieveURI(String retrieveURI) {
        this.retrieveURI = NONE.equals(retrieveURI) ? null : retrieveURI;
    }

    public boolean isLogRegisterMessage() {
        return logRegisterMessage;
    }

    public void setLogRegisterMessage(boolean b) {
        this.logRegisterMessage = b;
    }

    public boolean isLogReceivedMessage() {
        return logReceivedMessage;
    }
    public void setLogReceivedMessage(boolean b) {
        this.logReceivedMessage = b;
    }
    public boolean isLogResponseMessage() {
        return logResponseMessage;
    }
    public void setLogResponseMessage(boolean b) {
        this.logResponseMessage = b;
    }
    public boolean isIndentXmlLog() {
        return indentXmlLog;
    }
    public void setIndentXmlLog(boolean b) {
        this.indentXmlLog = b;
    }


    public boolean isSaveRequestAsFile() {
        return saveRequestAsFile;
    }
    public void setSaveRequestAsFile(boolean saveRequestAsFile) {
        this.saveRequestAsFile = saveRequestAsFile;
    }
    public boolean isMockRegistryResponse() {
        return mockRegistryResponse;
    }
    public void setMockRegistryResponse(boolean mockRegistryRsponse) {
        this.mockRegistryResponse = mockRegistryRsponse;
    }
    public String getMockError() {
        return mockError == null ? NONE : mockError;
    }
    public void setMockError(String mockError) {
        this.mockError = NONE.equals(mockError) ? null : mockError;
    }

    public ObjectName getDocumentStoreService() {
        return docStoreDelegate.getDocumentStoreService();
    }

    public void setDocumentStoreService(ObjectName name) {
        docStoreDelegate.setDocumentStoreService(name);
    }

    public RegistryResponseType storeAndRegisterDocuments(ProvideAndRegisterDocumentSetRequestType req) throws XDSException {
        Map<String, StoredDocument> storedDocuments = null;
        boolean success = false;
        XDSPerformanceLogger perfLogger = new XDSPerformanceLogger("XDSb", "ProvideAndRegisterDocumentSet");
        try {
            log.debug("------------ProvideAndRegisterDocumentSetRequest:"+req);
            perfLogger.startSubEvent("LogAndVerify");
            if ( logReceivedMessage ) {
                List<Document> docList = new ArrayList<Document>();
                docList.addAll(req.getDocument());
                req.getDocument().clear();
                log.info(" Received ProvideAndRegisterDocumentSetRequest"+InfoSetUtil.marshallObject( objFac.createProvideAndRegisterDocumentSetRequest(req), indentXmlLog));
                log.info("Documents:"+docList.size()+" DocumentElements in request. (Hidden in above xml representation!)");
                Document doc;
                for ( Iterator<Document> iter = docList.iterator() ; iter.hasNext() ; ) {
                    doc = iter.next();
                    log.info("Document:"+doc.getId()+" contentType:"+doc.getValue().getContentType()+
                            " size:"+doc.getValue().getInputStream().available());
                }
                req.getDocument().addAll(docList);
            }
            SubmitObjectsRequest submitRequest = req.getSubmitObjectsRequest();
            RegistryPackageType registryPackage = InfoSetUtil.getRegistryPackage(submitRequest);
            if ( registryPackage == null ) {
                log.error("No RegistryPackage found!");
                throw new XDSException( XDSConstants.XDS_ERR_REPOSITORY_ERROR, 
                        XDSConstants.XDS_ERR_MISSING_REGISTRY_PACKAGE, null);
            }
            String submissionUID = InfoSetUtil.getExternalIdentifierValue(UUID.XDSSubmissionSet_uniqueId,registryPackage);
            perfLogger.setEventProperty("SubmissionSetUID", submissionUID);
            if ( saveRequestAsFile ) {
                File f = new File(resolvePath("log/xdsb/pnr-"+submissionUID+".xml"));
                f.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(f);
                CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
                if ( msgContext != null ) {
                    SOAPMessage msg = msgContext.getSOAPMessage();
                    SOAPElementWriter writer = new SOAPElementWriter(fos, "UTF-8");
                    writer.writeElement((SOAPElementImpl) msg.getSOAPPart().getEnvelope());
                } else {
                    log.info("No Message Context found (Request is not part of a webservice request)! Save ProvideAndRegisterDocumentSetRequestType instead of SOAP message!");
                    InfoSetUtil.writeObject(req, fos, true);
                }
                fos.close();
            }
            perfLogger.endSubEvent();
            storedDocuments = exportDocuments(req, perfLogger);
            if ( logRegisterMessage ) {
                String xmlReq = InfoSetUtil.marshallObject(submitRequest, this.indentXmlLog);
                log.info("'Register Document Set' SubmitRequest:"+xmlReq);
            }
            log.info("SubmissionUID:"+submissionUID);
            logImport(submissionUID, true);
            RegistryResponseType rsp = dispatchSubmitObjectsRequest(submitRequest, perfLogger);
            success = checkResponse( rsp );
            perfLogger.startSubEvent("AuditAndBuildResponse");
            logExport(submissionUID, success);
            log.info("ProvideAndRegisterDocumentSetRequest success:"+success);
            if ( logResponseMessage ) {
                log.info("Received RegistryResponse:"+InfoSetUtil.marshallObject(objFac.createRegistryResponse(rsp), indentXmlLog));
            }
            perfLogger.endSubEvent();
            return rsp;
        } catch (XDSException x) {
            throw x;
        } catch (Throwable t) {
            throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR,"Provide And Register failed!",t);
       } finally {
           perfLogger.startSubEvent("PostProcess");
           perfLogger.setSubEventProperty("Success", String.valueOf(success));
           if ( storedDocuments != null ) {
               postProcessStorage(storedDocuments, success);
           }
           perfLogger.endSubEvent();
           perfLogger.flush();
       }
    }
    private RegistryResponseType dispatchSubmitObjectsRequest(
            SubmitObjectsRequest submitRequest, XDSPerformanceLogger perfLogger) throws MalformedURLException,
            JAXBException, XDSException {
        if ( mockRegistryResponse ) {
            log.info("Mock RegistryResponse! Bypass 'Register Document Set' transaction!");
            return getMockResponse(perfLogger);
        }
        perfLogger.startSubEvent("RegisterDocuments");
        perfLogger.setSubEventProperty("RegistryURI", xdsRegistryURI);
        DocumentRegistryPortType port = new DocumentRegistryService().getDocumentRegistryPortSoap12();
        Map<String, Object> reqCtx = ((BindingProvider)port).getRequestContext();
        List customHandlerChain = new ArrayList();
        customHandlerChain.add(new RegistryClientHeaderHandler());
        SOAPBinding binding = (SOAPBinding)((BindingProvider)port).getBinding();
        binding.setHandlerChain(customHandlerChain);               
        log.debug("OLD ENDPOINT_ADDRESS_PROPERTY:"+reqCtx.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
        reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, xdsRegistryURI);
        log.debug("NEW ENDPOINT_ADDRESS_PROPERTY:"+reqCtx.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
        //Dispatch<Source> dispatch = createDispatch();
        //Source s = InfoSetUtil.getSourceForObject(submitRequest);
        log.info("####################################################");
        log.info("####################################################");
        log.info("XDS.b: Send register document-b request to registry:"+xdsRegistryURI);
        log.info("####################################################");
        log.info("####################################################");
        RegistryResponseType rsp = port.registerDocumentSetB(submitRequest);
        log.info("Received RegistryResponse:"+InfoSetUtil.marshallObject(
                objFac.createRegistryResponse(rsp), indentXmlLog) );
        perfLogger.endSubEvent();
        return rsp;
    }

    private RegistryResponseType getMockResponse(XDSPerformanceLogger perfLogger) {
        RegistryResponseType rsp = objFac.createRegistryResponseType();
        log.info("MockError:"+mockError);
        perfLogger.startSubEvent("RegisterDocuments");
        perfLogger.setSubEventProperty("RegistryURI", "none - mocked");
        if ( mockError == null ) {
            rsp.setStatus(XDSConstants.XDS_B_STATUS_SUCCESS);
        } else {
            rsp.setStatus(XDSConstants.XDS_B_STATUS_FAILURE);
            RegistryErrorList errList = objFac.createRegistryErrorList();
            List<RegistryError> errors = errList.getRegistryError();
            RegistryError err = objFac.createRegistryError();
            int pos = mockError.indexOf('|');
            String errCode = pos == -1 ? XDSConstants.XDS_ERR_REPOSITORY_ERROR : mockError.substring(0,pos);
            String errMsg = pos == -1 ? mockError : mockError.substring(++pos);
            err.setErrorCode(errCode);
            err.setCodeContext(errMsg);
            errors.add(err);
        }
        perfLogger.endSubEvent();
        return rsp;
    }
    private void postProcessStorage( Map<String, StoredDocument> storedDocuments, 
            boolean success) {
        if ( success ) {
            docStoreDelegate.commitDocuments(storedDocuments.values());
        } else {
            docStoreDelegate.rollbackDocuments(storedDocuments.values());
        }
    }

    public RetrieveDocumentSetResponseType retrieveDocumentSet(RetrieveDocumentSetRequestType req) throws XDSException {
        XDSPerformanceLogger perfLogger = new XDSPerformanceLogger("XDSb", "RetrieveDocumentSet");
        String docUid;
        XDSDocument doc;
        RetrieveDocumentSetRequestType.DocumentRequest docReq;
        RetrieveDocumentSetResponseType rsp = objFac.createRetrieveDocumentSetResponseType();
        RetrieveDocumentSetResponseType.DocumentResponse docRsp;
        List localDocUids = new ArrayList();
        for ( Iterator<RetrieveDocumentSetRequestType.DocumentRequest> iter = req.getDocumentRequest().iterator() ; iter.hasNext() ; ) {
            docReq = iter.next();
            docUid = docReq.getDocumentUniqueId();
            if ( docReq.getRepositoryUniqueId().equals(this.repositoryUniqueId) ) {
                perfLogger.startSubEvent("RetrieveDocument");
                perfLogger.setSubEventProperty("DocumentUUID", docUid);
                doc = docStoreDelegate.retrieveDocument(docUid, null);
                if ( doc != null ) {
                    perfLogger.setSubEventProperty("DocumentSize", String.valueOf(doc.getXdsDocWriter().size()));
                    localDocUids.add(docUid);
                    try {
                        docRsp = getDocumentResponse(doc);
                        rsp.getDocumentResponse().add(docRsp);
                    } catch (IOException e) {
                        log.error("Error in building DocumentResponse for document:"+doc);
                    }
                } else {
                    log.warn("Document not found! document UID:"+docUid);
                }
                perfLogger.endSubEvent();
            } else {
                log.info("Retrieve Document Request for other Repository!("+docReq.getRepositoryUniqueId()+
                        ") docUid:"+docUid);
            }
        }
        perfLogger.startSubEvent("AuditAndCreateResponse");
        logRetrieve(localDocUids,true);
        RegistryResponseType regRsp = objFac.createRegistryResponseType();
        regRsp.setStatus(XDSConstants.XDS_B_STATUS_SUCCESS);
        rsp.setRegistryResponse(regRsp);
        perfLogger.endSubEvent();
        perfLogger.flush();
        return rsp;
    }

    private DocumentResponse getDocumentResponse(XDSDocument doc) throws IOException {
        RetrieveDocumentSetResponseType.DocumentResponse docRsp;
        docRsp = objFac.createRetrieveDocumentSetResponseTypeDocumentResponse();
        docRsp.setDocumentUniqueId(doc.getDocumentUID());
        docRsp.setMimeType(doc.getMimeType());
        docRsp.setRepositoryUniqueId(this.repositoryUniqueId);
        docRsp.setDocument(doc.getXdsDocWriter().getDataHandler());
        return docRsp;
    }

    public DataHandler retrieveDocument(String docUid, String mime) throws XDSException {
        log.info("Retrieve Document "+docUid+" with mime type:"+mime);
        XDSDocument doc = docStoreDelegate.retrieveDocument(docUid, mime);
        if ( doc != null ) {
            if ( mime == null || mime.equals( doc.getMimeType() ) ) {
                XDSDocumentWriter wr = doc.getXdsDocWriter();
                return new XdsDataHandler(wr, mime);
            }
            log.info("Requested mime type ("+mime+
                    ") doesn't match with mime type of stored document ("+doc.getMimeType()+")!");
        } else {
            log.info("Requested document not found:"+docUid);
        }
        return null;
    }

    public Map exportDocuments(ProvideAndRegisterDocumentSetRequestType req, XDSPerformanceLogger perfLogger) throws XDSException {
        Map extrObjs = InfoSetUtil.getExtrinsicObjects(req.getSubmitObjectsRequest());
        Map docs = InfoSetUtil.getDocuments(req);
        if ( extrObjs.size() > docs.size() ) {
            log.warn("Missing Documents! Found more ExtrinsicObjects("+extrObjs.size()+") than Documents("+docs.size()+")!");
            throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR,
                    XDSConstants.XDS_ERR_MISSING_DOCUMENT, null);
        } else if ( extrObjs.size() < docs.size() ) {
            log.warn("Missing Document Metadata! Found less ExtrinsicObjects("+extrObjs.size()+") than Documents("+docs.size()+")!");
            throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR,
                    XDSConstants.XDS_ERR_MISSING_DOCUMENT_METADATA, null);

        } else if ( docs.size() == 0 ) {
            log.info("No Documents to save!");
            return null;
        }
        Map storedDocuments = new HashMap();
        List list = req.getSubmitObjectsRequest().getRegistryObjectList().getIdentifiable();
        ExtrinsicObjectType extrObj;
        JAXBElement elem;
        Object o;
        Map slots;
        XDSDocument doc;
        XDSbDocument xdsDoc;
        Document document;
        for ( Iterator iter = list.iterator(); iter.hasNext() ; ) {
            elem = (JAXBElement) iter.next();
            o = elem.getValue();
            log.info("search for ExtrinsicObjectType RegistryObject:\n"+o+"\n"+ExtrinsicObjectType.class.getName());
            if ( o instanceof ExtrinsicObjectType) {
                try {
                    perfLogger.startSubEvent("StoreDocument");
                    extrObj = (ExtrinsicObjectType) o;
                    document = (Document) docs.get(extrObj.getId());
                    xdsDoc = new XDSbDocument(extrObj, wrFac.getDocumentWriter(document.getValue(), -1));
                    perfLogger.setSubEventProperty("DocumentUUID", xdsDoc.getDocumentUID());
                    Node n = InfoSetUtil.getNodeForObject(elem);
                    doc = docStoreDelegate.storeDocument(xdsDoc,new DOMSource(n) );
                    if ( doc != null ) {
                        perfLogger.setSubEventProperty("DocumentSize", String.valueOf(doc.getSize()));
                        storedDocuments.put(extrObj.getId(),doc);
                        slots = xdsDoc.getSlots();
                        addOrOverwriteSlot(extrObj, slots, XDSConstants.SLOT_REPOSITORY_UNIQUE_ID, this.getRepositoryUniqueId());
                        addOrOverwriteSlot(extrObj, slots, XDSConstants.SLOT_SIZE, String.valueOf(doc.getSize()));
                        addOrOverwriteSlot(extrObj, slots, XDSConstants.SLOT_HASH, doc.getHash());
                        if ( this.retrieveURI != null ) {
                            addOrOverwriteSlot(extrObj, slots, XDSConstants.SLOT_URI, this.getDocumentURI(doc.getDocumentUID(), xdsDoc.getMimeType()));
                        }
                    } else {
                        log.warn("Document already exists! docUid:"+xdsDoc.getDocumentUID());
                    }
                    perfLogger.endSubEvent();
                } catch ( Throwable x ) {
                    log.error("Export document failed!",x);
                    docStoreDelegate.rollbackDocuments(storedDocuments.values());
                    XDSException e;
                    if ( x instanceof XDSException) {
                        e = (XDSException)x;
                    } else {
                        e = new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR,
                                "Document already exists!", x);
                    }
                    throw e;
                }
            }
        }
        return storedDocuments;
    }

    private void addOrOverwriteSlot(RegistryObjectType ro, Map slots, String slotName, String val) throws JAXBException {
        addOrOverwriteSlot(ro, slots, slotName, new String[]{val});
    }
    private void addOrOverwriteSlot(RegistryObjectType ro, Map slots, String slotName, String[] val) throws JAXBException {
        SlotType1 slot = objFac.createSlotType1();
        slot.setName(slotName);
        ValueListType valueList = objFac.createValueListType();
        for ( int i = 0 ; i < val.length ; i++) {
            valueList.getValue().add(val[i]);
        }
        slot.setValueList(valueList);
        if ( slots.containsKey(slotName) ) {
            log.warn("RegistryObject id="+ro.getId()+" already contains slot '"+slotName+"'!");
            SlotType1 oldSlot = (SlotType1)slots.get(slotName); 
            ro.getSlot().remove(oldSlot);
        }   
        ro.getSlot().add(slot);
    }

    private boolean checkResponse(RegistryResponseType rsp) throws JAXBException {
        if ( rsp == null ){
            log.error("No RegistryResponse from registry!");
            return false;
        }
        log.debug("Check RegistryResponse:"+InfoSetUtil.marshallObject(objFac.createRegistryResponse(rsp), indentXmlLog) );
        String status = rsp.getStatus();
        log.debug("Rsp status:"+status );
        return status == null ? false : XDSConstants.XDS_B_STATUS_SUCCESS.equalsIgnoreCase(rsp.getStatus());
    }

    private Dispatch<Source> createDispatch() throws MalformedURLException, JAXBException {
        String targetNS = XDSConstants.NS_URN_IHE_ITI_XDS_B_2007;
        QName serviceName = new QName(targetNS, "XDSbRepositoryService");
        QName portName = new QName(targetNS, "XDSbRepositoryPort");
        Service service = Service.create(serviceName);
        configProxyAndTLS(xdsRegistryURI);
        service.addPort(portName, SOAPBinding.SOAP12HTTP_BINDING, new URL(xdsRegistryURI).toExternalForm());
        Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);

        log.info("############# add custom RegSOAPHeaderHandler!");
        SOAPBinding binding = (SOAPBinding)((BindingProvider)dispatch).getBinding();
        List customHandlerChain = new ArrayList();
        customHandlerChain.add(new RegSOAPHeaderHandler());
        binding.setHandlerChain(customHandlerChain);               
        log.info("############# add custom RegSOAPHeaderHandler done!");
        return dispatch;
    }

    /**
     * @param uuid
     * @param contentType
     * @return
     */
    public String[] getDocumentURI(String uuid, String contentType) {
        if (retrieveURI == null ) 
            return  null;
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
    }

    /**
     * 
     */
    private void configProxyAndTLS(String url) {
        String protocol = url.startsWith("https") ? "https" : "http";
        if ( proxyHost != null && proxyHost.trim().length() > 1 ) {
            System.setProperty( protocol+".proxyHost", proxyHost);
            System.setProperty(protocol+".proxyPort", String.valueOf(proxyPort));
        } else {
            System.setProperty(protocol+".proxyHost", "");
            System.setProperty(protocol+".proxyPort", "");
        }
        if ( "https".equals(protocol) && trustStoreURL != null ) {
            String keyStorePath = resolvePath(keystoreURL);
            String trustStorePath = resolvePath(trustStoreURL);
            System.setProperty("javax.net.ssl.keyStore", keyStorePath);
            if ( keystorePassword != null ) 
                System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);
            System.setProperty("javax.net.ssl.keyStoreType","PKCS12");
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            if ( trustStorePassword != null )
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
            if ( origHostnameVerifier == null) {
                origHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
                HostnameVerifier hv = new HostnameVerifier() {
                    public boolean verify(String urlHostName, SSLSession session) {
                        if ( !origHostnameVerifier.verify ( urlHostName, session)) {
                            if ( isAllowedUrlHost(urlHostName)) {
                                log.warn("Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
                            } else {
                                return false;
                            }
                        }
                        return true;
                    }

                    private boolean isAllowedUrlHost(String urlHostName) {
                        if (allowedUrlHost == null) return false;
                        if ( allowedUrlHost.equals("*")) return true;
                        return allowedUrlHost.equals(urlHostName);
                    }

                };

                HttpsURLConnection.setDefaultHostnameVerifier(hv);
            }
        }           

    }

    public static String resolvePath(String fn) {
        File f = new File(fn);
        if (f.isAbsolute()) return f.getAbsolutePath();
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath()).getAbsolutePath();
    }

    private void logExport(String submissionUID, boolean success) {
        HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
        String user = userInfo.getUserId();
        XDSExportMessage msg = XDSExportMessage.createDocumentRepositoryExportMessage(submissionUID);
        msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
            AuditEvent.OutcomeIndicator.MINOR_FAILURE);
        msg.setSource(AuditMessage.getProcessID(), 
                AuditMessage.getLocalAETitles(),
                AuditMessage.getProcessName(),
                AuditMessage.getLocalHostName());
        msg.setHumanRequestor(user != null ? user : "unknown", null, null);
        String host = "unknown";
        try {
            host = new URL(xdsRegistryURI).getHost();
        } catch (MalformedURLException ignore) {
        }
        msg.setDestination(xdsRegistryURI, null, "XDS Export", host );
        msg.validate();
        Logger.getLogger("auditlog").info(msg);
    }
    private void logImport(String submissionUID, boolean success) {
        HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
        String user = userInfo.getUserId();
        XDSImportMessage msg = new XDSImportMessage();
        msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
            AuditEvent.OutcomeIndicator.MAJOR_FAILURE);
        msg.setSource(AuditMessage.getProcessID(), 
                AuditMessage.getLocalAETitles(),
                AuditMessage.getProcessName(),
                AuditMessage.getLocalHostName() );
        msg.setHumanRequestor(user != null ? user : "unknown", null, null);

        String requestURI = userInfo.getRequestURI();
        String host = "unknown";
        try {
            host = new URL(requestURI).getHost();
        } catch (MalformedURLException ignore) {
        }
        msg.setDestination(requestURI, null, "XDS Export", host );
        msg.setSubmissionSet(submissionUID);
        msg.validate();
        Logger.getLogger("auditlog").info(msg);
    }

    private void logRetrieve(List<String> docUids, boolean success) {
        try {
            HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
            String user = userInfo.getUserId();
            XDSRetrieveMessage msg = XDSRetrieveMessage.createDocumentRepositoryRetrieveMessage(docUids);
            msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
                AuditEvent.OutcomeIndicator.MINOR_FAILURE);
            msg.setSource(AuditMessage.getProcessID(), 
                    AuditMessage.getLocalAETitles(),
                    AuditMessage.getProcessName(),
                    AuditMessage.getLocalHostName());
            msg.setHumanRequestor(user != null ? user : "unknown", null, null);
            msg.setDestination(userInfo.getRequestURI(), null, userInfo.getHostName(), userInfo.getIP() );
            msg.validate();
            Logger.getLogger("auditlog").info(msg);
        } catch ( Throwable t) {
            log.error("Cant send Audit Log for Retrieve Document Set! Ignored");
        }
    }

    public class XdsDataHandler extends DataHandler {
        private XDSDocumentWriter writer;
        public XdsDataHandler(XDSDocumentWriter wr, String mime) {
            super(null, mime);
            writer = wr;
        }

        public void writeTo(OutputStream out) throws IOException {
            writer.writeTo(out);
        }
    }

}

