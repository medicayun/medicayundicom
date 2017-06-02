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

import java.io.Closeable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A memory cache is a Map that throws things away based on how much memory is
 * being used. Items that are "Closeable" are closed before being thrown away.
 * 
 * There are additional behaviours that can be added as separate objects that
 * operate on this class. One of these auto-throws things away that are expired,
 * and the other closes things after some length of time, shorter than expiry
 * time.
 * 
 * @author bwallace
 * 
 * @param <T>
 */
public class MemoryCache<K, V> {

   private static final Logger log = LoggerFactory.getLogger(MemoryCache.class);

   static public final long DEFAULT_SIZE = 1024 * 1024;

   /**
     * The default maximum age for any item is 1 hour - after that, it will be
     * thrown away regardless.
     */
   protected long maxAge = 60 * 60 * 1000l;

   /** By default, don't close things */
   protected long closeAge = 0;

   protected long cacheSize;

   protected long currentSize;
   
   protected String cacheName;

   protected long counter = 1;

   /** Contains the items in least recently used ordering, to allow removal */
   protected SortedSet<InternalKey<K, V>> lruSet = new TreeSet<InternalKey<K, V>>();

   /**
     * findMap is used to lookup the information about an item - the value,
     * where it is located etc.
     */
   protected Map<K, InternalKey<K, V>> findMap = new HashMap<K, InternalKey<K, V>>();

   /**
     * Create a memory cache with 2 levels, and 1 meg of space, transitioning on
     * two or more requests to an L1 item.
     */
   public MemoryCache() {
	  this(DEFAULT_SIZE);
   }

   /**
     * Create a memory cache with the given number of levels, sizes and
     * transitions
     */
   public MemoryCache(long cacheSize) {
	  this.cacheSize = cacheSize;
   }

   /** Clears all items from the cache - except that those in progress will be returned without adding the value to the cache.
    * It is NOT recommended that any values be being looked up when a clear occurs.
    */
   public synchronized void clear() {
       if( closeAge>0 ) {
           for (InternalKey<K, V> ikey : lruSet) {
               ikey.close();
           }    
       }
       findMap.clear();
       lruSet.clear();
       currentSize = 0;
   }

   /**
     * Get the matching item from the cache, if available, otherwise it calls
     * the Future item to get the new value. Does not lock this while calling
     * the future, but is otherwise thread safe to call.
     */
   public V get(K key, SizeableFuture<V> valueGetter) {
	  InternalKey<K, V> value;
	  synchronized (this) {
		 value = findMap.get(key);
		 if (value != null) {
			boolean wasFound = lruSet.remove(value);
			if (wasFound) {
			   // This means there is already a value.
			   V ret = value.getValue();
			   value.setIncrement(counter++);
			   lruSet.add(value);
			   log.debug(cacheName + ", found cached item "+key);
			   return ret;
			}
		 } else {
			value = new InternalKey<K, V>(key);
			if( valueGetter!=null ) findMap.put(key,value);
		 }
	  }

	  // Allow returning a null value if no getter is supplied.
	  if (valueGetter == null)
		 return null;

	  log.debug(cacheName + ", callFutureValue "+key);
	  if (value.callFutureValue(valueGetter)) {
		 log.debug("Returning concurrent fetch "+key);
		 // Don't need to resynchronize on this, as the value is directly
		 // useable, and someone
		 // else did the resync, or will do it.
		 return value.getValue();
	  }

	  // It has to be added into the lruSet.
	  synchronized (this) {
		 long size = valueGetter.getSize();
		 if( size==0l && value.getValue()==null ) {
			   log.info("No value found in "+cacheName+" for "+key+" removing from map.");
			   findMap.remove(key);
			   return null;
		 }
		 if( size==0 ) {
			log.error("Size is 0 on "+valueGetter+" in "+cacheName+" using huge size:");
			size = Integer.MAX_VALUE;
		 }
		 value.setSize(size);
		 currentSize += valueGetter.getSize();
		 value.setIncrement(counter++);
		 value.updateOriginalAge();
		 if (!isToBig(value)) {
			log.debug("Adding to "+cacheName+" item "+key+" of size "+size);
			// The key may have been removed if caches are cleared.
			if( findMap.containsKey(key) )
			    lruSet.add(value);
		 } else {
			// Don't want it in the find map either - but anyone else who found
			// it while
			// we were waiting to retrieve it CAN use the shared value.
			// This will not likely be closed correctly.
			findMap.remove(key);
		 }
		 emptyLRU();
		 log.debug("Returning fetch "+key);
		 return value.getValue();
	  }
   }

   /** Indicates if the given value is to big for the map. */
   protected boolean isToBig(InternalKey<K, V> value) {
	  return value.getSize() > cacheSize / 5;
   }

