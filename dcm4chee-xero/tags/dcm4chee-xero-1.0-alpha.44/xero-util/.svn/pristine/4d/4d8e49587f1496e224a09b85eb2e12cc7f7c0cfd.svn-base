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

public class MetaDataBeanTest {

	Map<String, String> getTheme() {
		Map<String, String> ret = new HashMap<String, String>();
		ret.put("child1.meta1", "meta 1");
		ret.put("child1.meta2", "meta 2");
		ret.put("child1.meta3", "meta 3");

		// Causes child2 to inherit from child 1 by default.
		ret.put("child2.inherit", "child1");
		ret.put("child2.meta2", "alternate 2");
		ret.put("child2.meta3", "${null}");

		// Top-level attributes to test.
		ret.put("singleton1", "singleton 1");
		ret.put("singleton2", "singleton 2");

		// Bean injection tests
		ret.put("tbd.str5", "str 5");
		ret.put("tbd.str6", "garbage");
		ret.put("tb.inherit", "tbd");
		ret.put("tb.str1", "str 1");
		ret.put("tb.str2", "str 2");
		ret.put("tb.str3", "str 3");
		ret.put("tb.str4", "str 4");
		ret.put("tb.str6", "str 6");
		ret.put("tb.int1", "1");

		// Bean outjection test
		ret.put("out",
				"${class:org.dcm4chee.xero.metadata.TestBean}");

		// Complex inheritance test
		ret.put("a.b.c.v1", "abcv1"); // a only
		// ret.put("a.b.c.v2", "abcv2"); // d only
		ret.put("d.b.c.v2", "dbcv2");
		ret.put("a.b.c.v3", "abcv3"); // a and d
		ret.put("d.b.c.v3", "dbcv3");
		ret.put("a.b.c.v4", "abcv4"); // a,d, and i2
		ret.put("d.b.c.v4", "dbcv4");
		ret.put("a.v5", "av5");
		ret.put("d.v5", "dv5");

		ret.put("i1.inherit", "a");
		ret.put("i2.inherit", "a");
		ret.put("i2.b.c.inherit", "d.b.c");
		ret.put("i2.b.c.v4", "i2bcv4");

		return ret;
	}

	MetaDataBean mdb = new MetaDataBean(getTheme());

	@Test
	public void testComplexInheritance() {
		MetaDataBean i1bc = mdb.getForPath("i1.b.c");
		MetaDataBean i2bc = mdb.getForPath("i2.b.c");

		assert i1bc != null;
		assert i2bc != null;

		assert "abcv1".equals(i1bc.getValue("v1"));
		assert i1bc.getValue("v2") == null;
		assert "abcv3".equals(i1bc.getValue("v3"));
		assert "abcv4".equals(i1bc.getValue("v4"));

		assert "abcv1".equals(i2bc.getValue("v1"));
		assert "dbcv2".equals(i2bc.getValue("v2"));
		assert "dbcv3".equals(i2bc.getValue("v3"));
		assert "i2bcv4".equals(i2bc.getValue("v4"));
		assert "av5".equals(mdb.getChild("i2").getValue("v5"));
	}

	@Test
	public void testSimpleMetaDataBean() {
		assert mdb.getChild("singleton1").getValue().equals("singleton 1");
		assert mdb.getChild("singleton2").getValue().equals("singleton 2");
		MetaDataBean mdbChild1 = mdb.getChild("child1");
		assert mdbChild1 != null;
		assert mdbChild1.getChild("meta1").getValue().equals("meta 1");
		assert mdbChild1.getChild("meta1abs") == null;
		assert mdbChild1.getChild("meta2").getValue().equals("meta 2");
	}

	@Test
	public void testInheritedMetaData() {
		MetaDataBean mdb2 = mdb.getChild("child2");
		// Simple inheritance
		assert "meta 1".equals(mdb2.getChild("meta1").getValue());
		assert mdb2.getChild("meta2").getValue().equals("alternate 2");
		assert mdb2.getChild("meta3").getValue() == null;
	}

	/**
	 * Tests to see that a Java Bean with
	 * 
	 * @MetaData annotations gets injected correctly.
	 */
	@Test
	public void testInjection() {
		MetaDataBean mdbTB = mdb.getChild("tb");
		TestBean tb = new TestBean();
		// The next step is typically done as part of a setMetaData method.
		mdbTB.inject(tb);
		assert tb.int1 == 1;
		assert tb.str1.equals("str 1");
		assert tb.str2 == null;
		assert tb.str3.equals("str 4");
		// Inherit test
		assert tb.str5.equals("str 5");
		// Inherit with over-ride test.
		assert tb.str6.equals("str 6");
	}

	/**
	 * Tests to see that out-jection from an
	 * 
	 * @MetaData tag works.
	 */
	@Test
	public void testOutjection() {
		MetaDataBean mdbOut = mdb.getChild("out");
		assert (mdbOut) != null;
		TestBean bean = (TestBean) mdbOut.getValue();
		assert bean != null;
		assert mdbOut.getValue("str1") != null;
		assert mdbOut.getValue("str1").equals("meta-str1");
		assert bean.str1 != null;
		assert bean.str1.equals("meta-str1");
	}

}

class TestBean {
	public String str1 = "orig";
	public String str2, str3, str5, str6;

	public int int1;

	public TestBean childBean;

	@MetaData(out = "meta-str1")
	public void setStr1(String str1) {
		this.str1 = str1;
	};

	public void setStr2(String str2) {
		this.str2 = str2;
	};

	@MetaData(value="str4",required=false)
	public void setStr3(String str3) {
		this.str3 = str3;
	};

	@MetaData(required=false)
	public void setStr5(String str5) {
		this.str5 = str5;
	};

	@MetaData(required=false)
	public void setStr6(String str6) {
		this.str6 = str6;
	};

	@MetaData(required=false)
	public void setInt1(int int1) {
		this.int1 = int1;
	}

}

