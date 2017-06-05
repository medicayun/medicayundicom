/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;
import javax.servlet.http.HttpServletRequest;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.dcm.movescu.MoveOrder;
import org.dcm4chex.archive.ejb.interfaces.AEManager;
import org.dcm4chex.archive.ejb.interfaces.AEManagerHome;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.JMSDelegate;
import org.dcm4chex.archive.web.maverick.model.AbstractModel;
import org.dcm4chex.archive.web.maverick.model.InstanceModel;
import org.dcm4chex.archive.web.maverick.model.PatientModel;
import org.dcm4chex.archive.web.maverick.model.SeriesModel;
import org.dcm4chex.archive.web.maverick.model.StudyFilterModel;
import org.dcm4chex.archive.web.maverick.model.StudyModel;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1700 $ $Date: 2005-04-15 20:50:21 +0800 (周五, 15 4月 2005) $
 * @since 28.01.2004
 */
public class FolderSubmitCtrl extends FolderCtrl {
    
	public static final int MOVE_ERROR = -1;
	public static final int MOVE_STUDIES = 0;
	public static final int MOVE_SERIES = 1;
	public static final int MOVE_INSTANCES = 2;
	
    private static final int MOVE_PRIOR = 0;
    
    private static ContentEditDelegate delegate = null;

    public static ContentEditDelegate getDelegate() {
    	return delegate;
    }
    
	/**
	 * Get the model for the view.
	 * @throws 
	 */
    protected Object makeFormBean() {
        if ( delegate == null ) {
        	delegate = new ContentEditDelegate();
        	try {
        		delegate.init( getCtx() );
        	} catch( Exception x ) {
        		log.error("Cant make form bean!", x );
        	}
        }
        return super.makeFormBean();
    }
    
