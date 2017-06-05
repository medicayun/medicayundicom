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

package org.dcm4chex.archive.web.maverick.mwl.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MWLFilter {
	
	/** Holds the patient name of this filter. */
	private String patientName;
	/** holds the 'left' string value of start time range. */
	private String startDate = "";
	/** holds the 'right' string value of start time range. */
	private String endDate = "";
	/** holds the 'left' Long value of start time range. (null if string value is empty) */
	private Long startDateAsLong;
	/** holds the 'right' Long value of start time range. (null if string value is empty) */
	private Long endDateAsLong;
	/** Holds the modality of this filter. */
	private String modality;
	/** Holds the station AET of this filter. */
	private String stationAET;
	/** Holds the accession number of this filter. */
	private String accessionNumber;
	
	/** The Date/Time formatter to parse input field values. */
	private static final SimpleDateFormat dFormatter = new SimpleDateFormat("yyyy/MM/dd");
	/** The Date/Time formatter to parse input field values. */
	private static final SimpleDateFormat dtFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
	
	public MWLFilter() {
		String d = dFormatter.format( new Date() );//today 00:00:00
		try {
			setStartDate( d );
			setEndDate( d+" 23:59:59" );
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			this.startDateAsLong = null;
			this.startDate = null;
		} else {
			try {
				this.startDateAsLong = new Long( dtFormatter.parse( startDate ).getTime() ); //try date and time
			} catch ( Exception x ) {
				this.startDateAsLong = new Long( dFormatter.parse( startDate ).getTime() ); //ok, only date
			}
			//correct display view from inputs like 2005/20/50! 
			this.startDate = dtFormatter.format( new Date( startDateAsLong.longValue() ) ) ;
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
			this.endDateAsLong = null;
			this.endDate = null;
		} else {
			try {
				this.endDateAsLong = new Long( dtFormatter.parse( endDate ).getTime() );// try date and time
			} catch ( Exception x ) {
				this.endDateAsLong = new Long( dFormatter.parse( endDate ).getTime() );// ok only date
			}
			//correct display view from inputs like 2005/20/50! 
			this.endDate = dtFormatter.format( new Date( endDateAsLong.longValue() ) ) ;
		}
	}
	
	/**
	 * @return Returns the endCreationAsLong.
	 */
	public Long endDateAsLong() {
		return endDateAsLong;
	}
	/**
	 * @return Returns the startCreationAsLong.
	 */
	public Long startDateAsLong() {
		return startDateAsLong;
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
		if ( accessionNumber == null || accessionNumber.trim().length() < 1 )
			this.accessionNumber = null;
		else
			this.accessionNumber = accessionNumber;
	}
	
}
