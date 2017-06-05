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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class StoredQueryObject extends XDSQueryObject {

    public static final String STATUS_SUBMITTED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted";
    public static final String STATUS_APPROVED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved";
    public static final String STATUS_DEPRECATED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Deprecated";

    public static final String TAG_ADHOC_QUERY_REQ = "AdhocQueryRequest";
    public static final String TAG_ADHOC_QUERY = "AdhocQuery";
    public static final String TAG_RESPONSE_OPTION  = "ResponseOption";
    public static final String TAG_STORED_QUERY  = "StoredQuery";
    public static final String TAG_SLOT  = "Slot";
    public static final String TAG_VALUE_LIST  = "ValueList";
    public static final String TAG_VALUE  = "Value";

    public static final String ATTR_ID  = "id";
    public static final String ATTR_NAME  = "name";
    public static final String ATTR_RETURN_TYPE  = "returnType";
    public static final String ATTR_RETURN_COMPOSED_OBJ  = "returnComposedObjects";

    public static final String URN_RIM = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";
    public static final String URN_RS = "urn:oasis:names:tc:ebxml-regrep:xsd:registry:3.0";
    public static final String URN_Q = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0";

    public static final String STORED_QUERY_FIND_DOCUMENTS = "urn:uuid:14d4debf-8f97-4251-9a74-a90016b0af0d";
    public static final String STORED_QUERY_FIND_SUBMISSIONSETS = "urn:uuid:f26abbcb-ac74-4422-8a30-edb644bbc1a9";
    public static final String STORED_QUERY_FIND_FOLDERS = "urn:uuid:958f3006-baad-4929-a4deff1114824431";
    public static final String STORED_QUERY_GET_ALL = "urn:uuid:10b545ea-725c-446d-9b95-8aeb444eddf3";
    public static final String STORED_QUERY_GET_DOCUMENTS = "urn:uuid:5c4f972b-d56b-40ac-a5fcc8ca9b40b9d4";
    public static final String STORED_QUERY_GET_FOLDERS = "urn:uuid:5737b14c-8a1a-4539-b659-e03a34a5e1e4";
    public static final String STORED_QUERY_GET_SUBMISSIONSETS = "urn:uuid:51224314-5390-4169-9b91-b1980040715a";

    private String storedQueryId;
    private Map queryParams = new HashMap();
    
    private static Logger log = Logger.getLogger(StoredQueryObject.class);
    
    public StoredQueryObject(String storedQueryId) {
        this.storedQueryId = storedQueryId;
    }
    public StoredQueryObject(String storedQueryId, String returnType, boolean returnComposedObjects) {
        this(storedQueryId);
        setReturnType(returnType);
        this.setReturnComposedObjects(returnComposedObjects);
    }
    
    public void addQueryParameter( String name, Object value ) {
        queryParams.put(name, value);
    }
    
    
    public void addAdhocQueryRequest() throws SAXException {
        //th.startPrefixMapping(XMLNS_DEFAULT,URN_Q);
        th.startPrefixMapping(XMLNS_Q,URN_Q);
        th.startPrefixMapping(XMLNS_RS,URN_RS);
        th.startPrefixMapping(XMLNS_RIM,URN_RIM);
        th.startElement(URN_Q, TAG_ADHOC_QUERY_REQ, TAG_ADHOC_QUERY_REQ, EMPTY_ATTRIBUTES );
        //ResponseOption
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(URN_Q, ATTR_RETURN_TYPE, ATTR_RETURN_TYPE, "", getReturnType());
        attrs.addAttribute(URN_Q, ATTR_RETURN_COMPOSED_OBJ, ATTR_RETURN_COMPOSED_OBJ, "", isReturnComposedObjects() ? "true" : "false");
        th.startElement(URN_Q, TAG_RESPONSE_OPTION, TAG_RESPONSE_OPTION, attrs);
        th.endElement(URN_Q, TAG_RESPONSE_OPTION, TAG_RESPONSE_OPTION );
        //AdhocQuery
        AttributesImpl adhocAttrs = new AttributesImpl();
        adhocAttrs.addAttribute(URN_Q, ATTR_ID, ATTR_ID, "", storedQueryId);
        th.startElement(URN_RIM, TAG_ADHOC_QUERY, TAG_ADHOC_QUERY, adhocAttrs );
        addQueryParameters();
        th.endElement(URN_RIM, TAG_ADHOC_QUERY, TAG_ADHOC_QUERY );

        th.endElement(URN_Q, TAG_ADHOC_QUERY_REQ, TAG_ADHOC_QUERY_REQ );
        th.endPrefixMapping(XMLNS_RIM);
        th.endPrefixMapping(XMLNS_RS);
        th.endPrefixMapping(XMLNS_Q);
        //th.endPrefixMapping(XMLNS_DEFAULT);
    }
    
    private void addQueryParameters() throws SAXException {
        Map.Entry e;
        AttributesImpl attrs;
        Object o; 
        StringBuffer sb = new StringBuffer();
        for ( Iterator iter = queryParams.entrySet().iterator() ; iter.hasNext() ; ) {
            e = (Map.Entry) iter.next();
            sb.setLength(0);
            attrs = new AttributesImpl();
            attrs.addAttribute(URN_RIM, ATTR_NAME, ATTR_NAME, "", (String)e.getKey());
            th.startElement(URN_RIM, TAG_SLOT, TAG_SLOT, attrs );
            th.startElement(URN_RIM, TAG_VALUE_LIST, TAG_VALUE_LIST, EMPTY_ATTRIBUTES );
            if ( (o = e.getValue()) instanceof List ) {
                Iterator it = ((List)o).iterator();
                if ( it.hasNext() ) {
                    sb.append("(").append( getQueryValue( it.next() ) );
                    for ( ; it.hasNext() ; ) {
                        sb.append(",").append( getQueryValue( it.next() ) );
                    }
                    sb.append(")");
                    addValue(sb.toString());
                }
            } else {
                addValue( getQueryValue( o ) );
            }
            th.endElement(URN_RIM, TAG_VALUE_LIST, TAG_VALUE_LIST );
            th.endElement(URN_RIM, TAG_SLOT, TAG_SLOT );
        }
    }
    
    private String getQueryValue( Object o ) {
        return ( o instanceof Number ) ? o.toString() : "'"+o.toString()+"'";
    }
    
    private void addValue(String value) throws SAXException {
        th.startElement(URN_RIM, TAG_VALUE, TAG_VALUE, EMPTY_ATTRIBUTES );
        th.characters( value.toCharArray(),0,value.length());
        th.endElement(URN_RIM, TAG_VALUE, TAG_VALUE );
    }

    public String getResponseTag() {
        return "AdhocQueryResponse";
    }

}
