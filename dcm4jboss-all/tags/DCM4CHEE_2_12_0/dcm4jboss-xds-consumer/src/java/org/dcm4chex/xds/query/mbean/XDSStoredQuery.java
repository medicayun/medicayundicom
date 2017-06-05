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
package org.dcm4chex.xds.query.mbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryService;
import javax.xml.soap.SOAPException;

import org.apache.log4j.Logger;
import org.freebxml.omar.client.xml.registry.BulkResponseImpl;
import org.freebxml.omar.client.xml.registry.ConnectionImpl;
import org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.QueryImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponseType;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.Slot;
import org.oasis.ebxml.registry.bindings.rim.Value;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;
import org.oasis.ebxml.registry.bindings.rim.impl.ValueImpl;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 4729 $ $Date: 2007-07-24 08:42:12 +0800 (周二, 24 7月 2007) $
 * @since Mar, 2007
 */
public class XDSStoredQuery {

	private static Logger log = Logger.getLogger(XDSStoredQuery.class.getName());

    private static BindingUtility bu = BindingUtility.getInstance();        

    public static final String STORED_QUERY_FIND_DOCUMENTS = "urn:uuid:14d4debf-8f97-4251-9a74-a90016b0af0d";
    public static final String STORED_QUERY_FIND_SUBMISSIONSETS = "urn:uuid:f26abbcb-ac74-4422-8a30-edb644bbc1a9";
    public static final String STORED_QUERY_FIND_FOLDERS = "urn:uuid:958f3006-baad-4929-a4de-ff1114824431";
    public static final String STORED_QUERY_GET_ALL = "urn:uuid:10b545ea-725c-446d-9b95-8aeb444eddf3";
    public static final String STORED_QUERY_GET_DOCUMENTS = "urn:uuid:5c4f972b-d56b-40ac-a5fc-c8ca9b40b9d4";
    public static final String STORED_QUERY_GET_FOLDERS = "urn:uuid:5737b14c-8a1a-4539-b659-e03a34a5e1e4";
    public static final String STORED_QUERY_GET_ASSOCIATIONS = "urn:uuid:a7ae438b-4bc2-4642-93e9-be891f7bb155";
    public static final String STORED_QUERY_GET_DOC_AND_ASSOC = "urn:uuid:bab9529a-4a10-40b3-a01f-f68a615d247a";
    public static final String STORED_QUERY_GET_SUBMISSIONSETS = "urn:uuid:51224314-5390-4169-9b91-b1980040715a";
    public static final String STORED_QUERY_GET_SUBMISSIONSETS_AND_CONTENT = "urn:uuid:e8e3cb2c-e39c-46b9-99e4-c12f57260b83";
    public static final String STORED_QUERY_GET_FOLDER_AND_CONTENT = "urn:uuid:b909a503-523d-4517-8acf-8e5834dfc4c7";
    public static final String STORED_QUERY_GET_FOLDER_FOR_DOC = "urn:uuid:10cae35a-c7f9-4cf5-b61e-fc3278ffb578";
    public static final String STORED_QUERY_GET_RELATED_DOCS = "urn:uuid:d90e5407-b356-4d91-a89f-873917b4b0e6";
    
    public static final String V3_STATUS_PREFIX = "urn:oasis:names:tc:ebxml-regrep:StatusType:";
    public static final String V3_STATUS_SUBMITTED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted";
    public static final String V3_STATUS_APPROVED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved";
    public static final String V3_STATUS_DEPRECATED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Deprecated";
    
    private XDSQueryService service;
	public XDSStoredQuery(XDSQueryService service) {
        this.service = service;
	}

    public BulkResponse findDocuments(String patId, String status, boolean useLeafClass) throws SOAPException {
        HashMap map = new HashMap();
        map.put("$XDSDocumentEntryPatientId", patId);
        ArrayList l = getStatus(status);
        map.put("$XDSDocumentEntryStatus", l);
        log.info("---- findDocuments called ----");
        ReturnType retType = useLeafClass ? ReturnType.LEAF_CLASS : ReturnType.OBJECT_REF;
        return performStoredQuery( STORED_QUERY_FIND_DOCUMENTS, map, retType);
    }

    public BulkResponse findFolders(String patId, String status, boolean useLeafClass) throws SOAPException {
        HashMap map = new HashMap();
        map.put("$XDSFolderPatientId", patId);
        ArrayList l = getStatus(status);
        map.put("$XDSFolderStatus", l);
        log.info("---- findFolders called ----");
        ReturnType retType = useLeafClass ? ReturnType.LEAF_CLASS : ReturnType.OBJECT_REF;
        return performStoredQuery( STORED_QUERY_FIND_FOLDERS, map, retType);
    }

    public BulkResponse findSubmissionSets(String patId, String status) throws SOAPException {
        HashMap map = new HashMap();
        map.put("$XDSSubmissionSetPatientId", patId);
        ArrayList l = getStatus(status);
        map.put("$XDSSubmissionSetStatus", l);
        log.info("---- findSubmissionSets called ----");
        return performStoredQuery( STORED_QUERY_FIND_SUBMISSIONSETS, map, ReturnType.OBJECT_REF);
    }

