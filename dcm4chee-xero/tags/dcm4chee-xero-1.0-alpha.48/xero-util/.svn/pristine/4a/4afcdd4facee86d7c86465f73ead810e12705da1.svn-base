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
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for the grouped element JSON writer.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class GroupedElementJSONEventWriterTest
{
   private StringWriter outputWriter;
   private GroupedElementJSONEventWriter eventWriter;

   @BeforeMethod
   public void setup()
   {
      this.outputWriter = new StringWriter();
      this.eventWriter = new GroupedElementJSONEventWriter(outputWriter);
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
      GroupedElementJSONEventWriter sw = new GroupedElementJSONEventWriter(mw);
      
      assertFalse(mw.closed);
      sw.close();
      assertTrue(mw.closed);
   }
   
   @Test
   public void flush() throws XMLStreamException
   {
      MockWriter mw = new MockWriter();
      GroupedElementJSONEventWriter sw = new GroupedElementJSONEventWriter(mw);
      
      assertFalse(mw.flushed);
      sw.flush();
      assertTrue(mw.flushed);
   }

   @Test
   public void writeText()  throws XMLStreamException
   {
      eventWriter.add(createStartElement("report"));
      Characters text = XMLEventMocker.createCharacters("inconclusive");
      eventWriter.add(text);
      eventWriter.add(createEndElement("report"));
      
      assertEquals(getJSON(),
         "{\"tagName\":\"report\",\"TEXT\":\"inconclusive\"}");
   }
   
   
   @Test(enabled=false)
   public void writeText_ShouldEscapeLineFeeds()  throws XMLStreamException
   {
      
   }
   
   @Test
   public void writeAttribute_IngoreXSINamespace() throws XMLStreamException
   {
      String prefix = "i";
      String xsi = "http://www.w3.org/2001/XMLSchema-instance";
      String name = "type";
      String value = "SeriesBean";
      String type = "string";
         
      eventWriter.add(createStartElement("series"));
      eventWriter.add(XMLEventMocker.createAttribute(prefix,xsi,name,value,type));
      eventWriter.add(createAttribute("seriesUID", "1.2"));
      eventWriter.add(createEndElement("series")); 
      eventWriter.add(XMLEventMocker.createEndDocument());
      
      assertEquals(getJSON(),
         "{\"tagName\":\"series\",\"seriesUID\":\"1.2\"}",
         "Elements in the XSI namespace should be ignored.");
   }
   
   @Test
   public void writeAttribute() throws XMLStreamException
   {
      Attribute att = createAttribute("patientName","SHATNER^WILLIAM");
      eventWriter.add(att);
      
      assertEquals(getJSON(),
            "\"patientName\":\"SHATNER^WILLIAM\"");
   }
   
   @Test
   public void childElements_MustBeStoredInOriginalOrder() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createStartDocument());
      eventWriter.add(createStartElement("markup"));
      eventWriter.add(createStartElement("circle"));
      eventWriter.add(createEndElement("circle"));
      eventWriter.add(createStartElement("line"));
      eventWriter.add(createEndElement("line"));
      eventWriter.add(createStartElement("oval"));
      eventWriter.add(createEndElement("oval"));
      eventWriter.add(createEndElement("markup")); 
      eventWriter.add(XMLEventMocker.createEndDocument());
      
      assertEquals(getJSON(),
      "{\"tagName\":\"markup\",\"children\":[{\"tagName\":\"circle\"},{\"tagName\":\"line\"},{\"tagName\":\"oval\"}]}");
   }
   
   @Test
   public void writeAttribute_ShouldBeCommaSeparated() throws XMLStreamException
   {
      eventWriter.add(createAttribute("patientName","BILL"));
      eventWriter.add(createAttribute("patientID", "5"));
      
      assertEquals(getJSON(),
            "\"patientName\":\"BILL\",\"patientID\":\"5\"");
   }
   
   @Test
   public void element_MultipleItemsShouldBeInArray() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createStartDocument());
      eventWriter.add(createStartElement("series"));
      eventWriter.add(createStartElement("image"));
      eventWriter.add(createAttribute("InstanceNumber","1"));
      eventWriter.add(createEndElement("image")); 
      eventWriter.add(createStartElement("image"));
      eventWriter.add(createAttribute("InstanceNumber","2"));
      eventWriter.add(createEndElement("image")); 
      eventWriter.add(createEndElement("series")); 
      eventWriter.add(XMLEventMocker.createEndDocument());
      
      assertEquals(getJSON(),
         "{\"tagName\":\"series\",\"children\":[{\"tagName\":\"image\",\"InstanceNumber\":\"1\"},{\"tagName\":\"image\",\"InstanceNumber\":\"2\"}]}",
         "Elements must be contained in a Javascript array");
   }

   @Test 
   public void simpleAttributeAndElementHierarchy() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createStartDocument());
      eventWriter.add(createStartElement("results"));
      eventWriter.add(createStartElement("patient"));
      eventWriter.add(createAttribute("patientID", "1.1"));
      eventWriter.add(createStartElement("study"));
      eventWriter.add(createAttribute("studyUID", "1.2"));
      eventWriter.add(createEndElement("study")); // study
      eventWriter.add(createEndElement("patient")); // patient
      eventWriter.add(createEndElement("results")); // results
      eventWriter.add(XMLEventMocker.createEndDocument());
      
      assertEquals(getJSON(),
            "{\"tagName\":\"results\",\"children\":[{\"tagName\":\"patient\",\"patientID\":\"1.1\",\"children\":[{\"tagName\":\"study\",\"studyUID\":\"1.2\"}]}]}");
   }

   
   @Test
   public void writeStartElement() throws XMLStreamException
   {
      eventWriter.add(createStartElement("patient"));
      assertEquals(getJSON(),"{\"tagName\":\"patient\"");
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
      eventWriter.add(createAttribute("patientName", "BILL"));
      eventWriter.add(createAttribute("patientID", "5"));
      assertEquals(getJSON(),
            "\"patientName\":\"BILL\",\"patientID\":\"5\"");
   }
   
   @Test
   public void escapeQuotesInData() throws XMLStreamException
   {
      eventWriter.add(createAttribute("patientName", "Mr\"T\""));
      assertEquals(getJSON(),
         "\"patientName\":\"Mr\\\"T\\\"\"");
   }
   
   @Test
   public void escapeNewLineInData() throws XMLStreamException
   {
      eventWriter.add(createAttribute("comment", "Hello\nWorld"));
      assertEquals(getJSON(),
         "\"comment\":\"Hello\\nWorld\"");
   }
   
   @Test
   public void ignoreCarriageReturnInData() throws XMLStreamException
   {
      eventWriter.add(createAttribute("comment", "Hello\rWorld"));
      assertEquals(getJSON(),
         "\"comment\":\"HelloWorld\"");
   }
   
   @Test
   public void escapeTABInData() throws XMLStreamException
   {
      eventWriter.add(createAttribute("comment", "Hello\tWorld"));
      assertEquals(getJSON(),
         "\"comment\":\"Hello\\tWorld\"");
   }
   
   @Test
   public void escapeBackSpaceInData() throws XMLStreamException
   {
      eventWriter.add(createAttribute("comment", "Hello\bWorld"));
      assertEquals(getJSON(),
         "\"comment\":\"Hello\\bWorld\"");
   }
   
   @Test
   public void writeAttribute_IntegersWrittenWithoutQuotes() throws XMLStreamException
   {
      eventWriter.add(XMLEventMocker.createAttribute("i",null,"InstanceNumber","15","int"));
      assertEquals(getJSON(),
         "\"InstanceNumber\":15");
   }
   
   @Test
   public void checkJSONStructureWithJavascript() throws XMLStreamException, ScriptException
   {
      eventWriter.add(XMLEventMocker.createStartDocument());
      eventWriter.add(createStartElement("results"));
      eventWriter.add(createStartElement("patient"));
      eventWriter.add(createAttribute("patientID", "1.1"));
      eventWriter.add(createAttribute("patientName", "Back\\Slash"));
      eventWriter.add(createStartElement("study"));
      eventWriter.add(createAttribute("studyUID", "1.2"));
      eventWriter.add(createEndElement("study")); // study
      eventWriter.add(createEndElement("patient")); // patient
      eventWriter.add(createEndElement("results")); // results
      eventWriter.add(XMLEventMocker.createEndDocument());

      ScriptEngineManager sem = new ScriptEngineManager();
      ScriptEngine engine = sem.getEngineByName("JavaScript");
      engine.put("json","("+getJSON()+")");
      
      StringBuilder script = new StringBuilder();
      script.append("print(\"Data=\"+json);");
      script.append("function assertEquals(actual,expected){ if(actual!=expected)throw \"Error '\"+actual+\"'!='\"+actual+\"'\"; };");
      script.append("var data = eval(json);");
      script.append("print(\"patientName=\"+data.children[0].children[0].patientName);");
      script.append("assertEquals(data.children[0].patientName,\"Back\\\\Slash\");");
      script.append("assertEquals(data.children[0].patientID,\"1.1\");");
      script.append("assertEquals(data.children[0].children[0].studyUID,\"1.2\");");

      System.out.println("Script="+script);
      engine.eval(script.toString());
   }
   
   // Utility methods / classes
   
   private StartElement createStartElement(String name)
   {
      return XMLEventMocker.createStartElement(name);
   }
   
   private Attribute createAttribute(String name, String value)
   {
      return XMLEventMocker.createAttribute(name, value);
   }
   
   private EndElement createEndElement(String name)
   {
      return XMLEventMocker.createEndElement(name);
   }
   
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
