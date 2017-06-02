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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A FilterListConfig defines the configuration information for a specific set of filters in a filter list at
 * a specific location in the meta-data tree.  The SAME instance of the filter can have different filter list 
 * config instances in order to allow different instances in different parts of the tree.
 * @author bwallace
 *
 */
public class FilterListConfig<T> {
	private static Logger log = LoggerFactory.getLogger(FilterListConfig.class);
	
	List<FilterItem<T>> filterList = new ArrayList<FilterItem<T>>();
	Map<String,FilterItem<T>> filterMap = new HashMap<String,FilterItem<T>>();
	
	/** Create the filter list config instance at the given meta-data bean value. */
	@SuppressWarnings("unchecked")
   public FilterListConfig(MetaDataBean mdb)
	{
		List<MetaDataBean> sortedList = mdb.sorted(); 
		FilterItem<T> previous = null;
		for(MetaDataBean valueMdb : sortedList ) {
			Object value = valueMdb.getValue();
			if( value instanceof Filter ) {
				FilterItem<T> fi = new FilterItem<T>(valueMdb, (Filter<T>)value);
				filterMap.put(fi.getName(),fi);
				if( fi.priority<0 ) continue;
				FilterItem<T> fit = (FilterItem<T>) fi;
				if( previous!=null ) previous.nextFilterItem = fit;
				previous = fit;
				log.debug("Adding filter to "+mdb.getPath()+" item "+fi.getName());
				filterList.add((FilterItem<T>) fi);
			}
			else {
			   log.debug("Skipping item "+valueMdb.getPath()+"="+value);
			}
		}
		log.info("There are "+filterList.size() +" items in "+mdb.getPath());
	}
	
	/** This gets the named filter */
	public FilterItem<T> getNamedFilter(String childName) {
		return filterMap.get(childName);
	}
	
	/** Get the first filter to use - only returns a first filter if there is a priority>=0 filter,
	 * otherwise returns null.
	 */
	public FilterItem<T> getFirstFilter()
	{
		if( filterList.size()>0 ) return filterList.get(0);
		return null;
	}

}