    public BulkResponse getAll(String patId, String docStatus, String submissionSetStatus, String folderStatus) throws SOAPException {
        HashMap map = new HashMap();
        map.put("$patientId", patId);
        map.put("$XDSDocumentEntryStatus", getStatus(docStatus));
        map.put("$XDSSubmissionSetStatus", getStatus(submissionSetStatus));
        map.put("$XDSFolderStatus", getStatus(folderStatus));
        log.info("---- getAll called ----");
        return performStoredQuery( STORED_QUERY_GET_ALL, map, ReturnType.LEAF_CLASS);
    }
    
    public BulkResponse getDocuments(List uuids) throws SOAPException {
        Map map = new HashMap();
        map.put("$XDSDocumentEntryEntryUUID", uuids);
        log.info("---- getDocuments called ----");
        return performStoredQuery( STORED_QUERY_GET_DOCUMENTS, map, ReturnType.LEAF_CLASS);
    }
    public BulkResponse getFolders(List uuids) throws SOAPException {
        Map map = new HashMap();
        map.put("$XDSFolderEntryUUID", uuids);
        log.info("---- getFolders called ----");
        return performStoredQuery( STORED_QUERY_GET_FOLDERS, map, ReturnType.LEAF_CLASS);
    }

    public BulkResponse getAssociations(List uuids) throws SOAPException {
        Map map = new HashMap();
        map.put("$uuid", uuids);
        log.info("---- getAssociations called ----");
        return performStoredQuery( STORED_QUERY_GET_DOC_AND_ASSOC, map, ReturnType.LEAF_CLASS);
    }

    public BulkResponse getDocumentsAndAssocs(List uuids) throws SOAPException {
        Map map = new HashMap();
        map.put("$XDSDocumentEntryEntryUUID", uuids);
        log.info("---- getDocumentsAndAssocs called ----");
        return performStoredQuery( STORED_QUERY_GET_DOC_AND_ASSOC, map, ReturnType.OBJECT_REF);
    }

    public BulkResponse getSubmissionSets(List uuids) throws SOAPException {
        Map map = new HashMap();
        map.put("$uuid", uuids);
        log.info("---- getSubmissionSets called ----");
        return performStoredQuery( STORED_QUERY_GET_SUBMISSIONSETS, map, ReturnType.LEAF_CLASS);
    }
    
    
    public BulkResponse getSubmissionSetAndContents(String uuid) throws SOAPException {
        Map map = new HashMap();
        map.put("$XDSSubmissionSetEntryUUID", uuid);
        log.info("---- getSubmissionSetAndContents called ----");
        return performStoredQuery( STORED_QUERY_GET_SUBMISSIONSETS_AND_CONTENT, map, ReturnType.OBJECT_REF);
    }
    
    public BulkResponse getFolderAndContents(String uuid) throws SOAPException {
        Map map = new HashMap();
        map.put("$XDSFolderEntryUUID", uuid);
        log.info("---- getFolderAndContents called ----");
        return performStoredQuery( STORED_QUERY_GET_FOLDER_AND_CONTENT, map, ReturnType.OBJECT_REF);
    }

    public BulkResponse getFoldersForDocument(String uuid) throws SOAPException {
        Map map = new HashMap();
        map.put("$XDSDocumentEntryEntryUUID", uuid);
        log.info("---- getFoldersForDocument called ----");
        return performStoredQuery( STORED_QUERY_GET_FOLDER_FOR_DOC, map, ReturnType.OBJECT_REF);
    }

    public BulkResponse getRelatedDocuments(String uuid, List assocTypes) throws SOAPException {
        Map map = new HashMap();
        map.put("$XDSDocumentEntryEntryUUID", uuid);
        map.put("$AssociationTypes", assocTypes);
        log.info("---- GetRelatedDocuments called ----");
        return performStoredQuery( STORED_QUERY_GET_RELATED_DOCS, map, ReturnType.OBJECT_REF);
    }

