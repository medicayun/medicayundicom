/* $Id: StorageBean.java 1063 2004-03-22 19:38:23Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.session;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DcmServiceException;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.ejb.conf.AttributeCoercions;
import org.dcm4chex.archive.ejb.conf.AttributeFilter;
import org.dcm4chex.archive.ejb.conf.ConfigurationException;
import org.dcm4chex.archive.ejb.interfaces.CodeLocal;
import org.dcm4chex.archive.ejb.interfaces.DuplicateStorageException;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.FileLocalHome;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.MPPSLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * Storage Bean
 * 
 * @ejb.bean name="Storage" type="Stateless" view-type="remote"
 * jndi-name="ejb/Storage"
 * 
 * @ejb.transaction-type type="Container"
 * 
 * @ejb.transaction type="Required"
 * 
 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient"
 * 
 * @ejb.ejb-ref ejb-name="Study" view-type="local" ref-name="ejb/Study"
 * 
 * @ejb.ejb-ref ejb-name="Series" view-type="local" ref-name="ejb/Series"
 * 
 * @ejb.ejb-ref ejb-name="Instance" view-type="local" ref-name="ejb/Instance"
 * 
 * @ejb.ejb-ref ejb-name="File" view-type="local" ref-name="ejb/File"
 * 
 * @ejb.env-entry name="AttributeFilterConfigURL" type="java.lang.String"
 * value="resource:dcm4jboss-attribute-filter.xml"
 * 
 * @ejb.env-entry name="AttributeCoercionConfigURL" type="java.lang.String"
 * value="resource:dcm4jboss-attribute-coercion.xml"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger </a>
 * @version $Revision: 1063 $ $Date: 2004-03-23 03:38:23 +0800 (周二, 23 3月 2004) $
 *  
 */
public abstract class StorageBean implements SessionBean {

    private static Logger log = Logger.getLogger(StorageBean.class);

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private PatientLocalHome patHome;

    private StudyLocalHome studyHome;

    private SeriesLocalHome seriesHome;

    private InstanceLocalHome instHome;

    private FileLocalHome fileHome;

    private AttributeFilter attrFilter;

    private AttributeCoercions attrCoercions;

    private SessionContext sessionCtx;

