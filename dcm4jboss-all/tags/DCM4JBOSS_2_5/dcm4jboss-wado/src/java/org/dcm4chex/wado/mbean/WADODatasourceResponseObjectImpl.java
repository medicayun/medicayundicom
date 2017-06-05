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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.net.DataSource;
import org.dcm4chex.wado.common.WADOResponseObject;
import org.xml.sax.SAXException;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WADODatasourceResponseObjectImpl implements WADOResponseObject {

	private DataSource datasource;
	private Dataset dataset;
	String transferSyntax = null;
	private String contentType;
	private int returnCode;
	private String errorMessage;

	public WADODatasourceResponseObjectImpl( DataSource ds, String ts, String contentType, int retCode, String errMsg ) {
		this.datasource = ds;
		this.transferSyntax = ts;
		this.contentType = contentType;
		returnCode = retCode;
		errorMessage = errMsg;
	}
	public WADODatasourceResponseObjectImpl( Dataset ds, String ts, String contentType, int retCode, String errMsg ) {
		this.dataset = ds;
		this.transferSyntax = ts;
		this.contentType = contentType;
		returnCode = retCode;
		errorMessage = errMsg;
	}
	
	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.WADOResponseObject#getFile()
	 */
	public void execute( OutputStream out ) throws TransformerConfigurationException, SAXException, IOException {
		if ( datasource != null ) {
			datasource.writeTo( out, transferSyntax );
		} else if ( dataset != null ) {
			dataset.writeFile( out, DcmEncodeParam.valueOf( transferSyntax ));
		} else {
			throw new IllegalArgumentException("Cant execute WADO datasource response! Neither a datasource nor dataset object is set!");
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
