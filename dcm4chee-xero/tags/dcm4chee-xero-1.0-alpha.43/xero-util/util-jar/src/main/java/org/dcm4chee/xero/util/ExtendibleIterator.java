package org.dcm4chee.xero.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** This is an iterator that can have items added to it, plus a secondary iterator that is called
 * after this one is completed.  The set of items is cleared once the next iterator is started on, so that
 * they can be garbage collected if possible.
 * @author bwallace
 *
 */
public class ExtendibleIterator<T> implements Iterator<T> {
	Iterator<T> nextIterator;
	List<T> items = new ArrayList<T>();
	int position = 0;
	
	public ExtendibleIterator() {
	}
	
	public ExtendibleIterator(Iterator<T> nextIterator) {
		this.nextIterator = nextIterator;
	}

	/** Returns true if there are any directly owned items, or if the next iterators has items. */
   public boolean hasNext() {
   	if( position < items.size() ) return true;
   	return nextIterator!=null && nextIterator.hasNext();
   }

   /** Gets the next item - added items always come before nextIterator items */
   public T next() {
   	if( position < items.size() ) {
   		return items.get(position++);
   	}
   	items.clear();
   	return nextIterator.next();
   }

   /** Adds a new item to the iteration - before the previously added items
    * next iterator.
    * @param addItem
    */
   public void add(T addItem) {
   	items.add(0,addItem);
   }

   /** Unsupported */
   public void remove() {
   	throw new UnsupportedOperationException();
   }

}
