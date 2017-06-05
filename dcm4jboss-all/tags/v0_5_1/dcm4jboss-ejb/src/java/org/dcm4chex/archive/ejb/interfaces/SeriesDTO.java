/* $Id: SeriesDTO.java 945 2004-02-01 14:26:22Z gunterze $
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
 * @version $Revision: 945 $ $Date: 2004-02-01 22:26:22 +0800 (周日, 01 2月 2004) $
 * @since 14.01.2004
 */
public class SeriesDTO implements Serializable {

    public static final String DATETIME_FORMAT = "yyyy/MM/dd hh:mm";

    private int pk;
    private String seriesNumber;
    private String seriesIUID;
    private String modality;
    private String seriesDescription;
    private String seriesDateTime;
    private String bodyPartExamined;
    private String laterality;
    private int numberOfInstances;
    private String retrieveAETs;
    private List instances = new ArrayList();
    
    /**
     * @return
     */
    public final String getLaterality() {
        return laterality;
    }

    /**
     * @param laterality
     */
    public final void setLaterality(String laterality) {
        this.laterality = laterality;
    }

    /**
     * @return
     */
    public final String getSeriesDateTime() {
        return seriesDateTime;
    }

    /**
     * @param seriesDateTime
     */
    public final void setSeriesDateTime(String seriesDateTime) {
        this.seriesDateTime = seriesDateTime;
    }

    /**
     * @return
     */
    public final List getInstances() {
        return instances;
    }

    /**
     * @param instances
     */
    public final void setInstances(List instances) {
        this.instances = instances;
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
    public final String getBodyPartExamined() {
        return bodyPartExamined;
    }

    /**
     * @param bodyPartExamined
     */
    public final void setBodyPartExamined(String bodyPartExamined) {
        this.bodyPartExamined = bodyPartExamined;
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
    public final String getSeriesDescription() {
        return seriesDescription;
    }

    /**
     * @param seriesDescription
     */
    public final void setSeriesDescription(String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }

    /**
     * @return
     */
    public final String getSeriesIUID() {
        return seriesIUID;
    }

    /**
     * @param seriesIUID
     */
    public final void setSeriesIUID(String seriesIUID) {
        this.seriesIUID = seriesIUID;
    }

    /**
     * @return
     */
    public final String getSeriesNumber() {
        return seriesNumber;
    }

    /**
     * @param seriesNumber
     */
    public final void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
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
}
