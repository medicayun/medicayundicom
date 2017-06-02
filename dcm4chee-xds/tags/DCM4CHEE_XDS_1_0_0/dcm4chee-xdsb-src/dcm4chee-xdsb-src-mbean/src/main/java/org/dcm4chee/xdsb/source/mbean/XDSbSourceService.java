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
package org.dcm4chee.xdsb.source.mbean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.util.UIDUtils;
import org.dcm4chee.xds.common.UUID;
import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.audit.HttpUserInfo;
import org.dcm4chee.xds.common.audit.XDSExportMessage;
import org.dcm4chee.xds.common.delegate.XdsHttpCfgDelegate;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.infoset.v30.ExtrinsicObjectType;
import org.dcm4chee.xds.infoset.v30.ObjectFactory;
import org.dcm4chee.xds.infoset.v30.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds.infoset.v30.RegistryPackageType;
import org.dcm4chee.xds.infoset.v30.RegistryResponseType;
import org.dcm4chee.xds.infoset.v30.SubmitObjectsRequest;
import org.dcm4chee.xds.infoset.v30.ProvideAndRegisterDocumentSetRequestType.Document;
import org.dcm4chee.xds.common.utils.InfoSetUtil;
import org.dcm4chee.xds.infoset.v30.ws.DocumentRepositoryPortType;
import org.dcm4chee.xds.infoset.v30.ws.DocumentRepositoryPortTypeFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.w3c.dom.Node;



/**
 * @author franz.willer@gmail.com
 * @version $Revision: 5476 $ $Date: 2007-11-21 09:45:36 +0100 (Mi, 21 Nov 2007) $
 * @since Mar 08, 2006
 */
public class XDSbSourceService extends ServiceMBeanSupport {


    private static final String V2TOV3_XSL = "v2tov3.xsl";
    private static final String V3TOV2_XSL = "v3tov2.xsl";
    private static final String CERT = "CERT";
    private static final String NONE = "NONE";
    private String xdsRepositoryURI;

    private String sourceID;
    private boolean logRequest;
    private boolean logResponse;
    private boolean indentXmlLog;
    private boolean useSOAP11=false;
    private boolean forceSourceAsRequestor;
    
    private XdsHttpCfgDelegate httpCfgDelegate = new XdsHttpCfgDelegate();

    private ObjectFactory objFac = new ObjectFactory();

    private String v2Tov3Xslt;
    private String v3Tov2Xslt;
    private Templates v2toV3tpl;
    private Templates v3toV2tpl;
    private boolean forceMTOMRequest;

    public String getSourceId() {
        return sourceID;
    }
    public void setSourceId(String id) {
        this.sourceID = id.trim();
    }

    /**
     * @return Returns the docRepositoryURI.
     */
    public String getXDSRepositoryURI() {
        return this.xdsRepositoryURI;
    }
    /**
     * @param docRepositoryURI The docRepositoryURI to set.
     */
    public void setXDSRepositoryURI(String uri) {
        this.xdsRepositoryURI = uri.trim();
    }

    /**
     * @return Returns the logSOAPMessage.
     */
    public boolean isLogRequestMessage() {
        return logRequest;
    }
    /**
     * @param logSOAPMessage The logSOAPMessage to set.
     */
    public void setLogRequestMessage(boolean b) {
        this.logRequest = b;
    }

    public boolean isLogResponseMessage() {
        return logResponse;
    }
    public void setLogResponseMessage(boolean b) {
        this.logResponse = b;
    }
    public boolean isIndentXmlLog() {
        return indentXmlLog;
    }
    public void setIndentXmlLog(boolean indentSOAPLog) {
        this.indentXmlLog = indentSOAPLog;
    }


