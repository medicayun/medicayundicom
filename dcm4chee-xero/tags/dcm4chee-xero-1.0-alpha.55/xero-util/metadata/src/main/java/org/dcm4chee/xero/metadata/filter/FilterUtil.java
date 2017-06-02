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
package org.dcm4chee.xero.metadata.filter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.URIResolver;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.servlet.MetaDataServlet;
import org.dcm4chee.xero.metadata.servlet.UrlUriResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides static utilities to get access to various elements from the params
 * map etc.
 * 
 * @author bwallace
 * 
 */
public class FilterUtil {

	private static final Logger log = LoggerFactory.getLogger(FilterUtil.class);
	
   public static String getString(Map<?,?> params, String key, String def) {
	  Object v = getPath(params,key);
	  if (v == null)
		 return def;
	  if (v instanceof String)
		 return (String) v;
	  throw new IllegalArgumentException("Expected String value for " + key + " got class " + v.getClass() + " value " + v);
   }
   
   public static String getString(Map<?,?> params, String key) {
   	return getString(params,key,null);
   }
   
   public static boolean getBoolean(Map<String,Object> params, String key) {
       return getBoolean(params,key,false);
   }
   
   public static boolean getBoolean(Map<String,Object> params, String key, boolean def) {
	  Object v = params.get(key);
	  if( v==null ) return def;
	  if( v instanceof String ) {
		 String s= (String) v;
		 return "1".equals(s) || "true".equalsIgnoreCase(s);
	  }
	  if( v instanceof Boolean ) return (Boolean) v;
	  throw new IllegalArgumentException("Expected boolean value for " + key + " got class " + v.getClass() + " value " + v);
   }
   
   public static int getInt(Map<String, ?> params, String key) {
	  return getInt(params, key, 0);
   }

   public static int getInt(Map<String, ?> params, String key, int def) {
	  Object v = params.get(key);
	  if (v == null)
		 return def;
	  if (v instanceof String)
		 return Integer.parseInt((String) v);
	  if (v instanceof Integer)
		 return ((Integer) v).intValue();
	  throw new IllegalArgumentException("Expected integer value for " + key + " got class " + v.getClass() + " value " + v);
   }

   public static long getLong(Map<String, Object> params, String key) {
	  return getLong(params, key, 0l);
   }

   public static long getLong(Map<String, Object> params, String key, long def) {
	  Object v = params.get(key);
	  if (v == null)
		 return def;
	  if (v instanceof String)
		 return Long.parseLong((String) v);
	  if (v instanceof Integer)
		 return ((Integer) v).intValue();
	  if (v instanceof Long)
		 return ((Long) v).longValue();
	  throw new IllegalArgumentException("Expected long value for " + key + " got class " + v.getClass() + " value " + v);
   }

   /** Get the float values from params */
   public static float[] getFloats(Map<String, Object> params, String key, float[] def) {
	  Object v = params.get(key);
	  if (v == null)
		 return def;
	  if (v instanceof String) {
		 String s = (String) v;
		 return splitFloat(s, 0);
	  }
	  if (v instanceof float[])
		 return (float[]) v;
	  if (v instanceof Float)
		 return new float[] { (Float) v };
	  throw new IllegalArgumentException("Expected float[] value for " + key + " got class " + v.getClass() + " value " + v);
   }

   /** Splits region into sub-parts */
   public static double[] splitDouble(String region, int size) {
	  double ret[] = new double[size];
	  int start = 0;
	  region = region.trim();
	  for (int i = 0; i < ret.length; i++) {
		 if (start >= region.length())
			throw new IllegalArgumentException("Too few arguments in " + region);
		 int end = region.indexOf(',', start);
		 if (end < 0)
			end = region.length();
		 ret[i] = Double.parseDouble(region.substring(start, end));
		 start = end + 1;
	  }
	  if (start < region.length())
		 throw new IllegalArgumentException("Too many arguments in " + region);
	  return ret;
   }

   /** Splits region into sub-parts */
   public static float[] splitFloat(String region, int size) {
	  if (size == 0) {
		 int start = region.indexOf(',');
		 size++;
		 while (start > 0) {
			size++;
			start = region.indexOf(',', start + 1);
		 }
	  }
	  float ret[] = new float[size];
	  int start = 0;
	  region = region.trim();
	  for (int i = 0; i < ret.length; i++) {
		 if (start >= region.length())
			throw new IllegalArgumentException("Too few arguments in " + region);
		 int end = region.indexOf(',', start);
		 if (end < 0)
			end = region.length();
		 ret[i] = Float.parseFloat(region.substring(start, end));
		 start = end + 1;
	  }
	  if (start < region.length())
		 throw new IllegalArgumentException("Too many arguments in " + region);
	  return ret;
   }

	/** Creates/gets the model to use 
    * @return */
   @SuppressWarnings("unchecked")
   public static Map<String, Object> getModel(Map<String,Object> params, MetaDataBean defaultModel) {
   	Map<String,Object> model = (Map<String, Object>) params.get(MetaDataServlet.MODEL_KEY);
   	if( model!=null ) return model;
   	if( defaultModel!=null ) model = (Map<String, Object>) defaultModel.getValue();
   	else model = new HashMap<String,Object>();
   	params.put(MetaDataServlet.MODEL_KEY, model);
   	return model;
   }

	/** Creates/gets the model to use */
   public static Map<String, Object> getModel(Map<String,Object> params) {
   	return getModel(params,null);
   }

   /** Gets a map object from the named parameter value */
   @SuppressWarnings("unchecked")
   public static Map<String,Object> getMap(Map<?,?> params, String key) {
   	return (Map<String,Object>) getPath(params,key);
   }
   
