/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.TransformerConfigurationException;

import org.dcm4chex.wado.common.RIDResponseObject;
import org.xml.sax.SAXException;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RIDStreamResponseObjectImpl implements RIDResponseObject {

	private InputStream stream;
	private String contentType;
	private int returnCode;
	private String errorMessage;
	private static final int BUF_LEN = 65536;

	public RIDStreamResponseObjectImpl( InputStream is, String contentType, int retCode, String errMsg ) {
		this.stream = is;
		this.contentType = contentType;
		returnCode = retCode;
		errorMessage = errMsg;
	}
	
	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.WADOResponseObject#getFile()
	 */
	public void execute( OutputStream out ) throws TransformerConfigurationException, SAXException, IOException {
		if ( stream != null ) {
			InputStream in;
			if ( stream instanceof BufferedInputStream ) {
				in = stream;
			} else {
				in = new BufferedInputStream( stream, BUF_LEN );
			}
			byte[] buf = new byte[BUF_LEN];
			try {
				int len = in.read( buf );
				while ( len > 0 ) {
					out.write( buf, 0, len );
					len = in.read( buf );
				}
			} catch ( IOException e ) {
				throw e;
			} finally {
				in.close();
			}
		}
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
