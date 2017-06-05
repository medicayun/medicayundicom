/* $Id: AENewSubmitCtrl.java 1098 2004-04-29 08:55:13Z umbertoc $
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
public class AENewSubmitCtrl extends Errable
{

	private String title, hostName, chiperSuites;
	private int port, pk;
	private String newPar = null;
	private String cancelPar = null;
	

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
		return new AEData(
			0,
			this.title,
			this.hostName,
			this.port,
			this.chiperSuites);
	}

	protected String perform() throws Exception
	{
		if (newPar != null)
		{
			try
			{
				AEData newAE = getAE();
				lookupAEManager().newAE(newAE);
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
