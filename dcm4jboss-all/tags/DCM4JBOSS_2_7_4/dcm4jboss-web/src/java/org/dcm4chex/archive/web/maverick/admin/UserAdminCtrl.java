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
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
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
