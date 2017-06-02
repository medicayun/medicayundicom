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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.servlet.StringSafeRenderer;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The template group name can be set as a comma delimited set of names, or from
 * the meta-data itself to allow auto- configuration. The parent can also be set
 * as a child element, allowing nested definitions with customized setup
 * per-definition.
 * 
 * @author bwallace
 * 
 */
public class AutoStringTemplateGroup extends StringTemplateGroup implements MetaDataUser {
	private static final Logger log = LoggerFactory.getLogger(AutoStringTemplateGroup.class);
	StringTemplateGroup[] stgParents;
	private String template;
	
	MetaDataBean modelMdb;

	/** Create a StringTempalteGroup */
	public AutoStringTemplateGroup() {
		super("unknown");
		this.setAttributeRenderers(StringSafeRenderer.RENDERERS);
	}

    public AutoStringTemplateGroup(String ...groups) {
        super("unknown");
        this.setAttributeRenderers(StringSafeRenderer.RENDERERS);
        this.setGroupNames(groups);
    }

    @MetaData(required = false)
	public void setSuperGroup(StringTemplateGroup superGroup) {
		super.setSuperGroup(superGroup);
		this.setAttributeRenderers(StringSafeRenderer.RENDERERS);
		superGroup.setAttributeRenderers(StringSafeRenderer.RENDERERS);
	}

	/**
	 * Sets the group names - if this has only 1 name, then the super group won't
	 * be set, otherwise the super group will also be set. Takes either a
	 * comma-separated String, or a List of Strings.
	 * 
	 * @param names
	 */
	@MetaData(required = false)
	public void setGroupNames(Object names) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String[] parents;
		if (names instanceof String) {
			parents = ((String) names).split(",");
		} else if (names instanceof List) {
			List<?> lnames = (List<?>) names;
			parents = new String[lnames.size()];
			lnames.toArray(parents);
		} else if( names instanceof String[]) {
		    parents = (String[]) names;
		} else {
			throw new IllegalArgumentException("Names must be a String or a List, but is "
			      + (names == null ? "null" : (names.getClass().toString())));
		}
		stgParents = new StringTemplateGroup[parents.length];
		StringTemplateGroup parentSTG = null;
		int refresh = this.getRefreshInterval();
		for (int i = parents.length - 1; i >= 0; i--) {
			parents[i] = parents[i].trim();
			URL url = cl.getResource(parents[i]);
			if (url == null) {
				throw new IllegalArgumentException("No resource named " + parents[i]);
			}
			String rootDir = url.getFile();
			StringTemplateGroup ret;
			if (i > 0) {
				if (url.toString().endsWith(".stg")) {
					try {
						log.info("Loading string template group file {}", url);
						InputStreamReader r = new InputStreamReader(url.openStream());
						ret = new StringTemplateGroup(r);
				        ret.setAttributeRenderers(StringSafeRenderer.RENDERERS);
					} catch (IOException e) {
						throw new RuntimeException("Unable to read " + url);
					}
					ret.setSuperGroup(parents[i]);
				} else {
					log.info("Loading string template directory {}", rootDir);
					ret = new StringTemplateGroup(parents[i], rootDir);
			        ret.setAttributeRenderers(StringSafeRenderer.RENDERERS);
				}
				if (refresh > 0)
					ret.setRefreshInterval(refresh);
			} else {
				ret = this;
				setName(parents[i]);
				setRootDir(rootDir);
			}
			stgParents[i] = ret;
			log.info("root resource for {} is {}", parents[i], rootDir);
			if (rootDir == null || isArchivePath(rootDir)) {
				log.info("Using root resource {}", parents[i]);
				// Just use the name directly as the root resource.
				ret.setRootResource(parents[i]);
				ret.setRootDir(null);
			}
			if (parentSTG != null)
				ret.setSuperGroup(parentSTG);
			parentSTG = ret;
		}
		log.info("Name is " + this.getName() + " parents[0]=" + parents[0]);
	}

   /**
    * Determine if the path represents a resource or directory.
    * <p>
    * NOTE:  JBoss 4 returns resources that contain ZIP syntax URLs (.jar!) and
    * JBoss 5 returns a VFS path that makes it look like a normal directory.
    */
   protected boolean isArchivePath(String path)
   {
      return path != null && ( path.indexOf('!') > 0 || path.indexOf(".jar/")>=0 );
   }

	@MetaData(required = false)
	public void setName(String name) {
		super.setName(name);
	}

	@MetaData(required = false)
	public void setRefreshInterval(int refreshInterval) {
		super.setRefreshInterval(refreshInterval);
		if (stgParents == null)
			return;
		log.info("Setting refresh interval to %d", refreshInterval);
		for (int i = 1; i < stgParents.length; i++) {
			stgParents[i].setRefreshInterval(refreshInterval);
		}
	}

	/** This is not declared in the parent class, so add it here */
	@SuppressWarnings("unchecked")
	public Map getAttributeRenderers() {
		return this.attributeRenderers;
	}

	/**
	 * Sets the attribute renderers object - should be able to do this
	 * dynamically, but that isn't so easy because the types are based on class
	 * names. The better, long term solution is to have it auto-register for a
	 * variety of types, but register by format name.
	 * 
	 * @param name
	 */
	@MetaData(required = false)
	public void setAttributeRenderersName(String name) {
		if (name.equals("js")) {
			this.setAttributeRenderers(StringSafeRenderer.JS_RENDERERS);
		} else
			throw new IllegalArgumentException("Unknown renderers name - TODO implement a pluggable mechanism, name:"
			      + name);
	}

	@MetaData(required=false)
	public void setTemplate(String template) {
		this.template = template;
	}

	/** Gets the default template name to render */
	public String getTemplate() {
		return this.template;
	}
	
	/** Render the template/model object and return the result */
	@Override
	public String toString() {
		if( this.template==null ) {
			return super.toString();
		}

		Map<?,?> model = null;
		if( modelMdb!=null ) {
			model = (Map<?,?>) modelMdb.getValue();
		}
		StringTemplate st = this.getInstanceOf(this.template,model);
		String ret = st.toString();
		log.debug("Return script:{}",ret);
		return ret;
	}

	/** Sets the meta-data so that the model provider can be retrieved */
   public void setMetaData(MetaDataBean mdb) {
		modelMdb = mdb.getChild("model"); 
   }
}
