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

package org.dcm4chex.archive.web.maverick;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.web.maverick.model.StudyFilterModel;
import org.infohazard.maverick.flow.ControllerContext;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2565 $ $Date: 2006-06-27 18:51:01 +0800 (周二, 27 6月 2006) $
 * @since 28.01.2004
 */
public class FolderForm extends BasicFolderForm {

    static final String FOLDER_ATTRNAME = "folderFrom";

    /** Error code: General move Error. */
    public static final String ERROR_MOVE ="moveError";
    /** Error code: nothing is selected. */
    public static final String ERROR_MOVE_NO_SELECTION ="moveError_noSelection";
    /** Error code: More than one destination is selected. */
    public static final String ERROR_MOVE_TO_MANY_DEST ="moveError_toManyDest";
    /** Error code: No source is selected. */
    public static final String ERROR_MOVE_NO_SOURCE ="moveError_noSource";
    /** Error code: series and/or instances are selected. */
    public static final String ERROR_MOVE_UNSELECT_SERIES ="moveError_unselectSeries";
    /** Error code: series and/or instances are selected. */
    public static final String ERROR_MOVE_UNSELECT_INSTANCES ="moveError_unselectInstances";
    /** Error code: move studies to the same patient is not usefull. */
    public static final String ERROR_MOVE_SAME_PATIENT ="moveError_samePatient";
    /** Error code: move series to the same study is not usefull. */
    public static final String ERROR_MOVE_SAME_STUDY ="moveError_sameStudy";
    /** Error code: move instances to the same series is not usefull. */
    public static final String ERROR_MOVE_SAME_SERIES ="moveError_sameSeries";
    /** Error code: move series is only allowed between studies of the same patient. */
    public static final String ERROR_MOVE_DIFF_PATIENT ="moveError_diffPatient";
    /** Error code: move instances is only allowed between series of the same study. */
    public static final String ERROR_MOVE_DIFF_STUDY ="moveError_diffStudy";
    /** Error code: You can only move studies from one patient to another patient. */
    public static final String ERROR_MOVE_DIFF_STUDY_PARENT ="moveError_diffStudyParent";
    /** Error code: You can only move series from one study to another study. */
    public static final String ERROR_MOVE_DIFF_SERIES_PARENT ="moveError_diffSeriesParent";
    /** Error code: You can only move instances from one series to another series. */
    public static final String ERROR_MOVE_DIFF_INSTANCE_PARENT ="moveError_diffInstanceParent";

    private String patientID;

    private String patientName;

    private String accessionNumber;

    private String studyID;

    private String studyUID;

    private String studyDateRange;

    private String modality;

    private StudyFilterModel studyFilter = null;

    private List aets;

    private String destination;

    private boolean webViewer;
    
    /** Base URL for WADO service. Used for image view */
    private String wadoBaseURL;

	private boolean showStudyIUID;

	private boolean showSeriesIUID;
	
	private boolean filterAET = false;
	
	protected static Logger log = Logger.getLogger(FolderForm.class);
    
