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

package org.dcm4chex.archive.web.maverick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;
import javax.servlet.http.HttpServletRequest;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.dcm.movescu.MoveOrder;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.JMSDelegate;
import org.dcm4chex.archive.web.maverick.admin.DCMUser;
import org.dcm4chex.archive.web.maverick.ae.AEDelegate;
import org.dcm4chex.archive.web.maverick.model.InstanceModel;
import org.dcm4chex.archive.web.maverick.model.PatientModel;
import org.dcm4chex.archive.web.maverick.model.SeriesModel;
import org.dcm4chex.archive.web.maverick.model.StudyFilterModel;
import org.dcm4chex.archive.web.maverick.model.StudyModel;
import org.dcm4chex.archive.web.maverick.tf.TFModel;
import org.dcm4chex.archive.web.maverick.tf.TeachingFileDelegate;
import org.dcm4chex.archive.web.maverick.xdsi.XDSIExportDelegate;
import org.dcm4chex.archive.web.maverick.xdsi.XDSIModel;
import org.infohazard.maverick.flow.ControllerContext;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2727 $ $Date: 2006-09-07 17:16:12 +0800 (周四, 07 9月 2006) $
 * @since 28.01.2004
 */
public class FolderSubmitCtrl extends FolderCtrl {
    
	public static final String LOGOUT = "logout";
	private static final String EXPORT_SELECTOR = "exportSelector";
	private static final String XDSI_EXPORT = "xdsi_export";
	
    private static final int MOVE_PRIOR = 0;
    
    private static ContentEditDelegate delegate = null;
	private static AEDelegate aeDelegate = null;
    
    private FolderMoveDelegate moveDelegate = null;
	private TeachingFileDelegate tfDelegate;
	private XDSIExportDelegate xdsiDelegate;



    public static ContentEditDelegate getDelegate() {
    	return delegate;
    }
    public static ContentEditDelegate getDelegate(ControllerContext ctx) {
        if ( delegate == null ) {
        	delegate = new ContentEditDelegate();
        	try {
        		delegate.init( ctx );
        	} catch( Exception x ) {
        		log.error("Cant make form bean!", x );
        	}
        	StudyModel.setHttpRoot(ctx.getServletContext().getRealPath("/"));//set http root to check if a studyStatus image is available.
        }
    	return delegate;
    }
    
	/**
	 * Get the model for the view.
	 * @throws 
	 */
    protected Object makeFormBean() {
		getDelegate( getCtx() );
		return super.makeFormBean();
    }
    
