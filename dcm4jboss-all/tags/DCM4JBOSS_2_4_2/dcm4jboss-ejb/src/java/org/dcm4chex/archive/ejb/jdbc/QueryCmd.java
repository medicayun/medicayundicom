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
import java.util.Arrays;
import java.util.Iterator;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.DatasetUtils;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1736 $ $Date: 2005-05-12 14:11:16 +0800 (周四, 12 5月 2005) $
 */
public abstract class QueryCmd extends BaseCmd {

    public static int transactionIsolationLevel = 0;

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final String[] QRLEVEL = { "PATIENT", "STUDY", "SERIES",
            "IMAGE"};

    private static final String[] AVAILABILITY = { "ONLINE", "NEARLINE",
            "OFFLINE", "UNAVAILABLE"};

    private static final String SR_CODE = "sr_code";

    private static final String[] SERIES_REQUEST_LEFT_JOIN = {
        "SeriesRequest", null, "Series.pk", "SeriesRequest.series_fk"};
    
    public static QueryCmd create(Dataset keys, boolean filterResult)
            throws SQLException {
        QueryCmd cmd;
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        switch (Arrays.asList(QRLEVEL).indexOf(qrLevel)) {
        case 0:
            cmd = new PatientQueryCmd(keys, filterResult);
            break;
        case 1:
            cmd = new StudyQueryCmd(keys, filterResult);
            break;
        case 2:
            cmd = new SeriesQueryCmd(keys, filterResult);
            break;
        case 3:
            cmd = new ImageQueryCmd(keys, filterResult);
            break;
        default:
            throw new IllegalArgumentException("QueryRetrieveLevel=" + qrLevel);
        }
        cmd.init();
        return cmd;
    }

    protected final Dataset keys;

    protected final SqlBuilder sqlBuilder = new SqlBuilder();

	private final boolean filterResult;

    protected QueryCmd(Dataset keys, boolean filterResult)
    		throws SQLException {
        super(transactionIsolationLevel);
        this.keys = keys;
        this.filterResult = filterResult;
    }

    protected void init() {
        sqlBuilder.setSelect(getSelectAttributes());
        sqlBuilder.setFrom(getTables());
        sqlBuilder.setLeftJoin(getLeftJoin());
        sqlBuilder.setRelations(getRelations());
    }

    protected abstract String[] getSelectAttributes();

    protected abstract String[] getTables();

    protected String[] getLeftJoin() {
        return null;
    }

    protected String[] getRelations() {
        return null;
    }

    public void execute() throws SQLException {
        execute(sqlBuilder.getSql());
    }

    protected void addPatientMatch() {
        sqlBuilder.addWildCardMatch(null, "Patient.patientId", SqlBuilder.TYPE2,
                keys.getString(Tags.PatientID), false);
        sqlBuilder.addWildCardMatch(null, "Patient.patientName",
                SqlBuilder.TYPE2,
                keys.getString(Tags.PatientName),
                true);
        sqlBuilder.addRangeMatch(null, "Patient.patientBirthDate",
                SqlBuilder.TYPE2,
                keys.getDateTimeRange(Tags.PatientBirthDate,
                        Tags.PatientBirthTime));
        sqlBuilder.addWildCardMatch(null, "Patient.patientSex",
                SqlBuilder.TYPE2,
                keys.getString(Tags.PatientSex),
                false);
    }

    protected void addStudyMatch() {
        sqlBuilder.addLiteralMatch(null, "Study.numberOfStudyRelatedSeries",
                SqlBuilder.TYPE1,
                " != 0");
        sqlBuilder.addListOfUidMatch(null, "Study.studyIuid", SqlBuilder.TYPE1,
                keys.getStrings(Tags.StudyInstanceUID));
        sqlBuilder.addWildCardMatch(null, "Study.studyId", SqlBuilder.TYPE2,
                keys.getString(Tags.StudyID), false);
        sqlBuilder.addRangeMatch(null, "Study.studyDateTime", SqlBuilder.TYPE2,
                keys.getDateTimeRange(Tags.StudyDate, Tags.StudyTime));
        sqlBuilder.addWildCardMatch(null, "Study.accessionNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.AccessionNumber),
                false);
        sqlBuilder.addWildCardMatch(null, "Study.referringPhysicianName",
                SqlBuilder.TYPE2,
                keys.getString(Tags.ReferringPhysicianName),
                true);
        sqlBuilder.addModalitiesInStudyMatch(null,
                keys.getString(Tags.ModalitiesInStudy));
    }

