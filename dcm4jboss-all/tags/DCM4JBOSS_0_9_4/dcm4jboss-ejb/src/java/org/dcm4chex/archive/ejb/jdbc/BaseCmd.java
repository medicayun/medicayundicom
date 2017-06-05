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
 * CVS Revision: $Revision: 1214 $
 * Last CVS Commit: $Date: 2004-11-23 19:12:27 +0800 (周二, 23 11月 2004) $
 * Author of last CVS Commit: $Author: gunterze $
 */
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

    protected Connection con;
    protected Statement stmt;
    protected ResultSet rs = null;
    protected int prevLevel = 0;

    protected BaseCmd(DataSource ds, int transactionIsolationLevel) throws SQLException {
        InitialContext ctx = null;
        try {
            ctx = new InitialContext();
            con = ds.getConnection();
            prevLevel = con.getTransactionIsolation();
            if (transactionIsolationLevel > 0)
                con.setTransactionIsolation(transactionIsolationLevel);
            stmt = con.createStatement();
        } catch (SQLException e) {
            close();
            throw e;
        } catch (NamingException e) {
            throw new SQLException(e.getMessage());
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ignore) {}
            }
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
