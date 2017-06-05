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
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4chex.archive.common.DatasetUtils;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1736 $ $Date: 2005-05-12 14:11:16 +0800 (周四, 12 5月 2005) $
 * @since 10.02.2004
 */
public class MPPSQueryCmd extends BaseCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] FROM = { "Patient", "MPPS"};

    private static final String[] SELECT = { "Patient.encodedAttributes",
            "MPPS.encodedAttributes"};

    private static final String[] RELATIONS = { "Patient.pk",
    		"MPPS.patient_fk"};

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private final SqlBuilder sqlBuilder = new SqlBuilder();

    /**
     * @param ds
     * @throws SQLException
     */
    public MPPSQueryCmd(MPPSFilter filter) throws SQLException {
        super(transactionIsolationLevel);
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
        Dataset ds = dof.newDataset();       
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
        DatasetUtils.fromByteArray( isBlob ? blob1.getBytes(1,(int)blob1.length()) : rs.getBytes(1),
                DcmDecodeParam.EVR_LE, ds);
        DatasetUtils.fromByteArray( isBlob ? blob2.getBytes(1,(int)blob2.length()) : rs.getBytes(2),
                DcmDecodeParam.EVR_LE, ds);
        return ds;
    }
    
 
}