/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.session;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
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
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.ejb.conf.AttributeCoercions;
import org.dcm4chex.archive.ejb.conf.AttributeFilter;
import org.dcm4chex.archive.ejb.conf.ConfigurationException;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.FileLocalHome;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocal;
import org.dcm4chex.archive.ejb.interfaces.FileSystemLocalHome;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * Storage Bean
 * 
 * @ejb.bean name="Storage"
 *      type="Stateless"
 *      view-type="remote"
 *      jndi-name="ejb/Storage"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb.ejb-ref ejb-name="Patient"
 *              view-type="local"
 *              ref-name="ejb/Patient"
 * @ejb.ejb-ref ejb-name="Study"
 *              view-type="local"
 *              ref-name="ejb/Study"
 * @ejb.ejb-ref ejb-name="Series"
 *              view-type="local"
 *              ref-name="ejb/Series"
 * @ejb.ejb-ref ejb-name="Instance"
 *              view-type="local"
 *              ref-name="ejb/Instance"
 * @ejb.ejb-ref ejb-name="File"
 *              view-type="local"
 *              ref-name="ejb/File"
 * @ejb.ejb-ref ejb-name="FileSystem"
 *              view-type="local"
 *              ref-name="ejb/FileSystem"
 * 
 * @ejb.env-entry name="AttributeFilterConfigURL"
 *                type="java.lang.String"
 *                value="resource:dcm4jboss-attribute-filter.xml"
 * @ejb.env-entry name="AttributeCoercionConfigURL"
 *                type="java.lang.String"
 *                value="resource:dcm4jboss-attribute-coercion.xml"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger </a>
 * @version $Revision: 1530 $ $Date: 2005-02-12 01:04:56 +0800 (周六, 12 2月 2005) $
 *  
 */
public abstract class StorageBean implements SessionBean {

    private static final int ForbiddenAttributeCoercion = 0xCB00;
    
	private static Logger log = Logger.getLogger(StorageBean.class);

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private PatientLocalHome patHome;

    private StudyLocalHome studyHome;

    private SeriesLocalHome seriesHome;

    private InstanceLocalHome instHome;

    private FileLocalHome fileHome;

    private FileSystemLocalHome fileSystemHome;

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
            fileSystemHome = (FileSystemLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/FileSystem");
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
        fileSystemHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public org.dcm4che.data.Dataset store(java.lang.String callingAET,
            java.lang.String calledAET, org.dcm4che.data.Dataset ds,
            java.lang.String retrieveAET, java.lang.String dirpath,
            java.lang.String fileid, int size,
            byte[] md5) throws DcmServiceException {
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
                coerceInstanceIdentity(instance, ds, coercedElements);
            } catch (ObjectNotFoundException onfe) {
                attrCoercions.coerce(callingAET, calledAET, ds, coercedElements);
                final int[] filter = attrFilter.getInstanceFilter();
                instance = instHome.create(ds.subSet(filter),
                        getSeries(ds, coercedElements));
            }
            FileSystemLocal fs;
            try {
                fs = fileSystemHome.findByDirectoryPath(dirpath);
            } catch (ObjectNotFoundException onfe) {
                fs = fileSystemHome.create(dirpath, retrieveAET);
            }
            instance.setAvailability(Availability.ONLINE);
            instance.addRetrieveAET(fs.getRetrieveAET());
            FileLocal file = fileHome.create(fileid,
                    tsuid,
                    size,
                    md5,
                    instance,
                    fs);
            log.info("inserted records for instance[uid=" + iuid + "]");
            return coercedElements;
        } catch (Exception e) {
            log.error("inserting records for instance[uid=" + iuid
                    + "] failed:", e);
            sessionCtx.setRollbackOnly();
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }

    private SeriesLocal getSeries(Dataset ds, Dataset coercedElements)
            throws FinderException, CreateException, DcmServiceException {
        final String uid = ds.getString(Tags.SeriesInstanceUID);
        SeriesLocal series;
        try {
            series = seriesHome.findBySeriesIuid(uid);
            coerceSeriesIdentity(series, ds, coercedElements);
        } catch (ObjectNotFoundException onfe) {
            final int[] filter = attrFilter.getSeriesFilter();
            series = seriesHome.create(ds.subSet(filter),
                    getStudy(ds, coercedElements));
        }
        return series;
    }

    /**
     * @param ds
     * @return
     * @throws DcmServiceException 
     * @throws IllegalAttributeCoercionException 
     */
    private StudyLocal getStudy(Dataset ds, Dataset coercedElements)
            throws CreateException, FinderException, DcmServiceException {
        final String uid = ds.getString(Tags.StudyInstanceUID);
        StudyLocal study;
        try {
            study = studyHome.findByStudyIuid(uid);
            coerceStudyIdentity(study, ds, coercedElements);
        } catch (ObjectNotFoundException onfe) {
            final int[] filter = attrFilter.getStudyFilter();
            study = studyHome.create(ds.subSet(filter),
                    getPatient(ds, coercedElements));
        }

        return study;
    }

    /**
     * @param ds
     * @return
     * @throws DcmServiceException 
     * @throws IllegalAttributeCoercionException 
     */
    private PatientLocal getPatient(Dataset ds, Dataset coercedElements)
            throws CreateException, FinderException, DcmServiceException {
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
        final int[] filter = attrFilter.getPatientFilter();
        PatientLocal patient = patHome.create(ds.subSet(filter));
        return patient;
    }

