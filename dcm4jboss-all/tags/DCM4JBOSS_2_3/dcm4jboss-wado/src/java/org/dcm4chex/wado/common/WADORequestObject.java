/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.common;

import java.util.List;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface WADORequestObject extends BasicRequestObject {

	public static final int OK = 0;
	public static final int INVALID_WADO_URL = 1;
	public static final int INVALID_ROWS = 2;
	public static final int INVALID_COLUMNS = 3;
	public static final int INVALID_FRAME_NUMBER = 4;
	
	/**
	 * Returns the studyUID parameter of the http request.
	 * 
	 * @return studyUID
	 */
	String getStudyUID();
	
	/**
	 * Returns the seriesUID parameter of the http request.
	 * 
	 * @return seriesUID
	 */
	String getSeriesUID();
	
	/**
	 * Returns the objectUID parameter of the http request.
	 * 
	 * @return objectUID
	 */
	String getObjectUID();
	
	/**
	 * Returns the rows parameter of the http request.
	 * 
	 * @return rows
	 */
	String getRows();
	
	/**
	 * Returns the columns parameter of the http request.
	 * 
	 * @return columns
	 */
	String getColumns();
	
	/**
	 * Returns the frameNumber parameter of the http request.
	 * 
	 * @return frame number as String
	 */
	String getFrameNumber();
	
	/**
	 * Returns a list of content types as defined via the contentType http param.
	 * 
	 * @return requestType
	 */
	List getContentTypes();
	
}
