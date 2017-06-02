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

import static org.easymock.EasyMock.*;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class XMLEventMocker
{
   private static <T extends XMLEvent> T createXMLEvent(Class<T> typeToMock, int eventType)
   {
      T e = createNiceMock(typeToMock);
      expect(e.getEventType()).andStubReturn(eventType);
      return e;
   }
   
   public static StartElement createStartElement(String name)
   {
      QName qName = new QName(name);
      StartElement s = createXMLEvent(StartElement.class, XMLEvent.START_ELEMENT);
      expect(s.getName()).andStubReturn(qName);
      expect(s.getEventType()).andStubReturn(XMLEvent.START_ELEMENT);
      expect(s.asStartElement()).andStubReturn(s);
      expect(s.isStartElement()).andStubReturn(true);
      replay(s);
      return s;
   }
   
   public static StartDocument createStartDocument()
   {
      StartDocument d = createXMLEvent(StartDocument.class, XMLEvent.START_DOCUMENT);
      expect(d.isStartDocument()).andStubReturn(true);
      replay(d);
      return d;
   }
   
   public static EndDocument createEndDocument()
   {
      EndDocument d = createXMLEvent(EndDocument.class, XMLEvent.END_DOCUMENT);
      expect(d.isEndDocument()).andStubReturn(true);
      replay(d);
      return d;
   }
   
   public static EndElement createEndElement(String name)
   {
      QName qName = new QName(name);
      EndElement d = createXMLEvent(EndElement.class, XMLEvent.END_ELEMENT);
      expect(d.isEndElement()).andStubReturn(true);
      expect(d.asEndElement()).andStubReturn(d);
      expect(d.getName()).andStubReturn(qName);
      replay(d);
      return d;
   }
   
   
   public static EndElement createEndElement()
   {
      return createEndElement("");
   }
   
   public static Attribute createAttribute(String name, String value)
   {
      return createAttribute("", "", name, value, "string");
   }

   public static Attribute createAttribute(String prefix, String namespace, String name, String value, String type)
   {
      QName qName = new QName(namespace,name,prefix);
      Attribute att  = createXMLEvent(Attribute.class,XMLEvent.ATTRIBUTE);
      expect(att.getName()).andStubReturn(qName);
      expect(att.getValue()).andStubReturn(value);
      expect(att.getDTDType()).andStubReturn(type);
      expect(att.isAttribute()).andStubReturn(true);
      replay(att);
      return att;
   }

   public static Characters createCharacters(String text)
   {
      Characters c = createXMLEvent(Characters.class, XMLEvent.CHARACTERS);
      expect(c.getData()).andStubReturn(text);
      expect(c.isCharacters()).andStubReturn(true);
      expect(c.asCharacters()).andStubReturn(c);
      replay(c);
      return c;
   }

}
