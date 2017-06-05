/*
 * Created on 11.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.common;

import java.util.List;
import java.util.Map;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface BasicRequestObject {
	/**
	 * Returns the requestType parameter of the http request.
	 * 
	 * @return requestType
	 */
	String getRequestType();

	/**
	 * Returns a list of content types that the client supports.
	 * <p>
	 * This information comes from the http header.
	 * 
	 * 
	 * @return list of allowed content types or null if no restrictions.
	 */
	List getAllowedContentTypes();

	/** 
	 * Checks this request object and returns a request type code or an error code.
	 * 
	 * @return A request type code (>= 0) if it is a valid WADO request or an error code (<0).
	 */
	int checkRequest();

	/**
	 * Returns all parameter of the http request in a map.
	 * 
	 * @return All http parameter
	 */
	Map getRequestParams();

	/**
	 * Returns a Map of all request header fields of the http request.
	 * 
	 * @see org.dcm4chex.wado.common.WADORequestObject#getRequestHeaders()
	 * 
	 * @return All request header fields in a map.
	 */
	Map getRequestHeaders();

	String getRequestURL();
	
	String getErrorMsg();

}