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

package org.dcm4chex.archive.ejb.session;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DcmServiceException;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.common.SeriesStored;
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
import org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocalHome;

/**
 * Storage Bean
 * 
 * @ejb.bean name="Storage" type="Stateless" view-type="remote" 
 * 			 jndi-name="ejb/Storage"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"
 * 
 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient"
 * @ejb.ejb-ref ejb-name="Study"  view-type="local" ref-name="ejb/Study"
 * @ejb.ejb-ref ejb-name="Series" view-type="local" ref-name="ejb/Series"
 * @ejb.ejb-ref ejb-name="Instance" view-type="local" ref-name="ejb/Instance"
 * @ejb.ejb-ref ejb-name="File" view-type="local" ref-name="ejb/File"
 * @ejb.ejb-ref ejb-name="FileSystem" view-type="local" ref-name="ejb/FileSystem"
 * @ejb.ejb-ref ejb-name="StudyOnFileSystem" view-type="local" ref-name="ejb/StudyOnFileSystem"
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger </a>
 * @version $Revision: 3392 $ $Date: 2007-06-19 22:18:50 +0800 (周二, 19 6月 2007) $
 *  
 */
public abstract class StorageBean implements SessionBean {

    private static final int STORED = 0;
	private static final int RECEIVED = 1;

    private static Logger log = Logger.getLogger(StorageBean.class);

    private PatientLocalHome patHome;

    private StudyLocalHome studyHome;

    private SeriesLocalHome seriesHome;

    private InstanceLocalHome instHome;

    private FileLocalHome fileHome;

    private FileSystemLocalHome fileSystemHome;
    
    private StudyOnFileSystemLocalHome sofHome;
    
    private SessionContext sessionCtx;
    
    private static final int MAX_PK_CACHE_ENTRIES = 100;
    private static Map seriesPkCache = Collections.synchronizedMap(
     new LinkedHashMap() {
        protected boolean removeEldestEntry(Map.Entry eldest) {
           return size() > MAX_PK_CACHE_ENTRIES;
        }
    });

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
            sofHome = (StudyOnFileSystemLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/StudyOnFileSystem");
        } catch (NamingException e) {
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
        sofHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public org.dcm4che.data.Dataset store(org.dcm4che.data.Dataset ds,
            long fspk, java.lang.String fileid, long size, byte[] md5)
    throws DcmServiceException {
        FileMetaInfo fmi = ds.getFileMetaInfo();
        final String iuid = fmi.getMediaStorageSOPInstanceUID();
        final String cuid = fmi.getMediaStorageSOPClassUID();
        final String tsuid = fmi.getTransferSyntaxUID();
        log.info("inserting instance " + fmi);
        try {
            Dataset coercedElements = DcmObjectFactory.getInstance().newDataset();
            FileSystemLocal fs = fileSystemHome.findByPrimaryKey(new Long(fspk));
            InstanceLocal instance;
            try {
                instance = instHome.findBySopIuid(iuid);
                coerceInstanceIdentity(instance, ds, coercedElements);
            } catch (ObjectNotFoundException onfe) {
                instance = instHome.create(ds, getSeries(ds, coercedElements, fs));
            }
            FileLocal file = fileHome.create(fileid, tsuid, size, md5, 0,
                    instance, fs);
            instance.setAvailability(Availability.ONLINE);
            instance.addRetrieveAET(fs.getRetrieveAET());
            instance.setInstanceStatus(RECEIVED);
            instance.getSeries().setSeriesStatus(RECEIVED);
            log.info("inserted records for instance[uid=" + iuid + "]");
            return coercedElements;
        } catch (Exception e) {
            log.error("inserting records for instance[uid=" + iuid
                    + "] failed:", e);
            sessionCtx.setRollbackOnly();
            if (e instanceof DcmServiceException) {
                throw (DcmServiceException) e;                
            } else {
                throw new DcmServiceException(Status.ProcessingFailure, e);
            }
        }
    }

    /**
     * @ejb.interface-method
     */
    public SeriesStored makeSeriesStored(String seriuid)
    throws FinderException {
        SeriesLocal series = findBySeriesIuid(seriuid);
        return makeSeriesStored(series);
    }

    /**
     * @ejb.interface-method
     */
    public void commitSeriesStored(SeriesStored seriesStored)
    throws FinderException {
        Dataset ian = seriesStored.getIAN();
        Dataset refSeries = ian.get(Tags.RefSeriesSeq).getItem(0);
        DcmElement refSOPs = refSeries.get(Tags.RefSOPSeq);
        int numI = refSOPs.countItems();
        HashSet iuids = new HashSet(numI * 4 / 3 + 1);
        for (int i = 0; i < numI; i++) {
            iuids.add(refSOPs.getItem(i).getString(Tags.RefSOPInstanceUID));
        }
        String seriuid = refSeries.getString(Tags.SeriesInstanceUID);
        SeriesLocal series = findBySeriesIuid(seriuid);
        Collection c = series.getInstances();
        int remaining = 0;
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            InstanceLocal inst = (InstanceLocal) iter.next();
            if (inst.getInstanceStatus() != RECEIVED) {
                continue;
            }
            if (iuids.remove(inst.getSopIuid())) {
                inst.setInstanceStatus(STORED);
            } else {
                ++remaining;
            }
        }
        if (remaining == 0) {
            series.setSeriesStatus(STORED);
        }
    }
    