   /** Removes extra items at the given level */
   public synchronized void emptyLRU() {
	  Iterator<InternalKey<K, V>> it = lruSet.iterator();
	  long now = System.currentTimeMillis();
	  if( log.isDebugEnabled() ) {
		 // This is too expensive to compute if it isn't being printed.
		 log.debug("Cache "+cacheName+" current size="+currentSize+"/"+cacheSize+" with "+findMap.size()+" items and lruSet size="+lruSet.size());
	  }
	  while (it.hasNext()) {
		 InternalKey<K, V> first = it.next();
		 if( currentSize < cacheSize ) {
			if( this.maxAge == 0 || (now-first.getOriginalAge()) < this.maxAge ) {
			   return;
			}
		 }
		 it.remove();
		 findMap.remove(first.getKey());
		 currentSize -= first.getSize();
		 // This is in a moderate loop, so don't construct the string unless needed.
		 if( log.isDebugEnabled() ) log.debug("Throwing away "+first.getKey()+" in "+cacheName);
		 if (closeAge > 0)
			first.close();
	  }
	  log.debug("Threw away all items in the cache.");
	  currentSize = 0;
   }

   /**
     * Closes old items. Items are only closed if they haven't been recently
     * used - continually using an item will prevent it from being closed.
     */
   public synchronized void closeOld() {
	  if (closeAge <= 0)
		 return;
	  long now = System.currentTimeMillis();
	  for (InternalKey<K, V> ikey : lruSet) {
		 if (now - ikey.getOriginalAge() < closeAge)
			return;
		 ikey.close();
	  }
   }

   /** Remove an item from the cache */
   public synchronized V remove(Object key) {
	  InternalKey<K, V> item = findMap.remove(key);
	  if (item == null)
		 return null;
	  lruSet.remove(item);
	  currentSize -= item.getSize();
	  return item.getValue();
   }

   /**
     * One stop shopping to set ALL the cache sizes at each level. Total memory
     * consumed could be as much as number of levels times the size provided.
     * This will cause items to be deleted if you decrease the size, but only
     * LRU items, not items that are more than 10% of the new size.
     */
   public void setCacheSize(long size) {
	  if (size <= 0)
		 throw new IllegalArgumentException("Illegal cache size " + size);
	  cacheSize = size;
	  emptyLRU();
   }

   /** Gets the age after which this item is supposed to be closed */
   public long getCloseAge() {
	  return closeAge;
   }

   /** Sets the age after which items are to be closed. */
   public void setCloseAge(long closeAge) {
	  this.closeAge = closeAge;
   }

   /**
     * The internal item is used to determine which cache an item is located in
     * and last-access time etc. It is comparable so that the oldest item can be
     * found.
     * 
     * @author bwallace
     * 
     * @param <T>
     */
   static class InternalKey<K, V> implements Comparable<InternalKey<K, V>> {
	  private V value;

	  private K key;

	  private long increment;

	  private long originalAge;

	  private long size;

	  public InternalKey(K key) {
		 this.key = key;
	  }

	  /**
         * Get the value associated with this item. This WILL cause the item to
         * be updated and the last accessed time to be incremented, so if it is
         * in an ordered list etc it will need to be moved/repositioned
         * appropriately. DO NOT call this while InternalKey is a member of a
         * sorted set.
         */
	  public V getValue() {
		 return value;
	  }

	  /** Sets the current value */
	  public void setValue(V v) {
		 this.value = v;
	  }

	  /**
         * Calls the future to get the current value.
         * 
         * @return true if the future value was already set - indicates that the
         *         value can be directly used/returned.
         */
	  public synchronized boolean callFutureValue(Future<V> future) {
		 if (this.value != null) {
			return true;
		 }
		 try {
			this.value = future.get();
		 } catch (InterruptedException e) {
			throw new RuntimeException("Couldn't get value.", e);
		 } catch (ExecutionException e) {
			throw new RuntimeException("Couldn't get value.", e);
		 }
		 return false;
	  }

	  /**
         * Sets the current increment value - the ordering information for
         * access times.
         */
	  public void setIncrement(long increment) {
		 this.increment = increment;
	  }

	  /** Gets the key associated with this item */
	  public K getKey() {
		 return key;
	  }

	  /** Get the size of this item */
	  public long getSize() {
		 return size;
	  }

	  /** Sets the size of the object */
	  public void setSize(long size) {
		 if (size <= 0)
			throw new IllegalArgumentException("Invalid size: " + size);
		 this.size = size;
	  }

	  /**
         * Gets the original age
         */
	  public long getOriginalAge() {
		 return originalAge;
	  }

	  /**
         * Updates the original age
         */
	  public void updateOriginalAge() {
		 this.originalAge = System.currentTimeMillis();
	  }

	  /**
         * Equals is true ONLY on the key value, not on the current associated
         * value
         */
	  @Override
	  public boolean equals(Object obj) {
		 if (obj == null)
			return false;
		 return key.equals(((InternalKey<?, ?>) obj).getValue());
	  }

	  /** Just use the key hash code */
	  @Override
	  public int hashCode() {
		 return key.hashCode();
	  }

	  /** Compare items by last accessed */
	  public int compareTo(InternalKey<K, V> o) {
		 // Increments are guaranteed unique, so just compare by increment
		 // value.
		  if(increment<o.increment) return -1;
		  else if (increment==o.increment) return 0;
		  return 1;
	  }

	  /** Close this item if it is closeable */
	  public void close() {
		 if (value instanceof Closeable) {
			try {
			   log.warn("Closing "+value);
			   ((Closeable) value).close();
			} catch (java.io.IOException e) {
			   // No-op
			}
		 }
	  }

   }

}