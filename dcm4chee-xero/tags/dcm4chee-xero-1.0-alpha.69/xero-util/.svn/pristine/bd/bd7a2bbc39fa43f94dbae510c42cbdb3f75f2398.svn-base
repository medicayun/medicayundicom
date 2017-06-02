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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * TODO:  Just catch IOException in the writeEvent() method.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class JSONXMLEventWriterTest
{
   private StringWriter outputWriter;
   private JSONXMLEventWriter eventWriter;

   @BeforeMethod
   public void setup()
   {
      this.outputWriter = new StringWriter();
      this.eventWriter = new JSONXMLEventWriter(outputWriter);
   }
   
   @Test(expectedExceptions=IllegalArgumentException.class)
   public void constructorThrowsIllegalArgument_WhenWriterIsNull()
   {
      new JSONXMLEventWriter(null);
   }
   
   @Test
   public void close() throws XMLStreamException
   {
      MockWriter mw = new MockWriter();
      JSONXMLEventWriter sw = new JSONXMLEventWriter(mw);
      
      assertFalse(mw.closed);
      sw.close();
      assertTrue(mw.closed);
   }
   
   @Test
   public void flush() throws XMLStreamException
   {
      MockWriter mw = new MockWriter();
      JSONXMLEventWriter sw = new JSONXMLEventWriter(mw);
      
      assertFalse(mw.flushed);
      sw.flush();
      assertTrue(mw.flushed);
   }

   
   @Test
   public void writeAttribute() throws XMLStreamException
   {
      Attribute att = XMLEventMocker.createAttribute("patientName","SHATNER^WILLIAM");
      eventWriter.add(att);
      
      assertEquals(getJSON(),
            "\"patientName\":\"SHATNER^WILLIAM\"");
   }
   
   @Test
   public void writeAttribute_ShouldBeCommaSeparated() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createAttribute("patientName","BILL"));
      eventWriter.add(XMLEventMocker.createAttribute("patientID", "5"));
      
      assertEquals(getJSON(),
            "\"patientName\":\"BILL\",\"patientID\":\"5\"");
   }
   
   @Test
   public void element_MultipleItemsShouldBeInArray() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createStartDocument());
      eventWriter.add(XMLEventMocker.createStartElement("series"));
      eventWriter.add(XMLEventMocker.createStartElement("image"));
      eventWriter.add(XMLEventMocker.createAttribute("InstanceNumber","1"));
      eventWriter.add(XMLEventMocker.createEndElement("image")); 
      eventWriter.add(XMLEventMocker.createStartElement("image"));
      eventWriter.add(XMLEventMocker.createAttribute("InstanceNumber","2"));
      eventWriter.add(XMLEventMocker.createEndElement("image")); 
      eventWriter.add(XMLEventMocker.createEndElement("series")); 
      eventWriter.add(XMLEventMocker.createEndDocument());
      
      assertEquals(getJSON(),
         "{\"series\":[{\"image\":[{\"InstanceNumber\":\"1\"},{\"InstanceNumber\":\"2\"}]}]}",
         "Elements must be contained in a Javascript array");
   }

   @Test 
   public void simpleAttributeAndElementHierarchy() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createStartDocument());
      eventWriter.add(XMLEventMocker.createStartElement("results"));
      eventWriter.add(XMLEventMocker.createStartElement("patient"));
      eventWriter.add(XMLEventMocker.createAttribute("patientID", "1.1"));
      eventWriter.add(XMLEventMocker.createStartElement("study"));
      eventWriter.add(XMLEventMocker.createAttribute("studyUID", "1.2"));
      eventWriter.add(XMLEventMocker.createEndElement("study")); // study
      eventWriter.add(XMLEventMocker.createEndElement("patient")); // patient
      eventWriter.add(XMLEventMocker.createEndElement("results")); // results
      eventWriter.add(XMLEventMocker.createEndDocument());
      
      assertEquals(getJSON(),
            "{\"results\":[{\"patient\":[{\"patientID\":\"1.1\",\"study\":[{\"studyUID\":\"1.2\"}]}]}]}");
   }

   
   @Test
   public void writeStartDocument() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createStartDocument());
      assertEquals(getJSON(),"{");
   }
   
   
   @Test 
   public void writeEndDocument() throws XMLStreamException 
   {
      eventWriter.add(XMLEventMocker.createEndDocument());
      assertEquals(getJSON(),"}");
   }
   
   @Test
   public void writeStartElement() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createStartElement("patient"));
      assertEquals(getJSON(),"\"patient\":[{");
   }
   
   @Test
   public void writeEndElement() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createEndElement());
      assertEquals(getJSON(),"}");
   }
   


   @Test
   public void attribute_ShouldBeCommaSeparated() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createAttribute("patientName", "BILL"));
      eventWriter.add(XMLEventMocker.createAttribute("patientID", "5"));
      assertEquals(getJSON(),
            "\"patientName\":\"BILL\",\"patientID\":\"5\"");
   }
   
   @Test
   public void escapeQuotesInData() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createAttribute("patientName", "Mr\"T\""));
      assertEquals(getJSON(),
         "\"patientName\":\"Mr\\\"T\\\"\"");
      
   }
   
   @Test
   public void checkJSONStructureWithJavascript() throws XMLStreamException, ScriptException
   {
      eventWriter.add(XMLEventMocker.createStartDocument());
      eventWriter.add(XMLEventMocker.createStartElement("results"));
      eventWriter.add(XMLEventMocker.createStartElement("patient"));
      eventWriter.add(XMLEventMocker.createAttribute("patientID", "1.1"));
      eventWriter.add(XMLEventMocker.createAttribute("patientName", "Back\\Slash"));
      eventWriter.add(XMLEventMocker.createStartElement("study"));
      eventWriter.add(XMLEventMocker.createAttribute("studyUID", "1.2"));
      eventWriter.add(XMLEventMocker.createEndElement("study")); // study
      eventWriter.add(XMLEventMocker.createEndElement("patient")); // patient
      eventWriter.add(XMLEventMocker.createEndElement("results")); // results
      eventWriter.add(XMLEventMocker.createEndDocument());

      ScriptEngineManager sem = new ScriptEngineManager();
      ScriptEngine engine = sem.getEngineByName("JavaScript");
      engine.put("json","("+getJSON()+")");
      
      StringBuilder script = new StringBuilder();
      script.append("print(\"Data=\"+json);");
      script.append("function assertEquals(actual,expected){ if(actual!=expected)throw \"Error '\"+actual+\"'!='\"+actual+\"'\"; };");
      script.append("var data = eval(json);");
      script.append("print(\"patientName=\"+data.results[0].patient[0].patientName);");
      script.append("assertEquals(data.results[0].patient[0].patientName,\"Back\\\\Slash\");");
      script.append("assertEquals(data.results[0].patient[0].patientID,\"1.1\");");
      script.append("assertEquals(data.results[0].patient[0].study[0].studyUID,\"1.2\");");

      System.out.println("Script="+script);
      engine.eval(script.toString());
   }
   
   // Utility methods / classes

   private String getJSON() throws XMLStreamException
   {
      eventWriter.flush();
      return outputWriter.toString();
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
