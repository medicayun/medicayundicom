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

import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.web.maverick.Errable;

/**
 * @author umberto.cappellini@tiani.com
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
 */
public class AEEchoCtrl extends Errable
{
	private String title, hostName, cipherSuites, cipher1, cipher2, cipher3;
	private int port, pk;
	
	String aet;

	private boolean echoSucceed = false;
	private String echoResultMsg = null;
	
	private static AEDelegate delegate = null;

	
    private AEDelegate getDelegate() {
        if ( delegate == null ) {
        	delegate = new AEDelegate();
        	delegate.init( getCtx().getServletConfig() );
        }
        return delegate;
    }
	
	/**
	 * @param chiper1 The chiper1 to set.
	 */
	public final void setCipher1(String cipher1)
	{
		this.cipher1 = cipher1;
	}

	/**
	 * @param chiper2 The chiper2 to set.
	 */
	public final void setCipher2(String cipher2)
	{
		this.cipher2 = cipher2;
	}

	/**
	 * @param chiper3 The chiper3 to set.
	 */
	public final void setCipher3(String cipher3)
	{
		this.cipher3 = cipher3;
	}
	
	/**
	 * @param hostName The hostName to set.
	 */
	public final void setHostName(String hostName)
	{
		this.hostName = hostName;
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

	
	/**
	 * @return Returns the popupMsg.
	 */
	public String getEchoResultMsg() {
		return echoResultMsg;
	}
	
	private AEData getAE()
	{
		StringBuffer sb = new StringBuffer();
		if ( cipher1 != null && cipher1.length() > 0 ) sb.append( cipher1 );
		if ( cipher2 != null && cipher2.length() > 0 ) sb.append(",").append( cipher2 );
		if ( cipher3 != null && cipher3.length() > 0 ) sb.append(",").append( cipher3 );
		cipherSuites = sb.toString();
		return new AEData(
			pk,
			this.title,
			this.hostName,
			this.port,
			this.cipherSuites);
	}

	/**
	 * @return Returns the aet.
	 */
	public String getAet() {
		return aet;
	}
	/**
	 * @param aet The aet to set.
	 */
	public void setAet(String aet) {
		this.aet = aet;
	}
	protected String perform() throws Exception
	{
		if ( getAet() == null ) {
			echoResultMsg = getDelegate().echo( getAE(), 1);
		} else {
			echoResultMsg = getDelegate().echo( getAet(), 1);
		}
		echoSucceed = echoResultMsg.indexOf( "successfully") != -1;
		return "echoresult";
	}

	/**
	 * @return Returns the echoSucceed.
	 */
	public boolean isEchoSucceed() {
		return echoSucceed;
	}
}
