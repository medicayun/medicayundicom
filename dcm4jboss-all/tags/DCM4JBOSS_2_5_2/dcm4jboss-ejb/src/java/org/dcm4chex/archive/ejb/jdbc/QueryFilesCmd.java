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

import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.MD5;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1798 $ $Date: 2005-06-22 20:47:02 +0800 (周三, 22 6月 2005) $
 * @since 06.12.2004
 */
public final class QueryFilesCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] SELECT_ATTRIBUTE = { "File.filePath",
            "File.fileMd5Field", "FileSystem.directoryPath",
            "FileSystem.retrieveAET" };

    private static final String[] ENTITY = { "Instance", "File", "FileSystem" };

    private static final String[] RELATIONS = { "Instance.pk",
            "File.instance_fk", "File.filesystem_fk", "FileSystem.pk" };

    public QueryFilesCmd(String iuid) throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
        sqlBuilder.setFrom(ENTITY);
        sqlBuilder.setRelations(RELATIONS);
        sqlBuilder.addSingleValueMatch(null, "Instance.sopIuid",
                SqlBuilder.TYPE1, iuid);
		execute(sqlBuilder.getSql());
    }
    
    public FileDTO getFileDTO() throws SQLException {
        FileDTO dto = new FileDTO();
        dto.setFilePath(rs.getString(1));
        dto.setFileMd5(MD5.toBytes(rs.getString(2)));
        dto.setDirectoryPath(rs.getString(3));
        dto.setRetrieveAET(rs.getString(4));
		return dto;
    }

}
