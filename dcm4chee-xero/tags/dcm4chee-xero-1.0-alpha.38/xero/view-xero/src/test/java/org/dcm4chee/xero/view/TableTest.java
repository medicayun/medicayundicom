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
package org.dcm4chee.xero.view;

import org.antlr.stringtemplate.StringTemplate;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.dcm4chee.xero.metadata.access.MapWithDefaults;
import org.dcm4chee.xero.template.AutoStringTemplateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Renders a table using default data set and tests the resulting output.
 * @author bwallace
 *
 */
public class TableTest {
   static final Logger log = LoggerFactory.getLogger(TableTest.class);
   static ClassLoader cl = Thread.currentThread().getContextClassLoader();
   
   static boolean print = false;

   MetaDataBean mdb = StaticMetaData.getMetaData("test-view.metadata");
   MetaDataBean model = mdb.getChild("model");

   @Test
   public void searchResultsTableTest() {
	  MapWithDefaults mwd = new MapWithDefaults(model);
	  AutoStringTemplateGroup stg = new AutoStringTemplateGroup();
	  stg.setGroupNames("xero,xeroModel");
	  StringTemplate st = stg.getInstanceOf("xero",mwd);
	  String result = st.toString();
	  if( print ) log.info("Result=\n"+result);
	  int cnt = 100;
	  long start = System.nanoTime();
	  for(int i=0; i<cnt; i++) {
		 mwd = new MapWithDefaults(model);
		 st = stg.getInstanceOf("xero",mwd);
		 st.toString();
	  }
	  log.info("Table generation took "+(System.nanoTime()-start)/(1e6*cnt)+" ms/iteration.");
	  // TODO - add some asserts about the table setup
   }
}
