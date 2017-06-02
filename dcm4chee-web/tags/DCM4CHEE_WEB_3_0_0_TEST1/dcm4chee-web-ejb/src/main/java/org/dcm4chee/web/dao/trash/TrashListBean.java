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

package org.dcm4chee.web.dao.trash;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.VR;
import org.dcm4chee.archive.common.PrivateTag;
import org.dcm4chee.archive.entity.BaseEntity;
import org.dcm4chee.archive.entity.PrivateFile;
import org.dcm4chee.archive.entity.PrivateInstance;
import org.dcm4chee.archive.entity.PrivatePatient;
import org.dcm4chee.archive.entity.PrivateSeries;
import org.dcm4chee.archive.entity.PrivateStudy;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.jboss.annotation.ejb.LocalBinding;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since June 07, 2010
 */
@Stateless
@LocalBinding (jndiBinding=TrashListLocal.JNDI_NAME)
public class TrashListBean implements TrashListLocal {

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    public int countStudies(TrashListFilter filter) {
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT COUNT(*)");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter);
        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter);
        return ((Number) query.getSingleResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findStudies(TrashListFilter filter, int max, int index) {
        StringBuilder ql = new StringBuilder(64);
        ql.append("SELECT p, s");
        appendFromClause(ql, filter);
        appendWhereClause(ql, filter);
        ql.append(" ORDER BY p.patientName");
        Query query = em.createQuery(ql.toString());
        setQueryParameters(query, filter);
        return query.setMaxResults(max).setFirstResult(index).getResultList();
    }

    private static void appendFromClause(StringBuilder ql, TrashListFilter filter) {
        ql.append(filter.isPatientsWithoutStudies()
                ? " FROM PrivatePatient p LEFT JOIN p.studies s "
                : " FROM PrivatePatient p INNER JOIN p.studies s");
    }

    private static void appendWhereClause(StringBuilder ql, TrashListFilter filter) {
        ql.append(" WHERE p.privateType = 1");
        if ( QueryUtil.isUniversalMatch(filter.getStudyInstanceUID()) ) {
            appendPatientNameFilter(ql, QueryUtil.checkAutoWildcard(filter.getPatientName()));
            appendPatientIDFilter(ql, QueryUtil.checkAutoWildcard(filter.getPatientID()));
            appendIssuerOfPatientIDFilter(ql, QueryUtil.checkAutoWildcard(filter.getIssuerOfPatientID()));
            appendAccessionNumberFilter(ql, QueryUtil.checkAutoWildcard(filter.getAccessionNumber()));
        } else {
            ql.append(" AND s.studyInstanceUID = :studyInstanceUID");
        }
        appendSourceAETFilter(ql, filter.getSourceAET());
    }

    private static void setQueryParameters(Query query, TrashListFilter filter) {
        if ( QueryUtil.isUniversalMatch(filter.getStudyInstanceUID()) ) {
            setPatientNameQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getPatientName()));
            setPatientIDQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getPatientID()));
            setIssuerOfPatientIDQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getIssuerOfPatientID()));
            setAccessionNumberQueryParameter(query, QueryUtil.checkAutoWildcard(filter.getAccessionNumber()));
        } else {
            setStudyInstanceUIDQueryParameter(query, filter.getStudyInstanceUID());
        }
        setSourceAETQueryParameter(query, filter.getSourceAET());
    }

    private static void appendPatientNameFilter(StringBuilder ql, String patientName) {
        QueryUtil.appendPatientName(ql, "p.patientName", ":patientName", patientName);
    }

    private static void setPatientNameQueryParameter(Query query, String patientName) {
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

    private static void appendAccessionNumberFilter(StringBuilder ql,
            String accessionNumber) {
        QueryUtil.appendANDwithTextValue(ql, "s.accessionNumber", "accessionNumber", accessionNumber);
    }

    private static void setAccessionNumberQueryParameter(Query query,
            String accessionNumber) {
        QueryUtil.setTextQueryParameter(query, "accessionNumber", accessionNumber);
    }

    private static void setStudyInstanceUIDQueryParameter(Query query,
            String studyInstanceUID) {
        if (!QueryUtil.isUniversalMatch(studyInstanceUID)) {
            query.setParameter("studyInstanceUID", studyInstanceUID);
        }
    }


    private static void appendSourceAETFilter(StringBuilder ql,
            String sourceAET) {
        if (!QueryUtil.isUniversalMatch(sourceAET)) {
            ql.append(" AND EXISTS (SELECT ser FROM s.series ser WHERE ser.sourceAET = :sourceAET)");
        }
    }

    private static void setSourceAETQueryParameter(Query query,
            String sourceAET) {
        if (!QueryUtil.isUniversalMatch(sourceAET)) {
            query.setParameter("sourceAET", sourceAET);
        }
    }

    @SuppressWarnings("unchecked")
    public List<PrivateStudy> findStudiesOfPatient(long pk) {
        return em.createQuery("FROM PrivateStudy s WHERE s.patient.pk=?1")
                .setParameter(1, pk)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<PrivateSeries> findSeriesOfStudy(long pk) {
        return em.createQuery("FROM PrivateSeries s WHERE s.study.pk=?1 ORDER BY s.pk")
                .setParameter(1, pk)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<PrivateInstance> findInstancesOfSeries(long pk) {
        return em.createQuery("FROM PrivateInstance i WHERE i.series.pk=?1 ORDER BY i.pk")
                .setParameter(1, pk)
                .getResultList();
   }

    @SuppressWarnings("unchecked")
    public List<String> selectDistinctSourceAETs() {
        return em.createQuery("SELECT DISTINCT s.sourceAET FROM Series s WHERE s.sourceAET IS NOT NULL ORDER BY s.sourceAET")
                .getResultList();
    }

    public PrivatePatient getPatient(long pk) {
        return em.find(PrivatePatient.class, pk);
    }

    public PrivateStudy getStudy(long pk) {
        return em.find(PrivateStudy.class, pk);
    }

    public PrivateSeries getSeries(long pk) {
        return em.find(PrivateSeries.class, pk);
    }

    public PrivateInstance getInstance(long pk) {
        return em.find(PrivateInstance.class, pk);
    }
    
    public void removeTrashEntities(List<Long> pks, Class<? extends BaseEntity> clazz, boolean removeFile) {
        
        if (clazz.equals(PrivatePatient.class)) {
            for (Long pk : pks)
                removeTrashEntity(getPatient(pk), removeFile);
        } else if (clazz.equals(PrivateStudy.class)) {
            for (Long pk : pks)
                removeTrashEntity(getStudy(pk), removeFile);
        } else if (clazz.equals(PrivateSeries.class)) {
            for (Long pk : pks)
                removeTrashEntity(getSeries(pk), removeFile);
        } else if (clazz.equals(PrivateInstance.class)) {
            for (Long pk : pks)
                removeTrashEntity(getInstance(pk), removeFile);
        }
    }
    
    private void removeTrashEntity(BaseEntity entity, boolean removeFile) {
        if (entity == null) return;
        else {
            if (entity instanceof PrivatePatient) {
                PrivatePatient pp = (PrivatePatient) entity;
                for (PrivateStudy pst : pp.getStudies())
                    removeTrashEntity(pst, removeFile);
                em.remove(pp);
            } else if (entity instanceof PrivateStudy) {
                PrivateStudy pst = (PrivateStudy) entity;
                for (PrivateSeries pse : pst.getSeries())
                    removeTrashEntity(pse, removeFile);
                PrivatePatient p = pst.getPatient();
                em.remove(pst);
                if (p.getStudies().size() <= 1)
                    em.remove(p);
            } else if (entity instanceof PrivateSeries) {
                PrivateSeries pse = (PrivateSeries) entity;
                for (PrivateInstance pi : pse.getInstances())
                    removeTrashEntity(pi, removeFile);
                PrivateStudy pst = pse.getStudy();
                em.remove(pse);
                if (pst.getSeries().size() <= 1)
                    em.remove(pst);
            } else if (entity instanceof PrivateInstance) {
                PrivateInstance pi = (PrivateInstance) entity;
                for (PrivateFile pf : pi.getFiles()) {
                    if (removeFile) {
                        em.remove(pf);
                    } else {
                        pf.setInstance(null);
                        em.merge(pf);
                    }
                }
                PrivateSeries pse = pi.getSeries();
                em.remove(pi);
                if (pse.getInstances().size() <= 1)
                    em.remove(pse);
            } else return;
        }
    }
    
    public void removeTrashAll() {
        em.createQuery("UPDATE PrivateFile p SET p.instance = Null").executeUpdate();
        em.createQuery("DELETE FROM PrivateInstance pi").executeUpdate();
        em.createQuery("DELETE FROM PrivateSeries pse").executeUpdate();
        em.createQuery("DELETE FROM PrivateStudy pst").executeUpdate();
        em.createQuery("DELETE FROM PrivatePatient pp").executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public List<PrivateFile> getFilesForEntity(long pk, Class<? extends BaseEntity> clazz) {
        
        String query = "SELECT DISTINCT f FROM PrivateFile f LEFT JOIN FETCH f.fileSystem fs ";

        if (clazz.equals(PrivateInstance.class))
            query += "WHERE f.instance.pk = :pk";
        else {
            query += "LEFT JOIN f.instance.series se ";
            
            if (clazz.equals(PrivateSeries.class))
                query += "WHERE se.pk = :pk";
            else {
                query += "LEFT JOIN se.study st ";
                if (clazz.equals(PrivateStudy.class))
                    query += "WHERE st.pk = :pk";
                else if (clazz.equals(PrivatePatient.class))
                    query += "LEFT JOIN st.patient p WHERE p.pk = :pk";
                else return null;
            }
        }
        
        return em.createQuery(query)
            .setParameter("pk", pk)
            .getResultList();
    }

    public DicomObject getDicomAttributes(long filePk) {
        PrivateFile pf = em.find(PrivateFile.class, filePk);
        DicomObject dio = pf.getInstance().getAttributes();
        pf.getInstance().getSeries().getAttributes().copyTo(dio);
        pf.getInstance().getSeries().getStudy().getAttributes().copyTo(dio);
        pf.getInstance().getSeries().getStudy().getPatient().getAttributes().copyTo(dio);
        dio.putString(dio.resolveTag(PrivateTag.CallingAET, PrivateTag.CreatorID), 
                VR.AE, pf.getInstance().getSeries().getSourceAET());
        return dio;
    }

    public Long getNumberOfSeriesOfStudy(long studyPk) {
        return (Long) em.createQuery("SELECT COUNT(s) from PrivateSeries s WHERE s.study.pk = :studyPk")
        .setParameter("studyPk", studyPk)
        .getSingleResult();
    }
    
    public Long getNumberOfInstancesOfStudy(long studyPk) {
        return (Long) em.createQuery("SELECT DISTINCT COUNT(i) FROM PrivateInstance i, PrivateSeries se , PrivateStudy st WHERE i.series.pk = se.pk AND se.study.pk = st.pk AND st.pk = :studyPk")
        .setParameter("studyPk", studyPk)
        .getSingleResult();
    }

    public Long getNumberOfInstancesOfSeries(long seriesPk) {
        return (Long) em.createQuery("SELECT COUNT(i) from PrivateInstance i WHERE i.series.pk = :seriesPk")
        .setParameter("seriesPk", seriesPk)
        .getSingleResult();
    }
}
