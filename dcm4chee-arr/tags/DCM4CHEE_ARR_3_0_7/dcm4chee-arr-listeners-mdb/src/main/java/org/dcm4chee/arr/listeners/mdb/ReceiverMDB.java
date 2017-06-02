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
 * The Initial Developer of the Original Code is Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
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

package org.dcm4chee.arr.listeners.mdb;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.dcm4chee.arr.entities.AuditRecord;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since May 24, 2006
 * 
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(
                propertyName = "destinationType",
                propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(
                propertyName = "destination",
                propertyValue = "queue/ARRIncoming"),
        @ActivationConfigProperty(
                propertyName = "maxSession",
                propertyValue = "1") })
public class ReceiverMDB implements MessageListener {

    private static final Logger log = Logger.getLogger(ReceiverMDB.class);

    private static final int MSG_PROMPT_LEN = 200;
    
    private static final String IHEYR4_TO_ATNA_XSL = "arr-iheyr4-to-atna.xsl";

    private static Templates iheYr4toATNATpl;
    
    @PersistenceContext(unitName="dcm4chee-arr")
    private EntityManager em;
    private XMLReader xmlReader;
    private SAXTransformerFactory tf;
//    private XMLFilter xmlFilter;

    public void onMessage(Message msg) {
    	if(!(msg instanceof BytesMessage)) {
    		log.warn("Ignore unsupported " + msg.getClass().getName());
    		return;
    	}
        try {
            BytesMessage bytesMessage = (BytesMessage) msg;
            Date receiveDate = new Date(msg.getJMSTimestamp());
            int bodyLength = (int) bytesMessage.getBodyLength();
            byte[] xmldata = new byte[bodyLength];
            bytesMessage.readBytes(xmldata, bodyLength);
            process(xmldata, receiveDate);
        } catch (Throwable e) {
            log.error("Failed processing Byte Message", e);
            return;
        }
     }

    private void process(byte[] xmldata, Date receiveDate) {
        try {
            if (log.isDebugEnabled()) {
                 log.debug("Start processing - " + prompt(xmldata));
            }            
            boolean iheyr4 = isIHEYr4(xmldata);
            XMLReader reader = iheyr4 ? xmlFilter() : xmlReader();
            AuditRecord rec = new AuditRecord();
            DefaultHandler dh = new AuditRecordHandler(em, rec);
            reader.setContentHandler(dh);
            reader.setEntityResolver(dh);
            reader.setErrorHandler(dh);
            reader.setDTDHandler(dh);
            reader.parse(new InputSource(new ByteArrayInputStream(xmldata)));
            rec.setReceiveDateTime(receiveDate);
            rec.setIHEYr4(iheyr4);
            rec.setXmldata(xmldata);
            em.persist(rec);
            if (log.isDebugEnabled()) {
                log.debug("Finished processing - " + prompt(xmldata));
            }            
        } catch (Throwable e) {
            log.error("Failed processing - " + prompt(xmldata), e);
        }
    }

    private static String prompt(byte[] data) {
        try {
            return data.length > MSG_PROMPT_LEN
                    ? (new String(data, 0, MSG_PROMPT_LEN, "UTF-8") + "...") 
                    : new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    private XMLReader xmlReader() throws SAXException {
        if (xmlReader == null) {
            xmlReader = XMLReaderFactory.createXMLReader();
        }
        return xmlReader;
    }

    private XMLFilter xmlFilter() throws TransformerConfigurationException {
        if (tf == null) {
            tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        }
        if (iheYr4toATNATpl == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            iheYr4toATNATpl = tf.newTemplates(new StreamSource(
                    cl.getResource(IHEYR4_TO_ATNA_XSL).toString()));
        }
        return tf.newXMLFilter(iheYr4toATNATpl);
    }

    private static boolean isIHEYr4(byte[] xmldata) {
        int i = 0;
        while (xmldata[i++] != '<');
        if (xmldata[i] == '?') { // skip <?xml version="1.0" ..?>
            while (xmldata[i++] != '<');
        }
        return xmldata[i] == 'I';
    }

}
