package org.dcm4chee.xero.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.FilterItem;

/**
 * This class combines all items from an iterator passed into the constructor
 * with the next filter item to create a combined set of servlet response items
 * for the current overall item.
 * 
 * @author bwallace
 * 
 * @param <T>
 */
public abstract class FilterCombineIterator<T, K> extends CombineIterator<K> {
	Map<String, Object> params;
	Iterator<T> itemsIt;
	T item;
	FilterItem<Iterator<K>> filterItem;

	/**
	 * Create an iterator that combines the items and calls the next filter to
	 * get the result iterator to return
	 */
	public FilterCombineIterator(Iterator<T> itemsIt, FilterItem<Iterator<K>> filterItem, Map<String, Object> params) {
		super();
		this.itemsIt = itemsIt;
		this.filterItem = filterItem;
		// Sets up any nested params - default just uses the params instance
		// direclty
		this.setupNestedParams(params);
		this.setCombineIterator(new InternalIt());
	}

	/** Convenience method that takes a collection (list, set etc) */
	public FilterCombineIterator(Collection<T> itemsCol, FilterItem<Iterator<K>> filterItem, Map<String, Object> params) {
		this(itemsCol.iterator(), filterItem, params);
	}

	/** Convenience method that takes an array */
	public FilterCombineIterator(T[] itemsArr, FilterItem<Iterator<K>> filterItem, Map<String, Object> params) {
		this(Arrays.asList(itemsArr), filterItem, params);
	}

	/** Make any required changes to the parameters for this run. */
	abstract protected void updateParams(T item, Map<String, Object> params);

	/**
	 * Restore any parameters changed while calling the next filter - it is
	 * thread safe to store anything in this object that is required.
	 */
	protected void restoreParams(T item, Map<String, Object> params) {
		// No-op
	}

	/**
	 * Sets up any restore parameters that get put back into params after the
	 * call is done. This method should be over-ridden an a copy of the
	 * parameters taken and modified if the call makes significant
	 * changes/modifications to the parameter set.
	 */
	public void setupNestedParams(Map<String, Object> origParams) {
		params = origParams;
	}

	/** A minimal implementation of an iterator-iterator that uses all the
	 * fields from FilterCombineIterator to iterate.
	 * 
	 * @author bwallace
	 *
	 * @param <T>
	 * @param <K>
	 */
	class InternalIt implements Iterator<Iterator<K>> {

		/** Returns true if there are more object UIDs to iterate over */
		public boolean hasNext() {
		    if( item!=null ) {
                restoreParams(item,params);
		        item = null;
		    }
			return itemsIt.hasNext();
		}

		/**
		 * Figure out the enxt set of servlet response items - can return null, in
		 * which case it means just skip this one.
		 */
		public Iterator<K> next() {
		    if(item!=null) {
		        restoreParams(item,params);
		    }
			item = itemsIt.next();
			updateParams(item, params);
			Iterator<K> ret = filterItem.callNextFilter(params);
			return ret;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
