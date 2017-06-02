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
package org.dcm4chee.xero.metadata;

import java.util.Comparator;

/**
 * Compares two meta-data bean nodes.
 * @author bwallace
 *
 */
public class MetaDataComparator implements Comparator<MetaDataBean>{
	
	int defaultValue;
	String sortKey;
	
	/**
	 * Comapre meta-data nodes on "priority" and 0 as the sort key and
	 * default value.
	 *
	 */
	public MetaDataComparator()
	{
		this(MetaDataBean.DEFAULT_SORT_KEY);
	}
	
	/**
	 * Compare meta-data nodes for ordering, based on the child sortKey,
	 * with values missing sortKey have value defaultValue.
	 * @param sortKey is the child key to use for the priority/sort value.
	 * @param defaultValue is the value to use for sorting if no child key is found.
	 */
	public MetaDataComparator(String sortKey, int defaultValue)
	{
		this.sortKey = sortKey;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Compare meta-data nodes on the given sortKey, with 0 as the default value. 
	 * @param sortKey is the child key to use for the priority/sort value.
	 */
	public MetaDataComparator(String sortKey) {
		this(sortKey,0);
	}

	/**
	 * Compare two meta-data beans, by the value of the sortKey child.
	 * This will default to a biggest to smallest ordering.
	 * @param o1 is the first meta-data bean to compare.
	 * @param o2 is the second meta-data bean to compare.
	 * @return positive if o2 is bigger than o1 (that is, if the sortKey value is bigger), zero if they are the same, and negative otherwise.
	 */
	public int compare(MetaDataBean o1, MetaDataBean o2) {
		int o1Sort = o1.getIntValue(sortKey,defaultValue);
		int o2Sort = o2.getIntValue(sortKey,defaultValue);
		int ret = o2Sort - o1Sort;
		return ret; 
	}

}
