package org.dcm4chee.xdsa.registry.ws;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceContext;

import org.dcm4chee.xds.common.XDSConstants;
import org.dcm4chee.xds.common.exception.XDSException;
import org.dcm4chee.xds.infoset.v21.DocumentRegistryAPortType;
import org.dcm4chee.xds.infoset.v21.RegistryResponse;
import org.dcm4chee.xds.infoset.v21.SubmitObjectsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author me
 *
 */
@WebService(
        name="DocumentRegistryA_PortType",
        serviceName="DocumentRegistryA_Service",
        portName="DocumentRegistryA_Port_Soap11",
        targetNamespace="urn:ihe:iti:xds-b:2007",
        wsdlLocation="/WEB-INF/wsdl/registry_a.wsdl",
        endpointInterface="org.dcm4chee.xds.infoset.v21.DocumentRegistryAPortType"
)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public class DocumentRegistryA implements DocumentRegistryAPortType {

    @Resource
    WebServiceContext wsCtx;

    private XDSaRegistryDelegate delegate = new XDSaRegistryDelegate();
    private Logger log = LoggerFactory.getLogger(DocumentRegistryA.class);

    public DocumentRegistryA() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.dcm4chee.xds.common.ws.DocumentRegistryPortType#registerDocumentSetB(org.dcm4chee.xds.repo.ws.ProvideAndRegisterDocumentSetRequestType)
     */
    public RegistryResponse documentRegistryARegisterDocumentSet(SubmitObjectsRequest req) {
        log.info("documentRegistryRegisterDocumentSet (XDS.a) SubmitObjectsRequest:"+req);
        RegistryResponse rsp = null;
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

}