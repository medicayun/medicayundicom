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

import javax.ejb.CreateException;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.ejb.interfaces.ContentEdit;
import org.dcm4chex.archive.ejb.interfaces.ContentEditHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@tiani.com
 * @version $Revision: 1596 $ $Date: 2005-03-14 20:29:40 +0800 (周一, 14 3月 2005) $
 * @since 17.02.2005
 */
public class ContentEditService extends ServiceMBeanSupport {

	private ContentEdit ce;
	private ContentEdit contentEdit;
    private static Logger log = Logger.getLogger( ContentEditService.class.getName() );
	
	public ContentEditService() {
    }
    
    protected void startService() throws Exception {
    }

    protected void stopService() throws Exception {
    }

    public Dataset createPatient(Dataset ds) throws RemoteException, CreateException, HomeFactoryException {
    	log.info("create Partient");
    	return lookupContentEdit().createPatient( ds );
    }
    
    public void mergePatients(Integer patPk, int[] mergedPks) throws RemoteException, HomeFactoryException, CreateException {
    	log.info("merge Partient");
    	lookupContentEdit().mergePatients( patPk.intValue(), mergedPks );
    }
    
    public Dataset createStudy(Dataset ds, Integer patPk) throws CreateException, RemoteException, HomeFactoryException {
    	log.info("create study");
    	return lookupContentEdit().createStudy( ds, patPk.intValue() );
    }
    
    public Dataset createSeries(Dataset ds, Integer studyPk) throws CreateException, RemoteException, HomeFactoryException {
    	log.info("create Series");
    	return lookupContentEdit().createSeries( ds, studyPk.intValue() );
    }
    
    public void updatePatient(Dataset ds) throws RemoteException, HomeFactoryException, CreateException {
    	log.info("update Partient");
    	lookupContentEdit().updatePatient( ds );
    }
    
    public void updateStudy(Dataset ds) throws RemoteException, HomeFactoryException, CreateException {
    	log.info("update Study");
    	lookupContentEdit().updateStudy( ds );
    }
    
    public void updateSeries(Dataset ds) throws RemoteException, HomeFactoryException, CreateException {
    	log.info("update Series");
    	lookupContentEdit().updateSeries( ds );
    }
    
    public void deleteSeries(Integer seriesPk) throws RemoteException, HomeFactoryException, CreateException {
    	log.info("delete Series");
    	lookupContentEdit().deleteSeries( seriesPk.intValue() );
    }
    
    public void deleteStudy(Integer studyPk) throws RemoteException, HomeFactoryException, CreateException {
    	log.info("delete Study");
    	lookupContentEdit().deleteStudy( studyPk.intValue() );
    }
    
    public void deletePatient(Integer patPk) throws RemoteException, HomeFactoryException, CreateException {
    	log.info("delete Patient");
    	lookupContentEdit().deletePatient( patPk.intValue() );
    }    
    
    public void deleteInstance(Integer pk) throws RemoteException, HomeFactoryException, CreateException {
    	log.info("delete Instance");
    	lookupContentEdit().deleteInstance( pk.intValue() );
    }
    
    public void moveStudies(int[] study_pks, Integer patient_pk) throws RemoteException, HomeFactoryException, CreateException {
    	log.info("move Studies");
    	lookupContentEdit().moveStudies( study_pks, patient_pk.intValue() );
    }   
    
    public void moveSeries(int[] series_pks, Integer study_pk) throws RemoteException, HomeFactoryException, CreateException {
    	log.info("move Series");
    	lookupContentEdit().moveSeries( series_pks, study_pk.intValue() );
    }
    
    public void moveInstances(int[] instance_pks, Integer series_pk) throws RemoteException, HomeFactoryException, CreateException {
    	log.info("move Instances");
    	lookupContentEdit().moveInstances(instance_pks, series_pk.intValue() );
    }
    
    private ContentEdit lookupContentEdit() throws HomeFactoryException, RemoteException, CreateException  {
    	if ( contentEdit != null ) return contentEdit;
        ContentEditHome home = (ContentEditHome) EJBHomeFactory.getFactory()
                .lookup(ContentEditHome.class, ContentEditHome.JNDI_NAME);
        contentEdit = home.create();
        return contentEdit;
    }
    
}
