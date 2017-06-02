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

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.access.ValueList;
import org.testng.annotations.Test;

/** Tests the overall value list code.
 * @author bwallace
 *
 */
public class ValueListTest {
	static Map<String,Object> prop = new HashMap<String,Object>();
	static {
	   prop.put("list","${class:org.dcm4chee.xero.metadata.access.ValueList}");
	   prop.put("list.className","org.dcm4chee.xero.metadata.access.ListItem");
	   prop.put("list.a","${class:org.dcm4chee.xero.metadata.access.ListItem}");
	   prop.put("list.a.priority","50");
	   prop.put("list.b","${class:org.dcm4chee.xero.metadata.access.ListItem}");
	   prop.put("list.b.priority","25");
	   prop.put("list.c","${class:org.dcm4chee.xero.metadata.access.ListItem}");
	   prop.put("list.c.priority","75");
	   prop.put("list.f","${class:org.dcm4chee.xero.metadata.access.ListItem}");
	   prop.put("list.f.priority","100");
	   prop.put("list.e","${class:org.dcm4chee.xero.metadata.access.ListItem}");
	   prop.put("list.e.priority","100");
	   prop.put("list.d","${class:org.dcm4chee.xero.metadata.access.ValueListTest}");
	   prop.put("list.d.priority","0");
	   prop.put("empty","${class:org.dcm4chee.xero.metadata.access.ValueList}");
	}
	
	static MetaDataBean mdb = new MetaDataBean(prop);
	

	/** Tests that empty lists can be safely created */
	@Test
	@SuppressWarnings("unchecked")
	public void testEmptyList() {
	   ValueList<ListItem> vl = (ValueList<ListItem>)mdb.getValue("empty");
	   assert vl!=null;
	   assert vl.size()==0;
	}
	
	/** Tests the ordering and content type of the value list. */
	@Test
	@SuppressWarnings("unchecked")
	public void testValueList() {
		ValueList<ListItem> vl = (ValueList<ListItem>) mdb.getValue("list");
		assert vl!=null;
		// Item d is not included as it is the wrong type.  Thus 3 items.  
		assert vl.size()==5;
		assert vl.get(0).name.equals("b");
		assert vl.get(1).name.equals("a");
		assert vl.get(2).name.equals("c");
		// Items with same priority must be alphabetical by name
		assert vl.get(3).name.equals("e");
		assert vl.get(4).name.equals("f");
	}
	
}