    public void setSessionContext(SessionContext ctx) {
        sessionCtx = ctx;
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome = (PatientLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Patient");
            studyHome = (StudyLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Study");
            seriesHome = (SeriesLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Series");
            instHome = (InstanceLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Instance");
            fileHome = (FileLocalHome) jndiCtx.lookup("java:comp/env/ejb/File");
            attrFilter = new AttributeFilter((String) jndiCtx
                    .lookup("java:comp/env/AttributeFilterConfigURL"));
            attrCoercions = new AttributeCoercions((String) jndiCtx
                    .lookup("java:comp/env/AttributeCoercionConfigURL"));
        } catch (NamingException e) {
            throw new EJBException(e);
        } catch (ConfigurationException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetSessionContext() {
        sessionCtx = null;
        patHome = null;
        studyHome = null;
        seriesHome = null;
        instHome = null;
        fileHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public org.dcm4che.data.Dataset store(java.lang.String callingAET,
            java.lang.String calledAET, org.dcm4che.data.Dataset ds,
            java.lang.String retrieveAETs, java.lang.String basedir,
            java.lang.String fileid, int size, byte[] md5)
            throws DcmServiceException, DuplicateStorageException {
        FileMetaInfo fmi = ds.getFileMetaInfo();
        final String iuid = fmi.getMediaStorageSOPInstanceUID();
        final String cuid = fmi.getMediaStorageSOPClassUID();
        final String tsuid = fmi.getTransferSyntaxUID();
        log.info("inserting instance " + fmi);
        try {
            Dataset coercedElements = dof.newDataset();
            InstanceLocal instance = null;
            try {
                instance = instHome.findBySopIuid(iuid);
                checkDuplicateStorage(instance, basedir, md5);
                coerceInstanceIdentity(instance, ds, coercedElements);
            } catch (ObjectNotFoundException onfe) {
                attrCoercions
                        .coerce(callingAET, calledAET, ds, coercedElements);
                instance = instHome.create(ds.subSet(attrFilter
                        .getInstanceFilter()), getSeries(ds, coercedElements));
            }
            FileLocal file = fileHome.create(retrieveAETs, basedir, fileid,
                    tsuid, size, md5, instance);
            updateRetrieveAETs(instance, retrieveAETs);
            updateAvailability(instance);
            log.info("inserted records for instance[uid=" + iuid + "]");
            return coercedElements;
        } catch (DuplicateStorageException e) {
            log.warn("ignore attempt to store instance[uid=" +  iuid + "] duplicated");
            sessionCtx.setRollbackOnly();
            throw e;
        } catch (Exception e) {
            log.error("inserting records for instance[uid=" +  iuid + "] failed:", e);
            sessionCtx.setRollbackOnly();
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }

    private void checkDuplicateStorage(InstanceLocal instance, String basedir,
            byte[] md5) throws DuplicateStorageException {
        Collection c = instance.getFiles();
        for (Iterator it = c.iterator(); it.hasNext();) {
            FileLocal file = (FileLocal) it.next();
            if (file.getDirectoryPath().equals(basedir)
                    && Arrays.equals(file.getFileMd5(), md5)) { throw new DuplicateStorageException(); }
        }
    }

    /**
     * @param instance
     * @param retrieveAETs
     */
    private void updateRetrieveAETs(InstanceLocal instance, String retrieveAETs) {
        String[] a = StringUtils.split(retrieveAETs, '\\');
        for (int i = 0; i < a.length; i++) {
            if (instance.addRetrieveAET(a[i])) {
                SeriesLocal series = instance.getSeries();
                if (series.addRetrieveAET(a[i])) {
                    StudyLocal study = series.getStudy();
                    study.addRetrieveAET(a[i]);
                }
            }
        }
    }

    private void updateAvailability(InstanceLocal instance) {
        if (instance.updateAvailability(0)) {
            SeriesLocal series = instance.getSeries();
            if (series.updateAvailability()) {
                StudyLocal study = series.getStudy();
                study.updateAvailability();
            }
        }
    }

    /**
     * @param ds
     * @return
     */
    private SeriesLocal getSeries(Dataset ds, Dataset coercedElements)
            throws FinderException, CreateException {
        final String uid = ds.getString(Tags.SeriesInstanceUID);
        SeriesLocal series;
        try {
            series = seriesHome.findBySeriesIuid(uid);
            coerceSeriesIdentity(series, ds, coercedElements);
        } catch (ObjectNotFoundException onfe) {
            series = seriesHome.create(ds.subSet(attrFilter.getSeriesFilter()),
                    getStudy(ds, coercedElements));            
            MPPSLocal mpps;
            CodeLocal drcode;
            if ((mpps = series.getMpps()) != null && mpps.isIncorrectWorklistEntrySelected()) {
                series.hide();
            }
        }
        return series;
    }

    /**
     * @param ds
     * @return
     */
    private StudyLocal getStudy(Dataset ds, Dataset coercedElements)
            throws CreateException, FinderException {
        final String uid = ds.getString(Tags.StudyInstanceUID);
        StudyLocal study;
        try {
            study = studyHome.findByStudyIuid(uid);
            coerceStudyIdentity(study, ds, coercedElements);
        } catch (ObjectNotFoundException onfe) {
            study = studyHome.create(ds.subSet(attrFilter.getStudyFilter()),
                    getPatient(ds, coercedElements));
        }

        return study;
    }

    /**
     * @param ds
     * @return
     */
    private PatientLocal getPatient(Dataset ds, Dataset coercedElements)
            throws CreateException, FinderException {
        final String id = ds.getString(Tags.PatientID);
        Collection c = patHome.findByPatientId(id);
        for (Iterator it = c.iterator(); it.hasNext();) {
            PatientLocal patient = (PatientLocal) it.next();
            if (equals(patient, ds)) {
                PatientLocal mergedWith = patient.getMergedWith();
                if (mergedWith != null) {
                    patient = mergedWith;
                }
                coercePatientIdentity(patient, ds, coercedElements);
                return patient;
            }
        }
        PatientLocal patient = patHome.create(ds.subSet(attrFilter
                .getPatientFilter()));
        return patient;
    }

    private boolean equals(PatientLocal patient, Dataset ds) {
        // TODO Auto-generated method stub
        return true;
    }

    private void coercePatientIdentity(PatientLocal patient, Dataset ds,
            Dataset coercedElements) {
        coerceIdentity(patient.getAttributes(), ds, coercedElements);
    }

    private void coerceStudyIdentity(StudyLocal study, Dataset ds,
            Dataset coercedElements) {
        coercePatientIdentity(study.getPatient(), ds, coercedElements);
        coerceIdentity(study.getAttributes(), ds, coercedElements);
    }

    private void coerceSeriesIdentity(SeriesLocal series, Dataset ds,
            Dataset coercedElements) {
        coerceStudyIdentity(series.getStudy(), ds, coercedElements);
        coerceIdentity(series.getAttributes(), ds, coercedElements);
    }

    private void coerceInstanceIdentity(InstanceLocal instance, Dataset ds,
            Dataset coercedElements) {
        coerceSeriesIdentity(instance.getSeries(), ds, coercedElements);
        coerceIdentity(instance.getAttributes(), ds, coercedElements);
    }

    private boolean coerceIdentity(Dataset ref, Dataset ds,
            Dataset coercedElements) {
        boolean coercedIdentity = false;
        for (Iterator it = ref.iterator(); it.hasNext();) {
            DcmElement refEl = (DcmElement) it.next();
            DcmElement el = ds.get(refEl.tag());
            if (!equals(el, ds.getCharset(), refEl, ref.getCharset(),
                    coercedElements)) {
                log.warn("Coerce " + el + " to " + refEl);
                if (coercedElements != null) {
                    if (VRs.isLengthField16Bit(refEl.vr())) {
                        coercedElements.putXX(refEl.tag(), refEl
                                .getByteBuffer());
                    } else {
                        coercedElements.putXX(refEl.tag());
                    }
                }
                coercedIdentity = true;
            }
        }
        return coercedIdentity;
    }

    private boolean equals(DcmElement el, Charset cs, DcmElement refEl,
            Charset refCS, Dataset coercedElements) {
        final int vm = refEl.vm();
        if (el == null || el.vm() != vm) { return false; }
        final int vr = refEl.vr();
        if (vr == VRs.OW || vr == VRs.OB || vr == VRs.UN) { 
        // no check implemented!
        return true; }
        for (int i = 0; i < vm; ++i) {
            if (vr == VRs.SQ) {
                if (coerceIdentity(refEl.getItem(i), el.getItem(i), null)) {
                    if (coercedElements != null) {
                        coercedElements.putSQ(el.tag());
                    }
                }
            } else {
                try {
                    if (!(vr == VRs.PN ? refEl.getPersonName(i, refCS).equals(
                            el.getPersonName(i, cs)) : refEl
                            .getString(i, refCS).equals(el.getString(i, cs)))) { return false; }
                } catch (DcmValueException e) {
                    log.warn("Failure during coercion of " + el, e);
                }
            }
        }
        return true;
    }

    /**
     * @ejb.interface-method
     */
    public void commit(String iuid) throws FinderException {
        instHome.findBySopIuid(iuid).setCommitment(true);
    }
}
