/* $Id: PatientDTO.java 937 2004-01-26 21:26:25Z gunterze $
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
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 937 $ $Date: 2004-01-27 05:26:25 +0800 (周二, 27 1月 2004) $
 * @since 14.01.2004
 */
public class PatientDTO implements Serializable {

    public static final String DATE_FORMAT = "yyyy/MM/dd";
    private int pk;
    private String patientID;
    private String patientName;
    private String patientSex;
    private String patientBirthDate;
    private List studies = new ArrayList();
    
    /**
     * @return
     */
    public final String getPatientBirthDate() {
        return patientBirthDate;
    }

    /**
     * @param patientBirthDate
     */
    public final void setPatientBirthDate(String patientBirthDate) {
        this.patientBirthDate = patientBirthDate;
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
    public final String getPatientSex() {
        return patientSex;
    }

    /**
     * @param patientSex
     */
    public final void setPatientSex(String patientSex) {
        this.patientSex = patientSex;
    }

    /**
     * @return
     */
    public final int getPk() {
        return pk;
    }

    /**
     * @param pk
     */
    public final void setPk(int pk) {
        this.pk = pk;
    }

    /**
     * @return
     */
    public final List getStudies() {
        return studies;
    }

    /**
     * @param studies
     */
    public final void setStudies(List studies) {
        this.studies = studies;
    }

}
