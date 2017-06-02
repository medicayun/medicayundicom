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
 * Portions created by the Initial Developer are Copyright (C) 2008
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
package org.dcm4chee.xero.model;


import java.io.IOException;

import org.dcm4chee.xero.test.JSTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests the JavaScript model
 * 
 * @author bwallace
 * 
 */
public class XeroJSModelTest {
	static final Logger log = LoggerFactory.getLogger(XeroJSModelTest.class);

	static final JSTemplate jst = new JSTemplate("rhinoAccess", "xeroModelTests", "xeroModel", "xeroTest");

	@Test
	public void imageLayoutTest() {
		runTest("imageLayoutTests", false);
	}

	@Test
	public void mergedTest() {
		runTest("mergedTest", false);
	}

	@DataProvider(name="navigationProvider")
	public Object[][] navigationProvider() {
	   return new Object[][]{
             new Object[]{"test_lookupId"},
             new Object[]{"test_getExtChildren"},
             new Object[]{"test_getCustomChildren"},
             new Object[]{"test_setNavigation_expectEvent"},
             new Object[]{"test_setConversation_expectEvent"},
             new Object[]{"test_setMerged_expectEvent"},
             new Object[]{"testStudyQuery"},
             //new Object[]{"testModifiedQuery"},
       };
	}
	
	@Test(dataProvider="navigationProvider")
	public void navigationTests(String test) throws IOException {
	   jst.runTest("navigationTests", test, false);
	}

	@Test
	public void navConversationTest() {
		runTest("navConversationTests", false);
	}

    @Test
    public void layoutTest() {
        runTest("layoutTest");
    }

	@Test
	public void studyDataTest() {
		runTest("studyDataTests",false);
	}
	
	
	/** Run the named javascript object test */
	public static void runTest(String jsName) {
		jst.runTest(jsName,false);
	}
	
	/** Run the named, JavaScript object test */
	public static void runTest(String jsName, boolean verbose) {
		jst.runTest(jsName,verbose);
	}
	
}