    /**
     * @ejb.interface-method
     */
    public SeriesStored[] checkSeriesStored(long maxPendingTime)
    throws FinderException {
        Timestamp before = new Timestamp(System.currentTimeMillis() 
                - maxPendingTime);
        Collection c = seriesHome.findByStatusReceivedBefore(RECEIVED, before);
        if (c.isEmpty()) {
            return new SeriesStored[0]; 
        }
        log.info("Found " + c.size() + " Stored Series");
        ArrayList list = new ArrayList(c.size());
        SeriesStored seriesStored;
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            seriesStored = makeSeriesStored((SeriesLocal) iter.next());
            if (seriesStored != null) {
                list.add(seriesStored);
            }
        }
        return (SeriesStored[]) list.toArray(new SeriesStored[list.size()]);
    }
    
    private SeriesStored makeSeriesStored(SeriesLocal series) {
        StudyLocal study = series.getStudy();
        PatientLocal pat = study.getPatient();
        Dataset seriesAttrs = series.getAttributes(true);
        Dataset studyAttrs = study.getAttributes(true);
        Dataset patAttrs = pat.getAttributes(true);
        Dataset ian = DcmObjectFactory.getInstance().newDataset();
        ian.putUI(Tags.StudyInstanceUID, study.getStudyIuid());
        DcmElement refPPSSeq = ian.putSQ(Tags.RefPPSSeq);
        Dataset pps = seriesAttrs.getItem(Tags.RefPPSSeq);
        if (pps != null) {
            if (!pps.contains(Tags.PerformedWorkitemCodeSeq)) {
                pps.putSQ(Tags.PerformedWorkitemCodeSeq);
            }
            refPPSSeq.addItem(pps);
        }
        Dataset refSeries = ian.putSQ(Tags.RefSeriesSeq).addNewItem();
        DcmElement refSOPs = refSeries.putSQ(Tags.RefSOPSeq);
        refSeries.putUI(Tags.SeriesInstanceUID, series.getSeriesIuid());
        Collection c = series.getInstances();
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            InstanceLocal inst = (InstanceLocal) iter.next();
            if (inst.getInstanceStatus() != RECEIVED) {
                continue;
            }
            Dataset refSOP = refSOPs.addNewItem();           
            refSOP.putUI(Tags.RefSOPClassUID, inst.getSopCuid());
            refSOP.putUI(Tags.RefSOPInstanceUID, inst.getSopIuid());
            refSOP.putAE(Tags.RetrieveAET, StringUtils.split(
                    inst.getRetrieveAETs(), '\\')[0]);
            refSOP.putCS(Tags.InstanceAvailability, "ONLINE");
        }
        if (refSOPs.countItems() == 0) {
            return null;
        }
        SeriesStored seriesStored = new SeriesStored(patAttrs, studyAttrs,
                seriesAttrs, ian);
        return seriesStored;
    }
    
    /**
     * @ejb.interface-method
     */
    public void storeFile(java.lang.String iuid, java.lang.String tsuid,
    		java.lang.String dirpath, java.lang.String fileid,
    		int size, byte[] md5, int status)
    throws CreateException, FinderException
    {
		FileSystemLocal fs = fileSystemHome.findByDirectoryPath(dirpath);
		InstanceLocal instance = instHome.findBySopIuid(iuid);
        fileHome.create(fileid, tsuid, size, md5, status, instance, fs);    	
    }

    private SeriesLocal getSeries(Dataset ds, Dataset coercedElements, FileSystemLocal fs)
            throws Exception {
        final String uid = ds.getString(Tags.SeriesInstanceUID);
        SeriesLocal series;
        try {
            series = findBySeriesIuid(uid);
            coerceSeriesIdentity(series, ds, coercedElements);
        } catch (ObjectNotFoundException onfe) {
            series = seriesHome.create(ds, getStudy(ds, coercedElements, fs));
        }
        return series;
    }

    /**
     * @param ds
     * @param fs 
     * @return
     */
    private StudyLocal getStudy(Dataset ds, Dataset coercedElements, FileSystemLocal fs)
            throws Exception {
        final String uid = ds.getString(Tags.StudyInstanceUID);
        StudyLocal study;
        try {
            study = studyHome.findByStudyIuid(uid);
            coerceStudyIdentity(study, ds, coercedElements);
        } catch (ObjectNotFoundException onfe) {
            study = studyHome.create(ds, getPatient(ds, coercedElements));
            sofHome.create(study, fs);
        }

        return study;
    }

    private PatientLocal getPatient(Dataset ds, Dataset coercedElements)
            throws Exception {
        try {
            PatientLocal pat = patHome.searchFor(ds, true);
            coercePatientIdentity(pat, ds, coercedElements);
            return pat;
        } catch (ObjectNotFoundException e) {
            return patHome.create(ds);
        }
    }
    
    private void coercePatientIdentity(PatientLocal patient, Dataset ds,
            Dataset coercedElements) throws DcmServiceException, CreateException {
        patient.coerceAttributes(ds, coercedElements);
    }

    private void coerceStudyIdentity(StudyLocal study, Dataset ds,
            Dataset coercedElements) throws DcmServiceException, CreateException {
        coercePatientIdentity(study.getPatient(), ds, coercedElements);
        study.coerceAttributes(ds, coercedElements);
    }

    private void coerceSeriesIdentity(SeriesLocal series, Dataset ds,
            Dataset coercedElements) throws DcmServiceException, CreateException {
        coerceStudyIdentity(series.getStudy(), ds, coercedElements);
        series.coerceAttributes(ds, coercedElements);
    }

    private void coerceInstanceIdentity(InstanceLocal instance, Dataset ds,
            Dataset coercedElements) throws DcmServiceException, CreateException {
        coerceSeriesIdentity(instance.getSeries(), ds, coercedElements);
        instance.coerceAttributes(ds, coercedElements);
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
        for (int i = 0, n = refSOPSeq.countItems(); i < n; ++i) {
            final Dataset refSOP = refSOPSeq.getItem(i);
            final String iuid = refSOP.getString(Tags.RefSOPInstanceUID);
            final String aet = refSOP.getString(Tags.RetrieveAET, aet0);
            if (iuid != null && aet != null)
            	commited(seriesSet, studySet, iuid, aet);
        }
        for (Iterator series = seriesSet.iterator(); series.hasNext();) {
            final SeriesLocal ser = findBySeriesIuid((String) series.next());
			ser.updateDerivedFields(false, false, true, false, false);
        }
        for (Iterator studies = studySet.iterator(); studies.hasNext();) {
            final StudyLocal study = studyHome.findByStudyIuid((String) studies.next());
			study.updateDerivedFields(false, false, true, false, false, false);
        }
    }

    private void commited(HashSet seriesSet, HashSet studySet,
            final String iuid, final String aet) throws FinderException {
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
   		final SeriesLocal series = findBySeriesIuid(iuid);
   		series.updateDerivedFields(true, true, false, true, true);
    }
    
    /**
     * @ejb.interface-method
     */
    public void deleteInstances(String[] iuids, boolean deleteSeries, 
    		boolean deleteStudy) 
    throws FinderException, EJBException, RemoveException
    {
    	for (int i = 0; i < iuids.length; i++)
    	{
    		InstanceLocal inst = instHome.findBySopIuid(iuids[i]);
    		SeriesLocal series = inst.getSeries();
    		StudyLocal study = series.getStudy();
    		inst.remove();
    		series.updateDerivedFields(true, true, true, true, true);
    		if (deleteSeries && series.getNumberOfSeriesRelatedInstances() == 0)
    			series.remove();	    	
	    	study.updateDerivedFields(true, true, true, true, true, true);
	    	if (deleteStudy && study.getNumberOfStudyRelatedSeries() == 0)
	    		study.remove();
    	}
    }
    
    private SeriesLocal findBySeriesIuid(String uid) throws javax.ejb.FinderException {
    	Long pk = (Long)seriesPkCache.get(uid);
    	if (pk != null) {
            try { 
                return seriesHome.findByPrimaryKey(pk);
            } catch ( ObjectNotFoundException x ) {
                log.warn("Series "+uid+" not found with cached pk! Cache entry removed!");
                seriesPkCache.remove(uid);
            }
    	}
    	SeriesLocal ser = seriesHome.findBySeriesIuid(uid);
    	seriesPkCache.put(uid, ser.getPk());
    	return ser;
    }
    
    /**
     * @throws FinderException 
     * @ejb.interface-method
     */
    public boolean patientExistsWithDifferentDetails(Dataset ds, int[] detailTags) throws FinderException {
        String pid = ds.getString(Tags.PatientID);
        String issuer = ds.getString(Tags.IssuerOfPatientID);
        Collection c = issuer != null
                ? patHome.findByPatientIdWithIssuer(pid, issuer)
                : patHome.findByPatientId(pid);
        for ( Iterator iter = c.iterator(); iter.hasNext() ; ) {
            PatientLocal patient = (PatientLocal) iter.next();
            if(! checkDetails(ds, patient.getAttributes(false), detailTags)) {
                String suid = null;
                suid = ds.getString(Tags.StudyInstanceUID);
                if ( suid != null ) {
                    try {
                        studyHome.findByStudyIuid(suid);
                        log.info("Different patient details found but Study Instance UID ("+suid+") already exists! Patient ID not changed!");
                        return false;
                    } catch ( ObjectNotFoundException ignore ) {
                    }
                }
                suid = ds.getString(Tags.SeriesInstanceUID);
                if ( suid != null ) {
                    try {
                        seriesHome.findBySeriesIuid(suid);
                        log.info("Different patient details found but Series Instance UID ("+suid+") already exists! Patient ID not changed!");
                        return false;
                    } catch ( ObjectNotFoundException ignore ) {
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Check if given Detail Attributes are equal in both Datasets.
     * <p>
     * PN attributes are checked case insensitive!
     * <p>
     * SQ attributes are checked via SQElement equals method. 
     * (therefore PN attributes within a Sequence are NOT checked case insensitive!)
     * 
     * @param ds1           Dataset 1: missing detailAttributes are not checked
     * @param ds2           Dataset 2: missing detailAttributes are checked if also missing in ds1!
     * @param detailTags    List of detail attributes to check
     * 
     * @return true if all given details are equal.
     */
    private boolean checkDetails(Dataset ds1, Dataset ds2, int[] detailTags) {
        DcmElement elem1, elem2;
        int tag;
        for ( int i = 0 ; i < detailTags.length ; i++ ) {
            tag = detailTags[i];
            elem1 = ds1.get(tag);
            if ( elem1 != null ) {
                elem2 = ds2.get(tag);
                if ( elem2 == null ) return false; //
                if ( elem1.vr() == VRs.PN ) {
                    if ( ! ds1.getString(tag).equalsIgnoreCase(ds2.getString(tag)) )
                        return false;
                } else {
                    if ( ! elem1.equals(elem2))
                        return false;
                }
            } //else ignore detail check for attributes that are not in ds1!
        }
        return true;
    }

}

