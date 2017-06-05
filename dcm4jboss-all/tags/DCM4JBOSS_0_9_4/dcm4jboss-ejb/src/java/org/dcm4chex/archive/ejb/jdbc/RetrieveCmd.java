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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1216 $ $Date: 2004-11-24 07:46:21 +0800 (周三, 24 11月 2004) $
 * @since 26.08.2003
 */
public abstract class RetrieveCmd extends BaseCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] QRLEVEL = { "PATIENT", "STUDY", "SERIES",
            "IMAGE"};

    private static final String[] SELECT_ATTRIBUTE = { "File.pk",
            "Patient.patientId", "Patient.patientName",
            "Patient.encodedAttributes", "Study.studyIuid",
            "Study.encodedAttributes", "Series.encodedAttributes",
            "Instance.encodedAttributes", "Instance.sopIuid",
            "Instance.sopCuid", "Instance.retrieveAETs", "FileSystem.retrieveAET",
            "FileSystem.directoryPath", "File.filePath", "File.fileTsuid",
            "File.fileMd5Field", "File.fileSize"};

    private static final String[] ENTITY = { "Patient", "Study", "Series",
            "Instance"};

    private static final String[] LEFT_JOIN = { "File", "Instance.pk",
    		"File.instance_fk", "FileSystem", "File.filesystem_fk", "FileSystem.pk" };
    
    private static final String[] RELATIONS = { "Patient.pk",
            "Study.patient_fk", "Study.pk", "Series.study_fk", "Series.pk",
            "Instance.series_fk"};

    public static RetrieveCmd create(DataSource ds, Dataset keys)
            throws SQLException {
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        switch (Arrays.asList(QRLEVEL).indexOf(qrLevel)) {
        case 0:
            return new PatientRetrieveCmd(ds, keys);
        case 1:
            return new StudyRetrieveCmd(ds, keys);
        case 2:
            return new SeriesRetrieveCmd(ds, keys);
        case 3:
            return new ImageRetrieveCmd(ds, keys);
        default:
            throw new IllegalArgumentException("QueryRetrieveLevel=" + qrLevel);
        }
    }

    public static RetrieveCmd create(DataSource ds, DcmElement refSOPSeq)
            throws SQLException {
        return new FileRetrieveCmd(ds, refSOPSeq);
    }

    protected final SqlBuilder sqlBuilder = new SqlBuilder();

    protected RetrieveCmd(DataSource ds) throws SQLException {
        super(ds, transactionIsolationLevel);
        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
        sqlBuilder.setFrom(ENTITY);
        sqlBuilder.setLeftJoin(LEFT_JOIN);
        sqlBuilder.setRelations(RELATIONS);
    }

    public FileInfo[][] execute() throws SQLException {
        LinkedHashMap result = new LinkedHashMap();
        try {
            execute(sqlBuilder.getSql());
            read(result);
        } finally {
            close();
        }
        return toArray(result);
    }

    protected FileInfo[][] toArray(Map result) {
        FileInfo[][] array = new FileInfo[result.size()][];
        ArrayList list;
        Iterator it = result.values().iterator();
        for (int i = 0; i < array.length; i++) {
            list = (ArrayList) it.next();
            array[i] = (FileInfo[]) list.toArray(new FileInfo[list.size()]);
        }
        return array;
    }

    private void read(Map result) throws SQLException {
        ArrayList list;
        while (next()) {
            FileInfo info = new FileInfo(rs.getInt(1), rs.getString(2), rs
                    .getString(3), rs.getBytes(4), rs.getString(5), rs
                    .getBytes(6), rs.getBytes(7), rs.getBytes(8), rs
                    .getString(9), rs.getString(10), rs.getString(11), rs
                    .getString(12), rs.getString(13), rs.getString(14), rs
                    .getString(15), rs.getString(16), rs.getInt(17));
            list = (ArrayList) result.get(info.sopIUID);
            if (list == null) {
                result.put(info.sopIUID, list = new ArrayList());
            }
            list.add(info);
        }
    }

    static class PatientRetrieveCmd extends RetrieveCmd {

        PatientRetrieveCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds);
            String pid = keys.getString(Tags.PatientID);
            if (pid == null)
                    throw new IllegalArgumentException("Missing PatientID");

            sqlBuilder.addWildCardMatch("Patient.patientId", SqlBuilder.TYPE2,
                    pid, false);
        }
    }

    static class StudyRetrieveCmd extends RetrieveCmd {

        StudyRetrieveCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds);
            String[] uid = keys.getStrings(Tags.StudyInstanceUID);
            if (uid == null || uid.length == 0)
                    throw new IllegalArgumentException(
                            "Missing StudyInstanceUID");

            sqlBuilder.addListOfUidMatch("Study.studyIuid", SqlBuilder.TYPE1,
                    uid);
        }
    }

    static class SeriesRetrieveCmd extends RetrieveCmd {

        SeriesRetrieveCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds);
            String[] uid = keys.getStrings(Tags.SeriesInstanceUID);
            if (uid == null || uid.length == 0)
                    throw new IllegalArgumentException(
                            "Missing SeriesInstanceUID");

            sqlBuilder.addListOfUidMatch("Series.seriesIuid", SqlBuilder.TYPE1,
                    uid);
        }
    }

    static class ImageRetrieveCmd extends RetrieveCmd {
        final String[] uids;
        ImageRetrieveCmd(DataSource ds, Dataset keys) throws SQLException {
            super(ds);
            uids = keys.getStrings(Tags.SOPInstanceUID);
            if (uids == null || uids.length == 0)
                    throw new IllegalArgumentException("Missing SOPInstanceUID");

            sqlBuilder.addListOfUidMatch("Instance.sopIuid", SqlBuilder.TYPE1,
                    uids);
        }

        protected FileInfo[][] toArray(Map result) {
            FileInfo[][] array = new FileInfo[result.size()][];
            ArrayList list;
            for (int i = 0, j = 0; j < uids.length; ++j) {
                list = (ArrayList) result.remove(uids[j]);
                if (list != null) {
                    array[i++] = (FileInfo[]) list.toArray(new FileInfo[list.size()]);
                }
            }
            if (!result.isEmpty()) {
                throw new RuntimeException("Result Set contains " + result.size() + " non-matching entries!");
            }
            return array;
        }
    }

    static class FileRetrieveCmd extends RetrieveCmd {

        FileRetrieveCmd(DataSource ds, DcmElement refSOPSeq)
                throws SQLException {
            super(ds);
            String[] uid = new String[refSOPSeq.vm()];
            for (int i = 0; i < uid.length; i++) {
                uid[i] = refSOPSeq.getItem(i).getString(Tags.RefSOPInstanceUID);
            }

            sqlBuilder.addListOfUidMatch("Instance.sopIuid", SqlBuilder.TYPE1,
                    uid);
        }
    }
}
