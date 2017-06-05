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
import org.dcm4che.dict.Tags;

import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.PatientModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1726 $ $Date: 2005-05-11 02:04:48 +0800 (周三, 11 5月 2005) $
 * @since 5.10.2004
 *
 */
public class KeyObjectViewCtrl extends Dcm4JbossController {
    private int studyPk;
    private int seriesPk;

    private String sopIUID;
    
    private PatientModel patient;
    
    public PatientModel getPatient() {
    	return patient;
    }
    
	/**
	 * @return Returns the studyPk.
	 */
	public int getStudyPk() {
		return studyPk;
	}
	/**
	 * @param studyPk The studyPk to set.
	 */
	public void setStudyPk(int studyPk) {
		this.studyPk = studyPk;
	}
	/**
	 * @return Returns the seriesPk.
	 */
	public int getSeriesPk() {
		return seriesPk;
	}
	/**
	 * @param seriesPk The seriesPk to set.
	 */
	public void setSeriesPk(int seriesPk) {
		this.seriesPk = seriesPk;
	}
	/**
	 * @return Returns the sopIUID.
	 */
	public String getSopIUID() {
		return sopIUID;
	}
	/**
	 * @param sopIUID The sopIUID to set.
	 */
	public void setSopIUID(String sopIUID) {
		this.sopIUID = sopIUID;
	}

    protected String perform() throws Exception {
    	ContentManager cm = lookupContentManager();
    	patient = new PatientModel( cm.getPatientForStudy( studyPk ) );
    	if ( sopIUID == null && seriesPk >= 0 ) {
    		List l = cm.listInstancesOfSeries( seriesPk );
    		if ( l != null && !l.isEmpty() ) {
    			sopIUID = ((Dataset) l.get(0)).getString( Tags.SOPInstanceUID);
    		}
    	}
        return SUCCESS;
    }
    
    private ContentManager lookupContentManager() throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory.getFactory()
                .lookup(ContentManagerHome.class, ContentManagerHome.JNDI_NAME);
        return home.create();
    }
    

}