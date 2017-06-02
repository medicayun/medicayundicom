package org.dcm4chee.xds.repo.ws;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dcm4chee.xds.common.XDSConstants;

public class WSAddressingHandler extends GenericSOAPHandler
{
    private Logger log = LoggerFactory.getLogger(WSAddressingHandler.class);
    private static Set<QName> HEADERS = new HashSet<QName>();
    private String to = "http://www.w3.org/2005/08/addressing/anonymous";
    private String action;

    static {
        HEADERS.add( new AddressingConstantsImpl().getActionQName());
    }

    public Set getHeaders()
    {
        return Collections.unmodifiableSet(HEADERS);
    }

    protected boolean handleOutbound(MessageContext msgContext) {
        try {
            SOAPMessage msg = ((SOAPMessageContext)msgContext).getMessage();
            SOAPHeaderElement hdr = msg.getSOAPHeader().addHeaderElement(
                    new QName(XDSConstants.NS_WS_ADDRESSING, XDSConstants.SOAP_HEADER_TO, "wsa"));
            hdr.setValue(to);
            hdr = msg.getSOAPHeader().addHeaderElement(
                    new QName(XDSConstants.NS_WS_ADDRESSING, XDSConstants.SOAP_HEADER_ACTION, "wsa"));
            hdr.setValue(action + "Response");
        } catch (Exception e) {
            log.error("Error:",e);
        }
        return true;
    }

    protected boolean handleInbound(MessageContext msgContext) {
        try {
            SOAPMessage msg = ((SOAPMessageContext)msgContext).getMessage();
            SOAPHeader header = msg.getSOAPPart().getEnvelope().getHeader();
            SOAPElement elem = (SOAPElement)(header.getChildElements(new QName("http://www.w3.org/2005/08/addressing", "Action")).next());
            action = elem.getValue();
            log.info("Action is: " + action);
        } catch (Exception e) {
            log.error("Error: ",e);
        }
        return true;
    }
}