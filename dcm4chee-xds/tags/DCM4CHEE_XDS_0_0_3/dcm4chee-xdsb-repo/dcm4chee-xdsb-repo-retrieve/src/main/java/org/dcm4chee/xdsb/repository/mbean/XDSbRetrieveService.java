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
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.management.ObjectName;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.XDSPerformanceLogger;
import org.dcm4chee.xds.common.audit.HttpUserInfo;
import org.dcm4chee.xds.common.audit.XDSExportMessage;
import org.dcm4chee.xds.common.audit.XDSRetrieveMessage;
import org.dcm4chee.xds.common.delegate.XdsHttpCfgDelegate;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.infoset.ObjectFactory;
import org.dcm4chee.xds.common.infoset.RegistryError;
import org.dcm4chee.xds.common.infoset.RegistryErrorList;
import org.dcm4chee.xds.common.infoset.RegistryResponseType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetRequestType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds.common.infoset.ProvideAndRegisterDocumentSetRequestType.Document;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetRequestType.DocumentRequest;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetResponseType.DocumentResponse;
import org.dcm4chee.xds.common.store.DocumentStoreDelegate;
import org.dcm4chee.xds.common.store.XDSDocument;
import org.dcm4chee.xds.common.store.XDSDocumentWriter;
import org.dcm4chee.xds.common.utils.InfoSetUtil;
import org.dcm4chee.xds.common.ws.DocumentRepositoryPortType;
import org.dcm4chee.xds.common.ws.DocumentRepositoryPortTypeFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;


/**
 * @author franz.willer@gmail.com
 * @version $Revision: 5476 $ $Date: 2007-11-21 09:45:36 +0100 (Mi, 21 Nov 2007) $
 * @since Mar 08, 2006
 */
public class XDSbRetrieveService extends ServiceMBeanSupport {


    private static final String CERT = "CERT";
    private static final String NONE = "NONE";
    
    private boolean logRequestMessage;
    private boolean logResponseMessage;
    private boolean logRemoteRequestMessages;
    private boolean logRemoteResponseMessages;
    private boolean indentXmlLog;
    
    private Map<String, String> mapExternalRepositories = new HashMap<String, String>();


    private DocumentStoreDelegate docStoreDelegate = new DocumentStoreDelegate();
    private XdsHttpCfgDelegate httpCfgDelegate = new XdsHttpCfgDelegate();
    
    private ObjectFactory objFac = new ObjectFactory();
    private boolean auditLogIti17;
    private boolean auditLogIti43;

    public String getExternalRepositories() {
        if (mapExternalRepositories == null) return NONE;
        StringBuffer sb = new StringBuffer();
        for (Map.Entry e : mapExternalRepositories.entrySet()) {
            sb.append(e.getKey()).append('=').append(e.getValue()).append(System.getProperty("line.separator", "\n"));
        }
        return sb.toString();
    }

    public void setExternalRepositories(String s) {
        if (s == null || s.trim().equalsIgnoreCase(NONE) ) {
            mapExternalRepositories = null;
        } else {
            mapExternalRepositories = new HashMap<String,String>();
            StringTokenizer st = new StringTokenizer(s, "\r\n;=");
            while ( st.hasMoreElements() ) {
                mapExternalRepositories.put( st.nextToken().trim(), st.nextToken().trim());
            }
        }
    }

    public boolean isAuditLogIti17() {
        return auditLogIti17;
    }

    public void setAuditLogIti17(boolean auditLogIti17) {
        this.auditLogIti17 = auditLogIti17;
    }

    public boolean isAuditLogIti43() {
        return auditLogIti43;
    }

    public void setAuditLogIti43(boolean auditLogIti43) {
        this.auditLogIti43 = auditLogIti43;
    }