    public String getV2Tov3Xslt() {
        return v2Tov3Xslt == null ? "DEFAULT" : v2Tov3Xslt;
    }
    public void setV2Tov3Xslt(String tov3Xslt) {
        v2Tov3Xslt = "DEFAULT".equals(tov3Xslt) ? null : tov3Xslt.trim();
        v2toV3tpl = null;
    }
    public String getV3Tov2Xslt() {
        return v3Tov2Xslt == null ? "DEFAULT" : v3Tov2Xslt;
    }
    public void setV3Tov2Xslt(String tov2Xslt) {
        v3Tov2Xslt = "DEFAULT".equals(tov2Xslt) ? null : tov2Xslt.trim();
        v3toV2tpl = null;
    }
    private Templates getV2toV3Template() throws TransformerConfigurationException, TransformerFactoryConfigurationError, IOException {
        if ( v2toV3tpl == null ) {
            URL url = this.getClass().getResource(V2TOV3_XSL);
            SAXTransformerFactory tf = (SAXTransformerFactory)TransformerFactory.newInstance();
            if ( v2Tov3Xslt == null ) {
                v2toV3tpl = tf.newInstance().newTemplates(
                        new StreamSource(this.getClass().getResourceAsStream(V2TOV3_XSL)) );
            } else {
                v2toV3tpl = tf.newInstance().newTemplates(
                        new StreamSource(new File(resolvePath(v2Tov3Xslt))));
            }
        }
        return v2toV3tpl;
    }
    private Templates getV3toV2Template() throws TransformerConfigurationException, TransformerFactoryConfigurationError {
        if ( v3toV2tpl == null ) {
            SAXTransformerFactory tf = (SAXTransformerFactory)TransformerFactory.newInstance();
            if ( v3Tov2Xslt == null ) {
                v3toV2tpl = tf.newInstance().newTemplates(
                        new StreamSource(this.getClass().getResourceAsStream(V3TOV2_XSL)));
            } else {
                v3toV2tpl = tf.newInstance().newTemplates(
                        new StreamSource(new File(resolvePath(v3Tov2Xslt))));
            }
        }
        return v3toV2tpl;
    }
    public boolean isUseSOAP11() {
        return useSOAP11;
    }
    public void setUseSOAP11(boolean useSOAP11) {
        this.useSOAP11 = useSOAP11;
    }
    public boolean isForceSourceAsRequestor() {
        return forceSourceAsRequestor;
    }
    public void setForceSourceAsRequestor(boolean forceSourceAsRequestor) {
        this.forceSourceAsRequestor = forceSourceAsRequestor;
    }
    public boolean isForceMTOMRequest() {
        return forceMTOMRequest;
    }
    public void setForceMTOMRequest(boolean forceMTOMRequest) {
        this.forceMTOMRequest = forceMTOMRequest;
    }
    private boolean isRimV2(Node n) {
        if ( n instanceof org.w3c.dom.Document)
            n = n.getFirstChild();
        return XDSConstants.NS_URN_REGISTRY_2_1.equals(n.getNamespaceURI());
    }
    private Node convertRimVersion(Node nIn, boolean toV3) throws Exception {
        if ( log.isDebugEnabled()) {
            log.debug("convert rim version to "+(toV3 ? "V3" : "V2"));
            logNode("convert rim version input:", nIn);
            log.debug("Input is Document?:"+(nIn instanceof org.w3c.dom.Document));
        }
        DOMResult result = new DOMResult();
        convertV2V3(new DOMSource(nIn), result, toV3 ? getV2toV3Template() : getV3toV2Template() );
        Node nOut = result.getNode();
        if (log.isDebugEnabled() ) 
            logNode("convert rim version output:", nOut);
        return nOut;
    }
    private void convertV2V3(Source xmlSource, Result target, Templates tpl) throws TransformerConfigurationException, TransformerException{
        SAXTransformerFactory tf = (SAXTransformerFactory)TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler(tpl);
        th.getTransformer().transform(xmlSource, target);
    }

