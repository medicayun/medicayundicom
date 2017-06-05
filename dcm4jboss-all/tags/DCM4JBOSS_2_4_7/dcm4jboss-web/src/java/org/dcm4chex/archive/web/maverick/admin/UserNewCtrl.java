/* $Id: UserNewCtrl.java 1812 2005-06-23 11:13:28Z javawilli $
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

import org.apache.log4j.Logger;

/**
 * @author umberto.cappellini@tiani.com
 */
public class UserNewCtrl extends UserAdminCtrl
{
	private static Logger log = Logger.getLogger(UserNewCtrl.class.getName());
	
	protected String perform() throws Exception 
	{
		UserAdminModel model = getModel();
		if ( !model.isAdmin()) {
			log.warn("Illegal access to UserNewCtrl! User "+this.getCtx().getRequest().getUserPrincipal()+" is not in role WebAdmin!");
			return "error";
		}
		if ( getCtx().getRequest().getParameter("newUser") != null ) { //set from button 'new user'
			model.selectEditUser(null); //reset editUser
		}
		return "success";
	}

}


