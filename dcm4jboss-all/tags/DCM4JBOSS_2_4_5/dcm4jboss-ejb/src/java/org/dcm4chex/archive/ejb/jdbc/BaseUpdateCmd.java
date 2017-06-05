/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.ejb.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 *
 */
public abstract class BaseUpdateCmd extends BaseCmd {    

    protected BaseUpdateCmd(String dsJndiName, int transactionIsolationLevel,
			String sql) throws SQLException {
		super(dsJndiName, transactionIsolationLevel, sql);
    }
	
    public int execute() throws SQLException {
        return ((PreparedStatement) stmt).executeUpdate();
    }
}
