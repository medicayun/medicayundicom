/*
 * Created on 07.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chex.wado.common.WADORequestObject;
import org.dcm4chex.wado.common.WADOResponseObject;

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
		WADORequestObject reqObject = new WADORequestObjectImpl( request );
		int iErr = reqObject.checkRequest();
		if ( iErr == WADORequestObject.INVALID_WADO_URL ) {
			sendError( response, HttpServletResponse.SC_BAD_REQUEST, "Not a WADO URL!" );//required params missing!
		} else if ( iErr == WADORequestObject.INVALID_ROWS ) {
				sendError( response, HttpServletResponse.SC_BAD_REQUEST, "Error: rows parameter is inavlid! Must be an integer string." );
		} else if ( iErr == WADORequestObject.INVALID_COLUMNS ) {
				sendError( response, HttpServletResponse.SC_BAD_REQUEST, "Error: columns parameter is inavlid! Must be an integer string." );
		} else if ( iErr == WADORequestObject.INVALID_FRAME_NUMBER ) {
			sendError( response, HttpServletResponse.SC_BAD_REQUEST, "Error: frameNumber parameter is inavlid! Must be an integer string." );
		} else if ( iErr == WADORequestObject.OK ) { 
			WADOResponseObject respObject = delegate.getWADOObject( reqObject );
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
		} else {
			sendError( response, HttpServletResponse.SC_BAD_REQUEST, "Error: Please check URL parameter!" );
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
		try {
			InputStream stream = respObject.getStream();
			if ( stream != null ) {
				response.setContentType( respObject.getContentType() );
				
				OutputStream out = response.getOutputStream();
				InputStream in;
				if ( stream instanceof BufferedInputStream ) {
					in = stream;
				} else {
					in = new BufferedInputStream( stream, BUF_LEN );
				}
				byte[] buf = new byte[BUF_LEN];
				try {
					int len = in.read( buf );
					while ( len > 0 ) {
						out.write( buf, 0, len );
						len = in.read( buf );
					}
				} catch ( Exception e ) {
					delegate.getLogger().error("Exception while writing WADO file to client! reason:"+e.getMessage(), e );
				} finally {
					in.close();
				}
			}
		} catch ( Exception x ) {
			x.printStackTrace();
			sendError(	response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, x.getMessage() );
		}
		
	}

}
