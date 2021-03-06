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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.DatasetUtils;

/**
 * TODO: Cleanup
 */
public abstract class WadoQueryCmd extends BaseReadCmd {

    public static int transactionIsolationLevel = 0;

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    public static final String[] QRLEVEL = { "PATIENT", "STUDY", "SERIES",
            "IMAGE", "LOCATION"};

    private static final String[] AVAILABILITY = { "ONLINE", "NEARLINE",
            "OFFLINE", "UNAVAILABLE"};

    private static final String SR_CODE = "sr_code";

    private static final String[] SERIES_REQUEST_LEFT_JOIN = {
        "SeriesRequest", null, "Series.pk", "SeriesRequest.series_fk"};
    
    public static WadoQueryCmd create(Dataset keys, boolean filterResult,
            boolean noMatchForNoValue) throws SQLException {
        WadoQueryCmd cmd;
        String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
        switch (Arrays.asList(QRLEVEL).indexOf(qrLevel)) {
        case 0:
            cmd = new PatientQueryCmd(keys, filterResult, noMatchForNoValue);
            break;
        case 1:
            cmd = new StudyQueryCmd(keys, filterResult, noMatchForNoValue);
            break;
        case 2:
            cmd = new SeriesQueryCmd(keys, filterResult, noMatchForNoValue);
            break;
        case 3:
            cmd = new ImageQueryCmd(keys, filterResult, noMatchForNoValue);
            break;
        case 4:
            cmd = new LocationQueryCmd(keys, filterResult, noMatchForNoValue);
            break;
            
        default:
            throw new IllegalArgumentException("QueryRetrieveLevel=" + qrLevel);
        }
        cmd.init();
        return cmd;
    }

    protected final Dataset keys;

    protected final SqlBuilder sqlBuilder = new SqlBuilder();

    protected final boolean filterResult;

    protected final boolean type2;
    
