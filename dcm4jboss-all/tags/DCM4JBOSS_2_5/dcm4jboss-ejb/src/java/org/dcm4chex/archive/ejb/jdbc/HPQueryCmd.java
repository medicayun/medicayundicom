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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.HPLevel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1909 $ $Date: 2005-08-18 01:24:52 +0800 (周四, 18 8月 2005) $
 * @since Aug 17, 2005
 */
public class HPQueryCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] FROM = { "HP" };

    private static final String[] SELECT = { "HP.encodedAttributes" };

    private static final String USER_CODE = "user_code";
    private static final String REGION_CODE = "region_code";
    private static final String PROC_CODE = "proc_code";
    private static final String REASON_CODE = "reason_code";
    
    private final SqlBuilder sqlBuilder = new SqlBuilder();

    private final Dataset keys;
    
    public HPQueryCmd(Dataset keys) throws SQLException {
		super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
		String s;
		int i;
		this.keys = keys;
		// ensure keys contains (8,0005) for use as result filter
		if (!keys.contains(Tags.SpecificCharacterSet)) {
			keys.putCS(Tags.SpecificCharacterSet);
		}
		sqlBuilder.setSelect(SELECT);
		sqlBuilder.setFrom(FROM);
		sqlBuilder.setLeftJoin(getLeftJoin());
		sqlBuilder.addListOfUidMatch(null, "HP.sopIuid", SqlBuilder.TYPE1, keys
				.getStrings(Tags.SOPInstanceUID));
		sqlBuilder.addListOfUidMatch(null, "HP.sopCuid", SqlBuilder.TYPE1, keys
				.getStrings(Tags.SOPClassUID));
		sqlBuilder.addWildCardMatch(null, "HP.hangingProtocolName",
				SqlBuilder.TYPE2, keys.getString(Tags.HangingProtocolName),
				false);
		if ((s = keys.getString(Tags.HangingProtocolLevel)) != null) {
			sqlBuilder.addIntValueMatch(null, "HP.hangingProtocolLevelAsInt",
					SqlBuilder.TYPE1, HPLevel.toInt(s));
		}
		if ((i = keys.getInt(Tags.NumberOfPriorsReferenced, -1)) != -1) {
			sqlBuilder.addIntValueMatch(null, "HP.numberOfPriorsReferenced",
					SqlBuilder.TYPE1, i);
		}
		if ((i = keys.getInt(Tags.NumberOfScreens, -1)) != -1) {
			sqlBuilder.addIntValueMatch(null, "HP.numberOfScreens",
					SqlBuilder.TYPE2, i);
		}
		sqlBuilder.addWildCardMatch(null, "HP.hangingProtocolUserGroupName",
				SqlBuilder.TYPE2, keys
						.getString(Tags.HangingProtocolUserGroupName), false);
		addCodeMatch(keys
				.getItem(Tags.HangingProtocolUserIdentificationCodeSeq),
				USER_CODE);
		Dataset item = keys.getItem(Tags.HangingProtocolDefinitionSeq);
		if (item != null) {
			sqlBuilder.addWildCardMatch(null, "HPDefinition.modality",
					SqlBuilder.TYPE2, item.getString(Tags.Modality), false);
			sqlBuilder.addWildCardMatch(null, "HPDefinition.laterality",
					SqlBuilder.TYPE2, item.getString(Tags.Laterality), false);
			addCodeMatch(item.getItem(Tags.AnatomicRegionSeq), REGION_CODE);
			addCodeMatch(item.getItem(Tags.ProcedureCodeSeq), PROC_CODE);
			addCodeMatch(item.getItem(Tags.ReasonforRequestedProcedureCodeSeq),
					REASON_CODE);
		}
	}

    private void addCodeMatch(Dataset item, String alias) {
        if (item != null) {
            sqlBuilder.addSingleValueMatch(alias, "Code.codeValue",
                    SqlBuilder.TYPE2,
                    item.getString(Tags.CodeValue));
            sqlBuilder.addSingleValueMatch(alias, "Code.codingSchemeDesignator",
                    SqlBuilder.TYPE2,
                    item.getString(Tags.CodingSchemeDesignator));
        }
    }

    private boolean isMatchCode(Dataset code) {
        return code != null
                && (code.vm(Tags.CodeValue) > 0 || code
                        .vm(Tags.CodingSchemeDesignator) > 0);
    }

    private String[] getLeftJoin() {
		ArrayList list = new ArrayList();
		if (isMatchCode(keys
				.getItem(Tags.HangingProtocolUserIdentificationCodeSeq))) {
			list.add("Code");
			list.add(USER_CODE);
			list.add("HP.user_fk");
			list.add("Code.pk");
		}
		Dataset item = keys.getItem(Tags.HangingProtocolDefinitionSeq);
		if (item != null && !item.isEmpty()) {
			sqlBuilder.setDistinct(true);
			if (isMatchCode(item.getItem(Tags.AnatomicRegionSeq))) {
				list.add("rel_hpdef_region");
				list.add(null);
				list.add("HPDefinition.pk");
				list.add("rel_hpdef_region.hpdef_fk");
				list.add("Code");
				list.add(REGION_CODE);
				list.add("rel_hpdef_region.region_fk");
				list.add("Code.pk");
			}
			if (isMatchCode(item.getItem(Tags.ProcedureCodeSeq))) {
				list.add("rel_hpdef_proc");
				list.add(null);
				list.add("HPDefinition.pk");
				list.add("rel_hpdef_proc.hpdef_fk");
				list.add("Code");
				list.add(PROC_CODE);
				list.add("rel_hpdef_proc.proc_fk");
				list.add("Code.pk");
			}
			if (isMatchCode(item.getItem(Tags.ReasonforRequestedProcedureCodeSeq))) {
				list.add("rel_hpdef_reason");
				list.add(null);
				list.add("HPDefinition.pk");
				list.add("rel_hpdef_reason.hpdef_fk");
				list.add("Code");
				list.add(REASON_CODE);
				list.add("rel_hpdef_reason.reason_fk");
				list.add("Code.pk");
			}
		}
		return (String[]) (list.isEmpty() ? null : list.toArray(new String[list
				.size()]));
	}

    public void execute() throws SQLException {
        execute(sqlBuilder.getSql());
    }

    public Dataset getDataset() throws SQLException {
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        DatasetUtils.fromByteArray( getBytes(1), DcmDecodeParam.EVR_LE, ds);
        QueryCmd.adjustDataset(ds, keys);
        return ds.subSet(keys);
    }
	
}
