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

package org.dcm4chex.archive.web.maverick.mpps.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.jdbc.MPPSFilter;
import org.dcm4chex.archive.web.maverick.mpps.MPPSConsoleCtrl;

/**
 * @author franz.willer
 *
 * The Model for Modality Performed Procedure Steps WEB interface.
 */
public class MPPSModel {

	/** The session attribute name to store the model in http session. */
	public static final String MPPS_MODEL_ATTR_NAME = "mppsModel";
	
	/** Errorcode: no error */
    public static final String NO_ERROR ="OK";
    /** Errorcode: unsupported action */
	public static final String ERROR_UNSUPPORTED_ACTION = "UNSUPPORTED_ACTION";

	/** holds current error code. */
	private String errorCode = NO_ERROR;
	/** Holds the current offset for paging */
	private int offset = 0;
	/** Holds the limit for paging */
	private int limit = 20;
	/** Holds the total number of results of last search. */
	private int total = 0;

	/** Holds list of MPPSEntries */
	private List mppsEntries = new ArrayList();

	private MPPSFilter mppsFilter;
	

	/** Comparator to sort list of MPPS datasets. */
	private Comparator comparator = new MppsDSComparator();

	private boolean admin = false;
	private boolean mcmUser = false;
	
	/**
	 * Creates the model.
	 * <p>
	 * Creates the filter instance for this model.
	 */
	private MPPSModel( boolean admin ) {
		this.admin  = admin;
		getFilter();
	}
	
	/**
	 * Get the model for an http request.
	 * <p>
	 * Look in the session for an associated model via <code>MPPS_MODEL_ATTR_NAME</code><br>
	 * If there is no model stored in session (first request) a new model is created and stored in session.
	 * 
	 * @param request A http request.
	 * 
	 * @return The model for given request.
	 */
	public static final MPPSModel getModel( HttpServletRequest request ) {
		MPPSModel model = (MPPSModel) request.getSession().getAttribute(MPPS_MODEL_ATTR_NAME);
		if (model == null) {
				model = new MPPSModel(request.isUserInRole("WebAdmin"));
				model.mcmUser = request.isUserInRole("McmUser");
				request.getSession().setAttribute(MPPS_MODEL_ATTR_NAME, model);
				model.setErrorCode( NO_ERROR ); //reset error code
				model.filterWorkList( true );
		}
		return model;
	}

	public String getModelName() { return "MPPS"; }
	
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
	 * Returns the Filter of this model.
	 * 
	 * @return MPPSFilter instance that hold filter criteria values.
	 */
	public MPPSFilter getFilter() {
		if ( mppsFilter == null ) {
			mppsFilter = new MPPSFilter();
		}
		return mppsFilter;
	}
	
	/**
	 * Return a list of MPPSEntries for display.
	 * 
	 * @return Returns the mppsEntries.
	 */
	public List getMppsEntries() {
		return mppsEntries;
	}

	/**
	 * Update the list of MPPSEntries for the view.
	 * <p>
	 * The query use the search criteria values from the filter and use offset and limit for paging.
	 * <p>
	 * if <code>newSearch is true</code> will reset paging (set <code>offset</code> to 0!)
	 * @param newSearch
	 */
	public void filterWorkList(boolean newSearch) {
		
		if ( newSearch ) offset = 0;
		//_filterTest();
		List l = MPPSConsoleCtrl.getMppsDelegate().findMppsEntries( this.mppsFilter );
		Collections.sort( l, comparator );
		total = l.size();
		int end;
		if ( offset >= total ) {
			offset = 0;
			end = limit < total ? limit : total;
		} else {
			end = offset + limit;
			if ( end > total ) end = total;
		}
		Dataset ds;
		mppsEntries.clear();
		int countNull = 0;
		for ( int i = offset ; i < end ; i++ ){
			ds = (Dataset) l.get( i );
			if ( ds != null ) {
				mppsEntries.add( new MPPSEntry( ds ) );
			} else {
				countNull++;
			}
		}
		total -= countNull; // the real total (without null entries!)
	}


	/**
	 * Goto previous page.
	 */
	public void performPrevious() {
		if ( offset - limit >= 0 ) {
			offset -= limit;
			filterWorkList( false );
		}
	}

	/**
	 * Goto next page.
	 *
	 */
	public void performNext() {
		if ( offset + limit < total ) {
			offset += limit;
			filterWorkList( false );
		}
	}
	
	/**
	 * Inner class that compares two datasets for sorting Performed Procedure Steps 
	 * according Performed Procedure step start date/time.
	 * 
	 * @author franz.willer
	 *
	 * TODO To change the template for this generated type comment go to
	 * Window - Preferences - Java - Code Style - Code Templates
	 */
	public class MppsDSComparator implements Comparator {

		public MppsDSComparator() {
			
		}

		/**
		 * Compares the performed procedure step start date and time of two Dataset objects.
		 * <p>
		 * USe PPSStartDate and PPSStartTime to get the date.
		 * <p>
		 * Use the '0' Date (new Date(0l)) if the date is not in the Dataset.
		 * <p>
		 * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer 
		 * as the first argument is less than, equal to, or greater than the second.
		 * <p>
		 * Throws an Exception if one of the arguments is null or not a Dataset object.
		 * 
		 * @param arg0 	First argument
		 * @param arg1	Second argument
		 * 
		 * @return <0 if arg0<arg1, 0 if equal and >0 if arg0>arg1
		 */
		public int compare(Object arg0, Object arg1) {
			Dataset ds1 = (Dataset) arg0;
			Dataset ds2 = (Dataset) arg1;
			Date d1 = _getStartDateAsLong( ds1 );
			return d1.compareTo( _getStartDateAsLong( ds2 ) );
		}

		/**
		 * @param ds1 The dataset
		 * 
		 * @return the date of this PPS Dataset.
		 */
		private Date _getStartDateAsLong(Dataset ds) {
			if ( ds == null ) return new Date( 0l );
			
			Date d = ds.getDateTime( Tags.PPSStartDate, Tags.PPSStartTime );
			if ( d == null ) d = new Date(0l);
			return d;
		}
	}
	
}
