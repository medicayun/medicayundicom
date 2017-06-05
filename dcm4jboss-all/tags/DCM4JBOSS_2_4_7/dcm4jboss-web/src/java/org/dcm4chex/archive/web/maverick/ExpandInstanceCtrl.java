/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.InstanceModel;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1731 $ $Date: 2005-05-11 21:03:11 +0800 (周三, 11 5月 2005) $
 * @since 28.01.2004
 */
public class ExpandInstanceCtrl extends Dcm4JbossController {

    private int patPk;

    private int studyPk;

    private int seriesPk;
    
    private int instancePk;
    
    private boolean expand;

    public final void setPatPk(int patPk) {
        this.patPk = patPk;
    }

    public final void setSeriesPk(int seriesPk) {
        this.seriesPk = seriesPk;
    }

    public final void setStudyPk(int studyPk) {
        this.studyPk = studyPk;
    }

    public final void setInstancePk(int pk) {
        this.instancePk = pk;
    }
    
    public final void setExpand( boolean expand ) {
    	this.expand = expand;
    }
    
    protected String perform() throws Exception {
        try {
            FolderForm folderForm = FolderForm.getFolderForm(getCtx().getRequest());
            InstanceModel m = folderForm.getInstanceByPk(patPk,studyPk,seriesPk,instancePk);
        	if ( expand ) {
	            ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
	            	.getFactory().lookup(ContentManagerHome.class, ContentManagerHome.JNDI_NAME);
	            ContentManager cm = home.create();
	            if ( m != null ) m.setFiles(cm.listFilesOfInstance( instancePk ));
        	} else {
        		m.setFiles(null);
        	}
        } catch (Exception e) {
        }
        return SUCCESS;
    }

}