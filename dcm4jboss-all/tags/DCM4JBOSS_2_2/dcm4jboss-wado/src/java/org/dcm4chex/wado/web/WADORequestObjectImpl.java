/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.web;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4chex.wado.common.WADORequestObject;
import org.dcm4chex.wado.mbean.WADOService;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WADORequestObjectImpl implements WADORequestObject {

	private static Logger log = Logger.getLogger( WADOService.class.getName() );

	private String reqType;
	private String studyUID;
	private String seriesUID;
	private String instanceUID;
	private String rows;
	private String columns;
	private String frameNumber;
	
	private List contentTypes = null;
	private Map paramMap;
	private Map headerMap;

	/**
	 * Creates a WADORequestObjectImpl instance configured with http request.
	 * 
	 * @param request The http request.
	 */
	public WADORequestObjectImpl( HttpServletRequest request ) {
		reqType  = request.getParameter( "requestType" );
		studyUID = request.getParameter( "studyUID" );
		seriesUID = request.getParameter( "seriesUID" );
		instanceUID = request.getParameter( "objectUID" );
		// optional parameters - implemented
		String contentType = request.getParameter( "contentType" );
		rows = request.getParameter( "rows" );
		columns = request.getParameter( "columns" );
		frameNumber = request.getParameter("frameNumber");
		paramMap = request.getParameterMap();
		contentTypes = _string2List( contentType, "," );
		
		headerMap = new HashMap();
		Enumeration enum1 = request.getHeaderNames();
		String key;
		while ( enum1.hasMoreElements() ) {
			key = (String) enum1.nextElement();
			if ( log.isDebugEnabled() ) log.debug("header: "+key+"="+request.getHeader(key) );
			headerMap.put( key, request.getHeader(key) );
		}
	}
	
	/**
	 * Returns the value of reqType request parameter.
	 * 
	 * @see org.dcm4chex.wado.common.WADORequestObject#getRequestType()
	 * 
	 * @return Returns the reqType.
	 */
	public String getRequestType() {
		return reqType;
	}
	
	/**
	 * Returns the value of studyUID request parameter.
	 *  
	 * @see org.dcm4chex.wado.common.WADORequestObject#getStudyUID()
	 * 
	 * @return the studyUID.
	 */
	public String getStudyUID() {
		return studyUID;
	}

	/**
	 * Returns the value of seriesUID request parameter.
	 *  
	 * @see org.dcm4chex.wado.common.WADORequestObject#getSeriesUID()
	 * 
	 * @return the seriesUID.
	 */
	public String getSeriesUID() {
		return seriesUID;
	}

	/**
	 * Returns the value of objectUID request parameter.
	 *  
	 * @see org.dcm4chex.wado.common.WADORequestObject#getObjectUID()
	 * 
	 * @return the objectUID
	 */
	public String getObjectUID() {
		return instanceUID;
	}

	/**
	 * Returns the value of rows request parameter.
	 *  
	 * @see org.dcm4chex.wado.common.WADORequestObject#getRows()
	 * 
	 * @return the rows parameter (integer String)
	 */
	public String getRows() {
		return rows;
	}

	/**
	 * Returns the value of columns request parameter.
	 *  
	 * @see org.dcm4chex.wado.common.WADORequestObject#getColumns()
	 * 
	 * @return the columns parameter (integer String)
	 */
	public String getColumns() {
		return columns;
	}

	/**
	 * Returns the value of frameNumber request parameter.
	 *  
	 * @see org.dcm4chex.wado.common.WADORequestObject#getFrameNumber()
	 * 
	 * @return the frameNumber (integer String)
	 */
	public String getFrameNumber() {
		return frameNumber;
	}

	/**
	 * Returns the list of requested content types from the contentType request parameter.
	 * <p>
	 * The contentType param has one ore more content types seperated by ',' character.
	 *  
	 * @see org.dcm4chex.wado.common.WADORequestObject#getContentTypes()
	 * 
	 * @return A list of requested content types
	 */
	public List getContentTypes() {
		return contentTypes;
	}

	/**
	 * Not implemented!
	 * 
	 * @see org.dcm4chex.wado.common.WADORequestObject#getAllowedContentTypes()
	 * 
	 * @return always null
	 */
	public List getAllowedContentTypes() {
		// TODO not implemented yet
		return null;
	}

	/** 
	 * Checks this request object and returns an error code.
	 * <p>
	 * <DL>
	 * <DT>Following checks:</DT>
	 * <DD>  requestType must be "WADO"</DD>
	 * <DD>  studyUID, seriesUID and objectUID must be set</DD>
	 * <DD>  if rows is set: check if it is parseable to int</DD>
	 * <DD>  if columns is set: check if it is parseable to int</DD>
	 * <DD>  if frameNumber is set: check if it is parseable to int</DD>
	 * </DL>
	 * 
	 * @return OK if it is a valid WADO request or an error code.
	 */
	public int checkRequest() {
		if ( reqType == null || !"WADO".equalsIgnoreCase(reqType) ||
				studyUID == null  || seriesUID == null || instanceUID == null ) {
 			return INVALID_WADO_URL;
		}
		if ( rows != null ) {
			try {
				Integer.parseInt( rows );
			} catch ( Exception x ) {
				return INVALID_ROWS;
			}
		}
		if ( columns != null ) {
			try {
				Integer.parseInt( columns );
			} catch ( Exception x ) {
				return INVALID_COLUMNS;
			}
		}
		if ( frameNumber != null ) {
			try {
				Integer.parseInt( frameNumber );
			} catch ( Exception x ) {
				return INVALID_FRAME_NUMBER;
			}
		}
		return OK;
	}

	/**
	 * Returns a Map of all request parameters of the http request.
	 * 
	 * @see org.dcm4chex.wado.common.WADORequestObject#getRequestParams()
	 * 
	 * @return All request params in a map.
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
	
	/**
	 * Seperate the given String with delim character and return a List of the items.
	 * 
	 * @param s			String with one or more items seperated with a character.
	 * @param delim		The delimiter charecter.
	 * @return			A List with the seperated items
	 */
	private List _string2List( String s, String delim ) {
		if ( s == null ) return null;
		StringTokenizer st = new StringTokenizer( s, delim );
		List l = new ArrayList();
		while( st.hasMoreTokens() ) {
			l.add( st.nextToken().trim() );
		}
		return l;
	}
	
	/**
	 * Returns a short description of this request.
	 * <p>
	 * @return String representation of this request.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append( "WADO request:");
		Map mapParam = this.getRequestParams();
		Iterator iter = mapParam.keySet().iterator();
		Object key;
		while ( iter.hasNext() ) {
			key = iter.next();
			sb.append("&").append(key).append("=").append( ( (String[]) mapParam.get(key))[0] );
		}
		return sb.toString();
	}

}