    protected String perform() throws Exception {
        try {
            FolderForm folderForm = (FolderForm) getForm();
    		folderForm.setErrorCode( FolderForm.NO_ERROR );//reset error code
            setSticky(folderForm.getStickyPatients(), "stickyPat");
            setSticky(folderForm.getStickyStudies(), "stickyStudy");
            setSticky(folderForm.getStickySeries(), "stickySeries");
            setSticky(folderForm.getStickyInstances(), "stickyInst");
            HttpServletRequest rq = getCtx().getRequest();
            if (rq.getParameter("filter") != null
                    || rq.getParameter("filter.x") != null) { return query(true); }
            if (rq.getParameter("prev") != null
                    || rq.getParameter("prev.x") != null
                    || rq.getParameter("next") != null
                    || rq.getParameter("next.x") != null) { return query(false); }
            if (rq.getParameter("send") != null
                    || rq.getParameter("send.x") != null) { return send(); }
            if (rq.getParameter("del") != null
                    || rq.getParameter("del.x") != null) { return delete(); }
            if (rq.getParameter("merge") != null
                    || rq.getParameter("merge.x") != null) { return MERGE; }
            if (rq.getParameter("move") != null
                    || rq.getParameter("move.x") != null) { return move(); }
            if (rq.getParameter("showStudyIUID") != null ) folderForm.setShowStudyIUID( "true".equals( rq.getParameter("showStudyIUID") ) ); 
            if (rq.getParameter("showSeriesIUID") != null ) folderForm.setShowSeriesIUID( "true".equals( rq.getParameter("showSeriesIUID") ) ); 

            return FOLDER;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String query(boolean newQuery) throws Exception {

        ContentManager cm = lookupContentManager();

        try {
            FolderForm folderForm = (FolderForm) getForm();
            StudyFilterModel filter;
            try {
            	filter = folderForm.getStudyFilter();
            } catch ( NumberFormatException x ) {
            	folderForm.setErrorCode( ERROR_PARSE_DATE );
            	return FOLDER;
            }
            if (newQuery) {
                folderForm.setTotal(cm.countStudies(filter.toDataset()));
                folderForm.setAets(lookupAEManager().getAes());
            }
            List studyList = cm.listStudies(filter.toDataset(), folderForm
                    .getOffset(), folderForm.getLimit());
            List patList = new ArrayList();
            PatientModel curPat = null;
            for (int i = 0, n = studyList.size(); i < n; i++) {
                Dataset ds = (Dataset) studyList.get(i);
                PatientModel pat = new PatientModel(ds);
                if (!pat.equals(curPat)) {
                    patList.add(curPat = pat);
                }
                StudyModel study = new StudyModel(ds);
                if (study.getPk() != -1) {
                    curPat.getStudies().add(study);
                }
            }

            folderForm.updatePatients(patList);
        } finally {
            try {
                cm.remove();
            } catch (Exception e) {
            }
        }
        return FOLDER;
    }

    private String send() throws Exception {
        FolderForm folderForm = (FolderForm) getForm();
        List patients = folderForm.getPatients();
        for (int i = 0, n = patients.size(); i < n; i++) {
            PatientModel pat = (PatientModel) patients.get(i);
            if (folderForm.isSticky(pat))
                scheduleMoveStudiesOfPatient(pat.getPk());
            else
                scheduleMoveStudies(pat.getStudies());
        }
        return FOLDER;
    }

    private void scheduleMoveStudiesOfPatient(int pk) throws Exception {
        List studies = listStudiesOfPatient(pk);
        ArrayList uids = new ArrayList();
        for (int i = 0, n = studies.size(); i < n; i++) {
            final Dataset ds = (Dataset) studies.get(i);
            uids.add(ds.getString(Tags.StudyInstanceUID));
        }
        if (!uids.isEmpty()) {
            scheduleMove((String[]) uids.toArray(new String[uids.size()]),
                    null,
                    null);
        }
    }

    private void scheduleMoveStudies(List studies) {
        FolderForm folderForm = (FolderForm) getForm();
        ArrayList uids = new ArrayList();
        for (int i = 0, n = studies.size(); i < n; i++) {
            final StudyModel study = (StudyModel) studies.get(i);
            final String studyIUID = study.getStudyIUID();
            if (folderForm.isSticky(study))
                uids.add(studyIUID);
            else
                scheduleMoveSeries(studyIUID, study.getSeries());
        }
        if (!uids.isEmpty()) {
            scheduleMove((String[]) uids.toArray(new String[uids.size()]),
                    null,
                    null);
        }
    }

    private void scheduleMoveSeries(String studyIUID, List series) {
        FolderForm folderForm = (FolderForm) getForm();
        ArrayList uids = new ArrayList();
        for (int i = 0, n = series.size(); i < n; i++) {
            final SeriesModel serie = (SeriesModel) series.get(i);
            final String seriesIUID = serie.getSeriesIUID();
            if (folderForm.isSticky(serie))
                uids.add(seriesIUID);
            else
                scheduleMoveInstances(studyIUID, seriesIUID, serie
                        .getInstances());
        }
        if (!uids.isEmpty()) {
            scheduleMove(new String[] { studyIUID}, (String[]) uids
                    .toArray(new String[uids.size()]), null);
        }
    }

    private void scheduleMoveInstances(String studyIUID, String seriesIUID,
            List instances) {
        FolderForm folderForm = (FolderForm) getForm();
        ArrayList uids = new ArrayList();
        for (int i = 0, n = instances.size(); i < n; i++) {
            final InstanceModel inst = (InstanceModel) instances.get(i);
            if (folderForm.isSticky(inst)) uids.add(inst.getSopIUID());
        }
        if (!uids.isEmpty()) {
            scheduleMove(new String[] { studyIUID},
                    new String[] { seriesIUID},
                    (String[]) uids.toArray(new String[uids.size()]));
        }
    }

    private void scheduleMove(String[] studyIuids, String[] seriesIuids,
            String[] sopIuids) {
        FolderForm folderForm = (FolderForm) getForm();
        MoveOrder order = new MoveOrder(null, folderForm.getDestination(),
                MOVE_PRIOR, null, studyIuids, seriesIuids, sopIuids);
        try {
            log.info("Scheduling " + order);
            JMSDelegate.queue(MoveOrder.QUEUE, order, JMSDelegate
                    .toJMSPriority(MOVE_PRIOR), -1);
        } catch (JMSException e) {
            log.error("Failed: Scheduling " + order, e);
        }
    }

    private String delete() throws Exception {
        FolderForm folderForm = (FolderForm) getForm();
        deletePatients(folderForm.getPatients());
        folderForm.removeStickies();
        return FOLDER;
    }

    private void deletePatients(List patients)
            throws Exception {
        FolderForm folderForm = (FolderForm) getForm();
        for (int i = 0, n = patients.size(); i < n; i++) {
            PatientModel pat = (PatientModel) patients.get(i);
            if (folderForm.isSticky(pat)) {
                List studies = listStudiesOfPatient(pat.getPk());
                delegate.deletePatient(pat.getPk());
                for (int j = 0, m = studies.size(); j < m; j++) {
                    Dataset study = (Dataset) studies.get(j);
                    AuditLoggerDelegate.logStudyDeleted(getCtx(), pat
                            .getPatientID(), pat.getPatientName(), study
                            .getString(Tags.StudyInstanceUID), study
                            .getInt(Tags.NumberOfStudyRelatedInstances, 0),
                            null);
                }
                AuditLoggerDelegate.logPatientRecord(getCtx(), AuditLoggerDelegate.DELETE, pat
                        .getPatientID(), pat.getPatientName(), null);
            } else
                deleteStudies( pat);
        }
    }

    private void deleteStudies( PatientModel pat)
            throws Exception {
        List studies = pat.getStudies();
        FolderForm folderForm = (FolderForm) getForm();
        for (int i = 0, n = studies.size(); i < n; i++) {
            StudyModel study = (StudyModel) studies.get(i);
            if (folderForm.isSticky(study)) {
            	delegate.deleteStudy(study.getPk());
                AuditLoggerDelegate.logStudyDeleted(getCtx(),
                        pat.getPatientID(),
                        pat.getPatientName(),
                        study.getStudyIUID(),
                        study.getNumberOfInstances(),
                        null);
            } else {
                StringBuffer sb = new StringBuffer("Deleted ");
                final int deletedInstances = deleteSeries( study.getSeries(), sb);
                if (deletedInstances > 0) {
                    AuditLoggerDelegate.logStudyDeleted(getCtx(),
                            pat.getPatientID(),
                            pat.getPatientName(),
                            study.getStudyIUID(),
                            deletedInstances,
                            AuditLoggerDelegate.trim(sb));
                }
                    
            }
        }
    }

    private int deleteSeries(List series, StringBuffer sb)
    		throws Exception {
        int numInsts = 0;
        FolderForm folderForm = (FolderForm) getForm();
        for (int i = 0, n = series.size(); i < n; i++) {
            SeriesModel serie = (SeriesModel) series.get(i);
            if (folderForm.isSticky(serie)) {
            	delegate.deleteSeries(serie.getPk());
                numInsts += serie.getNumberOfInstances();
                sb.append("Series[");
                sb.append(serie.getSeriesIUID());
                sb.append("], ");
            } else {
                numInsts += deleteInstances(serie.getInstances(), sb);
            }
        }
        return numInsts;
    }

    private int deleteInstances(List instances,
            StringBuffer sb) throws Exception {
        int numInsts = 0;
        FolderForm folderForm = (FolderForm) getForm();
        for (int i = 0, n = instances.size(); i < n; i++) {
            InstanceModel instance = (InstanceModel) instances.get(i);
            if (folderForm.isSticky(instance)) {
            	delegate.deleteInstance(instance.getPk());
                ++numInsts;
                sb.append("Object[");
                sb.append(instance.getSopIUID());
                sb.append("], ");
            }
        }
        return numInsts;
    }

    private void setSticky(Set stickySet, String attr) {
        stickySet.clear();
        String[] newValue = getCtx().getRequest().getParameterValues(attr);
        if (newValue != null) {
            stickySet.addAll(Arrays.asList(newValue));
        }
    }

    private ContentManager lookupContentManager() throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
                .getFactory().lookup(ContentManagerHome.class,
                        ContentManagerHome.JNDI_NAME);
        return home.create();
    }

    private AEManager lookupAEManager() throws Exception {
        AEManagerHome home = (AEManagerHome) EJBHomeFactory.getFactory()
                .lookup(AEManagerHome.class, AEManagerHome.JNDI_NAME);
        return home.create();
    }

    private List listStudiesOfPatient(int patPk) throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
                .getFactory().lookup(ContentManagerHome.class,
                        ContentManagerHome.JNDI_NAME);
        ContentManager cm = home.create();
        try {
            return cm.listStudiesOfPatient(patPk);
        } finally {
            try {
                cm.remove();
            } catch (Exception e) {
            }
        }
    }
 
