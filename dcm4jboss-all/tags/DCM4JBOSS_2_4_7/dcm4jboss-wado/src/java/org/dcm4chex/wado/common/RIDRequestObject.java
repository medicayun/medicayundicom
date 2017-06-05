/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.common;


/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface RIDRequestObject extends BasicRequestObject{

	public static final int SUMMERY_INFO = 0;
	public static final int LIST_INFO = 1;
	public static final int DOCUMENT = 2;
	public static final int INVALID_RID_URL = -1;
	public static final int RID_REQUEST_NOT_SUPPORTED = -2;

	/**
	 * Returns the value of an http request parameter.
	 * 
	 * @param paraName The name of the request parameter.
	 * 
	 * @return The value of the parameter.
	 */
	String getParam( String paraName );
	
}
