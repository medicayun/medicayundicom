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
package org.dcm4chee.xds.consumer.query;

import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.xml.bind.JAXBException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.audit.HttpUserInfo;
import org.dcm4chee.xds.common.audit.XDSQueryMessage;
import org.dcm4chee.xds.common.delegate.XdsHttpCfgDelegate;
import org.dcm4chee.xds.infoset.v30.AdhocQueryRequest;
import org.dcm4chee.xds.infoset.v30.AdhocQueryResponse;
import org.dcm4chee.xds.infoset.v30.util.InfoSetUtil;
import org.dcm4chee.xds.infoset.v30.util.StoredQueryFactory;
import org.dcm4chee.xds.infoset.v30.ws.DocumentRegistryPortType;
import org.dcm4chee.xds.infoset.v30.ws.DocumentRegistryPortTypeFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.ws.core.ConfigProvider;
import org.jboss.ws.core.utils.UUIDGenerator;
import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;
import org.w3c.dom.Node;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 6078 $ $Date: 2008-02-25 14:30:10 +0100 (Mo, 25 Feb 2008) $
 * @since Mar, 2007
 */
public class XDSQueryService extends ServiceMBeanSupport {

    private static final String STANDARD_SOAP_1_2_WSADDRESSING_CLIENT = "Standard SOAP 1.2 WSAddressing Client";

    private static Logger log = Logger.getLogger(XDSQueryService.class);

//  http attributes to document registry actor (synchron) 	
    private String xdsQueryURI;

    private ObjectName pixQueryServiceName;

    private String affinityDomain;

    private boolean useSoap;
    private boolean useInfoset;
    private boolean useWSAddressHandler;
    
    private XdsHttpCfgDelegate httpCfgDelegate = new XdsHttpCfgDelegate();

    private StoredQueryFactory queryFac = new StoredQueryFactory();

    private boolean logResponseMessage;

    private boolean logRequestMessage;
    
    public XDSQueryService() {
    }
//  http

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

    public String getAffinityDomain() {
        return affinityDomain;
    }

    public void setAffinityDomain(String affinityDomain) {
        this.affinityDomain = affinityDomain.trim();
    }

