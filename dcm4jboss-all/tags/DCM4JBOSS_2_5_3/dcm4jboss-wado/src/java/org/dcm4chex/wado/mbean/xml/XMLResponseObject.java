/*
 * Created on 13.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.wado.mbean.xml;

import java.io.OutputStream;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface XMLResponseObject {
	void toXML( OutputStream out ) throws TransformerConfigurationException, SAXException;
	void embedXML( TransformerHandler th ) throws TransformerConfigurationException, SAXException;
}