    private List listSeriesOfStudy(int studyPk) throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
                .getFactory().lookup(ContentManagerHome.class,
                        ContentManagerHome.JNDI_NAME);
        ContentManager cm = home.create();
        try {
            return cm.listSeriesOfStudy(studyPk);
        } finally {
            try {
                cm.remove();
            } catch (Exception e) {
            }
        }
    }

    private List listInstancesOfSeries(int seriesPk) throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
                .getFactory().lookup(ContentManagerHome.class,
                        ContentManagerHome.JNDI_NAME);
        ContentManager cm = home.create();
        try {
            return cm.listInstancesOfSeries(seriesPk);
        } finally {
            try {
                cm.remove();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Move one ore more model instances to another parent.
     * <p>
     * The move is restricted with following rules:<br>
     * 1) the destinations model type must be the same as the source parents model type.<br>
     * 2) the destination must be different to the source parent.<br>
     * 3) all source models must have the same parent.<br>
     * 4) the destination and the source parent must have the same parent.<br>
     * 
     * @return the name of the next view.
     */
    private String move() {
    	int moveType = checkStickyForMove();
    	String ret = ERROR;
		FolderForm folderForm = (FolderForm) getForm();
    	if ( moveType != MOVE_ERROR) {
    		folderForm.setErrorCode( FolderForm.NO_ERROR );
    		int[] iaSrc;
    		int iDest;
    		ContentManager cm = null;
    		try {
    			ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
		        .getFactory().lookup(ContentManagerHome.class,
		                ContentManagerHome.JNDI_NAME);
		        cm = home.create();
    			
	    		if ( moveType == MOVE_STUDIES ) {
	    			_move_studies( cm );
	    		} else if ( moveType == MOVE_SERIES ) {
	    			_move_series( cm );
	    		} else if ( moveType == MOVE_INSTANCES ) {
	    			_move_instances( cm );
	    		}
	    		ret = FOLDER;
    		} catch ( Exception x ) {
        		folderForm.setErrorCode( FolderForm.ERROR_MOVE );
    			System.err.println("Exception:"+x);
    			x.printStackTrace(System.err);
    			
    			//ret is ERROR
    		}	finally {
    	            try {
    	            	if ( cm != null )
    	            		cm.remove();
    	            } catch (Exception e) {
    	            }
    		}
    	} else {
    		ret = FOLDER;
    	}
		return ret;
    }
    
    
    /**
     * Move selected studies to a patient.
     * 
	 * @param cm ContentManagerBean to update the model.
	 */
	private void _move_studies( ContentManager cm ) throws Exception {
		FolderForm folderForm = (FolderForm) getForm();
		int iDest = Integer.parseInt( (String) folderForm.getStickyPatients().iterator().next().toString() );
		int[] iaSrc = getIntArrayFromSet( folderForm.getStickyStudies() );
		delegate.moveStudies( iaSrc, iDest );
		
		PatientModel destPat = folderForm.getPatientByPk( iDest );
		List path = findModelPath( folderForm.getPatients(), iaSrc[0], 1 );
		PatientModel srcPat = (PatientModel) path.get(0);
		_updatePatientWithStudies( destPat, cm );
		_updatePatientWithStudies( srcPat, cm );
		StudyModel study;
		for ( int i = 0, len=iaSrc.length ; i < len ; i++ ) {
			study = new StudyModel( cm.getStudy( iaSrc[i] ) );
	        AuditLoggerDelegate.logProcedureRecord(getCtx(),
	                AuditLoggerDelegate.MODIFY,
	                destPat.getPatientID(),
	                destPat.getPatientName(),
	                study.getPlacerOrderNumber(),
	                study.getFillerOrderNumber(),
	                study.getStudyIUID(),
	                study.getAccessionNumber(),
	                "study moved from " + srcPat.getPatientName()+ " ("+srcPat.getPatientID()+")" );
		}
	}
	
    /**
     * update given patient with studies.
     * 
	 * @param patient Patient.
	 * @param cm ContentManagerBean to get current list of studies.
	 */
	private void _updatePatientWithStudies( PatientModel patient, ContentManager cm ) throws Exception {
		List studies = cm.listStudiesOfPatient( patient.getPk() );
        for (int i = 0, n = studies.size(); i < n; i++)
            studies.set(i, new StudyModel((Dataset) studies.get(i)));
        patient.setStudies( studies );
	}

	private void _move_series( ContentManager cm ) throws Exception {
		FolderForm folderForm = (FolderForm) getForm();
		int iDest = Integer.parseInt( (String) folderForm.getStickyStudies().iterator().next().toString() );
		int[] iaSrc = getIntArrayFromSet( folderForm.getStickySeries() );
		delegate.moveSeries( iaSrc, iDest );

		List destPath = findModelPath( folderForm.getPatients(), iDest, 1 );

		StudyModel destStudy = (StudyModel) destPath.get(1);
		List sourcePath = findModelPath( folderForm.getPatients(), iaSrc[0], 2 );
		StudyModel srcStudy = (StudyModel) sourcePath.get(1);//0..patient,1..study,2..series
		PatientModel pat = (PatientModel) destPath.get(0);
		//update model for view
		_updateStudyWithSeries( destStudy, cm );
		_updateStudyWithSeries( srcStudy, cm );
		//audit log for source study
		AuditLoggerDelegate.logProcedureRecord(getCtx(),
                AuditLoggerDelegate.MODIFY,
                pat.getPatientID(),
                pat.getPatientName(),
                srcStudy.getPlacerOrderNumber(),
                srcStudy.getFillerOrderNumber(),
                srcStudy.getStudyIUID(),
                srcStudy.getAccessionNumber(),
                iaSrc.length + " series moved to " + destStudy.getStudyDescription()+ " ("+destStudy.getStudyIUID()+")" );
		//audit log for destination study
		AuditLoggerDelegate.logProcedureRecord(getCtx(),
                AuditLoggerDelegate.MODIFY,
                pat.getPatientID(),
                pat.getPatientName(),
                destStudy.getPlacerOrderNumber(),
                destStudy.getFillerOrderNumber(),
                destStudy.getStudyIUID(),
                destStudy.getAccessionNumber(),
                iaSrc.length + " series moved from " + srcStudy.getStudyDescription()+ " ("+srcStudy.getStudyIUID()+")" );

	}
	
	private void _updateStudyWithSeries(StudyModel study, ContentManager cm) throws Exception {
		List series = cm.listSeriesOfStudy( study.getPk() );
        for (int i = 0, n = series.size(); i < n; i++)
        	series.set(i, new SeriesModel((Dataset) series.get(i)));
        study.setSeries( series );
        study.update( cm.getStudy( study.getPk() ) );
	}

	private void _move_instances( ContentManager cm ) throws Exception {
		FolderForm folderForm = (FolderForm) getForm();
		int iDest = Integer.parseInt( (String) folderForm.getStickySeries().iterator().next().toString() );
		int[] iaSrc = getIntArrayFromSet( folderForm.getStickyInstances() );
		delegate.moveInstances( iaSrc, iDest );

		List destPath = findModelPath( folderForm.getPatients(), iDest, 2 );

		SeriesModel destModel = (SeriesModel) destPath.get(2);//0..patient,1..study,2..series
		List sourcePath = findModelPath( folderForm.getPatients(), iaSrc[0], 3 );
		_updateSeriesWithInstances( destModel, cm );
		_updateSeriesWithInstances( (SeriesModel) sourcePath.get(2), cm );//0..patient,1..study,2..series,3..instances

		StudyModel srcStudy = (StudyModel) sourcePath.get(1);//0..patient,1..study,2..series
		PatientModel pat = (PatientModel) destPath.get(0);

		AuditLoggerDelegate.logProcedureRecord(getCtx(),
                AuditLoggerDelegate.MODIFY,
                pat.getPatientID(),
                pat.getPatientName(),
                srcStudy.getPlacerOrderNumber(),
                srcStudy.getFillerOrderNumber(),
                srcStudy.getStudyIUID(),
                srcStudy.getAccessionNumber(),
                iaSrc.length + " instances moved to " + destModel.getSeriesDescription()+ " ("+destModel.getSeriesIUID()+")" );
		
	}
	
	private void _updateSeriesWithInstances(SeriesModel series, ContentManager cm) throws Exception {
		List inst = cm.listInstancesOfSeries( series.getPk() );
        for (int i = 0, n = inst.size(); i < n; i++)
        	inst.set(i, InstanceModel.valueOf((Dataset) inst.get(i)));
        series.setInstances( inst );
        series.update( cm.getSeries( series.getPk() ) );
	}
	
	/**
     * Converts a Set of int values as String to an int array.
     * 
	 * @param set Set with String objects of int
	 * @return int Array
	 */
	public static int[] getIntArrayFromSet(Set set) {
		if ( set == null ) return null;
		int[] ia = new int[ set.size() ];
		int i = 0;
		Iterator iter = set.iterator();
		while ( iter.hasNext() ) {
			ia[i] = Integer.parseInt( (String) iter.next() );
			i++;
		}
		return ia;
	}

	/**
	 * Checks the current sticky settings for move request.
	 * <p>
	 * Checks if the Sticky Informations are correct and obtain the type of move.
	 * 
	 * @return Type of move ( MOVE_STUDIES, MOVE_SERIES or MOVE_INSTANCES) or MOVE_ERROR if move is not allowed.
	 */
	private int checkStickyForMove() {
    	FolderForm folderForm = (FolderForm) getForm();
       	int iStickyPat = folderForm.getStickyPatients().size();
       	int iStickyStu = folderForm.getStickyStudies().size();
       	int iStickySer = folderForm.getStickySeries().size();
       	int iStickyInst = folderForm.getStickyInstances().size();
       	if ( iStickyPat > 0 ) {
       		if ( !checkSticky( iStickyPat, iStickyStu, iStickySer, iStickyInst, MOVE_STUDIES ) ){
       			return MOVE_ERROR;
       		}
        	if ( checkStickyPlacement( folderForm.getStickyPatients(), folderForm.getStickyStudies(), 0, MOVE_STUDIES ) ) {
        		return MOVE_STUDIES;
       		}
       	} else if ( iStickyStu > 0 ) {
       		if ( !checkSticky( iStickyStu, iStickySer, iStickyInst, 0, MOVE_SERIES ) ) {
       			return MOVE_ERROR;
       		}
        	if ( this.checkStickyPlacement( folderForm.getStickyStudies(), folderForm.getStickySeries(), 1, MOVE_SERIES ) ) {
        		return MOVE_SERIES;	
        	}
       	} else if ( iStickySer > 0 ) {
       		if ( !checkSticky( iStickySer, iStickyInst, 0, 0, MOVE_INSTANCES ) ) {
       			return MOVE_ERROR;
       		}
        	if ( checkStickyPlacement( folderForm.getStickySeries(), folderForm.getStickyInstances(), 2, MOVE_INSTANCES ) ) {
        		return MOVE_INSTANCES;
        	}
       	} else {
       		folderForm.setErrorCode( FolderForm.ERROR_MOVE_NO_SELECTION );
       	}
   		return MOVE_ERROR; //nothing selected
    }
	
	/**
	 * Checks the numbers of selected stickies.
	 * <p>
	 * This method checks if only one destination, one ore more sources and no other stickies are set.
	 * <p>
	 * If one of the following rules are not fullfilled, a corresponding error code is set in FolderForms.
	 * <DL>
	 * <DT>Rule:</DT>
	 * <DD>  iDest must be 1</DD>
	 * <DD>  iSource must be greater than 0</DD>
	 * <DD>  iOther1 and iOther2 must be 0</DD>
	 * </DL>
	 * 
	 * @param iDest		Number of selected destinations
	 * @param iSource	Number of selected sources
	 * @param iOther1	Number of selected 
	 * @param iOther2
	 * 
	 * @return true if check is OK, false if otherwise.
	 */
	private boolean checkSticky( int iDest, int iSource, int iOther1, int iOther2, int moveMode ) {
    	FolderForm folderForm = (FolderForm) getForm();
		if ( iDest > 1) {
			folderForm.setErrorCode( FolderForm.ERROR_MOVE_TO_MANY_DEST );
			return false;
		} else if ( iSource < 1 ) {
			folderForm.setErrorCode( FolderForm.ERROR_MOVE_NO_SOURCE );
			return false;
		} else if ( iOther1 > 0 || iOther2 > 0 ) {
			switch ( moveMode ) {
				case MOVE_STUDIES:
					folderForm.setErrorCode( FolderForm.ERROR_MOVE_UNSELECT_SERIES );//Wrong: series and/or instances are selected!
					break;
				case MOVE_SERIES:
					folderForm.setErrorCode( FolderForm.ERROR_MOVE_UNSELECT_INSTANCES );//Wrong: instances are selected!
			}
			return false;
		}
		return true;
	}
    
	/**
	 * Returns the tree nodes for a given pk.
	 * <p>
	 * Because the pk's are only unique within a model type, it is necessary to use the correct
	 * depth value!
	 * <p>
	 * 
	 * @param parent 	This is the root model of the tree to search.
	 * @param pk		The pk as String to search
	 * @param depth		The tree depth where the pk should be found.
	 * @return A list with all nodes to get the model.
	 */
    private List findModelPath( List parent, String pk, int depth ) {
       	int iPk = Integer.parseInt(pk);
   	    return findModelPath( parent, iPk, depth );
    }
 
	/**
	 * Returns the tree nodes for a given pk.
	 * <p>
	 * Because the pk's are only unique within a model type, it is necessary to use the correct
	 * depth value!
	 * <p>
	 * 
	 * @param parent 	This is the root model of the tree to search.
	 * @param pk		The pk as int to search
	 * @param depth		The tree depth where the pk should be found.
	 * @return A list with all nodes to get the model.
	 */
    private List findModelPath( List parent, int iPk, int depth ) {
    	Iterator iter = parent.iterator();
    	AbstractModel model = null;
    	List l = new ArrayList();
    	while ( iter.hasNext() ) {
    		model = (AbstractModel) iter.next();
    		if ( depth == 0 ) {//should pk in this parent?
	    		if ( model.getPk() == iPk ) {
		    			l.add( model );
		    			return l;
	    		}
    		} else { //search in next tree segment.
 				List l1 = findModelPath( model.listOfChilds(), iPk, depth-1 );
				if ( l1 != null ) {
					l.add( model );
					l.addAll( l1 );
					return l;
				}
     		}
    	}
    	return null;
    }
    
    /**
     * Checks if the sticky informations are at correct positions.
     * <p>
     * This method checks following restrictions:<br>
     * 1) the destination must be different to the source parent.<br>
     * 2) all source models must have the same parent.<br>
     * 3) the destination and the source parent must have the same parent.<br>
     * <p>
     * If one of the checks failed a corresponding error code is set in FolderForms.
     * 
     * @param stickyParent Sticky information of the parent (the destination).
     * @param stickyChilds Sticky information of the childs (the source).
     * @param depth Tree depth (where is the destination) (use 0 for Patient,1 for Study,2 for Series)
     * @return
     */
    private boolean checkStickyPlacement( Set stickyParent, Set stickyChilds, int depth, int moveMode ) {
		FolderForm folderForm = (FolderForm) getForm();
  		List listParent, listChilds;
		listParent = findModelPath( folderForm.getPatients(), (String)stickyParent.iterator().next(), depth );
		listChilds = findModelPath( folderForm.getPatients(), (String)stickyChilds.iterator().next(), depth+1 );
		int listParentSize = listParent.size();
		if ( listParent.get(listParentSize-1).equals( listChilds.get(listChilds.size()-2) ) ) {
			if ( moveMode == MOVE_STUDIES)
				folderForm.setErrorCode( FolderForm.ERROR_MOVE_SAME_PATIENT );
			else if ( moveMode == MOVE_SERIES)
				folderForm.setErrorCode( FolderForm.ERROR_MOVE_SAME_STUDY );
			else if ( moveMode == MOVE_INSTANCES)
				folderForm.setErrorCode( FolderForm.ERROR_MOVE_SAME_SERIES );
			return false; //same parent;
		}
		listParentSize -= 1;
		for ( int i = 0 ; i < listParentSize ; i++ ) {
			if ( !listParent.get(i).equals( listChilds.get(i) ) ) {
				if ( moveMode == MOVE_SERIES)
					folderForm.setErrorCode( FolderForm.ERROR_MOVE_DIFF_PATIENT );
				else if ( moveMode == MOVE_INSTANCES)
					folderForm.setErrorCode( FolderForm.ERROR_MOVE_DIFF_STUDY );
				return false; //different parent(s) from parent;
			}
			
		}
		AbstractModel model = (AbstractModel) listChilds.get(listChilds.size()-2);//parent model of first child
		Iterator iter = stickyChilds.iterator();
		iter.next(); //skip first element (is used for listChilds)
		while ( iter.hasNext() ) {
			if ( !model.containsPK( Integer.parseInt( iter.next().toString() ) ) ) {
				if ( moveMode == MOVE_STUDIES)
					folderForm.setErrorCode( FolderForm.ERROR_MOVE_DIFF_STUDY_PARENT );
				else if ( moveMode == MOVE_SERIES)
					folderForm.setErrorCode( FolderForm.ERROR_MOVE_DIFF_SERIES_PARENT );
				else if ( moveMode == MOVE_INSTANCES)
					folderForm.setErrorCode( FolderForm.ERROR_MOVE_DIFF_INSTANCE_PARENT );
				return false; //this child has not the same parent as first child element
			}
		}
 		return true;
    }

}