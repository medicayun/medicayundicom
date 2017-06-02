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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides meta-data from a given string-string map.  It is used as both the initial, top level
 * property provider, and also within the child properties.  To include a child-property, declare in the parent
 * metadata:
 * <pre>
 * metaDataProvider.property=${class:org.dcm4chee.xero.metadata.PropertyProvider}
 * metaDataProvider.property.model=xero-view.metadata
 * </pre>
 * 
 * @author bwallace
 * 
 */
public class PropertyProvider implements MetaDataProvider, MetaDataUser {
	private static Logger log = LoggerFactory.getLogger(PropertyProvider.class);
	Map<String, Object> properties;

	/**
	 * This constructor is used outside of the MetaDataBean to create the
	 * initial property provider values.
	 * 
	 * @param properties contains the meta data to use.
	 */
	public PropertyProvider(Map<String, ?> properties) {
		this.properties = new HashMap<String, Object>(properties);
	}
	
	/**
	 * This constructor is used to create property providers that read other property files.
	 */
	public PropertyProvider() {
		this.properties = new HashMap<String, Object>();
	}

	/** Returns the meta-data information available from the properties file
	 * @param path is the location to get meta data for
	 * @param existingMetaData is the meta data already found at that location.
	 * @return map of a String to meta-data values.
	 * */
	public Map<String, ?> getMetaData(String path, MetaDataBean existingMetaData) {
		// This is more than is needed, as we really only need properties
		// starting with
		// path, but it doesn't matter that much as the meta-data gets parsed
		// just
		// once for each level.
		return properties;
	}

	/** Read all the listed property files and combine them.
	 * @param metaDataBean contains resource names containing
	 * property files.  Add these to any existing meta-data provided in the 
	 * initial constructor.
	 */
	@SuppressWarnings("unchecked")
	public void setMetaData(MetaDataBean metaDataBean) {
		List<MetaDataBean> children = metaDataBean.sorted();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		// Reverse in-place so as to add least-relevent items first, and
		// override them later.
		Collections.reverse(children);
		// Use an indexed loop to allow extending the items in the loop itself.
		for(int i=0; i< children.size(); i++) {
			MetaDataBean child = children.get(i);
			try {
				InputStream is = cl.getResourceAsStream((String) child
						.getValue());
				Properties props = new Properties();
				if( is==null ) {
					// This happens when the PropertyProvider is sub-classed with a direct map object.
					log.info("Child of "+metaDataBean.path + " has null resource stream "+child.path + " for path "+child.getValue());
					continue;
				}
				assert is!=null;
				props.load(is);
				String prefix = props.getProperty("metadata.prefix");
				
				if( prefix!=null ) {
					prefix = prefix.trim();
					if( !prefix.endsWith(".") ) prefix = prefix+".";
				}
				for (Map.Entry me : props.entrySet()) {
					String key = (String) me.getKey();
					if( prefix!=null ) key = prefix+key;
					properties.put(key, (String) me.getValue());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
