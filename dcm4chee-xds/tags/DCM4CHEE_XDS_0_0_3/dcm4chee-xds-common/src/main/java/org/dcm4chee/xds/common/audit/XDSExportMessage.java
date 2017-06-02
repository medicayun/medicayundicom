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

import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditEvent.TypeCode;

/**
 * This message describes the event of exporting data from a XDS subsystem to another.
 * Document Consumer: Provide And Register Transaction
 * Repository: Register Transaction
 * 
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision:  $ $Date: $
 * @since Jan 02, 2008
 * @see <a href="http://www.ihe.net/Technical_Framework/upload/IHE_ITI_TF_5-0_Vol2_FT_2008-12-12.pdf">
 * IT Infrastructure Technical Framework: Vol. 2 (ITI TF-2), 3.14.5.2.1 Document Repository or Integrated Document Source/Repository audit message</a>
 * @see <a href="http://www.ihe.net/Technical_Framework/upload/IHE_ITI_TF_5-0_Vol2_FT_2008-12-12.pdf">
 * IT Infrastructure Technical Framework: Vol. 2 (ITI TF-2), 3.15.6.1.1 Document Source audit message:</a>
 * @see <a href="http://www.ihe.net/Technical_Framework/upload/IHE_ITI_TF_5-0_Vol2_FT_2008-12-12.pdf">
 * IT Infrastructure Technical Framework: Vol. 2 (ITI TF-2), 3.17.5.1.2 Document Repository audit message</a>
 */
public class XDSExportMessage extends BasicXDSAuditMessage {

    public XDSExportMessage(TypeCode typeCode) {
        super(AuditEvent.ID.EXPORT, AuditEvent.ActionCode.READ, typeCode);
    }
    
    public static XDSExportMessage createDocumentSourceExportMessage(String submissionUID, String patID) {
    	XDSExportMessage msg = createMessage(submissionUID, TYPE_CODE_ITI15);
    	msg.setPatient(patID, null);
    	return msg;
    }
    
    public static XDSExportMessage createDocumentRepositoryExportMessage(String submissionUID, String patID) {
        XDSExportMessage msg = createMessage(submissionUID, TYPE_CODE_ITI14);
    	msg.setPatient(patID, null);
    	return msg;
    }

    public static XDSExportMessage createDocumentRepositoryRetrieveMessage(String docUri, String docUid) {
        XDSExportMessage msg = new XDSExportMessage(TYPE_CODE_ITI17);
        msg.setDocumentUri(docUri, docUid);
        return msg;
    }
    
    private static XDSExportMessage createMessage(String submissionUID, TypeCode typeCode) {
    	XDSExportMessage msg = new XDSExportMessage(typeCode);
    	msg.setSubmissionSet(submissionUID);
    	return msg;
    }

}