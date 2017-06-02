package org.dcm4chee.xds.common.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.ws.core.ConfigProvider;

public class DocumentRepositoryPortTypeFactory {

	public static DocumentRepositoryPortType getDocumentRepositoryPortSoap11(String endpointAddress, String action, String messageId) {
		DocumentRepositoryPortType port = new DocumentRepositoryService().getDocumentRepositoryPortSoap11();
		configurePort(port, endpointAddress, action, messageId);
		return port;
	}

	public static DocumentRepositoryPortType getDocumentRepositoryPortSoap12(String endpointAddress, String action, String messageId) {
		DocumentRepositoryPortType port = new DocumentRepositoryService().getDocumentRepositoryPortSoap12();
		configurePort(port, endpointAddress, action, messageId);
		return port;
	}
	
	public static DocumentRepositoryPortType configurePort(DocumentRepositoryPortType port, String endpointAddress, String action, String messageId) {
		BindingProvider bindingProvider = (BindingProvider)port;
		ConfigProvider configProvider = (ConfigProvider)port;
		SOAPBinding soapBinding = (SOAPBinding)bindingProvider.getBinding();
		soapBinding.setMTOMEnabled(true);
		
		// NOTE: The correct way to support WSAddressing on the client
		// is to do this call:
		// configProvider.setConfigName("Standard WSAddressing Client");
		// However, due to a JBoss bug (http://jira.jboss.com/jira/browse/JBWS-1880)
		// we must add a custom handler to force the injection of WSAddressing 
		// attributes, as done in the next 3 lines.
		List<Handler> customHandlerChain = new ArrayList<Handler>();
		customHandlerChain.add(new WSAddressingHandler(endpointAddress, action, messageId));
		soapBinding.setHandlerChain(customHandlerChain);
		 
		Map<String, Object> reqCtx = bindingProvider.getRequestContext();
		reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
		
		return port;
	}
}
