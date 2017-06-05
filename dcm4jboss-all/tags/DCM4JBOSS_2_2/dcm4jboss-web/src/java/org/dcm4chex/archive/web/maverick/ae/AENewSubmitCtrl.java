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
import org.dcm4chex.archive.web.maverick.*;

/**
 * @author umberto.cappellini@tiani.com
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1389 $ $Date: 2005-01-07 22:55:52 +0800 (周五, 07 1月 2005) $
 */
public class AENewSubmitCtrl extends Errable
{

	private String title, hostName, cipherSuites, cipher1, cipher2, cipher3;
	private int port, pk;
	private String newPar = null;
	private String cancelPar = null;
	

	/**
	 * @param chiperSuites The chiperSuites to set.
	 */
	public final void setCipherSuites(String cipherSuites)
	{
		this.cipherSuites = cipherSuites;
	}

	public final void setCipher1(String cipher)
	{
		this.cipher1 = cipher;
	}
	public final void setCipher2(String cipher)
	{
		this.cipher2 = cipher;
	}
	public final void setCipher3(String cipher)
	{
		this.cipher3 = cipher;
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

	public final void setNew(String newPar)
	{
		this.newPar = newPar;
	}

	public final void setCancel(String cancel)
	{
		this.cancelPar = cancel;
	}
	
	private AEData getAE()
	{
		if (cipherSuites == null || cipherSuites.length() < 1 ) {
			StringBuffer sb = new StringBuffer(cipher1);
			if ( cipher2 != null && cipher2.length() > 0 ) sb.append(",").append( cipher2 );
			if ( cipher3 != null && cipher3.length() > 0 ) sb.append(",").append( cipher3 );
			cipherSuites = sb.toString();
		}
		return new AEData(
			0,
			this.title,
			this.hostName,
			this.port,
			this.cipherSuites);
	}

	protected String perform() throws Exception
	{
		if (newPar != null)
		{
			try
			{
				AEData newAE = getAE();
				lookupAEManager().newAE(newAE);
				AuditLoggerDelegate.logActorConfig(getCtx(), "Add new AE: " + newAE, "NetWorking");
				return "success";
			} catch (Throwable e)
			{
				this.errorType = e.getClass().getName();
				this.message = e.getMessage();
				this.backURL = "aenew.m";
				return ERROR_VIEW;				
			}
		}
		else
			return "success";			
	}
}
