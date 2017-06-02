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
 * Portions created by the Initial Developer are Copyright (C) 2007
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
package org.dcm4chee.xero.metadata.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.json.GroupedElementJSONEventWriter;
import org.dcm4chee.xero.metadata.json.JSONPrettyPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class knows how to encode a single item as XML, based on the pre-condition
 * that the underlying item is configured to allow Jaxb.  This class doesn't do any
 * creation or filtering of the item, it merely serializes it as a response, with the
 * correct XML content type etc.
 * This filter always runs - there isn't any check on content type etc to cause it to
 * run or not, unless the provided value is null, or it isn't serializeable.
 * @author bwallace
 *
 */
public class JaxbFilter implements Filter<ServletResponseItem>
{
	static Logger log = LoggerFactory.getLogger(JaxbFilter.class);
	
	Filter<?> sourceFilter;
   private JAXBProvider provider;
	
	/**
	 * This class holds the filtered response item until it is time to be serialized
	 * 
	 * @author bwallace
	 *
	 */
	static class JaxbServletResponseItem implements ServletResponseItem {
		Object data;
		JAXBProvider provider;
		
		/** Hold the given data item, and JAXB context until serialization occurs. */
		JaxbServletResponseItem(Object data, JAXBProvider provider)
		{
			this.data = data;
			this.provider = provider;
		}
	   
		/** Actually write the XML out to the response.
		 * @param response to write teh XML to.
		 * @param request is ignored.
		 */
		public void writeResponse(HttpServletRequest request, HttpServletResponse response) {
			try {
				Marshaller m = provider.createMarshaller();
				response.setContentType("text/xml");
				String xml="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
				String pretty="<?xml-stylesheet type=\"text/xsl\" href=\"/wado2/pretty.xsl\"?>\n";
				OutputStream os = response.getOutputStream();
				os.write(xml.getBytes());
				os.write(pretty.getBytes());
				m.marshal(data, os);			
			} catch (JAXBException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Output JSON to clients using the indicated JAXBProvider.
	 * @author Andrew Cowan (andrew.cowan@agfa.com)
	 */
	static class JSONServletResponseItem implements ServletResponseItem
	{
	   private final Object data;
      private final JAXBProvider provider;
      private final boolean prettyPrint;

      JSONServletResponseItem(Object data, JAXBProvider provider, boolean prettyPrint)
      {
         this.data = data;
         this.provider = provider;
         this.prettyPrint = prettyPrint;
      }
      
      public void writeResponse(HttpServletRequest request, HttpServletResponse response) throws IOException
      {
         try {
            Marshaller m = provider.createMarshaller();
            response.setContentType("text/javascript");
            
            Writer writer = response.getWriter();
            if(prettyPrint)
               writer = new JSONPrettyPrinter(writer);
            
            GroupedElementJSONEventWriter jsonWriter = new GroupedElementJSONEventWriter(writer);
            m.marshal(data, jsonWriter);
         } catch (JAXBException e) {
            throw new RuntimeException(e);
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
	}
	

	/** Convert the object returned by the source filter (if any) into a servlet response item that 
	 * serializes it as XML.
	 * @param nextFilter is called to get the item to convert to XML.
	 * @param params are just passed down the filter chain.
	 * @return a servlet response that can be used to actually write the XML data.
	 */
	public ServletResponseItem filter(FilterItem<ServletResponseItem> nextFilter, Map<String, Object> params) {
		Object data = sourceFilter.filter(null, params);
		if( data==null ) return nextFilter.callNextFilter(params);
		
		String contentType = FilterUtil.getString(params, "contentType");
		boolean pretty = FilterUtil.getBoolean(params, "pretty", false);
		
		if(contentType == null || contentType.equalsIgnoreCase("text/xml"))
		return new JaxbServletResponseItem(data,provider);
		else if(contentType.contains("javascript"))
		   return new JSONServletResponseItem(data,provider,pretty);
		else
		   throw new RuntimeException("Unknown content type '"+contentType+"'");
	}

	/** Returns the source filter used to get the object data to render */
	public Filter<?> getSourceFilter() {
		return this.sourceFilter;
	}
	
	/**
	 * Sets the filter to use to return the object data. 
	 * @param sourceFilter
	 */
	@MetaData
	public void setSourceFilter(Filter<?> sourceFilter) {
		this.sourceFilter = sourceFilter;
	}
	
	@MetaData
	public void setContextPath(String contextPath) {
		try {
			this.provider = new JAXBProvider(contextPath);
		}
		catch(JAXBException e) {
			log.error("Could not find context "+contextPath+" caught exception "+e);
		}
	}

	/**
	 * Gets the default priority of this item. 
	 */
	@MetaData
	public static int getPriority() {
	   return 999999;
	}
}
