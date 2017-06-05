/*
 * Created on 11.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.web;

import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.wado.common.BasicRequestObject;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RequestObjectFactory {

	/**
	 * Returns an request object for given hjttp request.
	 * <p>
	 * Returns null, if the request is not either a WADO or IHE RID request.
	 * 
	 * @param request The http request.
	 * 
	 * @return
	 */
	public static BasicRequestObject getRequestObject( HttpServletRequest request ) {
		BasicRequestObject reqObj = null; 
		String reqType = request.getParameter("requestType");
		if ( reqType == null ) return null; //wrong URL
		if ( "WADO".equals( reqType ) ) {
			return new WADORequestObjectImpl( request );
		} else if ( "DOCUMENT".equals( reqType )) {
			return new RIDDocumentRequestObject( request );
		} else if ( reqType.startsWith( "SUMMARY" ) ) {
			return new RIDInfoRequestObject( request );
		} else if ( reqType.startsWith( "LIST" ) ) { //not supported
			return new RIDInfoRequestObject( request );// return an 'invalid (not supported)' request object.
		} else {
			return null; //wrong URL
		}
	}
}
