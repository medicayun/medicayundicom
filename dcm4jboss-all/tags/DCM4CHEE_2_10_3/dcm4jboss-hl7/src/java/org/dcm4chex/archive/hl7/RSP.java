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
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
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


package org.dcm4chex.archive.hl7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.regenstrief.xhl7.HL7XMLLiterate;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2405 $ $Date: 2006-04-07 15:51:19 +0800 (周五, 07 4月 2006) $
 * @since Mar 29, 2006
 */
class RSP extends ACK {

    public final String queryTag;
    public final String queryResponseStatus;
    private final ArrayList pids = new ArrayList();

    public RSP(Document msg) {
        super(msg);
        Element qak = msg.getRootElement().element("QAK");
        if (qak == null)
                throw new IllegalArgumentException("Missing QAK Segment");
        List qakfds = qak.elements(HL7XMLLiterate.TAG_FIELD);
        this.queryTag = toString(qakfds.get(0));
        this.queryResponseStatus = toString(qakfds.get(1));
        if (!"OK".equals(queryResponseStatus)) {
            return;
        }
        Element pid = msg.getRootElement().element("PID");
        if (pid == null)
            throw new IllegalArgumentException("Missing PID Segment");
        List pidfds = pid.elements(HL7XMLLiterate.TAG_FIELD);
        Element pidfd = (Element) pidfds.get(2);
        if (pidfd == null)
            throw new IllegalArgumentException("Missing PID-3 Field");
        pids.add(toPID(pidfd));
        List furtherPids = pidfd.elements(HL7XMLLiterate.TAG_REPEAT);
        for (Iterator iter = furtherPids.iterator(); iter.hasNext();) {
            pids.add(toPID((Element) iter.next()));            
        }
    }

    private String toPID(Element pidfd) {
        StringBuffer sb = new StringBuffer(toString(pidfd));
        Element authority = (Element) pidfd.elements(HL7XMLLiterate.TAG_COMPONENT).get(2);
        if (authority == null) {
            throw new IllegalArgumentException("Missing Authority in returned PID");
        }
        sb.append("^^^").append(toString(authority));
        List authorityUID = authority.elements(HL7XMLLiterate.TAG_SUBCOMPONENT);
        for (Iterator iter = authorityUID.iterator(); iter.hasNext();) {
            sb.append('&').append(toString(iter.next()));
        }
        return sb.toString();
    }

    public String toString() {
        return "RSP[code=" +  acknowledgmentCode 
                + ", msgID=" + messageControlID + ','
                + ", errorMsg=" + textMessage
                + ", status=" + queryResponseStatus
                + ", pid# =" + pids.size()
                + "]";
    }
    
    public List getPatientIDs() {
        return Collections.unmodifiableList(pids);
    }
    
}
