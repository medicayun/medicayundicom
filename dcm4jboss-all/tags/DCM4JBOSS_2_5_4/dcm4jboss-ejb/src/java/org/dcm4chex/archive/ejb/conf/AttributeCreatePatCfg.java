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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.archive.ejb.conf;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Configuration class for patient creation.
 * <p>
 * 
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
 * @since 30.05.2005
 */
public final class AttributeCreatePatCfg {

	private boolean enableCreatePatID = false;
	private String pattern = null;
	private String issuer = null;
	
	private boolean doubletPrevention = false;

	/**
	 * SAX Handler to read configuration from xml.
	 * @author franz.willer
	 *
	 */
    private class MyHandler extends DefaultHandler {
    	private StringBuffer sb = new StringBuffer();
        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
        	sb.setLength(0);
        	if ( qName.equals("generatePatID") ) {
        		enableCreatePatID = isTrue( attributes.getValue("enable") );
        	} else if ( qName.equals("doubletPrevention") ) {
        		doubletPrevention = isTrue( attributes.getValue("enable") );
        	}
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
        	sb.append(ch, start, length);
	    }
        
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
        	if (qName.equals("pattern")) {
	            pattern = sb.toString();
            } else if (qName.equals("issuer")) {
                issuer = sb.toString();
            }
        }
        
		public void endDocument() throws SAXException {
		}
		
		private boolean isTrue( String b ) {
			if ( b == null ) return false;
    		b = b.trim().toLowerCase();
        	return ( b.equals("yes") || b.equals("true") || b.equals("0") );
		}
    }

    /**
     * Creates an AttributeCreatePatCfg object and read the config file given by <code>uri</code>.
     * 
     * @param uri
     * @throws ConfigurationException
     */
    public AttributeCreatePatCfg(String uri) throws ConfigurationException {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(
                uri,
                new MyHandler());
        } catch (Exception e) {
            throw new ConfigurationException(
                "Failed to load createPatientConfig from " + uri,
                e);
        }
    }

    /**
     * Returns true if the creation of new patient ID is enabled.
     * <p>
     * The creation of a patient ID is only necessary if this attribute is missing in a C-Store request.
     * 
     * @return true if ID creatuion is enabled.
     */
    public final boolean isCreatePatIDEnabled() {
    	return enableCreatePatID;
    }
    
    /**
     * Retruns the ID 'pattern' used to format the patient ID.
     * 
     * @return The pattern to format patientID
     */
    public final String getPattern() {
        return pattern;
    }

    /**
     * Returns the issuer of the created patient ID.
     * 
     * @return the issuer of the created patientID.
     */
    public final String getIssuer() {
        return issuer;
    }

    /**
     * Returns true if doubletPrevention is enabled.
     * <p>
     * DoubletPrevention means, to inhibit the creation of two patients with equal name and birthdate.
     * <p>
     * This rule is also only useful if the patientID is missing!
     * 
     * @return
     */
    public final boolean isDoubletPreventionEnabled() {
        return doubletPrevention;
    }
}
