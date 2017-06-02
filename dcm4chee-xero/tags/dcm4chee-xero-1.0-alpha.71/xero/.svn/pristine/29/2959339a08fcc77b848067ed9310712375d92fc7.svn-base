/**
 ***** BEGIN LICENSE BLOCK *****
 Version: MPL 1.1/GPL 2.0/LGPL 2.1
 
 The contents of this file are subject to the Mozilla Public License Version 
 1.1 (the "License"); you may not use this file except in compliance with 
 the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the
 License.
 
 The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at http://sourceforge.net/projects/dcm4che
  
 The Initial Developer of the Original Code is Agfa Healthcare.
 Portions created by the Initial Developer are Copyright (C) 2009 the Initial Developer. All Rights Reserved.
 
 Contributor(s):
 Joe Boccanfuso <joe.boccanfuso@agfa.com>
 
 Alternatively, the contents of this file may be used under the terms of
 either the GNU General Public License Version 2 or later (the "GPL"), or
 the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 in which case the provisions of the GPL or the LGPL are applicable instead
 of those above. If you wish to allow use of your version of this file only
 under the terms of either the GPL or the LGPL, and not to allow others to
 use your version of this file under the terms of the MPL, indicate your
 decision by deleting the provisions above and replace them with the notice
 and other provisions required by the GPL or the LGPL. If you do not delete
 the provisions above, a recipient may use your version of this file under
 the terms of any one of the MPL, the GPL or the LGPL.
 
 ***** END LICENSE BLOCK *****
*/

package org.dcm4chee.xero.controller;

import org.dcm4chee.xero.test.JSTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class XeroJSStringTest
{
    static final Logger log = LoggerFactory.getLogger(XeroJSStringTest.class);

    private JSTemplate jst = new JSTemplate("rhinoAccess","xeroTest", "xeroController", "xeroControllerTests");
    
    private boolean isVerbose = false;

    @Test
    public void testTrim_ShouldNotAlterString_WhenTheStringHasNoLeadingOrTrailingWhitespace()
    {
        jst.runTest("stringTest", "testTrim_ShouldNotAlterString_WhenTheStringHasNoLeadingOrTrailingWhitespace", isVerbose);
    }

    @Test
    public void testTrim_ShouldTrimLeadingWhitespace()
    {
        jst.runTest("stringTest", "testTrim_ShouldTrimLeadingWhitespace", isVerbose);
    }
    
    @Test
    public void testTrim_ShouldTrimTrailingWhitespace()
    {
        jst.runTest("stringTest", "testTrim_ShouldTrimTrailingWhitespace", isVerbose);
    }
    
    @Test
    public void testTrim_ShouldTrimLeadingAndTrailingWhitespace()
    {
        jst.runTest("stringTest", "testTrim_ShouldTrimLeadingAndTrailingWhitespace", isVerbose);
    }
    
}