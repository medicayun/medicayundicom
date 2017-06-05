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

import javax.sql.DataSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4cheri.util.DatasetUtils;
import org.dcm4cheri.util.StringUtils;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
 */
public abstract class QueryCmd extends BaseCmd {

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final String[] QRLEVEL = { "PATIENT", "STUDY", "SERIES",
            "IMAGE"};

    private static final String[] AVAILABILITY = { "ONLINE", "NEARLINE",
            "OFFLINE"};

    public static QueryCmd create(DataSource ds, Dataset keys)
            throws SQLException {
        QueryCmd cmd;
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        switch (Arrays.asList(QRLEVEL).indexOf(qrLevel)) {
        case 0:
            cmd = new PatientQueryCmd(ds, keys);
            break;
        case 1:
            cmd = new StudyQueryCmd(ds, keys);
            break;
        case 2:
            cmd = new SeriesQueryCmd(ds, keys);
            break;
        case 3:
            cmd = new ImageQueryCmd(ds, keys);
            break;
        default:
            throw new IllegalArgumentException("QueryRetrieveLevel=" + qrLevel);
        }
        cmd.init();
        return cmd;
    }

    protected final Dataset keys;

    protected final SqlBuilder sqlBuilder = new SqlBuilder();

    protected QueryCmd(DataSource ds, Dataset keys) throws SQLException {
        super(ds);
        this.keys = keys;        
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
        keys.putCS(Tags.SpecificCharacterSet);
        keys.putAE(Tags.RetrieveAET);
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
                ds.putXX(tag, vr);
                continue;
            }
            if (vr == VRs.SQ) {
                Dataset keyItem = key.getItem();
                if (keyItem != null) {
                    for (int i = 0, n = el.vm(); i < n; ++i) {
                        adjustDataset(el.addNewItem(), keyItem);
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

        PatientQueryCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds, keys);
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

        StudyQueryCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds, keys);
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
                    "Study.retrieveAETs", "Study.availability"};
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
            putRetrieveAETs(ds, rs.getString(6));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(7)]);
            ds.putCS(Tags.QueryRetrieveLevel, "STUDY");
        }
    }

    static class SeriesQueryCmd extends QueryCmd {

        SeriesQueryCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds, keys);
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
                    "Series.retrieveAETs", "Series.availability"};
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
            putRetrieveAETs(ds, rs.getString(5));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(6)]);
            ds.putCS(Tags.QueryRetrieveLevel, "SERIES");
        }
    }

    static class ImageQueryCmd extends QueryCmd {

        ImageQueryCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds, keys);
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
                    "Instance.availability"};
        }

        protected String[] getTables() {
            return new String[] { "Patient", "Study", "Series", "Instance"};
        }

        protected String[] getLeftJoin() {
            return isMatchSrCode() ? new String[] { "Code",
                    "Instance.srcode_fk", "Code.pk"} : null;
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
            putRetrieveAETs(ds, rs.getString(5));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(6)]);
            ds.putCS(Tags.QueryRetrieveLevel, "IMAGE");
        }

    }

    protected boolean isMatchSrCode() {
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        return code != null
                && (code.vm(Tags.CodeValue) > 0 || code
                        .vm(Tags.CodingSchemeDesignator) > 0);
    }

    private static void putRetrieveAETs(Dataset ds, String aets) {
        if (aets != null) {
            ds.putAE(Tags.RetrieveAET, StringUtils.split(aets, '\\'));
        }
    }
}