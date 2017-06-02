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
package org.dcm4chee.xero.metadata.access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.MetaDataUser;

/**
 * A value list is a list of children of a meta-data node.
 * 
 * @author bwallace
 */
@SuppressWarnings("serial")
public class ValueList<T> extends ArrayList<T> implements MetaDataUser {

	/**
	 * This class is a temporary instance of the value list used for ordering
	 * the overall value list.
	 * 
	 * @author bwallace
	 * 
	 */
	static class ValueListItem<T> implements Comparable<ValueListItem<?>> {
		int priority = Integer.MAX_VALUE-1;
		String key;

		List<T> values;

		/** Create a value list item with 1 element */
		public ValueListItem(MetaDataBean mdb, T value) {
			Object priorityObj = mdb.getValue("priority");
			if (priorityObj != null && (priorityObj instanceof String)) {
				priority = Integer.parseInt((String) priorityObj);
			}
			this.values = Collections.singletonList(value);
			this.key = mdb.getChildName();
		}

		/** Create a value list item with multiple elements */
		public ValueListItem(MetaDataBean mdb, List<T> values) {
			Object priorityObj = mdb.getValue("priority");
			if (priorityObj != null && (priorityObj instanceof String)) {
				priority = Integer.parseInt((String) priorityObj);
			}
			this.values = values;
			this.key = mdb.getChildName();
		}

		/** Compare by priority values - make it deterministic by comparing on both
		 * priority and name
		 */
		public int compareTo(ValueListItem<?> vli) {
			int ret = this.priority - vli.priority;
			if( ret!=0 ) return ret;
			return this.key.compareTo(vli.key);
		}

		/** Return the value provided */
		public List<T> getValues() {
			return values;
		};

	}

	/**
	 * The priority child of an item defines the order to insert this in.
	 */
	protected String priority_name = "priority";

	/**
	 * The class is the type T.
	 */
	protected Class<T> clazz;

	/** Creates an anonymous or unknown typed value list. */
	public ValueList() {
		// Allow creation of anonymous or untyped value list.  
	}

	/** 
	 * Creates a value list on the given class - only elements of the given class are added.
	 */
	public ValueList(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	/** Creates an instance from the given existing list */
	public ValueList(Class<T> clazz, List<T> items ) {
		super(items);
		this.clazz = clazz;
	}

	/**
	 * Sets the class name to use.
	 * 
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	@MetaData(required = false)
	public void setClassName(String className) throws ClassNotFoundException {
		clazz = (Class<T>) Class.forName(className);
	}

	/**
	 * This sets the meta-data that is to be used for this object. Default
	 * values can be pre-configured by inheriting from this and over-riding the
	 * constructor(s).
	 */
	public void setMetaData(MetaDataBean metaDataBean) {
		if( metaDataBean==null ) return;
		List<ValueListItem<T>> list = new ArrayList<ValueListItem<T>>();
		for (Map.Entry<String, MetaDataBean> me : metaDataBean.metaDataEntrySet()) {
			T child = lookupChildInstance(me.getValue());
			if (child == null)
				continue;
			list.add(new ValueListItem<T>(me.getValue(), child));
		}
		Collections.sort(list);
		for(ValueListItem<T> vli : list) {
			//System.out.println("Adding vli "+vli.key+"="+vli.values.get(0)+" pri "+vli.priority);
			this.addAll(vli.getValues());
		}
	}

	/**
	 * This method returns the child instance to use in the overall list for the
	 * given, named item. This can be overridden to create new types of lists
	 * based on other criteria or to create lists of custom types. A lazy
	 * loading could be done based on such an object. This can return null to
	 * indicate no value, or throw an exception if there is something wrong with
	 * the child meta-data.
	 * 
	 * @param childMeta
	 *            is the child to create an object for.
	 * @return Item to add to the list, or null if no item to be added for this
	 *         child.
	 */
	@SuppressWarnings("unchecked")
	public T lookupChildInstance(MetaDataBean childMeta) {
		Object childValue = childMeta.getValue();
		if (childValue == null)
			return null;
		if (clazz == null || clazz.isInstance(childValue))
			return (T) childValue;
		return null;
	}

}
