/*
 * Created on 11.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dcm4che.util.ISO8601DateFormat;
import org.dcm4chex.wado.common.RIDRequestObject;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RIDInfoRequestObject extends BasicRequestObjectImpl implements
		RIDRequestObject {

	private String patientID;
	private String mostRecentResults;
	private String lowerDateTime;
	private String upperDateTime;
	private Map knownParams = new HashMap();
	
	private static List supportedInfoTypes = null;
	
	public RIDInfoRequestObject( HttpServletRequest request ) {
		super( request );
		patientID = request.getParameter( "patientID" );
		mostRecentResults = request.getParameter( "mostRecentResults" );
		lowerDateTime = request.getParameter( "lowerDateTime" );
		upperDateTime = request.getParameter( "upperDateTime" );
		knownParams.put( "requestType", getRequestType() );
		knownParams.put( "patientID", patientID );
		knownParams.put( "mostRecentResults", mostRecentResults );
		knownParams.put( "lowerDateTime", lowerDateTime );
		knownParams.put( "upperDateTime", upperDateTime );
	}
	/**
	 * @return Returns the lowerDateTime.
	 */
	public String getLowerDateTime() {
		return lowerDateTime;
	}
	/**
	 * @return Returns the mostRecentResults.
	 */
	public String getMostRecentResults() {
		return mostRecentResults;
	}
	/**
	 * @return Returns the patientID.
	 */
	public String getPatientID() {
		return patientID;
	}
	/**
	 * @return Returns the upperDateTime.
	 */
	public String getUpperDateTime() {
		return upperDateTime;
	}
	/**
	 * Returns the value of a 'known' http request parameter.
	 * <p>
	 * <DL>
	 * <DT>Following parameter are 'known' in this request object:</DT>
	 * <DD>requestType, patientID, mostRecentResults, lowerDateTime, upperDateTime</DD>
	 * </DL>
	 * 
	 * @param paraName Name of request parameter.
	 * 
	 * @return value of param or null if param is not set or not known.
	 */
	public String getParam(String paraName) {
		return (String)knownParams.get( paraName );
	}

	/* (non-Javadoc)
	 * @see org.dcm4chex.wado.common.BasicRequestObject#checkRequest()
	 */
	public int checkRequest() {
		if ( getRequestType() == null || patientID == null || mostRecentResults == null ) 
			return RIDRequestObject.INVALID_RID_URL; //required param missing!
		
		if ( ! getRequestType().startsWith( "SUMMARY") && ! getRequestType().startsWith( "LIST") )
			return RIDRequestObject.INVALID_RID_URL;//RID Information requestmust start either with SUMMARY or LIST
			
		if ( ! getSupportedInformationTypes().contains( getRequestType() ) )
			return RIDRequestObject.RID_REQUEST_NOT_SUPPORTED;
		
		try {
			Integer.parseInt( this.mostRecentResults );
		} catch ( Exception x ) {
			return RIDRequestObject.INVALID_RID_URL;//not an integer string
		}
		
		if ( this.lowerDateTime != null ) {
			if ( lowerDateTime.trim().length() > 0 ) {
				try {
					new ISO8601DateFormat().parse( lowerDateTime );			
				} catch ( Exception x ) {
					return RIDRequestObject.INVALID_RID_URL;//invalid date/time string
				}
			} else {
				lowerDateTime = null;
			}
		}

		if ( this.upperDateTime != null ) {
			if ( upperDateTime.trim().length() > 0 ) {
				try {
					new ISO8601DateFormat().parse( upperDateTime );			
				} catch ( Exception x ) {
					return RIDRequestObject.INVALID_RID_URL;//invalid date/time string
				}
			} else {
				upperDateTime = null;
			}
		}
		
		return RIDRequestObject.SUMMERY_INFO;
	}
	/**
	 * @return
	 */
	public List getSupportedInformationTypes() {
		if ( supportedInfoTypes == null ) {
			supportedInfoTypes = new ArrayList();
			supportedInfoTypes.add( "SUMMARY");
			supportedInfoTypes.add( "SUMMARY-RADIOLOGY");
			supportedInfoTypes.add( "SUMMARY-CARDIOLOGY");
			supportedInfoTypes.add( "SUMMARY-CARDIOLOGY-ECG");
		}
		return supportedInfoTypes;
	}

}
