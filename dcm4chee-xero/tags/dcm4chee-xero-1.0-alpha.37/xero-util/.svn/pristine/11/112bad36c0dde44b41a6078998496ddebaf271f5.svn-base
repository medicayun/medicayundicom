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
package org.dcm4chee.xero.metadata;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Thread;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class reads metadata from XML files such as Facelet files or other XML
 * tagged files (JSP would work as long as it is well-formed XML).  To use the XML metadata provider, 3 things
 * need to be done.  First, create a property or other file that can be found as a resource.  This will be used
 * as the base property file.  Second, include the XmlMetaDataProvider in your base metadata properties, something
 * like:
 * <pre>
 * metaDataProvider.model=${class:org.dcm4chee.xero.metadata.XmlMetaDataProvider}
 * metaDataProvider.model.priority=35
 * metaDataProvider.model.propertyFile=xero-view.metadata
 * metaDataProvider.model.rootXml=http://www.dcm4che.org/xero/metadata
 * metaDataProvider.model.suffix=.xml
 * </pre>
 * 
 * 
 * Thirdly, in your XML files contained in the same directory or a child directory, use attributes with the
 * namespace starting at rootXml, eg:
 * 
 * <pre>
 *   &lt;root xmlns:m="http://www.dcm4che.org/xero/metadata/view"
 *      m:script="script.js"
 *      m:script.priority="15" />
 * </pre>
 * 
 * The above declaration would declare view.script with value script.js and view.script.priority with value 15.
 * 
 * @author bwallace
 * 
 */
public class XmlMetaDataProvider implements MetaDataProvider, MetaDataUser {
	private static Logger log = Logger.getLogger(XmlMetaDataProvider.class);

	String rootXml;

	String suffix;
	String[] suffixes;
	
	String currentFile;

	Map<String, String> metaData;
	
	/** Used for parsing the XML files */
	SAXParser saxParser;

