/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import org.dcm4chex.archive.web.maverick.model.SeriesModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
 * @since 7.10.2004
 *
 */
public class SeriesEditCtrl extends Dcm4JbossController {

    private int patPk;

    private int studyPk;

    private int seriesPk;

    public final int getPatPk() {
        return patPk;
    }

    public final void setPatPk(int pk) {
        this.patPk = pk;
    }

    public final int getStudyPk() {
        return studyPk;
    }

    public final void setStudyPk(int pk) {
        this.studyPk = pk;
    }

    public final int getSeriesPk() {
        return seriesPk;
    }

    public final void setSeriesPk(int seriesPk) {
        this.seriesPk = seriesPk;
    }

    public SeriesModel getSeries() {
        return FolderForm.getFolderForm(getCtx().getRequest())
                .getSeriesByPk(patPk, studyPk, seriesPk);
    }

}