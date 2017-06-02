/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.server;

import org.dcm4che.hl7.HL7Exception;
import org.dcm4che.hl7.HL7Factory;
import org.dcm4che.hl7.HL7Service;
import org.dcm4che.hl7.HL7Message;
import org.dcm4che.hl7.MSHSegment;
import org.dcm4che.server.HL7Handler;
import org.dcm4che.util.MLLPInputStream;
import org.dcm4che.util.MLLPOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 3565 $ $Date: 2002-09-07 23:25:04 +0800 (周六, 07 9月 2002) $
 * @since August 11, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class HL7HandlerImpl implements HL7Handler {
    
    // Constants -----------------------------------------------------
    static final Logger log = Logger.getLogger(HL7HandlerImpl.class);
    
    // Variables -----------------------------------------------------
    private static final HL7Factory hl7Fact = HL7Factory.getInstance();
    
    private int soTimeout = 0;
    
    private HashSet receivingApps = null;
    
    private HashSet sendingApps = null;
    
    private HashMap hl7Services = new HashMap();
    
    // Constructors --------------------------------------------------
    
    // Methods -------------------------------------------------------
    
    public int getSoTimeout() {
        return soTimeout;
    }
        
    public void setSoTimeout(int timeout) {
        this.soTimeout = timeout;
    }
    
    public boolean addReceivingApp(String app) {
        if (receivingApps == null)
            receivingApps = new HashSet();
        
        return receivingApps.add(app);
    }
    
    public boolean addSendingApp(String app) {
        if (sendingApps == null)
            sendingApps = new HashSet();
        
        return sendingApps.add(app);
    }
    
    public String[] getReceivingApps() {
        return receivingApps != null
            ? (String[])receivingApps.toArray(new String[receivingApps.size()])
            : null;
    }
    
    public String[] getSendingApps() {
        return sendingApps != null
            ? (String[])sendingApps.toArray(new String[sendingApps.size()])
            : null;
    }
    
    public boolean removeReceivingApp(String app) {
        return receivingApps != null && receivingApps.remove(app);
    }
    
    public boolean removeSendingApp(String app) {
        return sendingApps != null && sendingApps.remove(app);
    }
    
    public void setReceivingApps(String[] apps) {
        receivingApps = apps != null
            ? new HashSet(Arrays.asList(apps))
            : null;
    }
    
    public void setSendingApps(String[] apps) {
        sendingApps = apps != null
            ? new HashSet(Arrays.asList(apps))
            : null;
    }
    
    public HL7Service putService(String msgTypeEvent,
            HL7Service service) {
        if (service != null) {
            return (HL7Service) hl7Services.put(msgTypeEvent, service);
        } else {
            return (HL7Service) hl7Services.remove(msgTypeEvent);
        }
    }

    private String toKey(String msgType, String trEvent) {
        StringBuffer sb =
            new StringBuffer(msgType.length() + trEvent.length() + 1);
        sb.append(msgType).append('^').append(trEvent);
        return sb.toString();
    }
    
    // Server.Handler -------------------------------------------
    public void handle(Socket s) throws IOException {
        s.setSoTimeout(soTimeout);
        MLLPInputStream in = new MLLPInputStream(
            new BufferedInputStream(s.getInputStream()));
        MLLPOutputStream out = new MLLPOutputStream(
            new BufferedOutputStream(s.getOutputStream()));
        try {
            byte[] data;
            while ((data = in.readMessage()) != null) {
                HL7Message msg = hl7Fact.parse(data);
                log.info("RCV: " + msg);
                byte[] res = execute(msg.header(), data);
                log.info("SND: " + hl7Fact.parse(res));
                out.writeMessage(res);
                out.flush();
            }
        } catch (HL7Exception e) {
            log.error("Could not understand: ", e);
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            try { out.close(); } catch (IOException ignore) {}
            try { s.close(); } catch (IOException ignore) {}
        }
    }
    
    public byte[] execute(MSHSegment msh, byte[] data) {
        try {
            if (receivingApps != null && !receivingApps.contains(
                    msh.getReceivingApplication())) {
                throw new HL7Exception.AR(
                    "Unrecognized Receiving Application: "
                    + msh.getReceivingApplication());   
            }
            if (sendingApps != null && !sendingApps.contains(
                    msh.getSendingApplication())) {
                throw new HL7Exception.AR(
                    "Unrecognized Sending Application: "
                    + msh.getSendingApplication());   
            }
            HL7Service service = (HL7Service) hl7Services.get(
                toKey(msh.getMessageType(), msh.getTriggerEvent()));
            if (service == null) {
                service = (HL7Service) hl7Services.get(msh.getMessageType());
            }
            if (service == null) {
                throw new HL7Exception.AR(
                    "Unrecognized Message Type^TriggerEvent "
                    + toKey(msh.getMessageType(), msh.getTriggerEvent()));   
            }
            return service.execute(data);
        } catch (HL7Exception e) {
            log.warn(e.getMessage(), e);
            return e.makeACK(msh);
        }
    }
    
    public boolean isSockedClosedByHandler() {
        return true;
    }
}
