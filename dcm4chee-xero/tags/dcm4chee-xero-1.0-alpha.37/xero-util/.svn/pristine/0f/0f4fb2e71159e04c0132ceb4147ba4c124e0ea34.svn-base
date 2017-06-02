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
package org.antlr.stringtemplate.js;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.junit.Test;

/** Tests string template performance */
public class StringTemplateTest {
   
   @Test
   public void stringtemplateTest() {
	  StringTemplate simple = new StringTemplate("<html><head><title>$title$</title></head><body>Hello $userName$</body></html>");
	  simple.setAttribute("title", "1");
	  simple.setAttribute("userName", "0");
	  String s = simple.toString();
	  assert s.equals("<html><head><title>1</title></head><body>Hello 0</body></html>");
   }
   
   public static void main(String[] args) {
	  int count = 1000;
	  StringTemplate simple = new StringTemplate("<html><head><title>$title$</title></head><body>Hello $userName$</body></html>");
	  simple.setAttribute("title", "1");
	  simple.setAttribute("userName", "0");
	  simple.toString();
	  
	  long start;
	  start = System.nanoTime();
	  for(int i=0; i<count; i++) {
		 StringTemplate dup = simple.getInstanceOf();
		 dup.setAttribute("title", i);
		 dup.setAttribute("userName", "Fred"+i);
		 dup.toString();
	  }
	  System.out.println("Simple test took "+(System.nanoTime()-start)/(1e6*count)+" ms/render");
	  
	  StringTemplateGroup group = new StringTemplateGroup("perf");
	  StringTemplate twoParamMidSize = group.getInstanceOf("templates/twoParamMidSize");
	  twoParamMidSize.setAttribute("title", "1");
	  twoParamMidSize.setAttribute("userName", "0");
	  twoParamMidSize.toString();

	  start = System.nanoTime();
	  for(int i=0; i<count; i++) {
		 twoParamMidSize = group.getInstanceOf("templates/twoParamMidSize");
		 twoParamMidSize.setAttribute("title", i);
		 twoParamMidSize.setAttribute("userName",i*2);
		 twoParamMidSize.toString();
	  }
	  System.out.println("Mid size two param test took "+(System.nanoTime()-start)/(1e6*count)+" ms/render");
	  
	  twoParamMidSize = group.getInstanceOf("templates/twoParamMidSize");
	  //System.out.println("Result="+twoParamMidSize.toString());
   }
}
