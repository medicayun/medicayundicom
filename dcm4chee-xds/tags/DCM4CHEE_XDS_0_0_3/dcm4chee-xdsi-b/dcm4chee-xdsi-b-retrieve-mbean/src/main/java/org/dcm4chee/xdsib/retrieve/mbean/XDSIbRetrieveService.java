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
package org.dcm4chee.xdsib.retrieve.mbean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.ejb.EJB;
import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.SAXWriter;
import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.XDSPerformanceLogger;
import org.dcm4chee.xds.common.audit.HttpUserInfo;
import org.dcm4chee.xds.common.audit.XDSExportMessage;
import org.dcm4chee.xds.common.audit.XDSRetrieveMessage;
import org.dcm4chee.xds.common.delegate.XdsHttpCfgDelegate;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.infoset.ObjectFactory;
import org.dcm4chee.xds.common.infoset.RegistryResponseType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetRequestType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetRequestType.DocumentRequest;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetResponseType.DocumentResponse;
import org.dcm4chee.xds.common.store.XDSDocumentWriter;
import org.dcm4chee.xds.common.utils.InfoSetUtil;
import org.dcm4chee.xds.common.ws.DocumentRepositoryPortType;
import org.dcm4chee.xds.common.ws.DocumentRepositoryPortTypeFactory;
import org.dcm4chee.xdsib.retrieve.dao.RetrieveLocal;
import org.jboss.annotation.ejb.Service;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;


/**
 * @author franz.willer@gmail.com
 * @version $Revision: 5476 $ $Date: 2007-11-21 09:45:36 +0100 (Mi, 21 Nov 2007) $
 * @since Mar 08, 2006
 */
@Service(objectName="dcm4chee.xds:service=XDSIbRetrieveService",  
        xmbean="resource:META-INF/jboss-service.xml")
public class XDSIbRetrieveService extends ServiceMBeanSupport {

    private static final String NONE = "NONE";
    
    private boolean logRequestMessage;
    private boolean logResponseMessage;
    private boolean logRemoteRequestMessages;
    private boolean logRemoteResponseMessages;
    private boolean indentXmlLog;
    
    private XdsHttpCfgDelegate httpCfgDelegate = new XdsHttpCfgDelegate();
    
    private ObjectFactory objFac = new ObjectFactory();
    private boolean auditLogIti17;
    private boolean auditLogIti43;
    private DicomRetrieveDelegate drDelegate = new DicomRetrieveDelegate();
    private Map<String, String> mapExternalRepositories = new HashMap<String, String>();

    private String repositoryUniqueId;

    @EJB  
    private RetrieveLocal retrieveBean; 
    
    public String getRepositoryUniqueId() {
        return repositoryUniqueId;
    }

    public void setRepositoryUniqueId(String repositoryUniqueId) {
        this.repositoryUniqueId = repositoryUniqueId;
    }

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

    public RetrieveDocumentSetResponseType retrieveDocumentSet(RetrieveDocumentSetRequestType req) throws XDSException {
        if ( logRequestMessage) {
            try {
                log.info("RetrieveImagingDocumentSetRequest:"+InfoSetUtil.marshallObject(
                    objFac.createRetrieveDocumentSetRequest(req), indentXmlLog) );
            } catch (JAXBException ignore) {
                log.debug("Failed to log RetrieveImagingDocumentSetRequest! Ignored", ignore);
            }
        }
        XDSPerformanceLogger perfLogger = new XDSPerformanceLogger("XDSb", "RetrieveImagingDocumentSet");
        String docUid, reqRepoUid;
        DataHandler doc;
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
                perfLogger.startSubEvent("RetrieveImagingDocument");
                perfLogger.setSubEventProperty("DocumentUUID", docUid);
                doc = drDelegate.retrieveDocument(docUid);
                if ( doc != null ) {
                    perfLogger.setSubEventProperty("DocumentSize", "");
                    localDocUids.add(docUid);
                    try {
                        docRsp = getDocumentResponse(docUid, repositoryUniqueId, doc);
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
            if (!remoteDocRequests.isEmpty()) {
                try {
                    addRemoteRetrieveRequests(rsp, remoteDocRequests, perfLogger);
                } catch (Exception e) {
                    log.error("Remote Document Retrieve failed!",e);
                }
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

    private DocumentResponse getDocumentResponse(String docUid, String repositoryUniqueId, DataHandler dh) throws IOException {
        RetrieveDocumentSetResponseType.DocumentResponse docRsp;
        docRsp = objFac.createRetrieveDocumentSetResponseTypeDocumentResponse();
        docRsp.setDocumentUniqueId(docUid);
        docRsp.setMimeType("application/dicom");
        docRsp.setRepositoryUniqueId(repositoryUniqueId);
        docRsp.setDocument(dh);
        return docRsp;
    }
    
    public RetrieveDocumentSetResponseType retrieveDocumentSet(String docUids, String repositoryUID, String homeUid, boolean useLocalRepo) throws XDSException {
        RetrieveDocumentSetRequestType rq = objFac.createRetrieveDocumentSetRequestType();
        StringTokenizer st = new StringTokenizer(docUids, "|");
        while ( st.hasMoreTokens()) {
            rq.getDocumentRequest().add( createDocRequest(st.nextToken(), repositoryUID, homeUid) );
        }
        return retrieveDocumentSet(rq);
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
    

    public DataHandler retrieveLocalDocument(String docUid) {
        log.info("Retrieve local Document (Dicom Object) "+docUid);
        log.info("@EJB injected RetrieveBean:"+retrieveBean);
        return drDelegate.retrieveDocument(docUid);
    }
    
    public String testRetrieveLocalDocument(String docUid) throws IOException, TransformerConfigurationException {
        DataHandler dh = retrieveLocalDocument(docUid);
        if ( dh == null) return "Document not found!";
        if ("application/dicom".equals(dh.getContentType())) {
            return dcm2xml(dh.getInputStream());
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dh.writeTo(baos);
        return baos.toString();
    }

    public String dcm2xml(InputStream is) throws IOException, TransformerConfigurationException {
            DicomInputStream dis = new DicomInputStream(is);
        try {
            SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
            TransformerHandler th = tf.newTransformerHandler(new StreamSource(is, ""));
            
            th.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            th.setResult(new StreamResult(baos));
            final SAXWriter writer = new SAXWriter(th, th);
            dis.setHandler(writer);
            dis.readDicomObject(new BasicDicomObject(), -1);
            return baos.toString();
        } finally {
            dis.close();
        }
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
                log.info("XDS-I.b Retrieve Service: Send retrieve imaging document set request to repository:"+extRepositoryURI);
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

