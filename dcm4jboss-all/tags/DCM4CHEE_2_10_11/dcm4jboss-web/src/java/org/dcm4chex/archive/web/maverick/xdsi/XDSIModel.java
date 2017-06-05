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

package org.dcm4chex.archive.web.maverick.xdsi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4chex.archive.web.maverick.BasicFormModel;
import org.dcm4chex.archive.web.maverick.util.CodeItem;
import org.dcm4chex.archive.xdsi.XDSIService;

/**
 * @author franz.willer
 *
 * The Model for Teaching File Selector WEB interface.
 */
public class XDSIModel extends BasicFormModel {

	/** The session attribute name to store the model in http session. */
	public static final String XDSI_ATTR_NAME = "xdsiModel";
	
    private static Logger log = Logger.getLogger( XDSIModel.class.getName() );

	private String user;
	
	private Set instances;
	
	private boolean exportPDF;
	
	private int selectedTitle, selectedEventCode, removeEventCode, selectedClassCode, selectedAuthorRole,
				selectedContentTypeCode, selectedHealthCareTypeCode;

	private CodeItem[] docTitleCodes;
	private CodeItem[] classCodes;
	private CodeItem[] eventCodes;
	private CodeItem[] authorRoles;
	private CodeItem[] confidentialityCodes;
	private CodeItem[] contentTypeCodes;
	private CodeItem[] healthCareFacilityTypeCodes;
	
	private List selectedEventCodes = new ArrayList();
	
	private Properties props = new Properties();

	private XDSIExportDelegate delegate;

	/**
	 * Creates the model.
	 * <p>
	 */
	private XDSIModel(String user, HttpServletRequest request) {
		super(request);
		this.user = user;
	}
	
	public void setMetadataProperties(Properties props) {
		if ( props == null ) return;
		this.props = props;
	}
	
	/**
	 * @return Returns the props.
	 */
	public Properties listMetadataProperties() {
		return props;
	}
	
	/**
	 * @return Returns the user.
	 */
	public String getUser() {
		return user;
	}
	
	/**
	 * Get the model for an http request.
	 * <p>
	 * Look in the session for an associated model via <code>TF_ATTR_NAME</code><br>
	 * If there is no model stored in session (first request) a new model is created and stored in session.
	 * 
	 * @param request A http request.
	 * 
	 * @return The model for given request.
	 */
	public static final XDSIModel getModel( HttpServletRequest request ) {
		XDSIModel model = (XDSIModel) request.getSession().getAttribute(XDSI_ATTR_NAME);
		if (model == null) {
				model = new XDSIModel(request.getUserPrincipal().getName(), request);
				request.getSession().setAttribute(XDSI_ATTR_NAME, model);
				model.setErrorCode( NO_ERROR ); //reset error code
		}
		return model;
	}

	public String getModelName() { return "XDSI"; }
	
	public CodeItem[] getDocTitles() {
		return docTitleCodes;
	}
	
	public void setDocTitles( CodeItem[] codes ) {
		docTitleCodes = codes;
	}
	
	/**
	 * @return Returns the instances.
	 */
	public Set getInstances() {
		return instances;
	}
	/**
	 * @param instances The instances to set.
	 */
	public void setInstances(Set instances) {
		this.instances = instances;
	}
	
	public int getNumberOfInstances() {
		return instances == null ? 0 : instances.size();
	}
	

	/**
	 * @return Returns the allowPDF.
	 */
	public boolean isPdfExport() {
		return exportPDF;
	}
	
	public void setPdfExport(boolean b) {
		exportPDF = b;
	}
	
	public void setSelectedAuthorRole(int selected) {
		if ( authorRoles == null || authorRoles.length <= selected) return;
		this.selectedAuthorRole = selected;
		CodeItem ci = authorRoles[selectedAuthorRole];
		props.setProperty(XDSIService.AUTHOR_ROLE, ci.getCodeValue());
		props.setProperty(XDSIService.AUTHOR_ROLE_DISPLAYNAME, ci.getCodeMeaning());
	}
	public int getSelectedAuthorRole() {
		return selectedAuthorRole;
	}
	public CodeItem selectedAuthorRole() {
		return selectedAuthorRole < 0 ? null : authorRoles[selectedAuthorRole];
	}
	
	/**
	 * @param selectedTitle The selectedTitle to set.
	 */
	public void setSelectedDocTitle(int selected) {
		this.selectedTitle = selected;
		if ( docTitleCodes != null ) 
			props.setProperty("docTitle", ""+selectedDocTitle());
	}
	public int getSelectedDocTitle() {
		return selectedTitle;
	}
	public CodeItem selectedDocTitle() {
		return selectedTitle < 0 ? null : docTitleCodes[selectedTitle];
	}

	public void setSelectedClassCode(int selected) {
		if ( classCodes == null || classCodes.length <= selected) return;
		this.selectedClassCode = selected;
		CodeItem ci = classCodes[selectedClassCode];
		props.setProperty("classCode", ci.getCodeValue());
		props.setProperty("classCodeDisplayName", ci.getCodeMeaning());
	}
	public int getSelectedClassCode() {
		return selectedClassCode;
	}
	public CodeItem selectedClassCode() {
		return selectedClassCode < 0 ? null : classCodes[selectedClassCode];
	}

	public void setSelectedContentTypeCode(int selected) {
		if ( contentTypeCodes == null || contentTypeCodes.length <= selected) return;
		this.selectedContentTypeCode = selected;
		CodeItem ci = contentTypeCodes[selectedContentTypeCode];
		props.setProperty("contentTypeCode", ci.getCodeValue());
		props.setProperty("contentTypeCodeDN", ci.getCodeMeaning());
	}
	public int getSelectedContentTypeCode() {
		return selectedContentTypeCode;
	}

