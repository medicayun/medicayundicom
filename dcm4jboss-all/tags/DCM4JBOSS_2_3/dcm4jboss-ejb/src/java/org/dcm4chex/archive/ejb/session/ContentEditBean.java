/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocalHome;
import org.dcm4chex.archive.ejb.interfaces.PatientLocal;
import org.dcm4chex.archive.ejb.interfaces.PatientLocalHome;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;
import org.dcm4chex.archive.ejb.interfaces.StudyLocalHome;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1701 $ $Date: 2005-04-15 20:52:14 +0800 (周五, 15 4月 2005) $
 * @since 14.01.2004
 * 
 * @ejb.bean
 *  name="ContentEdit"
 *  type="Stateless"
 *  view-type="remote"
 *  jndi-name="ejb/ContentEdit"
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
 */
public abstract class ContentEditBean implements SessionBean {

    private PatientLocalHome patHome;

    private StudyLocalHome studyHome;

    private SeriesLocalHome seriesHome;

    private InstanceLocalHome instHome;
    
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
    

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
        instHome = null;
    }

    /**
     * @throws CreateException
     * @ejb.interface-method
     */
    public Dataset createPatient(Dataset ds) throws CreateException {
        return patHome.create(ds).getAttributes(true);
    }

    /**
     * @ejb.interface-method
     */
    public Collection mergePatients(int patPk, int[] mergedPks) {
    	try {
	        PatientLocal dominant = patHome.findByPrimaryKey(new Integer(patPk));
	        ArrayList list = new ArrayList();
	        for (int i = 0; i < mergedPks.length; i++) {
	            if ( patPk == mergedPks[i] ) continue;
	            PatientLocal priorPat = patHome.findByPrimaryKey(new Integer(mergedPks[i]));
            	list.addAll(priorPat.getStudies());
	            dominant.getStudies().addAll(priorPat.getStudies());
	            dominant.getMpps().addAll(priorPat.getMpps());
	            dominant.getMwlItems().addAll(priorPat.getMwlItems());                
                dominant.getGsps().addAll(priorPat.getGsps());
	            priorPat.setMergedWith(dominant);
            }
	        ArrayList col = new ArrayList();
            Iterator iter = list.iterator();
            StudyLocal sl;
            while ( iter.hasNext() ) {
            	sl = (StudyLocal) iter.next();
            	col.add( getNotificationDataset( sl, sl.getSeries(), null ) );
            }
            return col;
        } catch (FinderException e) {
            throw new EJBException(e);
        }        
    }

    /**
     * @throws CreateException
     * @ejb.interface-method
     */
    public Dataset createStudy(Dataset ds, int patPk) throws CreateException {
    	try {
	        PatientLocal patient = patHome.findByPrimaryKey(new Integer(patPk));
	        return studyHome.create(ds, patient).getAttributes(true);
        } catch (FinderException e) {
            throw new EJBException(e);
        }
	        
    }
    
    /**
     * @throws CreateException
     * @ejb.interface-method
     */
    public Dataset createSeries(Dataset ds, int studyPk) throws CreateException {
    	try {
	        StudyLocal study = studyHome.findByPrimaryKey(new Integer(studyPk));
	        return seriesHome.create(ds, study).getAttributes(true);
        } catch (FinderException e) {
            throw new EJBException(e);
        }
	        
    }
    
    /**
     * @ejb.interface-method
     */
    public Collection updatePatient(Dataset ds) {

        try {
        	Collection col = new ArrayList();
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            final int pk = ds.getInt(PrivateTags.PatientPk, -1);
            PatientLocal patient = patHome
                    .findByPrimaryKey(new Integer(pk));
            patient.setAttributes(ds);
            Collection studies = patient.getStudies();
            Iterator iter = patient.getStudies().iterator();
            StudyLocal sl;
            while ( iter.hasNext() ) {
            	sl = (StudyLocal) iter.next();
            	col.add( getNotificationDataset( sl, sl.getSeries(), null ) );
            }
            return col;
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }

    /**
     * @ejb.interface-method
     */
    public Dataset updateStudy(Dataset ds) {

        try {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            final int pk = ds.getInt(PrivateTags.StudyPk, -1);
            StudyLocal study = studyHome
                    .findByPrimaryKey(new Integer(pk));
            study.setAttributes(ds);
            return getNotificationDataset( study, study.getSeries(), null );            
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }
    
    /**
     * @ejb.interface-method
     */
    public Dataset updateSeries(Dataset ds) {

        try {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            final int pk = ds.getInt(PrivateTags.SeriesPk, -1);
            SeriesLocal series = seriesHome
                    .findByPrimaryKey(new Integer(pk));
            series.setAttributes(ds);
            StudyLocal study = series.getStudy();
            study.updateDerivedFields(false, false, false, false, false, true);
            Collection col = new ArrayList(); col.add( series );
            return getNotificationDataset( study, col, null );            
        } catch (FinderException e) {
            throw new EJBException(e);
        }
    }
    
    /**
     * @ejb.interface-method
     */
    public void deleteSeries(int series_pk) throws RemoteException {
        try {
            SeriesLocal series = seriesHome.findByPrimaryKey(new Integer(
                    series_pk));
            StudyLocal study = series.getStudy();
            series.remove();
            study.updateDerivedFields(true, true, true, true, true, true);
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * @ejb.interface-method
     */
    public void deleteStudy(int study_pk) throws RemoteException {
        try {
            studyHome.remove(new Integer(study_pk));
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * @ejb.interface-method
     */
    public void deletePatient(int patient_pk) throws RemoteException {
        try {
            patHome.remove(new Integer(patient_pk));
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * @ejb.interface-method
     */
    public void deleteInstance(int instance_pk) throws RemoteException {
        try {
            InstanceLocal instance = instHome.findByPrimaryKey(new Integer(
                    instance_pk));
            SeriesLocal series = instance.getSeries();
            instance.remove();
            series.updateDerivedFields(true, true, true, true, true);
            series.getStudy().updateDerivedFields(true, true, true, true, true, true);
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (RemoveException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
            throw new RemoteException(e.getMessage());
        }
    }
    
    /**
     * @ejb.interface-method
     */
    public Collection moveStudies(int[] study_pks, int patient_pk)
    		throws RemoteException {
        try {
        	Collection col = new ArrayList();
            PatientLocal pat = patHome.findByPrimaryKey(new Integer(
                    patient_pk));
            Collection studies = pat.getStudies();
            for (int i = 0; i < study_pks.length; i++) {
                StudyLocal study = studyHome.findByPrimaryKey(new Integer(
                        study_pks[i]));
                PatientLocal oldPat = study.getPatient();
                if (oldPat.isIdentical(pat)) continue;
                studies.add(study);        
                col.add( getNotificationDataset( study, study.getSeries(), null ) );
                
            }
            return col;
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    
    /**
     * @ejb.interface-method
     */
    public Dataset moveSeries(int[] series_pks, int study_pk)
    		throws RemoteException {
        try {
            StudyLocal study = studyHome.findByPrimaryKey(new Integer(
                    study_pk));
            Collection seriess = study.getSeries();
            Collection movedSeriess = new ArrayList();
            for (int i = 0; i < series_pks.length; i++) {
                SeriesLocal series = seriesHome.findByPrimaryKey(new Integer(
                        series_pks[i]));
                StudyLocal oldStudy = series.getStudy();
                if (oldStudy.isIdentical(study)) continue;
                seriess.add(series);                
                movedSeriess.add( series );
                oldStudy.updateDerivedFields(true, true, true, true, true, true);
            }
            study.updateDerivedFields(true, true, true, true, true, true);
            return getNotificationDataset( study, movedSeriess, null );
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
            throw new RemoteException(e.getMessage());
        }
    }
    
    /**
     * @ejb.interface-method
     */
    public Dataset moveInstances(int[] instance_pks, int series_pk)
    		throws RemoteException {
        try {
            SeriesLocal series = seriesHome.findByPrimaryKey(new Integer(
                    series_pk));
            Collection instances = series.getInstances();
            for (int i = 0; i < instance_pks.length; i++) {
                InstanceLocal instance = instHome.findByPrimaryKey(new Integer(
                        instance_pks[i]));
                SeriesLocal oldSeries = instance.getSeries();
                if (oldSeries.isIdentical(series)) continue;
                instances.add(instance);                
                oldSeries.updateDerivedFields(true, true, true, true, true);
                oldSeries.getStudy().updateDerivedFields(true, true, true, true, true, true);
            }
            series.updateDerivedFields(true, true, true, true, true);
            series.getStudy().updateDerivedFields(true, true, true, true, true, true);
            Collection col = new ArrayList(); col.add( series );
            return getNotificationDataset( series.getStudy(), col, instances );
        } catch (EJBException e) {
            throw new RemoteException(e.getMessage());
        } catch (FinderException e) {
            throw new RemoteException(e.getMessage());
        }
    }
    
    private Dataset getNotificationDataset( StudyLocal study, Collection series, Collection instances ) {
    	Dataset ds = dof.newDataset();
    	ds.putUI( Tags.StudyInstanceUID, study.getStudyIuid() );
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
    	
    	return ds;
    }
}