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

package org.dcm4chee.arr.ejb;

import java.util.Date;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4chee.arr.util.AuditMessageUtils;
import org.dcm4chee.arr.util.XSLTUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                propertyValue = "queue/ARRReceiver"),
        @ActivationConfigProperty(
                propertyName = "maxSession",
                propertyValue = "1") })
public class ReceiverMDB implements MessageListener {

    private static Logger log = LoggerFactory.getLogger(ReceiverMDB.class);
    
    @PersistenceContext(unitName="dcm4chee-arr")
    private EntityManager em;
    private XMLReader xmlReader;

    public void onMessage(Message msg) {
    	if(!(msg instanceof BytesMessage)) {
    		log.warn("Ignore unsupported message: " + msg);
    		return;
    	}
    	
        byte[] xmldata;
        try {
            BytesMessage bytesMessage = (BytesMessage) msg;
            xmldata = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(xmldata);
        } catch (Throwable e) {
            log.error("Failed processing " + msg, e);
            return;
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Start processing {}", 
                        AuditMessageUtils.promptMsg(xmldata));
            }            
            AuditRecord rec = new AuditRecord();
            rec.setReceiveDateTime(new Date(msg.getJMSTimestamp()));
            rec.setIHEYr4(AuditMessageUtils.isIHEYr4(xmldata));
            rec.setXmldata(xmldata);
            if (xmlReader == null) {
        	xmlReader = XMLReaderFactory.createXMLReader();
	    }
            DefaultHandler dh = new AuditRecordHandler(em, rec);
            if (rec.isIHEYr4()) {
        	XSLTUtils.parseIHEYr4(xmlReader, dh, xmldata);
            } else {
        	XSLTUtils.parseATNA(xmlReader, dh, xmldata);
            }
            em.persist(rec);
            if (log.isDebugEnabled()) {
                log.debug("Finished processing {}", 
                        AuditMessageUtils.promptMsg(xmldata));
            }            
        } catch (Throwable e) {
            log.error("Failed processing " +
                    AuditMessageUtils.promptMsg(xmldata), e);
        }
     }
}
