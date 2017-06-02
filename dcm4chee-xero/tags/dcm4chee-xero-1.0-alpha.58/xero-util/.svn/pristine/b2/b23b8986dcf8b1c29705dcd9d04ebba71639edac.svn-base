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

import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.testng.annotations.Test;

/**
 *  This class tests some of the memory cache filter helper methods to create a query and
 *  to get a query.
 * @author bwallace
 *
 */
public class MemoryCacheFilterTest {

	/** Checks to see that removing parameters from the query string works
	 * successfully.
	 */
	@Test
	public void testQueryRemoval() {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("first", "123");
		map.put("end", "fini");
		map.put("middle", "456");
		map.put(MemoryCacheFilter.KEY_NAME,"first=123&middle=456&endmiddle=321&end=fini&");
		FilterUtil.removeFromQuery(map, "first");
		assert map.get(MemoryCacheFilter.KEY_NAME).equals("middle=456&endmiddle=321&end=fini");
		map.put(MemoryCacheFilter.KEY_NAME,"first=123&middle=456&endmiddle=321&end=fini");
		assert FilterUtil.removeFromQuery(map, "end")[0].equals("fini");
		assert map.get(MemoryCacheFilter.KEY_NAME).equals("first=123&middle=456&endmiddle=321");
		assert FilterUtil.removeFromQuery(map, "middle")[0].equals("456");
		assert map.get(MemoryCacheFilter.KEY_NAME).equals("first=123&endmiddle=321");
	}
}
