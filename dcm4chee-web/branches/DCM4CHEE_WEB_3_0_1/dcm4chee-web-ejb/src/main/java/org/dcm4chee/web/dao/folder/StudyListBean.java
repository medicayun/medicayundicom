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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4chee.web.dao.folder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.archive.common.Availability;
import org.dcm4chee.archive.common.StorageStatus;
import org.dcm4chee.archive.entity.File;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.StudyPermission;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.jboss.annotation.ejb.LocalBinding;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since Dec 17, 2008
 */
@Stateless
@LocalBinding(jndiBinding=StudyListLocal.JNDI_NAME)
public class StudyListBean implements StudyListLocal {

    private static Comparator<Instance> instanceComparator = new Comparator<Instance>() {
        public int compare(Instance o1, Instance o2) {
            String in1 = o1.getInstanceNumber();
            String in2 = o2.getInstanceNumber();
            return QueryUtil.compareIntegerStringAndPk(o1.getPk(), o2.getPk(), in1, in2);
        }
    };
    private static Comparator<Series> seriesComparator = new Comparator<Series>() {
        public int compare(Series o1, Series o2) {
            String in1 = o1.getSeriesNumber();
            String in2 = o2.getSeriesNumber();
            return QueryUtil.compareIntegerStringAndPk(o1.getPk(), o2.getPk(), in1, in2);
        }

    };

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;
    