    public Node exportDocuments(Node req, Map docs) throws XDSException {
        return exportDocuments(req, docs, false);
    }
    public Node exportDocuments(Node req, Map docs, boolean unifyUIDs) throws XDSException {
        try {
            log.info("------------exportDocuments:"+req);
            Unmarshaller unmarshaller = InfoSetUtil.getJAXBContext().createUnmarshaller();
            boolean v2Req = isRimV2(req);
            SubmitObjectsRequest submitRequest = (SubmitObjectsRequest)
            unmarshaller.unmarshal( v2Req ? convertRimVersion(req, true) : req );
            log.info("unmarshalled SubmitObjectsRequest:"+ submitRequest);
            if ( InfoSetUtil.getRegistryPackage(submitRequest, null) == null ) {
                submitRequest = unmarshallWorkaround(req, unmarshaller);
            }
            ProvideAndRegisterDocumentSetRequestType pnr = objFac.createProvideAndRegisterDocumentSetRequestType();
            pnr.setSubmitObjectsRequest(submitRequest);
            List l = pnr.getDocument();
            Map.Entry entry;
            Document doc;
            DataHandler dh;
            for ( Iterator iter = docs.entrySet().iterator() ; iter.hasNext() ;) {
                entry = (Map.Entry)iter.next();
                dh = (DataHandler) entry.getValue();
                doc = objFac.createProvideAndRegisterDocumentSetRequestTypeDocument();
                doc.setId((String) entry.getKey());
                doc.setValue(dh);
                l.add(doc);
            }
            if (unifyUIDs)
                unifyUIDs(pnr);
            RegistryResponseType regRsp = this.exportDocuments(pnr);
            DOMResult result = new DOMResult();
            InfoSetUtil.getJAXBContext().createMarshaller().marshal(
                    objFac.createRegistryResponse(regRsp), result);
            Node nRsp = result.getNode();
            if ( v2Req ) {
                return convertRimVersion(nRsp, false);
            } else {
                return nRsp;
            }
        } catch (Throwable t) {
            throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR,"Provide And Register failed!",t);
        }
    }
    private SubmitObjectsRequest unmarshallWorkaround(Node req,
            Unmarshaller unmarshaller) throws Exception {
        SubmitObjectsRequest submitRequest;
        File errFile = new File(resolvePath("log/xds/error/xds_err_unmarshall.xml"));
        errFile.getParentFile().mkdirs();
        FileOutputStream os = new FileOutputStream(errFile);
        writeNode(req, os);
        os.close();
        log.info("Unmarshalling Error! Request saved as file ("+errFile+")!" );
        log.info("Try to unmarshall with workaround!");
        DOMResult result = new DOMResult();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.writeNode(req, baos);
        byte[] xmlData = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(xmlData);
        for ( int i = 0 ; i < xmlData.length && xmlData[i] != '<' ; bais.read(),i++ );
        convertV2V3(new StreamSource(bais), result, getV2toV3Template() );
        Node nOut = result.getNode();
        submitRequest = (SubmitObjectsRequest) unmarshaller.unmarshal(nOut);
        log.debug("unmarshalled SubmitObjectsRequest WORKAROUND:"+ submitRequest);
        log.debug("InfoSetUtil.getRegistryPackage(UUID.XDSSubmissionSet):"+InfoSetUtil.getRegistryPackage(submitRequest, UUID.XDSSubmissionSet) );
        log.info("Unmarshall with workaround success:"+InfoSetUtil.getRegistryPackage(submitRequest, UUID.XDSSubmissionSet) != null );
        return submitRequest;
    }

    public boolean exportTestFile(String filename) throws Exception {
        Unmarshaller unmarshaller = InfoSetUtil.getJAXBContext().createUnmarshaller();
        JAXBElement o = (JAXBElement) unmarshaller.unmarshal(new File(filename));
        log.debug("unmarshalled Object:"+ o);
        ProvideAndRegisterDocumentSetRequestType req = (ProvideAndRegisterDocumentSetRequestType) o.getValue();
        log.debug("unmarshalled ProvideAndRegisterDocumentSet Request:"+ req);
        unifyUIDs(req);
        RegistryResponseType rsp = exportDocuments( req );
        return this.checkResponse(rsp);
    }

    private void unifyUIDs(ProvideAndRegisterDocumentSetRequestType req) {
        SubmitObjectsRequest so = req.getSubmitObjectsRequest();
        Map eoMap = InfoSetUtil.getExtrinsicObjects(so);
        ExtrinsicObjectType eo;
        //unify all Document UIDs
        for ( Iterator iter = eoMap.values().iterator() ; iter.hasNext() ;) {
            eo = (ExtrinsicObjectType) iter.next();
            InfoSetUtil.setExternalIdentifierValue(UUID.XDSDocumentEntry_uniqueId, UIDUtils.createUID(), eo);
        }
        RegistryPackageType rp = InfoSetUtil.getRegistryPackage(so, UUID.XDSSubmissionSet);
        InfoSetUtil.setExternalIdentifierValue(UUID.XDSSubmissionSet_uniqueId, UIDUtils.createUID(), rp);
        InfoSetUtil.setExternalIdentifierValue(UUID.XDSSubmissionSet_sourceId, this.getSourceId(), rp);
    }

    public RegistryResponseType exportDocuments(ProvideAndRegisterDocumentSetRequestType req) throws XDSException {
        try {
            log.debug("------------exportDocuments");
            SubmitObjectsRequest submitRequest = req.getSubmitObjectsRequest();
            if (logRequest) {
                log.info(InfoSetUtil.getLogMessage(req));
            }
            RegistryPackageType registryPackage = InfoSetUtil.getRegistryPackage(submitRequest, UUID.XDSSubmissionSet);
            if ( registryPackage == null ) {
                log.error("No RegistryPackage found!");
                throw new XDSException( XDSConstants.XDS_ERR_REPOSITORY_ERROR, 
                        XDSConstants.XDS_ERR_MISSING_REGISTRY_PACKAGE, null);
            }
            String submissionUID = InfoSetUtil.getExternalIdentifierValue(UUID.XDSSubmissionSet_uniqueId,registryPackage); 
            String patId = InfoSetUtil.getExternalIdentifierValue(UUID.XDSSubmissionSet_patientId,registryPackage);
            log.info("SubmissionUID:"+submissionUID);
            log.info("patId:"+patId);
            httpCfgDelegate.configTLS(xdsRepositoryURI);
            DocumentRepositoryPortType port = null;
            if (useSOAP11) {
                port = DocumentRepositoryPortTypeFactory.getDocumentRepositoryPortSoap11(
                        xdsRepositoryURI, 
                        XDSConstants.URN_IHE_ITI_2007_PROVIDE_AND_REGISTER_DOCUMENT_SET_B, 
                        java.util.UUID.randomUUID().toString());
            } else {
                port = DocumentRepositoryPortTypeFactory.getDocumentRepositoryPortSoap12(
                        xdsRepositoryURI, 
                        XDSConstants.URN_IHE_ITI_2007_PROVIDE_AND_REGISTER_DOCUMENT_SET_B, 
                        java.util.UUID.randomUUID().toString());
            }
            if ( forceMTOMRequest ) {
                SOAPBinding binding = (SOAPBinding)((BindingProvider)port).getBinding();
                List<Handler> handlerChain = binding.getHandlerChain();
                if ( handlerChain == null ) {
                    handlerChain = new ArrayList();
                }
                handlerChain.add(new EnsureMtomHandler());
                binding.setHandlerChain(handlerChain);               
            }            
            log.info("####################################################");
            log.info("####################################################");
            log.info("XDS.b: Send provide and register document-b request to repository:"+xdsRepositoryURI);
            log.info("####################################################");
            log.info("####################################################");
            RegistryResponseType rsp = port.documentRepositoryProvideAndRegisterDocumentSetB(req);
            if ( this.logResponse ) {
                log.info("Received RegistryResponse:"+InfoSetUtil.marshallObject(
                        objFac.createRegistryResponse(rsp), indentXmlLog) );
            }
            boolean success = checkResponse( rsp );
            logXdsBExport(submissionUID, patId, success);
            log.info("ProvideAndRegisterDocumentSetRequest success:"+success);
            return rsp;
            /*_*/
        } catch (XDSException x) {
            throw x;
        } catch (Throwable t) {
            throw new XDSException(XDSConstants.XDS_ERR_REPOSITORY_ERROR,"Provide And Register failed!",t);
        }
    }

    private boolean checkResponse(RegistryResponseType rsp) throws Exception {
        if ( rsp == null ){
            log.error("No RegistryResponse from registry!");
            return false;
        }
        log.debug("Check RegistryResponse:"+InfoSetUtil.marshallObject(objFac.createRegistryResponse(rsp), indentXmlLog) );
        String status = rsp.getStatus();
        log.debug("Rsp status:"+status );
        return status == null ? false : XDSConstants.XDS_B_STATUS_SUCCESS.equalsIgnoreCase(rsp.getStatus());
    }

    public void testExport(String fnV2SubmReq, String fnDoc) throws Exception {
        File fSubmReq = new File(resolvePath(fnV2SubmReq));
        if (!fSubmReq.isFile()) {
            throw new FileNotFoundException(fSubmReq.getAbsolutePath());
        }
        File fDoc = new File(resolvePath(fnDoc));
        SAXTransformerFactory tf = (SAXTransformerFactory)TransformerFactory.newInstance();
        DOMResult result = new DOMResult();

        tf.newTransformer().transform(new StreamSource(fSubmReq), result);
        Node req = result.getNode();
        DataHandler dh = new DataHandler(fDoc.toURL());
        Map docs = new HashMap();
        docs.put("doc_1", dh);
        this.exportDocuments(req, docs, true);
    }
    public void convertV2toV3File(String fnSrc, String fnDst) throws Exception {
        File src = new File(resolvePath(fnSrc));
        if (!src.isFile()) {
            throw new FileNotFoundException(src.getAbsolutePath());
        }
        File dst = new File(resolvePath(fnDst));
        dst.getParentFile().mkdirs();

        convertV2V3(new StreamSource(src), new StreamResult(dst), getV2toV3Template());
    }

    public static String resolvePath(String fn) {
        File f = new File(fn);
        if (f.isAbsolute()) return f.getAbsolutePath();
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath()).getAbsolutePath();
    }

    /**
     * Create an XDS.a style audit message for XDS-I PnR export.
     * <p>
     * This is used from XDSIService (part of archive) via jmx call for audit log!
     * </p><p>
     * Therefore the alternate user id is the list of locale AET's.
     * 
     * @param submissionUID
     * @param patId
     * @param success
     */
    public void logExport(String submissionUID, String patId, boolean success) {
        try {
            HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
            String user = userInfo.getUserId();
            XDSExportMessage msg = XDSExportMessage.createDocumentSourceExportMessage(submissionUID, patId);
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
                host = new URL(xdsRepositoryURI).getHost();
            } catch (MalformedURLException ignore) {
            }
            msg.setDestination(xdsRepositoryURI, null, null, host, false );
            msg.validate();
            Logger.getLogger("auditlog").info(msg);
        } catch ( Throwable t ) {
            log.warn("Audit Log (Export) failed! Ignored!",t);
        }
    }

    public void logXdsBExport(String submissionUID, String patId, boolean success) {
        try {
            HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
            String user = userInfo.getUserId();
            XDSExportMessage msg = XDSExportMessage.createDocumentSourceBExportMessage(submissionUID, patId);
            msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
                AuditEvent.OutcomeIndicator.MINOR_FAILURE);
            //TODO: get replyTo from real WS Addressing Header
            String replyTo = AddressingBuilder.getAddressingBuilder().newAddressingConstants().getAnonymousURI();
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
                host = new URL(xdsRepositoryURI).getHost();
            } catch (MalformedURLException ignore) {
            }
            msg.setDestination(xdsRepositoryURI, null, null, host, false );
            msg.validate();
            Logger.getLogger("auditlog").info(msg);
        } catch ( Throwable t ) {
            log.warn("Audit Log (Export) failed! Ignored!",t);
        }
    }
    
    public void logNode(String msg, Node node) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(msg.getBytes());
            writeNode( node, out );
            log.info(out.toString());
        } catch (Exception e) {
            log.warn("Failed to log Node "+node, e);
        }
    }
    public void writeNode(Node node, OutputStream out) {
        try {
            Source s = new DOMSource(node);
            out.write('\n');
            Transformer t = TransformerFactory.newInstance().newTransformer();
            if (indentXmlLog)
                t.setOutputProperty("indent", "yes");
            t.transform(s, new StreamResult(out));
        } catch (Exception e) {
            log.warn("Failed to log Node "+node, e);
        }
        /*_*/        
    }

}
