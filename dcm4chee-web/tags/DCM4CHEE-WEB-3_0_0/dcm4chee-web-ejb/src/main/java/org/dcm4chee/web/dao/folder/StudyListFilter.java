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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.web.dao.folder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 30, 2008
 */
public class StudyListFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    private String patientName;
    private String patientID;
    private String issuerOfPatientID;
    private boolean extendedQuery;
    private Date birthDateMin;
    private Date birthDateMax;
    private String accessionNumber;
    private Date studyDateMin;
    private Date studyDateMax;
    private String studyInstanceUID;
    private String modality;
    private String sourceAET;
    private Map<String,List<String>> sourceAetGroups;
    private String seriesInstanceUID;
    private boolean patientQuery;
    private boolean latestStudiesFirst;
    private boolean ppsWithoutMwl;

    public StudyListFilter(Map<String, List<String>> sourceAetGroups) {
        this.sourceAetGroups = sourceAetGroups;
        clear();
    }

    public void clear() {
        patientName = patientID = issuerOfPatientID = accessionNumber = 
            studyInstanceUID = modality = sourceAET = seriesInstanceUID = null;
        studyDateMin = studyDateMax = null; 
        birthDateMin = birthDateMax = null;
        extendedQuery = false;
        latestStudiesFirst = false;
        ppsWithoutMwl = false;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getIssuerOfPatientID() {
        return issuerOfPatientID;
    }

    public void setIssuerOfPatientID(String issuerOfPatientID) {
        this.issuerOfPatientID = issuerOfPatientID;
    }

    public boolean isExtendedQuery() {
        return extendedQuery;
    }

    public void setExtendedQuery(boolean extendedQuery) {
        this.extendedQuery = extendedQuery;
    }

    public Date getBirthDateMin() {
        return birthDateMin;
    }

    public void setBirthDateMin(Date birthdateMin) {
        this.birthDateMin = birthdateMin;
    }

    public Date getBirthDateMax() {
        return birthDateMax;
    }

    public void setBirthDateMax(Date birthdateMax) {
        this.birthDateMax = birthdateMax;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public boolean isPpsWithoutMwl() {
        return ppsWithoutMwl;
    }

    public void setPpsWithoutMwl(boolean ppsWithoutMwl) {
        this.ppsWithoutMwl = ppsWithoutMwl;
    }

    public Date getStudyDateMin() {
        return studyDateMin;
    }

    public void setStudyDateMin(Date studyDate) {
        this.studyDateMin = studyDate;
    }

    public Date getStudyDateMax() {
        return studyDateMax;
    }

    public void setStudyDateMax(Date studyDate) {
        this.studyDateMax = studyDate;
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getSourceAET() {
        return sourceAET;
    }

    public void setSourceAET(String sourceAET) {
        this.sourceAET = sourceAET;
    }

    public String[] getSourceAETs() {
        if (sourceAetGroups != null) {
            List<String> l = sourceAetGroups.get(sourceAET);
            if (l != null) {
                return l.toArray(new String[l.size()]);
            }
        }
        return new String[]{sourceAET};
    }
    
    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public void setSesriesInstanceUID(String seriesInstanceUID) {
        this.seriesInstanceUID = seriesInstanceUID;
    }
    
    public boolean isPatientQuery() {
        return patientQuery;
    }

    public void setPatientQuery(boolean patQuery) {
        this.patientQuery = patQuery;
    }

    public boolean isLatestStudiesFirst() {
        return latestStudiesFirst;
    }

    public void setLatestStudiesFirst(boolean latestStudiesFirst) {
        this.latestStudiesFirst = latestStudiesFirst;
    }
}
