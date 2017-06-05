/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.common;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface WADOResponseObject {
	
	/**
	 * The RIDCommand to execute this.
	 * 
	 * @return A command object to perform output process.
	 * @throws SAXException
	 * @throws TransformerConfigurationException
	 * @throws IOException
	 */
	void execute( OutputStream out ) throws TransformerConfigurationException, SAXException, IOException;
	
	/**
	 * Returns the content type that should be set in the response.
	 * 
	 * @return a content type like 'image/jpeg'
	 */
	String getContentType();
	
	/**
	 * Returns the HTTP return code for the response.
	 * <p>
	 * This can be used to send an error back to the client.
	 * 
	 * @return An http return code.
	 */
	int getReturnCode();
	
	/**
	 * Returns an error message.
	 * <p>
	 * 
	 * @return An error message or null if return code is OK.
	 */
	String getErrorMessage();
}
