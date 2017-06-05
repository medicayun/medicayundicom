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
 * @version Revision $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
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
            if ( log.isDebugEnabled() ) log.debug( "Release "+c.size()+" files from "+studyOnFs.asString() );
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
            		ser.updateDerivedFields(false, true, false, false, true, true);
            	}
            	study.updateDerivedFields(false, true, false, false, true, false, true);
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
