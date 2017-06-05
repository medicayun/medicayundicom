/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.model;

import java.util.ArrayList;
import java.util.List;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;

import org.dcm4chex.archive.common.PrivateTags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1181 $ $Date: 2004-10-15 07:08:49 +0800 (周五, 15 10月 2004) $
 * @since 05.10.2004
 *
 */
public class StudyModel extends AbstractModel {

    private List series = new ArrayList();

    private int pk;

    public StudyModel() {
    }

    public StudyModel(Dataset ds) {
        super(ds);
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        this.pk = ds.getInt(PrivateTags.StudyPk, -1);
    }

    public final int getPk() {
        return pk;
    }

    public final void setPk(int pk) {
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        ds.putUL(PrivateTags.StudyPk, pk);
        this.pk = pk;
    }

    public int hashCode() {
        return pk;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudyModel)) return false;
        StudyModel other = (StudyModel) o;
        return pk == other.pk;
    }

    public final String getPlacerOrderNumber() {
        return ds.getString(Tags.PlacerOrderNumber);
    }

    public final void setPlacerOrderNumber(String s) {
        ds.putSH(Tags.PlacerOrderNumber, s);
    }

    public final String getFillerOrderNumber() {
        return ds.getString(Tags.FillerOrderNumber);
    }

    public final void setFillerOrderNumber(String s) {
        ds.putSH(Tags.FillerOrderNumber, s);
    }

    public final String getAccessionNumber() {
        return ds.getString(Tags.AccessionNumber);
    }

    public final void setAccessionNumber(String s) {
        ds.putSH(Tags.AccessionNumber, s);
    }

    public final String getReferringPhysician() {
        return ds.getString(Tags.ReferringPhysicianName);
    }

    public final void setReferringPhysician(String s) {
        ds.putPN(Tags.ReferringPhysicianName, s);
    }

    public final String getStudyDateTime() {
        return getDateTime(Tags.StudyDate, Tags.StudyTime);
    }

    public final void setStudyDateTime(String s) {
        setDateTime(Tags.StudyDate, Tags.StudyTime, s);
    }

    public final String getStudyDescription() {
        return ds.getString(Tags.StudyDescription);
    }

    public final void setStudyDescription(String s) {
        ds.putLO(Tags.StudyDescription, s);
    }

    public final String getStudyID() {
        return ds.getString(Tags.StudyID);
    }

    public final void setStudyID(String s) {
        ds.putSH(Tags.StudyID, s);
    }

    public final String getStudyIUID() {
        return ds.getString(Tags.StudyInstanceUID);
    }

    public final void setStudyIUID(String s) {
        ds.putUI(Tags.StudyDescription, s);
    }

    public final String getModalitiesInStudy() {
        return StringUtils
                .toString(ds.getStrings(Tags.ModalitiesInStudy), '\\');
    }

    public final int getNumberOfInstances() {
        return ds.getInt(Tags.NumberOfStudyRelatedInstances, 0);
    }

    public final int getNumberOfSeries() {
        return ds.getInt(Tags.NumberOfStudyRelatedSeries, 0);
    }

    public final String getAvailability() {
        return ds.getString(Tags.InstanceAvailability);
    }

    public final String getRetrieveAETs() {
        return StringUtils.toString(ds.getStrings(Tags.RetrieveAET), '\\');
    }

    public final List getSeries() {
        return series;
    }

    public final void setSeries(List series) {
        this.series = series;
    }
}