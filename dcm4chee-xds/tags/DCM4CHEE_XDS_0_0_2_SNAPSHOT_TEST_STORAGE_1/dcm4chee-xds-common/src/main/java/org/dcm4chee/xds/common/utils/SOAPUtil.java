package org.dcm4chee.xds.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

public class SOAPUtil {

	private static SOAPUtil singleton;
	
	private SOAPUtil() {
	}

	public static SOAPUtil getInstance() {
		if (singleton == null ) {
			singleton = new SOAPUtil();
		}
		return singleton;
	}
	 
	public void logSOAPMessage(Logger log, SOAPMessage message, boolean indent) throws SOAPException, IOException, ParserConfigurationException, SAXException {
		Source s = message.getSOAPPart().getContent();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("SOAP message:".getBytes());
            Transformer t = TransformerFactory.newInstance().newTransformer();
            if (indent)
            	t.setOutputProperty("indent", "yes");
            t.transform(s, new StreamResult(out));
            log.info(out.toString());
        } catch (Exception e) {
            log.warn("Failed to log SOAP message", e);
        }
	}

}
