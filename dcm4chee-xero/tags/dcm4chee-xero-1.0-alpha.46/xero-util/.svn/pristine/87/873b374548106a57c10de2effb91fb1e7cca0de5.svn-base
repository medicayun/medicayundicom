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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provider knows how to inherit values from another meta-data instance.
 * Circular dependencies cause undefined results - depending on the order of
 * instantiation, you can get either value.
 * 
 * @author bwallace
 */
public class InheritProvider implements MetaDataProvider {
	
	private static final MetaDataProvider inheritProvider = new InheritProvider();
	
	static Logger log = LoggerFactory.getLogger(InheritProvider.class);

	/**
	 * Reads the existing meta-data for the given inherited path to find
	 * information to include for this path.
	 */
	public Map<String, Object> getMetaData(String path,
			MetaDataBean existingMetaData) {
		Map<String,  Object> ret = new HashMap<String, Object>();
		Set<String> alreadyInherited = new HashSet<String>();
		log.debug("Starting to inherit source "+existingMetaData.path);
		alreadyInherited.add(existingMetaData.path);
		addMetaData(path, "", existingMetaData, ret, alreadyInherited);
		log.debug("Completted inheritting "+existingMetaData.path);
		return ret;
	}

	/**
	 * @param path is the full path to use for found values.
	 * @param childPath is the path underneath mdb to look for values from.
	 * @param mdb is the current level of meta-data bean to look for children in.
	 * @param inherittedValues is the object to extend with additional values.
	 */
	protected void addMetaData(String path, String childPath, MetaDataBean mdb,
			Map<String, Object> inherittedValues, Set<String> alreadyInherited) {
		if( mdb==null ) return;
		
		MetaDataBean parent = mdb.getParent();
		// Things that have no parent are just the root element, and that can't inherit.
		if( parent==null ) return;
		
		Object inheritFromObj = mdb.getValue("inherit");
		if( inheritFromObj!=null && (inheritFromObj instanceof String)) {
			String inheritFrom = (String) inheritFromObj;
			MetaDataBean inheritParent = mdb.getRoot().getForPath(inheritFrom);
			if(inheritParent!=null) {
				MetaDataBean mdbInherit = inheritParent.getForPath(childPath);
				if( mdbInherit!=null && !alreadyInherited.contains(mdbInherit.path)) {
					log.debug("Inheriting from "+mdbInherit.path+" into "+path+" parent inherit "+inheritFrom + " at level "+mdb.path);
					alreadyInherited.add(mdbInherit.path);
					inheritFrom(path,mdbInherit,inherittedValues);
					addMetaData(path,"",mdbInherit, inherittedValues,alreadyInherited);
				}
				else {
				   if( mdbInherit!=null ) {
					  log.debug("Not inheritting from "+mdbInherit.path+" as it is already inheritted.");
				   }
				   else {
					  log.debug("No parent "+mdbInherit+" found to inherit from.");
				   }
				}
			}
		}

		// Get the name of the current mdb level.  This might be empty.
		String mdbName = mdb.path;
		int lastDot = mdbName.lastIndexOf('.')+1;
		mdbName = mdbName.substring(lastDot);
		
		// Get the relative child path for the parent
		String relativeChildPath;
		if(childPath.length()==0) {
		  relativeChildPath = mdbName;
		}
		else {
			relativeChildPath = mdbName + "." + childPath;
		}
		addMetaData(path, relativeChildPath, parent, inherittedValues, alreadyInherited);
	}

	/**
	 * Adds everything not already added from mdb into ret. 
	 * mdb must be at the same relative level as the existing meta-data bean.
	 * 
	 * @param path to put things into
	 * @param childPath
	 * @param mdb
	 * @param inherittedValues
	 */
	protected void inheritFrom(String path, MetaDataBean mdb, Map<String, Object> inherittedValues) {
		Map<String, MetaDataBean> children = mdb.getChildren();
		Object rawValue = mdb.rawValue;
		// This is actually the only real inherited value at this level - 
		// everything else is just another nested meta-data bean.
		if (rawValue != null && !inherittedValues.containsKey(path)) {
		    log.debug("Inheriting "+path+" raw value="+rawValue);
			inherittedValues.put(path, rawValue);
		}
		if (children != null) {
			for (Map.Entry<String, MetaDataBean> me : children.entrySet()) {
				if (!inherittedValues.containsKey(me.getKey())) {
					// The value doesn't really matter here - as long as it
					// is non-null, the path will create a separate sub-element.
				    log.debug("Inheritting "+path+"."+me.getKey()+"="+me.getValue().rawValue);
					inherittedValues.put(path+"."+me.getKey(), me.getValue().rawValue);
				}
			}
		}
	}

	public static MetaDataProvider getInheritProvider() {
		return inheritProvider;
	}
}
