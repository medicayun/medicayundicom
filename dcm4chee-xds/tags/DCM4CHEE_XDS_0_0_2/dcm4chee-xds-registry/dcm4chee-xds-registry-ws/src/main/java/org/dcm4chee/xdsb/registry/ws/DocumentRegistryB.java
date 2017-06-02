package org.dcm4chee.xdsb.registry.ws;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceContext;

import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.infoset.v30.AdhocQueryRequest;
import org.dcm4chee.xds.infoset.v30.AdhocQueryResponse;
import org.dcm4chee.xds.infoset.v30.RegistryResponseType;
import org.dcm4chee.xds.infoset.v30.SubmitObjectsRequest;
import org.dcm4chee.xds.infoset.v30.DocumentRegistryPortType;
import org.jboss.ws.annotation.EndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author me
 *
 */
@WebService(
        name="DocumentRegistry_PortType",
        serviceName="DocumentRegistry_Service",
        portName="DocumentRegistry_Port_Soap12",
        targetNamespace="urn:ihe:iti:xds-b:2007",
        wsdlLocation="/WEB-INF/wsdl/registry.wsdl",
        endpointInterface="org.dcm4chee.xds.infoset.v30.DocumentRegistryPortType"
)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
@EndpointConfig(configName = "Standard SOAP 1.2 WSAddressing Endpoint")
public class DocumentRegistryB implements DocumentRegistryPortType {

    @Resource
    WebServiceContext wsCtx;

    private XDSbRegistryDelegate delegate = new XDSbRegistryDelegate();
    private Logger log = LoggerFactory.getLogger(DocumentRegistryB.class);

    public DocumentRegistryB() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.xds.infoset.v30.DocumentRegistryPortType#registerDocumentSetB(org.dcm4chee.xds.repo.ws.ProvideAndRegisterDocumentSetRequestType)
     */
    public RegistryResponseType documentRegistryRegisterDocumentSetB(SubmitObjectsRequest req) {
        log.info("documentRegistryRegisterDocumentSetB SubmitObjectsRequest:"+req);
        RegistryResponseType rsp = null;
        try {
            rsp = delegate.registerDocuments(req);
        } catch (Exception x) {
            log.error("RegisterDocumentSet failed!Reason:"+x,x);
            try {
                rsp = delegate.getErrorRegistryResponse(new XDSException(XDSConstants.XDS_ERR_REGISTRY_ERROR,"Register Document Set failed!",x));
            } catch (JAXBException e) {
                log.error("Can't create Error RegistryResponse!",e);
            }
        }
        return rsp;
    }

    public AdhocQueryResponse documentRegistryRegistryStoredQuery(
            AdhocQueryRequest req) {
        log.info("documentRegistryRegistryStoredQuery AdhocQueryRequest:"+req);
        AdhocQueryResponse rsp = null;
        try {
            rsp = delegate.storedQuery(req);
        } catch (Exception x) {
            log.error("RegisterDocumentSet failed!Reason:"+x,x);
            try {
                rsp = delegate.getErrorQueryResponse(new XDSException(XDSConstants.XDS_ERR_REGISTRY_ERROR,"Register Document Set failed!",x));
            } catch (JAXBException e) {
                log.error("Can't create Error RegistryResponse!",e);
            }
        }
        return rsp;
    }



}