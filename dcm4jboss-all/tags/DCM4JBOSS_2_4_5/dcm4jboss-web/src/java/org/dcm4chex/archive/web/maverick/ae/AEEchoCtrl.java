/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.ae;

import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.web.maverick.Errable;

/**
 * @author umberto.cappellini@tiani.com
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1677 $ $Date: 2005-04-08 18:50:04 +0800 (周五, 08 4月 2005) $
 */
public class AEEchoCtrl extends Errable
{
	private String title, hostName, cipherSuites, cipher1, cipher2, cipher3;
	private int port, pk;
	
	String aet;

	private boolean echoSucceed = false;
	private String echoResultMsg = null;
	
	private static EchoDelegate delegate = null;

	
    private EchoDelegate getDelegate() {
        if ( delegate == null ) {
        	delegate = new EchoDelegate();
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
