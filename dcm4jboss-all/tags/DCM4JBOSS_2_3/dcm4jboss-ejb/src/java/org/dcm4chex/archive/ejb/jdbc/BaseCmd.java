/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
/* 
 * File: $Source$
 * Author: gunter
 * Date: 20.07.2003
 * Time: 16:22:09
 * CVS Revision: $Revision: 1275 $
 * Last CVS Commit: $Date: 2004-12-07 02:14:03 +0800 (周二, 07 12月 2004) $
 * Author of last CVS Commit: $Author: gunterze $
 */
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
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
    protected static DataSource ds;
    protected Connection con;
    protected Statement stmt;
    protected ResultSet rs = null;
    protected int prevLevel = 0;
    

    protected BaseCmd(int transactionIsolationLevel) throws SQLException {
        if (ds == null) {
            final String dsJndiName = JdbcProperties.getInstance().getDataSource();
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
        }
        try {
            con = ds.getConnection();
            prevLevel = con.getTransactionIsolation();
            if (transactionIsolationLevel > 0)
                con.setTransactionIsolation(transactionIsolationLevel);
            stmt = con.createStatement();
        } catch (SQLException e) {
            close();
            throw e;
        }
    }

    public void execute(String sql) throws SQLException {
        if (rs != null) {
            throw new IllegalStateException();
        }
        log.debug("SQL: " + sql);
        rs = stmt.executeQuery(sql);
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