    protected String perform() throws Exception {
        try {
            FolderForm folderForm = (FolderForm) getForm();
    		folderForm.setErrorCode( FolderForm.NO_ERROR );//reset error code
    		folderForm.setPopupMsg(null);
            HttpServletRequest rq = getCtx().getRequest();
            if ( rq.getParameter("accNr") != null ) {
            	log.warn("Somebody tried AutoLogin for web folder! Denied!");
            	return logout();
            }
            setSticky(folderForm.getStickyPatients(), "stickyPat");
            setSticky(folderForm.getStickyStudies(), "stickyStudy");
            setSticky(folderForm.getStickySeries(), "stickySeries");
            setSticky(folderForm.getStickyInstances(), "stickyInst");
            if ( rq.getParameter("showWithoutStudies") != null )
                folderForm.setShowWithoutStudies( "true".equals( rq.getParameter("showWithoutStudies")));
            folderForm.setFilterAET( "true".equals( rq.getParameter("filterAET")));
            if (rq.getParameter("logout") != null || rq.getParameter("logout.x") != null ) 
            	return logout();
            
            if ( log.isDebugEnabled() ) {
		        log.debug( "UserPrincipal:"+rq.getUserPrincipal().getName() );
		        log.debug( "UserPrincipal is in role "+DCMUser.WEBADMIN+":"+rq.isUserInRole(DCMUser.WEBADMIN) );
		        log.debug( "UserPrincipal is in role "+DCMUser.MCMUSER+":"+rq.isUserInRole(DCMUser.MCMUSER) );
		        log.debug( "UserPrincipal is in role "+DCMUser.DATACARE_USER+":"+rq.isUserInRole(DCMUser.DATACARE_USER) );
		        log.debug( "UserPrincipal is in role "+DCMUser.JBOSSADMIN+":"+rq.isUserInRole(DCMUser.JBOSSADMIN) );
		        log.debug( "UserPrincipal is in role "+DCMUser.ARRUSER+":"+rq.isUserInRole(DCMUser.ARRUSER) );
            }
            if (rq.getParameter("sessionChanged") != null ) {
            	folderForm.setPopupMsg("Session changed! Reloaded view with empty filter!");
            	rq.getSession().setAttribute("dcm4chee-session", "RELOADED");
            	return query(true); 
            }
        	rq.getSession().setAttribute("dcm4chee-session", "ACTIVE");
            if (folderForm.getTotal() < 1 || rq.getParameter("filter") != null
                    || rq.getParameter("filter.x") != null) { return query(true); }
            if (rq.getParameter("prev") != null
                    || rq.getParameter("prev.x") != null
                    || rq.getParameter("next") != null
                    || rq.getParameter("next.x") != null) { return query(false); }
            if (rq.getParameter("send") != null
                    || rq.getParameter("send.x") != null) { return send(); }
            Set allowedMethods = getPermissions().getPermissionsForApp("folder");
            if ( allowedMethods.contains("folder.delete") ) {
	            if (rq.getParameter("del") != null
	                    || rq.getParameter("del.x") != null) { return delete(); }
            }
            if ( allowedMethods.contains("folder.edit") ) {
	            if (rq.getParameter("merge") != null
	                    || rq.getParameter("merge.x") != null) { return merge(); }
            }
            if ( allowedMethods.contains("folder.move") ) {
            	if (rq.getParameter("move") != null
	                    || rq.getParameter("move.x") != null) { return move(); }
            }
            if ( allowedMethods.contains("folder.export_tf") ) {
	            if (rq.getParameter("exportTF") != null
	                    || rq.getParameter("exportTF.x") != null) { return exportTF(); }
            }
            if ( allowedMethods.contains("folder.export_xds") ) {
	            if (rq.getParameter("exportXDSI") != null
	                    || rq.getParameter("exportXDSI.x") != null) { return exportXDSI(); }
            }
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
        	String[] allowedAets = getAEFilterPermissions();
            try {
            	filter = folderForm.getStudyFilter();
            	if ( ! folderForm.isFilterAET() ) { //only if not filter a single AET!
            		filter.setCallingAETs( allowedAets );
            	}
            } catch ( NumberFormatException x ) {
            	folderForm.setErrorCode( ERROR_PARSE_DATE );
            	return FOLDER;
            }
            if (newQuery) {
                folderForm.setTotal(cm.countStudies(filter.toDataset(), !folderForm.isShowWithoutStudies()));
                List aes;
                if ( allowedAets == null ) {
                	aes = getAEDelegate().getAEs();
                } else {
                	Object ae;
                	aes = new ArrayList();
                	for ( int i = 0 ; i < allowedAets.length ; i++ ) {
                		ae = getAEDelegate().getAE(allowedAets[i]);
                		if ( ae != null ) 
                			aes.add( getAEDelegate().getAE(allowedAets[i]));
                	}
                }
                folderForm.setAets(aes);
            }
            List studyList = cm.listStudies(filter.toDataset(), !folderForm.isShowWithoutStudies(), 
					folderForm.isNoMatchForNoValue(), folderForm.getOffset(), folderForm.getLimit());
            folderForm.setStudies(studyList);
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
        clearSticky();
        return FOLDER;
    }

    private void scheduleMoveStudiesOfPatient(long pk) throws Exception {
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
        query(true);
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
            	delegate.movePatientToTrash(pat.getPk());
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
                deleteStudies( pat );
        }
    }

