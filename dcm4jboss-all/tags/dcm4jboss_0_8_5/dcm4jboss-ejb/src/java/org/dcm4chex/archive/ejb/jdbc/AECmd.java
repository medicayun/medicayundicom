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

package org.dcm4chex.archive.ejb.jdbc;

import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1023 $
 * @since 22.11.2003
 */
public final class AECmd extends BaseCmd
{

    private static final String[] ENTITY = { "AE" };

    private static final String[] SELECT_ATTRIBUTE =
        { "AE.pk", "AE.title", "AE.hostName", "AE.port", "AE.cipherSuites" };

    private final SqlBuilder sqlBuilder = new SqlBuilder();

    public AECmd(DataSource ds, String aet) throws SQLException
    {
        super(ds);
        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
        sqlBuilder.setFrom(ENTITY);
        sqlBuilder.addSingleValueMatch("AE.title", SqlBuilder.TYPE1, aet);
    }

    public AEData execute() throws SQLException
    {
        try
        {
            execute(sqlBuilder.getSql());
            return next()
                ? new AEData(
                	rs.getInt(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getInt(4),
                    rs.getString(5))
                : null;
        } finally
        {
            close();
        }
    }
}
