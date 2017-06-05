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
import java.util.ArrayList;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.GPSPSPriority;
import org.dcm4chex.archive.common.GPSPSStatus;
import org.dcm4chex.archive.common.InputAvailabilityFlag;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-05-12 14:11:16 +0800 (周四, 12 5月 2005) $
 * @since 02.04.2005
 */

public class GPWLQueryCmd extends BaseCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] FROM = { "Patient", "GPSPS"};

    private static final String[] SELECT = { "Patient.encodedAttributes",
            "GPSPS.encodedAttributes"};

    private static final String[] RELATIONS = { "Patient.pk",
            "GPSPS.patient_fk"};
    
    private static final String ITEM_CODE = "item_code";
    private static final String APP_CODE = "app_code";
    private static final String DEVNAME_CODE = "devname_code";
    private static final String DEVCLASS_CODE = "devclass_code";
    private static final String DEVLOC_CODE = "devloc_code";
    private static final String PERF_CODE = "perf_code";
    
    private final SqlBuilder sqlBuilder = new SqlBuilder();

    private final Dataset keys;
    
    public GPWLQueryCmd(Dataset keys) throws SQLException {
        super(transactionIsolationLevel);
        String s;
        this.keys = keys;
        // ensure keys contains (8,0005) for use as result filter
        if (!keys.contains(Tags.SpecificCharacterSet)) {
            keys.putCS(Tags.SpecificCharacterSet);
        }
        sqlBuilder.setSelect(SELECT);
        sqlBuilder.setFrom(FROM);
        sqlBuilder.setRelations(RELATIONS);
        sqlBuilder.setLeftJoin(getLeftJoin());
        sqlBuilder.addListOfUidMatch(null, "GPSPS.sopIuid",
                SqlBuilder.TYPE1,
                keys.getStrings(Tags.SOPInstanceUID));
        if ((s = keys.getString(Tags.GPSPSStatus)) != null) {
            sqlBuilder.addIntValueMatch(null, "GPSPS.gpspsStatusAsInt",
                    SqlBuilder.TYPE1,
                    GPSPSStatus.toInt(s));
        }
        if ((s = keys.getString(Tags.InputAvailabilityFlag)) != null) {
            sqlBuilder.addIntValueMatch(null, "GPSPS.inputAvailabilityFlagAsInt",
                    SqlBuilder.TYPE1,
                    InputAvailabilityFlag.toInt(s));
        }
        s = keys.getString(Tags.GPSPSPriority);
        if (s != null) {
            sqlBuilder.addIntValueMatch(null, "GPSPS.gpspsPriorityAsInt",
                    SqlBuilder.TYPE1,
                    GPSPSPriority.toInt(s));
        }
        sqlBuilder.addRangeMatch(null, "GPSPS.spsStartDateTime",
                SqlBuilder.TYPE1,
                keys.getDateRange(Tags.SPSStartDateAndTime));
        sqlBuilder.addRangeMatch(null, "GPSPS.expectedCompletionDateTime",
                SqlBuilder.TYPE2,
                keys.getDateRange(Tags.ExpectedCompletionDateAndTime));        
        addCodeMatch(Tags.ScheduledWorkitemCodeSeq, ITEM_CODE);
        addCodeMatch(Tags.ScheduledProcessingApplicationsCodeSeq, APP_CODE);
        addCodeMatch(Tags.ScheduledStationNameCodeSeq, DEVNAME_CODE);
        addCodeMatch(Tags.ScheduledStationClassCodeSeq, DEVCLASS_CODE);
        addCodeMatch(Tags.ScheduledStationGeographicLocationCodeSeq, DEVLOC_CODE);
        Dataset item = keys.getItem(Tags.ScheduledHumanPerformersSeq);
        if (item != null) {
            sqlBuilder.addWildCardMatch(null,
                    "GPSPSPerformer.humanPerformerName",
                    SqlBuilder.TYPE2,
                    item.getString(Tags.HumanPerformerName),
                    true);
            addCodeMatch(item.getItem(Tags.HumanPerformerCodeSeq), PERF_CODE);
        }
        item = keys.getItem(Tags.RefRequestSeq);
        if (item != null) {
            sqlBuilder.addSingleValueMatch(null,
                    "GPSPSRequest.requestedProcedureId",
                    SqlBuilder.TYPE2,
                    item.getString(Tags.RequestedProcedureID));
            sqlBuilder.addSingleValueMatch(null,
                    "GPSPSRequest.accessionNumber",
                    SqlBuilder.TYPE2,
                    item.getString(Tags.AccessionNumber));
        }        
        sqlBuilder.addSingleValueMatch(null, "Patient.patientId",
                SqlBuilder.TYPE1,
                keys.getString(Tags.PatientID));
        sqlBuilder.addWildCardMatch(null, "Patient.patientName",
                SqlBuilder.TYPE1,
                keys.getString(Tags.PatientName),
                true);
    }

    private void addCodeMatch(int tag, String alias) {
        addCodeMatch(keys.getItem(tag), alias);
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

    private boolean isMatchCode(int tag) {
        return isMatchCode(keys.getItem(tag));
    }

    private boolean isMatchCode(Dataset code) {
        return code != null
                && (code.vm(Tags.CodeValue) > 0 || code
                        .vm(Tags.CodingSchemeDesignator) > 0);
    }

    private boolean isMatchRefRequest() {
        Dataset refrq = keys.getItem(Tags.RefRequestSeq);
        return refrq != null
                && (refrq.vm(Tags.RequestedProcedureID) > 0
                        || refrq.vm(Tags.AccessionNumber) > 0);
    }
    
    private String[] getLeftJoin() {
        ArrayList list = new ArrayList();
        if (isMatchCode(Tags.ScheduledWorkitemCodeSeq)) {
            list.add("Code");
            list.add(ITEM_CODE);
            list.add("GPSPS.code_fk");
            list.add("Code.pk");
        }
        if (isMatchCode(Tags.ScheduledProcessingApplicationsCodeSeq)) {
            sqlBuilder.setDistinct(true);
            list.add("rel_gpsps_appcode");
            list.add(null);
            list.add("GPSPS.pk");
            list.add("rel_gpsps_appcode.gpsps_fk");
            list.add("Code");
            list.add(APP_CODE);
            list.add("rel_gpsps_appcode.code_fk");
            list.add("Code.pk");
        }
        if (isMatchCode(Tags.ScheduledStationNameCodeSeq)) {
            sqlBuilder.setDistinct(true);
            list.add("rel_gpsps_devname");
            list.add(null);
            list.add("GPSPS.pk");
            list.add("rel_gpsps_devname.gpsps_fk");
            list.add("Code");
            list.add(DEVNAME_CODE);
            list.add("rel_gpsps_devname.code_fk");
            list.add("Code.pk");
        }
        if (isMatchCode(Tags.ScheduledStationClassCodeSeq)) {
            sqlBuilder.setDistinct(true);
            list.add("rel_gpsps_devclass");
            list.add(null);
            list.add("GPSPS.pk");
            list.add("rel_gpsps_devclass.gpsps_fk");
            list.add("Code");
            list.add(DEVCLASS_CODE);
            list.add("rel_gpsps_devclass.code_fk");
            list.add("Code.pk");
        }
        if (isMatchCode(Tags.ScheduledStationGeographicLocationCodeSeq)) {
            sqlBuilder.setDistinct(true);
            list.add("rel_gpsps_devloc");
            list.add(null);
            list.add("GPSPS.pk");
            list.add("rel_gpsps_devloc.gpsps_fk");
            list.add("Code");
            list.add(DEVLOC_CODE);
            list.add("rel_gpsps_devloc.code_fk");
            list.add("Code.pk");
        }
        Dataset item = keys.getItem(Tags.ScheduledHumanPerformersSeq);
        if (item != null) {
            boolean matchCode = isMatchCode(item.getItem(Tags.HumanPerformerCodeSeq));
            if (matchCode || item.vm(Tags.HumanPerformerName) > 0) {
                sqlBuilder.setDistinct(true);
                list.add("GPSPSPerformer");
                list.add(null);
                list.add("GPSPS.pk");
                list.add("GPSPSPerformer.gpsps_fk");
                if (matchCode) {
                    list.add("Code");
                    list.add(PERF_CODE);
                    list.add("GPSPSPerformer.code_fk");
                    list.add("Code.pk");
                }
            }
        }
        if (isMatchRefRequest()) {
            sqlBuilder.setDistinct(true);
            list.add("GPSPSRequest");
            list.add(null);
            list.add("GPSPS.pk");
            list.add("GPSPSRequest.gpsps_fk");
        }
        return (String[]) (list.isEmpty() ? null
                : list.toArray(new String[list.size()]));
    }

    public void execute() throws SQLException {
        execute(sqlBuilder.getSql());
    }

    public Dataset getDataset() throws SQLException {
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        
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
        QueryCmd.adjustDataset(ds, keys);
        return ds.subSet(keys);
    }
}
