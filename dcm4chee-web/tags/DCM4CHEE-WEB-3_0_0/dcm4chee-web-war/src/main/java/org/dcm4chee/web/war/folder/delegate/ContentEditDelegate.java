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

package org.dcm4chee.web.war.folder.delegate;

import java.io.IOException;
import java.util.Collection;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.UIDUtils;
import org.dcm4chee.archive.common.Availability;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.web.common.delegate.BaseMBeanDelegate;
import org.dcm4chee.web.common.exceptions.SelectionException;
import org.dcm4chee.web.dao.vo.MppsToMwlLinkResult;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.folder.MoveEntitiesPage;
import org.dcm4chee.web.war.folder.SelectedEntities;
import org.dcm4chee.web.war.folder.model.InstanceModel;
import org.dcm4chee.web.war.folder.model.PPSModel;
import org.dcm4chee.web.war.folder.model.SeriesModel;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.dcm4chee.web.war.worklist.modality.model.MWLItemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 18, 2009
 */
public class ContentEditDelegate extends BaseMBeanDelegate {

    private static ContentEditDelegate delegate;

    private static Logger log = LoggerFactory.getLogger(ContentEditDelegate.class);

    private ContentEditDelegate() {
        super();
    }

    public boolean moveToTrash(SelectedEntities selected) {
        try {
            if ( selected.hasPatients()) {
                moveToTrash("movePatientsToTrash", toPks(selected.getPatients()));
            }
            if ( selected.hasStudies()) {
                moveToTrash("moveStudiesToTrash", toPks(selected.getStudies()));
            }
            if ( selected.hasPPS()) {
                moveToTrash("moveSeriesOfPpsToTrash", toPks(selected.getPpss()));
            }
            if ( selected.hasSeries()) {
                moveToTrash("moveSeriesToTrash", toPks(selected.getSeries()));
            }
            if ( selected.hasInstances()) {
                moveToTrash("moveInstancesToTrash", toPks(selected.getInstances()));
            }
        } catch (Exception x) {
            String msg = "Move to trash failed! Reason:"+x.getMessage();
            log.error(msg,x);
            return false;
        }
        return true;
    }
    
    public boolean deletePps(SelectedEntities selected) {
        try {
            server.invoke(serviceObjectName, "deletePps", 
                new Object[]{toPks(selected.getPpss())}, 
                new String[]{long[].class.getName()});
        } catch (Exception x) {
            String msg = "Delete PPS failed! Reason:"+x.getMessage();
            log.error(msg,x);
            return false;
        }
        return true;
    }

    private void moveToTrash(String op, long[] pks)
            throws InstanceNotFoundException, MBeanException,
            ReflectionException, IOException {
        server.invoke(serviceObjectName, op, new Object[]{pks}, 
                new String[]{long[].class.getName()});
    }

