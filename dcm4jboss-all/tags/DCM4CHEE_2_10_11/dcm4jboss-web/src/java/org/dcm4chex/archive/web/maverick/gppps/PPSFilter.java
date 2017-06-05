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

package org.dcm4chex.archive.web.maverick.gppps;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PPSFilter {
	private static final int END_DATE = 1;
	private static final int START_DATE = 0;
	protected Dataset queryDS = DcmObjectFactory.getInstance().newDataset();
	private String patientName;
	
	private static final SimpleDateFormat dtFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
	public PPSFilter() {
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
		patientName = name;
    	if ( name != null && 
			name.length() > 0 && 
			name.indexOf('*') == -1 &&
			name.indexOf('?') == -1) name+="*";
		queryDS.putPN(Tags.PatientName, name);
	}
	
	/**
	 * @return Returns the startDate.
	 */
	public String getStartDate() {
		Date[] dates = queryDS.getDateTimeRange(Tags.PPSStartDate,Tags.PPSStartTime);
		if ( dates == null || dates[0] == null) return null;
		return dtFormatter.format(dates[0]);
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
		setDateInRange( parseDate(startDate), START_DATE);
	}
	
	/**
	 * @return Returns the endCreationDate.
	 */
	public String getEndDate() {
		Date[] dates = queryDS.getDateTimeRange(Tags.PPSStartDate,Tags.PPSStartTime);
		if ( dates == null || dates[1] == null) return null;
		return dtFormatter.format(dates[1]);
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
		setDateInRange( parseDate(endDate), END_DATE);
	}
	
	private void setDateInRange( Date date, int startEnd) {
		Date[] dates = queryDS.getDateTimeRange(Tags.PPSStartDate,Tags.PPSStartTime);
		if ( dates == null ) dates = new Date[]{null,null};
		dates[startEnd] = date;
		queryDS.putDA(Tags.PPSStartDate, dates[0], dates[1]);
		queryDS.putTM(Tags.PPSStartTime, dates[0], dates[1]);
	}
	
	/**
	 * @return Returns the status.
	 */
	public String getStatus() {
		return queryDS.getString(Tags.PPSStatus);
	}
	/**
	 * @param status The status to set.
	 */
	public void setStatus(String status) {
		queryDS.putCS(Tags.PPSStatus, status);
	}
	/**
	 * @return
	 */
	public String getSopIuid() {
		return queryDS.getString(Tags.SOPInstanceUID);
	}
	
	public void setSopIuid( String uid ) {
		queryDS.putUI(Tags.SOPInstanceUID, uid);
	}
	/**
	 * @return
	 */
	public String getPatientID() {
		return queryDS.getString(Tags.PatientID);
	}
	
	public void setPatientID( String id ) {
		queryDS.putLO(Tags.PatientID, id);
	}

	
	/**
	 * @param startDate2
	 * @return
	 * @throws ParseException
	 */
	private Date parseDate(String date) throws ParseException {
		if ( date == null || date.trim().length() < 1) return null;
		ParsePosition pp = new ParsePosition(0);
		Date d = dtFormatter.parse( date, pp );
		if ( d == null ) {
				String s = date.substring(0, pp.getErrorIndex());
				SimpleDateFormat f1 = new SimpleDateFormat( dtFormatter.toPattern().substring( 0, pp.getErrorIndex() ) );
				d = f1.parse( s );
		}
		return d;
	}
	
	/**
	 * @return
	 */
	public Dataset toSearchDS() {
		return queryDS;
	}
}
