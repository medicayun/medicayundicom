package org.dcm4chee.xero.util;

import java.util.Iterator;

/**
 * Combines two iterator types together - usually with some custom logic around the top level
 * as this is usually combining a list of items of some sort with a child iterator on that type.
 * 
 * @author bwallace
 *
 */
public class CombineIterator<T> implements Iterator<T> {

	Iterator<Iterator<T>> combineIterator;
	Iterator<T> childIt = null;
	
	/** Create an iterator without setting the child iterator yet. */
	protected CombineIterator() {
		
	}
	
	/** Created a combined iterator that iterates over the child elements */
	public CombineIterator(Iterator<Iterator<T>> combineIterator) {
		this.combineIterator = combineIterator;
	}

	/** Sets the iterator to combine with */
	protected void setCombineIterator(Iterator<Iterator<T>> combineIterator) {
		this.combineIterator = combineIterator;
	}

	/** Iterates over combineIterator and childIt to get the next available value */
   public boolean hasNext() {
   	while( childIt==null && combineIterator.hasNext() ) {
   		childIt = combineIterator.next();
   		// Allow a null return value and just skip it.
   		if( childIt!=null && !childIt.hasNext() ) childIt = null;
   	}
   	return childIt!=null;
   }

   /** Figures out the next child iterator, and if that is emptied, figures out the
    * next parent iterator.
    */
   public T next() {
   	if( !hasNext() ) throw new IllegalArgumentException("No more items.");
   	T ret = childIt.next();
   	if( !childIt.hasNext() ) childIt = null;
   	return ret;
   }

	/** Unsupported */
   public void remove() {
		throw new UnsupportedOperationException("Remove not supported.");
   }

}