    public boolean isUseSoap() {
        return useSoap;
    }
    public void setUseSoap(boolean useSoap) {
        this.useSoap = useSoap;
    }
    public boolean isUseInfoset() {
        return useInfoset;
    }
    public void setUseInfoset(boolean b) {
        this.useInfoset = b;
    }
    public boolean isUseWSAddressHandler() {
        return useWSAddressHandler;
    }
    public void setUseWSAddressHandler(boolean b) {
        this.useWSAddressHandler = b;
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
    
    public final ObjectName getPixQueryServiceName() {
        return pixQueryServiceName;
    }

    public final void setPixQueryServiceName(ObjectName name) {
        this.pixQueryServiceName = name;
    }

    public AdhocQueryResponse findDocuments(String patId, String status,
            boolean useLeafClass) throws SOAPException, JAXBException {
        patId = getAffinityDomainPatientID(patId);
        return patId == null ? null : performQuery( queryFac.createFindDocumentsRequest(patId, getListString(status), useLeafClass) );
    }

    public AdhocQueryResponse findFolders(String patId, String status,
            boolean useLeafClass) throws SOAPException, JAXBException {
        patId = getAffinityDomainPatientID(patId);
        return patId == null ? null : performQuery( queryFac.createFindFoldersRequest(patId, getListString(status), useLeafClass));
    }

    public AdhocQueryResponse findSubmissionSets(String patId, String status, boolean useLeafClass)
    throws SOAPException, JAXBException {
        patId = getAffinityDomainPatientID(patId);
        return patId == null ? null : performQuery(queryFac.createFindSubmissionSetsRequest(patId, getListString(status), useLeafClass));
    }

    public String findAsXML(String cmd, String patId, String status,
            boolean useLeafClass, boolean encodeHtml) throws SOAPException, JAXBException, InstanceNotFoundException, MBeanException, ReflectionException {
        AdhocQueryResponse rsp = (AdhocQueryResponse) server.invoke(serviceName, cmd, 
                new Object[]{patId, status, new Boolean(useLeafClass)},
                new String[]{String.class.getName(), String.class.getName(), "boolean"});
        String xml = InfoSetUtil.marshallObject(rsp, true);
        return encodeHtml ? encodeHTML(xml) : xml;
    }
    
    public AdhocQueryResponse getAll(String patId, String docStatus,
            String submissionSetStatus, String folderStatus)
    throws SOAPException, JAXBException {
        patId = getAffinityDomainPatientID(patId);
        return patId == null ? null : performQuery(queryFac.createGetAllRequest(patId, getListString(docStatus), getListString(submissionSetStatus), getListString(folderStatus)));

    }

    public AdhocQueryResponse getDocuments(String uuids) throws SOAPException,
    JAXBException {
        return getDocuments(getListString(uuids));
    }

    public AdhocQueryResponse getDocuments(List uuids) throws SOAPException, JAXBException {
        return performQuery(queryFac.createGetDocumentsRequest(uuids));
    }

    public AdhocQueryResponse getFolders(String uuids) throws SOAPException,
    JAXBException {
        return getFolders(getListString(uuids));
    }

    public AdhocQueryResponse getFolders(List uuids) throws SOAPException,
    JAXBException {
        return performQuery(queryFac.createGetFoldersQuery(uuids));
    }

    public AdhocQueryResponse getAssociations(String uuids)
    throws SOAPException, JAXBException {
        return getAssociations(getListString(uuids));
    }

    public AdhocQueryResponse getAssociations(List uuids) throws SOAPException,
    JAXBException {
        return performQuery(queryFac.createGetAssociationsRequest(uuids));
    }

    public AdhocQueryResponse getDocumentsAndAssocs(String uuids)
    throws SOAPException, JAXBException {
        return getDocumentsAndAssocs(getListString(uuids));
    }

    public AdhocQueryResponse getDocumentsAndAssocs(List uuids)
    throws SOAPException, JAXBException {
        return performQuery(queryFac.createGetDocumentsAndAssocsRequest(uuids));
    }

    public AdhocQueryResponse getSubmissionSets(String uuids)
    throws SOAPException, JAXBException {
        return getSubmissionSets(getListString(uuids));
    }

    public AdhocQueryResponse getSubmissionSets(List uuids)
    throws SOAPException, JAXBException {
        return performQuery(queryFac.createGetSubmissionSetsRequest(uuids));
    }

    public AdhocQueryResponse getSubmissionSetAndContents(String uuid)
    throws SOAPException, JAXBException {
        return performQuery(queryFac.createGetSubmissionSetAndContentsRequest(uuid));
    }

    public AdhocQueryResponse getFolderAndContents(String uuid)
    throws SOAPException, JAXBException {
        return performQuery(queryFac.createGetFolderAndContentsRequest(uuid));
    }

    public AdhocQueryResponse getFoldersForDocument(String uuid)
    throws SOAPException, JAXBException {
        return performQuery(queryFac.createGetFoldersForDocumentRequest(uuid));
    }

    public String getAsXML(String cmd, String uuid, boolean encodeHtml) throws SOAPException, JAXBException, InstanceNotFoundException, MBeanException, ReflectionException {
        AdhocQueryResponse rsp = (AdhocQueryResponse) server.invoke(serviceName, cmd, 
                new Object[]{uuid},
                new String[]{String.class.getName()});
        String xml = InfoSetUtil.marshallObject(rsp, true);
        return encodeHtml ? encodeHTML(xml) : xml;
    }
    
    public AdhocQueryResponse getRelatedDocuments(String uuid, String assocTypes)
    throws SOAPException, JAXBException {
        return getRelatedDocuments(uuid, getListString(assocTypes));
    }

    public AdhocQueryResponse getRelatedDocuments(String uuid)
    throws SOAPException, JAXBException {
        return performQuery(queryFac.createRelatedDocumentsRequest(uuid, null));
    }

    public AdhocQueryResponse getRelatedDocuments(String uuid, List assocTypes)
    throws SOAPException, JAXBException {
        return performQuery(queryFac.createRelatedDocumentsRequest(uuid, assocTypes));
    }

    private ArrayList<String> getListString(String uuids) {
        if ( uuids == null ) return null;
        StringTokenizer st = new StringTokenizer(uuids,",");
        ArrayList<String> l = new ArrayList<String>();
        while ( st.hasMoreTokens() ) {
            l.add( st.nextToken().trim() );
        }
        return l;
    }
    
    public String testQueryRequest( String fn ) throws JAXBException, SOAPException {
        AdhocQueryRequest rq = (AdhocQueryRequest) InfoSetUtil.unmarshal(new File(fn));
        AdhocQueryResponse rsp = performQuery(rq);
        return InfoSetUtil.marshallObject(rsp, true);
    }
    
    public AdhocQueryResponse performQuery(AdhocQueryRequest rq) throws SOAPException, JAXBException {
        if ( rq == null )
            return null;
        String queryRequest = InfoSetUtil.marshallObject(rq, true);
        String queryId = rq.getAdhocQuery().getId();
        String patId = InfoSetUtil.getSlotValue(rq.getAdhocQuery().getSlot(), StoredQueryFactory.QRY_DOCUMENT_ENTRY_PATIENT_ID, null);
        if (logRequestMessage) {
            log.info("AdhocQueryRequest:"+queryRequest);
        }
        httpCfgDelegate.configTLS(xdsQueryURI);
        AdhocQueryResponse rsp = useSoap ? performQueryViaSoap(rq) : 
            useInfoset ? performQueryViaWSInfoset(rq) : performQueryViaWS(rq);
        if (logResponseMessage) {
            log.info("AdhocQueryResponse:"+InfoSetUtil.marshallObject(rsp, true));
        }
        logQuery(queryId, patId, queryRequest, checkResponse(rsp));
        return rsp;
    }

    public AdhocQueryResponse performQueryViaWS(AdhocQueryRequest rq) {
        DocumentRegistryPortType port = DocumentRegistryPortTypeFactory.getDocumentRegistryPortSoap12(xdsQueryURI,
        		XDSConstants.URN_IHE_ITI_2007_REGISTRY_STORED_QUERY, UUID.randomUUID().toString());
        return port.documentRegistryRegistryStoredQuery(rq);
    }
    
    public AdhocQueryResponse performQueryViaWSInfoset(AdhocQueryRequest rq) {
        DocumentRegistryPortType port = DocumentRegistryPortTypeFactory.getDocumentRegistryPortSoap12();
        BindingProvider bindingProvider = (BindingProvider)port;
        Map<String, Object> reqCtx = bindingProvider.getRequestContext();
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        AddressingProperties addrProps = builder.newAddressingProperties();
        try {
            addrProps.setTo(builder.newURI(xdsQueryURI));
            addrProps.setAction(builder.newURI(XDSConstants.URN_IHE_ITI_2007_REGISTRY_STORED_QUERY));
            addrProps.setMessageID(builder.newURI("urn:uuid:"+ UUIDGenerator.generateRandomUUIDString()));
        } catch (URISyntaxException x) {
            log.error("Failed to set AddressingProperties!",x);
        }
        reqCtx.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addrProps);
        if ( useWSAddressHandler ) {
            List<Handler> customHandlerChain = new ArrayList<Handler>();
            customHandlerChain.add(new WSAddressingClientHandler());
            SOAPBinding soapBinding = (SOAPBinding)bindingProvider.getBinding();
            soapBinding.setHandlerChain(customHandlerChain);        
        } else {
            ((ConfigProvider) port).setConfigName( STANDARD_SOAP_1_2_WSADDRESSING_CLIENT);
        }
        reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, xdsQueryURI);
        
