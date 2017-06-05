/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.Availability;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyOnFileSystemLocal;

/**
 * Used by FileSystemMgtBean
 * 
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2005-09-13 17:48:13 +0800 (周二, 13 9月 2005) $
 * @since 05.02.2005
 * 
 * @ejb.bean name="FileSystemMgtSupport" type="Stateless" view-type="local"
 *           jndi-name="ejb/FileSystemMgtSupport"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="RequiresNew"
 */

public abstract class FileSystemMgtSupportBean implements SessionBean {

    private static Logger log = Logger.getLogger(FileSystemMgtSupportBean.class);
    
    /**    
     * @ejb.interface-method
     */
    public long releaseStudy(StudyOnFileSystemLocal studyOnFs, Map ians, boolean deleteUncommited, boolean flushOnMedia,
            boolean flushExternal) throws EJBException, RemoveException,
            FinderException {
        long size = 0L;
        Dataset ian = null;

        if ( Thread.interrupted() ) {
        	log.warn("Interrupted state cleared for current thread!");
        }
        StudyLocal study = studyOnFs.getStudy();
        boolean release = flushExternal && study.isStudyExternalRetrievable() || flushOnMedia
        && study.isStudyAvailableOnMedia();
        boolean delete = deleteUncommited && study.getNumberOfCommitedInstances() == 0;
        if ( release || delete ) {
        	ian = DcmObjectFactory.getInstance().newDataset();
        	ians.put( study.getStudyIuid(), ian);
			ian.putCS(Tags.SpecificCharacterSet, "ISO_IR 100");
	        ian.putLO(Tags.PatientID,study.getPatient().getPatientId());
	        ian.putLO(Tags.PatientName,study.getPatient().getPatientName());
	        ian.putLO(Tags.StudyID,study.getStudyId());
	        ian.putLO(Tags.StudyInstanceUID,study.getStudyIuid());
	        DcmElement ppsSeq = ian.putSQ(Tags.RefPPSSeq);//We dont need this information (if available) at this point.
	        DcmElement refSerSeq = ian.putSQ(Tags.RefSeriesSeq);
        
            Collection c = studyOnFs.getFiles();
            FileLocal fileLocal;
            InstanceLocal il;
            Map seriesLocals = new HashMap();
            Map seriesSopSeq = new HashMap();
            SeriesLocal sl;
            DcmElement refSopSeq;
            for (Iterator iter = c.iterator(); iter.hasNext();) {
                fileLocal = (FileLocal) iter.next();
                if (log.isDebugEnabled())
                    log.debug("Release File:" + fileLocal.asString());
                size += fileLocal.getFileSize();
                il = fileLocal.getInstance();
                sl = il.getSeries();
                if ( ! seriesLocals.containsKey(sl.getPk()) ) {
                	seriesLocals.put(sl.getPk(), sl);
                	Dataset ds = refSerSeq.addNewItem();
                	ds.putUI(Tags.SeriesInstanceUID, sl.getSeriesIuid());
                	seriesSopSeq.put(sl.getPk(), refSopSeq = ds.putSQ(Tags.RefSOPSeq));
                } else {
                	refSopSeq = (DcmElement) seriesSopSeq.get( sl.getPk() );
                }
                Dataset refSOP = refSopSeq.addNewItem();
                refSOP.putAE(Tags.RetrieveAET, il.getRetrieveAETs());
                refSOP.putUI(Tags.RefSOPClassUID, il.getSopCuid());
                refSOP.putUI(Tags.RefSOPInstanceUID, il.getSopIuid());
                if ( release ) {
                	fileLocal.setInstance(null);
                	il.updateDerivedFields(true, true);
                	int avail = il.getAvailabilitySafe();
                    refSOP.putCS(Tags.InstanceAvailability, Availability.toString(avail));
                    if ( avail == Availability.OFFLINE ) {
                    	refSOP.putSH(Tags.StorageMediaFileSetID, il.getMedia().getFilesetId());
                    	refSOP.putUI(Tags.StorageMediaFileSetUID, il.getMedia().getFilesetIuid());
                    }
                } else {
                    refSOP.putCS(Tags.InstanceAvailability, Availability.toString(Availability.UNAVAILABLE));
                }
            }
            if (release) {
            	for (Iterator iter = seriesLocals.values().iterator(); iter.hasNext();) {
            		final SeriesLocal ser = (SeriesLocal) iter.next();
            		ser.updateDerivedFields(false, true, false, false, true);
            	}
            	study.updateDerivedFields(false, true, false, false, true, false);
            	log.info("Release Files of " + studyOnFs.asString() + " - "
                    + (size / 1000000.f) + "MB");
            	studyOnFs.remove();
			} else {
	            log.info("Delete " + studyOnFs.asString() + " - " + (size / 1000000.f)
	                    + "MB");
	            study.remove();
			}
        }
        return size;
    }
}
