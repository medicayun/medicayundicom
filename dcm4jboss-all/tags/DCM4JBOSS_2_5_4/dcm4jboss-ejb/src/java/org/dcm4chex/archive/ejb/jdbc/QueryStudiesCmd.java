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
 * @version $Revision: 2032 $ $Date: 2005-10-25 23:45:52 +0800 (周二, 25 10月 2005) $
 * @since 14.01.2004
 */
public class QueryStudiesCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static final String[] SELECT_ATTRIBUTE = { "Patient.pk",
            "Patient.encodedAttributes", "Study.pk", "Study.encodedAttributes",
            "Study.modalitiesInStudy", "Study.numberOfStudyRelatedSeries",
            "Study.numberOfStudyRelatedInstances", "Study.retrieveAETs",
            "Study.availability", "Study.filesetId", "Patient.hidden", "Study.hidden"};

    private static final String[] ENTITY = {"Patient"};
    private static final String[] ENTITY_FOR_HIDDEN = {"Series"};

    private static final String[] LEFT_JOIN = { 
            "Study", null, "Patient.pk", "Study.patient_fk",};
    private static final String[] LEFT_JOIN_FOR_HIDDEN = new String[] { 
    	"Instance", null, "Series.pk", "Instance.series_fk"};

	private boolean hideMissingStudies;
    
    private final SqlBuilder sqlBuilder = new SqlBuilder();

    public QueryStudiesCmd(Dataset filter, boolean showHidden, boolean hideMissingStudies)
            throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
				transactionIsolationLevel);
    	sqlBuilder.setFrom(ENTITY);
        sqlBuilder.setLeftJoin(LEFT_JOIN);
        if ( showHidden ) {
         	//Match.Node node = sqlBuilder.addNodeMatch("OR",false);
        	/* we have to build something like this:
        	 SELECT patient.pat_name, ....
        	 	FROM patient LEFT JOIN study ON (patient.pk = study.patient_fk) 
        	 	WHERE ( exists (SELECT series.pk FROM series  LEFT JOIN instance ON (series.pk = instance.series_fk)
                                WHERE ((study.pk = series.study_fk or (  study.hidden != 0)) 
                              		    AND ((patient.hidden != 0)OR(study.hidden != 0)OR(series.hidden != 0)OR(instance.hidden != 0) )) 
                                      ) 
                        OR (patient.hidden != 0)
                      )  
                
                AND (patient.merge_fk IS NULL) [AND ..] 
                ORDER BY patient.pat_name ASC, patient.pk ASC, study.study_datetime ASC 

        	 */
        	SqlBuilder subQuery = new SqlBuilder();
        	subQuery.setSelect(new String[]{"Series.pk"});
        	subQuery.setFrom(ENTITY_FOR_HIDDEN );
        	subQuery.setLeftJoin(LEFT_JOIN_FOR_HIDDEN);
           	Match.Node node0 = sqlBuilder.addNodeMatch("OR",false);
           	//define the subquery
           	Match.Node node1 = subQuery.addNodeMatch("OR",false);
           	node1.addMatch( new Match.FieldValue(null, "Study.pk", SqlBuilder.TYPE1, null, "Series.study_fk") );
           	node1.addMatch( subQuery.getBooleanMatch(null,"Study.hidden",SqlBuilder.TYPE1,true) );
        	Match.Node node2 = subQuery.addNodeMatch("OR",false);
        	node2.addMatch( subQuery.getBooleanMatch(null,"Patient.hidden",SqlBuilder.TYPE1,true));
        	node2.addMatch( subQuery.getBooleanMatch(null,"Study.hidden",SqlBuilder.TYPE1,true));
        	node2.addMatch( subQuery.getBooleanMatch(null,"Series.hidden",SqlBuilder.TYPE1,true));
        	node2.addMatch( subQuery.getBooleanMatch(null,"Instance.hidden",SqlBuilder.TYPE1,true));

        	node0.addMatch( new Match.Subquery(subQuery, null, null));
        	node0.addMatch( sqlBuilder.getBooleanMatch(null, "Patient.hidden",SqlBuilder.TYPE1, true));
        } else {
    		sqlBuilder.addBooleanMatch(null, "Study.hidden", SqlBuilder.TYPE2, false );
    		sqlBuilder.addBooleanMatch(null, "Patient.hidden", SqlBuilder.TYPE2, false );
        }
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
    	this.hideMissingStudies = hideMissingStudies && !showHidden;	
        if ( this.hideMissingStudies ) {
        	sqlBuilder.addNULLValueMatch(null,"Study.encodedAttributes", true);
    	}
        	
    }

    public int count() throws SQLException {
        try {
            sqlBuilder.setSelectCount(new String[]{"Study.pk"}, true);
            execute( sqlBuilder.getSql() );
            next();
            if (hideMissingStudies) return rs.getInt(1);
            //we have to add number of studies and number of patients without studies.
            int studies = rs.getInt(1);
            rs.close();
            rs = null;
            sqlBuilder.setSelectCount(new String[]{"Patient.pk"}, true);
        	sqlBuilder.addNULLValueMatch(null,"Study.pk", false);
            execute( sqlBuilder.getSql() );
            next();
            int emptyPatients = rs.getInt(1);
            List matches = sqlBuilder.getMatches();
            matches.remove( matches.size() - 1);//removes the Study.pk NULLValue match!
            return studies + emptyPatients;
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
                    if ( rs.getBoolean(12) ) 
                       	ds.putSS(PrivateTags.HiddenStudy,1);
                }
                if ( rs.getBoolean(11) ) 
                	ds.putSS(PrivateTags.HiddenPatient,1);
                result.add(ds);
            }
            return result;
        } finally {
            close();
        }
    }
}