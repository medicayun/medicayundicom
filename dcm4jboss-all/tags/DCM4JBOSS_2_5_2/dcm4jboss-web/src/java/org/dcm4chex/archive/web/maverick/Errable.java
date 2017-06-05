/*
 * Created on Feb 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.dcm4chex.archive.web.maverick;

import org.dcm4chex.archive.ejb.interfaces.AEManager;
import org.dcm4chex.archive.ejb.interfaces.AEManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;

/**
 * @author umberto.cappellini@tiani.com
 * Created: Feb 24, 2004 - 11:16:30 AM
 * Module: dcm4jboss-web
 * 
 */
public abstract class Errable extends Dcm4JbossController
{
	public static String ERROR_VIEW = "error";

	protected String errorType="Unknown Error";
	protected String message= "An Unrecognized Error Has Been Thrown";
	protected String backURL = "default.jsp";
	
	/**
	 * @return Returns the backURL.
	 */
	public final String getBackURL()
	{
		return backURL;
	}

	/**
	 * @return Returns the errorType.
	 */
	public final String getErrorType()
	{
		return errorType;
	}

	/**
	 * @return Returns the message.
	 */
	public final String getMessage()
	{
		return message;
	}
	
	protected AEManager lookupAEManager() throws Exception
	{
		AEManagerHome home =
			(AEManagerHome) EJBHomeFactory.getFactory().lookup(
					AEManagerHome.class,
					AEManagerHome.JNDI_NAME);
		return home.create();
	}			
	
}