    protected WadoQueryCmd(Dataset keys, boolean filterResult, boolean noMatchForNoValue)
    		throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
                transactionIsolationLevel, ResultSet.TYPE_SCROLL_INSENSITIVE);
        this.keys = keys;
        this.filterResult = filterResult;
        this.type2 = noMatchForNoValue ? SqlBuilder.TYPE1 : SqlBuilder.TYPE2;
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
        sqlBuilder.addWildCardMatch(null, "Patient.patientId", type2,
                keys.getString(Tags.PatientID));
        sqlBuilder.addPNMatch(new String[] {
                "Patient.patientName",
                "Patient.patientIdeographicName",
                "Patient.patientPhoneticName"},
                type2,
                keys.getString(Tags.PatientName));
        sqlBuilder.addRangeMatch(null, "Patient.patientBirthDate",
                type2,
                keys.getDateTimeRange(Tags.PatientBirthDate,
                        Tags.PatientBirthTime));
        sqlBuilder.addWildCardMatch(null, "Patient.patientSex",
                type2,
                keys.getString(Tags.PatientSex));
    }

    protected void addStudyMatch() {
        sqlBuilder.addLiteralMatch(null, "Study.numberOfStudyRelatedSeries",
                SqlBuilder.TYPE1,
                " != 0");
        sqlBuilder.addListOfUidMatch(null, "Study.studyIuid", SqlBuilder.TYPE1,
                keys.getStrings(Tags.StudyInstanceUID));
        sqlBuilder.addWildCardMatch(null, "Study.studyId", type2,
                keys.getString(Tags.StudyID));
        sqlBuilder.addRangeMatch(null, "Study.studyDateTime", type2,
                keys.getDateTimeRange(Tags.StudyDate, Tags.StudyTime));
        sqlBuilder.addWildCardMatch(null, "Study.accessionNumber",
                type2,
                keys.getString(Tags.AccessionNumber));
        sqlBuilder.addPNMatch(new String[] {
                "Study.referringPhysicianName",
                "Study.referringPhysicianIdeographicName",
                "Study.referringPhysicianPhoneticName"},
                type2,
                keys.getString(Tags.ReferringPhysicianName));
        sqlBuilder.addModalitiesInStudyNestedMatch(null,
                keys.getStrings(Tags.ModalitiesInStudy));
    }

    protected void addSeriesMatch() {
        sqlBuilder.addListOfUidMatch(null, "Series.seriesIuid",
                SqlBuilder.TYPE1,
                keys.getStrings(Tags.SeriesInstanceUID));
        sqlBuilder.addWildCardMatch(null, "Series.seriesNumber",
                type2,
                keys.getString(Tags.SeriesNumber));
        sqlBuilder.addWildCardMatch(null, "Series.modality", SqlBuilder.TYPE1,
                keys.getString(Tags.Modality));
        sqlBuilder.addRangeMatch(null, "Series.ppsStartDateTime",
                type2,
                keys.getDateTimeRange(Tags.PPSStartDate, Tags.PPSStartTime));
        Dataset rqAttrs = keys.getItem(Tags.RequestAttributesSeq);
        if (rqAttrs != null) {
            sqlBuilder.addWildCardMatch(null,
                    "SeriesRequest.requestedProcedureId",
                    type2,
                    rqAttrs.getString(Tags.RequestedProcedureID));
            sqlBuilder.addWildCardMatch(null,
                    "SeriesRequest.spsId",
                    type2,
                    rqAttrs.getString(Tags.SPSID));
        }

    }

    protected void addInstanceMatch() {
        sqlBuilder.addListOfUidMatch(null, "Instance.sopIuid", SqlBuilder.TYPE1,
                keys.getStrings(Tags.SOPInstanceUID));
        sqlBuilder.addListOfUidMatch(null, "Instance.sopCuid", SqlBuilder.TYPE1,
                keys.getStrings(Tags.SOPClassUID));
        sqlBuilder.addWildCardMatch(null, "Instance.instanceNumber",
                type2,
                keys.getString(Tags.InstanceNumber));
        sqlBuilder.addRangeMatch(null, "Instance.contentDateTime", type2,
                keys.getDateTimeRange(Tags.ContentDate, Tags.ContentTime));
        sqlBuilder.addWildCardMatch(null, "Instance.srCompletionFlag",
                type2,
                keys.getString(Tags.CompletionFlag));
        sqlBuilder.addWildCardMatch(null, "Instance.srVerificationFlag",
                type2,
                keys.getString(Tags.VerificationFlag));
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        if (code != null) {
            sqlBuilder.addSingleValueMatch(SR_CODE, "Code.codeValue",
                    type2,
                    code.getString(Tags.CodeValue));
            sqlBuilder.addSingleValueMatch(SR_CODE, "Code.codingSchemeDesignator",
                    type2,
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
                    for (int i = 0, n = el.countItems(); i < n; ++i) {
                        adjustDataset(el.getItem(i), keyItem);
                    }
                }
            }
        }
    }

    protected abstract void fillDataset(Dataset ds) throws SQLException;
    
    public class KeyData
	{
    	public Dataset ds;
    	public HashMap seq;
    	
    	public KeyData(Dataset ds, HashMap seq)
    	{
    		this.ds = ds;
    		this.seq = seq;
    	}
	}
    
    public void fillDatasetHierarchical(HashMap data) throws SQLException
	{
    	Long patientPk = null;
    	Long studyPk = null;
    	Long seriesPk = null;
    	Long imagePk = null;
    	Long filePk = null;
    	
    	String qrLevel = keys.getString(Tags.QueryRetrieveLevel);
    	int level = Arrays.asList(QRLEVEL).indexOf(qrLevel);
    	switch (level) {
    		case 4: // Location    	 
    			filePk = new Long(rs.getLong(14));
    		case 3: // Image
    			imagePk = new Long(rs.getLong(7)); 
    		case 2: // Series
    			seriesPk = new Long(rs.getLong(5));	 
    		case 1: // Study	        	
	        	studyPk = new Long(rs.getLong(3));	        
    		case 0: // Patient 	            
	            patientPk = new Long(rs.getInt(1));    
	            break;
	            
	        default:
	            throw new IllegalArgumentException("QueryRetrieveLevel=" + qrLevel);
    	}
    	
    	KeyData kd = getKeyData(patientPk, 2, data, false);    	
    	kd = (kd != null) ? getKeyData(studyPk, 4, kd.seq, false) : null;
    	kd = (kd != null) ? getKeyData(seriesPk, 6, kd.seq, false) : null;
    	kd = (kd != null) ? getKeyData(imagePk, 8, kd.seq, false) : null;    	
   		kd = (kd != null) ? getKeyData(filePk, -1, kd.seq, true) : null;
	}
    
    private KeyData getKeyData(Long pk, int blob, HashMap d, boolean leaf) throws SQLException
    {
    	if( pk == null )
    		return null;
    	
    	KeyData kd = null;
    	if( d.containsKey(pk) )
			kd = (KeyData)d.get(pk);
		else
		{
			Dataset ds = dof.newDataset();
			doFillDataset(ds, blob);
			kd = new KeyData(ds, leaf ? null : new HashMap());
			d.put(pk, kd);
		}
    	return kd;
    }

    public void doFillDataset(Dataset ds, int column) throws SQLException {
    	if(column > 0)
    		DatasetUtils.fromByteArray(getBytes(column), ds);
    	else
    		fillDatasetLocal(ds);
	}

    public void fillDatasetLocal(Dataset ds) throws SQLException
    {    	
    }
    
    static class PatientQueryCmd extends WadoQueryCmd {

        PatientQueryCmd(Dataset keys, boolean filterResult, boolean noMatchForNoValue) throws SQLException {
            super(keys, filterResult, noMatchForNoValue);
            // set JDBC binding for Oracle BLOB columns to LONGVARBINARY
            defineColumnType(2, Types.LONGVARBINARY);
        }

        protected void init() {
            super.init();
            addPatientMatch();
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.pk", "Patient.encodedAttributes"};
        }
        
        protected void fillDataset(Dataset ds) throws SQLException {
            doFillDataset(ds, 2);
            ds.putCS(Tags.QueryRetrieveLevel, "PATIENT");
        }

        protected String[] getTables() {
            return new String[] { "Patient"};
        }

    }

    static class StudyQueryCmd extends WadoQueryCmd {

        StudyQueryCmd(Dataset keys, boolean filterResult, boolean noMatchForNoValue)
        throws SQLException {
            super(keys, filterResult, noMatchForNoValue);
            // set JDBC binding for Oracle BLOB columns to LONGVARBINARY
            defineColumnType(2, Types.LONGVARBINARY);
            defineColumnType(4, Types.LONGVARBINARY);
        }

        protected void init() {
            super.init();
            addPatientMatch();
            addStudyMatch();
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.pk", "Patient.encodedAttributes",
                    "Study.pk","Study.encodedAttributes", "Study.modalitiesInStudy",
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
            doFillDataset(ds, 2);
            doFillDataset(ds, 4);
            ds.putCS(Tags.ModalitiesInStudy, StringUtils.split(rs.getString(5),
                    '\\'));
            ds.putIS(Tags.NumberOfStudyRelatedSeries, rs.getInt(6));
            ds.putIS(Tags.NumberOfStudyRelatedInstances, rs.getInt(6));
            ds.putSH(Tags.StorageMediaFileSetID, rs.getString(7));
            ds.putUI(Tags.StorageMediaFileSetUID, rs.getString(8));
            DatasetUtils.putRetrieveAET(ds, rs.getString(10), rs.getString(11));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(12)]);
            ds.putCS(Tags.QueryRetrieveLevel, "STUDY");
        }

    }

    static class SeriesQueryCmd extends WadoQueryCmd {

        SeriesQueryCmd(Dataset keys, boolean filterResult, boolean noMatchForNoValue)
        throws SQLException {
            super(keys, filterResult, noMatchForNoValue);
            // set JDBC binding for Oracle BLOB columns to LONGVARBINARY
            defineColumnType(2, Types.LONGVARBINARY);
            defineColumnType(4, Types.LONGVARBINARY);
            defineColumnType(6, Types.LONGVARBINARY);
        }

        protected void init() {
            super.init();
            addPatientMatch();
            addStudyMatch();
            addSeriesMatch();
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.pk", "Patient.encodedAttributes",
                    "Study.pk", "Study.encodedAttributes", 
					
					"Series.pk", "Series.encodedAttributes",
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
            doFillDataset(ds, 2);
            doFillDataset(ds, 4);
            doFillDataset(ds, 6);
            ds.putIS(Tags.NumberOfSeriesRelatedInstances, rs.getInt(7));
            ds.putSH(Tags.StorageMediaFileSetID, rs.getString(8));
            ds.putUI(Tags.StorageMediaFileSetUID, rs.getString(9));
            DatasetUtils.putRetrieveAET(ds, rs.getString(10), rs.getString(11));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(12)]);
            ds.putCS(Tags.QueryRetrieveLevel, "SERIES");
        }
    }

    static class ImageQueryCmd extends WadoQueryCmd {

        ImageQueryCmd(Dataset keys, boolean filterResult, boolean noMatchForNoValue)
        throws SQLException {
            super(keys, filterResult, noMatchForNoValue);
            // set JDBC binding for Oracle BLOB columns to LONGVARBINARY
            defineColumnType(2, Types.LONGVARBINARY);
            defineColumnType(4, Types.LONGVARBINARY);
            defineColumnType(6, Types.LONGVARBINARY);
            defineColumnType(8, Types.LONGVARBINARY);
       }

        protected void init() {
            super.init();
            addPatientMatch();
            addStudyMatch();
            addSeriesMatch();
            addInstanceMatch();
        }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.pk", "Patient.encodedAttributes",
                    "Study.pk", "Study.encodedAttributes", 
					
					"Series.pk", "Series.encodedAttributes",
                    "Instance.pk", "Instance.encodedAttributes", 
					
					"Instance.retrieveAETs",
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
            doFillDataset(ds, 2);
            doFillDataset(ds, 4);
            doFillDataset(ds, 6);
            doFillDataset(ds, 8);
            DatasetUtils.putRetrieveAET(ds, rs.getString(9), rs.getString(10));
            ds.putCS(Tags.InstanceAvailability, AVAILABILITY[rs.getInt(11)]);
            ds.putSH(Tags.StorageMediaFileSetID, rs.getString(12));
            ds.putUI(Tags.StorageMediaFileSetUID, rs.getString(13));
            ds.putCS(Tags.QueryRetrieveLevel, "IMAGE");
        }

    }

    static class LocationQueryCmd extends ImageQueryCmd {

    	LocationQueryCmd(Dataset keys, boolean filterResult, boolean noMatchForNoValue)
        throws SQLException {
            super(keys, filterResult, noMatchForNoValue);
         }

        protected String[] getSelectAttributes() {
            return new String[] { "Patient.pk", "Patient.encodedAttributes",
                    "Study.pk", "Study.encodedAttributes", 				
					"Series.pk", "Series.encodedAttributes",
                    "Instance.pk", "Instance.encodedAttributes", 
					"Instance.retrieveAETs",
                    "Instance.externalRetrieveAET", "Instance.availability",
                    "Media.filesetId", "Media.filesetIuid",
					"File.pk", "File.filePath", "File.fileTsuid", "File.fileStatus"
            		};
        }

        protected String[] getTables() {
            return new String[] { "Patient", "Study", "Series", "Instance", "File"};
        }
        
        protected String[] getRelations() {
            return new String[] { "Patient.pk", "Study.patient_fk", "Study.pk",
                    "Series.study_fk", "Series.pk", "Instance.series_fk", 
					"Instance.pk", "File.instance_fk"};
        }

        protected void fillDataset(Dataset ds) throws SQLException {
            super.fillDataset(ds); // We need image data
            
            fillDatasetLocal(ds);
        }

        public void fillDatasetLocal(Dataset ds) throws SQLException {
        	
        	ds.putUI(Tags.RefSOPTransferSyntaxUIDInFile, rs.getString(16));   
        	
        	 // TODO: need to create a private tag for this: LocationBaseUrl
            ds.putUN(Tags.ImageLocationRetired, "http://localhost:9080/wado".getBytes());
            ds.putCS(Tags.QueryRetrieveLevel, "LOCATION");
    	}
    }

    protected boolean isMatchSrCode() {
        Dataset code = keys.getItem(Tags.ConceptNameCodeSeq);
        return code != null
                && (code.containsValue(Tags.CodeValue)
                        || code.containsValue(Tags.CodingSchemeDesignator));
    }

    protected boolean isMatchRequestAttributes() {
        Dataset rqAttrs = keys.getItem(Tags.RequestAttributesSeq);
        return rqAttrs != null
                && (rqAttrs.containsValue(Tags.RequestedProcedureID)
                        || rqAttrs.containsValue(Tags.SPSID));
    }
}