	public void setSelectedHealthCareTypeCode(int selected) {
		if ( healthCareFacilityTypeCodes == null || healthCareFacilityTypeCodes.length <= selected) return;
		this.selectedHealthCareTypeCode = selected;
		CodeItem ci = healthCareFacilityTypeCodes[selectedHealthCareTypeCode];
		props.setProperty("healthCareFacilityTypeCode", ci.getCodeValue());
		props.setProperty("healthCareFacilityTypeCodeDN", ci.getCodeMeaning());
	}
	public int getSelectedHealthCareTypeCode() {
		return selectedHealthCareTypeCode;
	}
	
	public void setSelectedEventCode(int selected) {
		if ( eventCodes == null || eventCodes.length <= selected) return;
		selectedEventCode = selected;
	}
	public void addSelectedEventCode() {
		CodeItem ci = eventCodes[selectedEventCode];
		selectedEventCodes.add(ci);
		props.setProperty("eventCodeList", getEventCodeListString());
	}
	private String getEventCodeListString() {
		StringBuffer sb = new StringBuffer();
		for( Iterator iter = selectedEventCodes.iterator() ; iter.hasNext() ; ) {
			sb.append(iter.next()).append("|");
		}
		return sb.toString();
	}
	public int getSelectedEventCode() {
		return selectedEventCode;
	}

	public void setRemoveEventCode(int selected) {
		if ( selectedEventCodes == null || selectedEventCodes.size() <= selected) return;
		removeEventCode = selected;
	}
	public void removeSelectedEventCode() {
		if ( selectedEventCodes == null || selectedEventCodes.size() <= removeEventCode) return;
		selectedEventCodes.remove(removeEventCode);
		props.setProperty("eventCodeList", getEventCodeListString());
	}
	public int getRemoveEventCode() {
		return removeEventCode;
	}
	public void deselectAllEventCodes() {
		selectedEventCodes.clear();
		props.setProperty("eventCodeList", getEventCodeListString());
	}
	
/*************************************************************************
 * 
 */	
	public void setAuthorPerson( String s ) {
		props.setProperty(XDSIService.AUTHOR_PERSON, s);
	}
	public String getAuthorPerson() {
		return props.getProperty(XDSIService.AUTHOR_PERSON);
	}

	public void setAuthorSpeciality( String s ) {
		props.setProperty(XDSIService.AUTHOR_SPECIALITY, s);
	}
	public String getAuthorSpeciality() {
		return props.getProperty(XDSIService.AUTHOR_SPECIALITY);
	}
	
	public void setAuthorInstitution( String s ) {
		props.setProperty(XDSIService.AUTHOR_INSTITUTION, s);
	}
	public String getAuthorInstitution() {
		return props.getProperty(XDSIService.AUTHOR_INSTITUTION);
	}

// List of Codes
	public CodeItem[] getAuthorRoles() {
		return authorRoles;
	}
	public void setAuthorRoles(CodeItem[] roles) {
		if ( roles == null || roles.length < 1 ) {
			log.warn("AuthorRoles must not be empty! Please check AuthorRoleList configuration in XDSIService! XDSI Export is NOT IHE compliant");
		}
		authorRoles = roles;
	}

	/**
	 * @return Returns the classCodes.
	 */
	public CodeItem[] getClassCodes() {
		return classCodes;
	}
	/**
	 * @param classCodes The classCodes to set.
	 */
	public void setClassCodes(CodeItem[] classCodes) {
		this.classCodes = classCodes;
	}
	/**
	 * @return Returns the contentTypeCodes.
	 */
	public CodeItem[] getContentTypeCodes() {
		return contentTypeCodes;
	}
	/**
	 * @param codes The contentTypeCodes to set.
	 */
	public void setContentTypeCodes(CodeItem[] codes) {
		this.contentTypeCodes = codes;
	}
	/**
	 * @return Returns the healthCareFacilityTypeCodes.
	 */
	public CodeItem[] getHealthCareFacilityTypeCodes() {
		return healthCareFacilityTypeCodes;
	}
	/**
	 * @param codes The healthCareFacilityTypeCodes to set.
	 */
	public void setHealthCareFacilityTypeCodes(CodeItem[] codes) {
		this.healthCareFacilityTypeCodes = codes;
	}
	/**
	 * @return Returns the eventCodes.
	 */
	public CodeItem[] getEventCodes() {
		return eventCodes;
	}
	/**
	 * @param eventCodes The eventCodes to set.
	 */
	public void setEventCodes(CodeItem[] eventCodes) {
		this.eventCodes = eventCodes;
	}
	
	public List getSelectedEventCodes() {
		return selectedEventCodes;
	}
	/**
	 * @return Returns the eventCodes.
	 */
	public CodeItem[] getConfidentialityCodes() {
		return confidentialityCodes;
	}
	/**
	 * @param eventCodes The eventCodes to set.
	 */
	public void setConfiguredConfidentialityCodes(CodeItem[] codes) {
		this.confidentialityCodes = codes;
	}
	/**
	 * 
	 */
	public void clear() {
		setSelectedDocTitle(0);
		setSelectedEventCode(0);
		setSelectedClassCode(0);
		setSelectedAuthorRole(0);
		setSelectedContentTypeCode(0);
		setSelectedHealthCareTypeCode(0);
		this.deselectAllEventCodes();
        this.exportPDF=false;
	}

}
