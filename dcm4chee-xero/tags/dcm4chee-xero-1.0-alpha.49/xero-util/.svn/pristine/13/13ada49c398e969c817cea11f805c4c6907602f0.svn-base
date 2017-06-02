package org.dcm4chee.xero.metadata.filter;

import java.util.Map;

/** This is a class used to return a filter item singleton - mostly useful for
 * unit tests etc.
 * 
 * @author bwallace
 * @param <T>
 */
public class FilterItemSingleton<T> extends FilterItem<T> {
	T singleton;
	
	/**
	 * Provide the singleton return object to use for all requests.
	 * @param singleton
	 */
	public FilterItemSingleton(T singleton) {
		this.singleton = singleton;
	}

	/** Just return the singleton object */
	@Override
	public T callNextFilter(Map<String, Object> params) {
		return singleton;
	}

	
	
}
