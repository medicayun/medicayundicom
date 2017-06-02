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
 * Portions created by the Initial Developer are Copyright (C) 2007
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
package org.dcm4chee.xero.metadata.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet uses either JndiMetaData or SeamMetaData to get a list of
 * filters that can provide get/post responses based on query parameters, and
 * perhaps based on the content types accepted. The intent of this type of
 * servlet is that any two requests with the same parameters retrieve the same
 * data - except for changes to the underlying data made between requests. There
 * might be per-user filters that allow/deny the request based on the
 * parameters, but excluding that, the same results should be returned all the
 * time. If filtering per user/role/group is required, then the user/role/group
 * should be a parameter to this servlet and understood by the underlying
 * filters.
 * 
 * Usage in web.xml:
 * 
 * <pre>
 *  &lt;servlet&gt;
 *     &lt;servlet-name&gt;WADO&lt;/servlet-name&gt;
 *     &lt;servlet-class&gt;org.dcm4chee.xero.metadata.servlet.MetaDataServlet&lt;/servlet-class&gt;
 *     &lt;init-param&gt;
 *        &lt;param-name&gt;metaData&lt;/param-name&gt;
 *        &lt;param-value&gt;/wado2.metadata&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *        &lt;param-name&gt;filter&lt;/param-name&gt;
 *        &lt;param-value&gt;wado&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *  &lt;/servlet&gt;
 *  &lt;servlet-mapping&gt;
 *     &lt;servlet-name&gt;WADO&lt;/servlet-name&gt;
 *     &lt;url-pattern&gt;/wado&lt;/url-pattern&gt;
 *  &lt;/servlet-mapping&gt;
 * 
 * </pre>
 */
@SuppressWarnings("serial")
public class MetaDataServlet extends HttpServlet {
   public static final String MODEL_KEY = "model";

   public static final String REQUEST_TYPE = "requestType";

   private static Logger log = LoggerFactory.getLogger(MetaDataServlet.class);

   /** A key value to use to fetch the full request URI - t */
   public static final String REQUEST_URI = "_requestURI";
   public static final String REQUEST = "_request";
   public static final String RESPONSE = "_response";
   public static final String SERVLET_CONTEXT = "_servletContext";
   public static final String USER_KEY = "userName";

   /** The meta data needs to be read from the appropriate location. */
   MetaDataBean metaData;

   /** The lifecycle listener */
   Lifecycle lifecycle;
   static Map<String,Integer> counters =new HashMap<String,Integer>();
   
   /**
    * The filterItem contains the fixed information about how to handle this
    * request
    */
   FilterItem<ServletResponseItem> filterItem;

   /**
    * The filter needs to supply a type and a stream/byte array/data object to
    * send
    */
   Filter<ServletResponseItem> filter;

   /**
    * The time between refreshes of filtered response - this is set to an hour
    * as it isn't expected that new data arrives all that often. This does NOT
    * affect a user hitting refresh in the browser - that will directly reload
    * the data.
    */
   private long modifiedTimeAllowed = 60 * 60 * 1000; // 1 hour default.

   // Refresh screen
   // refreshes list.

   /**
    * Filter the items to get a return response by calling the filter that is 
    * defined in the web.xml file by the following parameters:
    * <p>
    * <dl>
    * <dd>metaData</dd><dt>*.metadata file to read for filter definitions</dt>
    * <dd>filter</dd><dt>Name of the filter to invoke</dt>
    * </dl>
    * 
    * @param request
    *           used to determine the parameters for the filter
    * @param response
    *           used to write the filtered data.
    * @throws ServletException
    * @throws IOException
    *            Sets SC_NO_CONTENT on a null return element.
    */
   protected void doFilter(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      try {
         Map<String, Object> params = computeParameterMap(request, response, getServletContext());
         log.debug("Invoking the filter chain with params: {}",params);
         
         ServletResponseItem sri = filter.filter(filterItem,params);
         response.setCharacterEncoding("UTF-8");
         if (sri == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No content found for this request.");
            return;
         }
         sri.writeResponse(request, response);
      } catch (Exception e) {
         log.error("Caught error " + e + " for URI " + request.getRequestURI() + " with parameters " + request.getQueryString(), e);
         try {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error:" + e);
         } catch (Exception e2) {
            // No-op
         }
      }
   }

   /**
    * Computes a parameter map that maps single valued items to Strings and
    * multi-valued items to String[]. This is handy for the normal case where
    * only a single item is expected - if the type is wrong, then it is usually
    * an error, so it is ok to just throw an exception.
    * 
    * @param request
    *           contains the parameter map to convert to a simple map.
    * @return A Map to String where only 1 value is present, and to String[] for
    *         multiple values. This break down makes the most sense in the usual
    *         case where there is only 1 possible value.
    */
   @SuppressWarnings("unchecked")
   public static Map<String, Object> computeParameterMap(HttpServletRequest request, HttpServletResponse response,
         ServletContext servletContext) {
      Map<String, String[]> parameters = request.getParameterMap();
      Map<String, Object> ret = new HashMap<String, Object>();
      for (Map.Entry<String, String[]> me : parameters.entrySet()) {
         if (me.getValue().length == 0)
            continue;
         if (me.getValue().length == 1)
            ret.put(me.getKey(), me.getValue()[0]);
         else
            ret.put(me.getKey(), me.getValue());
      }
      ret.put(MemoryCacheFilter.KEY_NAME, request.getQueryString());
      ret.put(REQUEST_URI, request.getRequestURI());
      // This is a bit ugly, but it does allow full access to the request if a
      // filter really
      // needs it - eg for auditing.
      ret.put(REQUEST, request);
      ret.put(RESPONSE, response);
      ret.put(USER_KEY, request.getRemoteUser());
      ret.put(SERVLET_CONTEXT, servletContext);
      ret.put(UrlUriResolver.URIRESOLVER, new UrlUriResolver(request, response, servletContext));

      Locale loc = request.getLocale();
      if (ret.containsKey("language")) {
         String language = (String) ret.get("language");
         int split = language.indexOf("_");
         String country = "";
         String variant = "";
         if (split > 0) {
            country = language.substring(split + 1);
            language = language.substring(0, split);
            split = country.indexOf("_");
            if (split >= 0) {
               variant = country.substring(split + 1);
               language = country.substring(0, split);
            }
         }
         log.info("Requested language " + language + "_" + country + "_" + variant);
         loc = new Locale(language, country, variant);
      }
      ret.put("locale", loc);
      return ret;
   }

