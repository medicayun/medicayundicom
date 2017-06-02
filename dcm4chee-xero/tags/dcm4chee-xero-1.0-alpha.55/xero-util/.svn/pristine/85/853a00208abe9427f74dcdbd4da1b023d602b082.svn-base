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
package org.dcm4chee.xero.rhino;

import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.access.LazyMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PathInstanceTest {

	Map<String,Object> src;
	Map<String,Object> lazy;
	
	PathInstance<Integer> pii;
	PathInstance<String> pix;
	
	static final String STATIC_SCRIPT = ""+
	  "function MyType(x) { this.x = x; };\n"+
	  "MyType.prototype.i=3;\n"+
	  "MyType.prototype.s='string';\n"+
	  "MyType.prototype.setX = function MyType_setX(x) { this.x = x; };\n"+
	  "var myUnmodifiable=new MyType('Hello, World.');\n"
	;
	
   @BeforeMethod
	public void init() {
		lazy = new HashMap<String,Object>();
		JavaScriptMapFactory jsmf = new JavaScriptMapFactory();
		jsmf.setScript(STATIC_SCRIPT);
		lazy.put("js",jsmf);
		pii = new PathInstance<Integer>();
		pii.setVar("js.myUnmodifiable.i");
		lazy.put("pii", pii);
		pix = new PathInstance<String>();
		pix.setVar("js.myUnmodifiable.x");
		lazy.put("pix", pix);
		src = new LazyMap(lazy);
	}

	@Test
	public void test_instance() {
		int i = (Integer) src.get("pii");
		assert i==3;
		String x = (String) src.get("pix");
		assert x.equals("Hello, World.");
	}
	
}
