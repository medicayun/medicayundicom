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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests the common access patterns for the memory cache.
 * data.
 * @author bwallace
 *
 */
public class MemoryCacheTest extends EasyMock {
	private static Logger log = LoggerFactory.getLogger(MemoryCacheTest.class);
	
	/** Create a default memory cache - 1 meg stored, 2 accesses required to promote,
	 * and 2 levels L1, and L2.
	 */
	MemoryCache<String,Object> memoryCache;
	
	static String key1 = "key1";
	static String key2 = "key2";

	FutureImpl item1, item2;
	
	/** Initialize some commonly used data */
	@BeforeMethod
	void init() {
	   item1 = new FutureImpl(new Object(), 10240l);
	   item2 = new FutureImpl(new Object(), 20480l);
	   memoryCache = new MemoryCache<String,Object>(1024*1024l);
	}
	
	/** This tests a simple cached item */
	@Test
	public void testCacheItem() throws Exception
	{
		assert memoryCache.get(key1, item1)==item1.get();
		assert memoryCache.get(key2, item2)==item2.get();
	}
		
	/**
	 * Tests that items get thrown out of cache appropriately at the L1 level.
	 */
	@Test
	public void testCacheSize() throws Exception
	{
		assert item2.get()!=null;
		assert item2.get()==item2.get();
		log.debug("item1={}, item2={}", item1.get(), item2.get());
		memoryCache.get("key0", item1);
		for(int i=1; i<60; i++) {
			if( i==48 ) {
				// Don't cause it to be promoted here - just make it most recent in L1.
				assert memoryCache.get("key0",null)==item1.get();
			}
			Object val = memoryCache.get("key"+i, item2);
			assert val!=null;
			log.debug("Testing key"+i+" value={} expected val={}",val,item2.get());
			assert val==item2.get();
		}
		// key 1 should have been thrown away.
		assert memoryCache.get("key1",null)==null;
		// It had better still have the most recent key.
		assert memoryCache.get("key59",null)==item2.get();
		// Should still have key 0 since it was recently acquired.
		assert memoryCache.get("key0",null)==item1.get();
	}
	
	static class FutureImpl implements SizeableFuture<Object> {
	   Object value;
	   long size;
	   
	   public FutureImpl(Object value, long size) {
		  this.value = value;
		  this.size = size;
	   }
	   
	  public boolean cancel(boolean arg0) {
		return false;
	  }

	  public Object get() throws InterruptedException, ExecutionException {
		 return value;
	  }

	  public Object get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
		return this;
	  }

	  public boolean isCancelled() {
		return false;
	  }

	  public boolean isDone() {
		return false;
	  }

	  public long getSize() {
		return size;
	  }
	   
	};
}
