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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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
package org.dcm4chee.xero.template;

import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.servlet.ErrorResponseItem;
import org.dcm4chee.xero.metadata.servlet.MetaDataServlet;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a filter based on StringTemplate. It creates the model,
 * adds it to the params, and then calls the next filter. It then renders the
 * specified template in the templateGroup, as defined by the model, or sets
 * them to be the values as defined by the defaults. If no default is specified,
 * the template will be the servlet path, excluding the dot elements, eg:
 * /xero/controller/display.jst would map to the view controller.display, unless
 * a default view name is used, in which case the default view will always be
 * used, or a view is specified in the model after the next filter is called.
 * The next filter can modify the model by calling various events or directly,
 * returning null to cause this filter to render, or it can return a servlet
 * response item directly, causing this servlet to just return the item.
 * 
 * If no model object is found in the metadata, a default hash map will be used.
 * 
 * The params are NOT available by default in the model - they MUST be copied
 * over into the model by some controller object. This prevents their being used
 * directly in the view with some XSS type attack. The only thing available in
 * the model by default (unless items are defined in the metadata), is the
 * template name, the templateGroup, and the contentType. (TODO - consider
 * whether to allow params to be available)
 * 
 * @author bwallace
 * 
 */
public class StringTemplateFilter implements Filter<ServletResponseItem>, MetaDataUser {
	public static final String MODEL_KEY = "model";

	private static Logger log = LoggerFactory.getLogger(StringTemplateFilter.class);

	public static final String TEMPLATE_NAME = "template";
	public static final String TEMPLATE_GROUP = "templateGroup";
	public static final String CONTENT_TYPE_NAME = "contentType";
	/** The default view is the one to show if no other view is defined */
	String defaultView;

	/** The default content type (xhtml unless otherwise specified) */
	String defaultContentType;

	/** The error view is to be shown when an exception is thrown */
	String errorView;

	/**
	 * The default group is the string template set to use if no other one is
	 * specified.
	 */
	StringTemplateGroup defaultGroup;

	MetaDataBean mdbModel;

	/**
	 * This method will return an object that renders the specified view. The
	 * provided model is used for the source data. It is recommended that all
	 * objects have been validated before they are permitted to be used.
	 */
	public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params) {
		try {
			Map<String, Object> model = FilterUtil.getModel(params,mdbModel);
			ServletResponseItem sri = filterItem.callNextFilter(params);
			if (sri != null)
				return sri;

			String template = (String) model.get(TEMPLATE_NAME);
			StringTemplateGroup stg = (StringTemplateGroup) model.get(TEMPLATE_GROUP);
			String contentType = (String) model.get(CONTENT_TYPE_NAME);
			if (contentType == null)
				contentType = defaultContentType;
			if (stg == null) {
				stg = defaultGroup;
				// No group - could mean another filter will return the stg.
				if (stg == null)
					return null;
				model.put(TEMPLATE_GROUP, stg);
			}
			if (template == null) {
			   template = defaultView;
               if (template == null)
					template = templateFromParams(params);
                log.debug("Model template {} default was {}", template, defaultView);
				if (template == null) {
					log.warn("View {} not found - returning 404", template);
					return new ErrorResponseItem(404, "View name not defined.");
				}
				model.put("template", template);
			}
			if (stg == null) {
				log.warn("String template group {} not found - returning 404", TEMPLATE_GROUP);
				return new ErrorResponseItem(404, "String template group not defined.");
			}
			log.info("Templating view {} from {}", template, stg.getName());
			StringTemplate st = stg.getInstanceOf(template, model);
			StringTemplateResponseItem stri = new StringTemplateResponseItem(st, stg, errorView);
			if (contentType != null)
				stri.setContentType(contentType);
			return stri;
		} catch (Exception e) {
			log.error("Caught exception " + e, e);
			return new ErrorResponseItem(404, "Internal server error:" + e);
		}
	}

	/** Generates the template name based on the URI used to call the object */
	public String templateFromParams(Map<String, Object> params) {
		String uri = (String) params.get(MetaDataServlet.REQUEST_URI);
		log.info("Template name is based on {}", uri);
		if (uri == null || uri.equals("") )
			return "default";
		int pos = uri.lastIndexOf('.');
		if (pos > 0)
			uri = uri.substring(0, pos);
		pos = uri.indexOf('/', 1);
		if (pos ==-1 ) {
		    // This is only required for root-context servlets.  For those
		    // servlets, it is NOT possible to render sub-directories right now.
		    // TODO Add a setting to allow this.
			uri = uri.substring(1);
		} else {
		   uri = uri.substring(pos + 1);
		   uri = uri.replace('/', '.');
		}
        uri = uri.trim();
		if( uri.equals("") ) uri="default";
		log.info("Final template name is '{}'", uri);
		return uri;
	}

	public String getDefaultView() {
		return defaultView;
	}

	@MetaData(required = false)
	public void setDefaultView(String defaultView) {
		this.defaultView = defaultView;
	}

	public String getDefaultContentType() {
		return defaultContentType;
	}

	@MetaData(required = false)
	public void setDefaultContentType(String defaultContentType) {
		this.defaultContentType = defaultContentType;
	}

	public String getErrorView() {
		return errorView;
	}

	@MetaData(required = false)
	public void setErrorView(String errorView) {
		this.errorView = errorView;
	}

	public StringTemplateGroup getDefaultGroup() {
		return defaultGroup;
	}

	@MetaData(required = false)
	public void setDefaultGroup(StringTemplateGroup defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	public void setDefaultGroupName(String defaultGroupName) {

	}

	public void setMetaData(MetaDataBean metaDataBean) {
		mdbModel = metaDataBean.getChild(MODEL_KEY);
	}
}
