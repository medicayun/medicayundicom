/*
 * Created on 21.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mwl.model;

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
import org.dcm4chex.archive.web.maverick.mwl.MWLConsoleCtrl;

/**
 * @author franz.willer
 *
 * The Model for Media Creation Managment WEB interface.
 */
public class MWLModel {

	/** The session attribute name to store the model in http session. */
	public static final String MWLMODEL_ATTR_NAME = "mwlModel";
	
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

	/** Holds list of MWLEntries */
	private List mwlEntries = new ArrayList();

	private MWLFilter mwlFilter;
	
	/** True if mwlScpAET is 'local' to allow deletion */
	private boolean isLocal = false;

	/** Comparator to sort list of SPS datasets. */
	private Comparator comparator = new SpsDSComparator();

	private boolean admin = false;
	
	/**
	 * Creates the model.
	 * <p>
	 * Creates the filter instance for this model.
	 */
	private MWLModel( boolean admin ) {
		this.admin  = admin;
		getFilter();
	}
	
	/**
	 * Get the model for an http request.
	 * <p>
	 * Look in the session for an associated model via <code>MWLMODEL_ATTR_NAME</code><br>
	 * If there is no model stored in session (first request) a new model is created and stored in session.
	 * 
	 * @param request A http request.
	 * 
	 * @return The model for given request.
	 */
	public static final MWLModel getModel( HttpServletRequest request ) {
		MWLModel model = (MWLModel) request.getSession().getAttribute(MWLMODEL_ATTR_NAME);
		if (model == null) {
				model = new MWLModel(request.isUserInRole("WebAdmin"));
				request.getSession().setAttribute(MWLMODEL_ATTR_NAME, model);
				model.setErrorCode( NO_ERROR ); //reset error code
				model.filterWorkList( true );
		}
		return model;
	}

