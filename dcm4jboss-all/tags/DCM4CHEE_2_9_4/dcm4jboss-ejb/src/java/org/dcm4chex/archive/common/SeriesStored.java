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
 * Portions created by the Initial Developer are Copyright (C) 2005
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

package org.dcm4chex.archive.common;

import java.io.Serializable;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2628 $ $Date: 2006-07-13 02:05:16 +0800 (周四, 13 7月 2006) $
 * @since Nov 7, 2005
 */
public class SeriesStored implements Serializable {
	
    private static final long serialVersionUID = -5829286003479809840L;
    private String callingAET;
	private String patientID;
	private String patientName;
	private String retrieveAET;
	private String accessionNumber;
	private final Dataset ian;
	
	public SeriesStored(Dataset ian) {
		this.ian = ian;
	}
    
    public String toString() {
        return "SeriesStored[calling=" + callingAET
            + ", suid=" + (ian != null ? ian.getString(Tags.StudyInstanceUID) : null)
            + "]";
    }

	public final Dataset getIAN() {
		return ian;
	}
	
	public final String getCallingAET() {
		return callingAET;
	}

	public final void setCallingAET(String callingAET) {
		this.callingAET = callingAET;
	}

	public final String getRetrieveAET() {
		return retrieveAET;
	}

	public final void setRetrieveAET(String retrieveAET) {
		this.retrieveAET = retrieveAET;
	}

	public final String getPatientID() {
		return patientID;
	}

	public final void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public final String getPatientName() {
		return patientName;
	}

	public final void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public final String getAccessionNumber() {
		return accessionNumber;
	}

	public final void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

}
