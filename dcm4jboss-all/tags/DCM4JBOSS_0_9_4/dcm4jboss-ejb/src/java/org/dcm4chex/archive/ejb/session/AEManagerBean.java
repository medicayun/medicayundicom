/* $Id: AEManagerBean.java 1023 2004-03-01 13:59:36Z umbertoc $
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
package org.dcm4chex.archive.ejb.session;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4chex.archive.ejb.interfaces.AELocal;
import org.dcm4chex.archive.ejb.interfaces.AELocalHome;
import org.dcm4chex.archive.ejb.jdbc.AEData;

/**
 * 
 * @author <a href="mailto:umberto.cappellini@tiani.com">Umberto Cappellini</a>
 * @version $Revision: 1023 $ $Date: 2004-03-01 21:59:36 +0800 (周一, 01 3月 2004) $
 * @since 14.01.2004
 * 
 * @ejb.bean
 *  name="AEManager"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/AEManager"
 * 
 * @ejb.transaction-type 
 *  type="Container"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.ejb-ref
 *  ejb-name="AE" 
 *  view-type="local"
 *  ref-name="ejb/AE" 
 */
public abstract class AEManagerBean implements SessionBean
{

	private AELocalHome aeHome;
	private SessionContext ctx;

	public void setSessionContext(SessionContext ctx)
		throws EJBException, RemoteException
	{		
		Context jndiCtx = null;
		try
		{
			jndiCtx = new InitialContext();
			aeHome = (AELocalHome) jndiCtx.lookup("java:comp/env/ejb/AE");
		} catch (NamingException e)
		{
			throw new EJBException(e);
		} finally
		{
			if (jndiCtx != null)
			{
				try
				{
					jndiCtx.close();
				} catch (NamingException ignore)
				{
				}
			}
		}
		this.ctx = ctx;
	}

	public void unsetSessionContext()
	{
		aeHome = null;
		ctx = null;
	}

	/**
	 * @ejb.interface-method
	 */
	public AEData getAe(int aePk) throws EJBException
	{
		try
		{
			AELocal ae = aeHome.findByPrimaryKey(new Integer(aePk));
			AEData aeDTO =
				new AEData(
						ae.getPk().intValue(),
						ae.getTitle(),
						ae.getHostName(),
						ae.getPort(),
						ae.getCipherSuites());
			return aeDTO;
		} catch (FinderException e)
		{
			throw new EJBException(e);
		}
	}
	
	
	/**
	 * @ejb.interface-method
	 */
	public List getAes() throws EJBException
	{
		try
		{
			ArrayList ret = new ArrayList();
			for (Iterator i = aeHome.findAll().iterator(); i.hasNext();)
			{
				AELocal ae = (AELocal) i.next();
				AEData aeDTO =
					new AEData(
						ae.getPk().intValue(),
						ae.getTitle(),
						ae.getHostName(),
						ae.getPort(),
						ae.getCipherSuites());
				ret.add(aeDTO);
			}
			return ret;
		} catch (FinderException e)
		{
			throw new EJBException(e);
		}
	}

	/**
	 * @ejb.interface-method
	 */
	public void updateAE(AEData modAE) throws FinderException
	{
		try
		{
				AELocal ae = aeHome.findByPrimaryKey(new Integer(modAE.getPk()));
				ae.setTitle(modAE.getTitle());
				ae.setHostName(modAE.getHostName());
				ae.setPort(modAE.getPort());
				ae.setCipherSuites(modAE.getCipherSuitesAsString());
		} catch (FinderException e)
		{
			ctx.setRollbackOnly();
			throw e;
		}
	}

	/**
	 * @ejb.interface-method
	 */
	public void newAE(AEData newAE) throws CreateException
	{
		aeHome.create(
			newAE.getTitle(),
			newAE.getHostName(),
			newAE.getPort(),
			newAE.getCipherSuitesAsString());
	}

	/**
	 * @ejb.interface-method
	 */
	public void removeAE(int  aePk) throws Exception
	{
		try
		{
			aeHome.remove(new Integer(aePk));
		} catch (RemoveException e)
		{
			throw new Exception(e);
		}
	}

}
