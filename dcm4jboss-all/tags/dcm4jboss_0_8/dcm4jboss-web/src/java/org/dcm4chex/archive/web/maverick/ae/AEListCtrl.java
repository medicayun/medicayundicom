/* $Id: AEListCtrl.java 1022 2004-03-01 13:30:54Z umbertoc $
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


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.dcm4chex.archive.web.maverick.Errable;

/**
 * @author umberto.cappellini@tiani.com
 */
public class AEListCtrl extends Errable
{
	public List getAEs() 
	{
		try
		{
			return lookupAEManager().getAes();
		} catch (RemoteException e)
		{
			e.printStackTrace();
			return new ArrayList();
		} catch (Exception e)
		{
			e.printStackTrace();
			return new ArrayList();			
		}
	}
}
