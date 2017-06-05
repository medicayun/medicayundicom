/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.SQLException;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1798 $
 * @since 22.11.2003
 */
public final class AECmd extends BaseReadCmd
{

    public static int transactionIsolationLevel = 0;

    private static final String[] ENTITY = { "AE" };

    private static final String[] SELECT_ATTRIBUTE =
        { "AE.pk", "AE.title", "AE.hostName", "AE.port", "AE.cipherSuites" };

    public AECmd(String aet) throws SQLException
    {
        super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
		SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
        sqlBuilder.setFrom(ENTITY);
        sqlBuilder.addSingleValueMatch(null, "AE.title", SqlBuilder.TYPE1, aet);
		execute(sqlBuilder.getSql());
    }

    public AEData getAEData() throws SQLException
    {
        try
        {
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
