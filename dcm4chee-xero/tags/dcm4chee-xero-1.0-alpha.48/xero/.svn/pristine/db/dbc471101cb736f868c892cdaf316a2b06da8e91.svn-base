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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.dcm4chee.xero.metadata.access.MapFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlFactory implements MapFactory<String>, MetaDataUser {
   private static final Logger log = LoggerFactory.getLogger(UrlFactory.class);
   
   /** The base of the URL */
   String base;
   List<String> queryParams = new ArrayList<String>();
   
   /** Create a URL from the base query, plus the set of keys used to search on.
    * TODO implement this to return something dynamic instead of the static version it has right now.
    */
   public String create(Map<String, Object> src) {
	  StringBuffer ret = new StringBuffer(base);
	  log.info("Creating a URL starting from "+base);
	  char sep = '?';
	  if( base.indexOf('?')>=0 ) sep = '&';
	  for(String key : queryParams) {
		 Object value = src.get(key);
		 log.info("Checking "+key+"="+value);
		 if( value==null ) continue;
		 ret.append(sep);
		 sep = '&';
		 ret.append(key).append('=').append(value.toString());
	  }
	  return ret.toString();
   }

   /**
    * Sets the base name for the query - not including the method (http: or file: etc) or the server/port
    * but rather just the service name, eg /wado2/study.xml
    * @param base
    */
   @MetaData
   public void setBase(String base) {
	  this.base = base;
   }

   /** Gets the set of query parameters from the metadata. */
   public void setMetaData(MetaDataBean mdb) {
	  for(Map.Entry<String,MetaDataBean> me : mdb.metaDataEntrySet() ) {		 
		 Object value = me.getValue().getValue();
		 if( "query".equals(value) ) {
			queryParams.add(me.getKey());
		 }
	  }
   }
}
