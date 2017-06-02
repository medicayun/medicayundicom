package org.dcm4chee.xdsb.repository.ws.client;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingConstants;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.core.soap.attachment.MimeConstants;
import org.jboss.ws.core.utils.UUIDGenerator;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrieveClientHeaderHandler extends GenericSOAPHandler
{
    private Logger log = LoggerFactory.getLogger(RetrieveClientHeaderHandler.class);
    private static Set<QName> HEADERS = new HashSet<QName>();

    static {
        HEADERS.add( new AddressingConstantsImpl().getActionQName());
    }

    public Set getHeaders()
    {
        log.info("@@@@@@@@@@@@ REPOSITORY RETRIEVE CLIENT @@@@@@@@@@@@@@@@ getHeaders :"+HEADERS);
        return Collections.unmodifiableSet(HEADERS);
    }

    protected boolean handleOutbound(MessageContext msgContext) {
        log.debug("@@@@@@@@@@@@ REPOSITORY RETRIEVE CLIENT @@@@@@@@@@@@@@@@ handleOutbound");
        try {
            AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
            AddressingConstants ADDR = builder.newAddressingConstants();

            AddressingProperties outProps = builder.newAddressingProperties();
            outProps.setAction(builder.newURI("urn:ihe:iti:2007:RegisterDocumentSet-b"));
            outProps.setMessageID(builder.newURI("urn:uuid:"+ UUIDGenerator.generateRandomUUIDString()));

            msgContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, outProps);
            msgContext.setScope(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, Scope.APPLICATION);
            log.info("@@@@@@@@@@@@ REPOSITORY RETRIEVE CLIENT @@@@@@@@@@@@@@@@ handleOutbound --SOAP Message end.");
        } catch (Exception e) {
            log.error("Error:",e);
        }
        log.debug("@@@@@@@@@@@@ REPOSITORY RETRIEVE CLIENT @@@@@@@@@@@@@@@@ handleOutbound end.");
        return true;
    }

    private void logMimeHdrContentType(MimeHeaders headers) {
        if (headers == null ) {
            log.info("MimeHeaders is null!");
            return;
        }
        String[] type = headers.getHeader(MimeConstants.CONTENT_TYPE);
        if ( type == null) {
            log.info("No contentType in MimeHeaders!");
            return;
        }
        for ( int i = 0; i < type.length ; i++) {
            log.info("contentType("+i+"):"+type[i]);
        }
    }
    protected boolean handleInbound(MessageContext msgContext) {
        log.debug("@@@@@@@@@@@@ CLIENT @@@@@@@@@@@@@@@@ handleInbound");

        return true;
    }

 
}