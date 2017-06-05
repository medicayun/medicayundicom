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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.ejb.conf.AttributeFilter;
import org.dcm4chex.archive.ejb.conf.ConfigurationException;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PrivateFileLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PrivateInstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.PrivateInstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PrivatePatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PrivatePatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PrivateSeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.PrivateSeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PrivateStudyLocal;
import org.dcm4chex.archive.ejb.interfaces.PrivateStudyLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * 
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 2531 $ $Date: 2006-06-20 22:49:49 +0800 (周二, 20 6月 2006) $
 * @since 27.12.2005
 * 
 * @ejb.bean
 *  name="PrivateManager"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/PrivateManager"
 * 
 * @ejb.transaction-type 
 *  type="Container"
 * 
 * @ejb.transaction 
 *  type="Required"
 * 
 * @ejb.ejb-ref
 *  ejb-name="Patient" 
 *  view-type="local"
 *  ref-name="ejb/Patient" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="Study" 
 *  view-type="local"
 *  ref-name="ejb/Study" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="Series" 
 *  view-type="local"
 *  ref-name="ejb/Series" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="Instance" 
 *  view-type="local"
 *  ref-name="ejb/Instance" 
 *  
 * @ejb.ejb-ref
 *  ejb-name="File" 
 *  view-type="local"
 *  ref-name="ejb/File" 
 *  
 * @ejb.ejb-ref
 *  ejb-name="PrivatePatient" 
 *  view-type="local"
 *  ref-name="ejb/PrivatePatient" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="PrivateStudy" 
 *  view-type="local"
 *  ref-name="ejb/PrivateStudy" 
 * 
 * @ejb.ejb-ref
 *  ejb-name="PrivateSeries" 
 *  view-type="local"
 *  ref-name="ejb/PrivateSeries" 
 *  
 * @ejb.ejb-ref
 *  ejb-name="PrivateInstance" 
 *  view-type="local"
 *  ref-name="ejb/PrivateInstance" 
 *  
 * @ejb.ejb-ref
 *  ejb-name="PrivateFile" 
 *  view-type="local"
 *  ref-name="ejb/PrivateFile" 
 *  
 * @ejb.env-entry name="AttributeFilterConfigURL" type="java.lang.String"
 *                value="resource:dcm4chee-attribute-filter.xml"
 *  
 */
public abstract class PrivateManagerBean implements SessionBean {

	private static final int DELETED = 1;
	
    private PatientLocalHome patHome;
    private StudyLocalHome studyHome;
    private SeriesLocalHome seriesHome;
    private InstanceLocalHome instHome;

    private PrivatePatientLocalHome privPatHome;
    private PrivateStudyLocalHome privStudyHome;
    private PrivateSeriesLocalHome privSeriesHome;
    private PrivateInstanceLocalHome privInstHome;
    private PrivateFileLocalHome privFileHome;

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    
    private AttributeFilter attrFilter;
    private static Logger log = Logger.getLogger( PrivateManagerBean.class.getName() );

