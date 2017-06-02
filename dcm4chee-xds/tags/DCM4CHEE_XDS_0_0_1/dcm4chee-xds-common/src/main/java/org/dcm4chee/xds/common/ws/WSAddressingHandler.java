package org.dcm4chee.xds.common.ws;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.dcm4chee.xds.common.XDSConstants;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSAddressingHandler extends GenericSOAPHandler
{
	private Logger log = LoggerFactory.getLogger(WSAddressingHandler.class);
	private static Set<QName> HEADERS = new HashSet<QName>();
	private String to;
	private String action;
	private String messageId;

	static {
		HEADERS.add( new AddressingConstantsImpl().getActionQName());
	}
	
	public WSAddressingHandler(String to, String action, String messageId) {
		this.to = to;
		this.action = action;
		this.messageId = messageId;
	}
	
	public Set getHeaders()
	{
		return Collections.unmodifiableSet(HEADERS);
	}

	protected boolean handleOutbound(MessageContext msgContext) {
		try {
			SOAPMessage msg = ((SOAPMessageContext)msgContext).getMessage();
			
			// Set the "To" header
			SOAPHeaderElement hdr = msg.getSOAPHeader().addHeaderElement(
					new QName(XDSConstants.NS_WS_ADDRESSING, XDSConstants.SOAP_HEADER_TO, "wsa"));
			hdr.setValue(to);
			
			// Set the "Action" header
			hdr = msg.getSOAPHeader().addHeaderElement(
					new QName(XDSConstants.NS_WS_ADDRESSING, XDSConstants.SOAP_HEADER_ACTION, "wsa"));
			hdr.setValue(action);
			
			// Set the "MessageID" header
			hdr = msg.getSOAPHeader().addHeaderElement(
					new QName(XDSConstants.NS_WS_ADDRESSING, XDSConstants.SOAP_HEADER_MSG_ID, "wsa"));
			hdr.setValue(messageId);
			
		} catch (Exception e) {
			log.error("Error:",e);
		}
		return true;
	}
	
	protected boolean handleInbound(MessageContext msgContext) {
		return true;
	}
}