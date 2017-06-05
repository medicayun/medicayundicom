/*
 * Created on 16.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.xml;

import java.util.Iterator;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XMLUtil {
	javax.swing.AbstractButton v;
	public static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
	
    private TransformerHandler th = null;
    
    private String nameSpacePrefix = null;
	
    public XMLUtil( TransformerHandler handle ) {
    	th = handle;
    }

    public XMLUtil( TransformerHandler handle, String nameSpacePrefix ) {
    	th = handle;
    	setNameSpace( nameSpacePrefix );
    }
    
    public void setNameSpace( String ns ) {
    	if ( ns != null && ! ns.endsWith( ":" ) ) ns = ns + ":";
    	nameSpacePrefix = ns;
    }

    public void startElement( String name, Attributes attr ) throws SAXException {
    	if ( nameSpacePrefix != null ) name = nameSpacePrefix + name; 
    	if ( attr == null ) attr = XMLUtil.EMPTY_ATTRIBUTES;
	    th.startElement("", name, name, attr );
	}
    
    public void startElement( String name, String attrName, String attrValue ) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        addAttribute( attr, attrName, attrValue );
        startElement( name, attr );
    }
    
    public void endElement( String name ) throws SAXException {
    	if ( nameSpacePrefix != null ) name = nameSpacePrefix + name; 
    	th.endElement("", name, name );
	}
    
    public void singleElement( String name, Attributes attr, String value ) throws SAXException {
    	startElement( name, attr );
    	addValue( value );
    	endElement( name );
    }
    
    public AttributesImpl newAttribute( String name, String value ) {
        AttributesImpl attr = new AttributesImpl();
        addAttribute( attr, name, value );
    	return attr;
    }
	
    public void addAttribute( AttributesImpl attr, String name, String value ) {
		if ( value == null ) return;
		attr.addAttribute("", name, name, "", value);		
	}

    public void addAttributes( AttributesImpl attr, Properties props ) {
     if ( props != null ) {
     	Iterator iter = props.keySet().iterator();
     	String key;
     	while ( iter.hasNext() ) {
     		key = iter.next().toString();
     		addAttribute( attr, key, props.getProperty( key ) );
     	}
     }
	}
	
    public void addValue( String value ) throws SAXException {
    	if ( value != null )
    		th.characters(value.toCharArray(), 0, value.length() );
	}

}
