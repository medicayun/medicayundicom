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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.archive.common.Availability;
import org.dcm4chee.archive.common.StorageStatus;
import org.dcm4chee.archive.entity.File;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.jboss.annotation.ejb.LocalBinding;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 17, 2008
 */
@Stateless
@LocalBinding (jndiBinding=StudyListLocal.JNDI_NAME)
public class StudyListBean implements StudyListLocal {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    public int countStudies(StudyListFilter filter) {
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT COUNT(*)");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter);
        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter);
        return ((Number) query.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findStudies(StudyListFilter filter, int max, int index) {
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT p, s");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter);
        ql.append(" ORDER BY p.patientName, s.studyDateTime");
        if (filter.isLatestStudiesFirst()) {
            ql.append(" DESC");
        }
        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter);
        return query.setMaxResults(max).setFirstResult(index).getResultList();
    }

    private static void appendFromClause(StringBuilder ql, StudyListFilter filter) {
        ql.append(filter.isPatientsWithoutStudies()
                ? " FROM Patient p LEFT JOIN p.studies s"
                : " FROM Patient p INNER JOIN p.studies s");
    }

    private static void appendWhereClause(StringBuilder ql,
            StudyListFilter filter) {
        ql.append(" WHERE p.mergedWith IS NULL");
        if ( filter.isExtendedQuery() && !QueryUtil.isUniversalMatch(filter.getStudyInstanceUID())) {
            ql.append(" AND s.studyInstanceUID = :studyInstanceUID");
        } else if (filter.isExtendedQuery() && !QueryUtil.isUniversalMatch(filter.getSeriesInstanceUID())) {
            appendSeriesInstanceUIDFilter(ql, filter.getSeriesInstanceUID());
        } else {
            appendPatientNameFilter(ql, QueryUtil.checkAutoWildcard(filter.getPatientName()));
            appendPatientIDFilter(ql, QueryUtil.checkAutoWildcard(filter.getPatientID()));
            appendIssuerOfPatientIDFilter(ql, QueryUtil.checkAutoWildcard(filter.getIssuerOfPatientID()));
            if ( filter.isExtendedQuery()) {
                appendPatientBirthDateFilter(ql, filter.getBirthDateMin(), filter.getBirthDateMax());
            }
            appendAccessionNumberFilter(ql, QueryUtil.checkAutoWildcard(filter.getAccessionNumber()));
            appendPpsWithoutMwlFilter(ql, filter.isPpsWithoutMwl());
            appendStudyDateMinFilter(ql, filter.getStudyDateMin());
            appendStudyDateMaxFilter(ql, filter.getStudyDateMax());
            appendModalityFilter(ql, filter.getModality());
            appendSourceAETFilter(ql, filter.getSourceAETs());
        }
    }

