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

package org.dcm4chex.archive.web.maverick.mcmc.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dcm4chex.archive.ejb.interfaces.MediaComposer;
import org.dcm4chex.archive.ejb.interfaces.MediaComposerHome;
import org.dcm4chex.archive.ejb.interfaces.MediaDTO;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.mcmc.MCMConsoleCtrl;

/**
 * @author franz.willer
 *
 * The Model for Media Creation Managment WEB interface.
 */
public class MCMModel {

	/** The session attribute name to store the model in http session. */
	public static final String MCMMODEL_ATTR_NAME = "mcmModel";
	/** 
	 * Status for action 'queue' 
	 * <p>
	 * This value is used in xsl to create queue action button for a media with this status.
	 */
	public static final String STATI_FOR_QUEUE = String.valueOf( MediaDTO.OPEN );
	
	/** Errorcode: no error */
    public static final String NO_ERROR ="OK";
    /** Errorcode: unsupported action */
	public static final String ERROR_UNSUPPORTED_ACTION = "UNSUPPORTED_ACTION";
	public static String ERROR_MEDIA_DELETE = "MEDIA_DELETE_FAILED";

	/** holds current error code. */
	private String errorCode = NO_ERROR;
    /** Popup message */
    private String popupMsg = null;

	/** Holds the current offset for paging */
	private int offset = 0;
	/** Holds the limit for paging */
	private int limit = 10;
	/** Holds the total number of results of last search. */
	private int total = 0;
	/** Holds the current list of media for the view. */
	private MediaList mediaList;
	/** Holds the filter for media search. */
	private MCMFilter filter;
	
	/** Holds availability status of MCM SCP */
	private boolean mcmNotAvail = false;

	/**
	 * Holds the 'checked' flag for 'checkMCM' checkbox.
	 * <p>
	 * checkMCM request parameter is only present if mcmNotAvail is true.
	 * <p>
	 * The availability check of mcm is done either before 'queue' action or 
	 * if checkMCM request parameter is true.
	 */
	private boolean checkAvail = false;
	private final boolean admin;
	private boolean mcmUser = false;
	
	/**
	 * Creates the model.
	 * <p>
	 * Perform an initial media search with the default filter. <br>
	 * (search for all media with status COLLECTING)
	 * <p>
	 * performs an initial availability check for MCM_SCP service.
	 */
	private MCMModel(boolean admin) {
		this.admin  = admin;
		getFilter();
		filterMediaList( true );
		mcmNotAvail = ! MCMConsoleCtrl.getMcmScuDelegate().checkMcmScpAvail();
	}
	
	/**
	 * Get the model for an http request.
	 * <p>
	 * Look in the session for an associated model via <code>MCMMODEL_ATTR_NAME</code><br>
	 * If there is no model stored in session (first request) a new model is created and stored in session.
	 * 
	 * @param request A http request.
	 * 
	 * @return The model for given request.
	 */
	public static final MCMModel getModel( HttpServletRequest request ) {
		MCMModel model = (MCMModel) request.getSession().getAttribute(MCMMODEL_ATTR_NAME);
		if (model == null) {
				model = new MCMModel(request.isUserInRole("WebAdmin"));
				model.mcmUser = request.isUserInRole("McmUser");
				request.getSession().setAttribute(MCMMODEL_ATTR_NAME, model);
				model.setErrorCode( NO_ERROR ); //reset error code
		}
		return model;
	}

	public String getModelName() { return "MCM"; }
	
	/**
	 * @return Returns true if the user have WebAdmin role.
	 */
	public boolean isAdmin() {
		return admin;
	}

	/**
	 * @return Returns the mcmUser.
	 */
	public boolean isMcmUser() {
		return mcmUser;
	}
	/**
	 * Set the error code of this model.
	 * 
	 * @param errorCode The error code
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode  = errorCode;
		
	}
	
	/**
	 * Get current error code of this model.
	 * 
	 * @return error code.
	 */
	public String getErrorCode() {
		return errorCode;
	}
	
	/**
	 * @return Returns the popupMsg.
	 */
	public String getPopupMsg() {
		return popupMsg;
	}
	/**
	 * @param popupMsg The popupMsg to set.
	 */
	public void setPopupMsg(String popupMsg) {
		this.popupMsg = popupMsg;
	}
	/**
	 * Returns the status for 'queue' action.
	 * <p>
	 * This value is used used in the view to create a 'queue' action button/link 
	 * for media with this status.
	 * 
	 * @return The status for queue action
	 */
	public String getStatiForQueue() {
		return STATI_FOR_QUEUE;
	}
	
	/**
	 * Returns current list of media.
	 * 
	 * @return List of media.
	 */
	public List getMediaList() {
		return mediaList;
	}
	
	/**
	 * Returns the Filter that is used to search media.
	 * 
	 * @return current filter.
	 */
	public MCMFilter getFilter() {
		if ( filter == null ) filter = new MCMFilter();
		return filter;
	}
	
