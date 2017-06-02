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

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event writer that will store all incoming XML events then write them out
 * when the {@link #flush()} is invoked.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class JSONXMLEventWriter implements XMLEventWriter
{
   private static Logger log = LoggerFactory.getLogger(JSONXMLEventWriter.class);
   
   private static final String QUOTE = "\"";
   
   private final Writer writer;
   
   private Queue<XMLEvent> events = new LinkedList<XMLEvent>();
   private StartElement previousElement = null;
   
   private JavaScriptFormatter format = new JavaScriptFormatter();
   
   public JSONXMLEventWriter(Writer writer)
   {
      if(writer == null)
         throw new IllegalArgumentException("Writer cannot be null");
      
      this.writer = writer;
   }
   
   
   /** 
    * Write out an attribute in JSON format: "name"="value"
    * @see javax.xml.stream.XMLStreamWriter#writeAttribute(java.lang.String, java.lang.String)
    */
   private void writeAttribute(Attribute att) throws XMLStreamException
   {
      String name = att.getName().getLocalPart();
      String value = att.getValue();

      writeQuoted(name);
      write(":");
      writeQuoted(format.toString(value));
         
      XMLEvent next = events.peek();
      if(next!=null && (next.isAttribute() || next.isStartElement()))
         write(",");
   }

   private void writeStartElement(StartElement element) throws XMLStreamException
   {
      String name = element.getName().getLocalPart();
      
      if(isNewElement(element))
      {
         writeQuoted(name);
         write(":");
         write("[");
      }
      
      write("{");
      
      previousElement = element;
   }
   
   
   private boolean isNewElement(StartElement element)
   {
      return previousElement == null || !previousElement.getName().equals(element.getName());
   }


   private void writeQuoted(String value) throws XMLStreamException
   {
      write(QUOTE);
      write(value);
      write(QUOTE);
   }

   
   
   // XMLEventWriter methods.

   /* (non-Javadoc)
    * @see javax.xml.stream.XMLEventWriter#add(javax.xml.stream.events.XMLEvent)
    */
   @Override
   public void add(XMLEvent event) throws XMLStreamException
   {
      events.add(event);
   }

   /* (non-Javadoc)
    * @see javax.xml.stream.XMLEventWriter#add(javax.xml.stream.XMLEventReader)
    */
   @Override
   public void add(XMLEventReader reader) throws XMLStreamException
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Close the underlying Writer.
    * @see javax.xml.stream.XMLStreamWriter#close()
    */
   public void close() throws XMLStreamException
   {
      try
      {
         writer.close();
      }
      catch (IOException e)
      {
         throw new XMLStreamException("Unable to close Writer",e);
      }
   }

   /**
    * Flush the underlying Writer.
    * @see javax.xml.stream.XMLStreamWriter#flush()
    */
   public void flush() throws XMLStreamException
   {
      try
      {
         XMLEvent e = null;
         while((e = events.poll()) != null)
         {
            writeEvent(e);
         }
         
         writer.flush();
      }
      catch (IOException e)
      {
         throw new XMLStreamException("Unable to flush Writer",e);
      }
   }

   private void writeEvent(XMLEvent e) throws XMLStreamException
   {
      switch(e.getEventType())
      {
      case XMLEvent.ATTRIBUTE:
         writeAttribute((Attribute)e);
         break;
      case XMLEvent.START_DOCUMENT:
         write("{");
         break;
      case XMLEvent.END_DOCUMENT:
         write("}");
         break;
      case XMLEvent.START_ELEMENT:
         writeStartElement((StartElement)e);
         break;
      case XMLEvent.END_ELEMENT:
         writeEndElement((EndElement)e);
         break;
      default:
         log.warn("Unknown event type: "+e.getClass());
      }
   }


   private void writeEndElement(EndElement e) throws XMLStreamException
   {
      write("}");
      
      XMLEvent next = events.peek();
      if(next != null)
         if(next.isEndElement() || next.isEndDocument())
            write("]");
         else
            write(",");
   }


   private void write(String str) 
      throws XMLStreamException
   {
      try
      {
         writer.write(str);
      }
      catch (IOException e)
      {
         throw new XMLStreamException(e);
      }
   }

   /**
    * Ignored
    * @see javax.xml.stream.XMLEventWriter#getNamespaceContext()
    */
   @Override
   public NamespaceContext getNamespaceContext()
   {
      return null;
   }

   /**
    * Ignored
    * @see javax.xml.stream.XMLEventWriter#getPrefix(java.lang.String)
    */
   @Override
   public String getPrefix(String uri) throws XMLStreamException
   {
      return null;
   }

   /**
    * Ignored
    * @see javax.xml.stream.XMLEventWriter#setDefaultNamespace(java.lang.String)
    */
   @Override
   public void setDefaultNamespace(String uri) throws XMLStreamException  
   {
   }

   /**
    * Ignored
    * @see javax.xml.stream.XMLEventWriter#setNamespaceContext(javax.xml.namespace.NamespaceContext)
    */
   @Override
   public void setNamespaceContext(NamespaceContext context) throws XMLStreamException  
   { 
   }

   /**
    * Ignored
    * @see javax.xml.stream.XMLEventWriter#setPrefix(java.lang.String, java.lang.String)
    */
   @Override
   public void setPrefix(String prefix, String uri) throws XMLStreamException
   { 
   }

}