    protected void addSeriesMatch() {
        sqlBuilder.addBooleanMatch(null, "Series.hidden", SqlBuilder.TYPE2,
                false);
        sqlBuilder.addListOfUidMatch(null, "Series.seriesIuid",
                SqlBuilder.TYPE1,
                keys.getStrings(Tags.SeriesInstanceUID));
        sqlBuilder.addWildCardMatch(null, "Series.seriesNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.SeriesNumber),
                false);
        sqlBuilder.addWildCardMatch(null, "Series.modality", SqlBuilder.TYPE1,
                keys.getString(Tags.Modality), false);
        sqlBuilder.addRangeMatch(null, "Series.ppsStartDateTime",
                SqlBuilder.TYPE2,
                keys.getDateRange(Tags.PPSStartDate, Tags.PPSStartTime));
        Dataset rqAttrs = keys.getItem(Tags.RequestAttributesSeq);
        if (rqAttrs != null) {
            sqlBuilder.addWildCardMatch(null,
                    "SeriesRequest.requestedProcedureId",
                    SqlBuilder.TYPE2,
                    rqAttrs.getString(Tags.RequestedProcedureID),
                    false);
            sqlBuilder.addWildCardMatch(null,
                    "SeriesRequest.spsId",
                    SqlBuilder.TYPE2,
                    rqAttrs.getString(Tags.SPSID),
                    false);
        }

    }

    protected void addInstanceMatch() {
        sqlBuilder.addListOfUidMatch(null, "Instance.sopIuid", SqlBuilder.TYPE1,
                keys.getStrings(Tags.SOPInstanceUID));
        sqlBuilder.addListOfUidMatch(null, "Instance.sopCuid", SqlBuilder.TYPE1,
                keys.getStrings(Tags.SOPClassUID));
        sqlBuilder.addWildCardMatch(null, "Instance.instanceNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.InstanceNumber),
                false);
        sqlBuilder.addRangeMatch(null, "Instance.contentDateTime", SqlBuilder.TYPE2,
                keys.getDateTimeRange(Tags.ContentDate, Tags.ContentTime));
        sqlBuilder.addWildCardMatch(null, "Instance.srCompletionFlag",
                SqlBuilder.TYPE2,
                keys.getString(Tags.CompletionFlag),
                false);
        sqlBuilder.addWildCardMatch(null, "Instance.srVerificationFlag",
                SqlBuilder.TYPE2,
                keys.getString(Tags.VerificationFlag),
                false);
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        if (code != null) {
            sqlBuilder.addSingleValueMatch(SR_CODE, "Code.codeValue",
                    SqlBuilder.TYPE2,
                    code.getString(Tags.CodeValue));
            sqlBuilder.addSingleValueMatch(SR_CODE, "Code.codingSchemeDesignator",
                    SqlBuilder.TYPE2,
                    code.getString(Tags.CodingSchemeDesignator));
        }
    }

    public Dataset getDataset() throws SQLException {
        Dataset ds = dof.newDataset();
        fillDataset(ds);
        adjustDataset(ds, keys);
        if (!filterResult) return ds;
        keys.putCS(Tags.SpecificCharacterSet);
        keys.putAE(Tags.RetrieveAET);
        keys.putSH(Tags.StorageMediaFileSetID);
        keys.putUI(Tags.StorageMediaFileSetUID);
        keys.putCS(Tags.InstanceAvailability);
        return ds.subSet(keys);
    }

