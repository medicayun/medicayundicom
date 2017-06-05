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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1272 $ $Date: 2004-12-03 23:22:30 +0800 (周五, 03 12月 2004) $
 * @since 26.08.2003
 */
public class RetrieveCmd extends BaseCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] SELECT_ATTRIBUTE = { "Instance.pk", "File.pk",
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
    
    private static SqlBuilder newSqlBuilder() {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
        sqlBuilder.setFrom(ENTITY);
        sqlBuilder.setLeftJoin(LEFT_JOIN);
        sqlBuilder.setRelations(RELATIONS);
        return sqlBuilder;
    }
    

    public static RetrieveCmd create(DataSource ds, Dataset keys)
            throws SQLException {
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        if (qrLevel == null || qrLevel.length() == 0)
            throw new IllegalArgumentException("Missing QueryRetrieveLevel");
        SqlBuilder sqlBuilder = newSqlBuilder();
        if ("PATIENT".equals(qrLevel)) {
            String pid = keys.getString(Tags.PatientID);
            if (pid == null)
                    throw new IllegalArgumentException("Missing PatientID");

            sqlBuilder.addWildCardMatch("Patient.patientId", SqlBuilder.TYPE2,
                    pid, false); 
        } else if ("STUDY".equals(qrLevel)) {
            String[] uid = keys.getStrings(Tags.StudyInstanceUID);
            if (uid == null || uid.length == 0)
                throw new IllegalArgumentException("Missing StudyInstanceUID");

            sqlBuilder.addListOfUidMatch("Study.studyIuid", SqlBuilder.TYPE1,
                    uid);
        } else if ("SERIES".equals(qrLevel)) {
            String[] uid = keys.getStrings(Tags.SeriesInstanceUID);
            if (uid == null || uid.length == 0)
                throw new IllegalArgumentException("Missing SeriesInstanceUID");

            sqlBuilder.addListOfUidMatch("Series.seriesIuid", SqlBuilder.TYPE1,
                    uid);
        } else if ("IMAGE".equals(qrLevel)) {
            String[] uids = keys.getStrings(Tags.SOPInstanceUID);
            if (uids == null || uids.length == 0)
                throw new IllegalArgumentException("Missing SOPInstanceUID");

            sqlBuilder.addListOfUidMatch("Instance.sopIuid", SqlBuilder.TYPE1,
                    uids);
            return new ImageRetrieveCmd(ds, sqlBuilder.getSql(), uids);
        } else {
            throw new IllegalArgumentException("QueryRetrieveLevel=" + qrLevel);
        }
        return new RetrieveCmd(ds, sqlBuilder.getSql());
    }

    public static RetrieveCmd create(DataSource ds, DcmElement refSOPSeq)
            throws SQLException {
        SqlBuilder sqlBuilder = newSqlBuilder();
        String[] uid = new String[refSOPSeq.vm()];
        for (int i = 0; i < uid.length; i++) {
            uid[i] = refSOPSeq.getItem(i).getString(Tags.RefSOPInstanceUID);
        }

        sqlBuilder.addListOfUidMatch("Instance.sopIuid", SqlBuilder.TYPE1,
                uid);
        return new RetrieveCmd(ds, sqlBuilder.getSql());
    }

    public static RetrieveCmd create(DataSource ds, String iuid)
            throws SQLException {
        SqlBuilder sqlBuilder = newSqlBuilder();

        sqlBuilder.addSingleValueMatch("Instance.sopIuid", SqlBuilder.TYPE1,
                iuid);
        return new RetrieveCmd(ds, sqlBuilder.getSql());
    }

    private final String sql;

    protected RetrieveCmd(DataSource ds, String sql) throws SQLException {
        super(ds, transactionIsolationLevel);
        this.sql = sql;
    }

    public FileInfo[][] execute() throws SQLException {
        Map result = map();
        try {
            execute(sql);
            ArrayList list;
            Object key;
            while (next()) {
                FileInfo info = new FileInfo(rs.getInt(2), rs.getString(3), rs
                        .getString(4), rs.getBytes(5), rs.getString(6), rs
                        .getBytes(7), rs.getBytes(8), rs.getBytes(9), rs
                        .getString(10), rs.getString(11), rs.getString(12), rs
                        .getString(13), rs.getString(14), rs.getString(15), rs
                        .getString(16), rs.getString(17), rs.getInt(18));
                key = key();
                list = (ArrayList) result.get(key);
                if (list == null) {
                    result.put(key, list = new ArrayList());
                }
                list.add(info);
            }
        } finally {
            close();
        }
        return toArray(result);
    }

    protected Map map() {
        return new TreeMap();
    }


    protected Object key() throws SQLException {
        return new Integer(rs.getInt(1));
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

    static class ImageRetrieveCmd extends RetrieveCmd {
        final String[] uids;
        ImageRetrieveCmd(DataSource ds, String sql, String[] uids) throws SQLException {
            super(ds, sql);
            this.uids = uids;
        }

        protected Map map() {
            return new HashMap();
        }

        protected Object key() throws SQLException {
            return rs.getString(10);
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
}