    private boolean equals(PatientLocal patient, Dataset ds) {
        // TODO Auto-generated method stub
        return true;
    }

    private void coercePatientIdentity(PatientLocal patient, Dataset ds,
            Dataset coercedElements) throws DcmServiceException {
        coerceIdentity(patient.getAttributes(false), ds, coercedElements);
    }

    private void coerceStudyIdentity(StudyLocal study, Dataset ds,
            Dataset coercedElements) throws DcmServiceException {
        coercePatientIdentity(study.getPatient(), ds, coercedElements);
        coerceIdentity(study.getAttributes(false), ds, coercedElements);
    }

    private void coerceSeriesIdentity(SeriesLocal series, Dataset ds,
            Dataset coercedElements) throws DcmServiceException {
        coerceStudyIdentity(series.getStudy(), ds, coercedElements);
        coerceIdentity(series.getAttributes(false), ds, coercedElements);
    }

    private void coerceInstanceIdentity(InstanceLocal instance, Dataset ds,
            Dataset coercedElements) throws DcmServiceException {
        coerceSeriesIdentity(instance.getSeries(), ds, coercedElements);
        coerceIdentity(instance.getAttributes(false), ds, coercedElements);
    }

    private boolean coerceIdentity(Dataset ref, Dataset ds,
            Dataset coercedElements) throws DcmServiceException {
        boolean coercedIdentity = false;
        for (Iterator it = ref.iterator(); it.hasNext();) {
            DcmElement refEl = (DcmElement) it.next();
            final int tag = refEl.tag();
			DcmElement el = ds.get(tag);
            if (!equals(el,
                    ds.getCharset(),
                    refEl,
                    ref.getCharset(),
                    coercedElements)) {
            	if (attrFilter.isCoercionForbidden(tag)) {
            		throw new DcmServiceException(ForbiddenAttributeCoercion,
            				"Storage would require forbidden Coercion of " + el + " to " + refEl);
            	}
                log.warn("Coerce " + el + " to " + refEl);
                if (coercedElements != null) {
                    if (VRs.isLengthField16Bit(refEl.vr())) {
                        coercedElements.putXX(tag, refEl
                                .getByteBuffer());
                    } else {
                        coercedElements.putXX(tag);
                    }
                }
                coercedIdentity = true;
            }
        }
        return coercedIdentity;
    }

    private boolean equals(DcmElement el, Charset cs, DcmElement refEl,
            Charset refCS, Dataset coercedElements)
    		throws DcmServiceException {
        final int vm = refEl.vm();
        if (el == null || el.vm() != vm) { return false; }
        final int vr = refEl.vr();
        if (vr == VRs.OW || vr == VRs.OB || vr == VRs.UN) {
            // no check implemented!
            return true;
        }
        for (int i = 0; i < vm; ++i) {
            if (vr == VRs.SQ) {
                if (coerceIdentity(refEl.getItem(i), el.getItem(i), null)) {
                    if (coercedElements != null) {
                        coercedElements.putSQ(el.tag());
                    }
                }
            } else {
                try {
                    if (!(vr == VRs.PN ? refEl.getPersonName(i, refCS)
                            .equals(el.getPersonName(i, cs)) : refEl
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
    
    /**
     * @ejb.interface-method
     */
    public void commited(Dataset stgCmtResult) throws FinderException {
        DcmElement refSOPSeq = stgCmtResult.get(Tags.RefSOPSeq);
        if (refSOPSeq == null) return;
        HashSet seriesSet = new HashSet();
        HashSet studySet = new HashSet();
        final String aet0 = stgCmtResult.getString(Tags.RetrieveAET);
        for (int i = 0, n = refSOPSeq.vm(); i < n; ++i) {
            final Dataset refSOP = refSOPSeq.getItem(i);
            final String iuid = refSOP.getString(Tags.RefSOPInstanceUID);
            final String aet = refSOP.getString(Tags.RetrieveAET, aet0);
            if (iuid != null && aet != null)
            	commited(seriesSet, studySet, iuid, aet);
        }
        for (Iterator series = seriesSet.iterator(); series.hasNext();) {
            final SeriesLocal ser = seriesHome.findBySeriesIuid((String) series.next());
			ser.updateDerivedFields(false, false, true, false, false);
        }
        for (Iterator studies = studySet.iterator(); studies.hasNext();) {
            final StudyLocal study = studyHome.findByStudyIuid((String) studies.next());
			study.updateDerivedFields(false, false, true, false, false, false);
        }
    }

    private void commited(HashSet seriesSet, HashSet studySet, final String iuid, final String aet) throws FinderException {
		InstanceLocal inst = instHome.findBySopIuid(iuid);
		inst.setExternalRetrieveAET(aet);
		SeriesLocal series = inst.getSeries();
		seriesSet.add(series.getSeriesIuid());
		StudyLocal study = series.getStudy();
		studySet.add(study.getStudyIuid());
	}
    
    /**
     * @ejb.interface-method
     */
    public void updateStudy(String iuid) throws FinderException {
        final StudyLocal study = studyHome.findByStudyIuid(iuid);
		study.updateDerivedFields(true, true, false, true, true, true);
    }
    
    /**
     * @ejb.interface-method
     */
    public void updateSeries(String iuid) throws FinderException {
        final SeriesLocal series = seriesHome.findBySeriesIuid(iuid);
        series.updateDerivedFields(true, true, false, true, true);
    }
}