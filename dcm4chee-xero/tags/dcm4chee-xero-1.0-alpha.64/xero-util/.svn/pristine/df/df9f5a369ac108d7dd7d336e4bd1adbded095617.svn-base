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
package org.dcm4chee.xero.template;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.servlet.StringSafeRenderer;
import org.dcm4chee.xero.metadata.MetaDataBean;
import org.dcm4chee.xero.metadata.StaticMetaData;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/** Tests the auto string template group by ensuring that the various attributes get correctly set. */
public class AutoStringTemplateGroupTest {

	@Test
	public void testAttributeSet() {
		  MetaDataBean root = StaticMetaData.getMetaData("util-test.metadata");
		  MetaDataBean mdb = root.getChild("model");
		  AutoStringTemplateGroup stg = (AutoStringTemplateGroup) mdb.getValue(StringTemplateFilter.TEMPLATE_GROUP);
		  assert stg!=null;
		  assert stg.getName().equals("utilTestTemplates");
		  assert stg.getRefreshInterval()==5;
		  assert stg.getSuperGroup().getName().equals("testSuper");
		  // TODO - figure out a better test for this...
		  assert stg.getAttributeRenderers()==StringSafeRenderer.JS_RENDERERS;
	}

	@Test
	public void testTemplateGroupFile() {
		  MetaDataBean root = StaticMetaData.getMetaData("util-test.metadata");
		  MetaDataBean mdb = root.getChild("model");
		  AutoStringTemplateGroup stg = (AutoStringTemplateGroup) mdb.getValue(StringTemplateFilter.TEMPLATE_GROUP);
		  assert stg!=null;
		  StringTemplate gt = stg.getInstanceOf("groupTemplate");
		  assert gt!=null;
		  String s = gt.toString();
		  assert s.indexOf("The group template")!=-1;
	}
	
	@Test
	public void testTemplateGroupMap() {
		  MetaDataBean root = StaticMetaData.getMetaData("util-test.metadata");
		  MetaDataBean mdb = root.getChild("model");
		  AutoStringTemplateGroup stg = (AutoStringTemplateGroup) mdb.getValue(StringTemplateFilter.TEMPLATE_GROUP);
		  assert stg!=null;
		  StringTemplate gt = stg.getInstanceOf("useMap");
		  assert gt!=null;
		  String s = gt.toString();
		  assert s.indexOf("a=eh")!=-1;
		  assert s.indexOf("one=1")!=-1;
		  assert s.indexOf("other default=def")!=-1;
	}
	
	@Test
	public void isResource_NullPathIsFalse()
	{
	   AutoStringTemplateGroup stg = new AutoStringTemplateGroup();
	   assertFalse(stg.isArchivePath(null));
	}
	
  @Test
   public void isResource_PathWithBangIsTrue()
   {
      AutoStringTemplateGroup stg = new AutoStringTemplateGroup();
      assertTrue(stg.isArchivePath("d:/myarchive.zip!/myfile"));
   }
  
  @Test
  public void isResource_PathWithJARIsTrue()
  {
     AutoStringTemplateGroup stg = new AutoStringTemplateGroup();
     assertTrue(stg.isArchivePath("d:/myarchive.jar/myfile"));
  }
}
