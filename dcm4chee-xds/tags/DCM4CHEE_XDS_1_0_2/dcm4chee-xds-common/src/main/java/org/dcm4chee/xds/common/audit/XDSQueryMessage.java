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

import java.io.UnsupportedEncodingException;

import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.ParticipantObject;
import org.dcm4che2.audit.message.AuditEvent.TypeCode;

/**
 * This message describes the event of a Stored Query.
 * Document Consumer: Stored Query Transaction
 * 
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision:  $ $Date: $
 * @since Apr 01, 2009
 */
public class XDSQueryMessage extends BasicXDSAuditMessage {

    public XDSQueryMessage() {
        super(AuditEvent.ID.QUERY, AuditEvent.ActionCode.EXECUTE, TYPE_CODE_ITI18);
    }

    public static XDSQueryMessage createConsumerStoredQueryMessage(String srcUserId, boolean success, boolean sourceIsRequestor) {
        XDSQueryMessage msg = new XDSQueryMessage();
        msg.setOutcomeIndicator(success ? AuditEvent.OutcomeIndicator.SUCCESS:
            AuditEvent.OutcomeIndicator.MINOR_FAILURE);
        msg.setSource(srcUserId, 
                AuditMessage.getProcessID(),
                AuditMessage.getProcessName(),
                AuditMessage.getLocalHostName(),
                sourceIsRequestor);
        return msg;
    }
    
    public ParticipantObject addQuery(String queryId, String queryRequest) {
        ParticipantObject po = new ParticipantObject(queryId, 
                new ParticipantObject.IDTypeCode(TYPE_CODE_ITI18.getCode(),
                        TYPE_CODE_ITI18.getCodeSystemName(), TYPE_CODE_ITI18.getDisplayName()));
        po.setParticipantObjectTypeCode(ParticipantObject.TypeCode.SYSTEM);
        po.setParticipantObjectTypeCodeRole(ParticipantObject.TypeCodeRole.QUERY);
        if ( queryRequest != null )
            try {
                po.setParticipantObjectQuery(queryRequest.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ignore) {}
        return addParticipantObject(po);
    }
}