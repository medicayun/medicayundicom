/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.ejb.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 2275 $ $Date: 2006-02-14 21:35:12 +0800 (周二, 14 2月 2006) $
 * @since 26.08.2003
 */
public class RetrieveCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final String[] SELECT_ATTRIBUTE = { "Instance.pk", "File.pk",
            "Patient.patientId", "Patient.patientName",
            "Patient.encodedAttributes", "Study.studyIuid", "Series.seriesIuid",
            "Study.encodedAttributes", "Series.encodedAttributes",
            "Instance.encodedAttributes", "Instance.sopIuid",
            "Instance.sopCuid", "Instance.externalRetrieveAET",
            "FileSystem.retrieveAET", "FileSystem.availability",
            "FileSystem.directoryPath", "File.filePath", "File.fileTsuid",
            "File.fileMd5Field", "File.fileSize", "File.fileStatus", "Series.pk"};

    private static final String[] ENTITY = { "Patient", "Study", "Series",
            "Instance"};

    private static final String[] LEFT_JOIN = { 
            "File", null, "Instance.pk", "File.instance_fk",
            "FileSystem", null, "File.filesystem_fk", "FileSystem.pk" };
    
    private static final String[] RELATIONS = { "Patient.pk",
            "Study.patient_fk", "Study.pk", "Series.study_fk", "Series.pk",
            "Instance.series_fk"};

    private static final Comparator DESC_FILE_PK = new Comparator() {

        public int compare(Object o1, Object o2) {
            FileInfo fi1 = (FileInfo) o1;
            FileInfo fi2 = (FileInfo) o2;
            int diffAvail = fi1.availability - fi2.availability;
            return diffAvail != 0 ? diffAvail : fi2.pk - fi1.pk;
        }
    };

    
    public static RetrieveCmd create(Dataset keys)
            throws SQLException {
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        if (qrLevel == null || qrLevel.length() == 0)
            throw new IllegalArgumentException("Missing QueryRetrieveLevel");
		if ("IMAGE".equals(qrLevel))
            return createInstanceRetrieve(keys);
		if ("SERIES".equals(qrLevel))
			return createSeriesRetrieve(keys);
		if ("STUDY".equals(qrLevel))
			return createStudyRetrieve(keys);
        if ("PATIENT".equals(qrLevel))			
			return createPatientRetrieve(keys);
        throw new IllegalArgumentException("QueryRetrieveLevel=" + qrLevel);
     }

    public static RetrieveCmd createPatientRetrieve(Dataset keys) throws SQLException {
		return new RetrieveCmd(new PatientSql(keys, true).getSql());
	}

	public static RetrieveCmd createStudyRetrieve(Dataset keys) throws SQLException {
		return new RetrieveCmd(new StudySql(keys, true).getSql());
	}

	public static RetrieveCmd createSeriesRetrieve(Dataset keys) throws SQLException {
		return new RetrieveCmd(new SeriesSql(keys, true).getSql());
	}

	public static ImageRetrieveCmd createInstanceRetrieve(Dataset keys)
		throws SQLException {
		return new ImageRetrieveCmd(new ImageSql(keys).getSql(), 
				keys.getStrings(Tags.SOPInstanceUID));
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
				FileInfo info = new FileInfo(rs.getInt(2), rs.getString(3),
						rs.getString(4), getBytes(5), rs.getString(6),
						rs.getString(7), getBytes(8), getBytes(9), getBytes(10),
						rs.getString(11), rs.getString(12), rs.getString(13),
						rs.getString(14), rs.getInt(15), rs.getString(16),
						rs.getString(17), rs.getString(18), rs.getString(19),
						rs.getInt(20), rs.getInt(21));
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

    public Map getStudyFileInfo() throws SQLException {
		Map all = map();
		Map series;
		try {
			ArrayList list;
			Object seriesKey, instKey;
			while (next()) {
				FileInfo info = new FileInfo(rs.getInt(2), rs.getString(3),
						rs.getString(4), getBytes(5), rs.getString(6),
						rs.getString(7), getBytes(8), getBytes(9), getBytes(10),
						rs.getString(11), rs.getString(12), rs.getString(13),
						rs.getString(14), rs.getInt(15), rs.getString(16),
						rs.getString(17), rs.getString(18), rs.getString(19),
						rs.getInt(20), rs.getInt(21));
				seriesKey = new Integer( rs.getInt(22));//series.pk
				instKey = new Integer( rs.getInt(1));//instance.pk
				series = (Map) all.get(seriesKey);
				if (series == null) {
					all.put( seriesKey, series = map());
					series.put(instKey, list = new ArrayList());
				} else {
					list = (ArrayList)series.get(instKey);
					if ( list == null ) {
						series.put(instKey, list = new ArrayList());
					}
				}
				list.add(info);
			}
		} finally {
			close();
		}
		return all;
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
			Arrays.sort(array[i], DESC_FILE_PK);
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
            return rs.getString(11);
        }
        
        protected FileInfo[][] toArray(Map result) {
            FileInfo[][] array = new FileInfo[result.size()][];
            ArrayList list;
            for (int i = 0, j = 0; j < uids.length; ++j) {
                list = (ArrayList) result.remove(uids[j]);
                if (list != null) {
                	array[i] = (FileInfo[]) list.toArray(new FileInfo[list.size()]);
        			Arrays.sort(array[i], DESC_FILE_PK);
        			++i;
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
