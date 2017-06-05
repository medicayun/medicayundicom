/*
 * Created on 20.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mwl;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.archive.web.maverick.Dcm4JbossFormController;
import org.dcm4chex.archive.web.maverick.mwl.model.MWLFilter;
import org.dcm4chex.archive.web.maverick.mwl.model.MWLModel;

/**
 * @author franz.willer
 *
 * The Maverick controller for Media Creation Manager.
 */
public class MWLConsoleCtrl extends Dcm4JbossFormController {


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
            } else {
            	String action = request.getParameter("action");
            	if ( action != null ) {
            		performAction( action, request );
            	}
            }
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

	/**
	 * @param action
	 * @param request
	 */
	private void performAction(String action, HttpServletRequest request) {
		if ( "delete".equalsIgnoreCase( action ) ) {
			if ( delegate.deleteMWLEntry( request.getParameter("spsid") ) ) {
				model.filterWorkList( false );
			} else {
				model.setErrorCode( ERROR_MWLENTRY_DELETE );
			}
		}
		
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
	
}
