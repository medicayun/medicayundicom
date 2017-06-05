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

import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.MD5;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1276 $ $Date: 2004-12-08 01:55:57 +0800 (周三, 08 12月 2004) $
 * @since 06.12.2004
 */
public final class QueryFilesCmd extends BaseCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] SELECT_ATTRIBUTE = { "File.filePath",
            "File.fileMd5Field", "FileSystem.directoryPath",
            "FileSystem.retrieveAET" };

    private static final String[] ENTITY = { "Instance", "File", "FileSystem" };

    private static final String[] RELATIONS = { "Instance.pk",
            "File.instance_fk", "File.filesystem_fk", "FileSystem.pk" };

    private final String sql;
        
    public QueryFilesCmd(String iuid) throws SQLException {
        super(transactionIsolationLevel);
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
        sqlBuilder.setFrom(ENTITY);
        sqlBuilder.setRelations(RELATIONS);
        sqlBuilder.addSingleValueMatch("Instance.sopIuid", SqlBuilder.TYPE1,
                iuid);
        this.sql = sqlBuilder.getSql();
    }
    
    public List execute() throws SQLException {
        ArrayList result = new ArrayList();
        try {
            execute(sql);
            while (next()) {
                FileDTO dto = new FileDTO();
                dto.setFilePath(rs.getString(1));
                dto.setFileMd5(MD5.toBytes(rs.getString(2)));
                dto.setDirectoryPath(rs.getString(3));
                dto.setRetrieveAET(rs.getString(4));
                result.add(dto);
            }
        } finally {
            close();
        }
        return result;        
    }

}