    public boolean isLogRequestMessage() {
        return logRequestMessage;
    }
    public void setLogRequestMessage(boolean b) {
        this.logRequestMessage = b;
    }
    public boolean isLogResponseMessage() {
        return logResponseMessage;
    }
    public void setLogResponseMessage(boolean b) {
        this.logResponseMessage = b;
    }
    public boolean isLogRemoteRequestMessages() {
        return logRemoteRequestMessages;
    }
    public void setLogRemoteRequestMessages(boolean logRemoteRequestMessages) {
        this.logRemoteRequestMessages = logRemoteRequestMessages;
    }
    public boolean isLogRemoteResponseMessages() {
        return logRemoteResponseMessages;
    }
    public void setLogRemoteResponseMessages(boolean logRemoteResponseMessages) {
        this.logRemoteResponseMessages = logRemoteResponseMessages;
    }
    public boolean isIndentXmlLog() {
        return indentXmlLog;
    }
    public void setIndentXmlLog(boolean b) {
        this.indentXmlLog = b;
    }


    public ObjectName getDocumentStoreService() {
        return docStoreDelegate.getDocumentStoreService();
    }

    public void setDocumentStoreService(ObjectName name) {
        docStoreDelegate.setDocumentStoreService(name);
    }

    public RetrieveDocumentSetResponseType retrieveDocumentSet(RetrieveDocumentSetRequestType req, String repositoryUniqueId) throws XDSException {
        if ( logRequestMessage) {
            try {
                log.info("RetrieveDocumentSetRequest:"+InfoSetUtil.marshallObject(
                    objFac.createRetrieveDocumentSetRequest(req), indentXmlLog) );
            } catch (JAXBException ignore) {
                log.debug("Failed to log RetrieveDocumentSetRequest! Ignored", ignore);
            }
        }
        XDSPerformanceLogger perfLogger = new XDSPerformanceLogger("XDSb", "RetrieveDocumentSet");
        String docUid, reqRepoUid;
        XDSDocument doc;
        RetrieveDocumentSetResponseType rsp = objFac.createRetrieveDocumentSetResponseType();
        RetrieveDocumentSetResponseType.DocumentResponse docRsp;
        List<String> localDocUids = new ArrayList<String>();
        List<RetrieveDocumentSetRequestType.DocumentRequest> reqDocList;
        Map<String, List<RetrieveDocumentSetRequestType.DocumentRequest>> remoteDocRequests = 
            new HashMap<String, List<RetrieveDocumentSetRequestType.DocumentRequest>>();
        for ( RetrieveDocumentSetRequestType.DocumentRequest docReq : req.getDocumentRequest() ) {
            reqRepoUid = docReq.getRepositoryUniqueId();
            docUid = docReq.getDocumentUniqueId();
            if ( reqRepoUid.equals(repositoryUniqueId) ) {
                perfLogger.startSubEvent("RetrieveDocument");
                perfLogger.setSubEventProperty("DocumentUUID", docUid);
                doc = docStoreDelegate.retrieveDocument(docUid, null);
                if ( doc != null ) {
                    perfLogger.setSubEventProperty("DocumentSize", String.valueOf(doc.getXdsDocWriter().size()));
                    localDocUids.add(docUid);
                    try {
                        docRsp = getDocumentResponse(doc, repositoryUniqueId);
                        rsp.getDocumentResponse().add(docRsp);
                    } catch (IOException e) {
                        log.error("Error in building DocumentResponse for document:"+doc);
                    }
                } else {
                    log.warn("Document not found! document UID:"+docUid);
                }
                perfLogger.endSubEvent();
            } else if ( mapExternalRepositories == null ) {
                String msg = "DocumentRepositoryUID="+reqRepoUid+" is unknown! This repository unique ID:"+repositoryUniqueId;
                log.warn(msg);
                throw new XDSException(XDSConstants.XDS_ERR_WRONG_REPOSITORY_UNIQUE_ID,
                        msg, null);
            } else {
                log.info("Retrieve Document Request for other Repository!("+docReq.getRepositoryUniqueId()+
                        ") docUid:"+docUid);
                reqDocList = remoteDocRequests.get(reqRepoUid);
                if (reqDocList == null) {
                    reqDocList = new ArrayList<DocumentRequest>();
                    remoteDocRequests.put(reqRepoUid, reqDocList);
                }
                reqDocList.add(docReq);
            }
        }
        if (!remoteDocRequests.isEmpty()) {
            try {
                addRemoteRetrieveRequests(rsp, remoteDocRequests, perfLogger);
            } catch (Exception e) {
                log.error("Remote Document Retrieve failed!",e);
            }
        }        
        perfLogger.startSubEvent("AuditAndCreateResponse");
        logRetrieve(localDocUids,true);
        RegistryResponseType regRsp = objFac.createRegistryResponseType();
        regRsp.setStatus(XDSConstants.XDS_B_STATUS_SUCCESS);
        rsp.setRegistryResponse(regRsp);
        if ( logResponseMessage) {
            log.info(InfoSetUtil.getLogMessage(rsp));
        }
        perfLogger.endSubEvent();
        perfLogger.flush();
        return rsp;
    }

