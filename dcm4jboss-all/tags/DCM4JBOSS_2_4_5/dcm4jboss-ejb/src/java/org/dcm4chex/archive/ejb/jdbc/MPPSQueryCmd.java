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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4chex.archive.common.DatasetUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1829 $ $Date: 2005-06-30 22:56:04 +0800 (周四, 30 6月 2005) $
 * @since 10.02.2004
 */
public class MPPSQueryCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] FROM = { "Patient", "MPPS"};

    private static final String[] SELECT = { "Patient.encodedAttributes",
            "MPPS.encodedAttributes"};

    private static final String[] RELATIONS = { "Patient.pk",
    		"MPPS.patient_fk"};

    private final SqlBuilder sqlBuilder = new SqlBuilder();

    /**
     * @param ds
     * @throws SQLException
     */
    public MPPSQueryCmd(MPPSFilter filter) throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
        // ensure keys contains (8,0005) for use as result filter
        sqlBuilder.setSelect(SELECT);
        sqlBuilder.setFrom(FROM);
        sqlBuilder.setRelations(RELATIONS);
        sqlBuilder.addSingleValueMatch(null, "MPPS.sopIuid",
                SqlBuilder.TYPE1,
                filter.getSopIuid() );
        if ( filter.isEmptyAccNo() ) {
        	sqlBuilder.addNULLValueMatch(null, "MPPS.accessionNumber", false );
        } else {
	        sqlBuilder.addSingleValueMatch(null, "MPPS.accessionNumber",
	                SqlBuilder.TYPE2,
	                filter.getAccessionNumber() );
        }
        sqlBuilder.addSingleValueMatch(null, "Patient.patientId",
                SqlBuilder.TYPE1,
                filter.getPatientID() );
        sqlBuilder.addWildCardMatch(null, "Patient.patientName",
                SqlBuilder.TYPE1,
                filter.getPatientName(),
                true);
        sqlBuilder.addSingleValueMatch(null, "MPPS.modality",
                SqlBuilder.TYPE1,
                filter.getModality());
        sqlBuilder.addSingleValueMatch(null, "MPPS.performedStationAET",
                SqlBuilder.TYPE1,
                filter.getStationAET());
        sqlBuilder.addRangeMatch(null, "MPPS.ppsStartDateTime",
                SqlBuilder.TYPE1,
                filter.dateTimeRange());
        sqlBuilder.addSingleValueMatch(null, "MPPS.ppsStatusAsInt",
                SqlBuilder.TYPE1,
				filter.getStatus());
    }

    public void execute() throws SQLException {
        execute(sqlBuilder.getSql());
    }

    public Dataset getDataset() throws SQLException {
        Dataset ds = DcmObjectFactory.getInstance().newDataset();       
        DatasetUtils.fromByteArray( getBytes(1), DcmDecodeParam.EVR_LE, ds);
        DatasetUtils.fromByteArray( getBytes(2), DcmDecodeParam.EVR_LE, ds);
        return ds;
    }
    
 
}