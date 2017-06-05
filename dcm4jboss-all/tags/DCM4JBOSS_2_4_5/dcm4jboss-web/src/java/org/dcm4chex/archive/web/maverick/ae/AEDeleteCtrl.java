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
public class AEDeleteCtrl extends Errable
{

	private int pk;

	/**
	 * @param oldPk The oldPk to set.
	 */
	public final void setPk(int pk)
	{
		this.pk = pk;
	}

	protected String perform() throws Exception
	{
		try
		{
		    AEManager mg = lookupAEManager();
		    AEData ae = mg.getAe(pk);
			mg.removeAE(pk);
			AuditLoggerDelegate.logActorConfig(getCtx(), "Removed AE: " + ae, "NetWorking");
			return "success";
		} catch (Throwable e)
		{
			this.errorType = e.getClass().getName();
			this.message = e.getMessage();
			this.backURL = "aedelete.m?pk=" + this.pk;
			return ERROR_VIEW;				
		}
	}		
}
