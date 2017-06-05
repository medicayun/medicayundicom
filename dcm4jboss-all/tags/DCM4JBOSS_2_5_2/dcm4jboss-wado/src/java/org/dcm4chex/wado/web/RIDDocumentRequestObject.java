/*
 * Created on 11.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.wado.common.RIDRequestObject;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RIDDocumentRequestObject extends BasicRequestObjectImpl implements
		RIDRequestObject {

	private String documentUID;
	private String preferredContentType;
	private Map knownParams = new HashMap();
	
	public RIDDocumentRequestObject( HttpServletRequest request ) {
		super( request );
		documentUID = request.getParameter( "documentUID" );
		preferredContentType = request.getParameter( "preferredContentType" );
		knownParams.put("requestType", getRequestType() );
		knownParams.put("documentUID", documentUID);
		knownParams.put("preferredContentType", preferredContentType);
	}
	/**
	 * @return Returns the documentUID.
	 */
	public String getDocumentUID() {
		return documentUID;
	}
	/**
	 * @return Returns the preferredContentType.
	 */
	public String getPreferredContentType() {
		return preferredContentType;
	}
	
	/**
	 * Returns the value of a 'known' http request parameter.
	 * <p>
	 * <DL>
	 * <DT>Following parameter are 'known' in this request object:</DT>
	 * <DD>requestType, documentUID, preferredContentType</DD>
	 * </DL>
	 * 
	 * @param paraName Name of request parameter.
	 * 
	 * @return value of param or null if param is not set or not known.
	 */
	public String getParam(String paraName) {
		return (String) knownParams.get( paraName );
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.BasicRequestObject#checkRequest()
	 */
	public int checkRequest() {
		if ( "DOCUMENT".equals( getRequestType() ) && documentUID != null && preferredContentType != null )
			return RIDRequestObject.DOCUMENT;
		else
			return RIDRequestObject.INVALID_RID_URL;
	}

}
