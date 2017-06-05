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
 * @version $Revision: 1540 $ $Date: 2005-02-19 02:52:25 +0800 (周六, 19 2月 2005) $
 */
public abstract class QueryCmd extends BaseCmd {

    public static int transactionIsolationLevel = 0;

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final String[] QRLEVEL = { "PATIENT", "STUDY", "SERIES",
            "IMAGE"};

    private static final String[] AVAILABILITY = { "ONLINE", "NEARLINE",
            "OFFLINE", "UNAVAILABLE"};

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
        sqlBuilder.addWildCardMatch("Patient.patientId", SqlBuilder.TYPE2, keys
                .getString(Tags.PatientID), false);
        sqlBuilder.addWildCardMatch("Patient.patientName",
                SqlBuilder.TYPE2,
                keys.getString(Tags.PatientName),
                true);
        sqlBuilder.addRangeMatch("Patient.patientBirthDate",
                SqlBuilder.TYPE2,
                keys.getDateTimeRange(Tags.PatientBirthDate,
                        Tags.PatientBirthTime));
        sqlBuilder.addWildCardMatch("Patient.patientSex",
                SqlBuilder.TYPE2,
                keys.getString(Tags.PatientSex),
                false);
    }

    protected void addStudyMatch() {
        sqlBuilder.addLiteralMatch("Study.numberOfStudyRelatedSeries",
                SqlBuilder.TYPE1,
                " != 0");
        sqlBuilder.addListOfUidMatch("Study.studyIuid", SqlBuilder.TYPE1, keys
                .getStrings(Tags.StudyInstanceUID));
        sqlBuilder.addWildCardMatch("Study.studyId", SqlBuilder.TYPE2, keys
                .getString(Tags.StudyID), false);
        sqlBuilder.addRangeMatch("Study.studyDateTime", SqlBuilder.TYPE2, keys
                .getDateTimeRange(Tags.StudyDate, Tags.StudyTime));
        sqlBuilder.addWildCardMatch("Study.accessionNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.AccessionNumber),
                false);
        sqlBuilder.addWildCardMatch("Study.referringPhysicianName",
                SqlBuilder.TYPE2,
                keys.getString(Tags.ReferringPhysicianName),
                true);
        sqlBuilder.addModalitiesInStudyMatch(keys
                .getString(Tags.ModalitiesInStudy));
    }

    protected void addSeriesMatch() {
        sqlBuilder.addBooleanMatch("Series.hidden", SqlBuilder.TYPE2, false);
        sqlBuilder.addListOfUidMatch("Series.seriesIuid",
                SqlBuilder.TYPE1,
                keys.getStrings(Tags.SeriesInstanceUID));
        sqlBuilder.addWildCardMatch("Series.seriesNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.SeriesNumber),
                false);
        sqlBuilder.addWildCardMatch("Series.modality", SqlBuilder.TYPE1, keys
                .getString(Tags.Modality), false);
        sqlBuilder.addRangeMatch("Series.ppsStartDateTime",
                SqlBuilder.TYPE2,
                keys.getDateRange(Tags.PPSStartDate, Tags.PPSStartTime));
    }

    protected void addInstanceMatch() {
        sqlBuilder.addListOfUidMatch("Instance.sopIuid", SqlBuilder.TYPE1, keys
                .getStrings(Tags.SOPInstanceUID));
        sqlBuilder.addListOfUidMatch("Instance.sopCuid", SqlBuilder.TYPE1, keys
                .getStrings(Tags.SOPClassUID));
        sqlBuilder.addWildCardMatch("Instance.instanceNumber",
                SqlBuilder.TYPE2,
                keys.getString(Tags.InstanceNumber),
                false);
        sqlBuilder.addWildCardMatch("Instance.srCompletionFlag",
                SqlBuilder.TYPE2,
                keys.getString(Tags.CompletionFlag),
                false);
        sqlBuilder.addWildCardMatch("Instance.srVerificationFlag",
                SqlBuilder.TYPE2,
                keys.getString(Tags.VerificationFlag),
                false);
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        if (code != null) {
            sqlBuilder.addWildCardMatch("Code.codeValue",
                    SqlBuilder.TYPE2,
                    code.getString(Tags.CodeValue),
                    false);
            sqlBuilder.addWildCardMatch("Code.codingSchemeDesignator",
                    SqlBuilder.TYPE2,
                    code.getString(Tags.CodingSchemeDesignator),
                    false);
            sqlBuilder.addWildCardMatch("Code.codingSchemeVersion",
                    SqlBuilder.TYPE2,
                    code.getString(Tags.CodingSchemeVersion),
                    false);
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
        DatasetUtils.fromByteArray(rs.getBytes(column),
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
            return new String[] { 
                    "Code", "Instance.srcode_fk", "Code.pk",
                    "Media", "Instance.media_fk", "Media.pk",
                    };
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
}