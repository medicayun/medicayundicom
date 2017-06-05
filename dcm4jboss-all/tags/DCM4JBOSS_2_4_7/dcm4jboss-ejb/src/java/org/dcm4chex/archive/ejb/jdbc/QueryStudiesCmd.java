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
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.common.PrivateTags;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1798 $ $Date: 2005-06-22 20:47:02 +0800 (周三, 22 6月 2005) $
 * @since 14.01.2004
 */
public class QueryStudiesCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final String[] SELECT_ATTRIBUTE = { "Patient.pk",
            "Patient.encodedAttributes", "Study.pk", "Study.encodedAttributes",
            "Study.modalitiesInStudy", "Study.numberOfStudyRelatedSeries",
            "Study.numberOfStudyRelatedInstances", "Study.retrieveAETs",
            "Study.availability", "Study.filesetId"};

    private static final String[] ENTITY = { "Patient"};

    private static final String[] LEFT_JOIN = { 
            "Study", null, "Patient.pk", "Study.patient_fk",};

    private final SqlBuilder sqlBuilder = new SqlBuilder();
    
    public QueryStudiesCmd(Dataset filter)
            throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
        sqlBuilder.setFrom(ENTITY);
        sqlBuilder.setLeftJoin(LEFT_JOIN);
        sqlBuilder.addLiteralMatch(null, "Patient.merge_fk", false, "IS NULL");
        sqlBuilder.addWildCardMatch(null, "Patient.patientId",
                SqlBuilder.TYPE2,
                filter.getString(Tags.PatientID),
                false);
        sqlBuilder.addWildCardMatch(null, "Patient.patientName",
                SqlBuilder.TYPE2,
                filter.getString(Tags.PatientName),
                true);
        sqlBuilder.addWildCardMatch(null, "Study.studyId", SqlBuilder.TYPE2,
                filter.getString(Tags.StudyID), false);
        sqlBuilder.addSingleValueMatch(null, "Study.studyIuid",
                SqlBuilder.TYPE1, filter.getString( Tags.StudyInstanceUID));
        sqlBuilder.addRangeMatch(null, "Study.studyDateTime",
                SqlBuilder.TYPE2,
                filter.getDateTimeRange(Tags.StudyDate, Tags.StudyTime));
        sqlBuilder.addWildCardMatch(null, "Study.accessionNumber",
                SqlBuilder.TYPE2,
                filter.getString(Tags.AccessionNumber),
                false);
        sqlBuilder.addModalitiesInStudyMatch(null, filter
                .getString(Tags.ModalitiesInStudy));
    }

    public int count() throws SQLException {
        try {
            sqlBuilder.setSelectCount();
            execute(sqlBuilder.getSql());
            next();
            return rs.getInt(1);
        } finally {
            close();
        }
    }

	
    public List list(int offset, int limit) throws SQLException {
        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
        sqlBuilder.addOrderBy("Patient.patientName", SqlBuilder.ASC);
        sqlBuilder.addOrderBy("Patient.pk", SqlBuilder.ASC);
        sqlBuilder.addOrderBy("Study.studyDateTime", SqlBuilder.ASC);
        sqlBuilder.setOffset(offset);
        sqlBuilder.setLimit(limit);
        try {
            execute(sqlBuilder.getSql());
            ArrayList result = new ArrayList();
            
            while (next()) {
                final byte[] patAttrs = getBytes(2);
                final byte[] styAttrs = getBytes(4);
                Dataset ds = dof.newDataset();
                ds.setPrivateCreatorID(PrivateTags.CreatorID);
                ds.putUL(PrivateTags.PatientPk, rs.getInt(1));
                DatasetUtils.fromByteArray(patAttrs, DcmDecodeParam.EVR_LE, ds);
                if (styAttrs != null) {
                    ds.putUL(PrivateTags.StudyPk, rs.getInt(3));
                    DatasetUtils.fromByteArray(styAttrs,
                            DcmDecodeParam.EVR_LE,
                            ds);
                    ds.putCS(Tags.ModalitiesInStudy, StringUtils.split(rs
                            .getString(5), '\\'));
                    ds.putIS(Tags.NumberOfStudyRelatedSeries, rs.getInt(6));
                    ds.putIS(Tags.NumberOfStudyRelatedInstances, rs.getInt(7));
                    ds.putAE(Tags.RetrieveAET, StringUtils.split(rs
                            .getString(8), '\\'));
                    ds.putCS(Tags.InstanceAvailability, Availability
                            .toString(rs.getInt(9)));
                    ds.putSH(Tags.StorageMediaFileSetID, rs.getString(10));
                }
                result.add(ds);
            }
            return result;
        } finally {
            close();
        }
    }
}