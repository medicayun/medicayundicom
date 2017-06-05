/*
 * Created on 11.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.web;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chex.wado.common.BasicRequestObject;
import org.dcm4chex.wado.common.RIDRequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RIDServlet extends HttpServlet {

	private static final int BUF_LEN = 65536;

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3258409538737483825L;
	private RIDServiceDelegate delegate;

	/**
	 * Initialize the RIDServiceDelegator.
	 * <p>
	 * Set the name of the MBean from servlet init param.
	 */
	public void init() {
		delegate = new RIDServiceDelegate();
		delegate.getLogger().info("RIDServiceDelegate initialized");
		delegate.init( getServletConfig() );
	}
	
	/**
	 * Handles the POST requset in the doGet method.
	 * 
	 * @param request 	The http request.
	 * @param response	The http response.
	 */
	public void doPost( HttpServletRequest request, HttpServletResponse response ){
		doGet( request, response);
	}

	/**
	 * Handles the GET requset.
	 * 
	 * @param request 	The http request.
	 * @param response	The http response.
	 */
	public void doGet( HttpServletRequest request, HttpServletResponse response ){
		BasicRequestObject reqObj = RequestObjectFactory.getRequestObject( request );
		delegate.getLogger().info("doGet: reqObj:"+reqObj);
		int reqTypeCode = RIDRequestObject.INVALID_RID_URL;
		if ( reqObj != null ) {
			reqTypeCode = reqObj.checkRequest();
		}
		if ( reqTypeCode == RIDRequestObject.INVALID_RID_URL) {
			sendError( response, HttpServletResponse.SC_BAD_REQUEST, "Not a IHE RID URL!" );//missing or wrong request parameter
			return;
		}
		if ( reqTypeCode == RIDRequestObject.RID_REQUEST_NOT_SUPPORTED ) {
			sendError( response, HttpServletResponse.SC_NOT_FOUND, "This IHE RID request is not supported!" );
			return;
		} 
		WADOResponseObject resp = null; //use WADOResponseObject for encapsulate response.
		if ( reqTypeCode == RIDRequestObject.SUMMERY_INFO ) {
			resp = delegate.getRIDSummary( (RIDRequestObject) reqObj );
		} else if ( reqTypeCode == RIDRequestObject.DOCUMENT ) {
			resp = delegate.getRIDDocument( (RIDRequestObject) reqObj );
		} else {
			sendError( response, HttpServletResponse.SC_NOT_IMPLEMENTED, "This IHE RID request is not yet implemented!" );
			return;
		}
		if ( resp != null ) {
			int returnCode = resp.getReturnCode();
			delegate.getLogger().info("doGet: resp returnCode:"+returnCode);
			if ( returnCode == HttpServletResponse.SC_OK ) {
				sendResponse( response, resp );
			} else {
				sendError( response, returnCode, resp.getErrorMessage() );
			}
		} else {
			sendError( response, HttpServletResponse.SC_NOT_FOUND, "Not found!" );
		}
	}
	
	/**
	 * Send an error response with given response code and message to the client.
	 * <p>
	 * It is recommended that this method is only called once per erequest!<br>
	 * Otherwise a IllegalStateException will be thrown!
	 * 
	 * @param response	The HttpServletResponse of the request.
	 * @param errCode	One of the response code defined in HttpServletResponse.
	 * @param msg		A description for the reason to send the error.
	 */
	private void sendError( HttpServletResponse response, int errCode, String msg ) {
		try {
			response.sendError( errCode, msg );
		} catch (IOException e) {
			//delegate.getLogger().error("Cant perform sendError( "+errCode+", "+msg+" )! reason:"+e.getMessage(), e );
		}
	}
	
	/**
	 * Send the retrieved file to the client.
	 * <p>
	 * Sets the content type as defined in the WADOResponseObject object.
	 * 
	 * @param response
	 * @param respObject
	 */
	private void sendResponse( HttpServletResponse response, WADOResponseObject respObject ) {
		response.setHeader("Expires","0");//disables client side caching!!!
		delegate.getLogger().info("sendResponse"+respObject);
		try {
			if ( respObject != null ) {
				response.setContentType( respObject.getContentType() );
				try {
					delegate.getLogger().info("respObject execute");
					//respObject.execute( System.out );
					respObject.execute( response.getOutputStream() );
					response.getOutputStream().close();
				} catch ( Exception e ) {
					delegate.getLogger().error("Exception while writing RID response to client! reason:"+e.getMessage(), e );
				}
				
			}
		} catch ( Exception x ) {
			x.printStackTrace();
			sendError(	response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, x.getMessage() );
		}
		
	}

	
}