    private static void setQueryParameters(Query query, StudyListFilter filter) {
        if ( filter.isExtendedQuery() && !QueryUtil.isUniversalMatch(filter.getStudyInstanceUID())) {
            setStudyInstanceUIDQueryParameter(query, filter.getStudyInstanceUID());
        } else if (filter.isExtendedQuery() && !QueryUtil.isUniversalMatch(filter.getSeriesInstanceUID())) {
            setSeriesInstanceUIDQueryParameter(query, filter.getSeriesInstanceUID());
        } else {
            setPatientNameQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getPatientName()));
            setPatientIDQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getPatientID()));
            setIssuerOfPatientIDQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getIssuerOfPatientID()));
            if ( filter.isExtendedQuery()) {
                setPatientBirthDateQueryParameter(query, filter.getBirthDateMin(), filter.getBirthDateMax());
            }
            setAccessionNumberQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getAccessionNumber()));
            setStudyDateMinQueryParameter(query, filter.getStudyDateMin());
            setStudyDateMaxQueryParameter(query, filter.getStudyDateMax());
            setModalityQueryParameter(query, filter.getModality());
            setSourceAETQueryParameter(query, filter.getSourceAETs());
        }
    }


    private static void appendPatientNameFilter(StringBuilder ql,
            String patientName) {
        QueryUtil.appendPatientName(ql, "p.patientName", ":patientName", patientName);
    }

    private static void setPatientNameQueryParameter(Query query,
            String patientName) {
        QueryUtil.setPatientNameQueryParameter(query, "patientName", patientName);
    }

    private static void appendPatientIDFilter(StringBuilder ql,
            String patientID) {
        QueryUtil.appendANDwithTextValue(ql, "p.patientID", "patientID", patientID);
    }

    private static void setPatientIDQueryParameter(Query query,
            String patientID) {
        QueryUtil.setTextQueryParameter(query, "patientID", patientID);
    }

    private static void appendIssuerOfPatientIDFilter(StringBuilder ql,
            String issuerOfPatientID) {
        QueryUtil.appendANDwithTextValue(ql, "p.issuerOfPatientID", "issuerOfPatientID", issuerOfPatientID);
    }

    private static void setIssuerOfPatientIDQueryParameter(Query query,
            String issuerOfPatientID) {
        QueryUtil.setTextQueryParameter(query, "issuerOfPatientID", issuerOfPatientID);
    }

    private static void appendPatientBirthDateFilter(StringBuilder ql, Date minDate, Date maxDate) {
        if (minDate!=null) {
            if (maxDate==null) {
                ql.append(" AND p.patientBirthDate >= :birthdateMin");
            } else {
                ql.append(" AND p.patientBirthDate BETWEEN :birthdateMin AND :birthdateMax");
                
            }
        } else if (maxDate!=null) {
            ql.append(" AND p.patientBirthDate <= :birthdateMax");
        }
    }
    private static void setPatientBirthDateQueryParameter(Query query, Date minDate, Date maxDate) {
        if ( minDate!=null)
            query.setParameter("birthdateMin", sdf.format(minDate));
        if ( maxDate!=null)
            query.setParameter("birthdateMax", sdf.format(maxDate));
    }

    private static void appendStudyDateMinFilter(StringBuilder ql, Date date) {
        if (date != null) {
            ql.append(" AND s.studyDateTime >= :studyDateTimeMin");
        }
    }

    private static void appendStudyDateMaxFilter(StringBuilder ql, Date date) {
        if (date != null) {
            ql.append(" AND s.studyDateTime <= :studyDateTimeMax");
        }
    }
    
    private static void setStudyDateMinQueryParameter(Query query, Date date) {
        setStudyDateQueryParameter(query, date, "studyDateTimeMin");
    }

    private static void setStudyDateMaxQueryParameter(Query query, Date date) {
        setStudyDateQueryParameter(query, date, "studyDateTimeMax");
    }

    private static void setStudyDateQueryParameter(Query query,
            Date studyDate, String param) {
        if (studyDate != null) {
            query.setParameter(param, studyDate, TemporalType.TIMESTAMP);
        }
    }

    private static void appendAccessionNumberFilter(StringBuilder ql,
            String accessionNumber) {
        QueryUtil.appendANDwithTextValue(ql, "s.accessionNumber", "accessionNumber", accessionNumber);
    }

    private static void setAccessionNumberQueryParameter(Query query,
            String accessionNumber) {
        QueryUtil.setTextQueryParameter(query, "accessionNumber", accessionNumber);
    }

    private static void appendPpsWithoutMwlFilter(StringBuilder ql, boolean ppsWithoutMwl) {
        if (ppsWithoutMwl) {
            ql.append(" AND EXISTS (SELECT ser FROM s.series ser WHERE "+
                    "ser.modalityPerformedProcedureStep IS NOT NULL AND ser.modalityPerformedProcedureStep.accessionNumber IS NULL)");
        }
    }
    
    private static void setStudyInstanceUIDQueryParameter(Query query,
            String studyInstanceUID) {
        if (!QueryUtil.isUniversalMatch(studyInstanceUID)) {
            query.setParameter("studyInstanceUID", studyInstanceUID);
        }
    }

    private static void appendModalityFilter(StringBuilder ql,
            String modality) {
        if (!QueryUtil.isUniversalMatch(modality)) {
            ql.append(" AND EXISTS (SELECT ser FROM s.series ser WHERE ser.modality = :modality)");
        }
    }

    private static void setModalityQueryParameter(Query query,
            String modality) {
        if (!QueryUtil.isUniversalMatch(modality)) {
            query.setParameter("modality", modality);
        }
    }

    private static void appendSourceAETFilter(StringBuilder ql,
            String[] sourceAETs) {
        if (!QueryUtil.isUniversalMatch(sourceAETs)) {
            if (sourceAETs.length == 1) {
                ql.append(" AND EXISTS (SELECT ser FROM s.series ser WHERE ser.sourceAET = :sourceAET)");
            } else {
                ql.append(" AND EXISTS (SELECT ser FROM s.series ser WHERE ser.sourceAET");
                QueryUtil.appendIN(ql, sourceAETs.length);
                ql.append(")");
            }
        }
    }

    private static void setSourceAETQueryParameter(Query query,
            String[] sourceAETs) {
        if (!QueryUtil.isUniversalMatch(sourceAETs)) {
            if (sourceAETs.length == 1) {
                query.setParameter("sourceAET", sourceAETs[0]);
            } else {
                QueryUtil.setParametersForIN(query, sourceAETs);
            }
        }
    }

    private static void appendSeriesInstanceUIDFilter(StringBuilder ql, String seriesInstanceUID) {
        if (!QueryUtil.isUniversalMatch(seriesInstanceUID)) {
            ql.append(" AND EXISTS (SELECT ser FROM s.series ser WHERE ser.seriesInstanceUID = :seriesInstanceUID)");
        }
    }
    
    private static void setSeriesInstanceUIDQueryParameter(Query query,
            String seriesInstanceUID) {
        if (!QueryUtil.isUniversalMatch(seriesInstanceUID)) {
            query.setParameter("seriesInstanceUID", seriesInstanceUID);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Study> findStudiesOfPatient(long pk, boolean latestStudyFirst) {
        return em.createQuery(latestStudyFirst
                    ? "FROM Study s WHERE s.patient.pk=?1 ORDER BY s.studyDateTime DESC"
                    : "FROM Study s WHERE s.patient.pk=?1 ORDER BY s.studyDateTime")
                .setParameter(1, pk)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Series> findSeriesOfStudy(long pk) {
        return em.createQuery("FROM Series s LEFT JOIN FETCH s.modalityPerformedProcedureStep WHERE s.study.pk=?1 ORDER BY s.pk")
                .setParameter(1, pk)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Series> findSeriesOfMpps(String uid) {
        return em.createQuery("FROM Series s WHERE s.performedProcedureStepInstanceUID=?1 ORDER BY s.pk")
                .setParameter(1, uid)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Instance> findInstancesOfSeries(long pk) {
        return em.createQuery("FROM Instance i LEFT JOIN FETCH i.media WHERE i.series.pk=?1 ORDER BY i.pk")
                .setParameter(1, pk)
                .getResultList();
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
        em.persist(series);
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
}
