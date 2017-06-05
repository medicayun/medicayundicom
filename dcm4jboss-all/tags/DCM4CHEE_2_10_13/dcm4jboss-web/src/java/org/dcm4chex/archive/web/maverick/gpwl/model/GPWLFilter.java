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

package org.dcm4chex.archive.web.maverick.gpwl.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GPWLFilter {

	//Required
	private String iuid;
	/** General Purpose Scheduled Procedure Step Status */
	private String status;
	/** Input Availabilty Flag */
	private String inputAvailability;
	/** General Purpose Scheduled Procedure Step Priority */
	private String priority;
	/** Scheduled Workitem code */
	private String workitemCode;
	/** Scheduled Station Name code */
	private String stationNameCode;
	/** Scheduled Station Class code */
	private String stationClassCode;
	/** Scheduled Station Geographic Location code */
	private String stationGeoCode;
	/** holds the 'left' string value of SPS Start time range. */
	private String spsStartDate = null;
	/** holds the 'right' string value of SPS Start time range. */
	private String spsEndDate = null;
	/** holds the 'left' string value of Expected Completion time range. */
	private String completionStartDate = null;
	/** holds the 'right' string value of Expected Completion time range. */
	private String completionEndDate = null;
	/** Scheduled Human Performer code */
	private String humanPerformerCode;
	/** Holds the patient name of this filter. */
	private String patientName;
	/** Holds the Patient ID of this filter. */
	private String patID;
	/** Holds the study instance UID of this filter. */
	private String studyIUID;
	/** Holds the accession number of this filter. */
	private String accessionNumber;

	
//optional	
	/** Scheduled Procedure Step ID */
	private String spsID;
	/** holds the 'left' string value of SPS Modification time range. */
	private String spsMStartDate = "";
	/** holds the 'right' string value of SPS Modification time range. */
	private String spsMEndDate = "";
	/** Scheduled Human Performer Name */
	private String humanPerformerName;

	/** The Date/Time formatter to parse input field values. */
	private static final SimpleDateFormat dFormatter = new SimpleDateFormat("yyyy/MM/dd");
	/** The Date/Time formatter to parse input field values. */
	private static final SimpleDateFormat dtFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
	
	public GPWLFilter() {
		String d = dFormatter.format( new Date() );//today 00:00:00
		try {
			setSPSStartDate( d );
			setSPSEndDate( d+" 23:59:00" );
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * @return Returns the iuid.
	 */
	public String getIUID() {
		return iuid;
	}
	/**
	 * @param iuid The iuid to set.
	 */
	public void setIUID(String iuid) {
		this.iuid = iuid;
	}
	/**
	 * @return Returns the humanPerformerCode.
	 */
	public String getHumanPerformerCode() {
		return humanPerformerCode;
	}
	/**
	 * @param humanPerformerCode The humanPerformerCode to set.
	 */
	public void setHumanPerformerCode(String humanPerformerCode) {
		this.humanPerformerCode = humanPerformerCode;
	}
	/**
	 * @return Returns the humanPerformerName.
	 */
	public String getHumanPerformerName() {
		return humanPerformerName;
	}
	/**
	 * @param humanPerformerName The humanPerformerName to set.
	 */
	public void setHumanPerformerName(String humanPerformerName) {
		this.humanPerformerName = humanPerformerName;
	}
	/**
	 * @return Returns the inputAvailability.
	 */
	public String getInputAvailability() {
		return inputAvailability;
	}
	/**
	 * @param inputAvailability The inputAvailability to set.
	 */
	public void setInputAvailability(String inputAvailability) {
		this.inputAvailability = inputAvailability;
	}
	/**
	 * @return Returns the patID.
	 */
	public String getPatID() {
		return patID;
	}
	/**
	 * @param patID The patID to set.
	 */
	public void setPatID(String patID) {
		this.patID = patID;
	}
	/**
	 * @return Returns the priority.
	 */
	public String getPriority() {
		return priority;
	}
	/**
	 * @param priority The priority to set.
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}
/**
 * @return Returns the spsID.
 */
public String getSpsID() {
	return spsID;
}
/**
 * @param spsID The spsID to set.
 */
public void setSpsID(String spsID) {
	this.spsID = spsID;
}
	/**
	 * @return Returns the stationClassCode.
	 */
	public String getStationClassCode() {
		return stationClassCode;
	}
	/**
	 * @param stationClassCode The stationClassCode to set.
	 */
	public void setStationClassCode(String stationClassCode) {
		this.stationClassCode = stationClassCode;
	}
	/**
	 * @return Returns the stationGeoCode.
	 */
	public String getStationGeoCode() {
		return stationGeoCode;
	}
	/**
	 * @param stationGeoCode The stationGeoCode to set.
	 */
	public void setStationGeoCode(String stationGeoCode) {
		this.stationGeoCode = stationGeoCode;
	}
	/**
	 * @return Returns the stationNameCode.
	 */
	public String getStationNameCode() {
		return stationNameCode;
	}
	/**
	 * @param stationNameCode The stationNameCode to set.
	 */
	public void setStationNameCode(String stationNameCode) {
		this.stationNameCode = stationNameCode;
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
	 * @return Returns the workitemCode.
	 */
	public String getWorkitemCode() {
		return workitemCode;
	}
	/**
	 * @param workitemCode The workitemCode to set.
	 */
	public void setWorkitemCode(String workitemCode) {
		this.workitemCode = workitemCode;
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
	 * Set the Scheduled Procedure Step query start date.
	 * <p>
	 * 
	 * 
	 * @param startDate The start Date to set.
	 * @throws ParseException
	 */
	public void setSPSStartDate(String startDate) throws ParseException {
		spsStartDate = normalizeDateString(startDate);
	}
	
	public String getSPSStartDate() {
		return spsStartDate;
	}
	
	public Date spsStartAsDate() throws ParseException {
		return string2Date( spsStartDate );
	}

	/**
	 * Set the Scheduled Procedure Step query end date.
	 * <p>
	 * 
	 * 
	 * @param endDate The end Date to set.
	 * @throws ParseException
	 */
	public void setSPSEndDate(String endDate) throws ParseException {
		spsEndDate = normalizeDateString(endDate);
	}
	
	public String getSPSEndDate() {
		return spsEndDate;
	}
	
	public Date spsEndAsDate() throws ParseException {
		return string2Date( spsEndDate );
	}

	/**
	 * Set the Scheduled Procedure Step query start date.
	 * <p>
	 * 
	 * 
	 * @param startDate The start Date to set.
	 * @throws ParseException
	 */
	public void setCompletionStartDate(String startDate) throws ParseException {
		completionStartDate = normalizeDateString(startDate);
	}
	
	public String getCompletionStartDate() {
		return completionStartDate;
	}
	
	public Date completionStartAsDate() throws ParseException {
		return string2Date( completionStartDate );
	}

	/**
	 * Set the Scheduled Procedure Step query end date.
	 * <p>
	 * 
	 * 
	 * @param endDate The end Date to set.
	 * @throws ParseException
	 */
	public void setCompletionEndDate(String endDate) throws ParseException {
		completionEndDate = normalizeDateString(endDate);
	}
	
	public String getCompletionEndDate() {
		return completionEndDate;
	}
	
	public Date completionEndAsDate() throws ParseException {
		return string2Date( completionEndDate );
	}
	
	/**
	 * Set the Scheduled Procedure Step query start date.
	 * <p>
	 * 
	 * 
	 * @param startDate The start Date to set.
	 * @throws ParseException
	 */
	public void setSPSMStartDate(String startDate) throws ParseException {
		spsMStartDate = normalizeDateString(startDate);
	}
	
	public String getSPSMStartDate() {
		return spsMStartDate;
	}
	
	public Date spsMStartAsDate() throws ParseException {
		return string2Date( spsMStartDate );
	}

	/**
	 * Set the Scheduled Procedure Step query end date.
	 * <p>
	 * 
	 * 
	 * @param endDate The end Date to set.
	 * @throws ParseException
	 */
	public void setSPSMEndDate(String endDate) throws ParseException {
		spsMEndDate = normalizeDateString(endDate);
	}
	
	public String getSPSMEndDate() {
		return spsMEndDate;
	}
	
	public Date spsMEndAsDate() throws ParseException {
		return string2Date(spsMEndDate);
	}
	
	/**
	 * @return
	 * @throws ParseException
	 */
	private Date string2Date(String dateString) throws ParseException {
		return dateString != null ? dtFormatter.parse( dateString ) : null;
	}
	private String normalizeDateString(String startDate) throws ParseException {
	
		if ( startDate == null || startDate.trim().length() < 1 ) return null;
		long l;
		try {
			l = dtFormatter.parse( startDate ).getTime(); //try date and time
		} catch ( Exception x ) {
			l = dFormatter.parse( startDate ).getTime(); //ok, only date
		}
		//correct display view from inputs like 2005/20/50! 
		return dtFormatter.format( new Date( l ) ) ;
	
	}
	
	

	/**
	 * @return Returns the studyIUID.
	 */
	public String getStudyIUID() {
		return studyIUID;
	}
	/**
	 * @param studyIUID The studyIUID to set.
	 */
	public void setStudyIUID(String studyIUID) {
		this.studyIUID = studyIUID;
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
	/**
	 * 
	 */
	public void clear() {
		status = null;
		inputAvailability = null;
		priority = null;
		workitemCode = null;
		stationNameCode = null;
		stationClassCode = null;
		stationGeoCode = null;
		spsStartDate = null;
		spsEndDate = null;
		completionStartDate = null;
		completionEndDate = null;
		humanPerformerCode = null;
		patientName = null;
		patID = null;
		studyIUID = null;
		accessionNumber = null;
		spsID = null;
		spsMStartDate = "";
		spsMEndDate = "";
		humanPerformerName = null;
	}
	
}
