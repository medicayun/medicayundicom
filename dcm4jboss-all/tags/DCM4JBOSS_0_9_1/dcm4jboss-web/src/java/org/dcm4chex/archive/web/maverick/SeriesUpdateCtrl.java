/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.SeriesModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
 * @since 5.10.2004
 *
 */
public class SeriesUpdateCtrl extends Dcm4JbossController {

    private int patPk;

    private int studyPk;
    
    private int seriesPk;

    private String bodyPartExamined;

    private String laterality;

    private String manufacturer;

    private String manufacturerModelName;

    private String modality;

    private String seriesDateTime;

    private String seriesDescription;

    private String seriesNumber;

    private String submit = null;

    private String cancel = null;

    public final void setPatPk(int patPk) {
        this.patPk = patPk;
    }

    public final void setStudyPk(int studyPk) {
        this.studyPk = studyPk;
    }

    public final void setSeriesPk(int seriesPk) {
        this.seriesPk = seriesPk;
    }
    
    public final void setBodyPartExamined(String s) {
        this.bodyPartExamined = s;
    }

    public final void setLaterality(String s) {
        this.laterality = s;
    }

    public final void setManufacturer(String s) {
        this.manufacturer = s;
    }

    public final void setManufacturerModelName(String s) {
        this.manufacturerModelName = s;
    }

    public final void setModality(String s) {
        this.modality = s;
    }

    public final void setSeriesDateTime(String s) {
        this.seriesDateTime = s;
    }

    public final void setSeriesDescription(String s) {
        this.seriesDescription = s;
    }

    public final void setSeriesNumber(String s) {
        this.seriesNumber = s;
    }
    
    protected String perform() throws Exception {
        if (submit != null)
            executeUpdate();
        return SUCCESS;
    }

    private ContentEdit lookupContentEdit() throws Exception {
        ContentEditHome home = (ContentEditHome) EJBHomeFactory.getFactory()
                .lookup(ContentEditHome.class, ContentEditHome.JNDI_NAME);
        return home.create();
    }

    private void executeUpdate() {
        try {
            SeriesModel series = FolderForm.getFolderForm(getCtx().getRequest())
                    .getSeriesByPk(patPk, studyPk, seriesPk);
            //updating data model
            series.setBodyPartExamined(bodyPartExamined);
            series.setLaterality(laterality);
            series.setModality(modality);
            series.setSeriesDateTime(seriesDateTime);
            series.setSeriesDescription(seriesDescription);
            series.setSeriesNumber(seriesNumber);
            ContentEdit ce = lookupContentEdit();
            ce.updateSeries(series.toDataset());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public final void setSubmit(String update) {
        this.submit = update;
    }

    public final void setCancel(String cancel) {
        this.cancel = cancel;
    }
}