	/**
	 * @return Returns true if the user have WebAdmin role.
	 */
	public boolean isAdmin() {
		return admin;
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
	 * @return MWLFilter instance that hold filter criteria values.
	 */
	public MWLFilter getFilter() {
		if ( mwlFilter == null ) {
			mwlFilter = new MWLFilter();
		}
		return mwlFilter;
	}
	
	/**
	 * Return a list of MWLEntries for display.
	 * 
	 * @return Returns the mwlEntries.
	 */
	public List getMwlEntries() {
		return mwlEntries;
	}
	/**
	 * Returns true if the MwlScpAET is 'local'.
	 * <p>
	 * <DL>
	 * <DT>If it is local:</DT>
	 * <DD>  1) Entries can be deleted. (shows a button in view)</DD>
	 * <DD>  2) The query for the working list is done directly without a CFIND.</DD>
	 * </DL>
	 * @return Returns the isLocal.
	 */
	public boolean isLocal() {
		return isLocal;
	}

	/**
	 * Update the list of MWLEntries for the view.
	 * <p>
	 * The query use the search criteria values from the filter and use offset and limit for paging.
	 * <p>
	 * if <code>newSearch is true</code> will reset paging (set <code>offset</code> to 0!)
	 * @param newSearch
	 */
	public void filterWorkList(boolean newSearch) {
		
		if ( newSearch ) offset = 0;
		//_filterTest();
		Dataset searchDS = getSearchDS( mwlFilter );
		isLocal = MWLConsoleCtrl.getMwlScuDelegate().isLocal();
		List l = MWLConsoleCtrl.getMwlScuDelegate().findMWLEntries( searchDS );
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
		mwlEntries.clear();
		for ( int i = offset ; i < end ; i++ ){
			ds = (Dataset) l.get( i );
			if ( ds != null )
				mwlEntries.add( new MWLEntry( ds ) );
		}
		total = mwlEntries.size(); // the real total (without null entries!)
	}

	/**
	 * Returns the query Dataset with search criteria values from filter argument.
	 * <p>
	 * The Dataset contains all fields that should be in the result; 
	 * the 'merging' fields will be set to the values from the filter.
	 * 
	 * @param mwlFilter2 The filter with search criteria values.
	 * 
	 * @return The Dataset that can be used for query.
	 */
	private Dataset getSearchDS(MWLFilter filter) {
		Dataset ds = DcmObjectFactory.getInstance().newDataset();
		//requested procedure
		ds.putSH( Tags.RequestedProcedureID );
		ds.putUI( Tags.StudyInstanceUID );
		//imaging service request
		ds.putSH( Tags.AccessionNumber, mwlFilter.getAccessionNumber() );
		ds.putLT( Tags.ImagingServiceRequestComments );
		ds.putPN( Tags.RequestingPhysician );
		ds.putPN( Tags.ReferringPhysicianName );
		ds.putLO( Tags.PlacerOrderNumber );
		ds.putLO( Tags.FillerOrderNumber );
		//Visit Identification
		ds.putLO( Tags.AdmissionID );
		//Patient Identification
		String patientName = mwlFilter.getPatientName();
    	if ( patientName != null && 
       		 patientName.length() > 0 && 
   			 patientName.indexOf('*') == -1 &&
   			 patientName.indexOf('?') == -1) patientName+="*";

		ds.putPN( Tags.PatientName, patientName );
		ds.putLO( Tags.PatientID);
		//Patient demographic
		ds.putDA( Tags.PatientBirthDate );
		ds.putCS( Tags.PatientSex );
		//Sched. procedure step seq
		DcmElement elem = ds.putSQ( Tags.SPSSeq );
		Dataset ds1 = elem.addNewItem();
		ds1.putAE( Tags.ScheduledStationAET, filter.getStationAET() );
		ds1.putSH( Tags.SPSID );
		ds1.putCS( Tags.Modality, filter.getModality() );
		ds1.putPN( Tags.ScheduledPerformingPhysicianName );
		if ( filter.getStartDate() != null || filter.getEndDate() != null ) {
			Date startDate = null, endDate = null;
			if ( filter.startDateAsLong() != null ) 
				startDate = new Date ( filter.startDateAsLong().longValue() );
			if ( filter.endDateAsLong() != null ) 
				endDate = new Date ( filter.endDateAsLong().longValue() );
			ds1.putDA( Tags.SPSStartDate, startDate, endDate );
			ds1.putTM( Tags.SPSStartTime, startDate, endDate );
		} else {
			ds1.putDA( Tags.SPSStartDate );
			ds1.putTM( Tags.SPSStartTime );
		}
		ds1.putSH( Tags.ScheduledStationName );
		//sched. protocol code seq;
		DcmElement spcs = ds1.putSQ( Tags.ScheduledProtocolCodeSeq );
		Dataset dsSpcs = spcs.addNewItem();
		dsSpcs.putSH( Tags.CodeValue );
		dsSpcs.putLO( Tags.CodeMeaning );
		dsSpcs.putSH( Tags.CodingSchemeDesignator );
		// or 
		ds1.putLO( Tags.SPSDescription );
		
		//Req. procedure code seq
		DcmElement rpcs = ds.putSQ( Tags.RequestedProcedureCodeSeq );
		Dataset dsRpcs = rpcs.addNewItem();
		dsRpcs.putSH( Tags.CodeValue );
		dsRpcs.putLO( Tags.CodeMeaning );
		dsRpcs.putSH( Tags.CodingSchemeDesignator );
		// or 
		ds.putLO( Tags.RequestedProcedureDescription );

		return ds;
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
	 * Inner class that compares two datasets for sorting Scheduled Procedure Steps 
	 * according scheduled Procedure step start date.
	 * 
	 * @author franz.willer
	 *
	 * TODO To change the template for this generated type comment go to
	 * Window - Preferences - Java - Code Style - Code Templates
	 */
	public class SpsDSComparator implements Comparator {

		private final Date DATE_0 = new Date(0l);
		public SpsDSComparator() {
			
		}

		/**
		 * Compares the scheduled procedure step start date and time of two Dataset objects.
		 * <p>
		 * USe either SPSStartDateAndTime or SPSStartDate and SPSStartTime to get the date.
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
		 * @return the date of this SPS Dataset.
		 */
		private Date _getStartDateAsLong(Dataset ds) {
			if ( ds == null ) return DATE_0;
			DcmElement e = ds.get( Tags.SPSSeq );
			if ( e == null ) return DATE_0;
			Dataset spsItem = e.getItem();//scheduled procedure step sequence item.
			Date d = spsItem.getDate( Tags.SPSStartDateAndTime );
			if ( d == null ) {
				d = spsItem.getDateTime( Tags.SPSStartDate, Tags.SPSStartTime );
			}
			if ( d == null ) d = DATE_0;
			return d;
		}
	}
	
}
