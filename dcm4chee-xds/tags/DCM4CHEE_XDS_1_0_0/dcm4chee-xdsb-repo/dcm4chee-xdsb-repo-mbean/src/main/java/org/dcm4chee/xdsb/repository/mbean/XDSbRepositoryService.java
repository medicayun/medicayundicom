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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.management.ObjectName;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.addressing.AddressingBuilder;
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
import org.dcm4chee.xds.common.delegate.XDSbServiceDelegate;
import org.dcm4chee.xds.common.delegate.XdsHttpCfgDelegate;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.infoset.v30.ExtrinsicObjectType;
import org.dcm4chee.xds.infoset.v30.ObjectFactory;
import org.dcm4chee.xds.infoset.v30.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds.infoset.v30.RegistryError;
import org.dcm4chee.xds.infoset.v30.RegistryErrorList;
import org.dcm4chee.xds.infoset.v30.RegistryObjectType;
import org.dcm4chee.xds.infoset.v30.RegistryPackageType;
import org.dcm4chee.xds.infoset.v30.RegistryResponseType;
import org.dcm4chee.xds.infoset.v30.RetrieveDocumentSetRequestType;
import org.dcm4chee.xds.infoset.v30.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds.infoset.v30.SlotType1;
import org.dcm4chee.xds.infoset.v30.SubmitObjectsRequest;
import org.dcm4chee.xds.infoset.v30.ValueListType;
import org.dcm4chee.xds.infoset.v30.ProvideAndRegisterDocumentSetRequestType.Document;
import org.dcm4chee.xds.infoset.v30.RetrieveDocumentSetRequestType.DocumentRequest;
import org.dcm4chee.xds.infoset.v30.RetrieveDocumentSetResponseType.DocumentResponse;
import org.dcm4chee.xds.common.store.DocumentStoreDelegate;
import org.dcm4chee.xds.common.store.XDSDocument;
import org.dcm4chee.xds.common.store.XDSDocumentWriter;
import org.dcm4chee.xds.common.store.XDSDocumentWriterFactory;
import org.dcm4chee.xds.common.store.XDSbDocument;
import org.dcm4chee.xds.common.utils.InfoSetUtil;
import org.dcm4chee.xds.infoset.v30.ws.DocumentRegistryPortType;
import org.dcm4chee.xds.infoset.v30.ws.DocumentRegistryPortTypeFactory;
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

    private static final String NONE = "NONE";

    private String repositoryUniqueId;
    private String xdsRegistryURI;

    private String retrieveURI;
    private boolean logReceivedMessage;
    private boolean logRegisterMessage;
    private boolean logResponseMessage;
    private boolean indentXmlLog;
    private boolean saveRequestAsFile;
    private boolean mockRegistryResponse = true;
    private String mockError;


    private DocumentStoreDelegate docStoreDelegate = new DocumentStoreDelegate();
    private XDSbServiceDelegate xdsbServiceDelegate = new XDSbServiceDelegate();
    private XdsHttpCfgDelegate httpCfgDelegate = new XdsHttpCfgDelegate();

    private ObjectFactory objFac = new ObjectFactory();
    private XDSDocumentWriterFactory wrFac = XDSDocumentWriterFactory.getInstance();
    private boolean disableForceMTOMResponse;
    private boolean forceSourceAsRequestor;

    private RetrieveDocumentSetResponseType rsp;
    
    public String getRepositoryUniqueId() {
        return repositoryUniqueId;
    }
    public void setRepositoryUniqueId(String repositoryUniqueId) {
        this.repositoryUniqueId = repositoryUniqueId.trim();
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
        this.xdsRegistryURI = xdsRegistryURI.trim();
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
        this.retrieveURI = NONE.equals(retrieveURI) ? null : retrieveURI.trim();
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
        this.mockError = NONE.equals(mockError) ? null : mockError.trim();
    }

    public boolean isDisableForceMTOMResponse() {
        return disableForceMTOMResponse;
    }
    public void setDisableForceMTOMResponse(boolean disableForceMTOMResponse) {
        this.disableForceMTOMResponse = disableForceMTOMResponse;
    }
    public boolean isForceSourceAsRequestor() {
        return forceSourceAsRequestor;
    }
    public void setForceSourceAsRequestor(boolean forceSourceAsRequestor) {
        this.forceSourceAsRequestor = forceSourceAsRequestor;
    }
    public ObjectName getDocumentStoreService() {
        return docStoreDelegate.getDocumentStoreService();
    }

    public void setDocumentStoreService(ObjectName name) {
        docStoreDelegate.setDocumentStoreService(name);
    }

    public ObjectName getXDSbRetrieveService() {
        return XDSbServiceDelegate.getXdsRetrieveServiceName();
    }

    public void setXDSbRetrieveService(ObjectName name) {
        XDSbServiceDelegate.setXdsRetrieveServiceName(name);
    }

    protected void startService() throws Exception {
        XDSbServiceDelegate.setXdsRepositoryServiceName(this.getServiceName());
    }

    public RegistryResponseType storeAndRegisterDocuments(ProvideAndRegisterDocumentSetRequestType req) throws XDSException {
        Map<String, XDSDocument> storedDocuments = null;
        boolean success = false;
        XDSPerformanceLogger perfLogger = new XDSPerformanceLogger("XDSb", "ProvideAndRegisterDocumentSet");
        try {
            log.debug("------------ProvideAndRegisterDocumentSetRequest:"+req);
            perfLogger.startSubEvent("LogAndVerify");
            if ( logReceivedMessage ) {
                log.info(InfoSetUtil.getLogMessage(req));
            }
            SubmitObjectsRequest submitRequest = req.getSubmitObjectsRequest();
            RegistryPackageType submissionSet = InfoSetUtil.getRegistryPackage(submitRequest, UUID.XDSSubmissionSet);
            if ( submissionSet == null ) {
                log.error("No RegistryPackage id=SubmissionSet found!");
                throw new XDSException( XDSConstants.XDS_ERR_REPOSITORY_ERROR, 
                        XDSConstants.XDS_ERR_MISSING_REGISTRY_PACKAGE, null);
            }
            String patId = checkPatientIDs(req, submissionSet);
            String submissionUID = InfoSetUtil.getExternalIdentifierValue(UUID.XDSSubmissionSet_uniqueId, submissionSet);

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
            //TODO: get replyTo from real WS Addressing Header
            String replyTo = AddressingBuilder.getAddressingBuilder().newAddressingConstants().getAnonymousURI();
            logImport(submissionUID, patId, replyTo, true);
            RegistryResponseType rsp = dispatchSubmitObjectsRequest(submitRequest, perfLogger);
            success = checkResponse( rsp );
            perfLogger.startSubEvent("AuditAndBuildResponse");
            logExport(submissionUID, patId, replyTo, success);
            log.info("ProvideAndRegisterDocumentSetRequest success:"+success);
            if ( logResponseMessage ) {
                log.info("Received RegistryResponse:"+InfoSetUtil.marshallObject(objFac.createRegistryResponse(rsp), indentXmlLog));
            }
            perfLogger.endSubEvent();
            CommonMessageContext ctx = MessageContextAssociation.peekMessageContext();
            ctx.put("DISABLE_FORCE_MTOM_RESPONSE", Boolean.toString(disableForceMTOMResponse));
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
        httpCfgDelegate.configTLS(xdsRegistryURI);
        DocumentRegistryPortType port = DocumentRegistryPortTypeFactory.getDocumentRegistryPortSoap12(xdsRegistryURI, 
        		XDSConstants.URN_IHE_ITI_2007_REGISTER_DOCUMENT_SET_B, java.util.UUID.randomUUID().toString());
        log.info("####################################################");
        log.info("####################################################");
        log.info("XDS.b: Send register document-b request to registry:"+xdsRegistryURI);
        log.info("####################################################");
        log.info("####################################################");
        RegistryResponseType rsp;
        try {
            rsp = port.documentRegistryRegisterDocumentSetB(submitRequest);
        } catch ( Exception x) {
            throw new XDSException( XDSConstants.XDS_ERR_REG_NOT_AVAIL, "Document Registry not available: "+xdsRegistryURI,x);
        }
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
            rsp.setRegistryErrorList(errList);
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
    private void postProcessStorage( Map<String, XDSDocument> storedDocuments, 
            boolean success) {
        if ( success ) {
            docStoreDelegate.commitDocuments(storedDocuments.values());
        } else {
            docStoreDelegate.rollbackDocuments(storedDocuments.values());
        }
    }

    public RetrieveDocumentSetResponseType retrieveDocumentSet(RetrieveDocumentSetRequestType req) throws XDSException {
        return retrieveDocumentSet(req, repositoryUniqueId);
    }
    private RetrieveDocumentSetResponseType retrieveDocumentSet(RetrieveDocumentSetRequestType req, String repoUID) throws XDSException {
        try {
            return xdsbServiceDelegate.retrieveDocumentSetFromXDSbRetrieveService(req, repoUID);
        } catch ( Exception x ) {
            if ( x instanceof XDSException ) {
                throw (XDSException)x;
            } else if ( x.getCause() instanceof XDSException ){
                throw (XDSException) x.getCause();
            }
            log.error( "Exception occured in retrieveDocumentSet: "+x.getMessage(), x );
            throw new XDSException( XDSConstants.XDS_ERR_REPOSITORY_ERROR, "Unexpected error in XDS service !: "+x.getMessage(),x);
        }
    }

    public Map exportDocuments(ProvideAndRegisterDocumentSetRequestType req, XDSPerformanceLogger perfLogger) throws XDSException {
        Map extrObjs = InfoSetUtil.getExtrinsicObjects(req.getSubmitObjectsRequest());
        Map docs = InfoSetUtil.getDocuments(req);
        if ( extrObjs.size() > docs.size() ) {
            log.warn("Missing Documents! Found more ExtrinsicObjects("+extrObjs.size()+") than Documents("+docs.size()+")!");
            throw new XDSException(XDSConstants.XDS_ERR_MISSING_DOCUMENT,
                    "", null);
        } else if ( extrObjs.size() < docs.size() ) {
            log.warn("Missing Document Metadata! Found less ExtrinsicObjects("+extrObjs.size()+") than Documents("+docs.size()+")!");
            throw new XDSException(XDSConstants.XDS_ERR_MISSING_DOCUMENT_METADATA,
                    "", null);

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
                    doc = docStoreDelegate.storeDocument(xdsDoc, new DOMSource(n) );
                    if ( doc != null ) {
                        perfLogger.setSubEventProperty("DocumentSize", String.valueOf(doc.getSize()));
                        storedDocuments.put(extrObj.getId(),doc);
                        slots = xdsDoc.getSlots();
                        addOrOverwriteSlot(extrObj, slots, XDSConstants.SLOT_REPOSITORY_UNIQUE_ID, getRepositoryUniqueId());
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

    private String checkPatientIDs(ProvideAndRegisterDocumentSetRequestType req, RegistryPackageType registryPackage) throws XDSException {
        String submissionPatId = InfoSetUtil.getExternalIdentifierValue(UUID.XDSSubmissionSet_patientId, registryPackage);
        Map<String, ExtrinsicObjectType> extrObjs = InfoSetUtil.getExtrinsicObjects(req.getSubmitObjectsRequest());
        String docPatId;
        for ( ExtrinsicObjectType eo : extrObjs.values() ) {
            docPatId = InfoSetUtil.getExternalIdentifierValue(UUID.XDSDocumentEntry_patientId, eo);
            if ( docPatId != null && !docPatId.equals(submissionPatId)) {
                String msg = "XDSDocumentEntry.patientId ("+docPatId+")and XDSSubmissionSet.patientId ("+submissionPatId+") doesn't match! ExtrinsicObject.Id:"+eo.getId();
                log.warn(msg);
                throw new XDSException(XDSConstants.XDS_ERR_PATID_DOESNOT_MATCH,
                        msg, null);
            }
        }
        RegistryPackageType folder = InfoSetUtil.getRegistryPackage(req.getSubmitObjectsRequest(), UUID.XDSFolder_ClassificationNode);
        String folderPatId = InfoSetUtil.getExternalIdentifierValue(UUID.XDSFolder_patientId, folder);
        if ( folderPatId != null && !folderPatId.equals(submissionPatId)) {
            String msg = "XDSFolder.patientId ("+folderPatId+")and XDSSubmissionSet.patientId ("+submissionPatId+") doesn't match!";
            log.warn(msg);
            throw new XDSException(XDSConstants.XDS_ERR_PATID_DOESNOT_MATCH,
                    msg, null);
        }
        return submissionPatId;
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

    public static String resolvePath(String fn) {
        File f = new File(fn);
        if (f.isAbsolute()) return f.getAbsolutePath();
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath()).getAbsolutePath();
    }

    private void logExport(String submissionUID, String patId, String replyTo, boolean success) {
        try {
            HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
            String user = userInfo.getUserId();
            XDSExportMessage msg = XDSExportMessage.createDocumentRepositoryBExportMessage(submissionUID, patId);
            msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
                AuditEvent.OutcomeIndicator.MINOR_FAILURE);
            msg.setSource(replyTo, 
                    AuditMessage.getProcessID(),
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
    
    private void logImport(String submissionUID, String patId, String replyTo, boolean success) {
        try {
            HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
            String user = userInfo.getUserId();
            XDSImportMessage msg = XDSImportMessage.createDocumentRepositoryBImportMessage(submissionUID, patId);
            msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
                AuditEvent.OutcomeIndicator.MAJOR_FAILURE);
            msg.setSource(replyTo, 
                    AuditMessage.getProcessID(),
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
    
    public RetrieveDocumentSetResponseType retrieveDocumentSet(String docUids, String repositoryUID, String homeUid, boolean useLocalRepo) throws XDSException {
        RetrieveDocumentSetRequestType rq = objFac.createRetrieveDocumentSetRequestType();
        StringTokenizer st = new StringTokenizer(docUids, "|");
        while ( st.hasMoreTokens()) {
            rq.getDocumentRequest().add( createDocRequest(st.nextToken(), repositoryUID, homeUid) );
        }
        return retrieveDocumentSet(rq, useLocalRepo ? repositoryUniqueId : null);
    }

    public String saveDocumentSet(String docUids, String repositoryUID, String homeUid, String baseDir, boolean useLocalRepo) throws XDSException, IOException {
        RetrieveDocumentSetRequestType rq = objFac.createRetrieveDocumentSetRequestType();
        StringTokenizer st = new StringTokenizer(docUids, "|");
        while ( st.hasMoreTokens()) {
            rq.getDocumentRequest().add( createDocRequest(st.nextToken(), repositoryUID, homeUid) );
        }
        rsp = retrieveDocumentSet(rq, useLocalRepo ? repositoryUniqueId : null);
        saveDocuments(baseDir, rsp.getDocumentResponse());
        return rsp.getRegistryResponse().getStatus();
    }
    
    private void saveDocuments(String baseDir,
            List<DocumentResponse> documentResponse) throws IOException {
        if ( documentResponse == null) return;
        File dir = new File(baseDir);
        if (!dir.isAbsolute()) {
            File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
            dir = new File(serverHomeDir, dir.getPath());
        }
        if ( !dir.exists() )
            dir.mkdirs();
        File f;
        for ( DocumentResponse doc : documentResponse ) {
            f = new File(dir, URLEncoder.encode(doc.getDocumentUniqueId(), "UTF-8"));
            writeDoc(f,doc.getDocument());
        }
        
    }
    private void writeDoc(File f, DataHandler dh) throws IOException {
        log.info("Save Document in file:"+f+ " Mime:"+dh.getContentType());
        FileOutputStream fos = new FileOutputStream(f);
        try {
            dh.writeTo(fos);
        } finally {
            fos.close();
        }
    }
    public DataHandler retrieveDocument(String docUid, String repositoryUID, String homeUid, boolean useLocalRepo) throws XDSException {
        RetrieveDocumentSetResponseType rsp = retrieveDocumentSet(docUid, repositoryUID, homeUid, useLocalRepo);
        try {
            if ( checkResponse(rsp.getRegistryResponse()) ) {
                List<DocumentResponse> l = rsp.getDocumentResponse();
                if ( l.size() == 1) {
                    DocumentResponse docRsp = l.get(0);
                    return docRsp.getDocument();
                } else if ( l.size() == 0 ) {
                    log.info("XDSDocument "+docUid+" not found on this Repository!");
                } else {
                    log.warn("More than one document found for documentUID:"+docUid);
                }
            }
        } catch ( Exception x) {
            throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR, "Error Checking response!",x);
        }
        return null;
    }

    public String retrieveDocumentAsString(String docUid, String repositoryUID, String homeUid, boolean useLocalRepo) throws XDSException, IOException {
        DataHandler dh = retrieveDocument(docUid, repositoryUID, homeUid, useLocalRepo);
        BufferedInputStream is = null;
        StringWriter w = null;
        try {
            if ( dh == null ) {
                return ("XDSDocument "+docUid+" not found on this Repository!");
            } else {
                w = new StringWriter();
                is = new BufferedInputStream(dh.getInputStream());
                int b;
                while ( (b = is.read()) != -1 ) {
                    if ( b > 31) {
                        w.write(b);
                    }
                }
                return w.toString();
            }
        } finally {
            if (is != null) 
                is.close();
            if (w != null)
                w.close();
        }
    }

    private DocumentRequest createDocRequest(String docUid,
            String repositoryUID, String homeUid) {
        DocumentRequest docRq = objFac.createRetrieveDocumentSetRequestTypeDocumentRequest();
        docRq.setDocumentUniqueId(docUid);
        docRq.setRepositoryUniqueId( (repositoryUID == null || repositoryUID.trim().length() == 0)
                ? this.getRepositoryUniqueId() : repositoryUID);
        if ( homeUid != null && homeUid.trim().length() > 0 )
            docRq.setHomeCommunityId(homeUid);
        return docRq;
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

