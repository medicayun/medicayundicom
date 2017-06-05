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
 * Variation of Maverick's ThrowawayFormBeanUser, including support for 
 * input="image" HTML parameters.
 * 
 * @author <a href="mailto:umberto.cappellini@tiani.com">Umberto Cappellini</a>
 * Created: Apr 29, 2004 - 2:24:54 PM
 * Module: dcm4jboss-web
 */
public class Dcm4JbossFormController extends Throwaway2
{
	public static final String ERROR_PARSE_DATE = "parseError_date";
	public static final String ERROR_PARSE_TIME = "parseError_time";
	public static final String ERROR_PARSE_DATETIME = "parseError_datetime";
	
	/**
	 * The form bean gets set here
	 */
	private Object formBean;

	/**
	 */
	protected Object getForm()
	{
		return this.formBean;
	}

	/**
	 * Executes this controller.  Override one of the other perform()
	 * methods to provide application logic.
	 */
	public final String go() throws Exception
	{
		this.formBean = this.makeFormBean();
		Map modified_parameters = new HashMap();
		Map parameters = this.getCtx().getRequest().getParameterMap();
		modified_parameters.putAll(parameters);
		boolean btnPressed = false;
		for (Iterator i = parameters.keySet().iterator(); i.hasNext();)
		{
			String parameterName = (String)i.next();
			if (parameterName.endsWith(".x"))
			{
				btnPressed = true;
				String newName =
					parameterName.substring(0, parameterName.indexOf(".x"));
				modified_parameters.put(newName, newName);
			}
		}
		// This controller is used in folder main pages, and therefore check new session only if a (img-)button is pressed.(parameterName ends with .x)
		if ( btnPressed  && this.getCtx().getRequest().getSession().isNew() ) return "sessionChanged";

		BeanUtils.populate(this.formBean, modified_parameters);
		BeanUtils.populate(this.formBean, this.getCtx().getControllerParams());

		this.getCtx().setModel(this.formBean);

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

	/**
	 * This method will be called to produce a simple bean whose properties
	 * will be populated with the http request parameters.  The parameters
	 * are useful for doing things like persisting beans across requests.
	 *
	 * Default is to return this.
	 */
	protected Object makeFormBean()
	{
		return this;
	}
}
