/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.mbean;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.management.Notification;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.ejb.interfaces.ContentManager;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@tiani.com
 * @version $Revision: 1635 $ $Date: 2005-03-22 16:48:29 +0800 (周二, 22 3月 2005) $
 * @since 17.02.2005
 */
public class ContentEditService extends ServiceMBeanSupport {

    public static final String EVENT_TYPE = "org.dcm4chex.archive.mbean.ContentEditService";

    private ContentEdit ce;
	private ContentEdit contentEdit;
    private static Logger log = Logger.getLogger( ContentEditService.class.getName() );

	private ContentManager contentMgr;

	public ContentEditService() {
    }
    
    protected void startService() throws Exception {
    }

    protected void stopService() throws Exception {
    }

    public Dataset createPatient(Dataset ds) throws RemoteException, CreateException, HomeFactoryException {
    	if ( log.isDebugEnabled() ) log.debug("create Partient");
    	return lookupContentEdit().createPatient( ds );
    }
    
    public void mergePatients(Integer patPk, int[] mergedPks) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("merge Partient");
    	Collection col = lookupContentEdit().mergePatients( patPk.intValue(), mergedPks );
    	Iterator iter = col.iterator();
    	while ( iter.hasNext() ) {
    		sendContentEditNotification( (Dataset) iter.next() );
    	}
    	
    }
    
    public Dataset createStudy(Dataset ds, Integer patPk) throws CreateException, RemoteException, HomeFactoryException {
    	if ( log.isDebugEnabled() ) log.debug("create study");
    	return lookupContentEdit().createStudy( ds, patPk.intValue() );
    }
    
    public Dataset createSeries(Dataset ds, Integer studyPk) throws CreateException, RemoteException, HomeFactoryException {
    	if ( log.isDebugEnabled() ) log.debug("create Series");
    	return lookupContentEdit().createSeries( ds, studyPk.intValue() );
    }
    
    public void updatePatient(Dataset ds) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("update Partient");
    	Collection col = lookupContentEdit().updatePatient( ds );
    	Iterator iter = col.iterator();
    	while ( iter.hasNext() ) {
    		sendContentEditNotification( (Dataset) iter.next() );
    	}
    }
    
    public void updateStudy(Dataset ds) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("update Study");
    	Dataset dsN = lookupContentEdit().updateStudy( ds );
    	sendContentEditNotification( dsN );
    }
    
    public void updateSeries(Dataset ds) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("update Series");
    	Dataset dsN = lookupContentEdit().updateSeries( ds );
    	sendContentEditNotification( dsN );
    }
    
    public void deleteSeries(Integer seriesPk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("delete Series");
    	lookupContentEdit().deleteSeries( seriesPk.intValue() );
    }
    
    public void deleteStudy(Integer studyPk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("delete Study");
    	lookupContentEdit().deleteStudy( studyPk.intValue() );
    }
    
    public void deletePatient(Integer patPk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("delete Patient");
    	lookupContentEdit().deletePatient( patPk.intValue() );
    }    
    
    public void deleteInstance(Integer pk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("delete Instance");
    	lookupContentEdit().deleteInstance( pk.intValue() );
    }
    
    public void moveStudies(int[] study_pks, Integer patient_pk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("move Studies");
    	Collection col = lookupContentEdit().moveStudies( study_pks, patient_pk.intValue() );
    	Iterator iter = col.iterator();
    	while ( iter.hasNext() ) {
    		sendContentEditNotification( (Dataset) iter.next() );
    	}
    }   
    
    public void moveSeries(int[] series_pks, Integer study_pk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("move Series");
    	Dataset ds = lookupContentEdit().moveSeries( series_pks, study_pk.intValue() );
    	sendContentEditNotification( ds );
    }
    
    public void moveInstances(int[] instance_pks, Integer series_pk) throws RemoteException, HomeFactoryException, CreateException {
    	if ( log.isDebugEnabled() ) log.debug("move Instances");
    	Dataset ds = lookupContentEdit().moveInstances(instance_pks, series_pk.intValue() );
    	sendContentEditNotification( ds );
    }
    
   
    void sendContentEditNotification(Dataset ds) {
    	if ( log.isDebugEnabled() ) {
			log.debug( "Send ContentEdit notification! ds:"); 
			log.debug( ds );
    	}
        long eventID = super.getNextNotificationSequenceNumber();
        Notification notif = new Notification(EVENT_TYPE, this, eventID);
        notif.setUserData(ds);
        super.sendNotification(notif);
    }

    private ContentEdit lookupContentEdit() throws HomeFactoryException, RemoteException, CreateException  {
    	if ( contentEdit != null ) return contentEdit;
        ContentEditHome home = (ContentEditHome) EJBHomeFactory.getFactory()
                .lookup(ContentEditHome.class, ContentEditHome.JNDI_NAME);
        contentEdit = home.create();
        return contentEdit;
    }

 
}
