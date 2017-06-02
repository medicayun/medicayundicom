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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser; 
import org.dcm4chee.xero.metadata.access.MapFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.servlet.FindAllTemplates;

/**
 * This is a factory that uses the FindAllTemplates, plus a list of registered templates to create a combined
 * list containing all the templates references from the given set of templates.
 * 
 * This is a map factory because it computes the total list dynamically, so as to allow for refreshes
 * of the child maps - there should be a way to make it use a fixed value instead (non-debug mode).
 * @author bwallace
 *
 */
public class AllTemplatesMap implements MetaDataUser, MapFactory<Map<String,StringTemplate>> {
	private static final Logger log = LoggerFactory.getLogger(AllTemplatesMap.class);
	
	List<String> startingTemplates;
	StringTemplateGroup templates;

	/** The meta-data is used to allow registration of additional lists of
	 * templates
	 */
	@SuppressWarnings("unchecked")
	public void setMetaData(MetaDataBean metaDataBean) {
		startingTemplates = new ArrayList<String>();
		for(Map.Entry<String,MetaDataBean> me : metaDataBean.metaDataEntrySet()) {
			Object val = me.getValue().getValue();
			if( val==null ) continue;
			if( val instanceof String ) {
				startingTemplates.add((String) val);
				log.info("Adding {} as a starting template.", val);
			}
			else if( val instanceof Map ) {
				log.info("Adding a child map set of values.");
				startingTemplates.addAll( ((Map<String,Object>) val).keySet() );
			}
			else if( val instanceof Collection ) {
				log.info("Adding a child list set of values.");
				startingTemplates.addAll( (Collection<String>) val );
			}
			else if( val instanceof AllTemplatesMap ) {
				log.info("Adding an all-templates list sub-child.");
				startingTemplates.addAll( ((AllTemplatesMap) val).startingTemplates );
			}
			else if( val instanceof StringTemplateGroup ) continue;
			else {
				log.warn("Unknown child type {}, not adding.", val.getClass());
			}
		}
		if( metaDataBean.getValue("templates")!=null ) templates = (StringTemplateGroup) metaDataBean.getValue("templateGroup");
		log.info("Found {} starting templates.");
	}

	/** Creates a list of stringtemplates that can be used to iterate over to
	 * compute things based on the set of templates to be used.
	 * 
	 * Uses "templateGroup" as the string template group - first looking in the children set by 
	 * setMetaData, and if that isn't found, looking for a peer named the same thing.
	 * 
	 * The returned list includes the names of all direct templates, super templates and indirect templates
	 * reachable from the provided list of templates.
	 * Eventually it is expected that there will also be an exclusion list to allow only additional templates
	 * to be added.
	 */
	public Map<String,StringTemplate> create(Map<String, Object> src) {
		StringTemplateGroup stg = templates;
		if( stg==null ) stg = (StringTemplateGroup) src.get("templateGroup");
		if( stg==null ) {
			log.warn("No templates defined to find all templates map.");
			return null;
		}
		return FindAllTemplates.findAllTemplates(stg,startingTemplates );
	}

}