    public void setSessionContext(SessionContext arg0) throws EJBException,
            RemoteException {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            patHome = (PatientLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Patient");
            studyHome = (StudyLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Study");
            seriesHome = (SeriesLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Series");
            instHome = (InstanceLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Instance");

            privPatHome = (PrivatePatientLocalHome) jndiCtx
            		.lookup("java:comp/env/ejb/PrivatePatient");
            privStudyHome = (PrivateStudyLocalHome) jndiCtx
            		.lookup("java:comp/env/ejb/PrivateStudy");
            privSeriesHome = (PrivateSeriesLocalHome) jndiCtx
            		.lookup("java:comp/env/ejb/PrivateSeries");
            privInstHome = (PrivateInstanceLocalHome) jndiCtx
    		.lookup("java:comp/env/ejb/PrivateInstance");
            privFileHome = (PrivateFileLocalHome) jndiCtx
    		.lookup("java:comp/env/ejb/PrivateFile");
            
            attrFilter = new AttributeFilter((String) jndiCtx
                    .lookup("java:comp/env/AttributeFilterConfigURL"));
        } catch (NamingException e) {
            throw new EJBException(e);
        } catch (ConfigurationException e) {
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
        instHome = null;
        privPatHome = null;
        privStudyHome = null;
        privSeriesHome = null;
        privInstHome = null;
        privFileHome = null;
    }

    
    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public void deletePrivateSeries(long series_pk) throws RemoteException, FinderException {
        try {
        	PrivateSeriesLocal series = privSeriesHome.findByPrimaryKey(new Long(series_pk));
    		PrivateStudyLocal study = series.getStudy();
    		series.remove();
    		if ( study.getSeries().isEmpty()) {
    			PrivatePatientLocal pat = study.getPatient();
    			study.remove();
    			if( pat.getStudies().isEmpty()) {
    				pat.remove();
    			}
    		}
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public Collection deletePrivateStudy(long study_pk) throws RemoteException, FinderException {
        try {
    		PrivateStudyLocal study = privStudyHome.findByPrimaryKey(new Long(study_pk));
        	ArrayList files = null;
			PrivatePatientLocal pat = study.getPatient();
			study.remove();
			if( pat.getStudies().isEmpty()) {
				pat.remove();
			}
			return files;
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
         }
    }


	/**
     * @ejb.interface-method
     */
    public void deletePrivatePatient(long patient_pk) throws RemoteException {
        try {
        	privPatHome.remove( new Long(patient_pk) );
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
		}
    }
    
    /**
     * @throws FinderException
     * @throws FinderException
     * @ejb.interface-method
     */
    public void deletePrivateInstance(long instance_pk) throws RemoteException, FinderException {
        try {
        	PrivateInstanceLocal instance = privInstHome.findByPrimaryKey(new Long(instance_pk));
        	PrivateSeriesLocal series = instance.getSeries();
        	instance.remove();
        	if ( series.getInstances().isEmpty() ) {
        		PrivateStudyLocal study = series.getStudy();
        		series.remove();
        		if ( study.getSeries().isEmpty()) {
        			PrivatePatientLocal pat = study.getPatient();
        			study.remove();
        			if( pat.getStudies().isEmpty()) {
        				pat.remove();
        			}
        		}
        	}
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        }
    }
    
    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public void deletePrivateFile(long file_pk) throws RemoteException {
        try {
        	privFileHome.remove(new Long(file_pk));
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        }
    }
    /**
     * @throws FinderException
     * @ejb.interface-method
     */
    public void deletePrivateFiles(Collection fileDTOs) throws RemoteException {
        try {
        	for ( Iterator iter = fileDTOs.iterator() ; iter.hasNext() ; ) {
        		privFileHome.remove(new Long( ((FileDTO) iter.next()).getPk()));
        	}
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        }
    }
    

    /**
     * @ejb.interface-method
     */
    public Dataset moveInstanceToTrash(long instance_pk) throws RemoteException {
        try {
            InstanceLocal instance = instHome.findByPrimaryKey(new Long(
                    instance_pk));
            SeriesLocal series = instance.getSeries();
            Collection colSeries = new ArrayList(); colSeries.add( series );
            Collection colInstance = new ArrayList(); colInstance.add( instance );
        	Dataset ds = getStudyMgtDataset( series.getStudy(), colSeries, colInstance );
            PrivateInstanceLocal privInstance =getPrivateInstance( instance, DELETED, null );
            instance.remove();
            series.updateDerivedFields(true, true, true, true, true);
            series.getStudy().updateDerivedFields(true, true, true, true, true, true);
            return ds;
        } catch (CreateException e) {
            throw new RemoteException(e.getMessage());
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
		}
    }

    /**
     * @ejb.interface-method
     */
    public Dataset moveSeriesToTrash(long series_pk) throws RemoteException {
        try {
            SeriesLocal series = seriesHome.findByPrimaryKey(new Long(series_pk));
            StudyLocal study = series.getStudy();
            Collection colSeries = new ArrayList(); colSeries.add( series );
            Collection colInstance = series.getInstances();
        	Dataset ds = getStudyMgtDataset( series.getStudy(), colSeries, colInstance );
            PrivateSeriesLocal privSeries =getPrivateSeries( series, DELETED, null, true );
            series.remove();
            study.updateDerivedFields(true, true, true, true, true, true);
            return ds;
        } catch (CreateException e) {
            throw new RemoteException(e.getMessage());
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
		}
    }

    /**
     * @ejb.interface-method
     */
    public Collection moveSeriesOfPPSToTrash(String ppsIUID, boolean removeEmptyParents) throws RemoteException {
        Collection result = new ArrayList(); // FIXME: NOT IN USE
        try {
        	Object[] ppsSeries = seriesHome.findByPpsIuid(ppsIUID).toArray();
        	if ( ppsSeries.length > 0 ) {
	        	SeriesLocal series = null;
	        	StudyLocal study = ((SeriesLocal)ppsSeries[0]).getStudy();
	        	for ( int i = 0 ; i < ppsSeries.length ; i++ ) {
	        		series = (SeriesLocal) ppsSeries[i];
	        		getPrivateSeries( series, DELETED, null, true );
		        	series.remove();
	        	}
	        	if ( removeEmptyParents && study.getSeries().isEmpty() ) {
        			study.remove();
	        	} else {
	        		study.updateDerivedFields(true,true,true,true,true,true);
	        	}
        	}
        } catch (FinderException ignore) {
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
		} 
        return result;
    }
    
    /**
     * @ejb.interface-method
     */
    public Dataset moveStudyToTrash(long study_pk) throws RemoteException {
        try {
            StudyLocal study = studyHome.findByPrimaryKey(new Long(study_pk));
            Collection colSeries = study.getSeries();
        	Dataset ds = getStudyMgtDataset( study, colSeries, null );
            PrivateStudyLocal privStudy =getPrivateStudy( study, DELETED, null, true );
            PatientLocal patient = study.getPatient();
            study.remove();
            return ds;
        } catch (CreateException e) {
            throw new RemoteException(e.getMessage());
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
		}
    }

    /**
     * @ejb.interface-method
     */
    public Dataset movePatientToTrash(long pat_pk) throws RemoteException {
        try {
        	PatientLocal patient = patHome.findByPrimaryKey(new Long(pat_pk));
        	Dataset ds = patient.getAttributes(true);
            PrivatePatientLocal privPat =getPrivatePatient( patient, DELETED, true );
            patient.remove();
            return ds;
        } catch (CreateException e) {
            throw new RemoteException(e.getMessage());
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
		}
    }
    
    private PrivateInstanceLocal getPrivateInstance( InstanceLocal instance, int type, PrivateSeriesLocal privSeries) throws FinderException, CreateException {
    	 Collection col = privInstHome.findBySopIuid( type, instance.getSopIuid() );
    	 PrivateInstanceLocal privInstance;
    	 if ( col.isEmpty() ) {
    	 	if ( privSeries == null ) {
    	 		privSeries = getPrivateSeries( instance.getSeries(), type, null, false );
    	 	}
    	 	privInstance = privInstHome.create(type, instance.getAttributes(true), privSeries );
    	 } else {
    	 	privInstance = (PrivateInstanceLocal) col.iterator().next();
    	 }
    	 Object[] files = instance.getFiles().toArray();
         FileLocal file;
         for ( int i = 0 ; i < files.length ; i++ ) {
         	file = (FileLocal) files[i];
         	privFileHome.create(file.getFilePath(), file.getFileTsuid(), 
         			file.getFileSize(),file.getFileMd5(),file.getFileStatus(), 
						privInstance, file.getFileSystem() );
         	try {
				file.remove();
			} catch (Exception x) {
				log.warn("Can not remove File record:"+file, x);
			}
         }
         return privInstance;
    }
    private PrivateSeriesLocal getPrivateSeries( SeriesLocal series, int type, PrivateStudyLocal privStudy, boolean includeInstances ) throws FinderException, CreateException {
    	Collection col = privSeriesHome.findBySeriesIuid( type, series.getSeriesIuid() );
    	PrivateSeriesLocal privSeries;
    	if ( col.isEmpty() ) { 
    	 	if ( privStudy == null ) {
    	 		privStudy = getPrivateStudy(series.getStudy(), type, null, false);
    	 	}
    	 	privSeries = privSeriesHome.create(type, series.getAttributes(true), privStudy );
    	} else {
    		privSeries = (PrivateSeriesLocal) col.iterator().next();
    	}
    	if ( includeInstances ) {
	        for ( Iterator iter = series.getInstances().iterator() ; iter.hasNext() ; ) {
	        	getPrivateInstance((InstanceLocal) iter.next(), type, privSeries);//move also all instances
	        }
    	}
    	return privSeries;
    }
    private PrivateStudyLocal getPrivateStudy( StudyLocal study, int type, PrivatePatientLocal privPat, boolean includeSeries ) throws FinderException, CreateException {
    	Collection col = privStudyHome.findByStudyIuid( type, study.getStudyIuid() );
    	PrivateStudyLocal privStudy;
    	if ( col.isEmpty() ) { 
    	 	if ( privPat == null ) {
    	 		privPat = getPrivatePatient(study.getPatient(), type, false);
    	 	}
    	 	privStudy = privStudyHome.create(type, study.getAttributes(true), privPat );
    	} else {
    		privStudy = (PrivateStudyLocal) col.iterator().next();
    	}
    	if ( includeSeries ) {
	        for ( Iterator iter = study.getSeries().iterator() ; iter.hasNext() ; ) {
	        	getPrivateSeries((SeriesLocal) iter.next(), type, privStudy, true);//move also all instances
	        }
    	}
    	return privStudy;
    }
    
    private PrivatePatientLocal getPrivatePatient( PatientLocal patient, int type, boolean includeStudies ) throws FinderException, CreateException {
     	 Collection col = privPatHome.findByPatientIdWithIssuer( type, patient.getPatientId(), patient.getIssuerOfPatientId() );
     	 PrivatePatientLocal privPat;
     	 if ( col.isEmpty() ) { 
     	 	privPat = privPatHome.create(type, patient.getAttributes(true) );
     	 } else {
     	 	privPat = (PrivatePatientLocal) col.iterator().next();
     	 }
     	if ( includeStudies ) {
	        for ( Iterator iter = patient.getStudies().iterator() ; iter.hasNext() ; ) {
	        	getPrivateStudy((StudyLocal) iter.next(), type, privPat, true);//move also all instances
	        }
    	}
     	 return privPat;
   }
    
    private Dataset getStudyMgtDataset( StudyLocal study, Collection series, Collection instances ) {
    	Dataset ds = dof.newDataset();
    	ds.putUI( Tags.StudyInstanceUID, study.getStudyIuid() );
    	log.debug("getStudyMgtDataset: studyIUID:"+study.getStudyIuid());
		DcmElement refSeriesSeq = ds.putSQ( Tags.RefSeriesSeq );
		Iterator iter = series.iterator();
		while ( iter.hasNext() ) {
			SeriesLocal sl = (SeriesLocal) iter.next();
			Dataset dsSer = refSeriesSeq.addNewItem();
			dsSer.putUI( Tags.SeriesInstanceUID, sl.getSeriesIuid() );
			Collection colInstances = ( instances != null && series.size() == 1 ) ? instances : sl.getInstances();
			Iterator iter2 = colInstances.iterator();
			DcmElement refSopSeq = null;
			if ( iter2.hasNext() )
				refSopSeq = dsSer.putSQ( Tags.RefSOPSeq );
			while ( iter2.hasNext() ) {
				InstanceLocal il = (InstanceLocal) iter2.next();
				Dataset dsInst = refSopSeq.addNewItem();
				dsInst.putUI( Tags.RefSOPClassUID, il.getSopCuid() );
				dsInst.putUI( Tags.RefSOPInstanceUID, il.getSopIuid() );
				dsInst.putAE( Tags.RetrieveAET, il.getRetrieveAETs() );
			}
		}
		if ( log.isDebugEnabled() ) { log.debug("return StgMgtDataset:");log.debug(ds);}
    	return ds;
    }
}