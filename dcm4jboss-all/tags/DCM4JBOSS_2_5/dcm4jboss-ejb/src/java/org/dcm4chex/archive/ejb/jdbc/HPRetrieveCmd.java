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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.DatasetUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1928 $ $Date: 2005-08-30 22:47:03 +0800 (周二, 30 8月 2005) $
 * @since Aug 22, 2005
 */
public class HPRetrieveCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] FROM = { "HP" };

    private static final String[] SELECT = { "HP.encodedAttributes" };

    private final SqlBuilder sqlBuilder = new SqlBuilder();

    public HPRetrieveCmd(Dataset keys) throws SQLException {
		super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
		sqlBuilder.setSelect(SELECT);
		sqlBuilder.setFrom(FROM);
		sqlBuilder.addListOfUidMatch(null, "HP.sopIuid", SqlBuilder.TYPE1,
				keys.getStrings(Tags.SOPInstanceUID));
	}
	
	public List getDatasets() throws SQLException {
		ArrayList result = new ArrayList();
		try {
	        execute(sqlBuilder.getSql());
			while (next()) {
				result.add(DatasetUtils.fromByteArray(
						getBytes(1), DcmDecodeParam.EVR_LE, null));			
			}
		} finally {
			close();
		}
		return result;
    }
	
}
