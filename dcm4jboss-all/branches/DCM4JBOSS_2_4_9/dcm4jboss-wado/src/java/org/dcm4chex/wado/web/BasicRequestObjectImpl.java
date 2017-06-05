/*
 * Created on 11.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.web;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4chex.wado.common.BasicRequestObject;
import org.dcm4chex.wado.mbean.WADOService;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class BasicRequestObjectImpl implements BasicRequestObject {

	private static Logger log = Logger.getLogger( WADOService.class.getName() );

	private String reqType;
	private Map headerMap;
	private Map paramMap;
	private List allowedContentTypes = null;

	private String reqURL;
	private String errMsg;

	/**
	 * Initialize an RequestObject with http request.
	 * <p>
	 * This constructor should be used by the real implementations.
	 * 
	 * @param request The http request.
	 */
	public BasicRequestObjectImpl( HttpServletRequest request ) {
		reqType  = request.getParameter( "requestType" );
		paramMap = request.getParameterMap();
		try {
		reqURL = request.getRequestURL().append("?").append(request.getQueryString()).toString();
		} catch ( Exception x ) {

		}
		headerMap  = new HashMap();
		Enumeration enum1 = request.getHeaderNames();
		String key;
		while ( enum1.hasMoreElements() ) {
			key = (String) enum1.nextElement();
			if ( log.isDebugEnabled() ) log.debug("header: "+key+"="+request.getHeader(key) );
			headerMap.put( key, request.getHeader(key) );
		}
		setAllowedContentTypes( request.getHeader("accept") );
		
	}
	
	/**
	 * @param accept
	 */
	private void setAllowedContentTypes(String accept ) {
		List l = null;
		String s;
		if ( accept != null) {
			l = new ArrayList();
			StringTokenizer st = new StringTokenizer( accept, ",");
			while ( st.hasMoreElements() ) {
				s = st.nextToken();
				if ( s.indexOf(";") != -1 ) s = s.substring( 0, s.indexOf(";") );//ignore quality value
				l.add( s.trim() );
			}
		}
		allowedContentTypes = l;
	}

	/**
	 * Returns the requestType parameter of the http request.
	 * 
	 * @return requestType
	 */
	public String getRequestType() {
		return reqType;
	}


	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.RIDRequestObject#getAllowedContentTypes()
	 */
	public List getAllowedContentTypes() {
		return this.allowedContentTypes;
	}

	/**
	 * Returns all parameter of the http request in a map.
	 * 
	 * @return All http parameter
	 */
	public Map getRequestParams() {
		return paramMap;
	}

	/**
	 * Returns a Map of all request header fields of the http request.
	 * 
	 * @see org.dcm4chex.wado.common.WADORequestObject#getRequestHeaders()
	 * 
	 * @return All request header fields in a map.
	 */
	public Map getRequestHeaders() {
		return headerMap;
	}
	

	public String getRequestURL() {
		return reqURL;
		
	}
	/**
	 * @return Returns the errMsg.
	 */
	public String getErrorMsg() {
		return errMsg;
	}
	/**
	 * @param errMsg The errMsg to set.
	 */
	protected void setErrorMsg(String errMsg) {
		this.errMsg = errMsg;
	}
}
