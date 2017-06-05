/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.TransformerConfigurationException;

import org.dcm4chex.wado.common.RIDResponseObject;
import org.dcm4chex.wado.mbean.xml.XMLResponseObject;
import org.xml.sax.SAXException;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RIDTransformResponseObjectImpl implements RIDResponseObject {

	private XMLResponseObject xmlResp;
	private String contentType;
	private int returnCode;
	private String errorMessage;

	public RIDTransformResponseObjectImpl( XMLResponseObject xmlResp, String contentType, int retCode, String errMsg ) {
		this.xmlResp = xmlResp;
		this.contentType = contentType;
		returnCode = retCode;
		errorMessage = errMsg;
	}
	
	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.WADOResponseObject#getFile()
	 */
	public void execute( OutputStream out ) throws TransformerConfigurationException, SAXException, IOException {
		xmlResp.toXML( out );
		out.flush();
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.WADOResponseObject#getContentType()
	 */
	public String getContentType() {
		return contentType;
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.WADOResponseObject#getReturnCode()
	 */
	public int getReturnCode() {
		return returnCode;
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.WADOResponseObject#getErrorMessage()
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
