/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

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
 * @version $Revision: 2531 $ $Date: 2006-06-20 22:49:49 +0800 (周二, 20 6月 2006) $
 * @since 5.10.2004
 *
 */
public class KeyObjectViewCtrl extends Dcm4cheeFormController {
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