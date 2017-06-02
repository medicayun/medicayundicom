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
import java.util.Map;

import org.testng.annotations.Test;

/** Tests whether meta-data providers listed in the meta-data itself are
 * correctly created/nested and deployed.
 * @author bwallace
 *
 */
public class MetaDataProviderTest {
	static Map<String,Object> prop = new HashMap<String,Object>();
	static {
	   prop.put("metaDataProvider.a","${class:org.dcm4chee.xero.metadata.MetaDataProviderBeanA}");
	   prop.put("metaDataProvider.a.priority","5");
	   prop.put("overrideA", "topOverrideA");
	   prop.put("overrideB", "topOverrideB");
	   prop.put("overrideAB", "topOverrideAB");
	}
	
	static MetaDataBean mdb = new MetaDataBean(prop);
	
	
	@Test
	public void firstTopLevelOverrides() {
		assert mdb.getValue("overrideA").equals("topOverrideA");
		assert mdb.getValue("overrideB").equals("topOverrideB");
		assert mdb.getValue("overrideAB").equals("topOverrideAB");
		
	}


	/** Tests to see that a metaDataProvider instance directly under the root
	 * is correctly read in.
	 */
	@Test
	public void firstLevelInheritanceTest() {
		assert mdb.getValue("OnlyA")!=null;
		assert mdb.getValue("OnlyA").equals("aOnlyA");
		assert mdb.getValue("BothAB").equals("aBothAB");
	}

	/** Tests to see that a metaDataProvider instance nested inside anotehr
	 * meta-data provider is also correclty read in.
	 */
	@Test
	public void secondLevelInheritanceTest() {
		assert mdb.getValue("OnlyB")!=null;
		assert mdb.getValue("OnlyB").equals("bOnlyB");
	}

}

class MetaDataProviderBeanA extends PropertyProvider
{
	static Map<String,Object> prop = new HashMap<String,Object>();
	static {
		   prop.put("metaDataProvider.b","${class:org.dcm4chee.xero.metadata.MetaDataProviderBeanB}");
		   prop.put("metaDataProvider.b.priority","10");
		   prop.put("overrideA", "aOverrideA");
		   prop.put("overrideAB", "aOverrideAB");
		   prop.put("OnlyA", "aOnlyA");
		   prop.put("BothAB", "aBothAB");
		}

	public MetaDataProviderBeanA() {
		super(prop);
	}
}

class MetaDataProviderBeanB extends PropertyProvider
{
	static Map<String,Object> prop = new HashMap<String,Object>();
	static {
		prop.put("overrideB", "bOverrideB");
		prop.put("overrideAB", "bOverrideAB");
		prop.put("OnlyB", "bOnlyB");
		prop.put("BothAB", "bBothAB");
	}

	public MetaDataProviderBeanB() {
		super(prop);
	}
}
