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
import org.dcm4chex.archive.web.maverick.model.StudyModel;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1177 $ $Date: 2004-10-07 20:17:09 +0800 (周四, 07 10月 2004) $
 * @since 28.01.2004
 */
public class ExpandPatientCtrl extends Dcm4JbossController {

    private int patPk;

    public final void setPatPk(int patPk) {
        this.patPk = patPk;
    }

    protected String perform() throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
                .getFactory().lookup(ContentManagerHome.class,
                        ContentManagerHome.JNDI_NAME);
        ContentManager cm = home.create();
        try {
            FolderForm folderForm = FolderForm.getFolderForm(getCtx()
                    .getRequest());
            List studies = cm.listStudiesOfPatient(patPk);
            for (int i = 0, n = studies.size(); i < n; i++)
                studies.set(i, new StudyModel((Dataset) studies.get(i)));
            folderForm.getPatientByPk(patPk).setStudies(studies);
        } finally {
            try {
                cm.remove();
            } catch (Exception e) {
            }
        }
        return SUCCESS;
    }

}