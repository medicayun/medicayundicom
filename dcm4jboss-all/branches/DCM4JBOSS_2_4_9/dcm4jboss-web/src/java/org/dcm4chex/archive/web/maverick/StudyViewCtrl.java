/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick;

import java.util.ArrayList;
import java.util.List;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.ejb.interfaces.ContentManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.web.maverick.model.PatientModel;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1823 $ $Date: 2005-06-27 20:42:06 +0800 (周一, 27 6月 2005) $
 * @since 5.10.2004
 *
 */
public class StudyViewCtrl extends Dcm4JbossController {
    private int patPk = -1;

    private int studyPk = -1;
    
    private int seriesPk = -1;
    
    private String patID = null;
    private String studyUID = null;
    private String seriesUID = null;
    
    private PatientModel patient;
    
    private StudyContainer study;

    private int selectedSeries = 1;

    public PatientModel getPatient() {
    	return patient;
    }
	/**
	 * @return Returns the sopIUIDs.
	 */
	public StudyContainer getStudyContainer() {
		return study;
	}
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
    
	/**
	 * @return Returns the seriesPk.
	 */
	public final int getSeriesPk() {
		return seriesPk;
	}
	/**
	 * @param seriesPk The seriesPk to set.
	 */
	public final void setSeriesPk(int seriesPk) {
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
    protected String perform() throws Exception { 
    	if ( studyPk < 0 ) studyPk = lookupContentManager().getStudyByIUID(studyUID).getInt(PrivateTags.StudyPk,-1);
    	patient = new PatientModel( lookupContentManager().getPatientForStudy( studyPk ) );
    	ContentManager cm = lookupContentManager();
    	Dataset sopIUIDs = cm.getSOPInstanceRefMacro( studyPk, true );
    	String serIUID = seriesPk < 0 ? seriesUID:cm.getSeries( this.seriesPk ).getString( Tags.SeriesInstanceUID );
    	study = new StudyContainer( sopIUIDs, serIUID );
       return SUCCESS;
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
    		for ( int i = 0, len = serSeq.vm() ; i < len ; i++ ) {
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
    		for ( int i = 0, len = refSopSq.vm() ; i < len ; i++ ) {
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