    static void adjustDataset(Dataset ds, Dataset keys) {
        for (Iterator it = keys.iterator(); it.hasNext();) {
            DcmElement key = (DcmElement) it.next();
            final int tag = key.tag();
            if (tag == Tags.SpecificCharacterSet) continue;

            final int vr = key.vr();
            DcmElement el = ds.get(tag);
            if (el == null) {
                el = ds.putXX(tag, vr);
            }
            if (vr == VRs.SQ) {
                Dataset keyItem = key.getItem();
                if (keyItem != null) {
                	if (el.isEmpty()) el.addNewItem();
                    for (int i = 0, n = el.vm(); i < n; ++i) {
                        adjustDataset(el.getItem(i), keyItem);
                    }
                }
            }
        }
    }

    protected abstract void fillDataset(Dataset ds) throws SQLException;

    protected void fillDataset(Dataset ds, int column) throws SQLException {
        // Get metadata from resultset
        // If a database does not fully support this command, it will return null
        ResultSetMetaData meta = rs.getMetaData();
        int colType = java.sql.Types.VARCHAR;
        boolean isBlob = false;
    	Blob blob1 = null;
        if( meta != null ) {
        	if( meta.getColumnType(column) == java.sql.Types.BLOB ) {
        		// We know for sure this column is a blob
        		isBlob = true;
            	blob1 = rs.getBlob(column);
        	}
        }
        DatasetUtils.fromByteArray( isBlob ? blob1.getBytes(1,(int)blob1.length()) : rs.getBytes(column),
                DcmDecodeParam.EVR_LE,
                ds);
    }

    static class PatientQueryCmd extends QueryCmd {

        PatientQueryCmd(Dataset keys, boolean filterResult) throws SQLException {
            super(keys, filterResult);
        }

        protected void init() {
            super.init();
            addPatientMatch();
        }

        protected void fillDataset(Dataset ds) throws SQLException {
            fillDataset(ds, 1);
            ds.putCS(Tags.QueryRetrieveLevel, "PATIENT");
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.encodedAttributes"};
        }

        protected String[] getTables() {
            return new String[] { "Patient"};
        }

    }

    static class StudyQueryCmd extends QueryCmd {

        StudyQueryCmd(Dataset keys, boolean filterResult) throws SQLException {
            super(keys, filterResult);
        }

        protected void init() {
            super.init();
            addPatientMatch();
            addStudyMatch();
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.encodedAttributes",
                    "Study.encodedAttributes", "Study.modalitiesInStudy",
                    "Study.numberOfStudyRelatedSeries",
                    "Study.numberOfStudyRelatedInstances",
                    "Study.filesetId", "Study.filesetIuid",                    
                    "Study.retrieveAETs", "Study.externalRetrieveAET",
                    "Study.availability"};
        }

        protected String[] getTables() {
            return new String[] { "Patient", "Study"};
        }

        protected String[] getRelations() {
            return new String[] { "Patient.pk", "Study.patient_fk"};
        }

