/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import java.util.List;

import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.InstanceModel;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
 * @since 28.01.2004
 */
public class ExpandSeriesCtrl extends Dcm4JbossController {

    private int patPk;

    private int studyPk;

    private int seriesPk;

    public final void setPatPk(int patPk) {
        this.patPk = patPk;
    }

    public final void setSeriesPk(int seriesPk) {
        this.seriesPk = seriesPk;
    }

    public final void setStudyPk(int studyPk) {
        this.studyPk = studyPk;
    }

    protected String perform() throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
                .getFactory().lookup(ContentManagerHome.class,
                        ContentManagerHome.JNDI_NAME);
        ContentManager cm = home.create();
        try {
            FolderForm folderForm = FolderForm.getFolderForm(getCtx()
                    .getRequest());
            List instances = cm.listInstancesOfSeries(seriesPk);
            for (int i = 0, n = instances.size(); i < n; i++)
                instances.set(i, InstanceModel.valueOf((Dataset) instances
                        .get(i)));
            folderForm.getSeriesByPk(patPk, studyPk, seriesPk)
                    .setInstances(instances);
        } finally {
            try {
                cm.remove();
            } catch (Exception e) {
            }
        }
        return SUCCESS;
    }

}