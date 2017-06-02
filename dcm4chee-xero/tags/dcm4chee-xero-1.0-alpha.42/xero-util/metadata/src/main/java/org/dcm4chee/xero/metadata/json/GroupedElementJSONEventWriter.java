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
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Event writer that will store all incoming XML events then write them out
 * when the {@link #flush()} is invoked.  JSON script is built to mirror as
 * closely as possible the structure of the parsed XML tree in the Javascript
 * client.
 * <p>
 * Elements that are read from the originating XML event stream will be grouped into a 
 * 'children' node in the resulting JSON.  If there is sufficient type information in 
 * the XML events, then numeric fields will be stored as literals rather than strings.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class GroupedElementJSONEventWriter implements XMLEventWriter
{
   private static Logger log = LoggerFactory.getLogger(GroupedElementJSONEventWriter.class);
   
   private static final String QUOTE = "\"";
   private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
   private static final String INT_TYPE = "int";
   
   private final Writer writer;
   
   private Queue<XMLEvent> events = new LinkedList<XMLEvent>();
   private boolean isParent = false;
   
   private JavaScriptFormatter format = new JavaScriptFormatter();
   
   public GroupedElementJSONEventWriter(Writer writer)
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
      // Ignore all nodes from the XSI namespace.
      if(XSI_NS.equalsIgnoreCase(att.getName().getNamespaceURI()))
         return;
      
      String name = att.getName().getLocalPart();
      String value = att.getValue();

      // Numbers do not need to be 
      if(INT_TYPE.equalsIgnoreCase(att.getDTDType()))
         writeAttribute(name, value,false);
      else
         writeAttribute(name, value,true);
   }
   
   private void writeAttribute(String name, String value) throws XMLStreamException
   {
      writeAttribute(name,value,true);
   }
   
   private void writeAttribute(String name, String value, boolean quoteValue)  throws XMLStreamException
   {
      writeQuoted(name);
      write(":");
      
      String formattedValue = format.toString(value);
      if(quoteValue)
         writeQuoted(formattedValue);
      else
         write(formattedValue);
      
      XMLEvent next = events.peek();
      if(next!=null && (next.isAttribute() || next.isStartElement() || next.isCharacters()))
         write(",");
   }
   
   private void writeStartElement(StartElement element) throws XMLStreamException
   {
      // Are there intervening elements?
      if(isParent)
      {
         writeQuoted("children");
         write(":");
         write("[");
      }
      
      write("{");
      writeAttribute("tagName",element.getName().getLocalPart());

      this.isParent = isParent(element);
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
      case XMLEvent.CHARACTERS:
         writeCharacters(e.asCharacters());
         break;
      case XMLEvent.START_ELEMENT:
         writeStartElement(e.asStartElement());
         break;
      case XMLEvent.END_ELEMENT:
         writeEndElement(e.asEndElement());
         break;
      case XMLEvent.NAMESPACE:
      case XMLEvent.COMMENT:
      case XMLEvent.START_DOCUMENT:
      case XMLEvent.END_DOCUMENT:
         // Ignore.
         break;
      default:
         //throw new RuntimeException("Unknown event: "+e);
         log.warn("Unknown event type: "+e.getClass());
      }
   }

   /**
    * @param text Embedded text to write into JSON script.
    * @throws XMLStreamException 
    */
   private void writeCharacters(Characters text) throws XMLStreamException
   {
      // DO we need to care about whitespace here?
      writeAttribute("TEXT",text.getData());
   }


   /**
    * Determine if the indicated node is a parent of other elements.
    */
   private boolean isParent(StartElement element)
   {
      if(element == null)
         return true;
      
      String name = element.getName().getLocalPart();
      boolean isParent = false;

      for(XMLEvent e : events)
      {
         if(e.isStartElement())
         {
            // Embedded elements are the definition of being a parent.
            isParent =  true;
            break;
         }
         else if(e.isEndElement())
         {
            EndElement end = (EndElement)e;
            isParent = !end.getName().getLocalPart().equals(name);
            break;
         }
      }
      
      return isParent;
   }

   private void writeEndElement(EndElement e) throws XMLStreamException
   {
      write("}");
      
      XMLEvent next = events.peek();
      if(next != null && !next.isEndDocument())
         if(next.isEndElement())
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
