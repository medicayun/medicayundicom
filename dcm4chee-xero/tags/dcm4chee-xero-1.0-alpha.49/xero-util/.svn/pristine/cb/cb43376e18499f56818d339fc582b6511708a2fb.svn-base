// ***** BEGIN LICENSE BLOCK *****
// Version: MPL 1.1/GPL 2.0/LGPL 2.1
// 
// The contents of this file are subject to the Mozilla Public License Version 
// 1.1 (the "License"); you may not use this file except in compliance with 
// the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
// 
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
// for the specific language governing rights and limitations under the
// License.
// 
// The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at http://sourceforge.net/projects/dcm4che
//  
// The Initial Developer of the Original Code is Agfa Healthcare.
// Portions created by the Initial Developer are Copyright (C) 2009 the Initial Developer. All Rights Reserved.
// 
// Contributor(s):
// Andrew Cowan <andrew.cowan@agfa.com>
// 
// Alternatively, the contents of this file may be used under the terms of
// either the GNU General Public License Version 2 or later (the "GPL"), or
// the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
// in which case the provisions of the GPL or the LGPL are applicable instead
// of those above. If you wish to allow use of your version of this file only
// under the terms of either the GPL or the LGPL, and not to allow others to
// use your version of this file under the terms of the MPL, indicate your
// decision by deleting the provisions above and replace them with the notice
// and other provisions required by the GPL or the LGPL. If you do not delete
// the provisions above, a recipient may use your version of this file under
// the terms of any one of the MPL, the GPL or the LGPL.
// 
// ***** END LICENSE BLOCK *****
package org.dcm4chee.xero.metadata.json;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JSONPrettyPrinterTest
{

   private JSONPrettyPrinter pretty;

   @BeforeMethod
   public void setup()
   {
      pretty = new JSONPrettyPrinter(new StringWriter());
   }
   
   @Test
   public void write_OneLevelIndent() throws IOException
   {
      String rawJSON = "{\"tagName\":\"markup\",\"children\":[{\"tagName\":\"circle\"},{\"tagName\":\"line\"},{\"tagName\":\"oval\"}]}";
      String prettyJSON = "{\"tagName\":\"markup\",\"children\":[\n\t{\"tagName\":\"circle\"},\n\t{\"tagName\":\"line\"},\n\t{\"tagName\":\"oval\"}]}";
       
      pretty.write(rawJSON);
      assertEquals(pretty.toString(),prettyJSON);
   }
   
   @Test
   public void close() throws IOException
   {
      MockWriter mw = new MockWriter();
      JSONPrettyPrinter jpp = new JSONPrettyPrinter(mw);
      
      assertFalse(mw.closed);
      jpp.close();
      assertTrue(mw.closed);
   }
   
   @Test
   public void flush() throws IOException
   {
      MockWriter mw = new MockWriter();
      JSONPrettyPrinter jpp = new JSONPrettyPrinter(mw);
      
      assertFalse(mw.flushed);
      jpp.flush();
      assertTrue(mw.flushed);
   }
   
   
   static class MockWriter extends Writer
   {
      public boolean flushed = false;
      public boolean closed = false;
      
      @Override
      public void close() throws IOException
      {
         closed = true;
      }

      @Override
      public void flush() throws IOException
      {
         flushed = true;
      }

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException
      {
         // Ignore.
      }
   }
}