   /**
    * The initialization needs to read the meta data object in from the
    * StaticMetaData coming from a file or resource with the name specified by
    * the metaData property to the servlet, eg:
    * 
    * <pre>
    * &lt;init-param&gt;
    *  &lt;param-name&gt;metaData&lt;/param-name&gt;
    *  &lt;param-value&gt;wado2.metadata&lt;/param-value&gt;
    * &lt;/init-param&gt;
    * </pre>
    * 
    * The FILTER used for this object is the filter list named by the filter
    * servlet parameter found in the above MetaData file (or in anything
    * referenced/used by that file) <br />
    * 
    * The lifecycle metadata value is used to control lifecycle events - start/stop overall.
    * 
    * @throws MalformedURLException
    */
   @SuppressWarnings("unchecked")
   @Override
   public void init() throws ServletException {
      String name = this.getInitParameter("metaData");
      log.debug("Loading meta-data from {} for servlet.", name);
      MetaDataBean root = StaticMetaData.getMetaData(name);
      String filterName = getInitParameter("filter");
      if (filterName == null)
         throw new IllegalArgumentException("Filter name not provided to meta-data servlet.");
      metaData = root.getForPath(filterName);
      if (metaData == null)
         throw new IllegalArgumentException("Filter/meta-data information not found for " + filterName);
      filter = (Filter<ServletResponseItem>) metaData.getValue();
      if (filter == null)
         throw new IllegalArgumentException("Filter not found for " + filterName);
      filterItem = new FilterItem(metaData);
      
      lifecycle = (Lifecycle) metaData.getValue("lifecycle");
      
      synchronized(counters) {
          Integer cnt = counters.get(getServletName());
          if( cnt!=null ) counters.put(getServletName(), 1+cnt);
          else {
             counters.put(getServletName(),1);
             log.info("Starting overall application "+getServletName());
             if( lifecycle!=null ) lifecycle.start(getServletName());
          }
      }

      Object val = metaData.getValue("maxAge");
      if (val != null) {
         modifiedTimeAllowed = Integer.parseInt((String) val);
      }
   }

   /** Destroys this object - just handles the counter increment/decrement. */
   @Override
   public void destroy() {
       synchronized(counters) {
           Integer cnt = counters.get(getServletName());
           if( cnt==null ) log.error("Already destroyed all instances, and trying to destroy another.");
           else if( cnt>1 ) counters.put(getServletName(),cnt-1);
           else {
              counters.remove(getServletName());
              log.info("Stopping overall servlet "+getServletName());
              if( lifecycle!=null ) lifecycle.stop(getServletName());
           }
       }
       super.destroy();
   }


   
   /**
    * Get requests can return last modified information. so add the cache
    * control headings and then proceed with a doFilter.
    */
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      long modifiedTime = getModifiedTimeAllowed() / 1000;
      long startTime = System.nanoTime();
      try {
         if (cacheResponse(req)) {
            resp.setHeader("Cache-Control", "max-age=" + Long.toString(modifiedTime));
            resp.setHeader("Pragma", null);
            resp.setHeader("Expires", null);
         }
         doFilter(req, resp);
      } finally {
         if (log.isDebugEnabled())
            log.debug("MetaData Servlet with expiry " + modifiedTime + " s called for " + req.getRequestURI() + " with parameters "
                  + req.getQueryString() + " took " + nanoTimeToString(System.nanoTime() - startTime));
      }
   }

   /** Indicate whether or not to cache the response data */
   protected boolean cacheResponse(HttpServletRequest req) {
      String v = req.getParameter("v");
      if( v==null || v.endsWith("SNAPSHOT") ) {
         log.info("Not setting caching for request - SNAPSHOT or not versioned.");
         return false;
      }
      return true;
   }

   /** Returns a string representation of the time duration dur */
   public static String nanoTimeToString(long dur) {
      if (dur <= 5e3)
         return dur + " ns";
      if (dur <= 5e6)
         return (dur / 1000) + " us";
      if (dur <= 5e9)
         return (dur / 1000000) + " ms";
      return (dur / 1000000000) + " s";
   }

   /** Returns the amount of time allowed for modifications. */
   protected long getModifiedTimeAllowed() {
      return modifiedTimeAllowed;
   }

   /**
    * Post responses always return new data - so just call doFilter directly.
    */
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      log.debug("MetaData Servlet as post for {}  with parameters  {}", req.getRequestURI(), req.getQueryString());
      doFilter(req, resp);
   }

   /** Gets the browser version string, all lower case */
   public static String getUserAgent(Map<?, ?> params) {
      HttpServletRequest req = (HttpServletRequest) params.get(REQUEST);
      return req.getHeader("USER-AGENT").toLowerCase();
   }

}