    public int count(StudyListFilter filter, List<String> roles) {
        if ((roles != null) && (roles.size() == 0)) return 0;
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT COUNT(*)");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter, roles);
        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter, roles);
        return ((Number) query.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Patient> findPatients(StudyListFilter filter, int max, int index, List<String> roles) {
        if ((roles != null) && (roles.size() == 0)) return new ArrayList<Patient>();
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT p");
        if (!filter.isPatientQuery())
            ql.append(", s");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter, roles);
        String studyDT = filter.isPatientQuery() ? null : 
            filter.isLatestStudiesFirst() ? "s.studyDateTime DESC" : "s.studyDateTime";
        QueryUtil.appendOrderBy(ql, new String[]{"p.patientName", studyDT});
        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter, roles);
        if (filter.isPatientQuery()) {
            return query.setMaxResults(max).setFirstResult(index).getResultList();
        } else {
            List<Object[]> result = query.setMaxResults(max).setFirstResult(index).getResultList();
            List<Patient> patientList = new ArrayList<Patient>();
            Patient patient = null;
            for (Object[] element: result) {
                patient = (Patient) element[0];
                if (!patientList.contains(patient)) {
                    patient.setStudies(new LinkedHashSet<Study>());
                    patientList.add(patient);
                }
                patient.getStudies().add((Study) element[1]);
            }
            return patientList;
        }
    }

    private static void appendFromClause(StringBuilder ql, StudyListFilter filter) {
        ql.append(" FROM Patient p");
        if (!filter.isPatientQuery() ) 
            ql.append(" INNER JOIN p.studies s");
    }

    private void appendWhereClause(StringBuilder ql, StudyListFilter filter, List<String> roles) {
        ql.append(" WHERE p.mergedWith IS NULL");
        if ( filter.isPatientQuery()) {
            appendPatFilter(ql, filter);
        } else {
            if ( filter.isExtendedQuery() && !QueryUtil.isUniversalMatch(filter.getStudyInstanceUID())) {
                ql.append(" AND s.studyInstanceUID = :studyInstanceUID");
            } else if (filter.isExtendedQuery() && !QueryUtil.isUniversalMatch(filter.getSeriesInstanceUID())) {
                QueryUtil.appendSeriesInstanceUIDFilter(ql, filter.getSeriesInstanceUID());
            } else {
                appendPatFilter(ql, filter);
                QueryUtil.appendAccessionNumberFilter(ql, QueryUtil.checkAutoWildcard(filter.getAccessionNumber(), filter.isAutoWildcard()));
                QueryUtil.appendPpsWithoutMwlFilter(ql, filter.isWithoutPps(), filter.isPpsWithoutMwl());
                QueryUtil.appendStudyDateMinFilter(ql, filter.getStudyDateMin());
                QueryUtil.appendStudyDateMaxFilter(ql, filter.getStudyDateMax());
                if (filter.isExactModalitiesInStudy()) {
                    QueryUtil.appendModalitiesInStudyExactFilter(ql, filter.getModality());
                } else {
                    QueryUtil.appendModalityFilter(ql, filter.getModality());
                }
                QueryUtil.appendSourceAETFilter(ql, filter.getSourceAETs());
            }
            if ((roles != null) && !filter.isPatientQuery())
                QueryUtil.appendDicomSecurityFilter(ql);
        }
    }

    private static void appendPatFilter(StringBuilder ql, StudyListFilter filter) {
        if (filter.isFuzzyPN()) {
            QueryUtil.appendPatientNameFuzzyFilter(ql, filter.getPatientName());
        } else {
            QueryUtil.appendPatientNameFilter(ql, QueryUtil.checkAutoWildcard(filter.getPatientName(), filter.isPNAutoWildcard()));
        }
        QueryUtil.appendPatientIDFilter(ql, QueryUtil.checkAutoWildcard(filter.getPatientID(), filter.isAutoWildcard()));
        QueryUtil.appendIssuerOfPatientIDFilter(ql, QueryUtil.checkAutoWildcard(filter.getIssuerOfPatientID(), filter.isAutoWildcard()));
        if ( filter.isExtendedQuery()) {
            QueryUtil.appendPatientBirthDateFilter(ql, filter.getBirthDateMin(), filter.getBirthDateMax());
        }
    }

    private static void setQueryParameters(Query query, StudyListFilter filter, List<String> roles) {
        if ( filter.isPatientQuery()) {
            setPatQueryParameters(query, filter);
        } else {
            if ( filter.isExtendedQuery() && !QueryUtil.isUniversalMatch(filter.getStudyInstanceUID())) {
                QueryUtil.setStudyInstanceUIDQueryParameter(query, filter.getStudyInstanceUID());
            } else if (filter.isExtendedQuery() && !QueryUtil.isUniversalMatch(filter.getSeriesInstanceUID())) {
                QueryUtil.setSeriesInstanceUIDQueryParameter(query, filter.getSeriesInstanceUID());
            } else {
                setPatQueryParameters(query, filter);
                QueryUtil.setAccessionNumberQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getAccessionNumber(), filter.isAutoWildcard()));
                QueryUtil.setStudyDateMinQueryParameter(query, filter.getStudyDateMin());
                QueryUtil.setStudyDateMaxQueryParameter(query, filter.getStudyDateMax());
                QueryUtil.setModalityQueryParameter(query, filter.getModality());
                QueryUtil.setSourceAETQueryParameter(query, filter.getSourceAETs());
            }
            if ((roles != null) && !filter.isPatientQuery())
                query.setParameter("roles", roles);
            if (!QueryUtil.isUniversalMatch(filter.getModality()) && filter.isExactModalitiesInStudy()) 
                query.setParameter("modality", filter.getModality());
        }
    }

    private static void setPatQueryParameters(Query query, StudyListFilter filter) {
        if (filter.isFuzzyPN()) {
            QueryUtil.setPatientNameFuzzyQueryParameter(query, filter.getPatientName());
        } else {
            QueryUtil.setPatientNameQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getPatientName(), filter.isPNAutoWildcard()));
        }
        QueryUtil.setPatientIDQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getPatientID(), filter.isAutoWildcard()));
        QueryUtil.setIssuerOfPatientIDQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getIssuerOfPatientID(), filter.isAutoWildcard()));
        if ( filter.isExtendedQuery()) {
            QueryUtil.setPatientBirthDateQueryParameter(query, filter.getBirthDateMin(), filter.getBirthDateMax());
        }
    }


    public int countStudiesOfPatient(long pk, List<String> roles) {
        if ((roles != null) && (roles.size() == 0)) return 0;
        return ((Number) getStudiesOfPatientQuery(true, pk, false, roles).getSingleResult()).intValue();
    }
    
    @SuppressWarnings("unchecked")
    public List<Study> findStudiesOfPatient(long pk, boolean latestStudyFirst, List<String> roles) {
        if ((roles != null) && (roles.size() == 0)) return new ArrayList<Study>();
        return getStudiesOfPatientQuery(false, pk, latestStudyFirst, roles).getResultList();
    }
        
    private Query getStudiesOfPatientQuery(boolean isCount, long pk, boolean latestStudyFirst, List<String> roles) {
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT " + (isCount ? "COUNT(s)" : "s") + " FROM Study s WHERE s.patient.pk=?1");
        if (roles != null)
            QueryUtil.appendDicomSecurityFilter(ql);
        if (!isCount)
            ql.append(latestStudyFirst
                  ? " ORDER BY s.studyDateTime DESC"
                  : " ORDER BY s.studyDateTime");
        Query query = em.createQuery(ql.toString());
        query.setParameter(1, pk);
        if (roles != null)
            query.setParameter("roles", roles);
        return query;
    }

    public boolean isActionForAllStudiesOfPatientAllowed(long patPk, String action, List<String> roles) {
        if (roles == null)
            return true;
        if (roles.isEmpty())
            return false;
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT COUNT(s) FROM Study s WHERE s.patient.pk = ?1")
        .append(" AND (s.studyInstanceUID NOT IN (SELECT sp.studyInstanceUID FROM StudyPermission sp WHERE sp.action = ?2 AND sp.role IN (:roles)))");
        Query query = em.createQuery(ql.toString());
        query.setParameter(1, patPk);
        query.setParameter(2, action);
        query.setParameter("roles", roles);
        return (((Number) query.getSingleResult()).intValue() == 0);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> findStudyPermissionActions(String studyInstanceUID, List<String> roles) {
        if(roles == null) return null;
        return ((roles != null) && (roles.size()) > 0) ? 
                em.createQuery("SELECT DISTINCT sp.action FROM StudyPermission sp WHERE sp.studyInstanceUID = :studyInstanceUID AND role IN (:roles)")
                    .setParameter("studyInstanceUID", studyInstanceUID)
                    .setParameter("roles", roles)
                    .getResultList()
                : new ArrayList<String>();
    }
    
    @SuppressWarnings("unchecked")
    public List<Series> findSeriesOfStudy(long pk) {
        return sortSeries(em.createQuery("FROM Series s LEFT JOIN FETCH s.modalityPerformedProcedureStep WHERE s.study.pk=?1 ORDER BY s.seriesNumber, s.pk")
                .setParameter(1, pk)
                .getResultList());
    }
    public int countSeriesOfStudy(long pk) {
        return ((Number) em.createQuery("SELECT COUNT(s) FROM Series s WHERE s.study.pk=?1")
                .setParameter(1, pk)
                .getSingleResult()).intValue();
    }
    
    public Series findSeriesByIuid(String iuid) {
        Query q = em.createQuery("FROM Series s LEFT JOIN FETCH s.modalityPerformedProcedureStep WHERE s.seriesInstanceUID = :iuid");
        q.setParameter("iuid", iuid);
        return (Series) q.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public List<Series> findSeriesOfMpps(String uid) {
        return sortSeries(em.createQuery("FROM Series s WHERE s.performedProcedureStepInstanceUID=?1 ORDER BY s.pk")
                .setParameter(1, uid)
                .getResultList());
    }

    private List<Series> sortSeries(List<Series> l) {
        Collections.sort(l, seriesComparator);
        return l;
    }

    @SuppressWarnings("unchecked")
    public List<Instance> findInstancesOfSeries(long pk) {
        List<Instance> l = em.createQuery("FROM Instance i LEFT JOIN FETCH i.media WHERE i.series.pk=?1 ORDER BY i.pk")
                .setParameter(1, pk)
                .getResultList();
        Collections.sort(l, instanceComparator);
        return l;
    }

    public int countInstancesOfSeries(long pk) {
        return ((Number) em.createQuery("SELECT COUNT(i) FROM Instance i WHERE i.series.pk=?1")
                .setParameter(1, pk)
                .getSingleResult()).intValue();
    }
    
    @SuppressWarnings("unchecked")
    public List<File> findFilesOfInstance(long pk) {
        return em.createQuery("FROM File f JOIN FETCH f.fileSystem WHERE f.instance.pk=?1 ORDER BY f.pk")
                .setParameter(1, pk)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> selectDistinctSourceAETs() {
        return em.createQuery("SELECT DISTINCT s.sourceAET FROM Series s WHERE s.sourceAET IS NOT NULL ORDER BY s.sourceAET")
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> selectDistinctModalities() {
        return em.createQuery("SELECT DISTINCT s.modality FROM Series s WHERE s.modality IS NOT NULL ORDER BY s.modality")
                .getResultList();
    }

    public Patient getPatient(long pk) {
        return em.find(Patient.class, pk);
    }

    public Patient updatePatient(long pk, DicomObject attrs) {
        Patient patient = em.find(Patient.class, pk);
        patient.setAttributes(attrs);
        return patient;
    }

    public Study getStudy(long pk) {
        return em.find(Study.class, pk);
    }

    public Study updateStudy(long pk, DicomObject attrs) {
        Study study = em.find(Study.class, pk);
        study.setAttributes(attrs);
        return study;
    }

    public Study addStudy(long patPk, DicomObject attrs) {
        Patient pat = em.find(Patient.class, patPk);
        Study study = new Study();
        study.setAttributes(attrs);
        study.setPatient(pat);
        study.setAvailability(Availability.ONLINE);
        em.persist(study);
        return study;
    }

    public void copyStudyPermissions(String srcStudyIuid, String destStudyIuid) {
        Query query = em.createQuery("SELECT sp FROM StudyPermission sp WHERE studyInstanceUID=?1");
        query.setParameter(1, srcStudyIuid);
        @SuppressWarnings("unchecked")
        List<StudyPermission> l = (List<StudyPermission>)query.getResultList();
        StudyPermission studyPermission;
        for (StudyPermission sp : l) {
            studyPermission = new StudyPermission();
            studyPermission.setAction(sp.getAction());
            studyPermission.setRole(sp.getRole());
            studyPermission.setStudyInstanceUID(destStudyIuid);
            em.persist(studyPermission);
        }
    }

    public Series getSeries(long pk) {
        return em.find(Series.class, pk);
    }

    public Series updateSeries(long pk, DicomObject attrs) {
        Series series = em.find(Series.class, pk);
        series.setAttributes(attrs);
        return series;
    }
    public Series addSeries(long studyPk, DicomObject attrs) {
        Study study = em.find(Study.class, studyPk);
        Series series = new Series();
        series.setAttributes(attrs);
        series.setStudy(study);
        series.setAvailability(Availability.ONLINE);
        series.setNumberOfSeriesRelatedInstances(0);
        series.setStorageStatus(StorageStatus.STORED);
        study.setNumberOfStudyRelatedSeries(study.getNumberOfStudyRelatedSeries()+1);
        em.persist(series);
        em.persist(study);
        return series;
    }

    public Instance getInstance(long pk) {
        return em.find(Instance.class, pk);
    }
    
    public Instance updateInstance(long pk, DicomObject attrs) {
        Instance inst = em.find(Instance.class, pk);
        inst.setAttributes(attrs);
        return inst;
    }

    public MPPS getMPPS(long pk) {
        return em.find(MPPS.class, pk);
    }

    public MPPS updateMPPS(long pk, DicomObject attrs) {
        MPPS mpps = em.find(MPPS.class, pk);
        mpps.setAttributes(attrs);
        return mpps;
    }

    public long countDownloadableInstances(String[] studyIuids, String[] seriesIuids, String[] sopIuids) {
        if (studyIuids == null && seriesIuids == null && sopIuids == null)
            return 0;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT count(i) FROM Instance i WHERE ");
        String[] uids = appendUIDs(studyIuids, seriesIuids, sopIuids, sb);
        Query q = em.createQuery(sb.toString());
        QueryUtil.setParametersForIN(q, uids);
        return (Long) q.getSingleResult();
    }


    @SuppressWarnings("unchecked")
    public List<Instance> getDownloadableInstances(String[] studyIuids, String[] seriesIuids, String[] sopIuids) {
        List<Instance> instances;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT i FROM Instance i JOIN FETCH i.series s JOIN FETCH s.study st JOIN FETCH st.patient WHERE ");
        String[] uids = appendUIDs(studyIuids, seriesIuids, sopIuids, sb);
        if (uids == null)
            return null;
        Query q = em.createQuery(sb.toString());
        QueryUtil.setParametersForIN(q, uids);
        instances = (List<Instance>) q.getResultList();
        for (Instance instance : instances) {
            for (File file : instance.getFiles()) 
                file.getFileSystem().getDirectoryPath();
        }       
        return instances;
    }
    
    public boolean hasStudyForeignPpsInfo(long studyPk) {
        return ((Long)em.createQuery("SELECT count(s) FROM Series s WHERE s.study.pk=?1 AND s.performedProcedureStepInstanceUID IS NOT NULL AND s.modalityPerformedProcedureStep IS NULL")
                .setParameter(1, studyPk).getSingleResult()) > 0l;
    }

    private String[] appendUIDs(String[] studyIuids, String[] seriesIuids,
            String[] sopIuids, StringBuilder sb) {
        String[] uids;
        if (sopIuids != null) {
            uids = sopIuids;
            sb.append("i.sopInstanceUID");
        } else if (seriesIuids != null) {
            uids = seriesIuids;
            sb.append("i.series.seriesInstanceUID");
        } else if (studyIuids != null) {
            uids = studyIuids;
            sb.append("i.series.study.studyInstanceUID");
        } else {
            return null;
        }
        QueryUtil.appendIN(sb, uids.length);
        return uids;
    }
}
