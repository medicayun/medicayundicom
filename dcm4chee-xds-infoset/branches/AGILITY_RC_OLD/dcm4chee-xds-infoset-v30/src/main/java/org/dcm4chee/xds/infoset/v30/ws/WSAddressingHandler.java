package org.dcm4chee.xds.infoset.v30.ws;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSAddressingHandler extends GenericSOAPHandler
{
	private Logger log = LoggerFactory.getLogger(WSAddressingHandler.class);

	public static final String SOAP_HEADER_ACTION = "Action";
	public static final String SOAP_HEADER_TO = "To";
	public static final String SOAP_HEADER_MSG_ID = "MessageID";
	public static final String NS_WS_ADDRESSING = "http://www.w3.org/2005/08/addressing";
	public static final String PREFIX = "wsa";
	
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
					new QName(NS_WS_ADDRESSING, SOAP_HEADER_TO, PREFIX));
			hdr.setValue(to);
			
			// Set the "Action" header
			hdr = msg.getSOAPHeader().addHeaderElement(
					new QName(NS_WS_ADDRESSING, SOAP_HEADER_ACTION, PREFIX));
			hdr.setValue(action);
			
			// Set the "MessageID" header
			hdr = msg.getSOAPHeader().addHeaderElement(
					new QName(NS_WS_ADDRESSING, SOAP_HEADER_MSG_ID, PREFIX));
			hdr.setValue(messageId);
			
		} catch (Exception e) {
			log.error("handleOutbound: could not add headers", e);
		}
		return true;
	}
	
	protected boolean handleInbound(MessageContext msgContext) {
		return true;
	}
}