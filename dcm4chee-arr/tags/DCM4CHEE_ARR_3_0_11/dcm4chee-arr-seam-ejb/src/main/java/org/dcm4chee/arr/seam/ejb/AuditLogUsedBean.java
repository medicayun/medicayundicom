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
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

package org.dcm4chee.arr.seam.ejb;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.Principal;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.servlet.http.HttpServletRequest;

import org.dcm4che2.audit.message.AuditLogUsedMessage;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.AuditEvent.OutcomeIndicator;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date:: xxxx-xx-xx $
 * @since Feb 10, 2010
 */
@Stateless
public class AuditLogUsedBean implements AuditLogUsedLocal {

    @Logger
    private transient Log log; 

    @Resource (mappedName="ConnectionFactory")
    private transient QueueConnectionFactory connFactory;

    @Resource (mappedName="queue/ARRIncoming")
    private transient Queue queue;

    public void log(HttpServletRequest rq, OutcomeIndicator outcome) {
        try {
            QueueConnection conn = connFactory.createQueueConnection();
            try {
                QueueSession jmsSession = conn.createQueueSession(false, 
                        QueueSession.AUTO_ACKNOWLEDGE);
                QueueSender sender = jmsSession.createSender(queue);
                final BytesMessage msg = jmsSession.createBytesMessage();
                msg.setStringProperty("sourceHostAddress", 
                        AuditMessage.getLocalHost().getHostAddress());
                msg.setStringProperty("sourceHostName", 
                        AuditMessage.getLocalHostName());
                AuditLogUsedMessage auditLogUsed = new AuditLogUsedMessage();
                auditLogUsed.getAuditEvent().setOutcomeIndicator(outcome);
                auditLogUsed.addUserPerson(
                        userID(rq),
                        null, null, rq.getRemoteHost());
                StringBuffer url = rq.getRequestURL();
                auditLogUsed.addSecurityAuditLog(
                        url.substring(0, url.lastIndexOf("/")));
                OutputStreamWriter out = new OutputStreamWriter(
                        new OutputStream() {
    
                            @Override
                            public void write(byte[] b, int off, int len)
                            throws IOException {
                                try {
                                    msg.writeBytes(b, off, len);
                                } catch (JMSException e) {
                                    throw new IOException(e.getMessage());
                                }
                            }
    
                            @Override
                            public void write(int b) throws IOException {
                                try {
                                    msg.writeByte((byte) b);
                                } catch (JMSException e) {
                                    throw new IOException(e.getMessage());
                                }
                            }
                        },
                        "UTF-8");
                auditLogUsed.output(out);
                out.flush();
                sender.send(msg);
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            log.warn("Failed to send Audit Log Used message", e);
        }
    }

    private String userID(HttpServletRequest rq) {
        Principal user = rq.getUserPrincipal();
        return user != null ? user.getName() : "UNKOWN_USER";
    }

}
