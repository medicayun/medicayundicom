/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
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
 * @version $Revision: 1768 $ $Date: 2005-06-02 17:16:43 +0800 (周四, 02 6月 2005) $
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
