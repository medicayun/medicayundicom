/* $Id: StudyFilterDTO.java 933 2004-01-21 23:25:59Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.dcm4chex.archive.ejb.interfaces;

import java.io.Serializable;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 933 $ $Date: 2004-01-22 07:25:59 +0800 (周四, 22 1月 2004) $
 * @since 14.01.2004
 */
public class StudyFilterDTO implements Serializable {

    private String patientID;
    private String patientName;
    private String accessionNumber;
    private String studyID;
    private String studyDateRange;
    private String modality;
    /**
     * @return
     */
    public final String getAccessionNumber() {
        return accessionNumber;
    }

    /**
     * @param accessionNumber
     */
    public final void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    /**
     * @return
     */
    public final String getModality() {
        return modality;
    }

    /**
     * @param modality
     */
    public final void setModality(String modality) {
        this.modality = modality;
    }

    /**
     * @return
     */
    public final String getPatientID() {
        return patientID;
    }

    /**
     * @param patientID
     */
    public final void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    /**
     * @return
     */
    public final String getPatientName() {
        return patientName;
    }

    /**
     * @param patientName
     */
    public final void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    /**
     * @return
     */
    public final String getStudyDateRange() {
        return studyDateRange;
    }

    /**
     * @param studyDateTime
     */
    public final void setStudyDateRange(String studyDateRange) {
        this.studyDateRange = studyDateRange;
    }

    /**
     * @return
     */
    public final String getStudyID() {
        return studyID;
    }

    /**
     * @param studyID
     */
    public final void setStudyID(String studyID) {
        this.studyID = studyID;
    }
}
