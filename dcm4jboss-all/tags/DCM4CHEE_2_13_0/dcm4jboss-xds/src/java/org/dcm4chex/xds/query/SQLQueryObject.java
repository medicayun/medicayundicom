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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SQLQueryObject extends XDSQueryObject {

    public static final String TAG_ADHOC_QUERY_REQ = "AdhocQueryRequest";
    public static final String TAG_RESPONSE_OPTION  = "ResponseOption";
    public static final String TAG_SQL_QUERY  = "SQLQuery";

    public static final String ATTR_RETURN_TYPE  = "returnType";
    public static final String ATTR_RETURN_COMPOSED_OBJ  = "returnComposedObjects";
    
    public static final String URN_RIM = "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1";
    public static final String URN_RS = "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1";
    public static final String URN_Q = "urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1";


    private String sql;
    
    private static Logger log = Logger.getLogger(SQLQueryObject.class);
    
    public SQLQueryObject(String sql) {
        this.sql = sql;
    }
    public SQLQueryObject(String sql, String returnType, boolean returnComposedObjects) {
        this(sql);
        setReturnType(returnType);
        this.setReturnComposedObjects(returnComposedObjects);
    }
    
    public void addAdhocQueryRequest() throws SAXException {
        th.startPrefixMapping(XMLNS_DEFAULT,URN_Q);
        th.startPrefixMapping(XMLNS_Q,URN_Q);
        th.startPrefixMapping(XMLNS_RS,URN_RS);
        th.startPrefixMapping(XMLNS_RIM,URN_RIM);
        th.startElement(URN_Q, TAG_ADHOC_QUERY_REQ, TAG_ADHOC_QUERY_REQ, EMPTY_ATTRIBUTES );
        //ResponseOption
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(URN_Q, ATTR_RETURN_TYPE, ATTR_RETURN_TYPE, "", RETURN_TYPE_LEAF);
        attrs.addAttribute(URN_Q, ATTR_RETURN_COMPOSED_OBJ, ATTR_RETURN_COMPOSED_OBJ, "", isReturnComposedObjects() ? "true" : "false");
        th.startElement(URN_Q, TAG_RESPONSE_OPTION, TAG_RESPONSE_OPTION, attrs);
        th.endElement(URN_Q, TAG_RESPONSE_OPTION, TAG_RESPONSE_OPTION );
        //SQLQuery
        th.startElement(URN_Q, TAG_SQL_QUERY, TAG_SQL_QUERY, EMPTY_ATTRIBUTES );
        th.characters(sql.toCharArray(),0,sql.length());
        th.endElement(URN_Q, TAG_SQL_QUERY, TAG_SQL_QUERY );

        th.endElement(URN_Q, TAG_ADHOC_QUERY_REQ, TAG_ADHOC_QUERY_REQ );
        th.endPrefixMapping(XMLNS_RIM);
        th.endPrefixMapping(XMLNS_RS);
        th.endPrefixMapping(XMLNS_Q);
        th.endPrefixMapping(XMLNS_DEFAULT);
    }

    public String getResponseTag() {
        return "RegistryResponse";
    }

}
