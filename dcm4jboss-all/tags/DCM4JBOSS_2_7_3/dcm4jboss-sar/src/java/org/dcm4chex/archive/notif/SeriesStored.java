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

package org.dcm4chex.archive.notif;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2079 $ $Date: 2005-11-17 01:36:45 +0800 (周四, 17 11月 2005) $
 * @since Nov 7, 2005
 */
public class SeriesStored implements Serializable {
	
	private static final long serialVersionUID = 3905240134780532535L;
	private String callingAET;
	private String calledAET;
	private String patientID;
	private String patientName;
	private String retrieveAET;
	private String accessionNumber;
	private String fsPath;
	private final Dataset ian;
	private final ArrayList fileInfos = new ArrayList();
	
	public SeriesStored() {
		ian = DcmObjectFactory.getInstance().newDataset();
		ian.putSQ(Tags.RefPPSSeq);
		DcmElement sq = ian.putSQ(Tags.RefSeriesSeq);
		sq.addNewItem().putSQ(Tags.RefSOPSeq);
	}

	public final Dataset getInstanceAvailabilityNotification() {
		return ian;
	}
	
	public final String getCalledAET() {
		return calledAET;
	}

	public final void setCalledAET(String calledAET) {
		this.calledAET = calledAET;
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

	public final String getFileSystemPath() {
		return fsPath;
	}

	public final void setFileSystemPath(String fsPath) {
		this.fsPath = fsPath;
	}

	private Dataset getSeries() {
		return ian.getItem(Tags.RefSeriesSeq);
	}

	public String getStudyInstanceUID() {
		return ian.getString(Tags.StudyInstanceUID);
	}

	public void setStudyInstanceUID(String iuid) {
		ian.putUI(Tags.StudyInstanceUID, iuid);
	}

	public String getSeriesInstanceUID() {
		return  getSeries().getString(Tags.SeriesInstanceUID);
	}

	public void setSeriesInstanceUID(String iuid) {
		getSeries().putUI(Tags.SeriesInstanceUID, iuid);
	}

	public int getNumberOfInstances() {
		return getSeries().vm(Tags.RefSOPSeq);
	}

	public String getPPSInstanceUID() {
		return getPPS(Tags.RefSOPInstanceUID);
	}

	public String getPPSClassUID() {
		return getPPS(Tags.RefSOPClassUID);
	}

	private String getPPS(int tag) {
		Dataset pps = ian.getItem(Tags.RefPPSSeq);
		return pps != null ? pps.getString(tag) : null;
	}

	public void setRefPPS(String instanceUID, String classUID) {
		DcmElement sq = ian.putSQ(Tags.RefPPSSeq);
		Dataset item = sq.addNewItem();
		item.putUI(Tags.RefSOPInstanceUID, instanceUID);
		item.putUI(Tags.RefSOPClassUID, classUID);
		item.putSQ(Tags.PerformedWorkitemCodeSeq);
	}

	public DcmElement getRefSOPSeq() {
		return getSeries().get(Tags.RefSOPSeq);
	}
	
	public void addFileInfo(FileInfo fileInfo) {
		Dataset item = getRefSOPSeq().addNewItem();
		item.putAE(Tags.RetrieveAET, retrieveAET);
		item.putCS(Tags.InstanceAvailability, "ONLINE");
		item.putUI(Tags.RefSOPInstanceUID, fileInfo.getSOPInstanceUID());
		item.putUI(Tags.RefSOPClassUID, fileInfo.getSOPClassUID());
		fileInfos.add(fileInfo);
	}
	
	public List getFileInfos() {
		return Collections.unmodifiableList(fileInfos);
	}
	
	
}
