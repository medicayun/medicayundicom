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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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

import org.apache.log4j.Logger;
import org.dcm4chee.xds.common.delegate.XdsHttpCfgDelegate;
import org.dcm4chee.xds.infoset.v30.AdhocQueryRequest;
import org.dcm4chee.xds.infoset.v30.AdhocQueryResponse;
import org.dcm4chee.xds.infoset.v30.DocumentRegistryPortType;
import org.dcm4chee.xds.infoset.v30.DocumentRegistryService;
import org.dcm4chee.xds.infoset.v30.util.InfoSetUtil;
import org.dcm4chee.xds.infoset.v30.util.StoredQueryFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;
import org.w3c.dom.Node;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 6078 $ $Date: 2008-02-25 14:30:10 +0100 (Mo, 25 Feb 2008) $
 * @since Mar, 2007
 */
public class XDSQueryService extends ServiceMBeanSupport {

    private static Logger log = Logger.getLogger(XDSQueryService.class);

//  http attributes to document registry actor (synchron) 	
    private String xdsQueryURI;

    private ObjectName pixQueryServiceName;

    private String affinityDomain;

    private boolean useSoap;
    
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
        return performQuery( queryFac.createFindDocumentsRequest(getAffinityDomainPatientID(patId), getListString(status), useLeafClass) );
    }

    public AdhocQueryResponse findFolders(String patId, String status,
            boolean useLeafClass) throws SOAPException, JAXBException {
        return performQuery( queryFac.createFindFoldersRequest(
                getAffinityDomainPatientID(patId), getListString(status), useLeafClass));
    }

    public AdhocQueryResponse findSubmissionSets(String patId, String status, boolean useLeafClass)
    throws SOAPException, JAXBException {
        return performQuery(queryFac.createFindSubmissionSetsRequest(
                getAffinityDomainPatientID(patId), getListString(status), useLeafClass));
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
        return performQuery(queryFac.createGetAllRequest(
                getAffinityDomainPatientID(patId), getListString(docStatus), getListString(submissionSetStatus), getListString(folderStatus)));

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
        if (logRequestMessage) {
            log.info("AdhocQueryRequest:"+InfoSetUtil.marshallObject(rq, true));
        }
        AdhocQueryResponse rsp = useSoap ? performQueryViaSoap(rq) : performQueryViaWS(rq);
        if (logResponseMessage) {
            log.info("AdhocQueryResponse:"+InfoSetUtil.marshallObject(rsp, true));
        }
        return rsp;
    }

    public AdhocQueryResponse performQueryViaWS(AdhocQueryRequest rq) {
        DocumentRegistryService s = new DocumentRegistryService();
        DocumentRegistryPortType port = s.getDocumentRegistryPortSoap12();
        BindingProvider bindingProvider = (BindingProvider)port;
        Map<String, Object> reqCtx = bindingProvider.getRequestContext();
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
        httpCfgDelegate.configTLS(xdsQueryURI);
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
    private boolean isFromDomain(String pid) {
        int pos = 0;
        for ( int i = 0 ; i < 3 ; i++) {
            pos = pid.indexOf('^', pos);
            if ( pos == -1 ) {
                log.warn("patient id does not contain domain (issuer)! :"+pid);
                return false;
            }
            pos++;
        }
        return pid.substring(pos).equals(this.affinityDomain);
    }

    private String toPIDString(String[] pid) {
        if (pid == null || pid.length < 1) return "";
        StringBuffer sb = new StringBuffer(pid[0]);
        log.debug("pid[0]:"+pid[0]);
        if ( pid.length > 1 ) {
            sb.append("^^^").append(pid[1]);
            log.debug("pid[1]:"+pid[1]);
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
}