    public int moveEntities(SelectedEntities selected) throws SelectionException {
        try {
            if (selected.getPpss().size() > 0) {
                throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVE_PPS, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_PPS);
            } 
            checkAllOnline(selected);
            // study(series,inst) -> pat
            int pats = selected.getPatients().size();
            if (pats > 1) {
                throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVE_DESTINATION, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_DESTINATION);
            } 
            if( pats == 1) {
                long patPk = selected.getPatients().iterator().next().getPk();
                if (selected.getStudies().size() < 1) {
                    if ( selected.hasSeries() && selected.hasInstances()) {
                        throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVE_SOURCE_LEVEL, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_SOURCE_LEVEL);
                    } else if (selected.hasSeries()) {
                        Study st = createNewStudy(selected.getSeries().iterator().next().getPPS().getStudy(), patPk);
                        return moveEntities("moveSeriesToStudy", st.getPk(), toPks(selected.getSeries()));
                    } else if (selected.hasInstances()) {
                        SeriesModel sModel = selected.getInstances().iterator().next().getSeries();
                        Study st = createNewStudy(sModel.getPPS().getStudy(), patPk);
                        Series series = createNewSeries(sModel, st.getPk());
                        return moveEntities("moveInstancesToSeries", series.getPk(), toPks(selected.getInstances()));
                    } else {
                        throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVE_NO_SOURCE, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_NO_SOURCE);
                    }
                } else {
                    return moveEntities("moveStudiesToPatient", patPk, toPks(selected.getStudies()));
                }
            }
            // series(inst) -> study
            int nrOfStudies = selected.getStudies().size();
            if ( nrOfStudies > 1) {
                throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVE_DESTINATION, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_DESTINATION);
            } 
            if( nrOfStudies == 1) {
                if (selected.getSeries().size() < 1) {
                    if ( selected.hasInstances()) {
                        SeriesModel sModel = selected.getInstances().iterator().next().getSeries();
                        Series series = createNewSeries(sModel, selected.getStudies().iterator().next().getPk());
                        return moveEntities("moveInstancesToSeries", series.getPk(), toPks(selected.getInstances()));
                    } else {
                        throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVE_NO_SOURCE, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_NO_SOURCE);
                    }
                }
                return moveEntities("moveSeriesToStudy", toPks(selected.getStudies())[0], toPks(selected.getSeries()));
            }
            // instances -> series
            int nrOfSeries = selected.getSeries().size();
            if ( nrOfSeries > 1) {
                throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVE_DESTINATION, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_DESTINATION);
            } 
            if( nrOfSeries == 1) {
                if (selected.getInstances().size() < 1) {
                    throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVE_NO_SOURCE, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_NO_SOURCE);
                }
                return moveEntities("moveInstancesToSeries", toPks(selected.getSeries())[0], toPks(selected.getInstances()));
            }
            throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVE_NO_SELECTION, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_NO_SELECTION);
        } catch (SelectionException x) {
            throw x;
        } catch (Exception x) {
            log.error("Move selected Entities failed! Reason:"+x.getMessage(),x);
            return -1;
        }
    }
    
    public int removeForeignPpsInfo(long studyPk) {
        try {
            return (Integer) server.invoke(serviceObjectName, "removeForeignPpsInfo", 
                new Object[]{studyPk}, 
                new String[]{long.class.getName()});
        } catch (Exception x) {
            log.error("Failed to remove foreign PPS Info for study! pk:"+studyPk, x);
            return -1;
        }
    }

    private void checkAllOnline(SelectedEntities selected) throws SelectionException {
        for ( StudyModel m : selected.getStudies()) {
            if (Availability.valueOf(m.getAvailability()) != Availability.ONLINE)
                throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVENOT_ONLINE, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_NOT_ONLINE);
        }
        for ( SeriesModel m : selected.getSeries()) {
            if (Availability.valueOf(m.getAvailability()) != Availability.ONLINE)
                throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVENOT_ONLINE, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_NOT_ONLINE);
        }
        for ( InstanceModel m : selected.getInstances()) {
            if (Availability.valueOf(m.getAvailability()) != Availability.ONLINE)
                throw new SelectionException(MoveEntitiesPage.MSG_ERR_SELECTION_MOVENOT_ONLINE, MoveEntitiesPage.MSGID_ERR_SELECTION_MOVE_NOT_ONLINE);
        }
    }

    private Study createNewStudy(StudyModel baseStudy, long patPk) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        DicomObject studyAttrs = baseStudy == null ? new BasicDicomObject() : baseStudy.getDataset();
        DicomObject newStudyAttrs = new BasicDicomObject();
        newStudyAttrs.putString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID());
        newStudyAttrs.putString(Tag.StudyDescription, VR.LO, studyAttrs.getString(Tag.StudyDescription));
        newStudyAttrs.putDate(Tag.StudyDate, VR.DA, studyAttrs.getDate(Tag.StudyDate));
        newStudyAttrs.putDate(Tag.StudyTime, VR.TM, studyAttrs.getDate(Tag.StudyTime));
        Study study = (Study) server.invoke(serviceObjectName, "createStudy", 
                new Object[]{newStudyAttrs, patPk}, 
                new String[]{DicomObject.class.getName(), long.class.getName()});
        return study;
    }

    private Series createNewSeries(SeriesModel baseSeries, long studyPk) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        DicomObject seriesAttrs = baseSeries == null ? new BasicDicomObject() : baseSeries.getDataset();
        DicomObject newSeriesAttrs = new BasicDicomObject();
        newSeriesAttrs.putString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        newSeriesAttrs.putString(Tag.SeriesDescription, VR.LO, seriesAttrs.getString(Tag.SeriesDescription));
        newSeriesAttrs.putDate(Tag.SeriesDate, VR.DA, seriesAttrs.getDate(Tag.SeriesDate));
        newSeriesAttrs.putDate(Tag.SeriesTime, VR.TM, seriesAttrs.getDate(Tag.SeriesTime));
        Series series = (Series) server.invoke(serviceObjectName, "createSeries", 
                new Object[]{newSeriesAttrs, studyPk}, 
                new String[]{DicomObject.class.getName(), long.class.getName()});
        return series;
    }
    
    private int  moveEntities(String op, long pk, long[] pks)
            throws InstanceNotFoundException, MBeanException,
            ReflectionException, IOException {
        return (Integer) server.invoke(serviceObjectName, op, new Object[]{pks, pk}, 
        new String[]{long[].class.getName(), long.class.getName()});
    }
    
    public MppsToMwlLinkResult linkMppsToMwl(Collection<PPSModel> ppsModels, MWLItemModel mwl) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        MppsToMwlLinkResult result = (MppsToMwlLinkResult) server.invoke(serviceObjectName, "linkMppsToMwl", 
                new Object[]{toPks(ppsModels), mwl.getPk(), null, null}, 
                new String[]{long[].class.getName(), long.class.getName(), String.class.getName(), String.class.getName()});
        return result;
    }
    
    public boolean unlink(PPSModel mpps)  throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        boolean status = (Boolean) server.invoke(serviceObjectName, "unlinkMpps", 
                new Object[]{mpps.getPk()}, 
                new String[]{long.class.getName()});
        mpps.getStudy().expand();
        return status;
    }
    
    private long[] toPks(Collection<? extends AbstractDicomModel> models) {
        long[] pks = new long[models.size()];
        int i=0;
        for (AbstractDicomModel m : models) {
            pks[i++] = m.getPk();
        }
        return pks;
    }

    @Override
    public String getServiceNameCfgAttribute() {
        return "contentEditServiceName";
    }

    public static ContentEditDelegate getInstance() {
        if (delegate==null)
            delegate = new ContentEditDelegate();
        return delegate;
    }
}