    private DocumentResponse getDocumentResponse(XDSDocument doc, String repositoryUniqueId) throws IOException {
        RetrieveDocumentSetResponseType.DocumentResponse docRsp;
        docRsp = objFac.createRetrieveDocumentSetResponseTypeDocumentResponse();
        docRsp.setDocumentUniqueId(doc.getDocumentUID());
        docRsp.setMimeType(doc.getMimeType());
        docRsp.setRepositoryUniqueId(repositoryUniqueId);
        docRsp.setDocument(doc.getXdsDocWriter().getDataHandler());
        return docRsp;
    }

    public DataHandler retrieveLocalDocument(String docUid, String mime) throws XDSException {
        log.info("Retrieve local Document "+docUid+" with mime type:"+mime);
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


    private boolean checkResponse(RegistryResponseType rsp) throws JAXBException {
        if ( rsp == null ){
            log.error("No RegistryResponse from repository (retrieve)!");
            return false;
        }
        log.debug("Check RegistryResponse:"+InfoSetUtil.marshallObject(objFac.createRegistryResponse(rsp), indentXmlLog) );
        String status = rsp.getStatus();
        log.debug("Rsp status:"+status );
        return status == null ? false : XDSConstants.XDS_B_STATUS_SUCCESS.equalsIgnoreCase(rsp.getStatus());
    }


    private RetrieveDocumentSetResponseType addRemoteRetrieveRequests(
            RetrieveDocumentSetResponseType mainResp, Map<String, List<DocumentRequest>> remoteDocRequests, XDSPerformanceLogger perfLogger) throws MalformedURLException,
            JAXBException, XDSException {
        perfLogger.startSubEvent("RetrieveRemoteDocuments");
        perfLogger.setSubEventProperty("NumberOfRemoteRepositories", 
                String.valueOf(remoteDocRequests.size()));
        DocumentRepositoryPortType port;
        String extRepositoryURI;
        List<DocumentRequest> unknownRepositoryRequests = new ArrayList<DocumentRequest>();
        for ( Map.Entry entry : remoteDocRequests.entrySet()) {
            extRepositoryURI = mapExternalRepositories.get( entry.getKey());
            if ( extRepositoryURI != null ) {
                httpCfgDelegate.configTLS(extRepositoryURI);
                port = DocumentRepositoryPortTypeFactory.getDocumentRepositoryPortSoap12(
                        extRepositoryURI, 
                        XDSConstants.URN_IHE_ITI_2007_RETRIEVE_DOCUMENT_SET, 
                        java.util.UUID.randomUUID().toString());
                log.info("####################################################");
                log.info("####################################################");
                log.info("XDS.b Retrieve Service: Send retrieve document set-b request to repository:"+extRepositoryURI);
                log.info("####################################################");
                log.info("####################################################");
                RetrieveDocumentSetRequestType req = objFac.createRetrieveDocumentSetRequestType();
                req.getDocumentRequest().addAll((List<DocumentRequest>) entry.getValue());
                if ( logRemoteRequestMessages) {
                    try {
                        log.info("Remote RetrieveDocumentSetRequest:"+InfoSetUtil.marshallObject(
                            objFac.createRetrieveDocumentSetRequest(req), indentXmlLog) );
                    } catch (JAXBException ignore) {
                        log.debug("Failed to log RetrieveDocumentSetRequest! Ignored", ignore);
                    }
                }
                RetrieveDocumentSetResponseType rsp = port.documentRepositoryRetrieveDocumentSet(req);
                if ( logRemoteResponseMessages) {
                    log.info(InfoSetUtil.getLogMessage(rsp));
                }
                if ( checkResponse(rsp.getRegistryResponse()) ) {
                    log.info("Add "+rsp.getDocumentResponse().size()+" documents from remote repository!");
                    mainResp.getDocumentResponse().addAll(rsp.getDocumentResponse());
                }
            } else {
                log.info("Unknown remote XDS.b Repository:"+extRepositoryURI);
                unknownRepositoryRequests.addAll((List<DocumentRequest>) entry.getValue());
            }
        }
        perfLogger.endSubEvent();
        return mainResp;
    }

    public static String resolvePath(String fn) {
        File f = new File(fn);
        if (f.isAbsolute()) return f.getAbsolutePath();
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath()).getAbsolutePath();
    }


