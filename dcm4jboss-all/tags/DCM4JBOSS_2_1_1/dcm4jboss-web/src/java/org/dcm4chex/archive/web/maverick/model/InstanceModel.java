/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.model;


import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.common.PrivateTags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1227 $ $Date: 2004-11-27 01:49:22 +0800 (周六, 27 11月 2004) $
 * @since 05.10.2004
 *
 */
public class InstanceModel extends AbstractModel {

    public static Object valueOf(Dataset ds) {
        String cuid = ds.getString(Tags.SOPClassUID);
        if (UIDs.GrayscaleSoftcopyPresentationStateStorage.equals(cuid))
                return new PresentationStateModel(ds);
        if (UIDs.BasicTextSR.equals(cuid) || UIDs.EnhancedSR.equals(cuid)
                || UIDs.ComprehensiveSR.equals(cuid)
                || UIDs.KeyObjectSelectionDocument.equals(cuid))
                return new StructuredReportModel(ds);
        return new ImageModel(ds);
    }

    private final int pk;

    public InstanceModel(Dataset ds) {
        super(ds);
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        this.pk = ds.getInt(PrivateTags.InstancePk, -1);
    }

    public final int getPk() {
        return pk;
    }

    public int hashCode() {
        return pk;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeriesModel)) return false;
        InstanceModel other = (InstanceModel) o;
        return pk == other.pk;
    }

    public final String getContentDateTime() {
        return getDateTime(Tags.ContentDate, Tags.ContentTime);
    }

    public final String getInstanceNumber() {
        return ds.getString(Tags.InstanceNumber);
    }

    public final String getSopCUID() {
        return ds.getString(Tags.SOPClassUID);
    }

    public final String getSopIUID() {
        return ds.getString(Tags.SOPInstanceUID);
    }

    public final String getAvailability() {
        return ds.getString(Tags.InstanceAvailability);
    }

    public final String getRetrieveAETs() {
        return StringUtils.toString(ds.getStrings(Tags.RetrieveAET), '\\');
    }
    
 }