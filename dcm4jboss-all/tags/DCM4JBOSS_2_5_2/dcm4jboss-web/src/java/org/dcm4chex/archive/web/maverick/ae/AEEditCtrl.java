/* $Id: AEEditCtrl.java 1698 2005-04-15 12:41:51Z javawilli $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.web.maverick.ae;

import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.web.maverick.*;

/**
 * @author umberto.cappellini@tiani.com
 */
public class AEEditCtrl extends Errable
{
	private int pk;
	
	public AEData getAE() throws Exception
	{
		try {
		return lookupAEManager().getAe(pk);
		} catch ( Exception x ) {
			x.printStackTrace();
		}
		return null;
	}

	protected String perform() throws Exception 
	{
		try
		{
			getAE();
			this.errorType = "";
			this.message = "";
			this.backURL= "";
			return "success";
		}
		catch (Exception e)
		{
			this.errorType = e.getClass().getName();
			this.message = e.getMessage();
			this.backURL= ""; //TBD
			return "error";
		}	
	}
	/**
	 * @param pk The pk to set.
	 */
	public final void setPk(int pk)
	{
		this.pk = pk;
	}
	


}


