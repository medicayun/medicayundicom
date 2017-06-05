/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.ejb.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class BaseCmd {
    protected static final Logger log = Logger.getLogger(BaseCmd.class);
    protected DataSource ds;
    protected Connection con;
    protected Statement stmt;
    protected int prevLevel = 0;
    

    protected BaseCmd(String dsJndiName, int transactionIsolationLevel, String sql)
			throws SQLException {
        try {
            Context jndiCtx = new InitialContext();
            try {
                ds = (DataSource) jndiCtx.lookup(dsJndiName);
            } finally {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        } catch (NamingException ne) {
            throw new RuntimeException(
                    "Failed to access Data Source: " + dsJndiName, ne);
        }
        try {
            con = ds.getConnection();
            prevLevel = con.getTransactionIsolation();
            if (transactionIsolationLevel > 0)
                con.setTransactionIsolation(transactionIsolationLevel);
			if (sql != null) {
		        log.debug("SQL: " + sql);
	            stmt = con.prepareStatement(sql);
			} else {
				stmt = con.createStatement();
			} 
        } catch (SQLException e) {
            close();
            throw e;
        }
    }
	
    public void close() {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) {}
            stmt = null;
        }
        if (con != null) {
            try {
                con.setTransactionIsolation(prevLevel);
            } catch (SQLException ignore) {}
            try {
                con.close();
            } catch (SQLException ignore) {}
            con = null;
        }
    }
}