	/**
	 * Performa a media search with current filter settings.
	 * <p>
	 * If <code>newSearch is true</code> the <code>offset</code> is set to <code>0</code> (get first result page).
	 * <p>
	 * The result of the search is stored in <code>mediaList</code> and <code>total</code> is updated
	 * with the total number of results for this search.
	 * 
	 * @param newSearch
	 */
	public void filterMediaList( boolean newSearch ) {
		if ( newSearch ) offset = 0;
		try {
			Collection col = new ArrayList();
			Long start = null;
			Long end = null;
			int[] stati = null;
			if ( filter.selectedStati() != null ) { //not all
					stati = filter.selectedStati();
			}
			if ( ! mcmNotAvail && ( stati == null || 
					filter.getSelectedStatiAsString().indexOf( String.valueOf( MediaDTO.BURNING ) ) != -1 ) ) {
				//perform get media creation status if filter contains PROCESSING media status.
				MCMConsoleCtrl.getMcmScuDelegate().updateMediaStatus();
			}
			if ( MCMFilter.DATE_FILTER_ALL.equals( filter.getCreateOrUpdateDate() ) ) {
				total = lookupMediaComposer().findByCreatedTime( col, start, end, stati, 
 						new Integer( offset ), new Integer( limit ), filter.isDescent() );
			} else {
				start = filter.startDateAsLong();
				end = filter.endDateAsLong();
				if ( MCMFilter.CREATED_FILTER.equals( filter.getCreateOrUpdateDate() ) ) {
					total = lookupMediaComposer().findByCreatedTime( col, start, end, stati, 
				 						new Integer( offset ), new Integer( limit ), filter.isDescent() );
				
				} else if ( MCMFilter.UPDATED_FILTER.equals( filter.getCreateOrUpdateDate() ) ) {
					total = lookupMediaComposer().findByUpdatedTime( col, start, end, stati, 
	 						new Integer( offset ), new Integer( limit ), filter.isDescent() );
				}
			}
			mediaList = new MediaList( col );
			col.clear();
		} catch ( Exception x ) {
			//TODO
			x.printStackTrace();
			mediaList = new MediaList();
		}
	}
	
	/**
	 * Returns the MediaComposer bean.
	 * 
	 * @return The MediaComposer bean.
	 * @throws Exception
	 */
	protected MediaComposer lookupMediaComposer() throws Exception
	{
		MediaComposerHome home =
			(MediaComposerHome) EJBHomeFactory.getFactory().lookup(
					MediaComposerHome.class,
					MediaComposerHome.JNDI_NAME);
		return home.create();
	}			
	
	/**
	 * Returns current page limit.
	 * 
	 * @return Returns the limit.
	 */
	public int getLimit() {
		return limit;
	}
	/**
	 * Set current page limit.
	 * 
	 * @param limit The limit to set.
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	/**
	 * Return current offset (page number; starts with 0).
	 * 
	 * @return Returns the offset.
	 */
	public int getOffset() {
		return offset;
	}
	/**
	 * Set current page offset
	 * @param offset The offset to set.
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	/**
	 * Return the total number of results of the last search.
	 * 
	 * @return Returns the total.
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * @return Returns the mcmNotAvail.
	 */
	public boolean isMcmNotAvail() {
		return mcmNotAvail;
	}
	/**
	 * @param mcmNotAvail The mcmNotAvail to set.
	 */
	public void setMcmNotAvail(boolean mcmNotAvail) {
		this.mcmNotAvail = mcmNotAvail;
	}
	/**
	 * @return Returns the checkAvail.
	 */
	public boolean isCheckAvail() {
		return checkAvail;
	}
	/**
	 * @param checkAvail The checkAvail to set.
	 */
	public void setCheckAvail(boolean checkAvail) {
		this.checkAvail = checkAvail;
	}
	/**
	 * Goto previous page.
	 */
	public void performPrevious() {
		if ( offset - limit >= 0 ) {
			offset -= limit;
			filterMediaList( false );
		}
	}

	/**
	 * Goto next page.
	 *
	 */
	public void performNext() {
		if ( offset + limit < total ) {
			offset += limit;
			filterMediaList( false );
		}
	}

	/**
	 * Update the mediaStatus of media with given pk.
	 * <p>
	 * This method updates the status with information in current model and in MediaLocal bean.
	 * 
	 * @param mediaPk		The pk of the media to change.
	 * @param status		The new media staus.
	 * @param statusInfo 	Info text for the new status.
	 */
	public void updateMediaStatus(int mediaPk, int status, String statusInfo) {
		MediaData md = mediaDataFromList( mediaPk );
		if ( md != null ) {
			md.setMediaStatus( status );
			md.setMediaStatusInfo( statusInfo );
			try {
				this.lookupMediaComposer().setMediaStatus( mediaPk, status, statusInfo );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Returns the MediaData object for given media pk.
	 * 
	 * @param mediaPk PK of a media.
	 * 
	 * @return The MediaData object.
	 */
	public MediaData mediaDataFromList( int mediaPk ) {
		int pos = getMediaList().indexOf( new MediaData( mediaPk) );
		if ( pos != -1 ) {
			return (MediaData) this.mediaList.get( pos );
		}
		return null;
		
	}
	
}
