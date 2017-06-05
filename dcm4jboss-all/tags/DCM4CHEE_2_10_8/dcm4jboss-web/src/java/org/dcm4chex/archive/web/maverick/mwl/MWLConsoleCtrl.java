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

package org.dcm4chex.archive.web.maverick.mwl;

import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.archive.web.maverick.Dcm4cheeFormController;
import org.dcm4chex.archive.web.maverick.mpps.model.MPPSModel;
import org.dcm4chex.archive.web.maverick.mwl.model.MWLFilter;
import org.dcm4chex.archive.web.maverick.mwl.model.MWLModel;

/**
 * @author franz.willer
 *
 * The Maverick controller for Media Creation Manager.
 */
public class MWLConsoleCtrl extends Dcm4cheeFormController {


	public static final String ERROR_MWLENTRY_DELETE = "deleteError_mwlEntry";
	/** the view model. */
	private MWLModel model;
	
	private static MWLScuDelegate delegate = null;

	/**
	 * Get the model for the view.
	 */
    protected Object makeFormBean() {
        if ( delegate == null ) {
        	delegate = new MWLScuDelegate();
        	delegate.init( getCtx().getServletConfig() );
        }
        model =  MWLModel.getModel(getCtx().getRequest());
        return model;
    }
	

	
    protected String perform() throws Exception {
        try {
            HttpServletRequest request = getCtx().getRequest();
    		model = MWLModel.getModel(request);
    		model.setErrorCode( MWLModel.NO_ERROR );
    		model.setPopupMsg(null);
    		model.setPatMergeAttributes(null);
            if ( request.getParameter("filter.x") != null ) {//action from filter button
            	try {
	        		checkFilter( request );
	            	model.filterWorkList( true );
            	} catch ( ParseException x ) {
            		model.setErrorCode( ERROR_PARSE_DATETIME );
            	}
            } else if ( request.getParameter("nav") != null ) {//action from a nav button. (next or previous)
            	String nav = request.getParameter("nav");
            	if ( nav.equals("prev") ) {
            		model.performPrevious();
            	} else if ( nav.equals("next") ) {
            		model.performNext();
            	}
            } else if ( request.getParameter("link.x") != null ) {//action from a global link button.
            	return performAction( "link", request );
            } else if ( request.getParameter("doLink.x") != null ) {
            	return performAction( "doLink", request );
            } else if ( request.getParameter("del.x") != null ) {//action from delete button.
        		String[] spsIDs = getSPSIds(request);
        		if ( spsIDs == null || spsIDs.length < 1) {
        			model.setPopupMsg("No Worklist Entry selected!");
        		} else {
        			for ( int i = 0 ; i < spsIDs.length ; i++ ) {
        				delegate.deleteMWLEntry( spsIDs[i] );
        			}
        			model.filterWorkList( false );
        		}
            } else {
            	String action = request.getParameter("action");
            	if ( action != null ) {
            		return performAction( action, request );
            	}
            }
            return SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

	/**
	 * @param action
	 * @param request
	 * @throws ParseException
	 */
	private String performAction(String action, HttpServletRequest request) throws ParseException {
		if ( "delete".equalsIgnoreCase( action ) ) {
			if ( delegate.deleteMWLEntry( request.getParameter("spsid") ) ) {
				model.filterWorkList( false );
			} else {
				model.setErrorCode( ERROR_MWLENTRY_DELETE );
			}
		} else if ("link".equals(action)) {
			MWLFilter filter = model.getFilter();
			if ( request.getParameter("mppsIUID") != null ) {
				model.setMppsIDs( request.getParameterValues("mppsIUID")); // direct url call
				filter.setPatientName(request.getParameter("patientName"));
			} else {
				MPPSModel mppsModel = MPPSModel.getModel(request);
				model.setMppsIDs( mppsModel.getMppsIUIDs() );//redirect from mpps controller
				filter.setPatientName(mppsModel.getPatientOfSelectedMpps());
			}
			filter.setAccessionNumber(null);
			filter.setEndDate(request.getParameter("endDate"));
			filter.setModality(request.getParameter("modality"));
			filter.setStartDate(request.getParameter("startDate"));
			filter.setStationAET(request.getParameter("stationAET"));
			model.filterWorkList( true );
		} else if ("doLink".equals(action)) {
			return doLink(request);
		} else if ("cancelLink".equals(action)) {
			model.setMppsIDs(null);
			return "cancelLink";
		}
		return SUCCESS;
	}



	/**
	 * @param request
	 * @return
	 */
	private String doLink(HttpServletRequest request) {
		String[] mppsIUIDs = model.getMppsIDs();
		model.setMppsIDs(null);
		String[] spsIDs = getSPSIds(request);
		if ( spsIDs == null || spsIDs.length < 1) {
			model.setPopupMsg("No Worklist Entry selected!");
			return SUCCESS;
		} else if ( spsIDs.length > 1 ) {
			String patID = (String) model.getMWLEntry(spsIDs[0]).getPatientID();
			for ( int i = 1 ; i < spsIDs.length ; i++ ) {
				if ( ! patID.equals(model.getMWLEntry(spsIDs[i]).getPatientID())) {
					model.setPopupMsg("All selected Worklist Entries must be from the same patient!");
					return SUCCESS;
				}
			}
		}
		if ( mppsIUIDs != null ) {
			Map map = delegate.linkMppsToMwl( spsIDs, mppsIUIDs );
			if ( map == null ) {
				model.setPopupMsg("Link selected MPPS to selected MWL failed!");
			} else if ( map.get("dominant") != null ) {
				model.setPatMergeAttributes(map);
				return "mergePatient";
			}
		}
		return "linkDone";
	}



	/**
	 * @param request
	 * @return
	 */
	private String[] getSPSIds(HttpServletRequest request) {
		String[] result;
		if ( request.getParameter("spsID") != null ) {
			result = new String[]{request.getParameter("spsID")};
		} else {
			result = request.getParameterValues("sticky");
		}
		return result;
	}



	/**
	 * Checks the http parameters for filter params and update the filter.
	 * 
	 * @param rq The http request.
	 * 
	 * @throws ParseException
	 * 
	 */
	private void checkFilter(HttpServletRequest rq) throws ParseException {
		MWLFilter filter = model.getFilter();
		if ( rq.getParameter("patientName") != null ) filter.setPatientName(rq.getParameter("patientName") );
		if ( rq.getParameter("startDate") != null ) filter.setStartDate(rq.getParameter("startDate") );
		if ( rq.getParameter("endDate") != null ) filter.setEndDate(rq.getParameter("endDate") );
		if ( rq.getParameter("modality") != null ) filter.setModality(rq.getParameter("modality") );
		if ( rq.getParameter("stationAET") != null ) filter.setStationAET(rq.getParameter("stationAET") );
		if ( rq.getParameter("accessionNumber") != null ) filter.setAccessionNumber(rq.getParameter("accessionNumber") );
	}

	/**
	 * Returns the delegater that is used to query the MWLSCP or delete an MWL Entry (only if MWLSCP AET is local)
	 * 
	 * @return The delegator.
	 */
	public static MWLScuDelegate getMwlScuDelegate() {
		return delegate;
	}
	
	protected String getCtrlName() {
		return "mwl_console";
	}
	
}