    static FolderForm getFolderForm(ControllerContext ctx) {
    	HttpServletRequest request = ctx.getRequest();
        FolderForm form = (FolderForm) request.getSession()
                .getAttribute(FOLDER_ATTRNAME);
        if (form == null) {
            form = new FolderForm(request);
            try {
            	String wadoBase = ctx.getServletConfig().getInitParameter("wadoBaseURL");
            	URL wadoURL = null;
            	if ( wadoBase != null ) {
            		try {
            			wadoURL = new URL(wadoBase);
            		} catch (MalformedURLException x){
            			log.warn("Invalid Servlet Init Parameter wadoBaseURL:"+wadoBase+"! Ignored");
            		}
            	}
            	if ( wadoURL == null ) {
            		wadoURL = new URL( request.isSecure() ? "https" : "http", request.getServerName(),
						request.getServerPort(), "/");
            	}
				form.setWadoBaseURL( wadoURL.toString() );
				URL url = new URL( "http", request.getServerName(), 
						request.getServerPort(), "/WebViewer/jvapplet.jar");
				try {
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.connect();
					if ( conn.getResponseCode() == HttpURLConnection.HTTP_OK )
						form.enableWebViewer();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            request.getSession().setAttribute(FOLDER_ATTRNAME, form);
            try {
                int limit = Integer.parseInt( ctx.getServletConfig().getInitParameter("limitNrOfStudies") );
            	if ( limit > 0 ) {
            		form.setLimit( limit );
            	} else {
            		log.warn("Wrong servlet ini parameter 'limitNrOfStudies' ! Must be greater 0! Ignored");
            	}
            } catch (Exception x) {
        		log.warn("Wrong servlet ini parameter 'limitNrOfStudies' ! Must be an integer greater 0! Ignored");
            }
        }
        form.setErrorCode( NO_ERROR ); //reset error code
		form.setPopupMsg(null);
        
        return form;
    }
    
    /**
	 * 
	 */
	private void enableWebViewer() {
		this.webViewer = true;
		
	}

	private FolderForm( HttpServletRequest request ) {
    	super(request);
    }
	
	public String getModelName() { return "FOLDER"; }

	/**
	 * @return Returns the webViewer.
	 */
	public boolean isWebViewer() {
		return webViewer;
	}

	/**
	 * @return Returns the wadoBaseURL.
	 */
	public String getWadoBaseURL() {
		return wadoBaseURL;
	}
	/**
	 * @param wadoBaseURL The wadoBaseURL to set.
	 */
	public void setWadoBaseURL(String wadoBaseURL) {
		this.wadoBaseURL = wadoBaseURL;
	}
	
    public final String getAccessionNumber() {
        return accessionNumber;
    }

    public final void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public final String getModality() {
        return modality;
    }

    public final void setModality(String modality) {
        this.modality = modality;
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

    public final String getStudyDateRange() {
        return studyDateRange;
    }

    public final void setStudyDateRange(String studyDateRange) {
        this.studyDateRange = studyDateRange;
    }

    public final String getStudyID() {
        return studyID;
    }

    public final void setStudyID(String studyID) {
        this.studyID = studyID;
    }

	/**
	 * @return Returns the studyUID.
	 */
	public String getStudyUID() {
		return studyUID;
	}
	/**
	 * @param studyUID The studyUID to set.
	 */
	public void setStudyUID(String studyUID) {
		this.studyUID = studyUID;
	}

    public final List getAets() {
        return aets;
    }

    public final void setAets(List aets) {
        this.aets = aets;
    }

    public final String getDestination() {
        return destination;
    }

    public final void setDestination(String destination) {
        this.destination = destination;
    }

    public final void setFilter(String filter) {
        resetOffset();
        studyFilter = null;
    }

    public final StudyFilterModel getStudyFilter() {
        if (studyFilter == null) {
            studyFilter = new StudyFilterModel();
            studyFilter.setPatientID(patientID);
            studyFilter.setPatientName(patientName);
            studyFilter.setAccessionNumber(accessionNumber);
            studyFilter.setStudyID(studyID);
            studyFilter.setStudyUID( studyUID );
            studyFilter.setStudyDateRange(studyDateRange);
            studyFilter.setModality(modality);
            studyFilter.setCallingAET(this.filterAET ? destination:null);
        }
        return studyFilter;
    }

	/**
	 * @param b
	 */
	public void setShowStudyIUID(boolean b) {
		showStudyIUID = b;
		
	}
	/**
	 * @return Returns the showStudyIUID.
	 */
	public boolean isShowStudyIUID() {
		return showStudyIUID;
	}
	/**
	 * @param b
	 */
	public void setShowSeriesIUID(boolean b) {
		showSeriesIUID = b;
		
	}
	/**
	 * @return Returns the showStudyIUID.
	 */
	public boolean isShowSeriesIUID() {
		return showSeriesIUID;
	}


	/**
	 * @return Returns the filterAET.
	 */
	public boolean isFilterAET() {
		return filterAET;
	}
	/**
	 * @param filterAET The filterAET to set.
	 */
	public void setFilterAET(boolean filterAET) {
		this.filterAET = filterAET;
	}
	/* (non-Javadoc)
	 * @see org.dcm4chex.archive.web.maverick.BasicFormPagingModel#gotoCurrentPage()
	 */
	public void gotoCurrentPage() {
		//We doesnt need this method here. FolderSubmitCtrl does not use performPrevious/performNext!
	}

	/**
	 * @param ctx
	 * @param string
	 */
	public static void setExternalPopupMsg(ControllerContext ctx, String msg) {
		getFolderForm(ctx).setExternalPopupMsg(msg);
		
	}

}