   /** Gets the URI Resolver from the parameter map */
   public static URIResolver getURIResolver(Map<?,?> params) {
		 URIResolver resolver = (URIResolver) params.get(UrlUriResolver.URIRESOLVER);
		 return resolver;
   }
   
   /** Gets a value in a dot-separated path key, assuming each child element other than the
    * last is a Map.
    * @param map
    * @param key
    * @return
    */
   public static Object getPath(Map<?,?> map, String key) {
  	  int nextDot = key.indexOf('.');
 	  if (nextDot < 0) return map.get(key);
 	  Map<?,?> nextMap = (Map<?,?>) map.get(key.substring(0,nextDot));
 	  if( nextMap==null ) {
 		  log.info("Unable to find child path "+key.substring(0,nextDot)+" in key "+key);
 		  return null;
 	  }
 	  return getPath(nextMap,key.substring(nextDot+1));
    }

   /** Returns the locale from the parameters */
   public static Locale getLocale(Map<?,?> params) {
	   Locale loc = (Locale) params.get("locale");
	   return loc;
   }

   /**
     * This class removes the provided strings from the query string, updating
     * the map in place.
     */
   public static Object[] removeFromQuery(Map<String, Object> map, String... removals) {
      Object[] ret = new Object[removals.length];
      String queryStr = (String) map.get(MemoryCacheFilter.KEY_NAME);
      if (queryStr == null)
    	 throw new IllegalArgumentException("Initial query string must not be null.");
      boolean removed = false;
      StringBuffer sb = new StringBuffer(queryStr);
      int i = 0;
      for (String remove : removals) {
    	 ret[i++] = map.remove(remove);
    	 int pos = sb.indexOf(remove + "=");
    	 if (pos == -1)
    		continue;
    	 int end = pos + remove.length();
    	 if (pos > 0) {
    		if (sb.charAt(pos - 1) != '&')
    		   continue;
    		pos--;
    	 }
    	 if (end < sb.length()) {
    		int nextAmp = sb.indexOf("&", end);
    		if (nextAmp == -1)
    		   end = sb.length();
    		else
    		   end = nextAmp;
    	 }
    	 sb.delete(pos, end);
    	 removed = true;
      }
      // Clean it up at the beginning and end
      if (sb.length() > 0) {
    	 if (sb.charAt(0) == '&') {
    		sb.delete(0, 1);
    		removed = true;
    	 }
    	 if (sb.charAt(sb.length() - 1) == '&') {
    		sb.delete(sb.length() - 1, sb.length());
    		removed = true;
    	 }
      }
      if (removed) {
    	 queryStr = sb.toString();
    	 map.put(MemoryCacheFilter.KEY_NAME, queryStr);
      }
      return ret;
   }

   /** Computes and sets the query string, based on either all objects, or the provide set, in order.
    * Not all listed objects need be in the actual query.
    */
   public static String computeQueryString(Map<String,Object> map, String... objects) {
       if( objects==null || objects.length==0 ) {
    	   objects = map.keySet().toArray(new String[0]);
       }
       StringBuffer queryStr = new StringBuffer();
       for(String key : objects) {
    	   Object val = map.get(key);
    	   if( val==null ) continue;
    	   queryStr.append("&").append(key).append("=").append(map.get(key));
       }
       String ret = queryStr.toString();
       map.put(MemoryCacheFilter.KEY_NAME, ret);
       return ret;
   }

   /**
     * Adds the given pairs of String/value entries to the parameter map and
     * query key. Must be pairs of strings.
     */
   public static void addToQuery(Map<String, Object> map, String... additions) {
      if ((additions.length % 2) == 1) {
    	 throw new IllegalArgumentException("Query values must be paired.");
      }
      String queryStr = (String) map.get(MemoryCacheFilter.KEY_NAME);
      if (queryStr == null)
    	 queryStr = "";
      StringBuffer sb = new StringBuffer(queryStr);
      for (int i = 0; i < additions.length; i += 2) {
    	 if (map.containsKey(additions[i])) {
    		String oldKey = "&" + additions[i] + "=" + map.get(additions[i]);
    		int pos = sb.indexOf(oldKey);
    		if (pos < 0) {
    		   oldKey = "?" + oldKey.substring(1);
    		   pos = sb.indexOf(oldKey);
    		}
    		if (pos < 0) {
    		   log.warn("Unable to find old key value " + oldKey);
    		} else {
    		   sb.delete(pos, pos+oldKey.length());
    		}
    	 }
    	 map.put(additions[i], additions[i + 1]);
    	 sb.append("&").append(additions[i]).append('=').append(additions[i + 1]);
    	 map.put(MemoryCacheFilter.KEY_NAME,sb.toString());
      }
   }

   /** This method creates a copy of the given parameters, in a new parameter map, and 
    * sets the cache key.
    * @param params
    * @return
    */
   public static Map<String, Object> copyParams(Map<String, Object> params, String...keys) {
      Map<String,Object> ret = new HashMap<String,Object>(keys.length+2);
      for(String key : keys) {
         Object val = params.get(key);
         if( val==null ) continue;
         ret.put(key,val);
      }
      computeQueryString(ret);
      return ret;
   }

   /** Restores the values back into the query after they have been removed by removeFromQuery */
   public static void restoreQuery(Map<String, Object> params, Object[] vals, String...keys) {
      String queryStr = (String) params.get(MemoryCacheFilter.KEY_NAME);
      if( queryStr==null ) queryStr = "";
      StringBuffer sb = new StringBuffer(queryStr);
      int i=0;
      for(String key : keys) {
         Object v=vals[i++];
         if( v==null ) continue;
         sb.append('&').append(key).append('=').append(v);
         params.put(key,v);
      }
      params.put(MemoryCacheFilter.KEY_NAME, sb.toString());
   }

}