    private void deleteStudies( PatientModel pat )
            throws Exception {
        List studies = pat.getStudies();
        FolderForm folderForm = (FolderForm) getForm();
        for (int i = 0, n = studies.size(); i < n; i++) {
            StudyModel study = (StudyModel) studies.get(i);
            if (folderForm.isSticky(study)) {
            	delegate.moveStudyToTrash(study.getPk());
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
               	delegate.moveSeriesToTrash(serie.getPk());
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

    private int deleteInstances(List instances, StringBuffer sb) throws Exception {
        int numInsts = 0;
        FolderForm folderForm = (FolderForm) getForm();
        for (int i = 0, n = instances.size(); i < n; i++) {
            InstanceModel instance = (InstanceModel) instances.get(i);
            if (folderForm.isSticky(instance)) {
               	delegate.moveInstanceToTrash(instance.getPk());
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

    private List listStudiesOfPatient(long patPk) throws Exception {
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
 

    private String merge() {
    	return MERGE;
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
    	try {
    		if ( moveDelegate == null ) moveDelegate = new FolderMoveDelegate( this );
    		moveDelegate.move(); 
    	} catch ( Exception x ) {
    		log.error("Error in folder move:", x);
    	}
		return FOLDER;
    }
    
    /**
     * Export selected instance to a Teaching Filesystem.
     * <p>
     * 
     * @return the name of the next view.
     */
    private String exportTF() {
		FolderForm folderForm = (FolderForm) getForm();
    	try {
    		if ( tfDelegate == null ) {
    			tfDelegate = new TeachingFileDelegate();
    			tfDelegate.init(getCtx());
    			getCtx().getRequest().getSession().setAttribute(TeachingFileDelegate.TF_ATTRNAME, tfDelegate);
    		}
        	Set instances = FolderUtil.getSelectedInstances(folderForm.getStickyPatients(),
					 folderForm.getStickyStudies(),
					 folderForm.getStickySeries(),
					 folderForm.getStickyInstances());
        	if ( log.isDebugEnabled() ) log.debug("Selected Instances:"+instances);

    		TFModel.getModel( getCtx().getRequest() ).setInstances(instances); 
    	} catch ( Exception x ) {
    		log.error("Error in export Teaching File:", x);
    		folderForm.setPopupMsg("Error:"+x.getMessage());
    		return FOLDER;
    	}
		return EXPORT_SELECTOR;
    }
   
    /**
     * Export selected instances to a XDS Repository.
     * <p>
     * 
     * @return the name of the next view.
     */
    private String exportXDSI() {
		FolderForm folderForm = (FolderForm) getForm();
    	try {
    		if ( xdsiDelegate == null ) {
    			xdsiDelegate = new XDSIExportDelegate();
    			xdsiDelegate.init(getCtx());
    			getCtx().getRequest().getSession().setAttribute(XDSIExportDelegate.XDSI_ATTRNAME, xdsiDelegate);
    		}
        	Set instances = FolderUtil.getSelectedInstances(folderForm.getStickyPatients(),
					 folderForm.getStickyStudies(),
					 folderForm.getStickySeries(),
					 folderForm.getStickyInstances());
        	if ( log.isDebugEnabled() ) log.debug("Selected Instances:"+instances);

    		XDSIModel.getModel( getCtx().getRequest() ).setInstances(instances); 
    	} catch ( Exception x ) {
    		log.error("Error in XDSI export! :", x);
    		folderForm.setPopupMsg("Error:"+x.getMessage());
    		return FOLDER;
    	}
		return XDSI_EXPORT;
    }
    
    /**
	 * 
	 */
	public void clearSticky() {
		FolderForm folderForm = (FolderForm) getForm();
       	folderForm.getStickyPatients().clear();		
       	folderForm.getStickyStudies().clear();		
       	folderForm.getStickySeries().clear();		
       	folderForm.getStickyInstances().clear();		
	}

	
	protected void logProcedureRecord( PatientModel pat, StudyModel study, String desc ) {
		AuditLoggerDelegate.logProcedureRecord(getCtx(),
                AuditLoggerDelegate.MODIFY,
                pat.getPatientID(),
                pat.getPatientName(),
                study.getPlacerOrderNumber(),
                study.getFillerOrderNumber(),
                study.getStudyIUID(),
                study.getAccessionNumber(),
                desc );
	}
	
	private String logout() {
    	getCtx().getRequest().getSession().invalidate();
    	return LOGOUT;
	}

    public AEDelegate getAEDelegate() {
        if ( aeDelegate == null ) {
        	aeDelegate = new AEDelegate();
        	aeDelegate.init( getCtx().getServletConfig() );
        }
        return aeDelegate;
    }
	
}