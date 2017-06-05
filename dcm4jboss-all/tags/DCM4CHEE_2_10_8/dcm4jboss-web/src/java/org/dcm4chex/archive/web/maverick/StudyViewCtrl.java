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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.ejb.jdbc.QueryStudiesCmd;
import org.dcm4chex.archive.util.Convert;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.PatientModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2575 $ $Date: 2006-06-29 00:51:48 +0800 (周四, 29 6月 2006) $
 * @since 5.10.2004
 *
 */
public class StudyViewCtrl extends Dcm4cheeFormController {
	private static final String SELECT = "select";
	
	private long patPk = -1;
    private long studyPk = -1;
    private long seriesPk = -1;
    
    private String patID = null;
    private String studyUID = null;
    private String seriesUID = null;
	private String accNr = null;    
    
    private PatientModel patient;
    
    private StudyContainer study;

    private int selectedSeries = 1;
    
    protected static Logger log = Logger.getLogger(StudyViewCtrl.class);

    public PatientModel getPatient() {
    	return patient;
    }
	/**
	 * @return Returns the sopIUIDs.
	 */
	public StudyContainer getStudyContainer() {
		return study;
	}
    public final long getPatPk() {
        return patPk;
    }

    public final void setPatPk(long pk) {
        this.patPk = pk;
    }

    public final long getStudyPk() {
        return studyPk;
    }

    public final void setStudyPk(long pk) {
        this.studyPk = pk;
    }
    
	/**
	 * @return Returns the seriesPk.
	 */
	public final long getSeriesPk() {
		return seriesPk;
	}
	/**
	 * @param seriesPk The seriesPk to set.
	 */
	public final void setSeriesPk(long seriesPk) {
		this.seriesPk = seriesPk;
	}
	
    public int getSelectedSeries() {
    	return this.selectedSeries;
    }

	/**
	 * @return Returns the patID.
	 */
	public String getPatID() {
		return patID;
	}
	/**
	 * @param patID The patID to set.
	 */
	public void setPatID(String patID) {
		this.patID = patID;
	}
	/**
	 * @return Returns the seriesUID.
	 */
	public String getSeriesUID() {
		return seriesUID;
	}
	/**
	 * @param seriesUID The seriesUID to set.
	 */
	public void setSeriesUID(String seriesUID) {
		this.seriesUID = seriesUID;
	}
	/**
	 * @return Returns the studyUID.
	 */
	public String getStudyUID() {
		return studyUID;
	}
	/**
	 * @param studyUID The studyUID to set.
	 */
	public void setStudyUID(String studyUID) {
		this.studyUID = studyUID;
	}
	
	public void setAccNr(String accNr) {
		this.accNr = accNr;
	}
    protected String perform() throws Exception { 
    	if ( studyPk < 0 ) {
    		if ( calcStudyPk() > 1 ) return SELECT;
    	}
    	patient = new PatientModel( lookupContentManager().getPatientForStudy( studyPk ) );
    	ContentManager cm = lookupContentManager();
    	Dataset sopIUIDs = cm.getSOPInstanceRefMacro( studyPk, true );
    	String serIUID = seriesPk < 0 ? seriesUID:cm.getSeries( this.seriesPk ).getString( Tags.SeriesInstanceUID );
    	study = new StudyContainer( sopIUIDs, serIUID );
       return SUCCESS;
    }
    
    private int calcStudyPk() throws Exception {
    	if ( studyUID == null ) {
    		if ( seriesPk >= 0 ) {
    			log.debug("Try to get studyUID via seriesPk!");
        		studyUID = lookupContentManager().getSeries(seriesPk).getString(Tags.StudyInstanceUID);
    		} else if ( seriesUID != null ) {
    			log.debug("Try to get studyUID via seriesUID!");
        		studyUID = lookupContentManager().getSeriesByIUID(seriesUID).getString(Tags.StudyInstanceUID);
    		} else if ( accNr != null ) {
    			log.debug("Try to get studyUID via accession number!");
	    		Dataset queryDS = DcmObjectFactory.getInstance().newDataset();
	    		queryDS.putSH(Tags.AccessionNumber, accNr);
	    		queryDS.putUI(Tags.StudyInstanceUID);
	    		List l = new QueryStudiesCmd(queryDS, true, true).list(0, 10);
	    		if ( l.size() < 1 ) {
	    			log.warn("No study found for Accession Number:"+accNr);
	    			return 0;
	    		} 
	    		if ( l.size() > 1 ) {
	    			log.warn("More than one study found for Accession Number:"+accNr);
	    			FolderForm.getFolderForm(getCtx()).setStudies(l);
	    			return l.size();
	    		} 
	    		studyUID = ((Dataset) l.get(0)).getString( Tags.StudyInstanceUID );
    		} else {
    			log.warn("StudyView call need either studyPk, studyUID or accNr!");
    			return 0;
    		}
    	}
        ByteBuffer bb = lookupContentManager().getStudyByIUID(studyUID).getByteBuffer(PrivateTags.StudyPk);
        studyPk = bb == null ? -1 : Convert.toLong(bb.array());
    	return 1;
    }
    
    private ContentManager lookupContentManager() throws Exception {
        ContentManagerHome home = (ContentManagerHome) EJBHomeFactory.getFactory()
                .lookup(ContentManagerHome.class, ContentManagerHome.JNDI_NAME);
        return home.create();
    }
    
    
    public class StudyContainer {
    	private String studyIUID;
    	
    	private List series = new ArrayList();
    	
    	public StudyContainer( Dataset ds, String seriesIUID ) {
    		studyIUID = ds.getString( Tags.StudyInstanceUID );
    		DcmElement serSeq = ds.get( Tags.RefSeriesSeq );
    		SeriesContainer ser;
    		String mod;
    		for ( int i = 0, len = serSeq.countItems() ; i < len ; i++ ) {
    			ser = new SeriesContainer( serSeq.getItem(i));
    			if ( seriesIUID != null && ! seriesIUID.equals(ser.getSeriesIUID()) ) continue;
    			mod = ser.getModality();
				if ( !"SR".equals(mod) && !"PR".equals(mod) && !"KO".equals(mod) && !"AU".equals(mod) ) {
					series.add( ser );
				}
    		}
    	}
    	

    	public String getStudyIUID() {
    		return studyIUID;
    	}
    	public List getSeries() {
    		return series;
    	}
    }

    public class SeriesContainer {
    	private String seriesIUID;
    	private String modality;
    	private String seriesNumber;
    	
    	private List instanceUIDs = new ArrayList();
    	
    	public SeriesContainer( Dataset ds ) {
    		seriesIUID = ds.getString( Tags.SeriesInstanceUID );
    		modality = ds.getString( Tags.Modality );
    		seriesNumber = ds.getString( Tags.SeriesNumber );
			DcmElement refSopSq = ds.get(Tags.RefSOPSeq);
    		for ( int i = 0, len = refSopSq.countItems() ; i < len ; i++ ) {
    			instanceUIDs.add( refSopSq.getItem(i).getString( Tags.RefSOPInstanceUID ) );
    		}
    	}
    	
    	public String getSeriesIUID() {
    		return seriesIUID;
    	}
    	public String getSeriesNumber() {
    		return seriesNumber;
    	}
    	public String getModality() {
    		return modality;
    	}
    	public List getInstanceUIDs() {
    		return instanceUIDs;
    	}
    }

}