        protected void fillDataset(Dataset ds) throws SQLException {
            fillDataset(ds, 1);
            fillDataset(ds, 2);
            ds.putCS(Tags.ModalitiesInStudy, StringUtils.split(rs.getString(3),
                    '\\'));
            ds.putIS(Tags.NumberOfStudyRelatedSeries, rs.getInt(4));
            ds.putIS(Tags.NumberOfStudyRelatedInstances, rs.getInt(5));
            ds.putSH(Tags.StorageMediaFileSetID, rs.getString(6));
            ds.putUI(Tags.StorageMediaFileSetUID, rs.getString(7));
            DatasetUtils.putRetrieveAET(ds, rs.getString(8), rs.getString(9));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(10)]);
            ds.putCS(Tags.QueryRetrieveLevel, "STUDY");
        }

    }

    static class SeriesQueryCmd extends QueryCmd {

        SeriesQueryCmd(Dataset keys, boolean filterResult) throws SQLException {
            super(keys, filterResult);
        }

        protected void init() {
            super.init();
            addPatientMatch();
            addStudyMatch();
            addSeriesMatch();
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.encodedAttributes",
                    "Study.encodedAttributes", "Series.encodedAttributes",
                    "Series.numberOfSeriesRelatedInstances",
                    "Series.filesetId", "Series.filesetIuid",                    
                    "Series.retrieveAETs", "Series.externalRetrieveAET",
                    "Series.availability"};
        }

        protected String[] getTables() {
            return new String[] { "Patient", "Study", "Series"};
        }

        protected String[] getRelations() {
            return new String[] { "Patient.pk", "Study.patient_fk", "Study.pk",
                    "Series.study_fk"};
        }

        protected String[] getLeftJoin() {
            if  (!isMatchRequestAttributes())
                return null;
            sqlBuilder.setDistinct(true);
            return SERIES_REQUEST_LEFT_JOIN;
        }
        
        protected void fillDataset(Dataset ds) throws SQLException {
            fillDataset(ds, 1);
            fillDataset(ds, 2);
            fillDataset(ds, 3);
            ds.putIS(Tags.NumberOfSeriesRelatedInstances, rs.getInt(4));
            ds.putSH(Tags.StorageMediaFileSetID, rs.getString(5));
            ds.putUI(Tags.StorageMediaFileSetUID, rs.getString(6));
            DatasetUtils.putRetrieveAET(ds, rs.getString(7), rs.getString(8));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(9)]);
            ds.putCS(Tags.QueryRetrieveLevel, "SERIES");
        }
    }

    static class ImageQueryCmd extends QueryCmd {

        ImageQueryCmd(Dataset keys, boolean filterResult) throws SQLException {
            super(keys, filterResult);
        }

        protected void init() {
            super.init();
            addPatientMatch();
            addStudyMatch();
            addSeriesMatch();
            addInstanceMatch();
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.encodedAttributes",
                    "Study.encodedAttributes", "Series.encodedAttributes",
                    "Instance.encodedAttributes", "Instance.retrieveAETs",
                    "Instance.externalRetrieveAET", "Instance.availability",
                    "Media.filesetId", "Media.filesetIuid"};
        }

        protected String[] getTables() {
            return new String[] { "Patient", "Study", "Series", "Instance"};
        }

        protected String[] getLeftJoin() {
            ArrayList list = new ArrayList(12);
            if (isMatchRequestAttributes()) {
                sqlBuilder.setDistinct(true);
                list.add("SeriesRequest");
                list.add(null);
                list.add("Series.pk");
                list.add("SeriesRequest.series_fk");
            }
            if (isMatchSrCode()) {
                list.add("Code");
                list.add(SR_CODE);
                list.add("Instance.srcode_fk");
                list.add("Code.pk");
            }
            list.add("Media");
            list.add(null);
            list.add("Instance.media_fk");
            list.add("Media.pk");
            return (String[]) list.toArray(new String[list.size()]);
        }

        protected String[] getRelations() {
            return new String[] { "Patient.pk", "Study.patient_fk", "Study.pk",
                    "Series.study_fk", "Series.pk", "Instance.series_fk"};
        }

        protected void fillDataset(Dataset ds) throws SQLException {
            fillDataset(ds, 1);
            fillDataset(ds, 2);
            fillDataset(ds, 3);
            fillDataset(ds, 4);
            DatasetUtils.putRetrieveAET(ds, rs.getString(5), rs.getString(6));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(7)]);
            ds.putSH(Tags.StorageMediaFileSetID, rs.getString(8));
            ds.putUI(Tags.StorageMediaFileSetUID, rs.getString(9));
            ds.putCS(Tags.QueryRetrieveLevel, "IMAGE");
        }

    }

    protected boolean isMatchSrCode() {
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        return code != null
                && (code.vm(Tags.CodeValue) > 0 || code
                        .vm(Tags.CodingSchemeDesignator) > 0);
    }

    protected boolean isMatchRequestAttributes() {
        Dataset rqAttrs = keys.getItem(Tags.RequestAttributesSeq);
        return rqAttrs != null
                && (rqAttrs.vm(Tags.RequestedProcedureID) > 0
                        || rqAttrs.vm(Tags.SPSID) > 0);
    }
}