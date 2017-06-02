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

import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A filter item is used to define the items in a filter list.  
 * @author bwallace
 *
 */
public class FilterItem<T> implements Comparable<FilterItem<?>> {
	private static Logger log = LoggerFactory.getLogger(FilterItem.class);
	String name;
	public Filter<T> filter;
	int priority;
	MetaDataBean metaData;
	Object config;
	FilterItem<T> nextFilterItem;
	
	/**
	 * This constructor can be used when all methods are being over-ridden, eg for test
	 * purposes.
	 */
	public FilterItem() {	
	}
	
	/**
	 * Create a filter item based on the given meta-data node.
	 * @param mdb to get information such as priority.
	 * @param filter to use.
	 */
	public FilterItem(MetaDataBean mdb, Filter<T> filter)
	{
		this.name = mdb.getChildName();
		this.filter = filter;
		priority = mdb.getIntValue("priority", 0);
		log.debug("Created filter item at "+mdb.getPath()+" name "+this.name+" priority "+this.priority);
		metaData = mdb;
	}
	
	/** Create a filter item from a meta-data bean, getting the filter
	 * from the value of the mdb.
	 * @param mdb to get filter and meta-data from.
	 */	
	@SuppressWarnings("unchecked")
   public FilterItem(MetaDataBean mdb) {
		this(mdb, (Filter<T>) mdb.getValue());
	}
	
	/** Compare this filter item to another filter item.
	 * @return int comparison value +ve/0/-ve for <, = and >
	 */
	public int compareTo(FilterItem<?> o) {
		int ret = o.priority - this.priority;
		if( ret!=0 ) return ret;
		ret = o.name.compareTo(this.name);
		if( ret!=0 ) return ret;
		return 0;
	}
	
	/**
	 * Return the configuration information for this filter item - if any.
	 * @return
	 */
	public Object getConfig()
	{
		if( config==null && metaData!=null ) {
			config = metaData.getValueConfig();
		}
		return config;
	}
	
	/** Returns the filter list config - this will be the normal method called rather than
	 * getConfig, but getConfig can be used for non FilterList items.
	 */
	@SuppressWarnings("unchecked")
   public FilterListConfig<T> getFilterListConfig() 
	{
		return (FilterListConfig<T>) getConfig();
	}
	
	/** Calls the next filter item in the chain.  The next filter is required to return the
	 * correct type.
	 * @param params to pass to the next filter
	 * @return The filtered return value.
	 */
   public T callNextFilter(Map<String,Object> params)
	{
		if( nextFilterItem==null ) return null;
		if( nextFilterItem.priority < 0 ) return null;
	   log.debug("Calling next filter {}",nextFilterItem.name);
		return nextFilterItem.filter.filter(nextFilterItem, params);
	}
	
	/** Calls the given, named filter item from this chain.  Named filters can return anything.
	 * @param filterName is the local name to look for.
	 * @param params are the parameters to pass.
	 * @return value from the filter.
	 */
   @SuppressWarnings("unchecked")
   public T callNamedFilter(String filterName, Map<String,Object> params)
	 {
		 FilterListConfig<T> fl = (FilterListConfig) metaData.getParent().getValueConfig();
		 if( fl==null ) {
			 log.warn("Parent "+metaData.getParent().getPath()+" did not have a filter list configuration item.");
			 return null;
		 }
		 FilterItem<T> namedFilter = fl.getNamedFilter(filterName);
		 if( namedFilter==null ) {
			 log.warn("No filter named "+filterName+" in "+metaData.getParent().getPath());
			 return null;			 
		 }
		 log.debug("Calling named filter {}",namedFilter.name);
		 return namedFilter.filter.filter(namedFilter, params);
	 }
	
	/** Indicates if the given filter is available in this filter list. */
	public boolean contains(String filterName) {
		 FilterListConfig<?> fl = (FilterListConfig<?>) metaData.getParent().getValueConfig();
		 if( fl==null ) {
			 log.warn("Parent "+metaData.getParent().getPath()+" did not have a filter list configuration item.");
			 return false;
		 }
		 FilterItem<?> namedFilter = fl.getNamedFilter(filterName);
		 if( namedFilter==null ) {
			 return false;			 
		 }
		 return true;
	}
	
	/** Return the next filters name/metadata path, if any */
	public String getNextFilterName() {
		if( nextFilterItem==null ) return null;
		return nextFilterItem.getName();
	}
	
	/** Returns the filter name - that is, the path, for the filter */
	public String getName() {
		if( metaData==null ) return null;
		return metaData.getChildName();
	}

	/** Calls the filter that this filter item is specified for.  
	 * The return type is Object since this could be a named item.
	 */
	public T callThisFilter(Map<String, Object> params) {
		return filter.filter(this,params);
	}
	
	
	public String toString()
	{
	   return "FilterItem:"+name;
	}
}