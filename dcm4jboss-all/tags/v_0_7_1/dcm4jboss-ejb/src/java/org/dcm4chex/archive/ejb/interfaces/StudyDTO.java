/* $Id: StudyDTO.java 1025 2004-03-02 23:55:32Z gunterze $
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
 * @version $Revision: 1025 $ $Date: 2004-03-03 07:55:32 +0800 (周三, 03 3月 2004) $
 * @since 14.01.2004
 */
public class StudyDTO implements Serializable {

    public static final String DATETIME_FORMAT = "yyyy/MM/dd hh:mm";

    private int pk;
    private String accessionNumber;
    private String studyID;
    private String studyIUID;
    private String studyDateTime;
    private String studyDescription;
    private String modalitiesInStudy;
    private int numberOfSeries;
    private int numberOfInstances;
    private String retrieveAETs;
    private int availability;
    private List series = new ArrayList();

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
    public final String getModalitiesInStudy() {
        return modalitiesInStudy;
    }

    /**
     * @param modalitiesInStudy
     */
    public final void setModalitiesInStudy(String modalitiesInStudy) {
        this.modalitiesInStudy = modalitiesInStudy;
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
    public final List getSeries() {
        return series;
    }

    /**
     * @param series
     */
    public final void setSeries(List series) {
        this.series = series;
    }

    /**
     * @return
     */
    public final String getStudyDateTime() {
        return studyDateTime;
    }

    /**
     * @param studyDateTime
     */
    public final void setStudyDateTime(String studyDateTime) {
        this.studyDateTime = studyDateTime;
    }

    /**
     * @return
     */
    public final String getStudyDescription() {
        return studyDescription;
    }

    /**
     * @param studyDescription
     */
    public final void setStudyDescription(String studyDescription) {
        this.studyDescription = studyDescription;
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

    /**
     * @return
     */
    public final String getStudyIUID() {
        return studyIUID;
    }

    /**
     * @param studyIUID
     */
    public final void setStudyIUID(String studyIUID) {
        this.studyIUID = studyIUID;
    }

    /**
     * @return
     */
    public final int getNumberOfInstances() {
        return numberOfInstances;
    }

    /**
     * @param numberOfInstances
     */
    public final void setNumberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    /**
     * @return
     */
    public final int getNumberOfSeries() {
        return numberOfSeries;
    }

    /**
     * @param numberOfSeries
     */
    public final void setNumberOfSeries(int numberOfSeries) {
        this.numberOfSeries = numberOfSeries;
    }

    /**
     * @return
     */
    public String getRetrieveAETs() {
        return retrieveAETs;
    }

    /**
     * @param retrieveAETs
     */
    public void setRetrieveAETs(String retrieveAETs) {
        this.retrieveAETs = retrieveAETs;
    }

    /**
     * @return
     */
    public final int getAvailability() {
        return availability;
    }

    /**
     * @param availability
     */
    public final void setAvailability(int availability) {
        this.availability = availability;
    }
}
