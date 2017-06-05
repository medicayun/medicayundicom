/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.jdbc;

import java.sql.Blob;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.SPSStatus;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1736 $ $Date: 2005-05-12 14:11:16 +0800 (周四, 12 5月 2005) $
 * @since 10.02.2004
 */
public class MWLQueryCmd extends BaseCmd {


    public static int transactionIsolationLevel = 0;

    private static final String[] FROM = { "Patient", "MWLItem"};

    private static final String[] SELECT = { "Patient.encodedAttributes",
            "MWLItem.encodedAttributes"};

    private static final String[] RELATIONS = { "Patient.pk",
    		"MWLItem.patient_fk"};

    private static final int[] OPEN_STATI = new int[] { SPSStatus.SCHEDULED, SPSStatus.ARRIVED };

    private final SqlBuilder sqlBuilder = new SqlBuilder();

    private final Dataset keys;

    /**
     * @param ds
     * @throws SQLException
     */
    public MWLQueryCmd(Dataset keys) throws SQLException {
        super(transactionIsolationLevel);
        this.keys = keys;
        // ensure keys contains (8,0005) for use as result filter
        if (!keys.contains(Tags.SpecificCharacterSet)) {
            keys.putCS(Tags.SpecificCharacterSet);
        }
        sqlBuilder.setSelect(SELECT);
        sqlBuilder.setFrom(FROM);
        sqlBuilder.setRelations(RELATIONS);
        Dataset spsItem = keys.getItem(Tags.SPSSeq);
        String status = null;
        if (spsItem != null) {
        	status = spsItem.getString(Tags.SPSStatus);
            sqlBuilder.addSingleValueMatch(null, "MWLItem.spsId",
                    SqlBuilder.TYPE1,
                    spsItem.getString(Tags.SPSID));
            sqlBuilder.addRangeMatch(null, "MWLItem.spsStartDateTime",
                    SqlBuilder.TYPE1,
                    spsItem.getDateTimeRange(Tags.SPSStartDate,
                            Tags.SPSStartTime));
            sqlBuilder.addSingleValueMatch(null, "MWLItem.modality",
                    SqlBuilder.TYPE1,
                    spsItem.getString(Tags.Modality));
            sqlBuilder.addSingleValueMatch(null, "MWLItem.scheduledStationAET",
                    SqlBuilder.TYPE1,
                    spsItem.getString(Tags.ScheduledStationAET));
            sqlBuilder.addWildCardMatch(null, "MWLItem.performingPhysicianName",
                    SqlBuilder.TYPE2,
                    spsItem.getString(Tags.PerformingPhysicianName),
                    false);
        }
    	if (status != null) {
            sqlBuilder.addIntValueMatch(null, "MWLItem.spsStatusAsInt",
                    SqlBuilder.TYPE1,
                    SPSStatus.toInt(status));
    	} else {
    		sqlBuilder.addListOfIntMatch(null, "MWLItem.spsStatusAsInt",
                    SqlBuilder.TYPE1,
                    OPEN_STATI );
    	}
        sqlBuilder.addSingleValueMatch(null, "MWLItem.requestedProcedureId",
                SqlBuilder.TYPE1,
                keys.getString(Tags.RequestedProcedureID));
        sqlBuilder.addSingleValueMatch(null, "MWLItem.accessionNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.AccessionNumber));
        sqlBuilder.addSingleValueMatch(null, "Patient.patientId",
                SqlBuilder.TYPE1,
                keys.getString(Tags.PatientID));
        sqlBuilder.addWildCardMatch(null, "Patient.patientName",
                SqlBuilder.TYPE1,
                keys.getString(Tags.PatientName),
                true);
    }

    public void execute() throws SQLException {
        execute(sqlBuilder.getSql());
    }

    public Dataset getDataset() throws SQLException {
        // Get metadata from resultset
        // If a database does not fully support this command, it will return null
        ResultSetMetaData meta = rs.getMetaData();
        int colType = java.sql.Types.VARCHAR;
        boolean isBlob = false;
    	Blob blob1 = null;
    	Blob blob2 = null;
        if( meta != null ) {
        	if( meta.getColumnType(1) == java.sql.Types.BLOB && 
        		meta.getColumnType(2) == java.sql.Types.BLOB	) {
        		// We know for sure these columns are blobs
        		isBlob = true;
            	blob1 = rs.getBlob(1);
            	blob2 = rs.getBlob(2);
        	}
        }
        Dataset ds = DatasetUtils.fromByteArray( isBlob ? blob1.getBytes(1,(int)blob1.length()) : rs.getBytes(1),
                DcmDecodeParam.EVR_LE, null);
        DatasetUtils.fromByteArray( isBlob ? blob2.getBytes(1,(int)blob2.length()) : rs.getBytes(2),
                DcmDecodeParam.EVR_LE, ds);
        QueryCmd.adjustDataset(ds, keys);
        return ds.subSet(keys);
    }
}