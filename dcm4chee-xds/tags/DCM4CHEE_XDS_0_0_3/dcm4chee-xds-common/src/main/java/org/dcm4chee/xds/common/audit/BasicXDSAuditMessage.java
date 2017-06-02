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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
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

package org.dcm4chee.xds.common.audit;

import java.util.Iterator;
import java.util.List;

import org.dcm4che2.audit.message.ActiveParticipant;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.ParticipantObject;
import org.dcm4che2.audit.message.AuditEvent.ID;
import org.dcm4che2.audit.message.AuditEvent.TypeCode;
import org.dcm4che2.audit.message.ParticipantObject.IDTypeCode;
import org.dcm4che2.audit.message.ParticipantObject.TypeCodeRole;

/**
 * <DL>
 * <DT>This class provides the common parts of audit message for XDS transactions:</DT>
 * <DD>Event (AuditMessage/EventIdentification)</DD>
 * <DD>Audit Source (AuditMessage/AuditSourceIdentification)</DD>
 * <DD>Source (AuditMessage/ActiveParticipant)</DD>
 * <DD>Destination (AuditMessage/ActiveParticipant)</DD>
 * <DD>Human Requestor (AuditMessage/ActiveParticipant)</DD>
 * <DD>Source (AuditMessage/ActiveParticipant)</DD>
 * <DD>Patient (AuditMessage/ParticipantObjectIdentification)</DD>
 * <DD>Submission Set (AuditMessage/ParticipantObjectIdentification)</DD>
 * </DL>
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision:  $ $Date: $
 * @since Jan 02, 2008
 * @see <a href="http://www.ihe.net/Technical_Framework/upload/IHE_ITI_TF_4.0_Vol2_FT_2007-08-22.pdf">
 * IT Infrastructure Technical Framework: Vol. 2 (ITI TF-2)</a>
 */
public abstract class BasicXDSAuditMessage extends AuditMessage {

    public static final TypeCode TYPE_CODE_ITI14 = new AuditEvent.TypeCode("ITI-14", "IHE Transactions", "Register Document Set");
    public static final TypeCode TYPE_CODE_ITI15 = new AuditEvent.TypeCode("ITI-15", "IHE Transactions", "Provide and Register Document Set");
    public static final TypeCode TYPE_CODE_ITI16 = new AuditEvent.TypeCode("ITI-16", "IHE Transactions", "Registry SQL Query");
    public static final TypeCode TYPE_CODE_ITI17 = new AuditEvent.TypeCode("ITI-17", "IHE Transactions", "Retrieve Document");
    public static final TypeCode TYPE_CODE_ITI18 = new AuditEvent.TypeCode("ITI-18", "IHE Transactions", "Registry Stored Query");
    public static final TypeCode TYPE_CODE_ITI43 = new AuditEvent.TypeCode("ITI-43", "IHE Transactions", "Retrieve Document Set");

    public static final AuditEvent.ID AUDIT_EVENT_ID_REPORT = new ID("3","IHE","Report");

    private static final ActiveParticipant.RoleIDCode ROLE_ID_HUMAN_REQUESTOR = new ActiveParticipant.RoleIDCode("HumanRequestor");

    public BasicXDSAuditMessage(AuditEvent.ID eventID, AuditEvent.ActionCode actionCode, TypeCode typeCode) {
        super(new AuditEvent(eventID,
                actionCode).addEventTypeCode(typeCode));
    }

    public ActiveParticipant setSource(String processID, String[] alternateIDs, 
            String processName, String hostname, boolean requestor) {
        return addActiveParticipant(
                XDSActiveParticipant.createActiveProcess(processID, alternateIDs, 
                        processName, hostname, requestor)
                        .addRoleIDCode(ActiveParticipant.RoleIDCode.SOURCE));
    }

    public ActiveParticipant setDestination(String uri, String[] alternateIDs, 
            String userName, String hostname, boolean requestor) {
        return addActiveParticipant(
                XDSActiveParticipant.createActiveProcess(uri, alternateIDs, 
                        userName, hostname, requestor)
                        .addRoleIDCode(ActiveParticipant.RoleIDCode.DESTINATION));
    }

