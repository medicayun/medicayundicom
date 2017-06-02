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
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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

package org.dcm4chex.xds.query;

import java.util.ArrayList;
import java.util.List;

public class StoredQueryObjectFactory extends XDSQueryObjectFatory {

    public static final String V3_STATUS_PREFIX = "urn:oasis:names:tc:ebxml-regrep:StatusType:";
    public static final String V3_STATUS_SUBMITTED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted";
    public static final String V3_STATUS_APPROVED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved";
    public static final String V3_STATUS_DEPRECATED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Deprecated";

    public static final String STORED_QUERY_FIND_DOCUMENTS = "urn:uuid:14d4debf-8f97-4251-9a74-a90016b0af0d";
    public static final String STORED_QUERY_FIND_SUBMISSIONSETS = "urn:uuid:f26abbcb-ac74-4422-8a30-edb644bbc1a9";
    public static final String STORED_QUERY_FIND_FOLDERS = "urn:uuid:958f3006-baad-4929-a4deff1114824431";
    public static final String STORED_QUERY_GET_ALL = "urn:uuid:10b545ea-725c-446d-9b95-8aeb444eddf3";
    public static final String STORED_QUERY_GET_DOCUMENTS = "urn:uuid:5c4f972b-d56b-40ac-a5fc-c8ca9b40b9d4";
    public static final String STORED_QUERY_GET_FOLDERS = "urn:uuid:5737b14c-8a1a-4539-b659-e03a34a5e1e4";
    public static final String STORED_QUERY_GET_SUBMISSIONSETS = "urn:uuid:51224314-5390-4169-9b91-b1980040715a";

    public XDSQueryObject newFindDocumentQuery(String patId, String status) {
        StoredQueryObject sqo = new StoredQueryObject(STORED_QUERY_FIND_DOCUMENTS);
        sqo.addQueryParameter("$XDSDocumentEntryPatientId", patId);
        ArrayList l = new ArrayList();
        if ( status != null ) {
            if ( ! status.startsWith(V3_STATUS_PREFIX)) {
                status = V3_STATUS_PREFIX + status;
            }
            l.add(status);
        } else {
            l.add(V3_STATUS_SUBMITTED);
            l.add(V3_STATUS_APPROVED);
            l.add(V3_STATUS_DEPRECATED);
        }
        sqo.addQueryParameter("$XDSDocumentEntryStatus", l);
        return sqo;
    }
    public XDSQueryObject newFindDocumentQuery(String patId, String status, 
                    String dateTimeAtt, String dateTimeFrom, String dateTimeTo, 
                    List classCodes, List psCodes, List hcftCodes, List evCodes) {
        StoredQueryObject sqo = (StoredQueryObject) newFindDocumentQuery(patId, status);
        if ( dateTimeAtt != null ) sqo.addQueryParameter("$XDSDocumentEntryCreationTimeFrom", dateTimeAtt);
        if ( classCodes != null ) sqo.addQueryParameter("$XDSDocumentEntryClassCode", classCodes);
        if ( psCodes != null ) sqo.addQueryParameter("$XDSDocumentEntryPracticeSettingCode", psCodes);
        if ( hcftCodes != null ) sqo.addQueryParameter("$XDSDocumentEntryHealthcareFacilityTypeCode", hcftCodes);
        if ( evCodes != null ) sqo.addQueryParameter("$XDSDocumentEntryEventCodeList", evCodes);
        return sqo;
    }

    public XDSQueryObject newGetDocumentQuery(String[] uuids) {
        StoredQueryObject sqo = new StoredQueryObject(STORED_QUERY_GET_DOCUMENTS);
        ArrayList l = new ArrayList();
        for ( int i = 0 ; i < uuids.length ; i++) {
            l.add(uuids[i]);
        }
        sqo.addQueryParameter("$XDSDocumentEntryEntryUUID", l);
        sqo.setReturnType(XDSQueryObject.RETURN_TYPE_LEAF);
        return sqo;
    }
    

}
