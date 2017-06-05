/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.web.maverick.ae;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.web.maverick.BasicFormModel;

/**
 * @author franz.willer
 *
 * The Model for Teaching File Selector WEB interface.
 */
public class AEModel extends BasicFormModel {

	/** The session attribute name to store the model in http session. */
	public static final String AE_ATTR_NAME = "aeModel";
	
	private static Logger log = Logger.getLogger( AEModel.class.getName() );

	private String user;
	private String title, hostName, cipherSuites, cipher1, cipher2, cipher3;
	private int port=-1, pk=-1;
	
	private boolean checkHost = false;
	
	/**
	 * @param chiperSuites The chiperSuites to set.
	 */
	public final void setCipherSuites(String cipherSuites)
	{
		this.cipherSuites = cipherSuites;
	}

	/**
	 * @param hostName The hostName to set.
	 */
	public final void setHostName(String hostName)
	{
		this.hostName = hostName;
	}
	
	public final void setCheckHost( boolean checkHost ) {
		this.checkHost = checkHost;
	}

	/**
	 * @return Returns the checkHost.
	 */
	public boolean isCheckHost() {
		return checkHost;
	}
	/**
	 * @param port The port to set.
	 */
	public final void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * @param title The title to set.
	 */
	public final void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @param oldPk The oldPk to set.
	 */
	public final void setPk(int pk)
	{
		this.pk = pk;
	}

	
	public final void setCipher1(String cipher)
	{
		this.cipher1 = cipher;
		cipherSuites = null;
	}
	public final void setCipher2(String cipher)
	{
		this.cipher2 = cipher;
		cipherSuites = null;
	}
	public final void setCipher3(String cipher)
	{
		this.cipher3 = cipher;
		cipherSuites = null;
	}
	
	public AEData getAE()
	{
		if (cipherSuites == null || cipherSuites.length() < 1 ) {
			StringBuffer sb = new StringBuffer();
			if ( cipher1 != null && cipher1.length() > 0 ) sb.append( cipher1 );
			if ( cipher2 != null && cipher2.length() > 0 ) sb.append(",").append( cipher2 );
			if ( cipher3 != null && cipher3.length() > 0 ) sb.append(",").append( cipher3 );
			cipherSuites = sb.toString();
		}
		return new AEData(
			pk,
			this.title,
			this.hostName,
			this.port,
			this.cipherSuites);
	}
	
	public void setAE( AEData ae ) {
		pk = ae.getPk();
		title = ae.getTitle();
		hostName = ae.getHostName();
		port = ae.getPort();
		cipherSuites = ae.getCipherSuitesAsString();
	}
	
	public void setAet(String aet) {
		title = aet;
		pk = -1;
		port = -1;
		hostName = null;
		cipherSuites = cipher1 = cipher2 = cipher3 = null;
	}


	/**
	 * Creates the model.
	 * <p>
	 */
	protected AEModel(String user, HttpServletRequest request) {
		super(request);
		this.user = user;
	}
	
	/**
	 * @return Returns the user.
	 */
	public String getUser() {
		return user;
	}
	
	/**
	 * Get the model for an http request.
	 * <p>
	 * Look in the session for an associated model via <code>AE_ATTR_NAME</code><br>
	 * If there is no model stored in session (first request) a new model is created and stored in session.
	 * 
	 * @param request A http request.
	 * 
	 * @return The model for given request.
	 */
	public static final AEModel getModel( HttpServletRequest request ) {
		AEModel model = (AEModel) request.getSession().getAttribute(AE_ATTR_NAME);
		if (model == null) {
				model = new AEModel(request.getUserPrincipal().getName(), request);
				request.getSession().setAttribute(AE_ATTR_NAME, model);
				model.setErrorCode( NO_ERROR ); //reset error code
		}
		return model;
	}

	public String getModelName() { return "AEMgr"; }
	

}
