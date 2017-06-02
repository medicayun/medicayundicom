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

import java.util.List;

import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.ParticipantObject;
import org.dcm4che2.audit.message.AuditEvent.TypeCode;
import org.dcm4che2.audit.message.ParticipantObject.IDTypeCode;

/**
 * This message describes the event of retrieving document from a XDS.b.
 * Document Consumer: Retrieve Document Set Transaction
 * 
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision:  $ $Date: $
 * @since Mar 18, 2008
 */
public class XDSRetrieveMessage extends BasicXDSAuditMessage {

    private static final IDTypeCode IDTYPE_CODE_ITI43 = new ParticipantObject.IDTypeCode(TYPE_CODE_ITI43.getCode(),
	        				TYPE_CODE_ITI43.getCodeSystemName(),
	        				TYPE_CODE_ITI43.getDisplayName());
	public XDSRetrieveMessage(TypeCode typeCode) {
        super(AUDIT_EVENT_ID_REPORT, AuditEvent.ActionCode.READ, typeCode);
    }
    
    public static XDSRetrieveMessage createDocumentRepositoryRetrieveMessage(List<String> docUids) {
    	return createMessage(docUids, TYPE_CODE_ITI43);
    }
    
    private static XDSRetrieveMessage createMessage(List<String> docUids, TypeCode typeCode) {
    	XDSRetrieveMessage msg = new XDSRetrieveMessage(typeCode);
    	for( String uid : docUids ) {
    		msg.addDocumentUID(uid);
    	}
    	return msg;
    }
    public ParticipantObject addDocumentUID(String uid) {
        ParticipantObject doc = new ParticipantObject(uid, IDTYPE_CODE_ITI43);
        doc.setParticipantObjectTypeCode(ParticipantObject.TypeCode.SYSTEM);
        doc.setParticipantObjectTypeCodeRole(ParticipantObject.TypeCodeRole.REPORT);
        return addParticipantObject(doc);
    }

}