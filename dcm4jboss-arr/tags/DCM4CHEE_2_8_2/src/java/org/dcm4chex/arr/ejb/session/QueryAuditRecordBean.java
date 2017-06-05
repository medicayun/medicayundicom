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
 * Joe Foraci <jforaci@users.sourceforge.net>
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

package org.dcm4chex.arr.ejb.session;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import javax.ejb.EJBException;
import javax.ejb.FinderException;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import org.dcm4chex.arr.ejb.entity.AuditRecordLocalHome;

/**
 *  QueryAuditRecord bean.
 *
 * @ejb:bean
 *  name="QueryAuditRecord"
 *  type="Stateless"
 *  view-type="local"
 *  local-jndi-name="ejb/QueryAuditRecord"
 * 
 * @ejb:transaction
 *  type="NotSupported"
 * 
 * @ejb:transaction-type
 *  type="Container"
 * 
 * @ejb:ejb-ref
 *  ref-name="ejb/AuditRecord"
 *  view-type="local"
 *  ejb-name="AuditRecord"
 * 
 * @ejb.env-entry 
 * 	name="Database"
 * 	type="java.lang.String"
 *  value="Hypersonic SQL"
 * 
 * @ejb:resource-ref
 *  res-name="jdbc/DS"
 *  res-type="javax.sql.DataSource"
 *  res-auth="Container"
 * 
 * @jboss:resource-ref 
 *  res-ref-name="jdbc/DS"
 *  res-type="javax.sql.DataSource"
 *  jndi-name="java:/DefaultDS"
 * 
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author  <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @created  February 15, 2003
 * @version  $Revision: 4426 $ $Date: 2005-11-04 22:21:07 +0800 (周五, 04 11月 2005) $
 */
public abstract class QueryAuditRecordBean implements SessionBean {

    private static final String ORDER_BY_TIMESTAMP = "timestamp";
	private static final String ORDER_BY_HOST = "host";
	private static final String ORDER_BY_TYPE = "type";
	static final Logger log = Logger.getLogger(QueryAuditRecordBean.class);

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";

    // Be aware of correct order ( implicit mapping to HSQL, PSQL, MYSQL,..) !
    static final String[] DATABASE = { "Hypersonic SQL", "PostgreSQL 7.2",
            "mySQL", "DB2", "Oracle9i", "MS SQLSERVER2000"};

    static final int HSQL = 0;

    static final int PSQL = 1;

    static final int MYSQL = 2;

    static final int DB2 = 3;

    static final int ORACLE = 4;

    static final int MSSQL = 5;
    
    private AuditRecordLocalHome home;

    private Connection con = null;

    private Statement stmt = null;

    private int database = -1;

