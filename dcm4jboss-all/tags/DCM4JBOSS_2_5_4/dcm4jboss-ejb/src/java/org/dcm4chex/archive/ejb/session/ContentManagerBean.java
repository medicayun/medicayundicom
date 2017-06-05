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

package org.dcm4chex.archive.ejb.session;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.MPPSLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;
import org.dcm4chex.archive.ejb.jdbc.QueryStudiesCmd;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 2010 $ $Date: 2005-10-07 03:55:27 +0800 (周五, 07 10月 2005) $
 * @since 14.01.2004
 * 
 * @ejb.bean name="ContentManager" type="Stateless" view-type="remote"
 *           jndi-name="ejb/ContentManager"
 * @ejb.transaction-type type="Container"
 * @ejb.transaction type="Required"

 * @ejb.ejb-ref ejb-name="Patient" view-type="local" ref-name="ejb/Patient" 
 * @ejb.ejb-ref ejb-name="Study" view-type="local" ref-name="ejb/Study" 
 * @ejb.ejb-ref ejb-name="Series" view-type="local" ref-name="ejb/Series" 
 * @ejb.ejb-ref ejb-name="Instance" view-type="local" ref-name="ejb/Instance" 
 */
public abstract class ContentManagerBean implements SessionBean {

    private static final int[] MPPS_FILTER_TAGS = { 
        Tags.PerformedStationAET, Tags.PerformedStationName,
        Tags.PPSStartDate, Tags.PPSStartTime, Tags.PPSEndDate,
        Tags.PPSEndTime, Tags.PPSStatus, Tags.PPSID,
        Tags.PPSDescription, Tags.PerformedProcedureTypeDescription,
        Tags.PerformedProtocolCodeSeq, Tags.ScheduledStepAttributesSeq, 
		Tags.PPSDiscontinuationReasonCodeSeq };
	
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private PatientLocalHome patHome;
    private StudyLocalHome studyHome;
    private SeriesLocalHome seriesHome;
    private InstanceLocalHome instanceHome;

