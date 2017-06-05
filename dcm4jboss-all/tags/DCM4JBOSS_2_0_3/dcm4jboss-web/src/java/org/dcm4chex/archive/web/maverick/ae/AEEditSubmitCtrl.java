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
 * @version $Revision: 1181 $ $Date: 2004-10-15 07:08:49 +0800 (周五, 15 10月 2004) $
 */
public class AEEditSubmitCtrl extends Errable
{
	private String title, hostName, chiperSuites;
	private int port, pk;

	private String update = null;
	private String cancel = null;

	/**
	 * @param chiperSuites The chiperSuites to set.
	 */
	public final void setChiperSuites(String chiperSuites)
	{
		this.chiperSuites = chiperSuites;
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

	private AEData getAE()
	{
		return new AEData(
			pk,
			this.title,
			this.hostName,
			this.port,
			this.chiperSuites);
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
