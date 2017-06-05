/* $Id: UserAdminCtrl.java 1812 2005-06-23 11:13:28Z javawilli $
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
package org.dcm4chex.archive.web.maverick.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.infohazard.maverick.ctl.Throwaway2;

/**
 * 
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 1812 $ $Date: 2005-06-23 19:13:28 +0800 (周四, 23 6月 2005) $
 * @since 22.06.2005
 */
public class UserAdminCtrl extends Throwaway2 {


	protected static Logger log = Logger.getLogger(UserAdminCtrl.class);
    
	UserAdminModel model;
    
    protected UserAdminModel getModel() {
    	return model;
    }
    
    protected Object makeFormBean() {
        return UserAdminModel.getModel(getCtx().getRequest());
    }

	/**
	 * Executes this controller.  Override one of the other perform()
	 * methods to provide application logic.
	 */
	public final String go() throws Exception
	{
		this.model = (UserAdminModel) this.makeFormBean();
		Map modified_parameters = new HashMap();
		Map parameters = this.getCtx().getRequest().getParameterMap();
		modified_parameters.putAll(parameters);
		for (Iterator i = parameters.keySet().iterator(); i.hasNext();)
		{
			String parameterName = (String)i.next();
			if (parameterName.endsWith(".x"))
			{
				String newName =
					parameterName.substring(0, parameterName.indexOf(".x"));
				modified_parameters.put(newName, newName);
			}
		}

		BeanUtils.populate(this.model, modified_parameters);
		BeanUtils.populate(this.model, this.getCtx().getControllerParams());

		this.getCtx().setModel(this.model);

		return this.perform();
	}

	/**
	 * This method can be overriden to perform application logic.
	 *
	 * Override this method if you want the model to be something
	 * other than the formBean itself.
	 *
	 * @param formBean will be a bean created by makeFormBean(),
	 * which has been populated with the http request parameters.
	 */
	protected String perform() throws Exception
	{
		return SUCCESS;
	}

}
