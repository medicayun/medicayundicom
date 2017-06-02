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
import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterList;
import org.testng.annotations.Test;

/**
 * Tests filter lists.  A filter is a specialized list where the OUTPUT of a filter
 * is determined by a chain of processing, based on a desired type, and some number of
 * parameters.
 * For instance, for WADO, the ultimate type might be a PNG servlet stream.
 * This might be a 2 chain element with 1 element being a standard servlet cache,
 * and the other being an Image Object to PNG filter.
 * The Image object is itself defined by a a filter set, with the following items:
 * Convert DICOM object to Java Image, Sub-region extract, resize, server-local memory cache, 
 * window-level,embed GSPS, embed demographics.  The embed GSPS might have multiple items
 * that compose it, including a convert DICOM to GSPS filter parameters.  Thus, there
 * are quite a few steps, some or all of which may need replaced or extended in some
 * circumstances.  The preference is always to extend, with replacement being preferred
 * only when the same set of filters need to be re-used in a different order or with different
 * low-level parameters that cannot be easily overridden.
 * 
 * Filters are usually expected to be configured as MBeans, usually JBoss MXBeans in fact,
 * with the top-level filter being configured as a servlet.  The servlet specification
 * does allow filters to be defined, but there are some reasons not to use that definition
 * for this use.  First, servlets need all the code included in the WAR file directly, or in a 
 * parent EAR.  The intention here is that filters can be added by deploying the new
 * service and just registering the JNDI names appropriately.  Then, refreshing the
 * WAR service should start using the new/modified filters.  Secondly, the servlet filters
 * are not easily re-defined for a second instance - for example, a site might want to
 * test a new version and deploy the new version of the filters, WITHOUT undeploying the
 * old version, or testing an extension for some specific use without having everyone need to
 * use the updated system, and finally, if a filter is important for some users, but doesn't
 * have a good way to determine the usage on a per-user basis, it should be possible to
 * deploy the filter set individually without redeploying everything else.
 * @author bwallace
 *
 */
public class FilterMetaDataTest {
	static class AddFilter implements Filter<Integer>
	{

		public Integer filter(FilterItem<Integer> filterItem, Map<String,Object> params) {
			Integer value = (Integer) filterItem.callNextFilter(params);
			if( value==null ) return null;
			Integer paramValue = (Integer) params.get("add");
			if ( paramValue==null ) return value;
			value = value + paramValue;
			return value;
		}

	}
	
	static class SourceFilter<T> implements Filter<T>
	{
		Class<T> clazz;
		
		public SourceFilter(Class<T> clazz)
		{
			this.clazz = clazz;
		}

		public T filter(FilterItem<T> filterItem, Map<String, Object> params) {
			Object value = params.get("src");
			if ( clazz.isInstance(value) ) return clazz.cast(value);
			Object nextValue = filterItem.callNextFilter(params);
			if( nextValue==null ) return null;
			if( ! clazz.isInstance(nextValue) ) {
				System.out.println("Wrong value type found:"+nextValue+" of type "+nextValue.getClass().getName()
						+ " from "+filterItem.getNextFilterName());
			}
			return clazz.cast(nextValue);
		}		
	}
	
	static class ConvertFilter implements Filter<String>
	{
		String subFilter;
		public ConvertFilter(String subFilter)
		{
			this.subFilter = subFilter;
		}
		
		/** Convert the object into a string */
		public String filter(FilterItem<String> filterItem, Map<String,Object> params) {
			String ret = filterItem.callNextFilter(params);
			if ( ret!=null ) return ret.toString();
			Object obj = filterItem.callNamedFilter(subFilter,params);
			if( obj!=null ) return obj.toString();
			return null;
		}

	}
	
	static Map<String,Object> properties = new HashMap<String,Object>();
	static {
		// A string filter
		properties.put("str","${class:org.dcm4chee.xero.metadata.filter.FilterList}");
		properties.put("str.priority", "-1");
		properties.put("str.int", "${ref:int}");
		properties.put("str.int.priority", -1);
		properties.put("str.convert",new ConvertFilter("int"));
		properties.put("str.convert.priority", "20");
		properties.put("str.source", new SourceFilter<String>(String.class));
		properties.put("str.source.priority", "10");
		// An int filter
		properties.put("int","${class:org.dcm4chee.xero.metadata.filter.FilterList}");
		// A -1 priority means don't use this filter unless explicitly requested.
		properties.put("int.priority", "-1");
		properties.put("int.baseItem",new SourceFilter<Integer>(Integer.class));		
		// TODO modify this to use meta-data to retrieve the filter priority. 
		properties.put("int.baseItem.priority","5");
		properties.put("int.addFilter", new AddFilter());
		properties.put("int.addFilter.priority", 10);
	}
	
	MetaDataBean mdb = new MetaDataBean(properties);
	
	@SuppressWarnings("unchecked")
   @Test
	public void singleIntFilterTest()
	{
		Map<String,Object> params = new HashMap<String,Object>();
		
		// This retrieves an int filter - this is the overall filter for this level, 
		// not a filter instance object.
		MetaDataBean mdbInt = mdb.getChild("int");
		FilterList<Integer> filter = (FilterList<Integer>) mdbInt.getValue();
		//FilterItem<Integer> fi = new FilterItem<Integer>(mdbInt);
		assert filter!=null;
		params.put("src", new Integer(13));
		params.put("add", new Integer(7));
		assert filter.filter(null, params)==20;
		params.put("src", "fred");
		assert filter.filter(null, params)==null;
	}

	@SuppressWarnings("unchecked")
   @Test
	public void multipleFilterTest()
	{
		Map<String,Object> params = new HashMap<String,Object>();
		
		MetaDataBean mdbStr = mdb.getChild("str");
		FilterList<String> filter = (FilterList<String>) mdbStr.getValue();
		assert filter!=null;
		params.put("src", new Integer(5));
		// Going through the src filter.
		String str = filter.filter(null, params);
		assert "5".equals(str);
		params.put("add", new Integer(-3));
		str = filter.filter(null, params);
		assert "2".equals(str);
		params.put("src", "14");
		assert "14".equals(filter.filter(null, params));
	}

}
