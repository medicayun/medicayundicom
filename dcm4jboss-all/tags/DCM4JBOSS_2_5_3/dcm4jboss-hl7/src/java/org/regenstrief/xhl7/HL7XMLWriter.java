/*  HL7XMLWriter - an HL7 v2 builder that produces traditionally
 *                 encoded HL7 messages from XML/SAX events.
 *
 *  Copyright (C) 2002, 2003 Regenstrief Institute. All rights reserved.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Written and maintained by Gunther Schadow <gschadow@regenstrief.org>
 *  Regenstrief Institute for Health Care
 *  1050 Wishard Blvd., Indianapolis, IN 46202, USA.
 *
 * $Id: HL7XMLWriter.java 1716 2005-04-24 14:08:24Z gunterze $
 */
package org.regenstrief.xhl7;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/** A SAX ContentHandler that produces traditionally encoded HL7
    messages from XML/SAX events. Message is sent to the OutputStream
    set for the Writer.

    <p>This ContentHandler is also its own facotry.

    @author Gunther Schadow
    @version $Id: HL7XMLWriter.java 1716 2005-04-24 14:08:24Z gunterze $ 
*/
public class HL7XMLWriter implements ContentHandler, HL7XMLLiterate, XMLWriter
{  
  private Writer _output = new OutputStreamWriter(System.out);
  private char _delimiters[] = DEFAULT_DELIMITERS.toCharArray();

  /** Default constructor used when instantiated by reflection. */
  public HL7XMLWriter() { }

  /** Convenient constructor that calls setOutputStream. 
      
      @param outputStream where the serialized HL7 goes.
   */
  public HL7XMLWriter(OutputStream outputStream) {
    setOutputStream(outputStream);
  }

  /** Set the output stream destination.

      @param outputStream where the serialized HL7 goes.
   */
  public void setOutputStream(OutputStream outputStream) {
    try {
		this._output = new OutputStreamWriter(outputStream, "ISO-8859-1");
	} catch (UnsupportedEncodingException e) {
		throw new RuntimeException(e);
	}
  }

  /** Get the content handler. This HL7XMLWriter is itself a 
      content handler, so it returns itself. But the XMLWriter interface
      is written such that the content handler could be a helper 
      object.

      <p>If no outputStream was set previously, an output stream to 
      standard out is used instead.

      @return itself as a content handler. 
  */
  public ContentHandler getContentHandler() {
    return this;
  }

  /** For the ContentHandler interface. */
  public void characters(char[] ch, int start, int length) 
    throws SAXException
  {
    try {
      int nextWritePosition = start;
      int endPosition = start + length;
    main_loop:
      while(nextWritePosition < endPosition) {
	int unescapeableRegionPosition = nextWritePosition;
	while(unescapeableRegionPosition < endPosition) {
	  char c = ch[unescapeableRegionPosition++];
	  for(int i = 0; i < NUMBER_OF_DELIMITERS; i++) {
	    if(c == this._delimiters[i]) {
	      int writeLength 
		= unescapeableRegionPosition - 1 - nextWritePosition;
	      if(writeLength > 0) {
		this._output.write(ch, nextWritePosition, writeLength);
	      }
	      this._output.write(this._delimiters[N_DEL_ESCAPE]);
	      this._output.write(DELIMITER_ESCAPES.charAt(i));
	      this._output.write(this._delimiters[N_DEL_ESCAPE]);	      
	      nextWritePosition = unescapeableRegionPosition;
	      continue main_loop;
	    }
	  }
	  if(c=='\r' || c=='\n') {
	    int writeLength 
	      = unescapeableRegionPosition - 1 - nextWritePosition;
	    if(unescapeableRegionPosition < endPosition) {
	      char nextc = ch[unescapeableRegionPosition];
	      if(c=='\r' && nextc=='\n')
		unescapeableRegionPosition++;
	    }
	    if(writeLength > 0) {
	      this._output.write(ch, nextWritePosition, writeLength);
	    }
	    this._output.write(this._delimiters[N_DEL_ESCAPE]);
	    this._output.write(".br");
	    this._output.write(this._delimiters[N_DEL_ESCAPE]);	      
	    nextWritePosition = unescapeableRegionPosition;
	    continue main_loop;
	  }
	}
	int writeLength = unescapeableRegionPosition - nextWritePosition;
	if(writeLength > 0)
	  this._output.write(ch, nextWritePosition, writeLength);	
	nextWritePosition = unescapeableRegionPosition;	
      }
    } catch(Exception ex) {
      throw new SAXException(ex);
    }
  }
  
