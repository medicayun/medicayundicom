package org.dcm4chee.xds.repo.ws;

import static javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_MTOM_BINDING;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBException;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;

import org.dcm4chee.xds.common.delegate.XDSbServiceDelegate;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.common.infoset.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds.common.infoset.RegistryResponseType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetRequestType;
import org.dcm4chee.xds.common.infoset.RetrieveDocumentSetResponseType;
import org.dcm4chee.xds.common.ws.DocumentRepositoryPortType;
import org.jboss.ws.annotation.EndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author me
 *
 */
@WebService(
        name="DocumentRepository_PortType",
        serviceName="DocumentRepository_Service",
        portName="DocumentRepository_Port_Soap12",
        targetNamespace="urn:ihe:iti:xds-b:2007",
        wsdlLocation="/WEB-INF/wsdl/XDS.b_DocumentRepository.wsdl",
        endpointInterface="org.dcm4chee.xds.common.ws.DocumentRepositoryPortType"
)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
@BindingType(value = SOAP12HTTP_MTOM_BINDING)
@EndpointConfig(configName = "Standard SOAP 1.2 WSAddressing Endpoint")
@HandlerChain(file = "xds_repo_handler.xml")
public class DocumentRepository implements DocumentRepositoryPortType {

    @Resource
    WebServiceContext wsCtx;

    private XDSbServiceDelegate delegate = new XDSbServiceDelegate();
    private Logger log = LoggerFactory.getLogger(DocumentRepository.class);

    public DocumentRepository() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.xds.repo.ws.DocumentRepositoryPortType#documentRepositoryProvideAndRegisterDocumentSetB(org.dcm4chee.xds.repo.ws.ProvideAndRegisterDocumentSetRequestType)
     */
    public RegistryResponseType documentRepositoryProvideAndRegisterDocumentSetB(
            ProvideAndRegisterDocumentSetRequestType req) {

        RegistryResponseType resp = null;
        try {
            resp = delegate.storeAndRegisterDocuments(req);
        } catch (XDSException x) {
            log.error("ProvideAndRegisterDocumentSet failed!Reason:"+x,x);
            try {
                resp = delegate.getErrorRegistryResponse(x);
            } catch (JAXBException e) {
                log.error("Can't create Error RegistryResponse!",e);
            }
        }
        return resp;
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.xds.repo.ws.DocumentRepositoryPortType#documentRepositoryRetrieveDocumentSet(org.dcm4chee.xds.repo.ws.RetrieveDocumentSetRequestType)
     */
    public RetrieveDocumentSetResponseType documentRepositoryRetrieveDocumentSet(
            RetrieveDocumentSetRequestType req) {
        RetrieveDocumentSetResponseType rsp =  null;
        try {
            rsp = delegate.retrieveDocumentSetFromXDSbRepositoryService(req);
        } catch (XDSException x) {
            log.error("ProvideAndRegisterDocumentSet failed!Reason:"+x,x);
            try {
                rsp = delegate.getErrorRetrieveDocumentSetResponse(x);
            } catch (JAXBException e) {
                log.error("Can't create Error RegistryResponse!",e);
            }
        }
        return rsp;
    }

}