    public ActiveParticipant setHumanRequestor(String userID, String altUserID, String userName, boolean requestor) {
        return addActiveParticipant(
                XDSActiveParticipant.createActivePerson(userID, altUserID, 
                        userName, null, requestor))
                        .addRoleIDCode(ROLE_ID_HUMAN_REQUESTOR);
    }

    public ParticipantObject setPatient(String id, String name) {
        //IN IHE we need full IDTypecode for patientId: 2,RFC-3881,Patient Number 
        ParticipantObject pat = new ParticipantObject(id, new IDTypeCode(IDTypeCode.PATIENT_ID.getCode(),"RFC-3881","Patient Number"));
        pat.setParticipantObjectTypeCode(ParticipantObject.TypeCode.PERSON);
        pat.setParticipantObjectTypeCodeRole(TypeCodeRole.PATIENT);            
        pat.setParticipantObjectName(name);
        return addParticipantObject(pat);
    }


    public ParticipantObject setSubmissionSet(String uid) {
        ParticipantObject subm = new ParticipantObject(uid, 
                new ParticipantObject.IDTypeCode("urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd","IHE XDS Metadata","Submission Set ClassificationNode UID"));
        subm.setParticipantObjectTypeCode(ParticipantObject.TypeCode.SYSTEM);
        subm.setParticipantObjectTypeCodeRole(ParticipantObject.TypeCodeRole.JOB);
        return addParticipantObject(subm);
    }

    public ParticipantObject setDocumentUri(String docUri, String docUid) {
        ParticipantObject po = new ParticipantObject(docUri, 
                new ParticipantObject.IDTypeCode("12","RFC-3881","URI"));
        po.setParticipantObjectTypeCode(ParticipantObject.TypeCode.SYSTEM);
        po.setParticipantObjectTypeCodeRole(ParticipantObject.TypeCodeRole.REPORT);
        if ( docUid != null )
            po.addParticipantObjectDetail("Document Unique Id", docUid);
        return addParticipantObject(po);
    }

    public void validate() {
        super.validate();
        ActiveParticipant source = null;
        ActiveParticipant dest = null;
        ActiveParticipant requestor = null;
        for (Iterator iter = activeParticipants.iterator(); iter.hasNext();) {
            ActiveParticipant ap = (ActiveParticipant) iter.next();
            List roleIDCodeIDs = ap.getRoleIDCodes();
            if (roleIDCodeIDs.contains(
                    ActiveParticipant.RoleIDCode.DESTINATION)) {
                if (dest != null) {
                    throw new IllegalStateException(
                    "Multiple Destination identification");
                }
                dest = ap;               
            } else if (roleIDCodeIDs.contains(
                    ActiveParticipant.RoleIDCode.SOURCE)) {
                if (source != null) {
                    throw new IllegalStateException(
                    "Multiple Source identification");
                }
                source = ap;               
            } else if (roleIDCodeIDs.contains(ROLE_ID_HUMAN_REQUESTOR) ) {
                requestor = ap;
            }            
        }
        if (dest == null) {
            throw new IllegalStateException("No Destination identification");
        }
        if (source == null) {
            throw new IllegalStateException("No Source identification");
        }
    }    
    // Workaround to get UserIsRequestor=true in Audit message without change in dcm4chee-audit!
    //TODO: Add setter method for encodeUserIsRequestorTrue in dcm4che-audit to make this switch available.

    static class XDSActiveParticipant extends ActiveParticipant {

        private static final String USER_IS_REQUESTOR = "UserIsRequestor";

        public XDSActiveParticipant(String userID, boolean userIsRequestor) {
            super(userID, userIsRequestor);
            if (this.getAttribute(USER_IS_REQUESTOR) == null ) {
                addAttribute(USER_IS_REQUESTOR, Boolean.valueOf(userIsRequestor), true);
            }
        }

        public static ActiveParticipant createActiveProcess(String processID,
                String[] aets, String processName, String nodeID, boolean requestor) {
            return createActivePerson(processID, AuditMessage.aetsToAltUserID(aets), 
                    processName, nodeID, requestor) ;
        }

        public static ActiveParticipant createActivePerson(String userID,
                String altUserID, String userName, String napID, boolean requestor) {
            XDSActiveParticipant ap = new XDSActiveParticipant(userID, requestor);
            ap.setAlternativeUserID(altUserID);
            ap.setUserName(userName);
            ap.setNetworkAccessPointID(napID);
            return ap;
        }

    }
}