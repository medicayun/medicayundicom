/*
 * Created on 10.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4che.dict.UIDs;
import org.dcm4chex.wado.mbean.ExtendedWADOService;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WADOExtRequestObject extends WADORequestObjectImpl {

	private static Logger log = Logger.getLogger( ExtendedWADOService.class.getName() );
	
	private String reqType;
	private String serviceType;
	private String level;
	
	/**
	 * Creates a WADORequestObjectImpl instance configured with http request.
	 * 
	 * @param request The http request.
	 */
	public WADOExtRequestObject( String reqType, HttpServletRequest request ) {
		super( request );
		this.reqType = reqType;
		serviceType = request.getParameter("serviceType");
		level = request.getParameter("serviceType");
	}
	
	
	/** 
	 * Checks this request object and returns an error code.
	 * <p>
	 * <DL>
	 * <DT>Following checks:</DT>
	 * <DD>  requestType must be "WADOext"</DD>
	 * <DD>  studyUID must be set</DD>
	 * <DD>  if transferSyntax is set: check if only one of explicitVRLittleEndian or deflatedExplicitVRLittleEndian is set</DD>
	 * </DL>
	 * 
	 * @return OK if it is a valid WADO request or an error code.
	 */
	public int checkRequest() {
		
		if ( getRequestType() == null || !reqType.equalsIgnoreCase(getRequestType()) ||
				serviceType == null ) {
			setErrorMsg("Extended WADO: URL not valid! Parameters must be set: requestType="+reqType+" and serviceType"); 
 			return INVALID_WADO_URL;
		}
		List l = getContentTypes();
		if ( l != null && l.size() > 0) {
			if ( l.size() > 1 ) {
				setErrorMsg("Extended WADO: Invalid contentType parameter! Only one content type (application/dicom) is allowed!");
				return INVALID_CONTENT_TYPE;
			}
			if ( ! "application/dicom".equals(l.get(0)) ) {
				setErrorMsg("Extended WADO: Invalid contentType parameter! Only application/dicom is allowed!");
				return INVALID_CONTENT_TYPE;
			}
		}
		String ts = getTransferSyntax();
		if ( ts != null && !UIDs.ExplicitVRLittleEndian.equals(ts) && !UIDs.DeflatedExplicitVRLittleEndian.equals(ts)) {
			setErrorMsg("Extended WADO: Invalid transferSyntax parameter! Only 1.2.840.10008.1.2.1(explicit VR little endian) or 1.2.840.10008.1.2.1.99(deflated explicit VR little endian) are allowed!");
			return INVALID_TRANSFER_SYNTAX;
		}
		
		return EXTENDED_WADO_URL;
	}

	/**
	 * @return Returns the serviceType.
	 */
	public String getServiceType() {
		return serviceType;
	}
	/**
	 * @param serviceType The serviceType to set.
	 */
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	/**
	 * @return Returns the level.
	 */
	public String getLevel() {
		return level;
	}
	/**
	 * @param level The level to set.
	 */
	public void setLevel(String level) {
		this.level = level;
	}
}
