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
import java.util.ArrayList;
import java.util.List;

import org.dcm4che.data.DcmObjectFactory;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2082 $ $Date: 2005-11-21 20:40:23 +0800 (周一, 21 11月 2005) $
 * @since 14.01.2004
 */
public class QueryAllStudiesCmd extends BaseCmd {

    public static int transactionIsolationLevel = 0;

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final String[] SELECT_ATTRIBUTE = { "Study.pk" };

    private static final String[] ENTITY = { "Study"};

    private final SqlBuilder sqlBuilder = new SqlBuilder();
    
    public QueryAllStudiesCmd()
            throws SQLException {
        super(transactionIsolationLevel);
        sqlBuilder.setFrom(ENTITY);
    }

    public int count() throws SQLException {
        try {
            sqlBuilder.setSelectCount();
            execute(sqlBuilder.getSql());
            next();
            return rs.getInt(1);
        } finally {
        	rs = null;
        }
    }

    public List list(int offset, int limit) throws SQLException {
        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
        sqlBuilder.addOrderBy("Study.pk", SqlBuilder.ASC);
        sqlBuilder.setOffset(offset);
        sqlBuilder.setLimit(limit);
        try {
            execute(sqlBuilder.getSql());
            ArrayList result = new ArrayList();
            while (next()) {
                 result.add(new Integer(rs.getInt(1)));
            }
            return result;
        } finally {
        	rs = null;
        }
    }
}