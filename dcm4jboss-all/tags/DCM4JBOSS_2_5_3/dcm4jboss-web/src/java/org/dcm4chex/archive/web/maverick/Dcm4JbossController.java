/*
 * Created on Apr 29, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.dcm4chex.archive.web.maverick;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.infohazard.maverick.ctl.Throwaway2;

/**
 * Variation of Maverick's ThrowawayBean2, including support for 
 * input="image" HTML parameters.
 * 
 * @author <a href="mailto:umberto.cappellini@tiani.com">Umberto Cappellini</a>
 * Created: Apr 29, 2004 - 2:24:54 PM
 * Module: dcm4jboss-web
 */
public class Dcm4JbossController extends Throwaway2
{
	protected String perform() throws Exception
	{
		return SUCCESS;
	}

	/**
	 */
	protected final String go() throws Exception
	{
		
		if ( this.getCtx().getRequest().getSession().isNew() ) return "sessionChanged";
		
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

		BeanUtils.populate(this, modified_parameters);
		BeanUtils.populate(this, this.getCtx().getControllerParams());
		
		this.getCtx().setModel(this);
		
		return this.perform();
	}	
}
