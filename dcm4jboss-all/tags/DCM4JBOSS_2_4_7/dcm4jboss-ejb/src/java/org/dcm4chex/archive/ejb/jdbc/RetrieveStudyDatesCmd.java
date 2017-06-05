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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1846 $ $Date: 2005-07-13 23:37:05 +0800 (周三, 13 7月 2005) $
 * @since 26.08.2003
 */
public class RetrieveStudyDatesCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    //be aware of order the fields: dont mix up different types (updated, created)  
    private static final String[] SELECT_ATTRIBUTE = { "Study.updatedTime", "Series.updatedTime", "Study.createdTime", "Series.createdTime" };

    private static final String[] ENTITY = { "Patient", "Study", "Series",
            "Instance"};


    private static final String[] RELATIONS = { "Patient.pk",
            "Study.patient_fk", "Study.pk", "Series.study_fk", "Series.pk",
            "Instance.series_fk"};
    
    
    public static RetrieveStudyDatesCmd create(Dataset keys)
            throws SQLException {
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        if (qrLevel == null || qrLevel.length() == 0)
            throw new IllegalArgumentException("Missing QueryRetrieveLevel");
		if ("IMAGE".equals(qrLevel)) {
            return new ImageRetrieveCmd(new ImageSql(keys).getSql(), 
					keys.getStrings(Tags.SOPInstanceUID));
        }
		if ("SERIES".equals(qrLevel)) {
			return new RetrieveStudyDatesCmd(new SeriesSql(keys, true).getSql());
        }
		if ("STUDY".equals(qrLevel)) {
			return new RetrieveStudyDatesCmd(new StudySql(keys, true).getSql());
        }
        if ("PATIENT".equals(qrLevel)) {			
			return new RetrieveStudyDatesCmd(new PatientSql(keys, true).getSql());
        }
        throw new IllegalArgumentException("QueryRetrieveLevel=" + qrLevel);
     }

    protected RetrieveStudyDatesCmd(String sql) throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
		execute(sql);
	}

    public Date getMostRecentUpdatedTime() throws SQLException {
    	return getMostRecent(1);//1 index of field study.updatedTime
    }
    public Date getMostRecentCreatedTime() throws SQLException {
    	return getMostRecent(3);//3 index of field study.createdTime
    }
    private Date getMostRecent(int idx) throws SQLException {
    	int idx2 = idx; idx2++;
		try {
			if ( ! next() ) {
				return null;
			}
			Date seriesDate, studyDate, mrDate;
			Date date = rs.getTimestamp(idx);
			rs.beforeFirst();
			while (next()) {
				studyDate = rs.getTimestamp(idx);
				seriesDate = rs.getTimestamp(idx2);
				mrDate = studyDate.after(seriesDate) ? studyDate:seriesDate;
				if ( mrDate.after(date) ) 
					date = mrDate;
			}
			return date;
		} finally {
			close();
		}
		
	}

    public Date getEldestUpdatedTime() throws SQLException {
    	return getEldest(1);//1 index of field study.updatedTime
    }
    public Date getEldestCreatedTime() throws SQLException {
    	return getEldest(3);//3 index of field study.createdTime
    }
    private Date getEldest(int idx) throws SQLException {
    	int idx2 = idx; idx2++;
		try {
			if ( ! next() ) {
				return null;
			}
			Date seriesDate, studyDate, mrDate;
			Date date = rs.getTimestamp(idx);
			rs.beforeFirst();
			while (next()) {
				studyDate = rs.getTimestamp(idx);
				seriesDate = rs.getTimestamp(idx2);
				mrDate = studyDate.before(seriesDate) ? studyDate:seriesDate;
				if ( mrDate.before(date) ) 
					date = mrDate;
			}
			return date;
		} finally {
			close();
		}
		
	}
    
    static class ImageRetrieveCmd extends RetrieveStudyDatesCmd {
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
