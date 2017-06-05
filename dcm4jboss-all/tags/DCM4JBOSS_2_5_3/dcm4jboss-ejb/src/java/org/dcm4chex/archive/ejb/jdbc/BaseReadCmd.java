/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.ejb.jdbc;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class BaseReadCmd extends BaseCmd {
    protected ResultSet rs = null;
    

    protected BaseReadCmd(String dsJndiName, int transactionIsolationLevel)
			throws SQLException {
		super(dsJndiName, transactionIsolationLevel, null);
	}

	protected BaseReadCmd(String dsJndiName, int transactionIsolationLevel,
			String sql) throws SQLException {
		super(dsJndiName, transactionIsolationLevel, sql);
	}
	
	public byte[] getBytes(int column) throws SQLException {
	    ResultSetMetaData meta = rs.getMetaData();
		if (meta != null && meta.getColumnType(column) == java.sql.Types.BLOB) {
			Blob blob = rs.getBlob(column);
			return blob != null ? blob.getBytes(1,(int)blob.length()) : null;
		}
		return rs.getBytes(column);
	}

    public void execute(String sql) throws SQLException {
        if (rs != null) {
            throw new IllegalStateException();
        }		
        log.debug("SQL: " + sql);
        rs = stmt.executeQuery(sql);
    }

    public void execute() throws SQLException {
        if (rs != null) {
            throw new IllegalStateException();
        }		
 		rs = ((PreparedStatement) stmt).executeQuery();
    }
	
    public boolean next() throws SQLException {
        if (rs == null) {
            throw new IllegalStateException();
        }
        return rs.next();
    }

    public void close() {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) {}
            rs = null;
        }
		super.close();
    }
}
