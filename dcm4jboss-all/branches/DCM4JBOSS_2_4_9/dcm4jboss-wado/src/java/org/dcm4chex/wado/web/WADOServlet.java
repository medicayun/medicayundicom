/*
 * Created on 07.12.2004
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
import org.dcm4chex.wado.common.WADOResponseObject;
import org.dcm4chex.wado.common.WADORequestObject;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WADOServlet extends HttpServlet {

	private static final int BUF_LEN = 65536;
	
	/** holds the WADOServiceDelegate instance */
    private static WADOServiceDelegate delegate;
	
    /** serialVersionUID because super class is serializable. */
	private static final long serialVersionUID = 3257008748022085682L;
	
	/**
	 * Initialize the WADOServiceDelegator.
	 * <p>
	 * Set the name of the MBean from servlet init param.
	 */
	public void init() {
		delegate = new WADOServiceDelegate();
		delegate.init( getServletConfig() );
		RequestObjectFactory.setWADOextRequestType( delegate.getExtendedWADORequestType() );
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
		BasicRequestObject reqObject = RequestObjectFactory.getRequestObject( request );
		if ( reqObject == null || ! (reqObject instanceof WADORequestObject) ) {
			RequestObjectFactory.setWADOextRequestType( delegate.getExtendedWADORequestType() );//try if config has changed!
			reqObject = RequestObjectFactory.getRequestObject( request );
			if ( reqObject == null ) {
				sendError( response, HttpServletResponse.SC_BAD_REQUEST, "Not A WADO URL" );
				return;
			}
		}
		int iErr = reqObject.checkRequest();
		if ( iErr < 0 ) {
			sendError( response, HttpServletResponse.SC_BAD_REQUEST, reqObject.getErrorMsg() );//required params missing or invalid!
			return;
		} else if ( iErr == WADORequestObject.EXTENDED_WADO_URL ) {
			if ( ! delegate.isWadoExtAllowed() ) {
				sendError( response, HttpServletResponse.SC_BAD_REQUEST, "Not A WADO URL" );
				return;
			}
		}
		WADOResponseObject respObject = delegate.getWADOObject( (WADORequestObject)reqObject );
		int returnCode = respObject.getReturnCode();
		if ( returnCode == HttpServletResponse.SC_OK ) {
			sendWADOFile( response, respObject );
		} else if ( returnCode == HttpServletResponse.SC_TEMPORARY_REDIRECT ) {
			try {
				response.sendRedirect( respObject.getErrorMessage() ); //error message contains redirect host.
			} catch (IOException e) {
				sendError( response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: cant send redirect to client! Redirect to host "+respObject.getErrorMessage()+" failed!" );
			}
		} else {
			sendError( response, returnCode, respObject.getErrorMessage() );
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
			delegate.getLogger().error("Cant perform sendError( "+errCode+", "+msg+" )! reason:"+e.getMessage(), e );
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
	private void sendWADOFile( HttpServletResponse response, WADOResponseObject respObject ) {
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
					delegate.getLogger().error("Exception while writing WADO response to client! reason:"+e.getMessage(), e );
				}
				
			}
		} catch ( Exception x ) {
			x.printStackTrace();
			sendError(	response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, x.getMessage() );
		}
	}

}
