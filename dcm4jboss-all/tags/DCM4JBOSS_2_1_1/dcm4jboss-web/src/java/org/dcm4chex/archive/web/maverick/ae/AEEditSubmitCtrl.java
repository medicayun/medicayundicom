/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.ae;

import org.dcm4chex.archive.ejb.interfaces.AEManager;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.web.maverick.*;

/**
 * @author umberto.cappellini@tiani.com
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1389 $ $Date: 2005-01-07 22:55:52 +0800 (周五, 07 1月 2005) $
 */
public class AEEditSubmitCtrl extends Errable
{
	private String title, hostName, cipherSuites, cipher1, cipher2, cipher3;
	private int port, pk;

	private String update = null;
	private String cancel = null;

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

	public final void setUpdate(String update)
	{
		this.update = update;
	}

	public final void setCancel(String cancel)
	{
		this.cancel = cancel;
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

	private AEData getAE()
	{
		if (cipherSuites == null || cipherSuites.length() < 1 ) {
			StringBuffer sb = new StringBuffer(cipher1);
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

	protected String perform() throws Exception
	{
		if (update != null)
		{
			AEData newAE = getAE();
			try
			{
			    AEManager mg = lookupAEManager();
			    AEData oldAE = mg.getAe(pk);
				mg.updateAE(newAE);
				AuditLoggerDelegate.logActorConfig(getCtx(), "Modify AE: " + oldAE + " -> " + newAE, "NetWorking");
				return "success";
			} catch (Throwable e)
			{
				this.errorType = e.getClass().getName();
				this.message = e.getMessage();
				this.backURL = "aeedit.m?pk=" + pk;
				return ERROR_VIEW;
			}
		} else
			return "success";
	}

}
