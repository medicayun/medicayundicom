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

import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.servlet.MetaDataServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Adds the IS_X browser identifications.
 * Also adds firebug on/off flag.
 * 
 * @author bwallace
 * @param <T>
 */
public class BrowserVersion<T> implements Filter<T> {
	private static Logger log = LoggerFactory.getLogger(BrowserVersion.class);

	/** Adds browser identification information */
   public T filter(FilterItem<T> filterItem, Map<String, Object> params) {
 	  String userAgent = MetaDataServlet.getUserAgent(params);
 	  Map<String,Object> model = FilterUtil.getModel(params);
 	  boolean isFirefox = false;
 	  boolean isIE = false;
 	  boolean isIE6 = false;
 	  log.debug("User agent is {}", userAgent);
 	  if( (userAgent.indexOf("msie")>=0) && (userAgent.indexOf("opera")==-1) ) {
 	 	  isIE = true;
 		  isIE6 = (userAgent.indexOf("msie 6")>=0);
 	  }
 	  else {
 		  isFirefox = (userAgent.indexOf("firefox")>=0);
 		  // As needed, add more browser identifications.
 	  }
 	  
      model.put("IS_IE", isIE);
      model.put("IS_IE6", isIE6);
      model.put("HAS_SVG", !isIE);
      model.put("HAS_VML", isIE);
 	  model.put("IS_FIREFOX", isFirefox); 
 	  model.put("IS_MOBILE", userAgent.indexOf("Mobile")>=0 || userAgent.indexOf("BlackBerry")>=0);
 	  
 	  if( params.containsKey("firebug") ) model.put("firebug", params.get("firebug"));
 	  model.put("userName", params.get("userName"));
 	  if( filterItem==null ) return null;
 	  return filterItem.callNextFilter(params);
   }

}
