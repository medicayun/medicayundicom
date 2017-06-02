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

import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses memory cache items that implement CacheItem directly to cache
 * in-memory items.
 */
public class MemoryCacheFilter<T> implements Filter<T>, MetaDataUser {

   private static Logger log = LoggerFactory.getLogger(MemoryCacheFilter.class);

   /** The default item to lookup in params to find the key for the cached item. */
   public static final String KEY_NAME = "queryStr";

   /** The key to use to enable or modify the caching */
   public static final String CACHE_CONTROL = "cache-control";

   /**
     * The metadata item to define how long to keep the object open for -
     * default to zero
     */
   public static final String CLOSE_AGE = "closeAge";

   /** The value to use for cache-control to NOT cache the item */
   public static final String NO_CACHE = "no-cache";

   /**
     * The key to use in the meta-data for the size, and in the params to denote
     * the size of the cached item.
     */
   public static final String CACHE_SIZE = "cacheSize";

   protected String paramKeyName = KEY_NAME;

   protected String cacheName, closeAge;
   
   MemoryCache<String, T> cache = new MemoryCache<String, T>();

   /**
     * Return an item from memory cache if available, otherwise use the next
     * filter item to get the returned item.
     * 
     * Desired behaviour: 1. If NO_CACHE is true then ignore the queue, just
     * call the next item. 2. Size the queue to cacheSize 3. Have a maximum age
     * of maxAge, and throw things away after that age. 4. Have a close after
     * time of closeAge. 5. If lockDuplicate is true, the create a lock and wait
     * for the response on a given item if a second thread requests the same
     * item.
     */
   public T filter(FilterItem<T> filterItem, Map<String, Object> params) {
	  boolean noCache = FilterUtil.getBoolean(params, NO_CACHE);
	  if (noCache) {
		  return filterItem.callNextFilter(params);
	  }
	  String key = computeKey(params);
	  if( key==null ) {
	     log.warn("Unknown image cache key - image probably not found.");
	     return null;
	  }
	  SizeableFuture<T> sf = new SizeableFutureFilter<T>(filterItem, params);

	  long start = System.nanoTime();
	  log.debug("Looking in cache "+cacheName +" for " + key);
	  T ret = cache.get(key, sf);
	  long dur = System.nanoTime() - start;
      if( log.isDebugEnabled() ) {
	     String msg = "Found in "+cacheName+" value " + key + "=" + ret + " in " + nanoTimeToString(dur);
	     log.debug(msg);
      }
	  if (dur > 1000000000)
		 log.warn("More than 1 second to fetch " + key);
	  return ret;
   }

   /**
     * Override this method to use a different parameter value to get the cached
     * item.
     * 
     * @param params
     * @return
     */
   protected String computeKey(Map<String, Object> params) {
	  if (params == null)
		 throw new IllegalArgumentException("Params to filter and compute key should not be null.");
	  Object okey = params.get(paramKeyName);
	  log.debug("Looking for key in " + paramKeyName + " found " + okey);
	  if (okey instanceof String)
		 return (String) okey;
	  if (okey instanceof String[])
		 throw new IllegalArgumentException("Memory cache key must be single valued.");
	  if (okey == null) {
		 log.warn("Memory cache can't cache a null key value, when looking for key " + paramKeyName);
		 return null;
	  }
	  return okey.toString();
   }

   public void clear() {
       log.info("Clearing cache "+cacheName);
       cache.clear();
   }
   
   /**
     * Sets the size of the cache in bytes.
     */
   public void setCacheSize(long size) {
	  cache.setCacheSize(size);
   }

   public void setMetaData(MetaDataBean metaDataBean) {
	  String keyName = (String) metaDataBean.getValue(KEY_NAME);
	  log.info("Setting key name for parameter to " + keyName);
	  if (keyName != null)
		 paramKeyName = keyName;
	  String cacheSize = (String) metaDataBean.getValue(CACHE_SIZE);
	  if (cacheSize != null)
		 setCacheSize(Long.parseLong(cacheSize));
	  cacheName = metaDataBean.getPath();
	  cache.cacheName = cacheName;
	  closeAge = (String) metaDataBean.getValue(CLOSE_AGE);
	  if (closeAge != null)
		 cache.setCloseAge(Long.parseLong(closeAge));
   }

   /**
     * Get a future item from the next filter item, and be able to say how bit
     * it is.
     * 
     * @author bwallace
     * 
     * @param <T>
     */
   static class SizeableFutureFilter<T> implements SizeableFuture<T> {
	  private long size;

	  FilterItem<T> filterItem;

	  Map<String, Object> params;

	  T value;

	  SizeableFutureFilter(FilterItem<T> filterItem, Map<String, Object> params) {
		 this.filterItem = filterItem;
		 this.params = params;
	  }

	  public boolean cancel(boolean arg0) {
		 throw new UnsupportedOperationException();
	  }

	  public T get() throws InterruptedException, ExecutionException {
		 value = (T) filterItem.callNextFilter(params);
		 if( value==null ) {
			log.info("Couldn't find value for sizeable future");
			return null;
		 }
		 size = FilterUtil.getLong(params,CACHE_SIZE,-1);
		 if (size >= 0) {
			if( size==0 ) {
			   log.warn("Size provided is 0.");
			}
		 } else {
			size = ((CacheItem) value).getSize();
			if( size==0 ) {
			   log.warn("Size returned by cache item "+value+" is zero.");
			}
		 }
		 return value;
	  }

	  public T get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
		 throw new UnsupportedOperationException();
	  }

	  public boolean isCancelled() {
		 return false;
	  }

	  public boolean isDone() {
		 throw new UnsupportedOperationException();
	  }

	  public long getSize() {
		 return size;
	  }

   }

}