    public void setSessionContext(SessionContext arg0)
        throws EJBException, RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome =
                (PatientLocalHome) jndiCtx.lookup("java:comp/env/ejb/Patient");
            studyHome =
                (StudyLocalHome) jndiCtx.lookup("java:comp/env/ejb/Study");
            seriesHome =
                (SeriesLocalHome) jndiCtx.lookup("java:comp/env/ejb/Series");
            instanceHome =
                (InstanceLocalHome) jndiCtx.lookup("java:comp/env/ejb/Instance");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetSessionContext() {
        patHome = null;
        studyHome = null;
        seriesHome = null;
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public Dataset getStudy(int studyPk) throws FinderException {
        return studyHome.findByPrimaryKey(new Integer(studyPk)).getAttributes(true);
    }
   
    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public Dataset getStudyByIUID(String studyIUID) throws FinderException {
        return studyHome.findByStudyIuid(studyIUID).getAttributes(true);
    }
    
    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public Dataset getSeries(int seriesPk) throws FinderException {
        return seriesHome.findByPrimaryKey(new Integer(seriesPk)).getAttributes(true);
    }
    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public Dataset getSeriesByIUID(String seriesIUID) throws FinderException {
        return seriesHome.findBySeriesIuid(seriesIUID).getAttributes(true);
    }

    /**
     * @ejb.interface-method
     */
    public int countStudies(Dataset filter, boolean showHidden, boolean hideWithoutStudies) {
        try {
            return new QueryStudiesCmd(filter, showHidden, hideWithoutStudies).count();
        } catch (SQLException e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public List listStudies(Dataset filter, boolean showHidden, boolean hideWithoutStudies, int offset, int limit) {
        try {
            return new QueryStudiesCmd(filter, showHidden, hideWithoutStudies).list(offset, limit);
        } catch (SQLException e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public List listStudiesOfPatient(int patientPk, boolean showHidden) throws FinderException {
        Collection c =
            patHome.findByPrimaryKey(new Integer(patientPk)).getStudies();
        List result = new ArrayList(c.size());
        StudyLocal study;
        for (Iterator it = c.iterator(); it.hasNext();) {
            study = (StudyLocal) it.next();
            if ( study.getHiddenSafe() == showHidden ) {
            	result.add(study.getAttributes(true));
            } else if (showHidden) { //are childs hidden?
            	if ( listSeriesOfStudy( study.getPk().intValue(), true).size() > 0 )
            		result.add(study.getAttributes(true));
            }
        }
        return result;
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public List listSeriesOfStudy(int studyPk, boolean showHidden) throws FinderException {
        Collection c =
            studyHome.findByPrimaryKey(new Integer(studyPk)).getSeries();
        List result = new ArrayList(c.size());
        SeriesLocal series;
        for (Iterator it = c.iterator(); it.hasNext();) {
            series = (SeriesLocal) it.next();
            if ( series.getHiddenSafe() == showHidden ) {
            	result.add( mergeMPPSAttr(series.getAttributes(true), series.getMpps()) );
	        } else if (showHidden) { //are childs hidden?
	        	if ( listInstancesOfSeries( series.getPk().intValue(), true).size() > 0 )
	        		result.add( mergeMPPSAttr(series.getAttributes(true), series.getMpps()) );
	        }
        }
        return result;
    }

    /**
	 * @param attributes
	 * @param ppsIuid
	 * @return
	 */
	private Dataset mergeMPPSAttr(Dataset ds, MPPSLocal mpps) {
		if ( mpps != null ) {
			ds.putAll( mpps.getAttributes().subSet(MPPS_FILTER_TAGS));
		}
		return ds;
	}

	/**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public List listInstancesOfSeries(int seriesPk, boolean showHidden) throws FinderException {
        Collection c =
            seriesHome.findByPrimaryKey(new Integer(seriesPk)).getInstances();
        List result = new ArrayList(c.size());
        InstanceLocal inst;
        for (Iterator it = c.iterator(); it.hasNext();) {
            inst = (InstanceLocal) it.next();
            if ( inst.getHiddenSafe() == showHidden )
            	result.add(inst.getAttributes(true));
        }
        return result;
    }

    /**
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public List listFilesOfInstance(int instancePk) throws FinderException {
        Collection c =
            instanceHome.findByPrimaryKey(new Integer(instancePk)).getFiles();
        List result = new ArrayList(c.size());
        for (Iterator it = c.iterator(); it.hasNext();) {
            FileLocal file = (FileLocal) it.next();
            result.add(file.getFileDTO());
        }
        return result;
    }
    
    /**
     * @throws FinderException
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public Dataset getSOPInstanceRefMacro( int studyPk, boolean insertModality ) throws FinderException {
    	Dataset ds = dof.newDataset();
    	StudyLocal sl = studyHome.findByPrimaryKey( new Integer( studyPk ) );
    	ds.putUI( Tags.StudyInstanceUID, sl.getStudyIuid() );
		DcmElement refSerSq = ds.putSQ(Tags.RefSeriesSeq);
		Iterator iterSeries = sl.getSeries().iterator();
		SeriesLocal series;
		String aet;
		int pos;
		while ( iterSeries.hasNext() ) {
			series = (SeriesLocal) iterSeries.next();
			Dataset serDS = refSerSq.addNewItem();
			serDS.putUI(Tags.SeriesInstanceUID, series.getSeriesIuid() );
			aet = series.getRetrieveAETs(); 
			pos = aet.indexOf('\\');
			if ( pos != -1 ) aet = aet.substring(0,pos);
			serDS.putAE( Tags.RetrieveAET, aet );
			serDS.putAE( Tags.StorageMediaFileSetID, series.getFilesetId() );
			serDS.putAE( Tags.StorageMediaFileSetUID, series.getFilesetIuid() );
			if ( insertModality ) {
				serDS.putCS( Tags.Modality, series.getModality() );
				serDS.putIS( Tags.SeriesNumber, series.getSeriesNumber() ); //Q&D 
			}
			DcmElement refSopSq = serDS.putSQ(Tags.RefSOPSeq);
			Collection col = series.getInstances();
			List l = ( col instanceof List ) ? (List)col : new ArrayList(col);
			Collections.sort( l, new InstanceNumberComparator() );
			Iterator iterInstances = l.iterator();
			InstanceLocal instance;
			while ( iterInstances.hasNext() ) {
				instance = (InstanceLocal) iterInstances.next();
				Dataset instDS = refSopSq.addNewItem();
				instDS.putUI( Tags.RefSOPInstanceUID, instance.getSopIuid() );
				instDS.putUI( Tags.RefSOPClassUID, instance.getSopCuid() );
			}
		} 
    	return ds;
    }
    
    /**
     * @throws FinderException
     *
     * @ejb.interface-method
     * @ejb.transaction type="Required"
     */
    public Dataset getPatientForStudy(int studyPk) throws FinderException {
    	StudyLocal sl = studyHome.findByPrimaryKey( new Integer( studyPk ) );
    	return sl.getPatient().getAttributes(false);
    }    
    
	public class InstanceNumberComparator implements Comparator {

		public InstanceNumberComparator() {
		}

		/**
		 * Compares the instance number of two InstanceLocal objects.
		 * <p>
		 * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer 
		 * as the first argument is less than, equal to, or greater than the second.
		 * <p>
		 * Throws an Exception if one of the arguments is null or neither a InstanceContainer or InstanceLocal object.<br>
		 * Also both arguments must be of the same type!
		 * <p>
		 * If arguments are of type InstanceLocal, the getInstanceSize Method of InstanceCollector is used to get filesize.
		 *  
		 * @param arg0 	First argument
		 * @param arg1	Second argument
		 * 
		 * @return <0 if arg0<arg1, 0 if equal and >0 if arg0>arg1
		 */
		public int compare(Object arg0, Object arg1) {
			InstanceLocal il1 = (InstanceLocal) arg0;
			InstanceLocal il2 = (InstanceLocal) arg1;
			return new Integer(il1.getInstanceNumber()).compareTo( new Integer(il2.getInstanceNumber()) );
		}
		
	}// end class
    
}
