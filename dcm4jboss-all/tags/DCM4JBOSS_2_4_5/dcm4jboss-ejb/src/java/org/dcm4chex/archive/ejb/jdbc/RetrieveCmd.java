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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1829 $ $Date: 2005-06-30 22:56:04 +0800 (周四, 30 6月 2005) $
 * @since 26.08.2003
 */
public class RetrieveCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] SELECT_ATTRIBUTE = { "Instance.pk", "File.pk",
            "Patient.patientId", "Patient.patientName",
            "Patient.encodedAttributes", "Study.studyIuid",
            "Study.encodedAttributes", "Series.encodedAttributes",
            "Instance.encodedAttributes", "Instance.sopIuid",
            "Instance.sopCuid", "Instance.externalRetrieveAET",
            "FileSystem.retrieveAET", "FileSystem.directoryPath",
            "File.filePath", "File.fileTsuid",
            "File.fileMd5Field", "File.fileSize", "File.fileStatus"};

    private static final String[] ENTITY = { "Patient", "Study", "Series",
            "Instance"};

    private static final String[] LEFT_JOIN = { 
            "File", null, "Instance.pk", "File.instance_fk",
            "FileSystem", null, "File.filesystem_fk", "FileSystem.pk" };
    
    private static final String[] RELATIONS = { "Patient.pk",
            "Study.patient_fk", "Study.pk", "Series.study_fk", "Series.pk",
            "Instance.series_fk"};
    
    public static RetrieveCmd create(Dataset keys)
            throws SQLException {
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        if (qrLevel == null || qrLevel.length() == 0)
            throw new IllegalArgumentException("Missing QueryRetrieveLevel");
		if ("IMAGE".equals(qrLevel)) {
            return new ImageRetrieveCmd(new ImageSql(keys).getSql(), 
					keys.getStrings(Tags.SOPInstanceUID));
        }
		if ("SERIES".equals(qrLevel)) {
			return new RetrieveCmd(new SeriesSql(keys, true).getSql());
        }
		if ("STUDY".equals(qrLevel)) {
			return new RetrieveCmd(new StudySql(keys, true).getSql());
        }
        if ("PATIENT".equals(qrLevel)) {			
			return new RetrieveCmd(new PatientSql(keys, true).getSql());
        }
        throw new IllegalArgumentException("QueryRetrieveLevel=" + qrLevel);
     }

    public static RetrieveCmd create(DcmElement refSOPSeq)
            throws SQLException {
        return new RetrieveCmd(new RefSOPSql(refSOPSeq).getSql());
    }

    protected RetrieveCmd(String sql) throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
		execute(sql);
	}

    public FileInfo[][] getFileInfos() throws SQLException {
		Map result = map();
		try {
			ArrayList list;
			Object key;
			while (next()) {
				FileInfo info = new FileInfo(rs.getInt(2), rs.getString(3), rs
						.getString(4), getBytes(5), rs.getString(6),
						getBytes(7), getBytes(8), getBytes(9),
						rs.getString(10), rs.getString(11), rs.getString(12),
						rs.getString(13), rs.getString(14), rs.getString(15),
						rs.getString(16), rs.getString(17), rs.getInt(18), rs
								.getInt(19));
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
        ImageRetrieveCmd(String sql, String[] uids) throws SQLException {
            super(sql);
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
                throw new RuntimeException("Result Set contains " 
						+ result.size() + " non-matching entries!");
            }
            return array;
        }
    }

	private static class Sql {
		final SqlBuilder sqlBuilder = new SqlBuilder();
		Sql() {
	        sqlBuilder.setSelect(SELECT_ATTRIBUTE);
	        sqlBuilder.setFrom(ENTITY);
	        sqlBuilder.setLeftJoin(LEFT_JOIN);
	        sqlBuilder.setRelations(RELATIONS);
		}
		public final String getSql() {
			return sqlBuilder.getSql();
		}
	}
	
	private static class PatientSql extends Sql {
		PatientSql(Dataset keys, boolean patientRetrieve) {
            String pid = keys.getString(Tags.PatientID);
            if (pid != null)
	            sqlBuilder.addWildCardMatch(null, "Patient.patientId",
	                    SqlBuilder.TYPE2, pid, false);
            else if (patientRetrieve)
                throw new IllegalArgumentException("Missing PatientID");
		}
	}

	private static class StudySql extends PatientSql {
		StudySql(Dataset keys, boolean studyRetrieve) {
			super(keys, false);
            String[] uid = keys.getStrings(Tags.StudyInstanceUID);
            if (uid != null && uid.length != 0)
	            sqlBuilder.addListOfUidMatch(null, "Study.studyIuid",
	                    SqlBuilder.TYPE1, uid);
            else if (studyRetrieve)
                throw new IllegalArgumentException("Missing StudyInstanceUID");
		}
	}
	
	private static class SeriesSql extends StudySql {
		SeriesSql(Dataset keys, boolean seriesRetrieve) {
			super(keys, false);
            String[] uid = keys.getStrings(Tags.SeriesInstanceUID);
            if (uid != null && uid.length != 0)
	            sqlBuilder.addListOfUidMatch(null, "Series.seriesIuid",
	                    SqlBuilder.TYPE1, uid);
            else if (seriesRetrieve)
                throw new IllegalArgumentException("Missing SeriesInstanceUID");
		}
	}
	
	private static class ImageSql extends SeriesSql {
		ImageSql(Dataset keys) {
			super(keys, false);
            String[] uid = keys.getStrings(Tags.SOPInstanceUID);
            if (uid != null && uid.length != 0)
	            sqlBuilder.addListOfUidMatch(null, "Instance.sopIuid",
	                    SqlBuilder.TYPE1, uid);
            else 
				throw new IllegalArgumentException("Missing SOPInstanceUID");
		}
	}

	private static class RefSOPSql extends Sql {
		RefSOPSql(DcmElement refSOPSeq) {
	        String[] uid = new String[refSOPSeq.vm()];
	        for (int i = 0; i < uid.length; i++) {
	            uid[i] = refSOPSeq.getItem(i).getString(Tags.RefSOPInstanceUID);
	        }

	        sqlBuilder.addListOfUidMatch(null, "Instance.sopIuid", SqlBuilder.TYPE1,
	                uid);
		}
	}
	
}
