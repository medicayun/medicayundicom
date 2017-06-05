/*
 * Created on 22.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.ejb.jdbc;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MPPSFilter {
	
	/** Holds the patient name of this filter. */
	private String patientName;
	/** holds the 'left' string value of start time range. */
	private String startDate = null;
	/** holds the 'right' string value of start time range. */
	private String endDate = null;
	/** Holds the modality of this filter. */
	private String modality;
	/** Holds the station AET of this filter. */
	private String stationAET;
	/** Holds the accession number of this filter. */
	private String accessionNumber;
	/** Holds the MPPS status of this filter. */
	private String status;
	
	private String sopIuid;
	private String patID;
	
	private static final SimpleDateFormat dtFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	private boolean emptyAccNo;
	
	public MPPSFilter() {
	}
	/**
	 * returns the patient name filter value.
	 * 
	 * @return Filter value of patient name field or null.
	 */
	public String getPatientName() {
		return patientName;
	}
	
	/**
	 * set the filter patient name.
	 * @param name
	 */
	public void setPatientName( String name ){
		if ( name == null || name.trim().length() < 1 ) 
			patientName = null;
		else
			patientName = name;
	}
	
	/**
	 * @return Returns the startDate.
	 */
	public String getStartDate() {
		return startDate;
	}
	/**
	 * Set the start date.
	 * <p>
	 * Set both <code>startDate and startDateAsLong</code>.<br>
	 * If the parameter is null or empty, both values are set to <code>null</code>
	 * 
	 * @param startDate The start Date to set.
	 * @throws ParseException
	 */
	public void setStartDate(String startDate) throws ParseException {
		if ( startDate == null || startDate.trim().length() < 1 ) {
			this.startDate = null;
		} else {
			this.startDate = checkDate( startDate );
		}
	
	}
	
	/**
	 * @return Returns the endCreationDate.
	 */
	public String getEndDate() {
		return endDate;
	}
	/**
	 * Set the end start date.
	 * <p>
	 * Set both <code>endDate and endDateAsLong</code>.<br>
	 * If the parameter is null or empty, both values are set to <code>null</code>
	 * 
	 * @param endDate The endDate to set.
	 * 
	 * @throws ParseException If param is not a date/time string of format specified in formatter.
	 */
	public void setEndDate(String endDate) throws ParseException {
		if ( endDate == null || endDate.trim().length() < 1 ) {
			this.endDate = null;
		} else {
			this.endDate = checkDate( endDate );
		}
	}
	
	/**
	 * returns the modality filter value.
	 * 
	 * @return Filter value of modality field or null.
	 */
	public String getModality() {
		return modality;
	}
	
	/**
	 * set the filter modality.
	 * @param name
	 */
	public void setModality( String mod ){
		if ( mod == null || mod.trim().length() < 1 )
			modality = null;
		else 
			modality = mod;
	}
	
	/**
	 * @return Returns the stationAET.
	 */
	public String getStationAET() {
		return stationAET;
	}
	/**
	 * @param aet The stationAET to set.
	 */
	public void setStationAET(String aet) {
		if ( aet == null || aet.trim().length() < 1 )
			stationAET = null;
		else
			stationAET = aet;
	}

	/**
	 * @return Returns the accessionNumber.
	 */
	public String getAccessionNumber() {
		return accessionNumber;
	}
	/**
	 * @param accessionNumber The accessionNumber to set.
	 */
	public void setAccessionNumber(String accessionNumber) {
		if ( accessionNumber == null | accessionNumber.trim().length() < 1 )
			this.accessionNumber = null;
		else
			this.accessionNumber = accessionNumber;
	}
	
	/**
	 * @return Returns the status.
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status The status to set.
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return
	 */
	public String getSopIuid() {
		return sopIuid;
	}
	
	public void setSopIuid( String uid ) {
		sopIuid = uid;
	}
	/**
	 * @return
	 */
	public String getPatientID() {
		return patID;
	}
	
	public void setPatientID( String id ) {
		patID = id;
	}
	/**
	 * @return
	 */
	public Date[] dateTimeRange() {
		if ( startDate == null && endDate == null ) return null;
		Date[] da = new Date[2];
		try {
			if ( startDate != null ) da[0] = parseDate( startDate );
			if ( endDate != null ) da[1] = parseDate( endDate );
		} catch ( ParseException x ) { /*ignore*/ }
		return da;
	}

	
	/**
	 * @param startDate2
	 * @return
	 * @throws ParseException
	 */
	private String checkDate(String date) throws ParseException {
		return dtFormatter.format( parseDate( date ) );
	}

	/**
	 * @param startDate2
	 * @return
	 * @throws ParseException
	 */
	private Date parseDate(String date) throws ParseException {
		ParsePosition pp = new ParsePosition(0);
		Date d = dtFormatter.parse( date, pp );
		if ( d == null ) {
				String s = date.substring(0, pp.getErrorIndex());
				SimpleDateFormat f1 = new SimpleDateFormat( dtFormatter.toPattern().substring( 0, pp.getErrorIndex() ) );
				d = f1.parse( s );
		}
		return d;
	}
	
	public boolean isEmptyAccNo() {
		return emptyAccNo;
	}
	/**
	 * @param parameter
	 */
	public void setEmptyAccNo(String parameter) {
		emptyAccNo = "true".equals( parameter );
	}
}
