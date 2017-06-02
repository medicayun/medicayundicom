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
import org.dcm4chee.xero.metadata.MetaDataUser;
import org.dcm4chee.xero.metadata.PreConfigMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter list is a filter that is just a collection of child filters. 
 * This can be used to combine filters in standard ways, such as an IMAGE based
 * WADO filter, or to be a top-level filter by itself, for instance the final
 * WADO filter that uses an IMAGE filter list, a report filter list etc.
 * Does NOT implement java.util.List. 
 * 
 * @author bwallace
 *
 */
public class FilterList<T> implements Filter<T>, PreConfigMetaData<FilterListConfig<T>>, MetaDataUser
{
	private static final Logger log = LoggerFactory.getLogger(FilterList.class);
	FilterItem<T> filterItem;

	/** Filters the data.  
	 * 
	 * @param configData contains the pre-computed configuration data.  This data is figured
	 * out from the meta-data typically, and is used to optimize the filter performance.
	 * @param params to use to retrieve the data.
	 * @return The filtered item, or null if not applicable.
	 */
	@SuppressWarnings("unchecked")
   public T filter(FilterItem<T> filterItem, Map<String, Object> params) {
		if( filterItem==null ) filterItem = this.filterItem;
	   FilterItem firstFilter = getFirstFilter(filterItem);
		if( firstFilter!=null ) {
			T ret = (T) firstFilter.filter.filter(firstFilter, params);
			if( ret!=null ) return (T) ret;
		}
		return filterItem.callNextFilter(params);
	}
	
	/**
	 * Gets the first filter item.  Returns null if there isn't a first filter.
	 */
   public FilterItem<T> getFirstFilter(FilterItem<T> filterItem) {
		if( filterItem==null ) filterItem = this.filterItem;
		FilterListConfig<T> filterListConfig = (FilterListConfig<T>) filterItem.getFilterListConfig();
		FilterItem<T> firstFilter = filterListConfig.getFirstFilter();
		if( firstFilter==null ) {
		   log.warn("First filter isn't found.");
		   return null;
		}
		log.debug("First filter is "+firstFilter.getName());
		return firstFilter;
	}
	
	/**
	 * Gets the named filter item.
	 */
   public FilterItem<?> getNamedFilter(FilterItem<T> filterItem, String name) {
		if( filterItem==null ) filterItem = this.filterItem;
		FilterListConfig<T> filterListConfig = (FilterListConfig<T>) filterItem.getFilterListConfig();
		FilterItem<?> namedFilter = filterListConfig.getNamedFilter(name);
		if( namedFilter==null ) {
		   log.warn("Unable to find filter named "+name);
		   return null;
		}
		log.debug("Filter "+name+" is "+namedFilter.getName());
		return namedFilter;
	}
	
	/** Retrieve any pre-computed list of child elements for this filter list,
	 * based on the given position in the meta-data - that allows multiple instances
	 * of the FilterList to re-use the same set of child elements based on the original
	 * location of the filter by using a ref, but still to have the correct next/previous
	 * elements.
	 * 
	 * @param mdb to get the configuration data from.  
	 * @return The filter config to use for the particular meta data bean.
	 */
	public FilterListConfig<T> getConfigMetaData(MetaDataBean mdb) {
		return new FilterListConfig<T>(mdb);
	}

	/**
	 * Defaults the filter item to a filter item created for the location of this
	 * filter item instnace - does not allow over-rides as a ref: instance, 
	 * but as a class: instance it has a separate instance for
	 * each location, so it does allow instances to be overridden.
	 */
   public void setMetaData(MetaDataBean metaDataBean) {
		this.filterItem = new FilterItem<T>(metaDataBean);
   }
}