        return port.documentRegistryRegistryStoredQuery(rq);
    }
    
    public AdhocQueryResponse performQueryViaSoap(AdhocQueryRequest rq) throws SOAPException, JAXBException {
        log.debug("Create SOAP Connection for Adhoc Query");
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage requestMsg = messageFactory.createMessage();
        SOAPEnvelope envelope = requestMsg.getSOAPPart().getEnvelope();
        SOAPBody soapBody = envelope.getBody();
        soapBody.addDocument(InfoSetUtil.getDocumentForObject(rq));
        logMessage("Request: ", requestMsg);
        log.debug("Query URI: " + xdsQueryURI);
        SOAPConnectionFactory connFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection conn = null;
        try {
            conn = connFactory.createConnection();
            SOAPMessage responseMsg = conn.call(requestMsg, xdsQueryURI);
            logMessage("Response :", responseMsg);
            SOAPBody body = responseMsg.getSOAPBody();
            Node node = body.getFirstChild();
            if (node != null
                    && node.getLocalName().equals("AdhocQueryResponse")) {
                return (AdhocQueryResponse) InfoSetUtil.node2Object(node);
            }
            if (node == null) {
                log.error("SOAP Message returns null content.");
            } else {
                log.error("Instead of AdhocQueryResponse "
                        + node.getLocalName() + " is returned.");
            }
            return null;
        } catch (SOAPException soapex) {
            log.error("Error in SOAP process: ", soapex);
            throw soapex;
        } catch (JAXBException jaxbex) {
            log.error("Error in XML binding: ", jaxbex);
            throw jaxbex;
        } catch (Exception ex) {
            log.error("Other error: ", ex);
            throw new SOAPException(ex);
        } finally {
            if (conn != null)
                conn.close();
        }
    }
 
    private void logMessage(String title, SOAPMessage soap) {
        if (log.isDebugEnabled()) {
            try {
                Transformer tf = TransformerFactory.newInstance().newTransformer();
                tf.setOutputProperty(OutputKeys.INDENT, "yes");
                StringWriter sw = new StringWriter();
                tf.transform(soap.getSOAPPart().getContent(), new StreamResult(sw) );
                log.debug(title + sw.toString());
            } catch (Exception ex) {
                // Ignore here
            }
        }
    }
    
    protected String getAffinityDomainPatientID(String patId) {
        if ( affinityDomain.charAt(0) == '=') {
            if ( affinityDomain.length() == 1 ) {
                log.info("PIX Query disabled: use patId: "+patId);
                return patId;
            } else if (affinityDomain.charAt(1)=='?') {
                log.info("PIX Query disabled: replace issuer with affinity domain! orig PatId:"+patId);
                int pos = patId.indexOf("^^^");
                if ( pos != -1 ) patId = patId.substring(0,pos);
                log.debug("patID changed! new patId:"+patId+"^^^"+affinityDomain.substring(2)+")");
                return patId+"^^^"+affinityDomain.substring(2);
            } else {
                log.info("PIX Query disabled: replace configured patient ID! :"+affinityDomain.substring(1));
                return affinityDomain.substring(1);
            }
        }
        if ( this.pixQueryServiceName == null ) {
            log.info("PIX Query disabled: use source patient ID!");
            return patId+"^^^"+affinityDomain;
        } else {
            try {
                List<String[]> pids = (List<String[]>) server.invoke(this.pixQueryServiceName,
                        "queryCorrespondingPIDs",
                        new Object[] { patId, "", new String[]{affinityDomain} },
                        new String[] { String.class.getName(), String.class.getName(), String[].class.getName() });
                String pid;
                for ( String[] pidElems : pids ) {
                    pid = toPIDString(pidElems);
                    if ( isFromDomain(pid) ) {
                        return pid;
                    }
                }
                log.error("Patient ID is not known in Affinity domain:"+affinityDomain);
                return null;
            } catch (Exception e) {
                log.error("Failed to get patientID for Affinity Domain:", e);
                return null;
            }
        }
    }
    protected boolean isFromDomain(String pid) {
        String assAuth = getAssigningAuthority(pid);
        if (assAuth == null || assAuth.indexOf('&') == -1)
            return false;
        if (assAuth.charAt(0) != '&') { //is namespace id subcomponent not empty?
            //we only compare <universal ID> and <universal ID type
            //leave subcomponent delimiter! (affinityDomain will almost always have no namespace id)
            assAuth = assAuth.substring(assAuth.indexOf('&'));
        }
        if (affinityDomain.charAt(0) == '&') {
            return assAuth.equals(affinityDomain);
        } else { //affinityDomain has namespace id but we ignore that!
            return assAuth.equals(affinityDomain.substring(affinityDomain.indexOf('&')));
        }
    }

    private String getAssigningAuthority(String pid) {
        int pos = 0;
        for ( int i = 0 ; i < 3 ; i++) {
            pos = pid.indexOf('^', pos);
            if ( pos == -1 ) {
                log.warn("patient id does not contain AssigningAuthority component! :"+pid);
                return null;
            }
            pos++;
        }
        int end = pid.indexOf('^', pos);
        return end == -1 ? pid.substring(pos) : pid.substring(pos, end);
    }

    protected String toPIDString(String[] pid) {
        if (pid == null || pid.length < 1) return "";
        StringBuffer sb = new StringBuffer(pid[0]);
        log.debug("pid[0]:"+pid[0]);
        if ( pid.length > 1 ) {
            sb.append("^^^");
            log.debug("pid[1]:"+pid[1]+" (excluded in returned string)");
        }
        for (int i = 2 ; i < pid.length; i++) {
            sb.append('&').append(pid[i]);
            log.debug("pid["+i+"]:"+pid[i]);
        }
        return sb.toString();
    }

    public static String resolvePath(String fn) {
        File f = new File(fn);
        if (f.isAbsolute()) return f.getAbsolutePath();
        File serverHomeDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(serverHomeDir, f.getPath()).getAbsolutePath();
    }

    public static String encodeHTML(String s) {
        StringBuffer sb = new StringBuffer();
        char c;
        for(int i=0 ; i<s.length() ; i++) {
            c = s.charAt(i);
            if(c > 127 || c=='"' || c=='<' || c=='>') {
                sb.append("&#"+(int)c+";");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean checkResponse(AdhocQueryResponse rsp) throws JAXBException {
        if ( rsp == null ){
            log.error("No AdhocQueryResponse from registry!");
            return false;
        }
        log.debug("Check AdhocQueryResponse:"+InfoSetUtil.marshallObject(rsp, true) );
        String status = rsp.getStatus();
        log.debug("Rsp status:"+status );
        return status == null ? false : XDSConstants.XDS_B_STATUS_SUCCESS.equalsIgnoreCase(rsp.getStatus());
    }
    
    private void logQuery(String queryId, String patId, String queryRequest, boolean success) {
        try {
            HttpUserInfo userInfo = new HttpUserInfo(AuditMessage.isEnableDNSLookups());
            String user = userInfo.getUserId();
            //TODO: get replyTo from real WS Addressing Header
            String replyTo = AddressingBuilder.getAddressingBuilder().newAddressingConstants().getAnonymousURI();
            XDSQueryMessage msg = XDSQueryMessage.createConsumerStoredQueryMessage(replyTo, success, true);
            if (user != null) {
                msg.setHumanRequestor(user, null, null, true);
            }
            String host = "unknown";
            try {
                host = new URL(xdsQueryURI).getHost();
            } catch (MalformedURLException ignore) {
            }
            msg.setDestination(xdsQueryURI, null, null, host, false );
            if ( patId != null ) {
                msg.setPatient(patId, null);
            }
            msg.addQuery(queryId, queryRequest);
            msg.validate();
            Logger.getLogger("auditlog").info(msg);
        } catch ( Throwable t ) {
            log.warn("Audit Log (Query) failed! Ignored!",t);
        }
    }
}
