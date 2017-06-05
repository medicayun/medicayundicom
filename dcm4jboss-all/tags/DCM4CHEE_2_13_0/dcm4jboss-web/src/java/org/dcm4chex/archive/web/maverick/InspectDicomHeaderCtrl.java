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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.PatientModel;
import org.dcm4chex.archive.web.maverick.model.StudyModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2659 $ $Date: 2006-07-28 16:55:02 +0200 (Fr, 28 Jul 2006) $
 * @since 5.10.2004
 *
 */
public class InspectDicomHeaderCtrl extends Dcm4cheeFormController {

	private long patPk = -1;
    private long studyPk = -1;
    private long seriesPk = -1;
    private long instancePk = -1;
    private boolean showAll;

    public final void setPatPk(long pk) {
        this.patPk = pk;
    }
    public final void setStudyPk(long pk) {
        this.studyPk = pk;
    }
    public final void setSeriesPk(long pk) {
        this.seriesPk = pk;
    }
    public final void setInstancePk(long pk) {
        this.instancePk = pk;
    }

    public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}
	protected String perform() throws Exception {
	    FolderForm  form = FolderForm.getFolderForm(getCtx());
	    Dataset ds = DcmObjectFactory.getInstance().newDataset();
	    StringBuffer sb = new StringBuffer();
	    ContentManager cm = null;
		try {
			if ( instancePk != -1 ) {
				ds.putAll(form.getInstanceByPk(patPk, studyPk, seriesPk, instancePk).toDataset());
				sb.append("INSTANCE,");
			}
			if ( ( showAll || ds.isEmpty() ) &&  seriesPk != -1 ) {
				ds.putAll(form.getSeriesByPk(patPk, studyPk, seriesPk).toDataset());
				sb.append("SERIES,");
			}
			if ( ( showAll || ds.isEmpty() ) &&  studyPk != -1 ) {
			        cm = lookupContentManager();
				ds.putAll( cm.getStudy(studyPk));
				sb.append("STUDY,");
			}
			if ( showAll || ds.isEmpty() ) {
			        if ( cm == null )
			            cm = lookupContentManager();
				ds.putAll(cm.getPatient(patPk));
				sb.append("PATIENT,");
			}
            getCtx().getRequest().getSession().setAttribute("dataset2view", ds);
            sb.setLength(sb.length()-1);
            getCtx().getRequest().getSession().setAttribute("titleOfdataset2view", 
            		form.formatMessage("folder.dicom_header", new String[]{sb.toString()}));
			return INSPECT;
		} catch ( Exception x ) {
			x.printStackTrace();
			form.setExternalPopupMsg("folder.err_inspect", getMsgAttrs());
			return ERROR;
		}
	}
	private String[] getMsgAttrs() {
		if ( true ) return new String[]{"pks:pat:"+patPk+", study:"+studyPk+", series:"+seriesPk+", instance:"+instancePk+", showAll:"+showAll};
		String[] msg = new String[2];
		if (instancePk != -1) {
			msg[0] = "INSTANCE";
			msg[1] = ( seriesPk == -1 || studyPk == -1 || patPk == -1) ? "Missing pk(s) of parent levels (series,study,patient)!" : "Instance not found";
		} else if (seriesPk != -1) {
			msg[0] = "SERIES";
			msg[1] = ( studyPk == -1 || patPk == -1) ? "Missing pk(s) of parent levels (study,patient)!" : "Series not found";
		} else if (studyPk != -1) {
			msg[0] = "STUDY";
			msg[1] = patPk == -1 ? "Missing pk of of parent level (patient)!" : "Study not found";
		} else {
			msg[0] = "PATIENT";
			msg[1] = patPk == -1 ? "Missing pk of patient!" : "Patient not found";
		}
		return msg;
	}
	
	private ContentManager lookupContentManager() throws Exception {
	    ContentManagerHome home = (ContentManagerHome) EJBHomeFactory
	         .getFactory().lookup(ContentManagerHome.class,
	               ContentManagerHome.JNDI_NAME);
	    return home.create();
	}
	
}