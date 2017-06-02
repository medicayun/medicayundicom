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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

import java.util.Collection;
import java.util.Date;

import org.dcm4chee.arr.entities.ActiveParticipant;
import org.dcm4chee.arr.entities.AuditRecord;
import org.dcm4chee.arr.entities.Code;
import org.dcm4chee.arr.entities.ParticipantObject;
import org.jboss.seam.annotations.Name;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 28, 2007
 */
@Name("audit_record")
public class AuditRecordEntry {
    
    private final AuditRecord auditRecord;
    
    public AuditRecordEntry(AuditRecord auditRecord) {
        this.auditRecord = auditRecord;
    }

    public String getSummary() {
        return XSLTUtils.toSummary(auditRecord.getXmldata());
    }    

    public Collection<ActiveParticipant> getActiveParticipants() {
        return auditRecord.getActiveParticipants();
    }

    public String getEnterpriseSiteID() {
        return auditRecord.getEnterpriseSiteID();
    }

    public String getEventAction() {
        return auditRecord.getEventAction();
    }

    public Date getEventDateTime() {
        return auditRecord.getEventDateTime();
    }

    public Code getEventID() {
        return auditRecord.getEventID();
    }

    public int getEventOutcome() {
        return auditRecord.getEventOutcome();
    }

    public Code getEventType() {
        return auditRecord.getEventType();
    }

    public Collection<ParticipantObject> getParticipantObjects() {
        return auditRecord.getParticipantObjects();
    }

    public int getPk() {
        return auditRecord.getPk();
    }

    public Date getReceiveDateTime() {
        return auditRecord.getReceiveDateTime();
    }

    public String getSourceID() {
        return auditRecord.getSourceID();
    }

    public int getSourceType() {
        return auditRecord.getSourceType();
    }

    public byte[] getXmldata() {
        return auditRecord.getXmldata();
    }

    public boolean isIHEYr4() {
        return auditRecord.isIHEYr4();
    }

    public String toString() {
        return auditRecord.toString();
    }    
}
