/*
 * Created on 20.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mcmc;

import java.text.ParseException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.util.JMSDelegate;
import org.dcm4chex.archive.web.maverick.Dcm4JbossFormController;
import org.dcm4chex.archive.web.maverick.mcmc.model.MCMFilter;
import org.dcm4chex.archive.web.maverick.mcmc.model.MCMModel;
import org.dcm4chex.archive.web.maverick.mcmc.model.MediaData;

/**
 * @author franz.willer
 *
 * The Maverick controller for Media Creation Manager.
 */
public class MCMConsoleCtrl extends Dcm4JbossFormController {

	/** The name of the JMS Queue used to queue a media for creation. */
	public static final String QUEUE = "MCMScu";

	/** the view model. */
	private MCMModel model;
	
	private static MCMScuDelegate delegate = null;

	/**
	 * Get the model for the view.
	 */
    protected Object makeFormBean() {
        if ( delegate == null ) {
        	delegate = new MCMScuDelegate();
        	delegate.init( getCtx().getServletConfig() );
        }
        return MCMModel.getModel(getCtx().getRequest());
    }
	

	
    protected String perform() throws Exception {
        try {
            HttpServletRequest request = getCtx().getRequest();
    		model = MCMModel.getModel(request);
    		model.setErrorCode( MCMModel.NO_ERROR );
            if ( request.getParameter("checkMCM") != null ) {
    			model.setMcmNotAvail( ! delegate.checkMcmScpAvail() );
    			model.setCheckAvail( true );
            } else {
            	model.setCheckAvail( false );
            }
            if ( request.getParameter("filter.x") != null ) {//action from filter button
        		checkFilter( request );
            	model.filterMediaList( true );
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
	 * Performs an action request.
	 * A action request is indicated by an 'action' http parameter.
	 * <p>
	 * Set the MCMModel.ERROR_UNSUPPORTED_ACTION error code in model if <code>action</code> is not defined.
	 *  
	 * @param action	The value of action request patrameter.
	 * @param request	The http request.
	 */
	private void performAction(String action, HttpServletRequest request) {
		if ( action.equalsIgnoreCase("queue") ) {
			model.setMcmNotAvail( ! delegate.checkMcmScpAvail() );
			if ( model.isMcmNotAvail() ) return;
			int mediaPk = Integer.parseInt( request.getParameter("mediaPk"));
           	MediaData md = model.mediaDataFromList( mediaPk );
           	if ( md != null ) {
   			   model.updateMediaStatus( mediaPk, MediaDTO.QUEUED, "" );
	           try {
	 				JMSDelegate.queue( QUEUE,
					        md.asMediaDTO(),
					        Message.DEFAULT_PRIORITY,
					        0L);
				} catch (JMSException e) {
					
					model.updateMediaStatus( mediaPk, MediaDTO.ERROR, e.getMessage() );
				}
           	}
		} else if ( action.equalsIgnoreCase("delete") ) {
			System.out.println("delete media:"+request.getParameter("mediaPk"));
			if ( ! delegate.deleteMedia( Integer.parseInt( request.getParameter("mediaPk"))) ) {
				model.setErrorCode( MCMModel.ERROR_MEDIA_DELETE );
			}
			model.filterMediaList( true );
		} else {
			model.setErrorCode( MCMModel.ERROR_UNSUPPORTED_ACTION );
		}
		
	}



	/**
	 * Checks the http parameters for filter params and update the filter.
	 * 
	 * @param rq The http request.
	 * 
	 * @return true if filter has been changed.
	 * 
	 * @throws ParseException
	 * 
	 */
	private boolean checkFilter(HttpServletRequest rq) throws ParseException {
		MCMFilter filter = model.getFilter();
		filter.setSelectedStati(rq.getParameterValues("mediaStatus") );
		if ( rq.getParameter("startDate") != null ) filter.setStartDate(rq.getParameter("startDate") );
		if ( rq.getParameter("endDate") != null ) filter.setEndDate(rq.getParameter("endDate") );
		if ( rq.getParameter("createOrUpdateDate") != null ) filter.setCreateOrUpdateDate(rq.getParameter("createOrUpdateDate") );
		return filter.isChanged();
	}
	
	public static MCMScuDelegate getMcmScuDelegate() {
		return delegate;
	}
	
}