    /**
     *  Sets the sessionContext attribute of the QueryAuditRecordBean object
     *
     * @param  sessionContext The new sessionContext value
     */
    public void setSessionContext(SessionContext sessionContext) {
        DataSource ds = null;
        try {
            Context jndiCtx = new InitialContext();
            home = (AuditRecordLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/AuditRecord");
            ds = (DataSource) jndiCtx.lookup("java:comp/env/jdbc/DS");
            String envVal = (String) jndiCtx.lookup("java:comp/env/Database");
            database = Arrays.asList(DATABASE).indexOf(envVal);
            if (database == -1) { throw new EJBException(
                    "Undefined Database Type: " + envVal); }
        } catch (NamingException e) {
            log.error("Failed lookup ns:", e);
            throw new EJBException(e);
        }
        try {
            con = ds.getConnection();
            stmt = con.createStatement();
        } catch (SQLException e) {
            log.error("Failed to connect to db:", e);
            unsetSessionContext();
            throw new EJBException(e);
        }
    }

    /**
     *  Description of the Method
     */
    public void unsetSessionContext() {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
            }
        }
    }

    /**
     * @param  pk Description of the Parameter
     * @return  The timeStamp value
     * @ejb:interface-method
     */
    public Timestamp getTimestamp(int pk) {
        try {
            return home.findByPrimaryKey(new Integer(pk)).getTimestamp();
        } catch (FinderException e) {
            log.error("Failed to access AuditRecord with pk=" + pk, e);
            throw new EJBException(e);
        }
    }

    /**
     * @param  pk Description of the Parameter
     * @return  The Type value
     * @ejb:interface-method
     */
    public String getType(int pk) {
        try {
            return home.findByPrimaryKey(new Integer(pk)).getType();
        } catch (FinderException e) {
            log.error("Failed to access AuditRecord with pk=" + pk, e);
            throw new EJBException(e);
        }
    }

    /**
     * @param  pk Description of the Parameter
     * @return  The xmlData value
     * @ejb:interface-method
     */
    public String getXmlData(int pk) {
        try {
            return home.findByPrimaryKey(new Integer(pk)).getXmlData();
        } catch (FinderException e) {
            log.error("Failed to access AuditRecord with pk=" + pk, e);
            throw new EJBException(e);
        }
    }

    /**
     * @param  type Description of the Parameter
     * @param  host Description of the Parameter
     * @param  from Description of the Parameter
     * @param  to Description of the Parameter
     * @return  Description of the Return Value
     * @ejb:interface-method
     */
    public String query(String[] type, String host, String from, String to,
            String aet, String userName, String patientName, String patientId,
            int offset, int limit, String[] orderBy, String[] orderDir) {
        ResultSet rs = null;
        String sql = buildSQL(type, host, from, to, aet, userName, patientName,
                patientId, offset, limit, orderBy, orderDir);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Execute Query: " + sql);
            }
            ResultSet countrs = stmt.executeQuery(buildCountSQL(type, host, from,
                    to, aet, userName, patientName, patientId));
            countrs.next();
            int ntot = countrs.getInt(1);
            rs = stmt.executeQuery(sql);
            return toXML(type, host, from, to, aet, userName, patientName,
                    patientId, orderBy, orderDir, rs, offset, limit, ntot);
        } catch (SQLException e) {
            log.error("Failed to query db:", e);
            throw new EJBException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    private final static String COLUMNS = "pk, msg_type, host_name, time_stamp, aet, user_name, patient_name, patient_id";
    private final static Hashtable TO_COLUMN = new Hashtable();
    static {
    	TO_COLUMN.put(ORDER_BY_TYPE, "msg_type");
    	TO_COLUMN.put(ORDER_BY_HOST, "host_name");
    	TO_COLUMN.put(ORDER_BY_TIMESTAMP, "time_stamp");
    }
	
    private String buildCountSQL(String[] type, String host, String from,
            String to, String aet, String userName, String patientName,
            String patientId) {
        return appendFromWhere(new StringBuffer("SELECT COUNT(pk)"), type,
                host, from, to, aet, userName, patientName, patientId)
                .toString();
    }

    private String buildSQL(String[] type, String host, String from, String to,
            String aet, String userName, String patientName, String patientId,
            int offset, int limit, String[] orderBy, String[] orderDir) {
        StringBuffer sb = new StringBuffer("SELECT ");
        if ( orderBy == null ) orderBy = new String[]{ORDER_BY_TIMESTAMP};
        if ( orderDir == null || orderDir.length != orderBy.length) {
	        orderDir = new String[ orderBy.length ];
	        for (int i = 0 ; i < orderDir.length ; i++ ) orderDir[i]="DESC";
        }
        if (limit > 0) {
	        switch (database) {
	        case HSQL:
	            sb.append("LIMIT ");
	            sb.append(offset);
	            sb.append(" ");
	            sb.append(limit);
	            sb.append(" ");
	            sb.append(COLUMNS);
	            break;
	        case DB2:
	            sb.append("* FROM ( SELECT ");
	            sb.append(COLUMNS);
	            sb.append(", ROW_NUMBER() OVER (");
	            appendOrderBy(sb, orderBy, orderDir);
	            sb.append(") AS rownum ");
	            break;
	        case ORACLE:
	            sb.append("* FROM ( SELECT ");
	            sb.append(COLUMNS);
	            sb.append(", ROWNUM as r1 FROM ( SELECT ");
	            sb.append(COLUMNS);
	            break;
	        case MSSQL:
	            sb.append("* FROM ( SELECT TOP ").append(limit).append(" ");
	            sb.append(COLUMNS);
	            sb.append(" FROM ( SELECT TOP ").append(limit+offset).append(" ");
	            sb.append(COLUMNS);
	            break;
	        default:
	            sb.append(COLUMNS);
	            break;
	        }
        } else {
            sb.append(COLUMNS);            
        }
        appendFromWhere(sb, type, host, from, to, aet, userName, patientName,
                patientId);
        appendOrderBy(sb, orderBy, orderDir);
        if (limit > 0) {
	        switch (database) {
	        case PSQL:
	        case MYSQL:
	            sb.append(" LIMIT ");
	            sb.append(limit);
	            sb.append(" OFFSET ");
	            sb.append(offset);
	            break;
	        case DB2:
	            sb.append(") AS foo WHERE rownum>");
	            sb.append(offset);
	            sb.append(" AND rownum<=");
	            sb.append(offset + limit);
	            break;
	        case ORACLE:
	            sb.append(") WHERE ROWNUM<= ");
	            sb.append(offset + limit);
	            sb.append(") WHERE r1>");
	            sb.append(offset);
	            break;
	        case MSSQL:
	            sb.append(") AS temp1");
	            appendOrderBy(sb, orderBy, invertOrderBy(orderDir));
	            sb.append(") AS temp2");
	            appendOrderBy(sb, orderBy, orderDir);
	            break;
	        }
        }
        return sb.toString();
    }

    /**
	 * @param orderDir
	 * @return
	 */
	private String[] invertOrderBy(String[] orderDir) {
		
		String[] inverted = new String[ orderDir.length ];
		for ( int i=0 ; i < orderDir.length ; i++) {
			inverted[i] = "ASC".equalsIgnoreCase(orderDir[i]) ? "DESC" : "ASC";
		}
		return inverted;
	}

	private StringBuffer appendOrderBy(StringBuffer sb, String[] orderBy,
            String[] orderDir) {
        if (orderBy != null) {
            for (int i = 0; i < orderBy.length; i++) {
                sb.append(i == 0 ? " ORDER BY " : ", ")
						.append(TO_COLUMN.get(orderBy[i]))
                        .append(" ").append(orderDir[i]);
            }
        }
        return sb;
    }

    private StringBuffer appendFromWhere(StringBuffer sb, String[] type,
            String host, String from, String to, String aet, String userName,
            String patientName, String patientId) {
        sb.append(" FROM audit_record WHERE");
        if (type != null && type.length > 0) {
            sb.append('(');
            for (int i = 0; i < type.length; i++) {
                sb.append(" audit_record.msg_type='").append(
                        type[i].replaceAll("'", "''")).append("' OR");
            }
            sb.setLength(sb.length() - 3);
            sb.append(") AND");
        }
        if (host.length() != 0) {
            sb.append(" UPPER(host_name)=UPPER('").append(
                    host.replaceAll("'", "''")).append("') AND");
        }
        boolean matchFrom = from.length() != 0;
        boolean matchTo = to.length() != 0;
        if (matchFrom && matchTo) {
            sb.append(" time_stamp BETWEEN '").append(
                    from.replaceAll("'", "''")).append("' AND '").append(
                    to.replaceAll("'", "''")).append("' AND");
        } else if (matchFrom) {
            sb.append(" time_stamp>='").append(from.replaceAll("'", "''"))
                    .append("' AND");
        } else if (matchTo) {
            sb.append(" time_stamp<='").append(to.replaceAll("'", "''"))
                    .append("' AND");
        }
        if (aet.length() != 0) {
            sb.append(" aet='").append(aet.replaceAll("'", "''")).append(
                    "' AND");
        }
        if (userName.length() != 0) {
            sb.append(" user_name='").append(userName.replaceAll("'", "''"))
                    .append("' AND");
        }
        if (patientName.length() != 0) {
            sb.append(" UPPER(patient_name)=UPPER('").append(
                    patientName.replaceAll("'", "''")).append("') AND");
        }
        if (patientId.length() != 0) {
            sb.append(" patient_id='").append(patientId.replaceAll("'", "''"))
                    .append("' AND");
        }
        sb.setLength(sb.length()
                - ((sb.charAt(sb.length() - 1) == 'D') ? 4 : 6));
        return sb;
    }

    private String maskNull(String s) {
        return s != null ? s.trim() : "";
    }

    private String toXML(String[] type, String host, String from, String to,
            String aet, String userName, String patientName, String patientId,
            String[] orderBy, String[] orderDir, ResultSet rs, int start,
            int pageSize, int ntot) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("<query type=\"");
        if (type != null) {
            for (int i = 0; i < type.length; i++)
                sb.append(type[i]).append(",");
            sb.setLength(sb.length() - 1);
        }
        String orderDirType = "", orderDirHost = "", orderDirTimestamp = "";
        if (orderBy != null) {
            for (int i = 0; i < orderBy.length; i++) {
                if (orderBy[i].equalsIgnoreCase(ORDER_BY_TYPE)) {
                    orderDirType = orderDir[i];
                } else if (orderBy[i].equalsIgnoreCase(ORDER_BY_HOST)) {
                    orderDirHost = orderDir[i];
                } else if (orderBy[i].equalsIgnoreCase(ORDER_BY_TIMESTAMP)) {
                    orderDirTimestamp = orderDir[i];
                }
            }
        }
        sb.append("\" host=\"").append(host).append("\" from=\"").append(from)
                .append("\" to=\"").append(to).append("\" start=\"").append(
                        start).append("\" pagesize=\"").append(pageSize)
                .append("\" aet=\"").append(aet).append("\" username=\"")
                .append(userName).append("\" patientname=\"").append(
                        patientName).append("\" patientid=\"")
                .append(patientId).append("\" sortdir-type=\"").append(
                        orderDirType).append("\" sortdir-host=\"").append(
                        orderDirHost).append("\" sortdir-timestamp=\"").append(
                        orderDirTimestamp);
        int n = 0; //to count number of records in this fetch
        StringBuffer sbRecs = new StringBuffer();
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);        
        while (rs.next()) {
            sbRecs.append("\n<record pk=\"").append(rs.getInt(1));
            sbRecs.append("\" type=\"").append(maskNull(rs.getString(2)));
            sbRecs.append("\" host=\"").append(maskNull(rs.getString(3)));            
            sbRecs.append("\" timestamp=\"");
            Date ts = rs.getTimestamp(4);
            if (ts != null) {
            	sbRecs.append(df.format(ts));
            }
            sbRecs.append("\" aet=\"").append(maskNull(rs.getString(5)));
            sbRecs.append("\" username=\"").append(maskNull(rs.getString(6)));
            sbRecs.append("\" patientname=\"")
                    .append(maskNull(rs.getString(7)));
            sbRecs.append("\" patientid=\"").append(maskNull(rs.getString(8)));
            sbRecs.append("\" />");
            ++n;
        }
        sb.append("\" nrecords=\"").append(n);
        sb.append("\" tot-nrecords=\"").append(ntot);
        sb.append("\" eof=\"").append(start + pageSize >= ntot).append("\">");
        sb.append(sbRecs);
        sb.append("\n</query>");
        if (log.isDebugEnabled()) {
            log.debug("Found " + n + " matching Audit Records:\n"
                    + sb.toString());
        }
        return sb.toString();
    }

}
