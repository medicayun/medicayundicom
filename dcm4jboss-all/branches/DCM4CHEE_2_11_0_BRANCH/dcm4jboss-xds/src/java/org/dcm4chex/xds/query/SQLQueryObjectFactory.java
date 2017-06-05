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

import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4chex.xds.query.match.ClassificationMatch;

public class SQLQueryObjectFactory extends XDSQueryObjectFatory  {

    private static Logger log = Logger.getLogger(SQLQueryObjectFactory.class);
    
    /* (non-Javadoc)
     * @see org.dcm4chex.xds.query.XDSQueryObjectFatory#newFindDocumentQuery(java.lang.String, java.lang.String)
     */
    public XDSQueryObject newFindDocumentQuery(String patId, String status) {
        return newFindDocumentQuery(patId, status, null, null, null, null, null, null ,null);
    }
    /* (non-Javadoc)
     * @see org.dcm4chex.xds.query.XDSQueryObjectFatory#newFindDocumentQuery(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, java.util.List, java.util.List, java.util.List)
     */
    public XDSQueryObject newFindDocumentQuery(String patId, String status, 
                    String dateTimeAtt, String dateTimeFrom, String dateTimeTo, 
                    List classCodes, List psCodes, List hcftCodes, List evCodes) {
        StringBuffer sbSelect = new StringBuffer();
        StringBuffer sbWhere = new StringBuffer();
        sbSelect.append("SELECT eo.id  FROM ExtrinsicObject eo, ExternalIdentifier ei");
        sbWhere.append(" WHERE eo.id = ei.registryobject");
        sbWhere.append(" AND ei.identificationScheme='urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427'");
        sbWhere.append(" AND ei.value='").append( patId ).append("'");
        if ( status != null ) sbWhere.append(" AND eo.status = '"+status+"'");
        addTimeMatch(dateTimeAtt, dateTimeFrom, dateTimeTo, sbSelect, sbWhere);
        addCodeMatch(ClassificationMatch.getClassCodeMatch(classCodes), sbSelect, sbWhere);
        addCodeMatch(ClassificationMatch.getPSCodeMatch(psCodes), sbSelect, sbWhere);
        addCodeMatch(ClassificationMatch.getHFTCodeMatch(hcftCodes), sbSelect, sbWhere);
        addCodeMatch(ClassificationMatch.getEVCodeMatch(evCodes), sbSelect, sbWhere);
        sbSelect.append(sbWhere);
        log.info("getDocument SQL String:"+sbSelect);
       return new SQLQueryObject(sbSelect.toString());
    }
    
    public XDSQueryObject newGetDocumentQuery( String[] uuids ) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT doc.id FROM ExtrinsicObject doc WHERE doc.id IN ( ");
        addListString(sb, uuids);
        sb.append(" )");
        return new SQLQueryObject(sb.toString(),XDSQueryObject.RETURN_TYPE_LEAF,true);
    }
    
    private static void addTimeMatch(String dateTimeAtt, String dateTimeFrom, String dateTimeTo, StringBuffer sbSelect, StringBuffer sbWhere) {
        if (dateTimeAtt == null || ( dateTimeFrom == null && dateTimeTo == null)) return;
        sbSelect.append(", Slot dateTime");
        sbWhere.append(" AND ( dateTime.parent = doc.id AND dateTime.name = ").append(dateTimeAtt);
        if ( dateTimeFrom != null ) sbWhere.append(" AND dateTime.value &gt;=  ").append(dateTimeFrom);
        if ( dateTimeTo != null ) sbWhere.append(" AND dateTime.value &lt;  ").append(dateTimeTo);
    }
    private static void addCodeMatch(ClassificationMatch classMatch, StringBuffer sbSelect, StringBuffer sbWhere) {
        if (classMatch == null ) return;
        sbSelect.append(", Classification ").append(classMatch.getName());
        sbWhere.append(" AND ").append(classMatch);
    }
    
    private void addListString( StringBuffer sb, String[] values ) {
        if ( values == null || values.length < 1 ) return;
        sb.append('\'').append( values[0] ).append('\'');
        for ( int i = 1 ; i < values.length ; i++ ) {
            sb.append(",'").append(values[i]).append('\'');
        }
    }

}
