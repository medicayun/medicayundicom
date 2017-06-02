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
package org.dcm4chee.xero.controller;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.dcm4chee.xero.metadata.access.LazyMap;
import org.dcm4chee.xero.metadata.access.MapFactory;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.parsers.Parser;
import org.dcm4chee.xero.parsers.ParserException;
import org.dcm4chee.xero.parsers.StringParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller takes an http request object, along with the definitions for parameters, and
 * creates or updates a map containing parsed/validated request parameters.  
 * If the parsing or validation fails, it can create an alternative map that contains safe strings with
 * the values, plus an indication per-item of the error (or that this item is ok).
 * It ignores all attributes that aren't specified in the request.
 * 
 * @author bwallace
 *
 */
public class RequestValidator<T> implements MetaDataUser, Filter<T> {
   private static final Logger log = LoggerFactory.getLogger(RequestValidator.class);
   Map<String,Parser> parsers = new HashMap<String,Parser>();
   Map<String,Object> factories;
   
   /** The name of the variable that this will try to create */
   String variable;
   
   public static final String PARAMETERS = "_parameters";
   public static final String PARSERS_KEY = "_parsers";
   
   /** Creates the validated request object.
    * The params list is the set of parameter values, while the
    * model is inside that as the model element.
    * The created validated object is the same name as this filter object.
    */
   public T filter(FilterItem<T> filterItem, Map<String,Object> params) {
 	  log.info("Creating validated request object for {}", variable);
	  Map<String,Object> validated = lookupInstanceValue();
	  for(Map.Entry<String,Parser> me : parsers.entrySet()) {
		 Parser p = me.getValue();
		 log.info("Looking for value "+me.getKey());
		 Object v = null;
		 try {
			v = p.parse(params);
		 } catch (ParserException e) {
			// TODO - handle this exception somehow in the return object....
			e.printStackTrace();
			continue;
		 }
		 validated.put(me.getKey(),v);
	  }
	  Map<String,Object> model = FilterUtil.getModel(params);
	  model.put(variable, validated);
	  return filterItem.callNextFilter(params);
   }

   /** Gets an instance value for this object.  Returns a lazy map on the factories child
    * object - this allows created additional computed attributes on the validated result.
    */
   protected Map<String,Object> lookupInstanceValue() {
	  return new LazyMap(factories);
   }   

   /**
    * Search the meta-data for parsers and factories
    */
   public void setMetaData(MetaDataBean mdb) {
	  log.info("Set meta data on request validator.");
	  for(Map.Entry<String,MetaDataBean> me : mdb.metaDataEntrySet() ) {		 
		 Object value = me.getValue().getValue();
		 log.info("Testing "+me.getKey()+"="+value + " instanceof MapFactory "+(value instanceof MapFactory));
		 if( value instanceof Parser ) {
			log.info("Adding parser "+me.getKey());
			parsers.put(me.getKey(), (Parser) value);			
		 }
		 else if( value instanceof MapFactory ) {
			if( factories==null ) {
			   factories = new HashMap<String,Object>();
			}
			log.info("Putting factory "+me.getKey());
			factories.put(me.getKey(), (MapFactory<?>) value);
		 }
	  }
	  this.variable = mdb.getChildName();
   }
 
   /** The the map of parameters provided for this query. */
   @SuppressWarnings("unchecked")
   public static final Map<String,Object> getParameters(Map<String,Object> src) {
	  return (Map<String,Object>) src.get(PARAMETERS);
   }
   
   /** Get a safe string value */
   public static final String getString(Map<String,Object> src, String key) {
	  try {
		 Object val = getParameters(src).get(key);
		 if( val instanceof String[] ) val = ((String[]) val)[0];
		 String ret = (String) val;
		 if(ret==null ) return null;
		 StringParser.validate(ret);
		 return ret;
	  }
	  catch(ParserException pe) {
		 log.warn("Error parsing "+key+" value.");
		 throw new RuntimeException(pe);
	  }
   }
}
