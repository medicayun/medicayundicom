package org.dcm4chee.xero.metadata.filter;

import java.util.Map;

/** This is a filter that returns the "return" value from the params as it's only action.
 * If return is null, then it looks for "exception" and if not null in the params, throws it as an exception.
 * 
 * The intended use is for testing, but it is in the primary classes as it won't be tested in this jar file but more
 * likely in unit tests elsewhere.
 * 
 * @author bwallace
 *
 * @param <T>
 */
public class ParamReturnFilter<T> implements Filter<T> {

	public static final String RETURN_KEY="return";
	public static final String EXCEPTION_KEY="exception";
	
	/** Returns the return or throws the exception value */ 
	@SuppressWarnings("unchecked")
   public T filter(FilterItem<T> filterItem, Map<String,Object> params) {
		T ret = (T) params.get(RETURN_KEY);
		if(ret!=null) return ret;
		RuntimeException e = (RuntimeException) params.get(EXCEPTION_KEY);
		if( e!=null ) throw e;
		return filterItem.callNextFilter(params);
	}
}