    public BulkResponse performStoredQuery(String queryId, Map queryParams, ReturnType type ) {
        try {
            String xdsQueryURI = service.getXDSQueryURI();
            log.info("Send query request to "+xdsQueryURI+
                    " (proxy:"+service.getProxyHost()+":"+service.getProxyPort()+")");
            service.configProxyAndTLS(xdsQueryURI);
            ProviderProperties.getInstance().put("javax.xml.registry.queryManagerURL", xdsQueryURI);
            ConnectionFactory connFactory = JAXRUtility.getConnectionFactory();
            ConnectionImpl connection = (ConnectionImpl) connFactory.createConnection();
            RegistryService service = connection.getRegistryService();
            BusinessQueryManager bqm = service.getBusinessQueryManager();
            DeclarativeQueryManagerImpl dqm = (org.freebxml.omar.client.xml.registry.DeclarativeQueryManagerImpl)service.getDeclarativeQueryManager();
            AdhocQueryRequest req = bu.queryFac.createAdhocQueryRequest();
            req.setId(org.freebxml.omar.common.Utility.getInstance().createId());
            AdhocQueryType adhocQuery = bu.rimFac.createAdhocQuery();
            adhocQuery.setId(queryId);
            addQueryParameters(adhocQuery.getSlot(), queryParams);
            req.setAdhocQuery(adhocQuery);
            ResponseOption ro = bu.queryFac.createResponseOption();
            ro.setReturnComposedObjects(true);
            ro.setReturnType(type);
            req.setResponseOption(ro);        
            Query query = new QueryImpl( req );
            log.info("req getResponseOption:"+req.getResponseOption());
            Date date = new Date();
            BulkResponse resp = dqm.executeQuery(query);
            if ( resp.getStatus() == JAXRResponse.STATUS_SUCCESS 
                    && req.getResponseOption().getReturnType() == ReturnType.OBJECT_REF ) {
                processObjectRefResponse(resp);
            }
            log.info("-------------------------------------------------");
            log.info("Performed Stored Query at "+date);
            log.info("RequestID:"+resp.getRequestId());
            log.info("ReturnType:"+type.getValue());
            log.info("Query ID:"+queryId);
            log.info("Query Parameters:"+queryParams);
            log.info("BulkResponse status:"+resp.getStatus());
            log.info("BulkResponse collection:"+resp.getCollection());
            log.info("BulkResponse exceptions:"+resp.getExceptions());
            log.info("-------------------------------------------------");
            return resp;
        } catch (JAXRException e) {
            log.error("JAXR Exception in performStoredQuery: message:"+e.getMessage());
        } catch (Exception e) {
            log.error("Exception in performStoredQuery: message:"+e.getMessage());
        }
        return null;
    }
    private void processObjectRefResponse(BulkResponse resp) throws JAXRException {
        log.debug("Process BulkResponse for ObjectRef ResultType!");
        RegistryResponseType regResp = ((BulkResponseImpl) resp).getRegistryResponse();
        Collection col = resp.getCollection();
        if (regResp instanceof AdhocQueryResponseType) {
            AdhocQueryResponseType aqr = (AdhocQueryResponseType) regResp;
            RegistryObjectListType queryResult = aqr.getRegistryObjectList();
            Iterator iter = queryResult.getIdentifiable().iterator();
            IdentifiableType obj;
            while (iter.hasNext()) {
                obj = (IdentifiableType) iter.next();
                if (obj instanceof ObjectRefType) {
                    col.add( ((ObjectRefType) obj).getId() );
                } else {
                    log.warn("Found wrong object type in response with return type OBJECT_REF! Ignored:"+obj);
                }
            }
        }
    }

    private void addQueryParameters(List slots, Map queryParams) throws JAXBException {
        Map.Entry e;
        Object o; 
        StringBuffer sb = new StringBuffer();
        for ( Iterator iter = queryParams.entrySet().iterator() ; iter.hasNext() ; ) {
            e = (Map.Entry) iter.next();
            sb.setLength(0);
            if ( (o = e.getValue()) instanceof List ) {
                Iterator it = ((List)o).iterator();
                if ( it.hasNext() ) {
                    sb.append("(").append( getQueryValue( it.next() ) );
                    for ( ; it.hasNext() ; ) {
                        sb.append(",").append( getQueryValue( it.next() ) );
                    }
                    sb.append(")");
                    slots.add( getSlot( (String) e.getKey(), sb.toString() ) );
                }
            } else {
                slots.add( getSlot( (String) e.getKey(), getQueryValue(o) ) );
            }
        }
    }
    
    private String getQueryValue( Object o ) {
        return ( o instanceof Number ) ? o.toString() : "'"+o.toString()+"'";
    }
    
    private Slot getSlot(String name, String value) throws JAXBException {
        org.oasis.ebxml.registry.bindings.rim.Slot slot = bu.rimFac.createSlot();
        slot.setSlotType("SlotType1");
        slot.setName(name);
        ValueListType valueList = bu.rimFac.createValueListType();
        List l = valueList.getValue();
        Value val = bu.rimFac.createValue(value);
        l.add(val);
        slot.setValueList( valueList );
        if ( log.isDebugEnabled() )
            log.debug("getSlot:"+slot.getName()+"="+((ValueImpl)slot.getValueList().getValue().get(0)).getValue());
        return slot;
    }
 
    private ArrayList getStatus(String status) {
        ArrayList l = new ArrayList();
        if ( status != null && status.trim().length() > 0 ) {
            if ( ! status.startsWith(V3_STATUS_PREFIX)) {
                status = V3_STATUS_PREFIX + status;
            }
            l.add(status);
        } else {
            l.add(V3_STATUS_SUBMITTED);
            l.add(V3_STATUS_APPROVED);
            l.add(V3_STATUS_DEPRECATED);
        }
        return l;
    }
    
}
