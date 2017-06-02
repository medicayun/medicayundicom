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
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.web.dao.util;

import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @author Franz Willer <fwiller@gmail.com>
 * @version $Revision$ $Date$
 * @since Apr 25, 2010
 */

public class QueryUtil {

    public static Query getQueryForPks(EntityManager em, String base, long[] pks) {
        Query q;
        int len=pks.length;
        if (len == 1) {
            q = em.createQuery(base+"= :pk").setParameter("pk", pks[0]);
        } else {
            StringBuilder sb = new StringBuilder(base);
            appendIN(sb, len);
            q = em.createQuery(sb.toString());
            setParametersForIN(q, pks);
        }
        return q;
    }
    
    public static void appendIN(StringBuilder sb, int len) {
        sb.append(" IN ( ?");
        for (int i = 1 ; i < len ; i++ ) {
            sb.append(i).append(", ?");
        }
        sb.append(len).append(" )");
    }
        
    public static void setParametersForIN(Query q, long[] pks) {
        int i = 1;
        for ( long pk : pks ) {
            q.setParameter(i++, pk);
        }
    }    
    
    public static void setParametersForIN(Query q, Object[] values) {
        int i = 1;
        for ( Object v : values ) {
            q.setParameter(i++, v);
        }
    }    
    
    public static Query getPatientQuery(EntityManager em, String patId, String issuer) {
        StringBuilder sb = new StringBuilder();
        boolean useIssuer = issuer != null && issuer.trim().length() > 0;
        sb.append("SELECT OBJECT(p) FROM Patient p WHERE patientID = :patId");
        if (useIssuer) {
            sb.append(" AND issuerOfPatientID = :issuer");
        }
        Query qP = em.createQuery(sb.toString()).setParameter("patId", patId);
        if (useIssuer)
            qP.setParameter("issuer", issuer);
        return qP;
        
    }
        
    public static String checkAutoWildcard(String s) {
        if (isUniversalMatch(s)) {
            return null;
        } else if (s.indexOf('*')!=-1 || s.indexOf('?')!=-1 || s.indexOf('^')!=-1) {
            return s;
        } else {
            return s+'*';
        } 
    }
    public static boolean isUniversalMatch(String s) {
        return s == null || s.length() == 0  || s.equals("*");
    }
    public static boolean isUniversalMatch(String[] sa) {
        if (sa == null || sa.length == 0)
            return true;
        for (int i = 0 ; i < sa.length ; i++) {
            if (sa[i] == null || sa[i].equals("*"))
                return true;
        }
        return false;
    }
    public static boolean containsWildcard(String s) {
        return s.indexOf('*') != -1 || s.indexOf('?') != -1;
    }
    public static boolean needEscape(String s) {
        return s.indexOf('%') != -1 || s.indexOf('_') != -1;
    }

    public static boolean isMustNotNull(String s) {
        return "?*".equals(s) || "*?".equals(s);
    }

    public static String toLike(String s) {
        StringBuilder param = new StringBuilder();
        StringTokenizer tokens = new StringTokenizer(s, "*?_%", true);
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            switch (token.charAt(0)) {
            case '%':
                param.append("\\%");
                break;
            case '*':
                param.append('%');
                break;
            case '?':
                param.append('_');
                break;
            case '_':
                param.append("\\_");
                break;
            default:
                param.append(token);
            }
        }
        return param.toString();
    }

    public static void appendANDwithTextValue(StringBuilder ql, String fieldName, String varName, String value) {
        if (value!=null) {
            ql.append(" AND ").append(fieldName);
            if ("-".equals(value)) {
                ql.append(" IS NULL");
            } else if (isMustNotNull(value)) {
                ql.append(" IS NOT NULL");
            } else if (QueryUtil.containsWildcard(value)) {
                ql.append(" LIKE ");
                if (needEscape(value)) {
                    ql.append("'").append(toLike(value)).append("' ESCAPE '\\'");
                } else {
                    ql.append(toVarName(fieldName,varName));
                }
            } else {
                ql.append(" = ").append(toVarName(fieldName, varName));
            }
        }
    }

    public static void setTextQueryParameter(Query query, String varName, String value) {
        if (value!=null
                && !"-".equals(value)
                && !QueryUtil.isMustNotNull(value)
                && !needEscape(value)) {
            query.setParameter(varName,
                    QueryUtil.containsWildcard(value)
                            ? toLike(value)
                            : value);
        }
    }

    public static void appendPatientName(StringBuilder ql, String fieldName, String varName, String patientName) {
        if (patientName!=null) {
            ql.append(" AND ").append(fieldName).append(" LIKE ");
            if (needEscape(patientName)) {
                ql.append("'").append(toPatientNameQueryString(patientName)).append("' ESCAPE '\\'");
            } else {
                ql.append(toVarName(fieldName, varName));
            }
        }
    }
    
    public static void setPatientNameQueryParameter(Query query, String varName, String patientName) {
        if (patientName!=null && !needEscape(patientName)) {
            query.setParameter(varName, toPatientNameQueryString(patientName));
        }
    }

    public static String toPatientNameQueryString(String patientName) {
        int padcarets = 4;
        StringBuilder param = new StringBuilder();
        StringTokenizer tokens = new StringTokenizer(patientName.toUpperCase(),
                "^*?_%", true);
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            switch (token.charAt(0)) {
            case '%':
                param.append("\\%");
                break;
            case '*':
                param.append('%');
                break;
            case '?':
                param.append('_');
                break;
            case '^':
                padcarets--;
                param.append('^');
                break;
            case '_':
                param.append("\\_");
                break;
            default:
                param.append(token);
            }
        }
        while (padcarets-- > 0) {
            param.append("^%");
        }
        return param.toString();
    }

    private static Object toVarName(String fieldName, String varName) {
        if (varName == null) {
            if (fieldName == null)
                throw new IllegalArgumentException("toVarName: filedName must not be null if varName is null");
            int pos = fieldName.lastIndexOf('.');
            varName = ":"+fieldName.substring(++pos);
        } else if (varName.charAt(0) != ':') {
            varName = ":"+varName;
        }
        return varName;
    }
    
}
