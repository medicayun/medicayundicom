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
 * @version $Revision: 2573 $ $Date: 2006-06-29 00:14:25 +0800 (周四, 29 6月 2006) $
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
	public AEData getAe(long aePk) throws EJBException
	{
		try
		{
			AELocal ae = aeHome.findByPrimaryKey(new Long(aePk));
			AEData aeDTO =
				new AEData(
						ae.getPk().longValue(),
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
	public AEData getAeByTitle(String aet)
	{
		try
		{
			AELocal ae = aeHome.findByAET( aet );
			AEData aeDTO =
				new AEData(
						ae.getPk().longValue(),
						ae.getTitle(),
						ae.getHostName(),
						ae.getPort(),
						ae.getCipherSuites());
			return aeDTO;
		} catch (FinderException e)
		{
			return null;
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
						ae.getPk().longValue(),
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
				AELocal ae = aeHome.findByPrimaryKey(new Long(modAE.getPk()));
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
	public void removeAE(long aePk) throws Exception
	{
		try
		{
			aeHome.remove(new Long(aePk));
		} catch (RemoveException e)
		{
			throw new Exception(e);
		}
	}

}
