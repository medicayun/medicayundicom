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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below. 
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

import javax.servlet.http.HttpServletRequest;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;


/**
 * @author Franz Willer franz.willer@gwi-ag.com
 * @version $Id: RIDExport.java 3085 2007-01-22 09:48:41Z gunterze $
 * @since Jan 10, 2007
 */
public class RIDExport {

    private HttpServletRequest request;
    private Dataset dsInfo;
    private String errorMsg;

    public RIDExport(HttpServletRequest request, Dataset dsInfo) {
        this.request = request;
        this.dsInfo = dsInfo;
    }

	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * @return Returns the patId.
	 */
	public String getPatId() {
		return dsInfo.getString(Tags.PatientID);
	}
	
	public String getPatName() {
		return dsInfo.getString(Tags.PatientName);
	}

	/**
	 * @return Returns the instanceUID.
	 */
	public String getObjectUID() {
		return dsInfo.getString(Tags.SOPInstanceUID);
	}
	/**
	 * @return Returns the seriesUID.
	 */
	public String getSeriesUID() {
		return dsInfo.getString(Tags.SeriesInstanceUID);
	}
	/**
	 * @return Returns the studyUID.
	 */
	public String getStudyUID() {
		return dsInfo.getString(Tags.StudyInstanceUID);
	}
	
	/**
	 * @return Returns the errorMsg.
	 */
	public String getErrorMsg() {
		return errorMsg;
	}
	/**
	 * @param errorMsg The errorMsg to set.
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	/**
	 * @return Returns the cuid.
	 */
	public String getSOPClassUID() {
		return dsInfo.getString(Tags.SOPClassUID);
	}
	/**
	 * @param cuid The cuid to set.
	 */
	public void setSOPClassUID(String cuid) {
		dsInfo.putUI(Tags.SOPClassUID, cuid);
	}
	/**
	 * @return Returns the dsInfo.
	 */
	public Dataset getDataset() {
		return dsInfo;
	}
}