  /** For the ContentHandler interface. */
  public void startElement(String namespaceURI, String localName, 
			   String qName, Attributes atts)  
    throws SAXException
  {
    try {
      localName = localName.intern();
      if(localName == TAG_FIELD) {
	this._output.write(this._delimiters[N_DEL_FIELD]);      
      } else if(localName == TAG_REPEAT) {
	this._output.write(this._delimiters[N_DEL_REPEAT]);      
      } else if(localName == TAG_COMPONENT) {
	this._output.write(this._delimiters[N_DEL_COMPONENT]);      
      } else if(localName == TAG_SUBCOMPONENT) {
	this._output.write(this._delimiters[N_DEL_SUBCOMPONENT]);      
      } else if(localName == TAG_ESCAPE) {
	this._output.write(this._delimiters[N_DEL_ESCAPE]);      
      } else if(localName == "FHS" 
		|| localName == "BHS" 
		|| localName == "MSH") {
	String fieldDelimiter = atts.getValue(ATT_DEL_FIELD);
	if(fieldDelimiter != null)
	  this._delimiters[N_DEL_FIELD] = fieldDelimiter.charAt(0);
	String repeatDelimiter = atts.getValue(ATT_DEL_REPEAT);
	if(repeatDelimiter != null)
	  this._delimiters[N_DEL_REPEAT] = repeatDelimiter.charAt(0);
	String componentDelimiter = atts.getValue(ATT_DEL_COMPONENT);
	if(componentDelimiter != null)
	  this._delimiters[N_DEL_COMPONENT] = componentDelimiter.charAt(0);
	String subcomponentDelimiter = atts.getValue(ATT_DEL_SUBCOMPONENT);
	if(subcomponentDelimiter != null)
	  this._delimiters[N_DEL_SUBCOMPONENT] 
	    = subcomponentDelimiter.charAt(0);
	String escapeDelimiter = atts.getValue(ATT_DEL_ESCAPE);
	if(escapeDelimiter != null)
	  this._delimiters[N_DEL_ESCAPE] = escapeDelimiter.charAt(0);
	this._output.write(localName);
	this._output.write(this._delimiters);
      } else if(localName == TAG_ROOT) {
      } else {
	this._output.write(localName);
      }
    } catch(Exception ex) {
      throw new SAXException(ex);
    }
  }

  /** For the ContentHandler interface. */
  public void endElement(String namespaceURI, 
			 String localName, String qName) 
    throws SAXException
  {
    try {
      localName = localName.intern();
      if(localName == TAG_FIELD) {
      } else if(localName == TAG_REPEAT) {
      } else if(localName == TAG_COMPONENT) {
      } else if(localName == TAG_SUBCOMPONENT) {
      } else if(localName == TAG_ESCAPE) {
	this._output.write(this._delimiters[N_DEL_ESCAPE]);      
      } else if(localName == TAG_ROOT) {
      } else {
	this._output.write('\r');
	this._output.flush();
      }
    } catch(Exception ex) {
      throw new SAXException(ex);
    }
  }

  /** For the ContentHandler interface - a no-op. */
  public void startDocument() {}
  /** For the ContentHandler interface - a no-op. */
  public void endDocument() { }    
  /** For the ContentHandler interface - a no-op. */
  public void startPrefixMapping(String prefix, String uri) {}
  /** For the ContentHandler interface - a no-op. */
  public void endPrefixMapping(String prefix) {}
  /** For the ContentHandler interface - a no-op. */
  public void ignorableWhitespace(char[] ch, int start, int length) {}
  /** For the ContentHandler interface - a no-op. */
  public void processingInstruction(String target, String data) {}
  /** For the ContentHandler interface - a no-op. */
  public void setDocumentLocator(org.xml.sax.Locator locator) {}
  /** For the ContentHandler interface - a no-op. */
  public void skippedEntity(String name) {}
}
