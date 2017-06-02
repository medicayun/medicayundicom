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

import org.testng.annotations.Test;

/**
 * Tests the instance value provider
 * @author bwallace
 *
 */
public class ReferenceValueProviderTest {
	static MetaDataBean mdb = StaticMetaData.getMetaData("ReferenceValueProvider.metadata"); 

	@Test
	public void testStringSame() {
	   assert mdb!=null;
	   Object v = mdb.getValue("value1");
	   assert v!=null;
	   Object vSame = mdb.getValue("valueSameAs1");
	   assert vSame!=null;
	   assert vSame==v;
	}

	@Test
	public void testInheritSame() {
	   assert mdb!=null;
	   Object v = mdb.getValue("value1");
	   assert v!=null;
	   Object vSame = mdb.getValue("valueSameAs1H");
	   assert vSame!=null;
	   System.out.println(vSame);
	   assert vSame==v;
	}

	@Test
	public void testObjectSame() {
	   assert mdb!=null;
	   Object v = mdb.getValue("value.two");
	   assert v!=null;
	   Object vSame = mdb.getValue("valueSameAsTwo");
	   assert vSame!=null;
	   assert vSame==v;
	}

	@Test
	public void testObjectDifferent() {
	   assert mdb!=null;
	   Object v = mdb.getValue("value.two");
	   assert v!=null;
	   Object vSame = mdb.getValue("valueDifferentFromTwo");
	   assert vSame!=null;
	   assert vSame!=v;
	}
	
	@Test void testInheritsObjectsSame() {
		assert mdb!=null;
		Object v = mdb.getValue("value3");
		assert v!=null;
		assert v.getClass().getName().contains("ReferenceValueProvider");
		
		Object vRef = mdb.getValue("value3Ref");
		assert vRef!=null;
		assert vRef.getClass().getName().contains("ReferenceValueProvider");
		
		Object vInherit = mdb.getValue("value3Inherits");
		assert vInherit!=null;
		assert vInherit.getClass().getName().contains("ReferenceValueProvider");
		
		assert v==vRef;
		assert v!=vInherit;

	}
	
	@Test void testDifferencesBetweenRefAndInherit() {
		assert mdb!=null;
		Object stringValue = mdb.getValue("value4");
		assert stringValue!=null;
		assert stringValue.equals("some value");

		Object objectValue = mdb.getValue("value4.provider");
		assert objectValue!=null;
		assert objectValue.getClass().getName().contains("ReferenceValueProvider");

		Object stringRef = mdb.getValue("value4Ref");
		assert stringRef!=null;
		assert stringRef.equals("some value");

		Object objectRef = mdb.getValue("value4Ref.provider");
		assert objectRef!=null;
		assert objectRef.getClass().getName().contains("ReferenceValueProvider");

		Object stringInherit = mdb.getValue("value4Inherit");
		assert stringInherit!=null;
		assert stringInherit.equals("some value");

		Object objectInherit = mdb.getValue("value4Inherit.provider");
		assert objectInherit!=null;
		assert objectInherit.getClass().getName().contains("ReferenceValueProvider");
		
		// String values should all be equal
		assert stringValue == stringRef;
		assert stringValue == stringInherit;
		
		// These object values should be equal, as one is a pointer to the other...
		assert objectValue == objectRef;
		
		// ... but these should not be.  They are separate instances.
		assert objectValue != objectInherit;
	
	}

}