	/**
	 * This handler looks for namespace prefixed items to include.
	 */
	DefaultHandler defaultHandler = new DefaultHandler() {
		Map<String, String> prefixToNode = new HashMap<String, String>();

		/**
		 * Read the attributes from the given element, using their namespace
		 * as a parent meta-data location name, and their localName to extend the
		 * location under the parent meta-data.
		 * An example would be:<br />
		 * Attribute namespace: <code>http://metadata/a/b</code> <br />
		 * where the rootXml is http://metadata would provide a base name of
		 * <code>a.b</code>  Then, if the localname was c.d, with value 'v'
		 * then the final meta-data from this object would be:
		 * a.b.c.d=v
		 * This is added to the metaData variable.
		 * @param uri unused.
		 * @param localName unused
		 * @param qName unused
		 * @param attributes that are in the meta-data namespace are parsed
		 * and the localName is added to that to form a full name.
		 */
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			int len = attributes.getLength();
			for (int i = 0; i < len; i++) {
				String uriAt = attributes.getURI(i);
				if (uriAt.startsWith(rootXml)
						&& (uriAt.length() == rootXml.length() || uriAt
								.charAt(rootXml.length()) == '/')) {
					String prefixPath = prefixToNode.get(uriAt);
					if (prefixPath == null) {
						prefixPath = uriAt.substring(rootXml.length());
						prefixPath = prefixPath.replace('/', '.');
						if (prefixPath.startsWith("."))
							prefixPath = prefixPath.substring(1) + '.';
						prefixToNode.put(uriAt, prefixPath);
					}
					String key = prefixPath + attributes.getLocalName(i);
					String value = attributes.getValue(i);
					log.info("XML Metadata "+currentFile+": " + key + "=" + value);
					metaData.put(key, value);
				}
			}
		}

	};

	/**
	 * Gets the meta-data for the given path - in fact, returns all
	 * XML provided meta-data.
	 * @param path unused
	 * @param existingMetaData unused
	 * @return A map containing all the XML based meta-data from this given rootXml.
	 */
	public Map<String, ?> getMetaData(String path, MetaDataBean existingMetaData) {
		log.debug("Getting meta-data information from XML files for path "
				+ path + " existing meta-data path="
				+ existingMetaData.getPath());
		log.debug("rootXml=" + rootXml);
		return metaData;
	}

	/**
	 * Returns a File reference to the directory containing the propertyFile
	 * resource.
	 * 
	 * @param propertyFile is the propertyFile to return the parent directory for.
	 * @return File contianing propertyFile.
	 */
	static public File getFileContaining(String propertyFile) {
	  ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	  log.debug("Looking for " + propertyFile);
	  URL url = classLoader.getResource(propertyFile);
	  if (url == null) {
		 throw new IllegalArgumentException("Property file '" + propertyFile + "' not found.");
	  }
	  String base = url.toString();
	  if (base.startsWith("file:")) {
		 File file;
		 try {
			file = new File(url.toURI());
		 } catch (URISyntaxException e) {
			throw new RuntimeException(e);
		 }
		 assert file.exists();
		 File parent = file.getParentFile();
		 log.debug("Parent file=" + parent);
		 return parent;
	  } else {
		 throw new UnsupportedOperationException("Can only read meta data from un-packed file structures, got url " + base);
	  }
   }

	/**
	 * Recursively search through all directories, returning all elements
	 * matching fileFilter.
	 * 
	 * @param dir to start from.
	 * @param fileFilter to match items from dir
	 * @param files to add to all matching files to.
	 */
	static public void recursiveListFiles(File dir, FileFilter fileFilter,
			List<File> files) {
		log.debug("Looking into directory for files " + dir);
		File[] children = dir.listFiles();
		for (File child : children) {
			if (child.isDirectory()) {
				recursiveListFiles(child, fileFilter, files);
			} else if (fileFilter.accept(child)) {
				log.debug("Found file to parse:" + child);
				files.add(child);
			}
		}
	}

	/**
	 * Scans the XML file for properties. Uses the SAX parser to efficiently
	 * scan for properties.
	 * @param file to scan for matching XML meta-data.
	 */
	protected void scanFile(File f) {
		try {
			log.debug("Scanning "+f);
			currentFile = f.getName();
			if( saxParser==null ) {
				SAXParserFactory spf = SAXParserFactory.newInstance();
				spf.setNamespaceAware(true);
				spf.setValidating(false);
				saxParser = spf.newSAXParser();
			}
			saxParser.parse(f, defaultHandler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (SAXException e) {
			log.warn("Caught exception "+e+" on "+f);
		} catch (IOException e) {
			log.warn("Caught exception "+e+" on "+f);
		}
	}

	/**
	 * Uses the meta-data to figure out what directory to scan, based on the
	 * directory containin the meta-data property file
	 * @param metaDataBean contains information about what namespaces
	 * to search for xml files for, and what suffixes to use for them.
	 */
	public void setMetaData(MetaDataBean metaDataBean) {
		if (rootXml != null)
			return;
		try {
			String propertyFile = (String) metaDataBean
					.getValue("propertyFile");
			suffix = (String) metaDataBean.getValue("suffix");
			if (suffix == null) {
				suffixes = new String[]{ ".xhtml" };
			}
			else {
			   suffixes = suffix.split(",");
			}
			rootXml = (String) metaDataBean.getValue("rootXml");
			metaData = new HashMap<String, String>();
			File dir = getFileContaining(propertyFile);
			FileFilter fileFilter = new FileFilter() {
				public boolean accept(File pathname) {
				   for(int i=0; i<suffixes.length; i++) {
					  boolean b = pathname.toString().endsWith(suffixes[i]);
					  if( b ) return true;
				   }
					return false;
				}

			};
			List<File> files = new ArrayList<File>();
			recursiveListFiles(dir, fileFilter, files);
			log.info("Dir containing XML files to search is:" + dir
					+ " looking for suffix " + suffix + " found "+files.size() + " items.");
			for (File f : files) {
				scanFile(f);
			}
			saxParser = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