    private void logRetrieve(List<String> docUids, boolean success) {
        if ( auditLogIti17 ) {
            try {
                HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
                String docUri = userInfo.getRequestURL();
                for ( String docUid : docUids ) {
                    this.logExport(docUri, docUid, userInfo, success);
                }
            } catch ( Throwable t) {
                log.error("Cant send Audit Log for Retrieve Document (ITI-17)! Ignored");
            }
        }
        if ( auditLogIti43 ) {
            try {
                HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
                String user = userInfo.getUserId();
                XDSRetrieveMessage msg = XDSRetrieveMessage.createDocumentRepositoryRetrieveMessage(docUids);
                msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
                    AuditEvent.OutcomeIndicator.MINOR_FAILURE);
                msg.setSource(AuditMessage.getProcessID(), 
                        AuditMessage.getLocalAETitles(),
                        AuditMessage.getProcessName(),
                        AuditMessage.getLocalHostName(),
                        true);
                msg.setHumanRequestor(user != null ? user : "unknown", null, null, true);
                msg.setDestination(userInfo.getRequestURL(), null, userInfo.getHostName(), userInfo.getIP(), false );
                msg.validate();
                Logger.getLogger("auditlog").info(msg);
            } catch ( Throwable t) {
                log.error("Cant send Audit Log for Retrieve Document Set (ITI-43)! Ignored");
            }
        }
    }

    private void logExport(String docUri, String docUid, HttpUserInfo userInfo, boolean success) {
        try {
            String user = userInfo.getUserId();
            XDSExportMessage msg = XDSExportMessage.createDocumentRepositoryRetrieveMessage(docUri, docUid);
            msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
                AuditEvent.OutcomeIndicator.MAJOR_FAILURE);
            msg.setSource(AuditMessage.getProcessID(), 
                    AuditMessage.getLocalAETitles(),
                    AuditMessage.getProcessName(),
                    AuditMessage.getLocalHostName(),
                    false);
    
            String requestURI = userInfo.getRequestURL();
            String host = "unknown";
            try {
                host = new URL(requestURI).getHost();
            } catch (MalformedURLException ignore) {
            }
            msg.setDestination(requestURI, null, null, host, true );
            msg.validate();
            Logger.getLogger("auditlog").info(msg);
        } catch ( Throwable t ) {
            log.warn("Audit Log (Import) failed! Ignored!",t);
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

