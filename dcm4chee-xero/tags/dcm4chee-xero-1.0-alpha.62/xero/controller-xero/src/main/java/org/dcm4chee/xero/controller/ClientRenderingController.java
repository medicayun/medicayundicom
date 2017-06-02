// ***** BEGIN LICENSE BLOCK *****
// Version: MPL 1.1/GPL 2.0/LGPL 2.1
// 
// The contents of this file are subject to the Mozilla Public License Version 
// 1.1 (the "License"); you may not use this file except in compliance with 
// the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
// 
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
// for the specific language governing rights and limitations under the
// License.
// 
// The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at http://sourceforge.net/projects/dcm4che
//  
// The Initial Developer of the Original Code is Agfa Healthcare.
// Portions created by the Initial Developer are Copyright (C) 2009 the Initial Developer. All Rights Reserved.
// 
// Contributor(s):
// Andrew Cowan <andrew.cowan@agfa.com>
// 
// Alternatively, the contents of this file may be used under the terms of
// either the GNU General Public License Version 2 or later (the "GPL"), or
// the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
// in which case the provisions of the GPL or the LGPL are applicable instead
// of those above. If you wish to allow use of your version of this file only
// under the terms of either the GPL or the LGPL, and not to allow others to
// use your version of this file under the terms of the MPL, indicate your
// decision by deleting the provisions above and replace them with the notice
// and other provisions required by the GPL or the LGPL. If you do not delete
// the provisions above, a recipient may use your version of this file under
// the terms of any one of the MPL, the GPL or the LGPL.
// 
// ***** END LICENSE BLOCK *****

package org.dcm4chee.xero.controller;

import java.util.Map;

import javax.xml.transform.URIResolver;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.rhino.JavaScriptObjectWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pre-renders the client UI to HTML to improve the preceived performance of the client.
 * 
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class ClientRenderingController<T> implements Filter<T>
{
   @SuppressWarnings("unused")
   private static Logger log = LoggerFactory.getLogger(ClientRenderingController.class);
   
   private static final String PRERENDER_KEY = "prerender";
   
   public ClientRenderingController()
   {
   }
   
   /**
    * Update the client model with Javascript and pass it down the filter chain
    * to be rendered by string template.
    * 
    * @see org.dcm4chee.xero.metadata.filter.Filter#filter(org.dcm4chee.xero.metadata.filter.FilterItem,
    *      java.util.Map)
    */
   public T filter(FilterItem<T> filterItem, Map<String, Object> params)
   {
      // Allow prerendering to be turned off for testing purposes.
      if(!FilterUtil.getBoolean(params,PRERENDER_KEY,true))
         return filterItem.callNextFilter(params);
      
      Map<String, Object> model = FilterUtil.getModel(params);
      try 
      {
         // Assign the URIResolver to replace the XMLHttpRequest in the server.
         URIResolver resolver = FilterUtil.getURIResolver(params);
         String queryStr = FilterUtil.getString(params, "queryStr");
         
         JavaScriptObjectWrapper js = (JavaScriptObjectWrapper) model.get("js");
         JavaScriptObjectWrapper jsController = (JavaScriptObjectWrapper) js.get("controller");         
         if(jsController == null)
            throw new RuntimeException("Controller does not exist in Javascript context");
         
         jsController.callMethod("updateClientWithResolver",queryStr,resolver);
      }
      catch(Exception e)
      {
         throw new RuntimeException("Unable to load display page due to "+e,e);
      }
      
      return filterItem.callNextFilter(params);
   }
}
