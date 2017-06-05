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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.web.maverick.model.BasicFilterModel;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MWLFilter extends BasicFilterModel {
	private Dataset dsSPS;
    
    private String startDate;
    
	public MWLFilter() {
        super();
	}
    
    public void init() {
        //requested procedure
        ds.putSH( Tags.RequestedProcedureID );
        ds.putUI( Tags.StudyInstanceUID );
        //imaging service request
        ds.putSH( Tags.AccessionNumber );
        ds.putLT( Tags.ImagingServiceRequestComments );
        ds.putPN( Tags.RequestingPhysician );
        ds.putPN( Tags.ReferringPhysicianName );
        ds.putLO( Tags.PlacerOrderNumber );
        ds.putLO( Tags.FillerOrderNumber );
        //Visit Identification
        ds.putPN( Tags.PatientName );
        ds.putLO( Tags.AdmissionID );
        ds.putLO( Tags.PatientID);
        //Patient demographic
        ds.putDA( Tags.PatientBirthDate );
        ds.putCS( Tags.PatientSex );
        //Sched. procedure step seq
        dsSPS = ds.putSQ(Tags.SPSSeq).addNewItem();
        dsSPS.putCS(Tags.SPSStatus);
        dsSPS.putAE( Tags.ScheduledStationAET );
        dsSPS.putSH( Tags.SPSID );
        dsSPS.putCS( Tags.Modality );
        dsSPS.putPN( Tags.ScheduledPerformingPhysicianName );
        String d = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        try {
            setStartDate( d );
        } catch (ParseException ignore) {}
        dsSPS.putSH( Tags.ScheduledStationName );
        //sched. protocol code seq;
        DcmElement spcs = dsSPS.putSQ( Tags.ScheduledProtocolCodeSeq );
        Dataset dsSpcs = spcs.addNewItem();
        dsSpcs.putSH( Tags.CodeValue );
        dsSpcs.putLO( Tags.CodeMeaning );
        dsSpcs.putSH( Tags.CodingSchemeDesignator );
        // or 
        dsSPS.putLO( Tags.SPSDescription );
        
        //Req. procedure code seq
        DcmElement rpcs = ds.putSQ( Tags.RequestedProcedureCodeSeq );
        Dataset dsRpcs = rpcs.addNewItem();
        dsRpcs.putSH( Tags.CodeValue );
        dsRpcs.putLO( Tags.CodeMeaning );
        dsRpcs.putSH( Tags.CodingSchemeDesignator );
        // or 
        ds.putLO( Tags.RequestedProcedureDescription );
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
        this.startDate = startDate;
        setDateRange(dsSPS, Tags.SPSStartDate, startDate );
	}
	
	
	/**
	 * returns the modality filter value.
	 * 
	 * @return Filter value of modality field or null.
	 */
	public String getModality() {
		return dsSPS.getString( Tags.Modality );
	}
	
	/**
	 * set the filter modality.
	 * @param name
	 */
	public void setModality( String mod ){
		if ( mod == null || mod.trim().length() < 1 )
            dsSPS.putCS( Tags.Modality );
		else 
            dsSPS.putCS( Tags.Modality, mod);
	}
	
	/**
	 * @return Returns the stationAET.
	 */
	public String getStationAET() {
		return dsSPS.getString( Tags.ScheduledStationAET );
	}
	/**
	 * @param aet The stationAET to set.
	 */
	public void setStationAET(String aet) {
		if ( aet == null || aet.trim().length() < 1 )
            dsSPS.putAE( Tags.ScheduledStationAET );
		else
            dsSPS.putAE( Tags.ScheduledStationAET, aet);
	}

	/**
	 * @return Returns the accessionNumber.
	 */
	public String getAccessionNumber() {
		return ds.getString( Tags.AccessionNumber );
	}
	/**
	 * @param accessionNumber The accessionNumber to set.
	 */
	public void setAccessionNumber(String accessionNumber) {
		if ( accessionNumber == null || accessionNumber.trim().length() < 1 )
            ds.putSH( Tags.AccessionNumber );
		else
            ds.putSH( Tags.AccessionNumber, accessionNumber);
	}
	
}
